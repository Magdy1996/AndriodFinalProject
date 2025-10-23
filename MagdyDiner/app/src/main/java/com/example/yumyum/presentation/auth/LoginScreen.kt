@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.yumyum.presentation.auth

import android.util.Log
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.yumyum.presentation.navigation.Screen
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale

// Top-level enum for the three auth screens (moved out of the composable)
enum class AuthScreenState { Welcome, Login, Register }

/**
 * Welcome / Login / Register screen that matches the provided visual mockups.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    navController: NavController,
    viewModel: LoginViewModel = hiltViewModel()
) {
    // UI state: which sub-screen to show; use rememberSaveable so state survives recomposition/config changes
    // Show the Welcome sub-screen first so users see the choice to Login or Register after the splash
    // Use remember instead of rememberSaveable for the enum-backed UI state to avoid saved-state (Bundle) serialization issues
    // This keeps state across recompositions but not process death — it's the safer default and eliminates enum-saver issues.
    val screenState = remember { mutableStateOf(AuthScreenState.Welcome) }

    // ViewModel state
    val currentUserState = viewModel.currentUserId.collectAsState(initial = 0L)
    val currentUser = currentUserState.value

    // Debug: log the initial screen state and current user id to help diagnose runtime navigation
    Log.d("LoginScreen", "Initial auth screenState=${screenState.value} currentUser=$currentUser")

    // ViewModel state
    val status by viewModel.statusMessage.collectAsState(initial = null)

    // Shared form fields
    val username = remember { mutableStateOf("") }
    val password = remember { mutableStateOf("") }
    val passwordConfirm = remember { mutableStateOf("") }
    val displayName = remember { mutableStateOf("") }

    // Register-only fields
    val gmail = remember { mutableStateOf("") }
    val phone = remember { mutableStateOf("") }
    val address = remember { mutableStateOf("") }

    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    // Use a coroutine scope to show snackbars without blocking navigation logic
    val coroutineScope = rememberCoroutineScope()
    // Track whether a register request is in flight so UI gives immediate feedback
    val isRegistering = remember { mutableStateOf(false) }

    // Track whether a login request is in flight so we can disable the Login button and avoid double-taps
    val isLoggingIn = remember { mutableStateOf(false) }

    // Simple validations
    val minPasswordLength = 3
    val passwordsMatch = password.value == passwordConfirm.value
    val passwordLongEnough = password.value.length >= minPasswordLength
    // UI-level enable: require username, password length, matching passwords, and a non-empty email
    // This keeps the Register button visible but not clickable until the user fills required fields.
    val canOpenRegister = username.value.isNotBlank() && passwordLongEnough && passwordsMatch && gmail.value.trim().isNotEmpty()

    // On success navigate into app (close login) or switch to login after sign up
    LaunchedEffect(status) {
        status?.let { s ->
            // Debug: log status updates so we can verify sign-in/sign-up messages at runtime
            Log.d("LoginScreen", "Auth status update: $s")
            // Reset registering flag when we get a status update
            isRegistering.value = false
            // Reset logging-in flag when we get a status update (either success or failure)
            isLoggingIn.value = false
            // Show a toast for status so the user sees feedback immediately
            Toast.makeText(context, s, Toast.LENGTH_SHORT).show()
            // Also show a snackbar to make the status unmissable without blocking navigation
            coroutineScope.launch {
                snackbarHostState.showSnackbar(s)
            }
            when {
                s.startsWith("Signed in") -> {
                    // Always navigate to Categories (menu) after successful login so the user sees the meals
                    navController.navigate(Screen.CategoriesScreen.route) {
                        popUpTo(Screen.LoginScreen.route) { inclusive = true }
                    }
                }
                s.startsWith("Signed up") || s.startsWith("Signed up successfully") -> {
                    // After successful sign up, show the Login sub-screen so the user can login
                    // (do not auto-enter the app)
                    screenState.value = AuthScreenState.Login
                }
            }
        }
    }

    // Outer background: dark sides like screenshot
    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF2E2E2E))) {
        Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) {
            // Centered card that looks like the purple panel in your screenshot
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .wrapContentSize(Alignment.Center)
            ) {
                // Compute a responsive max height for the card (85% of available vertical space)
                val cardMaxHeight = maxHeight * 0.85f

                // Card container
                val cardGradient = Brush.verticalGradient(
                    colors = listOf(Color(0xFF7B4CE6), Color(0xFF31186A))
                )

                Box(
                    modifier = Modifier
                        .widthIn(max = 360.dp)
                        // Use a responsive max height derived from BoxWithConstraints so the inner verticalScroll gets
                        // a finite constraint regardless of screen size or parent layout.
                        .heightIn(max = cardMaxHeight)
                        .wrapContentSize(Alignment.Center)
                        .clip(RoundedCornerShape(12.dp))
                        .background(cardGradient)
                        // standard padding — image will live inside the card and push content naturally
                        .padding(horizontal = 20.dp, vertical = 28.dp)
                ) {
                    Spacer(modifier = Modifier.height(10.dp))

                    // use a LazyColumn bounded by available card height
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = cardMaxHeight - 40.dp),
                        contentPadding = PaddingValues(vertical = 6.dp),
                        verticalArrangement = Arrangement.Top
                    ) {
                        item {
                            when (screenState.value) {
                                AuthScreenState.Register -> {
                                    // Use the shared RegisterFormView for a consistent layout and behavior
                                    RegisterFormView(
                                         username = username.value,
                                         onUsernameChange = { username.value = it },
                                         gmail = gmail.value,
                                         onGmailChange = { gmail.value = it },
                                         password = password.value,
                                         onPasswordChange = { password.value = it },
                                         passwordConfirm = passwordConfirm.value,
                                         onPasswordConfirmChange = { passwordConfirm.value = it },
                                         displayName = displayName.value,
                                         onDisplayNameChange = { displayName.value = it },
                                         phone = phone.value,
                                         onPhoneChange = { phone.value = it },
                                         address = address.value,
                                         onAddressChange = { address.value = it },
                                         onBack = {
                                             Log.d("LoginScreen", "Register view back clicked - switching to Login")
                                             screenState.value = AuthScreenState.Login
                                         },
                                         onRegister = {
                                            // Basic client-side validation and robust error handling to avoid crashes
                                            if (!canOpenRegister) {
                                                coroutineScope.launch { snackbarHostState.showSnackbar("Please fill required fields and ensure passwords match") }
                                                return@RegisterFormView
                                            }
                                            val emailTrim = gmail.value.trim()
                                            if (emailTrim.isEmpty()) {
                                                coroutineScope.launch { snackbarHostState.showSnackbar("Email is required") }
                                                return@RegisterFormView
                                            }
                                            val emailRegex = "[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-z]{2,}".toRegex()
                                            if (!emailRegex.matches(emailTrim)) {
                                                coroutineScope.launch { snackbarHostState.showSnackbar("Please enter a valid email address") }
                                                return@RegisterFormView
                                            }

                                            if (isRegistering.value) return@RegisterFormView
                                            // Set registering flag and call ViewModel inside our UI coroutine scope so we can catch immediate errors
                                            isRegistering.value = true
                                            coroutineScope.launch {
                                                try {
                                                    // Defensive logging for troubleshooting
                                                    Log.d("LoginScreen", "onRegister: calling viewModel.signUp username=${username.value.trim()} email=$emailTrim")
                                                    viewModel.signUp(
                                                        username = username.value.trim(),
                                                        password = password.value,
                                                        displayName = displayName.value.trim().ifEmpty { null },
                                                        email = emailTrim,
                                                        phoneNumber = phone.value.trim().ifEmpty { null },
                                                        address = address.value.trim().ifEmpty { null }
                                                    )
                                                } catch (e: Exception) {
                                                    Log.e("LoginScreen", "onRegister exception", e)
                                                    isRegistering.value = false
                                                    snackbarHostState.showSnackbar("Registration failed: ${e.message ?: "unknown error"}")
                                                }
                                            }
                                            // Note: ViewModel will emit status messages; we rely on that to clear isRegistering when status updates arrive
                                         },
                                        status = status,
                                        canRegister = canOpenRegister,
                                        isRegistering = isRegistering.value
                                    )
                                }

                                AuthScreenState.Login -> {
                                    // Show the existing Login form composable
                                    LoginFormView(
                                         username = username.value,
                                         onUsernameChange = { username.value = it },
                                         password = password.value,
                                         onPasswordChange = { password.value = it },
                                         onBack = {
                                             Log.d("LoginScreen", "Login form back clicked - switching to Register")
                                             Toast.makeText(context, "Back to register", Toast.LENGTH_SHORT).show()
                                             screenState.value = AuthScreenState.Register
                                         },
                                         onLogin = {
                                            if (username.value.trim().isNotBlank() && password.value.isNotBlank() && !isLoggingIn.value) {
                                                Log.d("LoginScreen", "Login button clicked - calling signIn")
                                                // Disable further taps immediately
                                                isLoggingIn.value = true
                                                viewModel.signIn(username.value.trim(), password.value)
                                            } else {
                                                Log.w("LoginScreen", "Login validation failed: username blank=${username.value.trim().isBlank()}, password blank=${password.value.isBlank()}, isLoggingIn=${isLoggingIn.value}")
                                            }
                                         },
                                        status = status,
                                        isLoggingIn = isLoggingIn.value
                                    )
                                }

                                AuthScreenState.Welcome -> {
                                    WelcomeView(
                                        onLoginClick = { screenState.value = AuthScreenState.Login },
                                        onSignUpClick = { screenState.value = AuthScreenState.Register },
                                        onContinueGuest = {
                                            navController.navigate(Screen.CategoriesScreen.route) {
                                                popUpTo(Screen.LoginScreen.route) { inclusive = true }
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Observe currentUser changes and navigate to Categories after a successful login attempt
    LaunchedEffect(viewModel.currentUserId.collectAsState(initial = 0L).value) {
        // no-op here; viewModel.signIn handles navigation via status messages
    }

    // Password update dialog (kept for future use; shows when viewModel triggers it)
    // Note: viewModel.passwordUpdateRequest is observed above; dialog behavior preserved if needed later
}

@Composable
private fun WelcomeView(onLoginClick: () -> Unit, onSignUpClick: () -> Unit, onContinueGuest: () -> Unit) {
    // simple entrance state for staggered animations
    val visible = remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        // small delay to let the screen settle before animating in
        kotlinx.coroutines.delay(80)
        visible.value = true
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(min = 320.dp, max = 420.dp)
            .padding(vertical = 18.dp, horizontal = 12.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // logo area animates in first
        AnimatedVisibility(
            visible = visible.value,
            enter = fadeIn(animationSpec = tween(durationMillis = 420, delayMillis = 0)) + scaleIn(initialScale = 0.94f, animationSpec = tween(durationMillis = 420))
        ) {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
                // Decorative soft circles behind the logo for depth
                Box(
                    modifier = Modifier
                        .size(220.dp)
                        .offset(x = (-40).dp, y = 6.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF7B4CE6).copy(alpha = 0.12f))
                ) {}
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .offset(x = 48.dp, y = 28.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF4B25D6).copy(alpha = 0.10f))
                ) {}

                // Load the asset named welcome.png which the app will look up in src/main/assets
                AsyncImage(
                    model = "file:///android_asset/welcome_meal.png",
                    contentDescription = "Welcome image",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(width = 110.dp, height = 150.dp)
                        .offset(y = 12.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // staggered buttons: Login first, then Sign Up and Continue as Guest
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            PrettyPrimaryButton(
                text = "Login",
                onClick = onLoginClick,
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = onSignUpClick,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White.copy(alpha = 0.1f),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f))
            ) {
                Text("Sign up", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }

            TextButton(
                onClick = onContinueGuest,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Continue as Guest", color = Color.White.copy(alpha = 0.8f), fontSize = 16.sp)
            }
        }
    }
}

// Helper composables: buttons and compact Login/Register forms
@Composable
private fun PrettyPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = 48.dp, // reduced from 52.dp to make buttons slightly slimmer
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(height).shadow(8.dp, RoundedCornerShape(28.dp)),
        shape = RoundedCornerShape(28.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1EA0FF), contentColor = Color.White),
        enabled = enabled
    ) {
        Text(text, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun LoginFormView(
    username: String,
    onUsernameChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    onBack: () -> Unit,
    onLogin: () -> Unit,
    status: String?,
    isLoggingIn: Boolean = false
) {
    // make the form a bit narrower so boxes look smaller overall
    Column(modifier = Modifier.fillMaxWidth().widthIn(max = 280.dp).padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Welcome", color = Color.White, fontSize = 48.sp, fontWeight = FontWeight.Bold) // slightly larger
        Spacer(modifier = Modifier.height(6.dp))
        Text("Login to your account", color = Color.White.copy(alpha = 0.95f), fontSize = 18.sp) // slightly larger
        Spacer(modifier = Modifier.height(20.dp))

        Text("username", color = Color.White.copy(alpha = 0.45f), fontSize = 16.sp, modifier = Modifier.fillMaxWidth()) // larger label
        VisibleTextField(
            value = username,
            onValueChange = onUsernameChange,
            modifier = Modifier.fillMaxWidth().height(40.dp).shadow(6.dp, RoundedCornerShape(12.dp)), // slightly smaller height
        )
        // DEBUG: echo the raw value so we can confirm what's actually stored in the field
        Text(username, color = Color.White.copy(alpha = 0.9f), fontSize = 12.sp, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(14.dp))

        Text("Password", color = Color.White.copy(alpha = 0.45f), fontSize = 16.sp, modifier = Modifier.fillMaxWidth())
        VisiblePasswordField(
            value = password,
            onValueChange = onPasswordChange,
            modifier = Modifier.fillMaxWidth().height(40.dp).shadow(6.dp, RoundedCornerShape(12.dp))
        )

        Spacer(modifier = Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            TextButton(onClick = { /* remember me */ }) { Text("Remember me", color = Color.White.copy(alpha = 0.9f), fontSize = 14.sp) }
            TextButton(onClick = { /* forgot */ }) { Text("Forget Password?", color = Color.White.copy(alpha = 0.9f), fontSize = 14.sp) }
        }

        Spacer(modifier = Modifier.height(18.dp))
        PrettyPrimaryButton(text = if (isLoggingIn) "Logging in..." else "Login", onClick = onLogin, modifier = Modifier.fillMaxWidth(), enabled = !isLoggingIn)

        Spacer(modifier = Modifier.height(12.dp))
        TextButton(onClick = onBack) { Text("Don't have an account? Sign up", color = Color.White.copy(alpha = 0.9f), fontSize = 14.sp) }

        status?.let { Spacer(modifier = Modifier.height(12.dp)); Text(it, color = MaterialTheme.colorScheme.error) }

        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
private fun RegisterFormView(
    username: String,
    onUsernameChange: (String) -> Unit,
    gmail: String,
    onGmailChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    passwordConfirm: String,
    onPasswordConfirmChange: (String) -> Unit,
    displayName: String,
    onDisplayNameChange: (String) -> Unit,
    phone: String,
    onPhoneChange: (String) -> Unit,
    address: String,
    onAddressChange: (String) -> Unit,
    onBack: () -> Unit,
    onRegister: () -> Unit,
    status: String?,
    canRegister: Boolean,
    isRegistering: Boolean
) {
    // Removed inner verticalScroll to avoid nesting a scrollable inside another scrollable
    Column(modifier = Modifier.fillMaxWidth().widthIn(max = 280.dp).padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Welcome", color = Color.White, fontSize = 48.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(6.dp))
        Text("Sign up", color = Color.White.copy(alpha = 0.95f), fontSize = 18.sp)

        Spacer(modifier = Modifier.height(20.dp))
        Text("username", color = Color.White.copy(alpha = 0.45f), fontSize = 16.sp, modifier = Modifier.fillMaxWidth())
        VisibleTextField(
            value = username,
            onValueChange = onUsernameChange,
            modifier = Modifier.fillMaxWidth().height(40.dp).shadow(6.dp, RoundedCornerShape(12.dp)),
        )
        // DEBUG: echo the raw value so we can confirm what's actually stored in the field
        Text(username, color = Color.White.copy(alpha = 0.9f), fontSize = 12.sp, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(14.dp))

        Text("Email", color = Color.White.copy(alpha = 0.45f), fontSize = 16.sp, modifier = Modifier.fillMaxWidth())
        VisibleTextField(
            value = gmail,
            onValueChange = onGmailChange,
            modifier = Modifier.fillMaxWidth().height(40.dp).shadow(6.dp, RoundedCornerShape(12.dp)),
        )
        Text(gmail, color = Color.White.copy(alpha = 0.9f), fontSize = 12.sp, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(14.dp))

        Text("Password", color = Color.White.copy(alpha = 0.45f), fontSize = 16.sp, modifier = Modifier.fillMaxWidth())
        VisiblePasswordField(
            value = password,
            onValueChange = onPasswordChange,
            modifier = Modifier.fillMaxWidth().height(40.dp).shadow(6.dp, RoundedCornerShape(12.dp))
        )
        Spacer(modifier = Modifier.height(14.dp))

        Text("Confirm Password", color = Color.White.copy(alpha = 0.45f), fontSize = 16.sp, modifier = Modifier.fillMaxWidth())
        VisiblePasswordField(
            value = passwordConfirm,
            onValueChange = onPasswordConfirmChange,
            modifier = Modifier.fillMaxWidth().height(40.dp).shadow(6.dp, RoundedCornerShape(12.dp))
        )

        Spacer(modifier = Modifier.height(14.dp))
        // Optional additional fields shown on Register screen
        Text("Display name (optional)", color = Color.White.copy(alpha = 0.45f), fontSize = 16.sp, modifier = Modifier.fillMaxWidth())
        VisibleTextField(
            value = displayName,
            onValueChange = onDisplayNameChange,
            modifier = Modifier.fillMaxWidth().height(40.dp).shadow(6.dp, RoundedCornerShape(12.dp)),
        )
        Text(displayName, color = Color.White.copy(alpha = 0.9f), fontSize = 12.sp, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(14.dp))

        Text("Phone (optional)", color = Color.White.copy(alpha = 0.45f), fontSize = 16.sp, modifier = Modifier.fillMaxWidth())
        VisibleTextField(
            value = phone,
            onValueChange = onPhoneChange,
            modifier = Modifier.fillMaxWidth().height(40.dp).shadow(6.dp, RoundedCornerShape(12.dp)),
        )
        Text(phone, color = Color.White.copy(alpha = 0.9f), fontSize = 12.sp, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(14.dp))

        Text("Address (optional)", color = Color.White.copy(alpha = 0.45f), fontSize = 16.sp, modifier = Modifier.fillMaxWidth())
        VisibleTextField(
            value = address,
            onValueChange = onAddressChange,
            modifier = Modifier.fillMaxWidth().height(40.dp).shadow(6.dp, RoundedCornerShape(12.dp)),
        )
        Text(address, color = Color.White.copy(alpha = 0.9f), fontSize = 12.sp, modifier = Modifier.fillMaxWidth())

        Spacer(modifier = Modifier.height(14.dp))
        // Always show the Register button; keep the label readable when disabled and switch to green when enabled
        val registerEnabled = canRegister && !isRegistering
        Button(
            onClick = onRegister,
            modifier = Modifier.fillMaxWidth().height(48.dp).shadow(8.dp, RoundedCornerShape(12.dp)),
            shape = RoundedCornerShape(12.dp),
            enabled = registerEnabled,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (registerEnabled) Color(0xFF2ECA6A) else Color(0xFF3A3A3A), // green when enabled, dark gray when disabled
                contentColor = Color.White,
                disabledContainerColor = Color(0xFF3A3A3A),
                disabledContentColor = Color.White.copy(alpha = 0.95f)
            )
        ) {
            Text(if (isRegistering) "Registering..." else "Register", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        }

        Spacer(modifier = Modifier.height(12.dp))
        TextButton(onClick = onBack) { Text("Already have an Account? Login", color = Color.White.copy(alpha = 0.9f), fontSize = 14.sp) }

        status?.let { Spacer(modifier = Modifier.height(12.dp)); Text(it, color = MaterialTheme.colorScheme.error) }

        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
private fun VisibleTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    // A minimal visible text field built on BasicTextField so characters are never masked.
    Box(
        modifier = modifier
            .background(Color.White, RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        if (value.isEmpty()) {
            Text("", color = Color(0xFF8A8A8A))
        }
        SelectionContainer {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                textStyle = TextStyle(color = Color(0xFF111111), fontWeight = FontWeight.SemiBold, fontSize = 16.sp),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun VisiblePasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // Styled like VisibleTextField but masks input using PasswordVisualTransformation
    Box(
        modifier = modifier
            .background(Color.White, RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        SelectionContainer {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                textStyle = TextStyle(color = Color(0xFF111111), fontWeight = FontWeight.SemiBold, fontSize = 16.sp),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LoginScreenPreview() {
    // Use a dark background so preview matches the login screen context
    Box(modifier = Modifier.background(Color(0xFF2E2E2E)).fillMaxWidth()) {
        WelcomeView(onLoginClick = {}, onSignUpClick = {}, onContinueGuest = {})
    }
}
