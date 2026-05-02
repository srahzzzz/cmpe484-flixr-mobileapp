package com.example.flixr.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.zIndex
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.flixr.BuildConfig
import com.example.flixr.R
import com.example.flixr.auth.AuthViewModel
import com.example.flixr.movies.Movie
import com.example.flixr.movies.SavedMovieRepository
import com.example.flixr.movies.TmdbClient
import com.example.flixr.reviews.Review
import com.example.flixr.reviews.ReviewRepository
import com.google.firebase.auth.FirebaseAuth
import com.example.flixr.ui.theme.FlixrGradientBottomLeft
import com.example.flixr.ui.theme.FlixrGradientTopRight
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.min
import android.util.Patterns
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.List
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Root Compose entry point.
 *
 * Step-by-step runtime flow:
 * 1) `AuthViewModel` listens to FirebaseAuth changes.
 * 2) When signed in, it loads `users/{uid}` from Firestore.
 * 3) If missing username/profile (common for Google first sign-in), we show onboarding.
 * 4) Email/password users get a profile written during signup, so they should land on Home.
 */
@Composable
fun FlixrApp(viewModel: AuthViewModel = viewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    // In-app splash (custom gradient + logo + "Flixr") for a FIXED duration.
    // We do NOT use flixeropeningpage.png here (only used as inspiration).
    var showSplash by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        delay(3000)
        showSplash = false
    }

    // Activity Result API: Google account picker returns an Intent result to this launcher.
    val googleLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            // `data` contains the account picker result for GoogleSignIn.
            viewModel.onGoogleSignInResult(result.data)
        }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        if (showSplash) {
            BrandedSplashScreen()
            return@Surface
        }

        when {
            state.firebaseUser == null -> {
                // Signed out: show a simple welcome + navigation to auth screens.
                AuthLandingScreen(
                    isBusy = state.isBusy,
                    errorMessage = state.errorMessage,
                    successMessage = state.successMessage,
                    isGoogleConfigured = viewModel.isGoogleConfigured(),
                    onLoginEmail = viewModel::loginEmail,
                    onSignUpEmail = viewModel::signUpEmail,
                    onGoogleClick = {
                        // Launch Google account picker. Firebase sign-in happens in the ViewModel after result.
                        googleLauncher.launch(viewModel.buildGoogleSignInIntent())
                    },
                    onClearSuccess = viewModel::clearSuccess,
                )
            }

            state.needsUsername -> {
                GoogleUsernameOnboardingScreen(
                    email = state.firebaseUser?.email.orEmpty(),
                    isBusy = state.isBusy,
                    errorMessage = state.errorMessage,
                    onClearError = viewModel::clearError,
                    onClaimUsername = viewModel::claimUsername,
                    onBackToWelcome = { viewModel.signOut() },
                )
            }

            else -> {
                HomeScreen(
                    username = state.profile?.username.orEmpty(),
                    email = state.profile?.email ?: state.firebaseUser?.email.orEmpty(),
                    isBusy = state.isBusy,
                    errorMessage = state.errorMessage,
                    onClearError = viewModel::clearError,
                    onSignOut = viewModel::signOut,
                )
            }
        }
    }
}

@Composable
private fun BrandedSplashScreen() {
    val gradient =
        Brush.verticalGradient(
            colors = listOf(FlixrGradientTopRight, FlixrGradientBottomLeft),
        )

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(gradient),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = painterResource(id = R.drawable.flixr_logo_solo),
                contentDescription = null,
                // Large, but not layout-breaking because it's on the splash only.
                modifier = Modifier.height(260.dp),
                contentScale = ContentScale.Fit,
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = "Flixr",
                style = MaterialTheme.typography.titleLarge.copy(fontSize = 44.sp),
                color = Color.White,
            )
        }
    }
}

@Composable
private fun AuthLandingScreen(
    isBusy: Boolean,
    errorMessage: String?,
    successMessage: String?,
    isGoogleConfigured: Boolean,
    onLoginEmail: (email: String, password: String) -> Unit,
    onSignUpEmail: (username: String, email: String, password: String) -> Unit,
    onGoogleClick: () -> Unit,
    onClearSuccess: () -> Unit,
) {
    // Local mini-navigation (keeps dependencies low for coursework).
    var screen by remember { mutableStateOf(LandingScreen.Welcome) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(successMessage) {
        val msg = successMessage
        if (!msg.isNullOrBlank()) {
            // Ensure we actually show the green popup on screen.
            screen = LandingScreen.Login
            scope.launch {
                snackbarHostState.showSnackbar(message = msg, withDismissAction = true)
            }
            onClearSuccess()
        }
    }

    val gradient =
        Brush.verticalGradient(
            colors = listOf(FlixrGradientTopRight, FlixrGradientBottomLeft),
        )

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(gradient)
                .statusBarsPadding()
                .navigationBarsPadding(),
    ) {
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.TopCenter).padding(top = 8.dp),
            snackbar = { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = Color(0xFF2E7D32), // green
                    contentColor = Color.White,
                    actionColor = Color.White,
                )
            },
        )
        val logoHeight = rememberLogoHeightDp(maxDp = 170f)
        when (screen) {
            LandingScreen.Welcome -> {
                Column(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Spacer(Modifier.height(90.dp))

                    Image(
                        painter = painterResource(id = R.drawable.flixr_logo_solo),
                        contentDescription = null,
                        modifier = Modifier.height(logoHeight),
                        contentScale = ContentScale.Fit,
                    )

                    Spacer(Modifier.height(18.dp))

                    Text(
                        text = "Flixr",
                        style = MaterialTheme.typography.titleLarge.copy(fontSize = 44.sp),
                        color = Color.White,
                        textAlign = TextAlign.Center,
                    )

                    Spacer(Modifier.height(6.dp))

                    Text(
                        text = "Sign in to continue.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White.copy(alpha = 0.75f),
                        textAlign = TextAlign.Center,
                    )

                    Spacer(Modifier.height(36.dp))

                    if (!errorMessage.isNullOrBlank()) {
                        Text(
                            text = errorMessage,
                            color = Color.White.copy(alpha = 0.9f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth().alpha(0.9f),
                        )
                        Spacer(Modifier.height(12.dp))
                    }

                    OutlinedButton(
                        onClick = { screen = LandingScreen.Login },
                        enabled = !isBusy,
                        modifier = Modifier.fillMaxWidth().height(54.dp),
                        shape = RoundedCornerShape(28.dp),
                        border = BorderStroke(1.5.dp, Color.White.copy(alpha = 0.9f)),
                        colors =
                            ButtonDefaults.outlinedButtonColors(
                                contentColor = Color.White,
                                disabledContentColor = Color.White.copy(alpha = 0.5f),
                            ),
                    ) {
                        Text(
                            text = "Login",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        )
                    }

                    Spacer(Modifier.height(14.dp))

                    OutlinedButton(
                        onClick = { screen = LandingScreen.Signup },
                        enabled = !isBusy,
                        modifier = Modifier.fillMaxWidth().height(54.dp),
                        shape = RoundedCornerShape(28.dp),
                        border = BorderStroke(1.5.dp, Color.White.copy(alpha = 0.9f)),
                        colors =
                            ButtonDefaults.outlinedButtonColors(
                                contentColor = Color.White,
                                disabledContentColor = Color.White.copy(alpha = 0.5f),
                            ),
                    ) {
                        Text(
                            text = "Create account",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        )
                    }

                    Spacer(Modifier.height(14.dp))

                    // Optional: Google button (kept here so Login screen matches screenshot exactly).
                    OutlinedButton(
                        onClick = onGoogleClick,
                        enabled = !isBusy && isGoogleConfigured,
                        modifier = Modifier.fillMaxWidth().height(54.dp),
                        shape = RoundedCornerShape(28.dp),
                        border = BorderStroke(1.5.dp, Color.White.copy(alpha = 0.9f)),
                        colors =
                            ButtonDefaults.outlinedButtonColors(
                                contentColor = Color.White,
                                disabledContentColor = Color.White.copy(alpha = 0.5f),
                            ),
                    ) {
                        Text(
                            text = "Continue with Google",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        )
                    }

                    if (!isGoogleConfigured) {
                        Spacer(Modifier.height(10.dp))
                        Text(
                            text = "Google Sign-In not configured yet (set default_web_client_id).",
                            color = Color.White.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }

            LandingScreen.Login -> {
                LoginScreen(
                    isBusy = isBusy,
                    errorMessage = errorMessage,
                    isGoogleConfigured = isGoogleConfigured,
                    onBack = { screen = LandingScreen.Welcome },
                    onLoginEmail = onLoginEmail,
                    onGoogleClick = onGoogleClick,
                )
            }

            LandingScreen.Signup -> {
                SignupScreen(
                    isBusy = isBusy,
                    errorMessage = errorMessage,
                    onBack = { screen = LandingScreen.Welcome },
                    onGoLogin = { screen = LandingScreen.Login },
                    onSignUp = onSignUpEmail,
                )
            }
        }
    }
}

private enum class LandingScreen { Welcome, Login, Signup }

@Composable
private fun LoginScreen(
    isBusy: Boolean,
    errorMessage: String?,
    isGoogleConfigured: Boolean,
    onBack: () -> Unit,
    onLoginEmail: (email: String, password: String) -> Unit,
    onGoogleClick: () -> Unit,
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    // Exact-style login screen (replicates screenshot layout/colors).
    val gradient =
        Brush.verticalGradient(
            colors = listOf(FlixrGradientTopRight, FlixrGradientBottomLeft),
        )

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(gradient)
                .statusBarsPadding()
                .navigationBarsPadding(),
    ) {
        val logoHeight = rememberLogoHeightDp(maxDp = 170f)
        IconButton(
            onClick = onBack,
            enabled = !isBusy,
            modifier = Modifier.padding(10.dp).zIndex(1f),
        ) {
            Icon(
                imageVector = Icons.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Color.White,
            )
        }

        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 28.dp)
                    .verticalScroll(rememberScrollState())
                    .imePadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(80.dp))

            Image(
                painter = painterResource(id = R.drawable.flixr_logo_solo),
                contentDescription = null,
                modifier = Modifier.height(logoHeight),
                contentScale = ContentScale.Fit,
            )

            Spacer(Modifier.height(18.dp))

            Text(
                text = "Login",
                style = MaterialTheme.typography.titleLarge.copy(fontSize = 44.sp),
                color = Color.White,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(6.dp))

            Text(
                text = "Sign in to continue.",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.75f),
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(36.dp))

            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(
                    text = "EMAIL",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White.copy(alpha = 0.75f),
                    modifier = Modifier.fillMaxWidth(),
                )

                PillTextField(
                    value = email,
                    onValueChange = { email = it },
                    placeholder = "hello@reallygreatsite.com",
                    keyboardType = KeyboardType.Email,
                    enabled = !isBusy,
                )

                Spacer(Modifier.height(6.dp))

                Text(
                    text = "PASSWORD",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White.copy(alpha = 0.75f),
                    modifier = Modifier.fillMaxWidth(),
                )

                PillTextField(
                    value = password,
                    onValueChange = { password = it },
                    placeholder = "******",
                    keyboardType = KeyboardType.Password,
                    enabled = !isBusy,
                    isPassword = true,
                )
            }

            if (!errorMessage.isNullOrBlank()) {
                Spacer(Modifier.height(14.dp))
                Text(
                    text = errorMessage,
                    color = Color.White.copy(alpha = 0.9f),
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .alpha(0.9f),
                    textAlign = TextAlign.Center,
                )
            }

            Spacer(Modifier.height(26.dp))

            OutlinedButton(
                onClick = { onLoginEmail(email, password) },
                enabled = !isBusy && email.isNotBlank() && password.isNotBlank(),
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(28.dp),
                border = BorderStroke(1.5.dp, Color.White.copy(alpha = 0.9f)),
                colors =
                    ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.White,
                        disabledContentColor = Color.White.copy(alpha = 0.5f),
                    ),
            ) {
                Text(
                    text = "Login",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = Color.White,
                )
            }

            // Screenshot login screen doesn't show Google/back buttons; keep those on the Welcome screen.
        }
    }
}

@Composable
private fun PillTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardType: KeyboardType,
    enabled: Boolean,
    isPassword: Boolean = false,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        enabled = enabled,
        singleLine = true,
        placeholder = {
            Text(
                text = placeholder,
                color = Color.White.copy(alpha = 0.6f),
            )
        },
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        shape = RoundedCornerShape(16.dp),
        colors =
            androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                disabledBorderColor = Color.Transparent,
                focusedContainerColor = Color.White.copy(alpha = 0.22f),
                unfocusedContainerColor = Color.White.copy(alpha = 0.22f),
                disabledContainerColor = Color.White.copy(alpha = 0.14f),
                cursorColor = Color.White,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                disabledTextColor = Color.White.copy(alpha = 0.7f),
            ),
    )
}

@Composable
private fun SignupScreen(
    isBusy: Boolean,
    errorMessage: String?,
    onBack: () -> Unit,
    onGoLogin: () -> Unit,
    onSignUp: (username: String, email: String, password: String) -> Unit,
) {
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    // Live validation (UI-level) so the button enables/disables immediately.
    val normalizedUsername = com.example.flixr.auth.Username.normalize(username)
    val usernameError: String? =
        runCatching {
            com.example.flixr.auth.Username.validateOrThrow(normalizedUsername)
            null
        }.getOrElse {
            "Usernames must be 3-20 chars: lowercase letters, numbers, underscore only."
        }
    val usernameOk = usernameError == null

    val emailTrimmed = email.trim()
    val emailOk = Patterns.EMAIL_ADDRESS.matcher(emailTrimmed).matches()
    val passwordOk = password.length >= 6

    // UX choice: keep the button enabled when fields are filled, and show clear inline errors.
    // This avoids the "button isn't working" confusion.
    val fieldsFilled = normalizedUsername.isNotBlank() && emailTrimmed.isNotBlank() && password.isNotBlank()
    val canSubmit = !isBusy && fieldsFilled

    // Exact-style create-account screen (replicates screenshot layout/colors).
    val gradient =
        Brush.verticalGradient(
            colors = listOf(FlixrGradientTopRight, FlixrGradientBottomLeft),
        )

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(gradient)
                .statusBarsPadding()
                .navigationBarsPadding(),
    ) {
        val logoHeight = rememberLogoHeightDp(maxDp = 150f)
        IconButton(
            onClick = onBack,
            enabled = !isBusy,
            modifier = Modifier.padding(10.dp),
        ) {
            Icon(
                imageVector = Icons.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Color.White,
            )
        }

        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Scrollable content (validation messages can grow here without pushing the CTA off-screen)
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(Modifier.height(80.dp))

                Image(
                    painter = painterResource(id = R.drawable.flixr_logo_solo),
                    contentDescription = null,
                    modifier = Modifier.height(logoHeight),
                    contentScale = ContentScale.Fit,
                )

                Spacer(Modifier.height(18.dp))

                Text(
                    text = "Create\nAccount",
                    style = MaterialTheme.typography.titleLarge.copy(fontSize = 40.sp, lineHeight = 42.sp),
                    color = Color.White,
                    textAlign = TextAlign.Center,
                )

                Spacer(Modifier.height(10.dp))

                Text(
                    text = "Already Registered? Log in here.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(alpha = 0.75f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.clickable(enabled = !isBusy) { onGoLogin() },
                )

                Spacer(Modifier.height(30.dp))

                Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(
                        text = "USERNAME",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White.copy(alpha = 0.75f),
                        modifier = Modifier.fillMaxWidth(),
                    )
                    PillTextField(
                        value = username,
                        onValueChange = { username = it },
                        placeholder = "jiara_martins",
                        keyboardType = KeyboardType.Text,
                        enabled = !isBusy,
                    )
                    if (!usernameOk && normalizedUsername.isNotBlank()) {
                        Text(
                            text = "Use 3–20 chars: a–z, 0–9, underscore. (example: jiara_martins)",
                            color = Color.White.copy(alpha = 0.75f),
                            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 12.sp, lineHeight = 16.sp),
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }

                    Spacer(Modifier.height(6.dp))

                    Text(
                        text = "EMAIL",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White.copy(alpha = 0.75f),
                        modifier = Modifier.fillMaxWidth(),
                    )
                    PillTextField(
                        value = email,
                        onValueChange = { email = it },
                        placeholder = "hello@reallygreatsite.com",
                        keyboardType = KeyboardType.Email,
                        enabled = !isBusy,
                    )
                    if (!emailOk && emailTrimmed.isNotBlank()) {
                        Text(
                            text = "Please enter a valid email address.",
                            color = Color.White.copy(alpha = 0.75f),
                            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 12.sp, lineHeight = 16.sp),
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }

                    Spacer(Modifier.height(6.dp))

                    Text(
                        text = "PASSWORD",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White.copy(alpha = 0.75f),
                        modifier = Modifier.fillMaxWidth(),
                    )
                    PillTextField(
                        value = password,
                        onValueChange = { password = it },
                        placeholder = "******",
                        keyboardType = KeyboardType.Password,
                        enabled = !isBusy,
                        isPassword = true,
                    )
                    if (!passwordOk && password.isNotBlank()) {
                        Text(
                            text = "Password must be at least 6 characters.",
                            color = Color.White.copy(alpha = 0.75f),
                            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 12.sp, lineHeight = 16.sp),
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }

                if (!errorMessage.isNullOrBlank()) {
                    Spacer(Modifier.height(14.dp))
                    Text(
                        text = errorMessage,
                        color = Color.White.copy(alpha = 0.9f),
                        modifier = Modifier.fillMaxWidth().alpha(0.9f),
                        textAlign = TextAlign.Center,
                    )
                }

                // Breathing room above the pinned CTA.
                Spacer(Modifier.height(18.dp))
            }

            // Pinned bottom CTA (stays visible; moves above keyboard).
            Column(
                modifier = Modifier.fillMaxWidth().imePadding().padding(bottom = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                OutlinedButton(
                    onClick = {
                        if (!usernameOk || !emailOk || !passwordOk) return@OutlinedButton
                        onSignUp(normalizedUsername, emailTrimmed, password)
                    },
                    enabled = canSubmit && usernameOk && emailOk && passwordOk,
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    shape = RoundedCornerShape(28.dp),
                    border = BorderStroke(1.5.dp, Color.White.copy(alpha = 0.9f)),
                    colors =
                        ButtonDefaults.outlinedButtonColors(
                            contentColor = Color.White,
                            disabledContentColor = Color.White.copy(alpha = 0.5f),
                        ),
                ) {
                    Text(
                        text = "Sign up",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    )
                }

                if (isBusy) {
                    Spacer(Modifier.height(10.dp))
                    LinearBusy()
                }
            }
        }
    }
}

@Composable
private fun GoogleUsernameOnboardingScreen(
    email: String,
    isBusy: Boolean,
    errorMessage: String?,
    onClearError: () -> Unit,
    onClaimUsername: (String) -> Unit,
    onBackToWelcome: () -> Unit,
) {
    var username by remember { mutableStateOf("") }

    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedButton(onClick = onBackToWelcome, enabled = !isBusy, modifier = Modifier.fillMaxWidth()) {
            Text("Back")
        }
        Text("Choose a username", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text("Signed in as: $email", style = MaterialTheme.typography.bodyMedium)

        OutlinedTextField(
            value = username,
            onValueChange = {
                username = it
                onClearError()
            },
            label = { Text("Unique username") },
            supportingText = { Text("Lowercase, 3-20 chars, letters/numbers/underscore") },
            modifier = Modifier.fillMaxWidth(),
        )

        if (!errorMessage.isNullOrBlank()) {
            Text(text = errorMessage, color = MaterialTheme.colorScheme.error)
        }

        Button(
            onClick = { onClaimUsername(username) },
            enabled = !isBusy && username.isNotBlank(),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Save username")
        }

        if (isBusy) LinearBusy()
    }
}

@Composable
private fun HomeSectionTitle(text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(
            modifier =
                Modifier
                    .width(4.dp)
                    .height(22.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(MaterialTheme.colorScheme.primary),
        )
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun HomeScreen(
    username: String,
    email: String,
    isBusy: Boolean,
    errorMessage: String?,
    onClearError: () -> Unit,
    onSignOut: () -> Unit,
) {
    var screen by remember { mutableStateOf("home") } // "home" | "details" | "profile" | "watchlist"
    var detailsReturnScreen by remember { mutableStateOf("home") }
    var query by remember { mutableStateOf("") }
    var results by remember { mutableStateOf<List<Movie>>(emptyList()) }
    var trendingMovies by remember { mutableStateOf<List<Movie>>(emptyList()) }
    var isLoadingTrending by remember { mutableStateOf(true) }
    var selected by remember { mutableStateOf<Movie?>(null) }
    var localError by remember { mutableStateOf<String?>(null) }
    var isSearching by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    val reviewRepo = remember { ReviewRepository() }
    val savedRepo = remember { SavedMovieRepository() }

    LaunchedEffect(Unit) {
        val apiKey = BuildConfig.TMDB_API_KEY.trim()
        if (apiKey.isBlank()) {
            isLoadingTrending = false
            localError = "TMDB API key missing. Add TMDB_API_KEY to local.properties, sync, and rebuild."
            return@LaunchedEffect
        }
        var feedError: String? = null
        try {
            trendingMovies = TmdbClient.api.getTrendingMovies(apiKey).results
        } catch (e: Exception) {
            feedError = e.message ?: "Failed to load trending movies."
        } finally {
            isLoadingTrending = false
        }
        if (feedError != null) {
            localError = feedError
        }
    }

    fun doSearch() {
        val q = query.trim()
        if (q.isBlank()) return
        val apiKey = BuildConfig.TMDB_API_KEY.trim()
        if (apiKey.isBlank()) {
            localError = "TMDB API key missing. Add TMDB_API_KEY to local.properties, sync, and rebuild."
            return
        }

        focusManager.clearFocus()
        localError = null
        isSearching = true
        scope.launch {
            try {
                results = TmdbClient.api.searchMovies(apiKey = apiKey, query = q).results
            } catch (e: Exception) {
                localError = e.message ?: "Search failed."
            } finally {
                isSearching = false
            }
        }
    }

    if (screen == "details" && selected != null) {
        MovieDetailsScreen(
            movie = selected!!,
            reviewRepo = reviewRepo,
            savedRepo = savedRepo,
            onBack = { screen = detailsReturnScreen },
        )
        return
    }

    if (screen == "watchlist") {
        WatchlistScreen(
            savedRepo = savedRepo,
            onBack = { screen = "home" },
            onOpenMovie = { movie ->
                detailsReturnScreen = "watchlist"
                selected = movie
                screen = "details"
            },
        )
        return
    }

    if (screen == "profile") {
        ProfileScreen(
            onBack = { screen = "home" },
        )
        return
    }

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors =
                            listOf(
                                MaterialTheme.colorScheme.background,
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                FlixrGradientTopRight.copy(alpha = 0.07f),
                                FlixrGradientBottomLeft.copy(alpha = 0.06f),
                            ),
                    ),
                ),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                tonalElevation = 2.dp,
                shadowElevation = 0.dp,
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
            ) {
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Home",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Text(
                            text = if (username.isNotBlank()) "Hi, $username" else "Welcome",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        if (email.isNotBlank()) {
                            Text(
                                text = email,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f),
                            )
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        IconButton(onClick = { screen = "watchlist" }) {
                            Icon(
                                imageVector = Icons.Filled.List,
                                contentDescription = "Watchlist",
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        }
                        IconButton(onClick = { screen = "profile" }) {
                            Icon(
                                imageVector = Icons.Filled.AccountCircle,
                                contentDescription = "Profile",
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                }
            }

            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors =
                    OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.45f),
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        cursorColor = MaterialTheme.colorScheme.primary,
                    ),
                label = { Text("Search movies") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                keyboardActions = KeyboardActions(onSearch = { doSearch() }),
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = { doSearch() },
                    enabled = !isSearching && query.trim().isNotBlank(),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                        ),
                ) {
                    Text(if (isSearching) "Searching..." else "Search")
                }
                TextButton(
                    onClick = { results = emptyList(); query = ""; localError = null },
                    enabled = !isSearching,
                    colors =
                        ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary,
                        ),
                ) {
                    Text("Clear")
                }
            }

            if (!localError.isNullOrBlank()) {
                Text(text = localError!!, color = MaterialTheme.colorScheme.error)
            }
            if (!errorMessage.isNullOrBlank()) {
                Text(text = errorMessage, color = MaterialTheme.colorScheme.error)
            }

            LazyColumn(
                modifier = Modifier.fillMaxWidth().weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                if (isLoadingTrending && trendingMovies.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().height(220.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }

                if (trendingMovies.isNotEmpty()) {
                    item {
                        HomeSectionTitle("Trending this week")
                    }
                item {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        items(trendingMovies, key = { it.id }) { movie ->
                            NetflixShelfMovieCard(
                                movie = movie,
                                onClick = {
                                    detailsReturnScreen = "home"
                                    selected = movie
                                    screen = "details"
                                },
                            )
                        }
                    }
                }
            }

                if (results.isNotEmpty()) {
                    item {
                        HomeSectionTitle("Search results")
                    }
                    items(results, key = { it.id }) { movie ->
                        HomeSearchResultRow(
                            movie = movie,
                            onClick = {
                                detailsReturnScreen = "home"
                                selected = movie
                                screen = "details"
                            },
                        )
                    }
                }

                if (!isLoadingTrending && trendingMovies.isEmpty() && results.isEmpty() && localError.isNullOrBlank()) {
                    item {
                        Text(
                            text = "Search above to find movies, or check back when trending loads.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            OutlinedButton(
                onClick = {
                    onClearError()
                    onSignOut()
                },
                enabled = !isBusy,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors =
                    ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary,
                    ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.55f)),
            ) {
                Text("Sign out")
            }

            if (isBusy) LinearBusy()
        }
    }
}

@Composable
private fun MovieDetailsScreen(
    movie: Movie,
    reviewRepo: ReviewRepository,
    savedRepo: SavedMovieRepository,
    onBack: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    var ratingText by remember { mutableStateOf("5") }
    var reviewText by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    var reviews by remember { mutableStateOf<List<Review>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var savedMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(movie.id) {
        isLoading = true
        error = null
        savedMessage = null
        try {
            reviews = reviewRepo.getReviewsForContent(movie.id.toString())
        } catch (e: Exception) {
            error = e.message ?: "Failed to load reviews."
        } finally {
            isLoading = false
        }
    }

    val scroll = rememberScrollState()
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .verticalScroll(scroll)
                .imePadding(),
        verticalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(300.dp),
        ) {
            MovieDetailHeroPoster(movie = movie)
            Surface(
                modifier =
                    Modifier
                        .align(Alignment.TopStart)
                        .statusBarsPadding()
                        .padding(8.dp),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                tonalElevation = 2.dp,
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = "Back",
                    )
                }
            }
        }

        Column(
            modifier =
                Modifier
                    .padding(horizontal = 16.dp)
                    .padding(top = 16.dp, bottom = 20.dp)
                    .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = movie.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val date = movie.releaseDate?.takeIf { it.isNotBlank() }
                if (date != null) {
                    Text(
                        date,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                    )
                }
                movie.voteAverage?.let { avg ->
                    Text(
                        text = "TMDB ★ ${String.format("%.1f", avg)}",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }

            if (!movie.overview.isNullOrBlank()) {
                Text(
                    text = movie.overview!!,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f),
                )
            }

            OutlinedButton(
                onClick = {
                    val uid = FirebaseAuth.getInstance().currentUser?.uid
                    if (uid.isNullOrBlank()) {
                        error = "You must be logged in to add movies."
                        return@OutlinedButton
                    }
                    isSaving = true
                    error = null
                    savedMessage = null
                    scope.launch {
                        try {
                            savedRepo.saveMovieForUser(uid, movie)
                            savedMessage = "Added to your watchlist."
                        } catch (e: Exception) {
                            error = e.message ?: "Could not save to watchlist."
                        } finally {
                            isSaving = false
                        }
                    }
                },
                enabled = !isSaving,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(imageVector = Icons.Filled.Favorite, contentDescription = null)
                Spacer(Modifier.size(8.dp))
                Text(if (isSaving) "Saving..." else "Add to Watchlist")
            }

            if (!savedMessage.isNullOrBlank()) {
                Text(savedMessage!!, color = MaterialTheme.colorScheme.primary)
            }

        Text("Write a review", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)

        OutlinedTextField(
            value = ratingText,
            onValueChange = { ratingText = it.filter { ch -> ch.isDigit() }.take(2) },
            label = { Text("Rating (0-10)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        )

        OutlinedTextField(
            value = reviewText,
            onValueChange = { reviewText = it },
            label = { Text("Your review") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
        )

        Button(
            onClick = {
                val rating = ratingText.toIntOrNull()
                if (rating == null || rating !in 0..10) {
                    error = "Rating must be between 0 and 10."
                    return@Button
                }
                val uid = FirebaseAuth.getInstance().currentUser?.uid
                if (uid.isNullOrBlank()) {
                    error = "You must be logged in to post reviews."
                    return@Button
                }

                focusManager.clearFocus()
                isSaving = true
                error = null
                scope.launch {
                    try {
                        reviewRepo.addReview(
                            Review(
                                user_id = uid,
                                content_id = movie.id.toString(),
                                rating = rating,
                                review_text = reviewText.trim(),
                            ),
                        )
                        reviewText = ""
                        ratingText = "5"
                        reviews = reviewRepo.getReviewsForContent(movie.id.toString())
                    } catch (e: Exception) {
                        error = e.message ?: "Failed to save review."
                    } finally {
                        isSaving = false
                    }
                }
            },
            enabled = !isSaving && reviewText.trim().isNotBlank(),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(if (isSaving) "Posting..." else "Post review")
        }

        if (!error.isNullOrBlank()) {
            Text(error!!, color = MaterialTheme.colorScheme.error)
        }

        Text("Reviews", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        if (isLoading) {
            LinearBusy()
        } else if (reviews.isEmpty()) {
            Text("No reviews yet.")
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                for (r in reviews) {
                    Surface(tonalElevation = 2.dp, modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("Rating: ${r.rating}/10", fontWeight = FontWeight.SemiBold)
                            Text(r.review_text)
                        }
                    }
                }
            }
        }
    }
    }
}

private fun tmdbPosterUrl(path: String?, width: Int): String? {
    val p = path?.trim().orEmpty()
    if (p.isBlank()) return null
    // `poster_path` usually starts with "/" already. Both variants work.
    return "https://image.tmdb.org/t/p/w$width$p"
}

@Composable
private fun MovieDetailHeroPoster(movie: Movie) {
    val url = tmdbPosterUrl(movie.posterPath, width = 780)
    Box(modifier = Modifier.fillMaxSize()) {
        if (url == null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    "No poster available",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            AsyncImage(
                model = url,
                contentDescription = movie.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        }
    }
}

/** Netflix-style horizontal shelf tile (poster + title + date). */
@Composable
private fun NetflixShelfMovieCard(
    movie: Movie,
    onClick: () -> Unit,
) {
    val dateLabel = movie.releaseDate?.takeIf { it.isNotBlank() } ?: "—"
    Column(
        modifier =
            Modifier
                .width(140.dp)
                .clickable(onClick = onClick),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth().height(200.dp),
            shape = RoundedCornerShape(12.dp),
            tonalElevation = 2.dp,
        ) {
            val url = tmdbPosterUrl(movie.posterPath, width = 342)
            if (url == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        "No poster",
                        textAlign = TextAlign.Center,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    )
                }
            } else {
                AsyncImage(
                    model = url,
                    contentDescription = movie.title,
                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop,
                )
            }
        }
        Text(
            text = movie.title,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = dateLabel,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun HomeSearchResultRow(
    movie: Movie,
    onClick: () -> Unit,
    onRemove: (() -> Unit)? = null,
    removeInProgress: Boolean = false,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OutlinedButton(
            onClick = onClick,
            modifier = Modifier.weight(1f),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TmdbPosterThumb(movie = movie)
                Column(modifier = Modifier.weight(1f)) {
                    Text(movie.title, fontWeight = FontWeight.SemiBold)
                    val date = movie.releaseDate?.takeIf { it.isNotBlank() } ?: "Unknown release date"
                    Text(date, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                }
            }
        }
        if (onRemove != null) {
            IconButton(
                onClick = onRemove,
                enabled = !removeInProgress,
            ) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "Remove from watchlist",
                    tint = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}

@Composable
private fun TmdbPosterThumb(movie: Movie) {
    val url = tmdbPosterUrl(movie.posterPath, width = 185)
    Surface(
        modifier = Modifier.size(width = 54.dp, height = 80.dp),
        shape = RoundedCornerShape(10.dp),
        tonalElevation = 1.dp,
    ) {
        if (url == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No\nposter", textAlign = TextAlign.Center, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
            }
        } else {
            AsyncImage(
                model = url,
                contentDescription = null,
                modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(10.dp)),
                contentScale = ContentScale.Crop,
            )
        }
    }
}

@Composable
private fun TmdbPosterLarge(movie: Movie) {
    val url = tmdbPosterUrl(movie.posterPath, width = 342)
    Surface(
        modifier = Modifier.size(width = 120.dp, height = 180.dp),
        shape = RoundedCornerShape(14.dp),
        tonalElevation = 1.dp,
    ) {
        if (url == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No\nposter", textAlign = TextAlign.Center, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
            }
        } else {
            AsyncImage(
                model = url,
                contentDescription = null,
                modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(14.dp)),
                contentScale = ContentScale.Crop,
            )
        }
    }
}

@Composable
private fun WatchlistScreen(
    savedRepo: SavedMovieRepository,
    onBack: () -> Unit,
    onOpenMovie: (Movie) -> Unit,
) {
    val uid = FirebaseAuth.getInstance().currentUser?.uid
    var movies by remember { mutableStateOf<List<Movie>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var loadError by remember { mutableStateOf<String?>(null) }
    var deletingMovieId by remember { mutableStateOf<Int?>(null) }
    var deleteError by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(uid) {
        if (uid.isNullOrBlank()) {
            loading = false
            movies = emptyList()
            loadError = "Sign in to view your watchlist."
            return@LaunchedEffect
        }
        loading = true
        loadError = null
        try {
            movies = savedRepo.getWatchlistForUser(uid)
        } catch (e: Exception) {
            loadError = e.message ?: "Could not load watchlist."
        } finally {
            loading = false
        }
    }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 16.dp),
    ) {
        OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = null)
            Spacer(Modifier.size(8.dp))
            Text("Back")
        }
        Text(
            text = "My Watchlist",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )
        if (!deleteError.isNullOrBlank()) {
            Text(deleteError!!, color = MaterialTheme.colorScheme.error)
        }

        when {
            loading -> {
                Box(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }
            loadError != null -> {
                Text(loadError!!, color = MaterialTheme.colorScheme.error)
            }
            movies.isEmpty() -> {
                Text(
                    text = "No movies saved yet. Open a title and tap Add to Watchlist.",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                )
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(movies, key = { it.id }) { movie ->
                        HomeSearchResultRow(
                            movie = movie,
                            onClick = { onOpenMovie(movie) },
                            onRemove =
                                if (uid.isNullOrBlank()) {
                                    null
                                } else {
                                    {
                                        val u = uid
                                        scope.launch {
                                            deletingMovieId = movie.id
                                            deleteError = null
                                            try {
                                                savedRepo.removeMovieFromWatchlist(u, movie.id)
                                                movies = savedRepo.getWatchlistForUser(u)
                                            } catch (e: Exception) {
                                                deleteError = e.message ?: "Could not remove movie."
                                            } finally {
                                                deletingMovieId = null
                                            }
                                        }
                                    }
                                },
                            removeInProgress = deletingMovieId == movie.id,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileScreen(
    viewModel: AuthViewModel = viewModel(),
    onBack: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val profile = state.profile

    var username by remember { mutableStateOf(profile?.username.orEmpty()) }
    var bio by remember { mutableStateOf(profile?.bio.orEmpty()) }
    var pickedImage by remember { mutableStateOf<android.net.Uri?>(null) }

    val picker =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            pickedImage = uri
        }

    LaunchedEffect(profile?.username, profile?.bio) {
        // Keep fields in sync when profile loads/refreshes.
        if (!profile?.username.isNullOrBlank()) username = profile?.username.orEmpty()
        bio = profile?.bio.orEmpty()
    }

    Column(
        modifier = Modifier.padding(20.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        OutlinedButton(onClick = onBack, enabled = !state.isBusy, modifier = Modifier.fillMaxWidth()) {
            Text("Back")
        }

        Text("Profile", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(
            text = "Your saved movies appear on the Watchlist screen (list icon on Home).",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
        )

        val photoModel = pickedImage ?: profile?.profilePictureUrl
        Surface(shape = RoundedCornerShape(16.dp), tonalElevation = 1.dp, modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.padding(12.dp).fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(shape = RoundedCornerShape(999.dp), tonalElevation = 2.dp, modifier = Modifier.size(78.dp)) {
                    if (photoModel == null) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Icon(imageVector = Icons.Filled.AccountCircle, contentDescription = null)
                        }
                    } else {
                        AsyncImage(
                            model = photoModel,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(profile?.email.orEmpty(), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                    OutlinedButton(
                        onClick = { picker.launch("image/*") },
                        enabled = !state.isBusy,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Icon(imageVector = Icons.Filled.Edit, contentDescription = null)
                        Spacer(Modifier.size(8.dp))
                        Text("Change picture")
                    }
                }
            }
        }

        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = !state.isBusy,
            label = { Text("Username") },
            supportingText = { Text("3–20 chars, lowercase letters/numbers/underscore") },
        )

        OutlinedTextField(
            value = bio,
            onValueChange = { bio = it },
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isBusy,
            label = { Text("Bio") },
            minLines = 3,
        )

        if (!state.errorMessage.isNullOrBlank()) {
            Text(state.errorMessage!!, color = MaterialTheme.colorScheme.error)
        }

        Button(
            onClick = {
                viewModel.updateProfile(
                    username = username,
                    bio = bio,
                    newLocalPhoto = pickedImage,
                )
            },
            enabled = !state.isBusy && state.firebaseUser != null,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(if (state.isBusy) "Saving..." else "Save changes")
        }
    }
}

@Composable
private fun LinearBusy() {
    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(modifier = Modifier.height(8.dp))
        CircularProgressIndicator()
    }
}

@Composable
private fun rememberLogoHeightDp(maxDp: Float): androidx.compose.ui.unit.Dp {
    val cfg = LocalConfiguration.current
    // Scale logo with screen height so it never squashes the form.
    // Target ~22% of screen height, capped for small devices.
    val target = (cfg.screenHeightDp * 0.22f).dp
    return min(target.value, maxDp).dp
}
