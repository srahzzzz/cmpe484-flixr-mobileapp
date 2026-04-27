package com.example.flixr.auth

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * UI-facing auth state.
 *
 * We keep this intentionally simple for coursework clarity:
 * - `profile` is loaded from Firestore when signed in.
 * - `needsUsername` is true for Google users who don't have a `users/{uid}` profile yet.
 */
data class AuthUiState(
    val firebaseUser: FirebaseUser? = null,
    val profile: UserProfile? = null,
    val needsUsername: Boolean = false,
    val isBusy: Boolean = false,
    // We keep the opening splash visible until we've completed the first auth/profile check.
    val isInitialized: Boolean = false,
    val errorMessage: String? = null,
)

class AuthViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = AuthRepository(app.applicationContext)

    private val _state = MutableStateFlow(AuthUiState())
    val state: StateFlow<AuthUiState> = _state.asStateFlow()

    init {
        // Listen to auth changes once for the whole ViewModel lifetime.
        viewModelScope.launch {
            repo.observeAuthState().collect { user ->
                onFirebaseUserChanged(user)
            }
        }
    }

    fun clearError() {
        _state.update { it.copy(errorMessage = null) }
    }

    /**
     * UI helper: Google Sign-In requires a Web client id string resource.
     * Until it's configured, we disable the Google button instead of crashing at runtime.
     */
    fun isGoogleConfigured(): Boolean = repo.isWebClientIdConfigured()

    /**
     * Google Sign-In intent should be launched from an Activity context.
     * The ViewModel only builds it; the UI owns the ActivityResult launcher.
     */
    fun buildGoogleSignInIntent(): Intent = repo.buildGoogleSignInIntent()

    fun onGoogleSignInResult(data: Intent?) {
        viewModelScope.launch {
            _state.update { it.copy(isBusy = true, errorMessage = null) }
            try {
                repo.completeGoogleSignIn(data)
                // Busy clears when auth state listener refreshes profile state.
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isBusy = false,
                        errorMessage = e.message ?: "Google sign-in failed.",
                    )
                }
            }
        }
    }

    fun loginEmail(email: String, password: String) {
        viewModelScope.launch {
            _state.update { it.copy(isBusy = true, errorMessage = null) }
            try {
                repo.loginEmail(email, password)
            } catch (e: FirebaseAuthInvalidUserException) {
                _state.update { it.copy(isBusy = false, errorMessage = "No account found for that email.") }
            } catch (e: FirebaseAuthInvalidCredentialsException) {
                _state.update { it.copy(isBusy = false, errorMessage = "Wrong password (or invalid credentials).") }
            } catch (e: Exception) {
                _state.update { it.copy(isBusy = false, errorMessage = e.message ?: "Login failed.") }
            }
        }
    }

    fun signUpEmail(username: String, email: String, password: String) {
        viewModelScope.launch {
            _state.update { it.copy(isBusy = true, errorMessage = null) }
            try {
                repo.signUpEmail(username, email, password)
            } catch (e: UsernameTakenException) {
                _state.update { it.copy(isBusy = false, errorMessage = e.message) }
            } catch (e: Exception) {
                _state.update { it.copy(isBusy = false, errorMessage = e.message ?: "Signup failed.") }
            }
        }
    }

    fun claimUsername(username: String) {
        viewModelScope.launch {
            _state.update { it.copy(isBusy = true, errorMessage = null) }
            try {
                repo.claimUsernameForCurrentGoogleUser(username)
            } catch (e: UsernameTakenException) {
                _state.update { it.copy(isBusy = false, errorMessage = e.message) }
            } catch (e: Exception) {
                _state.update { it.copy(isBusy = false, errorMessage = e.message ?: "Could not save username.") }
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            _state.update { it.copy(isBusy = true, errorMessage = null) }
            try {
                repo.signOutEverywhere()
            } catch (e: Exception) {
                _state.update { it.copy(isBusy = false, errorMessage = e.message ?: "Sign out failed.") }
            }
        }
    }

    private suspend fun onFirebaseUserChanged(user: FirebaseUser?) {
        if (user == null) {
            _state.value =
                AuthUiState(
                    firebaseUser = null,
                    profile = null,
                    needsUsername = false,
                    isBusy = false,
                    isInitialized = true,
                    errorMessage = null,
                )
            return
        }

        // Signed in: load profile from Firestore.
        _state.update { it.copy(firebaseUser = user, isBusy = true, errorMessage = null) }

        val profile =
            try {
                repo.getUserProfileOrNull(user.uid)
            } catch (e: Exception) {
                // Don't brick the UI if Firestore read fails (usually rules/network).
                _state.update {
                    it.copy(
                        isBusy = false,
                        errorMessage = e.message ?: "Failed to load profile from Firestore.",
                    )
                }
                null
            }

        // Google users won't have a `users/{uid}` doc until they finish onboarding.
        val needsUsername = profile == null || profile.username.isBlank()

        _state.update {
            it.copy(
                firebaseUser = user,
                profile = profile,
                needsUsername = needsUsername,
                isBusy = false,
                isInitialized = true,
            )
        }
    }
}
