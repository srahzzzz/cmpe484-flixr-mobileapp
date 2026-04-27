package com.example.flixr.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.alpha
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.flixr.R
import com.example.flixr.auth.AuthViewModel
import com.example.flixr.ui.theme.FlixrGradientBottomLeft
import com.example.flixr.ui.theme.FlixrGradientTopRight
import kotlinx.coroutines.delay
import kotlin.math.min

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
                    isGoogleConfigured = viewModel.isGoogleConfigured(),
                    onLoginEmail = viewModel::loginEmail,
                    onSignUpEmail = viewModel::signUpEmail,
                    onGoogleClick = {
                        // Launch Google account picker. Firebase sign-in happens in the ViewModel after result.
                        googleLauncher.launch(viewModel.buildGoogleSignInIntent())
                    },
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
    isGoogleConfigured: Boolean,
    onLoginEmail: (email: String, password: String) -> Unit,
    onSignUpEmail: (username: String, email: String, password: String) -> Unit,
    onGoogleClick: () -> Unit,
) {
    // Local mini-navigation (keeps dependencies low for coursework).
    var screen by remember { mutableStateOf(LandingScreen.Welcome) }

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
    val usernameOk =
        runCatching {
            com.example.flixr.auth.Username.validateOrThrow(
                com.example.flixr.auth.Username.normalize(username)
            )
            true
        }.getOrDefault(false)
    val emailOk = email.trim().contains("@") && email.trim().contains(".")
    val passwordOk = password.length >= 6
    val canSubmit = !isBusy && usernameOk && emailOk && passwordOk

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
                    modifier = Modifier.fillMaxWidth().alpha(0.9f),
                    textAlign = TextAlign.Center,
                )
            }

            Spacer(Modifier.height(26.dp))

            OutlinedButton(
                onClick = { onSignUp(username, email, password) },
                enabled = canSubmit,
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
private fun HomeScreen(
    username: String,
    email: String,
    isBusy: Boolean,
    errorMessage: String?,
    onClearError: () -> Unit,
    onSignOut: () -> Unit,
) {
    Column(
        modifier = Modifier.padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Home", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text("Username: $username")
        Text("Email: $email")

        if (!errorMessage.isNullOrBlank()) {
            Text(text = errorMessage, color = MaterialTheme.colorScheme.error)
        }

        Button(
            onClick = {
                onClearError()
                onSignOut()
            },
            enabled = !isBusy,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Sign out")
        }

        if (isBusy) LinearBusy()
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
