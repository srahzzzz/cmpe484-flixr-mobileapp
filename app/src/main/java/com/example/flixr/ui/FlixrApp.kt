@file:OptIn(ExperimentalMaterial3Api::class)

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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.zIndex
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
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
import androidx.compose.foundation.border
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as lazyGridItems
import androidx.compose.material3.Checkbox

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.rememberModalBottomSheetState
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
import com.example.flixr.movies.EpisodeTrackingRepository
import com.example.flixr.movies.loadBrowseTabResults
import com.example.flixr.movies.MoodPresets
import com.example.flixr.movies.Movie
import com.example.flixr.movies.MovieDetails
import com.example.flixr.movies.SavedMovieRepository
import com.example.flixr.movies.TmdbClient
import com.example.flixr.movies.TvShowItem
import com.example.flixr.movies.TvSeasonDetails
import com.example.flixr.movies.WatchHistoryRepository
import com.example.flixr.messages.MessageRepository
import com.example.flixr.reviews.LikeRepository
import com.example.flixr.reviews.Review
import com.example.flixr.reviews.ReviewCommentRepository
import com.example.flixr.reviews.ReviewRepository
import com.example.flixr.social.FollowRepository
import com.example.flixr.social.UserDiscoveryRepository
import com.example.flixr.notifications.NotificationRepository
import com.example.flixr.lists.UserList
import com.example.flixr.lists.UserListRepository
import com.example.flixr.prefs.ThemeMode
import com.example.flixr.prefs.ThemePreferences
import kotlin.random.Random
import com.example.flixr.stats.AnalyticsRepository
import com.example.flixr.stats.UserAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.example.flixr.ui.theme.FlixrGradientBottomLeft
import com.example.flixr.ui.theme.FlixrGradientTopRight
import com.example.flixr.ui.theme.flixrHeroPrimaryText
import com.example.flixr.ui.theme.flixrHeroSecondaryText
import com.example.flixr.ui.theme.flixrMainSurfaceGradientBrush
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.min
import android.util.Patterns
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.offset
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.NavigationBarItemDefaults
import com.example.flixr.movies.discoverMoviesFiltered
import com.example.flixr.ui.theme.FlixrAccent
import com.example.flixr.ui.theme.FlixrAccentDim
import com.example.flixr.ui.theme.FlixrGold
import com.example.flixr.ui.theme.FlixrLiveRed
import com.example.flixr.ui.theme.FlixrMuted
import com.example.flixr.ui.theme.FlixrSurface
import com.example.flixr.ui.theme.FlixrSurfaceBright
import com.example.flixr.ui.theme.flixrNavigationBarContainerColor

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
fun FlixrApp(
    viewModel: AuthViewModel = viewModel(),
    themePreferences: ThemePreferences? = null,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    // In-app splash (custom gradient + logo + "Flixr") for a FIXED duration.
    // We do NOT use flixeropeningpage.png here (only used as inspiration).
    var showSplash by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        delay(3000)
        showSplash = false
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
                    onLoginEmail = viewModel::loginEmail,
                    onSignUpEmail = viewModel::signUpEmail,
                    onPasswordResetEmail = viewModel::sendPasswordReset,
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
                    authViewModel = viewModel,
                    username = state.profile?.username.orEmpty(),
                    email = state.profile?.email ?: state.firebaseUser?.email.orEmpty(),
                    isBusy = state.isBusy,
                    errorMessage = state.errorMessage,
                    themePreferences = themePreferences,
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
    onLoginEmail: (email: String, password: String) -> Unit,
    onSignUpEmail: (username: String, email: String, password: String) -> Unit,
    onPasswordResetEmail: (email: String) -> Unit,
    onClearSuccess: () -> Unit,
) {
    // Local mini-navigation (keeps dependencies low for coursework).
    var screen by remember { mutableStateOf(LandingScreen.Welcome) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(successMessage, screen) {
        val msg = successMessage
        if (!msg.isNullOrBlank()) {
            if (msg.contains("created", ignoreCase = true)) {
                screen = LandingScreen.SignupSuccess
            } else if (screen != LandingScreen.ForgotPassword) {
                screen = LandingScreen.Login
                scope.launch {
                    snackbarHostState.showSnackbar(message = msg, withDismissAction = true)
                }
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
                    containerColor = MaterialTheme.colorScheme.inverseSurface,
                    contentColor = MaterialTheme.colorScheme.inverseOnSurface,
                    actionColor = MaterialTheme.colorScheme.primary,
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
                }
            }

            LandingScreen.Login -> {
                LoginScreen(
                    isBusy = isBusy,
                    errorMessage = errorMessage,
                    onBack = { screen = LandingScreen.Welcome },
                    onLoginEmail = onLoginEmail,
                    onForgotPassword = { screen = LandingScreen.ForgotPassword },
                )
            }

            LandingScreen.ForgotPassword -> {
                ForgotPasswordScreen(
                    isBusy = isBusy,
                    errorMessage = errorMessage,
                    onBack = { screen = LandingScreen.Login },
                    onSendResetLink = onPasswordResetEmail,
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

            LandingScreen.SignupSuccess -> {
                SignupSuccessScreen(onGoLogin = { screen = LandingScreen.Login })
            }
        }
    }
}

@Composable
private fun SignupSuccessScreen(onGoLogin: () -> Unit) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = Icons.Filled.CheckCircle,
            contentDescription = null,
            modifier = Modifier.size(120.dp),
            tint = Color.White,
        )
        Spacer(Modifier.height(24.dp))
        Text(
            text = "Account created!",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = "Your Flixr account is ready. Sign in to start tracking films and TV.",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White.copy(alpha = 0.85f),
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(36.dp))
        Button(
            onClick = onGoLogin,
            modifier = Modifier.fillMaxWidth().height(54.dp),
            shape = RoundedCornerShape(28.dp),
        ) {
            Text("Go to login", style = MaterialTheme.typography.titleMedium)
        }
    }
}

private enum class LandingScreen { Welcome, Login, ForgotPassword, Signup, SignupSuccess }

@Composable
private fun LoginScreen(
    isBusy: Boolean,
    errorMessage: String?,
    onBack: () -> Unit,
    onLoginEmail: (email: String, password: String) -> Unit,
    onForgotPassword: () -> Unit,
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

            TextButton(
                onClick = onForgotPassword,
                enabled = !isBusy,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Forgot password?", color = Color.White.copy(alpha = 0.95f))
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
        }
    }
}

@Composable
private fun ForgotPasswordScreen(
    isBusy: Boolean,
    errorMessage: String?,
    onBack: () -> Unit,
    onSendResetLink: (email: String) -> Unit,
) {
    var email by remember { mutableStateOf("") }
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
            Text(
                text = "Reset password",
                style = MaterialTheme.typography.titleLarge.copy(fontSize = 32.sp),
                color = Color.White,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Enter your account email. Firebase will send a reset link if the account exists.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.78f),
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(28.dp))
            Text(
                text = "EMAIL",
                style = MaterialTheme.typography.labelLarge,
                color = Color.White.copy(alpha = 0.75f),
                modifier = Modifier.fillMaxWidth(),
            )
            PillTextField(
                value = email,
                onValueChange = { email = it },
                placeholder = "you@example.com",
                keyboardType = KeyboardType.Email,
                enabled = !isBusy,
            )
            if (!errorMessage.isNullOrBlank()) {
                Spacer(Modifier.height(12.dp))
                Text(
                    text = errorMessage,
                    color = Color.White.copy(alpha = 0.9f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            Spacer(Modifier.height(24.dp))
            OutlinedButton(
                onClick = { onSendResetLink(email) },
                enabled = !isBusy && email.isNotBlank(),
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
                    text = if (isBusy) "Sending…" else "Send reset link",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                )
            }
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
private fun HomeSectionTitle(
    text: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = text.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = FlixrMuted,
            letterSpacing = 1.2.sp,
        )
        if (actionLabel != null && onAction != null) {
            TextButton(
                onClick = onAction,
                contentPadding = PaddingValues(0.dp),
            ) {
                Text(
                    text = actionLabel,
                    color = FlixrAccent,
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }
    }
}

private fun activityAvatarColor(userId: String): Color {
    val palette =
        listOf(
            Color(0xFF22C55E),
            Color(0xFF3B82F6),
            Color(0xFFEC4899),
            Color(0xFFF59E0B),
            Color(0xFF14B8A6),
        )
    val idx = (userId.hashCode() and Int.MAX_VALUE) % palette.size
    return palette[idx]
}

private fun movieFromTmdbDetails(d: MovieDetails): Movie =
    Movie(
        id = d.id,
        title = d.title.orEmpty().ifBlank { "Movie ${d.id}" },
        posterPath = d.posterPath,
        releaseDate = d.releaseDate,
        overview = d.overview,
        voteAverage = d.voteAverage,
    )

private val BrowseGenreFilters: List<Pair<String?, String>> =
    listOf(
        null to "All",
        "28" to "Action",
        "35" to "Comedy",
        "18" to "Drama",
        "27" to "Horror",
        "878" to "Sci-Fi",
        "10749" to "Romance",
    )

@Composable
private fun HomeScreen(
    authViewModel: AuthViewModel,
    username: String,
    email: String,
    isBusy: Boolean,
    errorMessage: String?,
    themePreferences: ThemePreferences?,
    onClearError: () -> Unit,
    onSignOut: () -> Unit,
) {
    var screen by remember { mutableStateOf("main") } // main|details|followers|following|messages|chat|profile|userSearch|notifications|lists|listDetail
    var chatPeerUid by remember { mutableStateOf<String?>(null) }
    var profileUid by remember { mutableStateOf<String?>(null) }
    var detailsReturnTab by remember { mutableStateOf(0) }
    var mainTab by remember { mutableStateOf(0) } // 0 Home, 1 Discover, 2 Mood, 3 Activity, 4 Me
    var showTvSearch by remember { mutableStateOf(false) }
    var browseGenreId by remember { mutableStateOf<String?>(null) }
    var browseMoodId by remember { mutableStateOf<String?>(null) }
    var browseYearFrom by remember { mutableStateOf(1990) }
    var browseYearTo by remember { mutableStateOf(2026) }
    var browseMinRating by remember { mutableStateOf(0f) }
    var query by remember { mutableStateOf("") }
    var results by remember { mutableStateOf<List<Movie>>(emptyList()) }
    var trendingMovies by remember { mutableStateOf<List<Movie>>(emptyList()) }
    var popularMovies by remember { mutableStateOf<List<Movie>>(emptyList()) }
    var newReleases by remember { mutableStateOf<List<Movie>>(emptyList()) }
    var watchlistPreview by remember { mutableStateOf<List<Movie>>(emptyList()) }
    var moodSelectedId by remember { mutableStateOf("thrilled") }
    var moodRefresh by remember { mutableStateOf(0) }
    var moodMovies by remember { mutableStateOf<List<Movie>>(emptyList()) }
    var moodLoading by remember { mutableStateOf(false) }
    var isLoadingTrending by remember { mutableStateOf(true) }
    var selected by remember { mutableStateOf<Movie?>(null) }
    var tvTrackingShow by remember { mutableStateOf<TvShowItem?>(null) }
    var localError by remember { mutableStateOf<String?>(null) }
    var isSearching by remember { mutableStateOf(false) }
    var isDiscoverLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    val reviewRepo = remember { ReviewRepository() }
    val likeRepo = remember { LikeRepository() }
    val followRepo = remember { FollowRepository() }
    val discoveryRepo = remember { UserDiscoveryRepository() }
    val savedRepo = remember { SavedMovieRepository() }
    val watchHistoryRepo = remember { WatchHistoryRepository() }
    val analyticsRepo = remember { AnalyticsRepository() }
    val episodeRepo = remember { EpisodeTrackingRepository() }
    val reviewCommentRepo = remember { ReviewCommentRepository() }
    val messageRepo = remember { MessageRepository() }
    val notificationRepo = remember { NotificationRepository() }
    val userListRepo = remember { UserListRepository() }
    var unreadNotifications by remember { mutableIntStateOf(0) }
    var selectedUserList by remember { mutableStateOf<UserList?>(null) }
    var trackedTvShows by remember { mutableStateOf<List<TvShowItem>>(emptyList()) }

    var followingIds by remember { mutableStateOf<List<String>>(emptyList()) }
    var tvShowProgress by remember { mutableStateOf<List<TvShowHomeProgress>>(emptyList()) }
    var browseRefresh by remember { mutableStateOf(0) }
    val uid = FirebaseAuth.getInstance().currentUser?.uid

    LaunchedEffect(uid) {
        val u = uid
        if (u.isNullOrBlank()) {
            followingIds = emptyList()
            watchlistPreview = emptyList()
            tvShowProgress = emptyList()
            return@LaunchedEffect
        }
        followRepo.listenFollowingIds(u).collect { followingIds = it }
        try {
            watchlistPreview = savedRepo.getWatchlistForUser(u).take(6)
        } catch (_: Exception) {
            watchlistPreview = emptyList()
        }
        notificationRepo.listenUnreadCount(u).collect { unreadNotifications = it }
        val tmdbKey = BuildConfig.TMDB_API_KEY.trim()
        if (tmdbKey.isNotBlank()) {
            try {
                val ids = episodeRepo.getTrackedShowIds(u)
                trackedTvShows =
                    ids.take(8).mapNotNull { id ->
                        runCatching {
                            TmdbClient.api.getTvDetails(id, tmdbKey).let { d ->
                                TvShowItem(
                                    id = d.id,
                                    name = d.name,
                                    posterPath = d.posterPath,
                                    firstAirDate = d.firstAirDate,
                                    overview = d.overview,
                                )
                            }
                        }.getOrNull()
                    }
            } catch (_: Exception) {
                trackedTvShows = emptyList()
            }
        }
    }

    LaunchedEffect(trackedTvShows, uid) {
        val u = uid
        val key = BuildConfig.TMDB_API_KEY.trim()
        if (u.isNullOrBlank() || key.isBlank() || trackedTvShows.isEmpty()) {
            tvShowProgress = emptyList()
            return@LaunchedEffect
        }
        tvShowProgress = loadTvShowHomeProgress(u, trackedTvShows, key, episodeRepo)
    }

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
            popularMovies = TmdbClient.api.getPopularMovies(apiKey).results
            newReleases = TmdbClient.api.getNowPlayingMovies(apiKey).results
        } catch (e: Exception) {
            feedError = e.message ?: "Failed to load trending movies."
        } finally {
            isLoadingTrending = false
        }
        if (feedError != null) {
            localError = feedError
        }
    }

    LaunchedEffect(moodSelectedId, moodRefresh) {
        val apiKey = BuildConfig.TMDB_API_KEY.trim()
        if (apiKey.isBlank()) return@LaunchedEffect
        moodLoading = true
        try {
            val page = Random.nextInt(1, 6)
            val seed = System.currentTimeMillis() + moodRefresh
            moodMovies =
                discoverMoviesFiltered(
                    api = TmdbClient.api,
                    apiKey = apiKey,
                    genreId = null,
                    moodId = moodSelectedId,
                    yearFrom = 1990,
                    yearTo = 2026,
                    minVoteUser = 0f,
                    page = page,
                    shuffleSeed = seed,
                ).take(16)
        } catch (_: Exception) {
            moodMovies = emptyList()
        } finally {
            moodLoading = false
        }
    }

    fun runBrowseLoad() {
        val apiKey = BuildConfig.TMDB_API_KEY.trim()
        if (apiKey.isBlank()) {
            localError = "TMDB API key missing. Add TMDB_API_KEY to local.properties, sync, and rebuild."
            return
        }
        localError = null
        focusManager.clearFocus()
        scope.launch {
            isSearching = true
            isDiscoverLoading = true
            try {
                results =
                    loadBrowseTabResults(
                        api = TmdbClient.api,
                        apiKey = apiKey,
                        query = query,
                        genreId = browseGenreId,
                        moodId = browseMoodId,
                        yearFrom = browseYearFrom,
                        yearTo = browseYearTo,
                        minVote = browseMinRating,
                        shuffleSeed =
                            if (browseMoodId != null && query.trim().isBlank()) {
                                System.currentTimeMillis() + browseRefresh
                            } else {
                                null
                            },
                    )
            } catch (e: Exception) {
                localError = e.message ?: "Could not load titles."
            } finally {
                isSearching = false
                isDiscoverLoading = false
            }
        }
    }

    LaunchedEffect(mainTab, browseGenreId, browseMoodId, browseYearFrom, browseYearTo, browseMinRating, browseRefresh) {
        if (mainTab != 1) return@LaunchedEffect
        val apiKey = BuildConfig.TMDB_API_KEY.trim()
        if (apiKey.isBlank()) return@LaunchedEffect
        if (query.trim().isNotBlank()) return@LaunchedEffect
        isDiscoverLoading = true
        localError = null
        val seed = if (browseMoodId != null) System.currentTimeMillis() + browseRefresh else null
        try {
            results =
                loadBrowseTabResults(
                    api = TmdbClient.api,
                    apiKey = apiKey,
                    query = "",
                    genreId = browseGenreId,
                    moodId = browseMoodId,
                    yearFrom = browseYearFrom,
                    yearTo = browseYearTo,
                    minVote = browseMinRating,
                    shuffleSeed = seed,
                )
        } catch (e: Exception) {
            localError = e.message ?: "Browse failed."
        } finally {
            isDiscoverLoading = false
        }
    }

    fun openProfile(uid: String) {
        profileUid = uid
        screen = "profile"
    }

    fun openMovieFromContentId(contentId: String, returnTab: Int) {
        val mid = contentId.toIntOrNull() ?: 0
        selected =
            Movie(
                id = mid,
                title = "TMDB $mid",
                posterPath = null,
                releaseDate = null,
                overview = null,
                voteAverage = null,
            )
        detailsReturnTab = returnTab
        screen = "details"
    }

    val apiKeyTrimmed = BuildConfig.TMDB_API_KEY.trim()
    if (tvTrackingShow != null && apiKeyTrimmed.isNotBlank()) {
        TvShowTrackingScreen(
            show = tvTrackingShow!!,
            episodeRepo = episodeRepo,
            apiKey = apiKeyTrimmed,
            onBack = { tvTrackingShow = null },
        )
        return
    }

    if (screen == "details" && selected != null) {
        MovieDetailsScreen(
            movie = selected!!,
            reviewRepo = reviewRepo,
            reviewCommentRepo = reviewCommentRepo,
            likeRepo = likeRepo,
            savedRepo = savedRepo,
            watchHistoryRepo = watchHistoryRepo,
            userListRepo = userListRepo,
            discoveryRepo = discoveryRepo,
            onBack = {
                screen = "main"
                mainTab = detailsReturnTab
            },
            onOpenProfile = { openProfile(it) },
        )
        return
    }

    if (screen == "profile" && profileUid != null) {
        UserProfileScreen(
            profileUid = profileUid!!,
            followRepo = followRepo,
            reviewRepo = reviewRepo,
            discoveryRepo = discoveryRepo,
            onBack = {
                screen = "main"
                mainTab = 4
            },
            onOpenMovie = { openMovieFromContentId(it, detailsReturnTab) },
            onMessage = { peer ->
                chatPeerUid = peer
                screen = "chat"
            },
        )
        return
    }

    if (screen == "following") {
        FollowingScreen(
            followRepo = followRepo,
            discoveryRepo = discoveryRepo,
            onBack = {
                screen = "main"
                mainTab = 4
            },
            onOpenProfile = { openProfile(it) },
            onUnfollow = { fid ->
                val u = uid
                if (!u.isNullOrBlank()) {
                    scope.launch {
                        try {
                            followRepo.unfollow(u, fid)
                        } catch (_: Exception) {
                        }
                    }
                }
            },
        )
        return
    }

    if (screen == "userSearch") {
        UserSearchScreen(
            discoveryRepo = discoveryRepo,
            followRepo = followRepo,
            onBack = {
                screen = "main"
                mainTab = 4
            },
            onOpenProfile = { openProfile(it) },
        )
        return
    }

    if (screen == "notifications") {
        NotificationsScreen(
            notificationRepo = notificationRepo,
            onBack = { screen = "main"; mainTab = 0 },
            onOpenProfile = { openProfile(it) },
            onOpenChat = { peer ->
                chatPeerUid = peer
                screen = "chat"
            },
            onOpenMovie = { openMovieFromContentId(it, 0) },
        )
        return
    }

    if (screen == "lists") {
        UserListsScreen(
            listRepo = userListRepo,
            onBack = { screen = "main"; mainTab = 4 },
            onOpenList = { list ->
                selectedUserList = list
                screen = "listDetail"
            },
        )
        return
    }

    if (screen == "listDetail" && selectedUserList != null) {
        UserListDetailRoute(
            list = selectedUserList!!,
            listRepo = userListRepo,
            onBack = { screen = "lists" },
            onListUpdated = { selectedUserList = it },
            onOpenMovie = { movie ->
                detailsReturnTab = 4
                selected = movie
                screen = "details"
            },
        )
        return
    }

    if (screen == "followers") {
        FollowersScreen(
            followRepo = followRepo,
            discoveryRepo = discoveryRepo,
            onBack = {
                screen = "main"
                mainTab = 4
            },
            onOpenProfile = { openProfile(it) },
            onMessage = { peer ->
                chatPeerUid = peer
                screen = "chat"
            },
        )
        return
    }

    if (screen == "messages") {
        MessagesHomeScreen(
            followRepo = followRepo,
            discoveryRepo = discoveryRepo,
            onBack = {
                screen = "main"
                mainTab = 4
            },
            onOpenChat = { peer ->
                chatPeerUid = peer
                screen = "chat"
            },
            onOpenProfile = { openProfile(it) },
        )
        return
    }

    if (screen == "chat" && chatPeerUid != null && uid != null) {
        ChatScreen(
            myUid = uid!!,
            peerUid = chatPeerUid!!,
            messageRepo = messageRepo,
            onBack = {
                screen = "messages"
            },
        )
        return
    }

    if (screen == "watchlist") {
        WatchlistScreen(
            savedRepo = savedRepo,
            onBack = { screen = "main"; mainTab = 4 },
            onOpenMovie = { movie ->
                detailsReturnTab = 4
                selected = movie
                screen = "details"
            },
        )
        return
    }

    if (screen == "watched") {
        WatchedHistoryScreen(
            watchHistoryRepo = watchHistoryRepo,
            onBack = { screen = "main"; mainTab = 4 },
            onOpenMovie = { movie ->
                detailsReturnTab = 4
                selected = movie
                screen = "details"
            },
        )
        return
    }

    if (screen == "analytics") {
        AnalyticsScreen(
            analyticsRepo = analyticsRepo,
            tmdbApiKey = BuildConfig.TMDB_API_KEY.trim(),
            onBack = { screen = "main"; mainTab = 4 },
            onOpenWatched = {
                screen = "watched"
            },
        )
        return
    }

    if (showTvSearch && apiKeyTrimmed.isNotBlank()) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(flixrMainSurfaceGradientBrush())
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
        ) {
            FlixrSubScreenTopBar(title = "Track TV", onBack = { showTvSearch = false })
            TvShowsTab(
                apiKey = apiKeyTrimmed,
                onPickShow = {
                    tvTrackingShow = it
                    showTvSearch = false
                },
                onMissingKey = { localError = it },
            )
        }
        return
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(flixrMainSurfaceGradientBrush()),
        ) {
            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .statusBarsPadding(),
            ) {
                Column(
                    modifier =
                        Modifier
                            .weight(1f)
                            .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    when (mainTab) {
                        0 ->
                            HomeTabHeader(
                                username = username,
                                unreadNotifications = unreadNotifications,
                                onNotifications = { screen = "notifications" },
                            )

                        1 ->
                            DiscoverTabHeader(
                                onTvShows = {
                                    if (apiKeyTrimmed.isBlank()) {
                                        localError = "TMDB API key missing."
                                    } else {
                                        showTvSearch = true
                                    }
                                },
                            )

                        2 ->
                            MoodMatchPageHeader(
                                onSparkle = { moodRefresh++ },
                            )

                        3 -> Spacer(Modifier.height(4.dp))

                        else ->
                            SectionTitleBar(
                                title = "Profile",
                                subtitle = "Account & lists",
                            )
                    }

                    if (!localError.isNullOrBlank()) {
                        Text(text = localError!!, color = MaterialTheme.colorScheme.error)
                    }
                    if (!errorMessage.isNullOrBlank()) {
                        Text(text = errorMessage, color = MaterialTheme.colorScheme.error)
                    }

                    when (mainTab) {
                        0 ->
                            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                                HomeFeedTab(
                                    trendingMovies = trendingMovies,
                                    popularMovies = popularMovies,
                                    newReleases = newReleases,
                                    watchlistPreview = watchlistPreview,
                                    tvShowProgress = tvShowProgress,
                                    isLoadingTrending = isLoadingTrending,
                                    onOpenTvShow = { tvTrackingShow = it },
                                    onMovieClick = { movie ->
                                        detailsReturnTab = 0
                                        selected = movie
                                        screen = "details"
                                    },
                                )
                            }

                        1 ->
                            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                                BrowseMoviesTab(
                                    query = query,
                                    onQueryChange = { query = it },
                                    browseGenreId = browseGenreId,
                                    onGenreChange = {
                                        browseGenreId = it
                                        browseMoodId = null
                                        if (query.trim().isBlank()) {
                                            scope.launch {
                                                val key = BuildConfig.TMDB_API_KEY.trim()
                                                if (key.isBlank()) return@launch
                                                isDiscoverLoading = true
                                                try {
                                                    results =
                                                        loadBrowseTabResults(
                                                            api = TmdbClient.api,
                                                            apiKey = key,
                                                            query = "",
                                                            genreId = browseGenreId,
                                                            moodId = browseMoodId,
                                                            yearFrom = browseYearFrom,
                                                            yearTo = browseYearTo,
                                                            minVote = browseMinRating,
                                                        )
                                                } catch (e: Exception) {
                                                    localError = e.message ?: "Browse failed."
                                                } finally {
                                                    isDiscoverLoading = false
                                                }
                                            }
                                        }
                                    },
                                    browseMoodId = browseMoodId,
                                    onMoodChange = {
                                        if (it == browseMoodId && it != null) {
                                            browseRefresh++
                                        } else {
                                            browseMoodId = it
                                            if (it != null) browseRefresh++
                                        }
                                        browseGenreId = null
                                        if (query.trim().isBlank()) {
                                            scope.launch {
                                                val key = BuildConfig.TMDB_API_KEY.trim()
                                                if (key.isBlank()) return@launch
                                                isDiscoverLoading = true
                                                try {
                                                    results =
                                                        loadBrowseTabResults(
                                                            api = TmdbClient.api,
                                                            apiKey = key,
                                                            query = "",
                                                            genreId = browseGenreId,
                                                            moodId = browseMoodId,
                                                            yearFrom = browseYearFrom,
                                                            yearTo = browseYearTo,
                                                            minVote = browseMinRating,
                                                            shuffleSeed =
                                                                if (browseMoodId != null) {
                                                                    System.currentTimeMillis() + browseRefresh
                                                                } else {
                                                                    null
                                                                },
                                                        )
                                                } catch (e: Exception) {
                                                    localError = e.message ?: "Browse failed."
                                                } finally {
                                                    isDiscoverLoading = false
                                                }
                                            }
                                        }
                                    },
                                    onMoodReshuffle = { browseRefresh++ },
                                    browseYearFrom = browseYearFrom,
                                    browseYearTo = browseYearTo,
                                    browseMinRating = browseMinRating,
                                    onYearFromChange = { browseYearFrom = it },
                                    onYearToChange = { browseYearTo = it },
                                    onMinRatingChange = { browseMinRating = it },
                                    results = results,
                                    isSearching = isSearching || isDiscoverLoading,
                                    genreFilters = BrowseGenreFilters,
                                    onSearchClick = { runBrowseLoad() },
                                    onClear = {
                                        results = emptyList()
                                        query = ""
                                        browseGenreId = null
                                        browseMoodId = null
                                        browseYearFrom = 1990
                                        browseYearTo = 2026
                                        browseMinRating = 0f
                                        localError = null
                                    },
                                    onMovieClick = { movie ->
                                        detailsReturnTab = 1
                                        selected = movie
                                        screen = "details"
                                    },
                                )
                            }

                        2 ->
                            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                                MoodMatchTab(
                                    selectedMoodId = moodSelectedId,
                                    onSelectMood = { moodSelectedId = it },
                                    moodMovies = moodMovies,
                                    isLoading = moodLoading,
                                    onMovieClick = { movie ->
                                        detailsReturnTab = 2
                                        selected = movie
                                        screen = "details"
                                    },
                                )
                            }

                        3 ->
                            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                                ActivityHubScreen(
                                    reviewRepo = reviewRepo,
                                    likeRepo = likeRepo,
                                    discoveryRepo = discoveryRepo,
                                    followRepo = followRepo,
                                    followingIds = followingIds,
                                    onOpenMovieFromContentId = { openMovieFromContentId(it, 3) },
                                    onOpenProfile = { openProfile(it) },
                                    onMessage = { peer ->
                                        chatPeerUid = peer
                                        screen = "chat"
                                    },
                                    onFollowers = { screen = "followers" },
                                    onFollowing = { screen = "following" },
                                    onMessages = { screen = "messages" },
                                )
                            }

                        else ->
                            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                                MeTab(
                                    authViewModel = authViewModel,
                                    reviewRepo = reviewRepo,
                                    themePreferences = themePreferences,
                                    onWatchlist = { screen = "watchlist" },
                                    onWatchedHistory = { screen = "watched" },
                                    onLists = { screen = "lists" },
                                    onAnalytics = { screen = "analytics" },
                                    onOpenReviewMovie = { openMovieFromContentId(it, 4) },
                                    onSignOut = {
                                        onClearError()
                                        onSignOut()
                                    },
                                    isBusy = isBusy,
                                )
                            }
                    }
                }

                val navItemColors =
                    NavigationBarItemDefaults.colors(
                        selectedIconColor = FlixrAccent,
                        selectedTextColor = FlixrAccent,
                        indicatorColor = FlixrAccent.copy(alpha = 0.22f),
                        unselectedIconColor = FlixrMuted,
                        unselectedTextColor = FlixrMuted,
                    )
                NavigationBar(
                    containerColor = flixrNavigationBarContainerColor(),
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    tonalElevation = 0.dp,
                ) {
                    NavigationBarItem(
                        selected = mainTab == 0,
                        onClick = { mainTab = 0 },
                        icon = { Icon(Icons.Filled.Home, contentDescription = null) },
                        label = { Text("Home") },
                        colors = navItemColors,
                    )
                    NavigationBarItem(
                        selected = mainTab == 1,
                        onClick = { mainTab = 1 },
                        icon = { Icon(Icons.Filled.Search, contentDescription = null) },
                        label = { Text("Discover") },
                        colors = navItemColors,
                    )
                    NavigationBarItem(
                        selected = mainTab == 2,
                        onClick = { mainTab = 2 },
                        icon = { Icon(Icons.Filled.Star, contentDescription = null) },
                        label = { Text("Mood") },
                        colors = navItemColors,
                    )
                    NavigationBarItem(
                        selected = mainTab == 3,
                        onClick = { mainTab = 3 },
                        icon = { Icon(Icons.Filled.Notifications, contentDescription = null) },
                        label = { Text("Activity") },
                        colors = navItemColors,
                    )
                    NavigationBarItem(
                        selected = mainTab == 4,
                        onClick = { mainTab = 4 },
                        icon = { Icon(Icons.Filled.Person, contentDescription = null) },
                        label = { Text("Profile") },
                        colors = navItemColors,
                    )
                }
            }
        }

    }
}

@Composable
private fun HomeTabHeader(
    username: String,
    unreadNotifications: Int = 0,
    onNotifications: () -> Unit = {},
) {
    val first =
        username
            .trim()
            .split(" ", "_", ".")
            .firstOrNull()
            ?.takeIf { it.isNotBlank() }
            ?.replaceFirstChar { c -> c.uppercase() }
            ?: "there"
    val initial = (first.firstOrNull() ?: 'F').uppercaseChar().toString()
    val appDarkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier =
                    Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(FlixrAccent, FlixrAccentDim),
                            ),
                        ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = initial,
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            }
            Column {
                Text(
                    text = "Hey $first",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color =
                        if (appDarkTheme) {
                            Color.Black
                        } else {
                            flixrHeroPrimaryText()
                        },
                )
                Text(
                    text = "What do you want to watch?",
                    style = MaterialTheme.typography.bodySmall,
                    color = flixrHeroSecondaryText(),
                )
            }
        }
        Box {
            val heroIcon = flixrHeroPrimaryText()
                IconButton(onClick = onNotifications) {
                    Icon(
                        imageVector = Icons.Filled.Notifications,
                        contentDescription = "Notifications",
                        tint = heroIcon,
                    )
                }
                if (unreadNotifications > 0) {
                    Box(
                        modifier =
                            Modifier
                                .align(Alignment.TopEnd)
                                .offset(x = 4.dp, y = 4.dp)
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(FlixrLiveRed),
                    )
                }
            }
    }
}

@Composable
private fun DiscoverTabHeader(onTvShows: () -> Unit = {}) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Discover",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = flixrHeroPrimaryText(),
            )
            Text(
                text = "Search films · mood · genre",
                style = MaterialTheme.typography.bodySmall,
                color = flixrHeroSecondaryText(),
            )
        }
        OutlinedButton(
            onClick = onTvShows,
            shape = RoundedCornerShape(14.dp),
        ) {
            Icon(Icons.Filled.LiveTv, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.size(6.dp))
            Text("TV shows")
        }
    }
}

@Composable
private fun MoodMatchPageHeader(
    onSparkle: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column {
            Text(
                text = "Mood Match",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = flixrHeroPrimaryText(),
            )
            Text(
                text = "How are you feeling tonight?",
                style = MaterialTheme.typography.bodySmall,
                color = flixrHeroSecondaryText(),
            )
        }
        IconButton(
            onClick = onSparkle,
            modifier =
                Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(FlixrSurface),
        ) {
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = "Refresh picks",
                tint = FlixrGold,
            )
        }
    }
}

@Composable
private fun SectionTitleBar(
    title: String,
    subtitle: String,
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = flixrHeroPrimaryText(),
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = flixrHeroSecondaryText(),
        )
    }
}

@Composable
private fun UserListDetailRoute(
    list: UserList,
    listRepo: UserListRepository,
    onBack: () -> Unit,
    onListUpdated: (UserList) -> Unit,
    onOpenMovie: (Movie) -> Unit,
) {
    var currentList by remember(list.list_id) { mutableStateOf(list) }
    var movies by remember { mutableStateOf<List<Movie>>(emptyList()) }
    LaunchedEffect(currentList.list_id, currentList.movie_ids) {
        val key = BuildConfig.TMDB_API_KEY.trim()
        movies =
            currentList.movie_ids.mapNotNull { mid ->
                val id = mid.toIntOrNull() ?: return@mapNotNull null
                if (key.isBlank()) {
                    Movie(id = id, title = "TMDB $id", posterPath = null, releaseDate = null, overview = null, voteAverage = null)
                } else {
                    runCatching {
                        movieFromTmdbDetails(TmdbClient.api.getMovieDetails(id, key))
                    }.getOrNull()
                        ?: Movie(id = id, title = "TMDB $id", posterPath = null, releaseDate = null, overview = null, voteAverage = null)
                }
            }
    }
    UserListDetailScreen(
        list = currentList,
        listRepo = listRepo,
        movies = movies,
        onBack = onBack,
        onListUpdated = { updated ->
            currentList = updated
            onListUpdated(updated)
        },
        onOpenMovie = onOpenMovie,
    )
}

private data class TvShowHomeProgress(
    val show: TvShowItem,
    val watchedCount: Int,
    val totalCount: Int,
    val nextEpisodeLabel: String?,
)

private suspend fun loadTvShowHomeProgress(
    uid: String,
    shows: List<TvShowItem>,
    apiKey: String,
    episodeRepo: EpisodeTrackingRepository,
): List<TvShowHomeProgress> =
    shows.mapNotNull { show ->
        runCatching {
            val watchedKeys = episodeRepo.getWatchedEpisodeKeys(uid, show.id)
            val details = TmdbClient.api.getTvDetails(show.id, apiKey)
            val n = (details.numberOfSeasons ?: 0).coerceAtMost(20)
            val seasons = mutableListOf<TvSeasonDetails>()
            for (s in 1..n) {
                try {
                    seasons.add(TmdbClient.api.getTvSeason(show.id, s, apiKey))
                } catch (_: Exception) {
                }
            }
            val total = seasons.sumOf { it.episodes.size }
            val watched =
                seasons.sumOf { season ->
                    season.episodes.count { ep ->
                        watchedKeys.contains("${season.seasonNumber}_${ep.episodeNumber}")
                    }
                }
            val next =
                seasons.firstNotNullOfOrNull { season ->
                    season.episodes.firstOrNull { ep ->
                        !watchedKeys.contains("${season.seasonNumber}_${ep.episodeNumber}")
                    }?.let { ep ->
                        "S${season.seasonNumber} E${ep.episodeNumber}"
                    }
                }
            TvShowHomeProgress(show, watched, total, next)
        }.getOrNull()
    }

@Composable
private fun HomeFeedTab(
    trendingMovies: List<Movie>,
    popularMovies: List<Movie>,
    newReleases: List<Movie>,
    watchlistPreview: List<Movie>,
    tvShowProgress: List<TvShowHomeProgress>,
    isLoadingTrending: Boolean,
    onMovieClick: (Movie) -> Unit,
    onOpenTvShow: (TvShowItem) -> Unit,
) {
    val recommendedHint =
        remember(watchlistPreview) {
            if (watchlistPreview.isNotEmpty()) {
                "Because you watched ${watchlistPreview.first().title}"
            } else {
                "Because your taste is impeccable"
            }
        }
    val recommendedShelf =
        remember(popularMovies, trendingMovies) {
            when {
                popularMovies.size >= 8 -> popularMovies.drop(2).take(8)
                else -> trendingMovies.drop(4).take(8)
            }
        }

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        if (isLoadingTrending && trendingMovies.isEmpty()) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().height(180.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = FlixrAccent)
                }
            }
        }

        if (tvShowProgress.isNotEmpty()) {
            item {
                HomeSectionTitle(text = "Your TV & episodes")
            }
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    items(tvShowProgress, key = { it.show.id }) { progress ->
                        val show = progress.show
                        Column(
                            modifier =
                                Modifier
                                    .width(110.dp)
                                    .clickable { onOpenTvShow(show) },
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                val url = tmdbPosterUrl(show.posterPath, width = 185)
                                if (url != null) {
                                    AsyncImage(
                                        model = url,
                                        contentDescription = show.name,
                                        modifier = Modifier.height(150.dp).fillMaxWidth(),
                                        contentScale = ContentScale.Crop,
                                    )
                                } else {
                                    Box(
                                        Modifier.height(150.dp).fillMaxWidth(),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Text(
                                            show.name,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.padding(6.dp),
                                        )
                                    }
                                }
                            }
                            Text(
                                show.name,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = flixrHeroPrimaryText(),
                            )
                            if (progress.totalCount > 0) {
                                Text(
                                    "${progress.watchedCount}/${progress.totalCount} watched",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = FlixrMuted,
                                )
                                LinearProgressIndicator(
                                    progress = {
                                        progress.watchedCount.toFloat() / progress.totalCount.toFloat()
                                    },
                                    modifier =
                                        Modifier
                                            .fillMaxWidth()
                                            .height(4.dp)
                                            .clip(RoundedCornerShape(99.dp)),
                                    color = FlixrAccent,
                                    trackColor = Color.White.copy(alpha = 0.12f),
                                )
                            }
                            progress.nextEpisodeLabel?.let { label ->
                                Text(
                                    "Next: $label",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = FlixrAccent,
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            HomeSectionTitle(
                text = "Trending today",
                actionLabel = "See all",
                onAction = { /* stays on home; full grids live on Discover */ },
            )
        }
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                items(trendingMovies.take(12), key = { it.id }) { movie ->
                    NetflixShelfMovieCard(movie = movie, onClick = { onMovieClick(movie) })
                }
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                HomeSectionTitle(text = "Recommended for you")
                Text(
                    text = recommendedHint,
                    style = MaterialTheme.typography.bodySmall,
                    color = FlixrMuted,
                )
            }
        }
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                items(recommendedShelf, key = { it.id }) { movie ->
                    NetflixShelfMovieCard(movie = movie, onClick = { onMovieClick(movie) })
                }
            }
        }

        item {
            HomeSectionTitle(text = "New in theaters")
        }
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                items(newReleases.take(12), key = { it.id }) { movie ->
                    NetflixShelfMovieCard(movie = movie, onClick = { onMovieClick(movie) })
                }
            }
        }
    }
}

@Composable
private fun MoodMatchTab(
    selectedMoodId: String,
    onSelectMood: (String) -> Unit,
    moodMovies: List<Movie>,
    isLoading: Boolean,
    onMovieClick: (Movie) -> Unit,
) {
    val moods = MoodPresets.all
    val scroll = rememberScrollState()
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .verticalScroll(scroll),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        moods.chunked(2).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                for (m in row) {
                    val sel = selectedMoodId == m.id
                    Surface(
                        modifier =
                            Modifier
                                .weight(1f)
                                .height(118.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .border(
                                    width = if (sel) 2.dp else 1.dp,
                                    color =
                                        if (sel) {
                                            FlixrAccent
                                        } else {
                                            Color.White.copy(alpha = 0.08f)
                                        },
                                    shape = RoundedCornerShape(16.dp),
                                )
                                .clickable { onSelectMood(m.id) },
                        color =
                            if (sel) {
                                FlixrSurfaceBright.copy(alpha = 0.95f)
                            } else {
                                FlixrSurface.copy(alpha = 0.85f)
                            },
                    ) {
                        Column(
                            modifier =
                                Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 10.dp, vertical = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                        ) {
                            Text(text = m.emoji, style = MaterialTheme.typography.headlineMedium)
                            Text(
                                text = m.label,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                textAlign = TextAlign.Center,
                            )
                            Text(
                                text = m.subtitle,
                                style = MaterialTheme.typography.labelSmall,
                                color = FlixrMuted,
                                textAlign = TextAlign.Center,
                                maxLines = 2,
                            )
                        }
                    }
                }
                if (row.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }

        val moodLabel = MoodPresets.byId(selectedMoodId)?.label?.uppercase().orEmpty()
        Text(
            text = "Because you chose $moodLabel",
            style = MaterialTheme.typography.labelSmall,
            color = FlixrMuted,
            letterSpacing = 1.1.sp,
        )

        if (isLoading && moodMovies.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth().height(160.dp),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = FlixrAccent)
            }
        } else {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                itemsIndexed(moodMovies, key = { _, m -> m.id }) { idx, movie ->
                    MoodMatchPosterCard(
                        movie = movie,
                        index = idx,
                        onClick = { onMovieClick(movie) },
                    )
                }
            }
        }
    }
}

@Composable
private fun MoodMatchPosterCard(
    movie: Movie,
    index: Int,
    onClick: () -> Unit,
) {
    val gradients =
        listOf(
            listOf(Color(0xFF5B21B6), Color(0xFF1E1B4B)),
            listOf(Color(0xFF78350F), Color(0xFF1C1917)),
            listOf(Color(0xFF991B1B), Color(0xFF450A0A)),
            listOf(Color(0xFF1E3A8A), Color(0xFF0F172A)),
        )
    val g = gradients[index % gradients.size]
    Column(
        modifier =
            Modifier
                .width(132.dp)
                .clickable(onClick = onClick),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(176.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Brush.verticalGradient(g)),
        ) {
            val url = tmdbPosterUrl(movie.posterPath, width = 342)
            if (url != null) {
                AsyncImage(
                    model = url,
                    contentDescription = movie.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
            }
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, Color.Black.copy(alpha = 0.65f)),
                            ),
                        ),
            )
            Text(
                text = movie.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier =
                    Modifier
                        .align(Alignment.BottomCenter)
                        .padding(10.dp),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun BrowseMoviesTab(
    query: String,
    onQueryChange: (String) -> Unit,
    browseGenreId: String?,
    onGenreChange: (String?) -> Unit,
    browseMoodId: String?,
    onMoodChange: (String?) -> Unit,
    onMoodReshuffle: () -> Unit,
    browseYearFrom: Int,
    browseYearTo: Int,
    browseMinRating: Float,
    onYearFromChange: (Int) -> Unit,
    onYearToChange: (Int) -> Unit,
    onMinRatingChange: (Float) -> Unit,
    results: List<Movie>,
    isSearching: Boolean,
    genreFilters: List<Pair<String?, String>>,
    onSearchClick: () -> Unit,
    onClear: () -> Unit,
    onMovieClick: (Movie) -> Unit,
) {
    var filterSheetOpen by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val movieRows = remember(results) { results.chunked(2) }

    if (filterSheetOpen) {
        ModalBottomSheet(
            onDismissRequest = { filterSheetOpen = false },
            sheetState = sheetState,
        ) {
            Column(
                modifier =
                    Modifier
                        .padding(horizontal = 20.dp)
                        .padding(bottom = 28.dp)
                        .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    "Filters",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    "Release years apply to TMDB discover when the search box is empty.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text("Release year (from)", style = MaterialTheme.typography.labelLarge)
                Slider(
                    value = browseYearFrom.toFloat(),
                    onValueChange = { v ->
                        val y = v.toInt().coerceIn(1950, 2030)
                        onYearFromChange(y)
                        if (y > browseYearTo) onYearToChange(y)
                    },
                    valueRange = 1950f..2030f,
                    steps = 79,
                )
                Text("Release year (to)", style = MaterialTheme.typography.labelLarge)
                Slider(
                    value = browseYearTo.toFloat(),
                    onValueChange = { v ->
                        val y = v.toInt().coerceIn(1950, 2030)
                        onYearToChange(y)
                        if (y < browseYearFrom) onYearFromChange(y)
                    },
                    valueRange = 1950f..2030f,
                    steps = 79,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(
                        "All time" to (1950 to 2030),
                        "2020s" to (2020 to 2026),
                        "2010s" to (2010 to 2019),
                    ).forEach { (label, range) ->
                        FilterChip(
                            selected = browseYearFrom == range.first && browseYearTo == range.second,
                            onClick = {
                                onYearFromChange(range.first)
                                onYearToChange(range.second)
                            },
                            label = { Text(label) },
                        )
                    }
                }
                Text("Minimum TMDB score (0–10)", style = MaterialTheme.typography.labelLarge)
                Slider(
                    value = browseMinRating,
                    onValueChange = onMinRatingChange,
                    valueRange = 0f..10f,
                )
                Button(
                    onClick = { filterSheetOpen = false },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Done")
                }
            }
        }
    }

    LazyColumn(
        modifier =
            Modifier
                .fillMaxSize()
                .imePadding(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
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
                        focusedContainerColor = FlixrSurface.copy(alpha = 0.65f),
                        unfocusedContainerColor = FlixrSurface.copy(alpha = 0.45f),
                    ),
                label = { Text("Search films…") },
                placeholder = { Text("Try mood or genre chips below") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                keyboardActions = KeyboardActions(onSearch = { onSearchClick() }),
            )
        }
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = onSearchClick,
                    enabled = !isSearching && query.trim().isNotBlank(),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                ) {
                    Text(if (isSearching) "Loading..." else "Search")
                }
                OutlinedButton(
                    onClick = { filterSheetOpen = true },
                    enabled = !isSearching,
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Filters")
                }
                TextButton(onClick = onClear, enabled = !isSearching) {
                    Text("Clear")
                }
            }
        }
        item {
            val hasActiveFilters =
                browseMoodId != null ||
                    browseGenreId != null ||
                    browseYearFrom != 1990 ||
                    browseYearTo != 2026 ||
                    browseMinRating > 0.05f
            if (hasActiveFilters) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    if (browseMoodId != null) {
                        item {
                            val moodLabel = MoodPresets.byId(browseMoodId)?.label ?: browseMoodId
                            FilterChip(
                                selected = true,
                                onClick = { onMoodChange(null) },
                                label = { Text("Mood: $moodLabel ×") },
                            )
                        }
                    }
                    if (browseGenreId != null) {
                        item {
                            val genreLabel =
                                genreFilters.firstOrNull { it.first == browseGenreId }?.second ?: browseGenreId
                            FilterChip(
                                selected = true,
                                onClick = { onGenreChange(null) },
                                label = { Text("Genre: $genreLabel ×") },
                            )
                        }
                    }
                    if (browseYearFrom != 1990 || browseYearTo != 2026) {
                        item {
                            FilterChip(
                                selected = true,
                                onClick = {
                                    onYearFromChange(1990)
                                    onYearToChange(2026)
                                },
                                label = { Text("Years: $browseYearFrom–$browseYearTo ×") },
                            )
                        }
                    }
                    if (browseMinRating > 0.05f) {
                        item {
                            FilterChip(
                                selected = true,
                                onClick = { onMinRatingChange(0f) },
                                label = { Text("Min ★ ${String.format("%.1f", browseMinRating)} ×") },
                            )
                        }
                    }
                }
            }
        }
        item {
            Text(
                text = "Mood",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    FilterChip(
                        selected = browseMoodId == null,
                        onClick = { onMoodChange(null) },
                        label = { Text("Any") },
                    )
                }
                items(MoodPresets.all.size, key = { MoodPresets.all[it].id }) { idx ->
                    val m = MoodPresets.all[idx]
                    FilterChip(
                        selected = browseMoodId == m.id,
                        onClick = {
                            if (browseMoodId == m.id) {
                                onMoodReshuffle()
                            } else {
                                onMoodChange(m.id)
                            }
                        },
                        label = { Text(m.label) },
                    )
                }
            }
        }
        item {
            Text(
                text = "Genre",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(genreFilters.size, key = { genreFilters[it].second }) { idx ->
                    val (id, label) = genreFilters[idx]
                    FilterChip(
                        selected = browseGenreId == id,
                        onClick = { onGenreChange(id) },
                        label = { Text(label) },
                    )
                }
            }
        }
        if (isSearching) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }
        } else if (results.isEmpty()) {
            item {
                Text(
                    text = "Results appear here after search or browse.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 24.dp),
                )
            }
        } else {
            items(movieRows.size, key = { index -> movieRows[index].firstOrNull()?.id ?: index }) { rowIndex ->
                val row = movieRows[rowIndex]
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    row.forEach { movie ->
                        Box(modifier = Modifier.weight(1f)) {
                            NetflixShelfMovieCard(movie = movie, onClick = { onMovieClick(movie) })
                        }
                    }
                    if (row.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun TvShowsTab(
    apiKey: String,
    onPickShow: (TvShowItem) -> Unit,
    onMissingKey: (String) -> Unit,
) {
    var q by remember { mutableStateOf("") }
    var shows by remember { mutableStateOf<List<TvShowItem>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }
    var err by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "Track TV episodes",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
        )
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)),
        ) {
            Text(
                text = "Search a series, tap a result, then check off episodes season by season. Progress saves to your account.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(12.dp),
            )
        }
        OutlinedTextField(
            value = q,
            onValueChange = { q = it },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = { Text("TV show name") },
            keyboardActions = KeyboardActions(
                onSearch = {
                    scope.launch {
                        if (apiKey.isBlank()) {
                            onMissingKey("TMDB API key missing.")
                            return@launch
                        }
                        loading = true
                        err = null
                        try {
                            shows = TmdbClient.api.searchTv(apiKey = apiKey, query = q.trim()).results
                        } catch (e: Exception) {
                            err = e.message ?: "TV search failed."
                        } finally {
                            loading = false
                        }
                    }
                },
            ),
        )
        Button(
            onClick = {
                scope.launch {
                    if (apiKey.isBlank()) {
                        onMissingKey("TMDB API key missing.")
                        return@launch
                    }
                    loading = true
                    err = null
                    try {
                        shows = TmdbClient.api.searchTv(apiKey = apiKey, query = q.trim()).results
                    } catch (e: Exception) {
                        err = e.message ?: "TV search failed."
                    } finally {
                        loading = false
                    }
                }
            },
            enabled = q.trim().isNotBlank() && !loading,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(if (loading) "Searching..." else "Search shows")
        }
        err?.let { Text(it, color = MaterialTheme.colorScheme.error) }

        if (loading && shows.isEmpty()) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .weight(1f),
            ) {
                items(shows, key = { it.id }) { show ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        tonalElevation = 1.dp,
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .clickable { onPickShow(show) },
                    ) {
                        Row(
                            modifier =
                                Modifier
                                    .padding(12.dp)
                                    .fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            TmdbTvPosterThumb(path = show.posterPath, title = show.name)
                            Column(modifier = Modifier.weight(1f)) {
                                Text(show.name, fontWeight = FontWeight.SemiBold)
                                Text(
                                    show.firstAirDate ?: "—",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TmdbTvPosterThumb(
    path: String?,
    title: String,
) {
    val url = tmdbPosterUrl(path, width = 185)
    Surface(
        modifier = Modifier.size(width = 48.dp, height = 72.dp),
        shape = RoundedCornerShape(10.dp),
        tonalElevation = 1.dp,
    ) {
        if (url == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("-", fontSize = 12.sp)
            }
        } else {
            AsyncImage(
                model = url,
                contentDescription = title,
                modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(10.dp)),
                contentScale = ContentScale.Crop,
            )
        }
    }
}

@Composable
private fun TvShowTrackingScreen(
    show: TvShowItem,
    episodeRepo: EpisodeTrackingRepository,
    apiKey: String,
    onBack: () -> Unit,
) {
    val uid = FirebaseAuth.getInstance().currentUser?.uid
    var seasons by remember { mutableStateOf<List<TvSeasonDetails>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var watchedKeys by remember { mutableStateOf<Set<String>>(emptySet()) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(show.id, uid) {
        loading = true
        error = null
        try {
            val details = TmdbClient.api.getTvDetails(show.id, apiKey)
            val n = (details.numberOfSeasons ?: 0).coerceAtMost(40)
            val loaded = mutableListOf<TvSeasonDetails>()
            for (s in 1..n) {
                try {
                    loaded.add(TmdbClient.api.getTvSeason(show.id, s, apiKey))
                } catch (_: Exception) {
                }
            }
            seasons = loaded
            if (!uid.isNullOrBlank()) {
                watchedKeys = episodeRepo.getWatchedEpisodeKeys(uid, show.id)
            }
        } catch (e: Exception) {
            error = e.message ?: "Could not load show."
        } finally {
            loading = false
        }
    }

    fun refreshWatched() {
        val u = uid ?: return
        scope.launch {
            watchedKeys = episodeRepo.getWatchedEpisodeKeys(u, show.id)
        }
    }

    val totalEp = seasons.sumOf { it.episodes.size }
    val watchedCount =
        seasons.sumOf { season ->
            season.episodes.count { ep ->
                watchedKeys.contains("${season.seasonNumber}_${ep.episodeNumber}")
            }
        }

    val allEpisodes =
        remember(seasons) {
            seasons.flatMap { season ->
                season.episodes.map { ep -> season.seasonNumber to ep.episodeNumber }
            }
        }

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(flixrMainSurfaceGradientBrush()),
    ) {
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp),
        )
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
        FlixrSubScreenTopBar(title = show.name, onBack = onBack)
        if (allEpisodes.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedButton(
                    onClick = {
                        val u = uid ?: return@OutlinedButton
                        scope.launch {
                            try {
                                episodeRepo.setAllEpisodesWatched(u, show.id, allEpisodes, true)
                                refreshWatched()
                                snackbarHostState.showSnackbar("All episodes marked watched")
                            } catch (e: Exception) {
                                error = e.message
                            }
                        }
                    },
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Watch all")
                }
                OutlinedButton(
                    onClick = {
                        val u = uid ?: return@OutlinedButton
                        scope.launch {
                            try {
                                episodeRepo.setAllEpisodesWatched(u, show.id, allEpisodes, false)
                                refreshWatched()
                                snackbarHostState.showSnackbar("All episodes cleared")
                            } catch (e: Exception) {
                                error = e.message
                            }
                        }
                    },
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Clear all")
                }
            }
        }
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)),
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TmdbTvPosterThumb(path = show.posterPath, title = show.name)
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        show.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        if (totalEp > 0) {
                            "$watchedCount / $totalEp episodes checked"
                        } else {
                            "Loading episodes…"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    if (!show.overview.isNullOrBlank()) {
                        Text(
                            text = show.overview!!.take(160) + if (show.overview!!.length > 160) "…" else "",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }
        if (totalEp > 0) {
            LinearProgressIndicator(
                progress = { watchedCount.toFloat() / totalEp.toFloat() },
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .padding(vertical = 10.dp)
                        .clip(RoundedCornerShape(999.dp)),
                trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                color = FlixrAccent,
            )
        }
        if (!error.isNullOrBlank()) {
            Text(error!!, color = MaterialTheme.colorScheme.error)
        }
        if (loading && seasons.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Column
        }
        uid ?: run {
            Text("Sign in to track episodes.", color = MaterialTheme.colorScheme.error)
            return@Column
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize(),
        ) {
            items(seasons, key = { it.seasonNumber }) { season ->
                var expanded by remember(season.seasonNumber) { mutableStateOf(true) }
                val seasonWatched =
                    season.episodes.count { ep ->
                        watchedKeys.contains("${season.seasonNumber}_${ep.episodeNumber}")
                    }
                val seasonTotal = season.episodes.size
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)),
                ) {
                    Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                        Row(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { expanded = !expanded }
                                    .padding(vertical = 8.dp, horizontal = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column {
                                Text(
                                    text = season.name ?: "Season ${season.seasonNumber}",
                                    fontWeight = FontWeight.SemiBold,
                                    style = MaterialTheme.typography.titleSmall,
                                )
                                if (seasonTotal > 0) {
                                    Text(
                                        "$seasonWatched / $seasonTotal episodes",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                            Text(
                                if (expanded) "Hide" else "Show",
                                style = MaterialTheme.typography.labelLarge,
                                color = FlixrAccent,
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            TextButton(
                                onClick = {
                                    val u = uid ?: return@TextButton
                                    scope.launch {
                                        try {
                                            episodeRepo.setSeasonWatched(
                                                u,
                                                show.id,
                                                season.seasonNumber,
                                                season.episodes.map { it.episodeNumber },
                                                true,
                                            )
                                            refreshWatched()
                                        } catch (e: Exception) {
                                            error = e.message
                                        }
                                    }
                                },
                            ) {
                                Text("Mark season watched")
                            }
                            TextButton(
                                onClick = {
                                    val u = uid ?: return@TextButton
                                    scope.launch {
                                        try {
                                            episodeRepo.setSeasonWatched(
                                                u,
                                                show.id,
                                                season.seasonNumber,
                                                season.episodes.map { it.episodeNumber },
                                                false,
                                            )
                                            refreshWatched()
                                        } catch (e: Exception) {
                                            error = e.message
                                        }
                                    }
                                },
                            ) {
                                Text("Clear season")
                            }
                        }
                        if (expanded) {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                for (ep in season.episodes) {
                                    val key = "${season.seasonNumber}_${ep.episodeNumber}"
                                    val checked = watchedKeys.contains(key)
                                    Surface(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp),
                                        color =
                                            if (checked) {
                                                FlixrAccent.copy(alpha = 0.1f)
                                            } else {
                                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                                            },
                                    ) {
                                        Row(
                                            modifier =
                                                Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 6.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        ) {
                                            Checkbox(
                                                checked = checked,
                                                onCheckedChange = { next ->
                                                    val u = uid ?: return@Checkbox
                                                    scope.launch {
                                                        try {
                                                            episodeRepo.setEpisodeWatched(
                                                                u,
                                                                show.id,
                                                                season.seasonNumber,
                                                                ep.episodeNumber,
                                                                next,
                                                            )
                                                            refreshWatched()
                                                            snackbarHostState.showSnackbar(
                                                                if (next) "Episode saved" else "Episode unchecked",
                                                            )
                                                        } catch (e: Exception) {
                                                            error = e.message
                                                        }
                                                    }
                                                },
                                            )
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    ep.name ?: "Episode ${ep.episodeNumber}",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                )
                                                Text(
                                                    "S${season.seasonNumber} E${ep.episodeNumber}",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        }
    }
}

@Composable
fun ReviewListCard(
    review: Review,
    movieTitle: String,
    posterPath: String?,
    onClick: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
        border = BorderStroke(1.dp, FlixrAccent.copy(alpha = 0.18f)),
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top,
        ) {
            val url = tmdbPosterUrl(posterPath, width = 92)
            Surface(
                modifier = Modifier.size(56.dp),
                shape = RoundedCornerShape(10.dp),
                color = FlixrSurfaceBright,
            ) {
                if (url != null) {
                    AsyncImage(
                        model = url,
                        contentDescription = movieTitle,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                    )
                } else {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("—", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(movieTitle, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleSmall)
                TmdbRatingStars(voteAverage = review.rating.takeIf { it > 0 }?.toDouble())
                Text(
                    review.review_text,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    "Tap to open movie →",
                    style = MaterialTheme.typography.labelSmall,
                    color = FlixrAccent,
                )
            }
        }
    }
}

@Composable
private fun AppearanceLightDarkToggle(
    isDark: Boolean,
    onToggle: (Boolean) -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            AppearanceToggleSegment(
                label = "Light",
                icon = Icons.Filled.WbSunny,
                selected = !isDark,
                onClick = { onToggle(false) },
                selectedBrush =
                    Brush.linearGradient(
                        colors =
                            listOf(
                                Color(0xFFFFF8FC),
                                Color(0xFFFFE8F2),
                                Color(0xFFFFD4E8),
                            ),
                    ),
                selectedContentColor = Color(0xFF7A1F4A),
                selectedBorderColor = Color(0xFFFF6BA8).copy(alpha = 0.45f),
                modifier = Modifier.weight(1f),
            )
            AppearanceToggleSegment(
                label = "Dark",
                icon = Icons.Filled.DarkMode,
                selected = isDark,
                onClick = { onToggle(true) },
                selectedBrush =
                    Brush.linearGradient(
                        colors =
                            listOf(
                                Color(0xFF0B1D5C),
                                Color(0xFF1B1624),
                                FlixrGradientBottomLeft,
                            ),
                    ),
                selectedContentColor = Color.White,
                selectedBorderColor = FlixrAccent.copy(alpha = 0.85f),
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun AppearanceToggleSegment(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    selectedBrush: Brush,
    selectedContentColor: Color,
    selectedBorderColor: Color,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(16.dp)
    val contentColor =
        if (selected) {
            selectedContentColor
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f)
        }

    Box(
        modifier =
            modifier
                .height(52.dp)
                .clip(shape)
                .then(
                    if (selected) {
                        Modifier
                            .background(selectedBrush, shape)
                            .border(1.5.dp, selectedBorderColor, shape)
                    } else {
                        Modifier
                    },
                )
                .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(22.dp),
                tint = contentColor,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = contentColor,
            )
        }
    }
}

@Composable
private fun MeQuickLinkTile(
    title: String,
    subtitle: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Surface(
        modifier =
            modifier
                .heightIn(min = 88.dp)
                .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.14f)),
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = FlixrAccent,
                modifier = Modifier.size(28.dp),
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun MeTab(
    authViewModel: AuthViewModel,
    reviewRepo: ReviewRepository,
    themePreferences: ThemePreferences?,
    onWatchlist: () -> Unit,
    onWatchedHistory: () -> Unit,
    onLists: () -> Unit,
    onAnalytics: () -> Unit,
    onOpenReviewMovie: (String) -> Unit,
    onSignOut: () -> Unit,
    isBusy: Boolean,
) {
    val uid = FirebaseAuth.getInstance().currentUser?.uid
    var myReviews by remember { mutableStateOf<List<Review>>(emptyList()) }
    var reviewMovieMeta by remember { mutableStateOf<Map<String, Pair<String, String?>>>(emptyMap()) }
    var profileTab by remember { mutableStateOf(0) }
    val scope = rememberCoroutineScope()
    val apiKey = BuildConfig.TMDB_API_KEY.trim()

    LaunchedEffect(uid) {
        val u = uid
        if (u.isNullOrBlank()) {
            myReviews = emptyList()
            return@LaunchedEffect
        }
        try {
            myReviews = reviewRepo.getReviewsForUser(u)
        } catch (_: Exception) {
            myReviews = emptyList()
        }
    }

    LaunchedEffect(myReviews, apiKey) {
        if (apiKey.isBlank() || myReviews.isEmpty()) {
            reviewMovieMeta = emptyMap()
            return@LaunchedEffect
        }
        val meta = mutableMapOf<String, Pair<String, String?>>()
        for (r in myReviews) {
            val id = r.content_id.toIntOrNull() ?: continue
            if (meta.containsKey(r.content_id)) continue
            runCatching {
                val m = TmdbClient.api.getMovieDetails(id, apiKey)
                meta[r.content_id] = (m.title ?: "Movie $id") to m.posterPath
            }.getOrElse {
                meta[r.content_id] = "Movie $id" to null
            }
        }
        reviewMovieMeta = meta
    }

    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "Your library",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = flixrHeroPrimaryText(),
        )

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                MeQuickLinkTile(
                    title = "Watchlist",
                    subtitle = "Saved to watch later",
                    icon = Icons.Filled.List,
                    modifier = Modifier.weight(1f),
                    onClick = onWatchlist,
                )
                MeQuickLinkTile(
                    title = "Watched",
                    subtitle = "Finished movies",
                    icon = Icons.Filled.History,
                    modifier = Modifier.weight(1f),
                    onClick = onWatchedHistory,
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                MeQuickLinkTile(
                    title = "Stats",
                    subtitle = "Ratings & genres",
                    icon = Icons.Filled.Star,
                    modifier = Modifier.weight(1f),
                    onClick = onAnalytics,
                )
                MeQuickLinkTile(
                    title = "Lists",
                    subtitle = "Custom collections",
                    icon = Icons.Filled.List,
                    modifier = Modifier.weight(1f),
                    onClick = onLists,
                )
            }
        }

        if (themePreferences != null) {
            val mode by themePreferences.themeMode.collectAsStateWithLifecycle(ThemeMode.SYSTEM)
            val isDarkTheme =
                mode == ThemeMode.DARK ||
                    (mode == ThemeMode.SYSTEM && isSystemInDarkTheme())
            Text(
                text = "Appearance",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = flixrHeroPrimaryText(),
            )
            AppearanceLightDarkToggle(
                isDark = isDarkTheme,
                onToggle = { dark ->
                    scope.launch {
                        themePreferences.setThemeMode(
                            if (dark) ThemeMode.DARK else ThemeMode.LIGHT,
                        )
                    }
                },
            )
        }

        TabRow(
            selectedTabIndex = profileTab,
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.55f),
            contentColor = MaterialTheme.colorScheme.primary,
            divider = {},
        ) {
            Tab(selected = profileTab == 0, onClick = { profileTab = 0 }, text = { Text("Profile") })
            Tab(selected = profileTab == 1, onClick = { profileTab = 1 }, text = { Text("My reviews (${myReviews.size})") })
        }

        when (profileTab) {
            0 ->
                ProfileScreen(
                    viewModel = authViewModel,
                    themePreferences = themePreferences,
                    embedded = true,
                    onBack = {},
                    onOpenAnalytics = onAnalytics,
                )

            1 ->
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (myReviews.isEmpty()) {
                        Text(
                            "No reviews yet. Rate titles from Browse or Home.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    } else {
                        for (r in myReviews) {
                            val (title, poster) = reviewMovieMeta[r.content_id] ?: ("Movie ${r.content_id}" to null)
                            ReviewListCard(
                                review = r,
                                movieTitle = title,
                                posterPath = poster,
                                onClick = { onOpenReviewMovie(r.content_id) },
                            )
                        }
                    }
                }
        }

        OutlinedButton(
            onClick = onSignOut,
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

@Composable
private fun ReviewLikeRow(
    review: Review,
    currentUid: String?,
    likeRepo: LikeRepository,
    likedReviewIds: Set<String>,
    onLikedIdsChange: (Set<String>) -> Unit,
    busyReviewId: String?,
    onBusyChange: (String?) -> Unit,
    onError: (String?) -> Unit,
) {
    val scope = rememberCoroutineScope()
    val liked = review.review_id.isNotBlank() && review.review_id in likedReviewIds
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        IconButton(
            onClick = {
                val uid = currentUid ?: run {
                    onError("Sign in to like reviews.")
                    return@IconButton
                }
                val rid = review.review_id
                if (rid.isBlank()) return@IconButton
                scope.launch {
                    onBusyChange(rid)
                    onError(null)
                    try {
                        likeRepo.toggleLike(uid, rid)
                        onLikedIdsChange(
                            if (liked) likedReviewIds - rid else likedReviewIds + rid,
                        )
                    } catch (e: Exception) {
                        onError(e.message ?: "Could not update like.")
                    } finally {
                        onBusyChange(null)
                    }
                }
            },
            enabled =
                (busyReviewId == null || busyReviewId != review.review_id) &&
                    review.review_id.isNotBlank(),
        ) {
            Icon(
                imageVector = Icons.Filled.Favorite,
                contentDescription = if (liked) "Unlike" else "Like",
                tint =
                    if (liked) {
                        Color(0xFFE91E63)
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f)
                    },
            )
        }
        Text(
            text = "${review.likes_count}",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun MovieDetailsScreen(
    movie: Movie,
    reviewRepo: ReviewRepository,
    reviewCommentRepo: ReviewCommentRepository,
    likeRepo: LikeRepository,
    savedRepo: SavedMovieRepository,
    watchHistoryRepo: WatchHistoryRepository,
    userListRepo: UserListRepository,
    discoveryRepo: UserDiscoveryRepository,
    onBack: () -> Unit,
    onOpenProfile: (String) -> Unit,
) {
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val currentUid = FirebaseAuth.getInstance().currentUser?.uid
    var showAddToList by remember { mutableStateOf(false) }

    var ratingText by remember { mutableStateOf("5") }
    var reviewText by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    var reviews by remember { mutableStateOf<List<Review>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var savedMessage by remember { mutableStateOf<String?>(null) }
    var watchHistoryMessage by remember { mutableStateOf<String?>(null) }
    var isWatchBusy by remember { mutableStateOf(false) }

    var likedReviewIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    var likeBusyReviewId by remember { mutableStateOf<String?>(null) }
    var authorLabels by remember { mutableStateOf<Map<String, String>>(emptyMap()) }

    var displayMovie by remember(movie.id) { mutableStateOf(movie) }
    LaunchedEffect(movie.id) {
        val key = BuildConfig.TMDB_API_KEY.trim()
        val needsHydrate = movie.posterPath == null || movie.title.startsWith("TMDB ")
        if (key.isBlank() || !needsHydrate) {
            displayMovie = movie
            return@LaunchedEffect
        }
        try {
            val d = TmdbClient.api.getMovieDetails(movie.id, key)
            displayMovie = movieFromTmdbDetails(d)
        } catch (_: Exception) {
            displayMovie = movie
        }
    }

    val reviewAuthorKey = reviews.map { it.user_id }.distinct().joinToString(",")
    LaunchedEffect(reviewAuthorKey) {
        authorLabels =
            if (reviews.isEmpty()) emptyMap()
            else discoveryRepo.loadUsernameLabels(reviews.map { it.user_id })
    }

    val reviewIdsKey = reviews.joinToString(",") { it.review_id }
    LaunchedEffect(currentUid, reviewIdsKey) {
        val u = currentUid
        if (u.isNullOrBlank()) {
            likedReviewIds = emptySet()
            return@LaunchedEffect
        }
        val ids = reviews.mapNotNull { it.review_id.takeIf { id -> id.isNotBlank() } }
        likedReviewIds = likeRepo.filterLikedReviewIds(u, ids)
    }

    LaunchedEffect(movie.id) {
        ratingText = "5"
        reviewText = ""
        isLoading = true
        error = null
        savedMessage = null
        watchHistoryMessage = null
        try {
            val initial = reviewRepo.getReviewsForContent(movie.id.toString())
            reviews = initial
            val uid = FirebaseAuth.getInstance().currentUser?.uid
            val mine = initial.firstOrNull { it.user_id == uid }
            if (mine != null) {
                ratingText = mine.rating.toString()
                reviewText = mine.review_text
            }
        } catch (e: Exception) {
            error = e.message ?: "Failed to load reviews."
        } finally {
            isLoading = false
        }
        try {
            reviewRepo.listenReviewsForContent(movie.id.toString()).collect { list ->
                reviews = list
            }
        } catch (e: Exception) {
            error = e.message ?: "Review sync failed."
        }
    }

    val scroll = rememberScrollState()
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(flixrMainSurfaceGradientBrush())
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
            MovieDetailHeroPoster(movie = displayMovie)
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
                text = displayMovie.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val date = displayMovie.releaseDate?.takeIf { it.isNotBlank() }
                if (date != null) {
                    Text(
                        date,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                    )
                }
                displayMovie.voteAverage?.let { avg ->
                    Text(
                        text = "TMDB ★ ${String.format("%.1f", avg)}",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }

            if (!displayMovie.overview.isNullOrBlank()) {
                Text(
                    text = displayMovie.overview!!,
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
                            savedRepo.saveMovieForUser(uid, displayMovie)
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

            OutlinedButton(
                onClick = { showAddToList = true },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Add to list")
            }

            OutlinedButton(
                onClick = {
                    val uid = FirebaseAuth.getInstance().currentUser?.uid
                    if (uid.isNullOrBlank()) {
                        error = "Sign in to track watch history."
                        return@OutlinedButton
                    }
                    isWatchBusy = true
                    watchHistoryMessage = null
                    scope.launch {
                        try {
                            watchHistoryRepo.markWatched(uid, movie.id.toString())
                            watchHistoryMessage = "Saved to Watch History."
                        } catch (e: Exception) {
                            error = e.message ?: "Could not update watch history."
                        } finally {
                            isWatchBusy = false
                        }
                    }
                },
                enabled = !isWatchBusy && !isSaving,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(if (isWatchBusy) "Saving..." else "Mark as watched")
            }

            if (!watchHistoryMessage.isNullOrBlank()) {
                Text(watchHistoryMessage!!, color = MaterialTheme.colorScheme.secondary)
            }
            Text(
                text = "Your full watched list is on Profile → Watched.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
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

            val myReview = reviews.firstOrNull { it.user_id == currentUid }

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
                            val mineNow = reviews.firstOrNull { it.user_id == uid }
                            if (mineNow != null && mineNow.review_id.isNotBlank()) {
                                reviewRepo.updateReview(
                                    mineNow.copy(
                                        rating = rating,
                                        review_text = reviewText.trim(),
                                        updated_at = System.currentTimeMillis(),
                                    ),
                                )
                            } else {
                                reviewRepo.addReview(
                                    Review(
                                        user_id = uid,
                                        content_id = movie.id.toString(),
                                        rating = rating,
                                        review_text = reviewText.trim(),
                                    ),
                                )
                            }
                            reviews = reviewRepo.getReviewsForContent(movie.id.toString())
                            val refreshed = reviews.firstOrNull { it.user_id == uid }
                            if (refreshed != null) {
                                ratingText = refreshed.rating.toString()
                                reviewText = refreshed.review_text
                            }
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
                Text(
                    when {
                        isSaving -> "Saving..."
                        myReview != null -> "Update review"
                        else -> "Post review"
                    },
                )
            }
                }
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
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
                            border = BorderStroke(1.dp, FlixrAccent.copy(alpha = 0.15f)),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.Top,
                                ) {
                                    Column(
                                        modifier = Modifier.weight(1f),
                                        verticalArrangement = Arrangement.spacedBy(6.dp),
                                    ) {
                                        if (r.user_id == currentUid) {
                                            Text(
                                                "Your review",
                                                style = MaterialTheme.typography.labelMedium,
                                                color = MaterialTheme.colorScheme.primary,
                                            )
                                        } else if (r.user_id.isNotBlank()) {
                                            Text(
                                                text = usernameLabel(authorLabels, r.user_id),
                                                style = MaterialTheme.typography.labelMedium,
                                                color = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.clickable { onOpenProfile(r.user_id) },
                                            )
                                        }
                                        TmdbRatingStars(voteAverage = r.rating.takeIf { it > 0 }?.toDouble())
                                        Text(r.review_text, style = MaterialTheme.typography.bodyMedium)
                                    }
                                    Column(
                                        horizontalAlignment = Alignment.End,
                                        verticalArrangement = Arrangement.spacedBy(4.dp),
                                    ) {
                                        ReviewLikeRow(
                                            review = r,
                                            currentUid = currentUid,
                                            likeRepo = likeRepo,
                                            likedReviewIds = likedReviewIds,
                                            onLikedIdsChange = { likedReviewIds = it },
                                            busyReviewId = likeBusyReviewId,
                                            onBusyChange = { likeBusyReviewId = it },
                                            onError = { error = it },
                                        )
                                        if (r.user_id == currentUid && r.review_id.isNotBlank()) {
                                            IconButton(
                                                onClick = {
                                                    scope.launch {
                                                        try {
                                                            reviewRepo.deleteReview(r.review_id)
                                                            reviews =
                                                                reviewRepo.getReviewsForContent(movie.id.toString())
                                                            ratingText = "5"
                                                            reviewText = ""
                                                        } catch (e: Exception) {
                                                            error = e.message ?: "Failed to delete review."
                                                        }
                                                    }
                                                },
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Filled.Delete,
                                                    contentDescription = "Delete review",
                                                    tint = MaterialTheme.colorScheme.error,
                                                )
                                            }
                                        }
                                    }
                                }
                                if (r.review_id.isNotBlank()) {
                                    ReviewCommentsSection(
                                        reviewId = r.review_id,
                                        commentRepo = reviewCommentRepo,
                                        currentUid = currentUid,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddToList) {
        AddToListDialog(
            listRepo = userListRepo,
            movieId = movie.id.toString(),
            onDismiss = { showAddToList = false },
        )
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

/** Netflix-style horizontal shelf tile (poster + title + TMDB star row + date). */
@Composable
private fun TmdbRatingStars(voteAverage: Double?) {
    if (voteAverage == null) return
    val v = voteAverage.coerceIn(0.0, 10.0)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(1.dp),
    ) {
        for (i in 1..5) {
            val threshold = i - 0.5
            val filled = (v / 10.0) * 5.0 >= threshold
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint =
                    if (filled) {
                        FlixrGold
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.22f)
                    },
            )
        }
        Text(
            text = String.format("%.1f", v),
            style = MaterialTheme.typography.labelSmall,
            color = FlixrMuted,
            modifier = Modifier.padding(start = 4.dp),
        )
    }
}

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
            color = FlixrSurfaceBright,
        ) {
            val url = tmdbPosterUrl(movie.posterPath, width = 342)
            if (url == null) {
                Box(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .background(
                                Brush.linearGradient(
                                    listOf(
                                        FlixrAccent.copy(alpha = 0.85f),
                                        FlixrAccentDim.copy(alpha = 0.95f),
                                    ),
                                ),
                            ),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        movie.title,
                        textAlign = TextAlign.Center,
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.92f),
                        modifier = Modifier.padding(12.dp),
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
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
        TmdbRatingStars(voteAverage = movie.voteAverage)
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
                    TmdbRatingStars(voteAverage = movie.voteAverage)
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
private fun ActivityHubScreen(
    reviewRepo: ReviewRepository,
    likeRepo: LikeRepository,
    discoveryRepo: UserDiscoveryRepository,
    followRepo: FollowRepository,
    followingIds: List<String>,
    onOpenMovieFromContentId: (String) -> Unit,
    onOpenProfile: (String) -> Unit,
    onMessage: (String) -> Unit,
    onFollowers: () -> Unit,
    onFollowing: () -> Unit,
    onMessages: () -> Unit,
) {
    val myUid = FirebaseAuth.getInstance().currentUser?.uid
    var searchQuery by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }
    var searchLoading by remember { mutableStateOf(false) }
    var followingSet by remember { mutableStateOf<Set<String>>(emptySet()) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(myUid, followingIds) {
        followingSet = followingIds.toSet()
    }

    LaunchedEffect(searchQuery) {
        val q = searchQuery.trim()
        if (q.length < 2) {
            searchResults = emptyList()
            return@LaunchedEffect
        }
        searchLoading = true
        try {
            searchResults = discoveryRepo.searchUsernamesByPrefix(q)
        } catch (_: Exception) {
            searchResults = emptyList()
        } finally {
            searchLoading = false
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = "Activity",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = flixrHeroPrimaryText(),
        )
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = { Text("Search friends by @username") },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            shape = RoundedCornerShape(14.dp),
        )
        if (searchLoading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }
        if (searchResults.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                searchResults.take(6).forEach { (name, uid) ->
                    if (uid == myUid) return@forEach
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(
                                "@$name",
                                modifier = Modifier.clickable { onOpenProfile(uid) },
                                fontWeight = FontWeight.SemiBold,
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                if (!followingSet.contains(uid)) {
                                    TextButton(
                                        onClick = {
                                            val u = myUid ?: return@TextButton
                                            scope.launch {
                                                try {
                                                    followRepo.follow(u, uid)
                                                    followingSet = followingSet + uid
                                                } catch (_: Exception) {
                                                }
                                            }
                                        },
                                    ) {
                                        Text("Follow")
                                    }
                                }
                                TextButton(
                                    onClick = { onMessage(uid) },
                                    enabled = followingSet.contains(uid),
                                ) {
                                    Text("Message")
                                }
                            }
                        }
                    }
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedButton(onClick = onFollowers, modifier = Modifier.weight(1f)) {
                Text("Followers", maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            OutlinedButton(onClick = onFollowing, modifier = Modifier.weight(1f)) {
                Text("Following", maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            OutlinedButton(onClick = onMessages, modifier = Modifier.weight(1f)) {
                Text("Messages", maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
        SocialFeedScreen(
            reviewRepo = reviewRepo,
            likeRepo = likeRepo,
            discoveryRepo = discoveryRepo,
            followingIds = followingIds,
            embedded = true,
            showHeader = false,
            onOpenMovieFromContentId = onOpenMovieFromContentId,
            onOpenProfile = onOpenProfile,
            onMessage = onMessage,
        )
    }
}

@Composable
private fun SocialFeedScreen(
    reviewRepo: ReviewRepository,
    likeRepo: LikeRepository,
    discoveryRepo: UserDiscoveryRepository,
    followingIds: List<String>,
    embedded: Boolean = false,
    showHeader: Boolean = true,
    onOpenMovieFromContentId: (String) -> Unit,
    onOpenProfile: (String) -> Unit,
    onMessage: ((String) -> Unit)? = null,
) {
    val uid = FirebaseAuth.getInstance().currentUser?.uid
    var activityLabels by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var activityReviews by remember { mutableStateOf<List<Review>>(emptyList()) }
    var feedError by remember { mutableStateOf<String?>(null) }
    var likedReviewIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    var likeBusyReviewId by remember { mutableStateOf<String?>(null) }

    val activityIdsKey = activityReviews.joinToString(",") { it.review_id }
    LaunchedEffect(uid, activityIdsKey) {
        val u = uid
        if (u.isNullOrBlank()) {
            likedReviewIds = emptySet()
            return@LaunchedEffect
        }
        likedReviewIds =
            likeRepo.filterLikedReviewIds(u, activityReviews.mapNotNull { it.review_id.takeIf { id -> id.isNotBlank() } })
    }

    LaunchedEffect(followingIds) {
        if (followingIds.isEmpty()) {
            activityReviews = emptyList()
            return@LaunchedEffect
        }
        try {
            reviewRepo.listenReviewsFromUsers(followingIds).collect { activityReviews = it }
        } catch (e: Exception) {
            feedError = e.message
        }
    }

    val activityAuthorKey = activityReviews.map { it.user_id }.distinct().joinToString(",")
    LaunchedEffect(activityAuthorKey) {
        activityLabels =
            if (activityReviews.isEmpty()) emptyMap()
            else discoveryRepo.loadUsernameLabels(activityReviews.map { it.user_id })
    }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .then(
                    if (!embedded) {
                        Modifier.statusBarsPadding().navigationBarsPadding().padding(horizontal = 20.dp, vertical = 16.dp)
                    } else {
                        Modifier
                    },
                ),
    ) {
        if (showHeader) {
            Text(
                text = "Activity",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text =
                    if (followingIds.isEmpty()) {
                        "Search for friends above to follow them and see their reviews."
                    } else {
                        "Reviews from people you follow."
                    },
                style = MaterialTheme.typography.bodySmall,
                color = if (embedded) FlixrMuted else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        if (!feedError.isNullOrBlank()) {
            Text(feedError!!, color = MaterialTheme.colorScheme.error)
        }

        if (uid.isNullOrBlank()) {
            Text(
                "Sign in to see the activity feed.",
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
            )
            return@Column
        }

        if (showHeader && followingIds.isEmpty()) {
            Text(
                "Your feed is empty. Search for friends above to follow classmates.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(activityReviews, key = { it.review_id }) { r ->
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    tonalElevation = 1.dp,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top,
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                Text(
                                    text = usernameLabel(activityLabels, r.user_id),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.clickable { onOpenProfile(r.user_id) },
                                )
                                Text(
                                    text = r.review_text,
                                    style = MaterialTheme.typography.bodyMedium,
                                    maxLines = 6,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.clickable { onOpenMovieFromContentId(r.content_id) },
                                )
                                Text(
                                    text = "Movie ${r.content_id} · comments on title page →",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.clickable { onOpenMovieFromContentId(r.content_id) },
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                if (onMessage != null) {
                                    TextButton(
                                        onClick = { onMessage.invoke(r.user_id) },
                                        enabled = followingIds.contains(r.user_id),
                                    ) {
                                        Icon(Icons.Filled.Chat, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.size(4.dp))
                                        Text("Message")
                                    }
                                }
                                ReviewLikeRow(
                                    review = r,
                                    currentUid = uid,
                                    likeRepo = likeRepo,
                                    likedReviewIds = likedReviewIds,
                                    onLikedIdsChange = { likedReviewIds = it },
                                    busyReviewId = likeBusyReviewId,
                                    onBusyChange = { likeBusyReviewId = it },
                                    onError = { msg -> feedError = msg },
                                )
                            }
                        }
                    }
                }
            }

            if (followingIds.isNotEmpty() && activityReviews.isEmpty()) {
                item {
                    Text(
                        "No reviews from people you follow yet.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun AnalyticsScreen(
    analyticsRepo: AnalyticsRepository,
    tmdbApiKey: String,
    onBack: () -> Unit,
    onOpenWatched: () -> Unit = {},
) {
    val uid = FirebaseAuth.getInstance().currentUser?.uid
    var stats by remember { mutableStateOf<UserAnalytics?>(null) }
    var loading by remember { mutableStateOf(true) }
    var loadError by remember { mutableStateOf<String?>(null) }
    var refreshKey by remember { mutableStateOf(0) }

    LaunchedEffect(uid, tmdbApiKey, refreshKey) {
        if (uid.isNullOrBlank()) {
            loading = false
            stats = null
            loadError = "Sign in to see your analytics."
            return@LaunchedEffect
        }
        loading = true
        loadError = null
        try {
            stats = analyticsRepo.loadUserAnalytics(uid, tmdbApiKey)
        } catch (e: Exception) {
            loadError = e.message ?: "Could not load analytics."
            stats = null
        } finally {
            loading = false
        }
    }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(flixrMainSurfaceGradientBrush())
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            OutlinedButton(onClick = onBack, modifier = Modifier.weight(1f)) {
                Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = null)
                Spacer(Modifier.size(8.dp))
                Text("Back")
            }
            OutlinedButton(
                onClick = { refreshKey++ },
                enabled = !uid.isNullOrBlank() && !loading,
                modifier = Modifier.weight(1f),
            ) {
                Text("Refresh")
            }
        }
        Text(
            text = "Your stats",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text =
                "Counts and averages are computed from Firestore when you open this screen " +
                    "(derived data — not stored separately). Genre and watch time use TMDB for movies you marked watched.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        if (tmdbApiKey.isBlank()) {
            Text(
                "Add TMDB_API_KEY in local.properties to enable genre and estimated watch time.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.tertiary,
            )
        }

        if (!loadError.isNullOrBlank()) {
            Text(loadError!!, color = MaterialTheme.colorScheme.error)
        }

        when {
            loading -> {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }
            stats != null -> {
                val s = stats!!
                AnalyticsStatCard(
                    label = "Titles on watchlist",
                    value = "${s.watchlistCount}",
                )
                AnalyticsStatCard(
                    label = "Titles marked watched",
                    value = "${s.watchedTitlesCount}",
                )
                if (s.watchedTitlesCount > 0) {
                    TextButton(onClick = onOpenWatched) {
                        Icon(Icons.Filled.History, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.size(8.dp))
                        Text("Open watched list")
                    }
                }
                AnalyticsStatCard(
                    label = "Reviews written",
                    value = "${s.reviewsWritten}",
                )
                AnalyticsStatCard(
                    label = "Average rating given",
                    value =
                        if (s.averageRatingGiven != null) {
                            String.format("%.1f / 10", s.averageRatingGiven)
                        } else {
                            "—"
                        },
                )

                AnalyticsStatCard(
                    label = "Top genre (from watched titles)",
                    value =
                        if (!s.topGenreName.isNullOrBlank()) {
                            "${s.topGenreName} (${s.topGenreScore})"
                        } else {
                            "—"
                        },
                )

                AnalyticsStatCard(
                    label = "Estimated watch time (TMDB runtimes)",
                    value = formatWatchMinutes(s.estimatedWatchMinutes),
                )

                if (s.genreBreakdown.isNotEmpty()) {
                    Text(
                        text = "Genre mix (watched)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        val topGenres = s.genreBreakdown.take(6)
                        val maxG = topGenres.maxOfOrNull { it.second }?.coerceAtLeast(1) ?: 1
                        for ((name, count) in topGenres) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Text(
                                    text = name,
                                    style = MaterialTheme.typography.labelLarge,
                                    modifier = Modifier.width(120.dp),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                LinearProgressIndicator(
                                    progress = { count.toFloat() / maxG },
                                    modifier =
                                        Modifier
                                            .weight(1f)
                                            .height(8.dp),
                                    trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    color = MaterialTheme.colorScheme.secondary,
                                )
                                Text(
                                    text = "$count",
                                    style = MaterialTheme.typography.labelMedium,
                                    modifier = Modifier.width(28.dp),
                                    textAlign = TextAlign.End,
                                )
                            }
                        }
                    }
                }

                if (s.reviewsWritten > 0 && s.ratingsHistogram.isNotEmpty()) {
                    Text(
                        text = "Rating spread (your reviews)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                    val maxCount = s.ratingsHistogram.values.maxOrNull()?.coerceAtLeast(1) ?: 1
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        for (rating in 0..10) {
                            val count = s.ratingsHistogram[rating] ?: 0
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Text(
                                    text = "$rating",
                                    style = MaterialTheme.typography.labelLarge,
                                    modifier = Modifier.width(22.dp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                LinearProgressIndicator(
                                    progress = { count.toFloat() / maxCount },
                                    modifier =
                                        Modifier
                                            .weight(1f)
                                            .height(10.dp),
                                    trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    color = MaterialTheme.colorScheme.primary,
                                )
                                Text(
                                    text = "$count",
                                    style = MaterialTheme.typography.labelMedium,
                                    modifier = Modifier.width(28.dp),
                                    textAlign = TextAlign.End,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AnalyticsStatCard(
    label: String,
    value: String,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 1.dp,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

private fun formatWatchMinutes(minutes: Int): String {
    if (minutes <= 0) return "—"
    val h = minutes / 60
    val m = minutes % 60
    return when {
        h > 0 && m > 0 -> "${h}h ${m}m (~$minutes min)"
        h > 0 -> "${h}h (~$minutes min)"
        else -> "${m}m"
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
                .background(flixrMainSurfaceGradientBrush())
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
private fun WatchedHistoryScreen(
    watchHistoryRepo: WatchHistoryRepository,
    onBack: () -> Unit,
    onOpenMovie: (Movie) -> Unit,
) {
    val uid = FirebaseAuth.getInstance().currentUser?.uid
    var movies by remember { mutableStateOf<List<Movie>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var loadError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(uid) {
        val u = uid
        if (u.isNullOrBlank()) {
            loading = false
            movies = emptyList()
            loadError = "Sign in to view watched titles."
            return@LaunchedEffect
        }
        loading = true
        loadError = null
        try {
            val ids = watchHistoryRepo.listWatchedMovieIds(u)
            val key = BuildConfig.TMDB_API_KEY.trim()
            movies =
                if (key.isBlank()) {
                    ids.map { id ->
                        Movie(
                            id = id,
                            title = "Movie #$id",
                            posterPath = null,
                            releaseDate = null,
                            overview = null,
                            voteAverage = null,
                        )
                    }
                } else {
                    ids.take(120).map { id ->
                        try {
                            movieFromTmdbDetails(TmdbClient.api.getMovieDetails(id, key))
                        } catch (_: Exception) {
                            Movie(
                                id = id,
                                title = "Movie #$id",
                                posterPath = null,
                                releaseDate = null,
                                overview = null,
                                voteAverage = null,
                            )
                        }
                    }
                }
        } catch (e: Exception) {
            loadError = e.message ?: "Could not load watch history."
        } finally {
            loading = false
        }
    }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(flixrMainSurfaceGradientBrush())
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 16.dp),
    ) {
        OutlinedButton(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
        ) {
            Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = null)
            Spacer(Modifier.size(8.dp))
            Text("Back")
        }
        Text(
            text = "Watched",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = "Movies you marked as watched on each movie page (newest first).",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

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
                    text =
                        "Nothing here yet. Open a movie, scroll to actions, and tap Mark as watched.",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
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
                            onRemove = null,
                            removeInProgress = false,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileScreen(
    viewModel: AuthViewModel,
    themePreferences: ThemePreferences? = null,
    embedded: Boolean = false,
    onBack: () -> Unit,
    onOpenAnalytics: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val profile = state.profile
    val scope = rememberCoroutineScope()

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

    val outerScroll = rememberScrollState()
    Column(
        modifier =
            Modifier
                .padding(if (embedded) 0.dp else 20.dp)
                .then(if (!embedded) Modifier.verticalScroll(outerScroll) else Modifier),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (!embedded) {
            OutlinedButton(onClick = onBack, enabled = !state.isBusy, modifier = Modifier.fillMaxWidth()) {
                Text("Back")
            }
        }

        if (!embedded) {
            Text("Profile", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(
                text = "Your saved movies appear on the Watchlist screen (Me tab → Watchlist).",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
            )

            OutlinedButton(
                onClick = onOpenAnalytics,
                enabled = !state.isBusy,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors =
                    ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary,
                    ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)),
            ) {
                Icon(imageVector = Icons.Filled.Star, contentDescription = null)
                Spacer(Modifier.size(8.dp))
                Text("Your stats & analytics")
            }
        }

        val photoModel = pickedImage ?: profile?.profilePictureUrl
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
            tonalElevation = 1.dp,
            shadowElevation = 0.dp,
            modifier = Modifier.fillMaxWidth(),
        ) {
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
