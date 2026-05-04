package com.example.flixr.auth

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.example.flixr.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Central place for:
 * - Firebase Auth (email/password + Google)
 * - Firestore profile reads/writes
 * - Unique username enforcement via `usernames/{username}` mapping documents
 *
 * Firestore layout:
 * - users/{uid}                 -> profile fields
 * - usernames/{normalizedName}  -> { uid: "<auth uid>" }  (acts as a uniqueness lock)
 */
class AuthRepository(
    private val appContext: Context,
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val storage: FirebaseStorage = FirebaseStorage.getInstance(),
) {
    private fun usersCollection() = db.collection("users")
    private fun usernamesCollection() = db.collection("usernames")

    fun observeAuthState(): Flow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            // `null` means signed out.
            trySend(firebaseAuth.currentUser)
        }
        auth.addAuthStateListener(listener)

        // Emit the current value immediately (important for first composition).
        trySend(auth.currentUser)

        awaitClose { auth.removeAuthStateListener(listener) }
    }

    fun webClientId(): String = appContext.getString(R.string.default_web_client_id).trim()

    fun isWebClientIdConfigured(): Boolean {
        val id = webClientId()
        return id.isNotBlank() && !id.equals("REPLACE_ME_WEB_CLIENT_ID.apps.googleusercontent.com", ignoreCase = true)
    }

    /**
     * Build the Google Sign-In intent.
     *
     * NOTE: For Firebase Auth + Google, you must pass the **Web client ID** into
     * `requestIdToken(...)`. This is not the Android OAuth client id.
     */
    fun buildGoogleSignInIntent(): Intent {
        val webClientId = webClientId()
        check(isWebClientIdConfigured()) {
            "Google Sign-In is not configured yet. Set `default_web_client_id` in strings.xml " +
                "(Web OAuth client id from Firebase) and re-download google-services.json after enabling Google Sign-In."
        }

        val gso =
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(webClientId)
                .requestEmail()
                .build()

        return GoogleSignIn.getClient(appContext, gso).signInIntent
    }

    /**
     * Finish Google Sign-In after the user returns from the account picker activity.
     */
    suspend fun completeGoogleSignIn(data: Intent?) {
        val task = GoogleSignIn.getSignedInAccountFromIntent(data)
        val account = task.getResult(ApiException::class.java)

        val idToken =
            account.idToken ?: error("Google account did not return an idToken (check Web client id / SHA config).")

        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential).await()
    }

    /**
     * Sign out of BOTH FirebaseAuth and GoogleSignIn.
     *
     * Why both?
     * - If you only sign out Firebase, the next Google prompt may "silently" pick the same Google account.
     */
    suspend fun signOutEverywhere() {
        auth.signOut()
        if (isWebClientIdConfigured()) {
            val gso =
                GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(webClientId())
                    .requestEmail()
                    .build()
            GoogleSignIn.getClient(appContext, gso).signOut().await()
        }
    }

    suspend fun getUserProfileOrNull(uid: String): UserProfile? {
        val snap = usersCollection().document(uid).get().await()
        if (!snap.exists()) return null
        val data = snap.data ?: return null
        return UserProfile.fromFirestore(data, fallbackUid = uid)
    }

    /**
     * Uploads the image to Firebase Storage and returns its download URL.
     */
    suspend fun uploadProfilePicture(uid: String, imageUri: Uri): String {
        val ref =
            storage.reference
                .child("profile_pictures")
                .child(uid)
                .child("${System.currentTimeMillis()}.jpg")

        ref.putFile(imageUri).await()
        return ref.downloadUrl.await().toString()
    }

    /**
     * Update profile fields in Firestore (and username mapping if username changes).
     *
     * Firestore layout:
     * - users/{uid}
     * - usernames/{normalized} -> { uid }
     *
     * Username uniqueness is enforced in a transaction.
     */
    suspend fun updateUserProfile(
        uid: String,
        currentUsername: String,
        newUsernameRaw: String?,
        newBio: String?,
        newProfilePictureUrl: String?,
    ) {
        val targetBio = newBio?.trim()
        val currentNorm = Username.normalize(currentUsername)

        val newUsernameNorm =
            newUsernameRaw
                ?.trim()
                ?.takeIf { it.isNotBlank() }
                ?.let { Username.normalize(it) }

        if (newUsernameNorm != null) Username.validateOrThrow(newUsernameNorm)

        val userRef = usersCollection().document(uid)
        val currentUsernameRef = usernamesCollection().document(currentNorm)
        val newUsernameRef = newUsernameNorm?.let { usernamesCollection().document(it) }

        db.runTransaction { tx ->
            if (newUsernameRef != null && newUsernameNorm != currentNorm) {
                val newSnap = tx.get(newUsernameRef)
                if (newSnap.exists()) {
                    val existingUid = newSnap.getString("uid").orEmpty()
                    if (existingUid.isNotBlank() && existingUid != uid && existingUid != "__RESERVED__") {
                        throw UsernameTakenException("That username is already taken.")
                    }
                }
                tx.set(newUsernameRef, mapOf("uid" to uid, "created_at" to Timestamp.now()), SetOptions.merge())
                // Best-effort cleanup of the old mapping (only if it points to this user).
                val oldSnap = tx.get(currentUsernameRef)
                val oldUid = oldSnap.getString("uid").orEmpty()
                if (oldSnap.exists() && oldUid == uid) {
                    tx.delete(currentUsernameRef)
                }
            }

            val updates = linkedMapOf<String, Any?>()
            if (newUsernameNorm != null && newUsernameNorm != currentNorm) updates["username"] = newUsernameNorm
            if (targetBio != null) updates["bio"] = targetBio
            if (newProfilePictureUrl != null) updates["profile_picture"] = newProfilePictureUrl

            if (updates.isNotEmpty()) {
                tx.set(userRef, updates, SetOptions.merge())
            }
            null
        }.await()

        // Optional: keep FirebaseAuth displayName/photoUrl in sync (non-critical).
        runCatching {
            val user = auth.currentUser
            if (user != null && user.uid == uid) {
                val builder = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                if (newUsernameNorm != null && newUsernameNorm != currentNorm) builder.setDisplayName(newUsernameNorm)
                if (!newProfilePictureUrl.isNullOrBlank()) builder.setPhotoUri(Uri.parse(newProfilePictureUrl))
                user.updateProfile(builder.build()).await()
            }
        }
    }

    suspend fun loginEmail(email: String, password: String) {
        auth.signInWithEmailAndPassword(email.trim(), password).await()
    }

    /** Sends Firebase password-reset email (no-op if user missing; still succeeds for privacy). */
    suspend fun sendPasswordResetEmail(emailRaw: String) {
        val email = emailRaw.trim()
        require(email.isNotBlank()) { "Email is required." }
        try {
            auth.sendPasswordResetEmail(email).await()
        } catch (_: FirebaseAuthInvalidUserException) {
            // Do not reveal whether the account exists.
        }
    }

    /**
     * Email/password signup with **unique username** reservation.
     *
     * Strategy:
     * 1) Transaction on `usernames/{name}` ensures nobody else can take it.
     * 2) Create Firebase Auth user.
     * 3) Write `users/{uid}` profile.
     *
     * If step (2) or (3) fails, we attempt to delete the username reservation (best-effort).
     */
    suspend fun signUpEmail(usernameRaw: String, emailRaw: String, password: String) {
        val username = Username.normalize(usernameRaw)
        Username.validateOrThrow(username)

        val email = emailRaw.trim()
        require(email.isNotBlank()) { "Email is required." }
        require(password.length >= 6) { "Password must be at least 6 characters." }

        val usernameRef = usernamesCollection().document(username)

        // (1) Reserve username
        try {
            db.runTransaction { tx ->
                val snap = tx.get(usernameRef)
                if (snap.exists()) {
                    val existingUid = snap.getString("uid").orEmpty()
                    if (existingUid.isNotBlank()) {
                        throw UsernameTakenException("That username is already taken.")
                    }
                }
                tx.set(usernameRef, mapOf("uid" to "__RESERVED__", "created_at" to Timestamp.now()))
                null
            }.await()
        } catch (e: FirebaseFirestoreException) {
            // Common student mistake: rules deny writes -> transaction fails.
            throw IllegalStateException(
                "Firestore rejected the username reservation. Check Firestore rules for `usernames` writes.",
                e,
            )
        }

        // (2) Create auth user
        try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val user = result.user ?: error("Signup succeeded but FirebaseUser is null.")

            // Claim the reservation for real.
            usernameRef.set(mapOf("uid" to user.uid, "created_at" to Timestamp.now()), SetOptions.merge()).await()

            // Optional: set Firebase "displayName" (not the same as unique username, but nice for Google flows).
            runCatching {
                val update = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                    .setDisplayName(username)
                    .build()
                user.updateProfile(update).await()
            }

            // (3) Write profile doc
            val profile =
                UserProfile(
                    userId = user.uid,
                    username = username,
                    email = email,
                    profilePictureUrl = user.photoUrl?.toString(),
                    bio = "",
                    createdAt = Timestamp.now(),
                )
            usersCollection().document(user.uid).set(profile.toFirestoreMap()).await()
        } catch (e: FirebaseAuthUserCollisionException) {
            // Email already exists -> rollback username reservation
            runCatching { usernameRef.delete().await() }
            throw e
        } catch (e: Exception) {
            // Unknown failure after reservation -> try rollback (best-effort)
            runCatching { usernameRef.delete().await() }
            throw e
        }
    }

    /**
     * For Google users, FirebaseAuth account exists immediately, but we still need a unique username.
     *
     * Flow:
     * - Transaction ensures `usernames/{username}` is free OR already owned by this uid.
     * - Write/merge `users/{uid}` profile.
     */
    suspend fun claimUsernameForCurrentGoogleUser(usernameRaw: String) {
        val user = auth.currentUser ?: error("Not signed in.")
        val username = Username.normalize(usernameRaw)
        Username.validateOrThrow(username)

        val usernameRef = usernamesCollection().document(username)
        val userRef = usersCollection().document(user.uid)

        db.runTransaction { tx ->
            val usernameSnap = tx.get(usernameRef)
            if (usernameSnap.exists()) {
                val existingUid = usernameSnap.getString("uid").orEmpty()
                if (existingUid.isNotBlank() && existingUid != user.uid && existingUid != "__RESERVED__") {
                    throw UsernameTakenException("That username is already taken.")
                }
            }

            tx.set(usernameRef, mapOf("uid" to user.uid, "created_at" to Timestamp.now()), SetOptions.merge())

            val email = user.email.orEmpty()
            val profile =
                UserProfile(
                    userId = user.uid,
                    username = username,
                    email = email,
                    profilePictureUrl = user.photoUrl?.toString(),
                    bio = "",
                    createdAt = Timestamp.now(),
                )
            tx.set(userRef, profile.toFirestoreMap(), SetOptions.merge())
            null
        }.await()

        runCatching {
            val update = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                .setDisplayName(username)
                .build()
            user.updateProfile(update).await()
        }
    }
}

class UsernameTakenException(message: String) : Exception(message)
