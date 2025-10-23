@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.yumyum.presentation.auth

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.asImageBitmap
import com.example.yumyum.presentation.navigation.Screen
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch

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
    val screenState = rememberSaveable { mutableStateOf(AuthScreenState.Welcome) }

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
    // Observe ViewModel password update request and show a dialog when non-null
    val passwordUpdateRequest by viewModel.passwordUpdateRequest.collectAsState(initial = null)
    // Dialog local state
    val showPasswordUpdateDialog = rememberSaveable { mutableStateOf(false) }
    val oldPasswordField = rememberSaveable { mutableStateOf("") }
    val newPasswordField = rememberSaveable { mutableStateOf("") }
    val newPasswordConfirmField = rememberSaveable { mutableStateOf("") }
    val isUpdatingPassword = remember { mutableStateOf(false) }

    // Shared field colors used by the helper LoginFormView (and available if needed elsewhere)
    val fieldColors = TextFieldDefaults.outlinedTextFieldColors(
        containerColor = Color.White.copy(alpha = 0.98f),
        textColor = Color(0xFF111111),
        placeholderColor = Color(0xFF8A8A8A),
        focusedBorderColor = Color.Transparent,
        unfocusedBorderColor = Color.Transparent,
        cursorColor = Color(0xFF111111)
    )

    // Track whether a register request is in flight so UI gives immediate feedback
    val isRegistering = remember { mutableStateOf(false) }

    // Track whether a login request is in flight so we can disable the Login button and avoid double-taps
    val isLoggingIn = remember { mutableStateOf(false) }

    // Track whether the user pressed the login button this session; used to avoid auto-skip on app start
    val loginAttempted = remember { mutableStateOf(false) }

    // Remember the register button bounds in root coordinates so we can detect taps on it
    val registerButtonBounds = remember { mutableStateOf<Rect?>(null) }

    // Helper that performs the same signup action; we call it from the button and from root tap handler
    fun performSignUp() {
        Toast.makeText(context, "Register clicked", Toast.LENGTH_SHORT).show()
        Log.d("LoginScreen", "Register clicked: username=${username.value}, email=${gmail.value}")
        username.value = username.value.trim()
        isRegistering.value = true
        viewModel.signUp(
            username = username.value,
            password = password.value,
            displayName = displayName.value.trim().ifEmpty { null },
            email = gmail.value.trim(),
            phoneNumber = phone.value.trim().ifEmpty { null },
            address = address.value.trim().ifEmpty { null }
        )
    }

    // Simple validations
    val minPasswordLength = 3
    val passwordsMatch = password.value == passwordConfirm.value
    val passwordLongEnough = password.value.length >= minPasswordLength
    val canLogin = username.value.isNotBlank() && password.value.isNotBlank()
    val canOpenRegister = username.value.isNotBlank() && passwordLongEnough && passwordsMatch

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
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .wrapContentSize(Alignment.Center)
            ) {
                // Card container
                val cardGradient = Brush.verticalGradient(
                    colors = listOf(Color(0xFF7B4CE6), Color(0xFF31186A))
                )

                Box(
                    modifier = Modifier
                        .widthIn(max = 360.dp)
                        .fillMaxSize()
                        .wrapContentSize(Alignment.Center)
                        .clip(RoundedCornerShape(12.dp))
                        .background(cardGradient)
                        .padding(horizontal = 20.dp, vertical = 28.dp)
                ) {
                    // Floating circular menu mimic (moved up so it doesn't overlap the first field)
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .offset(x = (-140).dp, y = 40.dp)
                            .clip(CircleShape)
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("≡", color = Color(0xFF6A4BD6), fontWeight = FontWeight.Bold)
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth(), // removed .verticalScroll(rememberScrollState()) to avoid nested scroll/infinite constraints
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Top
                    ) {
                        when (screenState.value) {
                            AuthScreenState.Register -> {
                                // Keep the existing fancy register UI inline (unchanged)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Welcome", color = Color.White, fontSize = 40.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("Create your account", color = Color.White.copy(alpha = 0.95f))

                                Spacer(modifier = Modifier.height(20.dp))

                                @Composable
                                fun FieldWithLabel(label: String, value: String, onValue: (String) -> Unit, isPassword: Boolean = false) {
                                    Box(modifier = Modifier.fillMaxWidth()) {
                                        OutlinedTextField(
                                            value = value,
                                            onValueChange = onValue,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(64.dp)
                                                .shadow(6.dp, RoundedCornerShape(14.dp)),
                                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                                containerColor = Color(0xFFFFFFFF),
                                                textColor = Color(0xFF030303),
                                                placeholderColor = Color(0xFF6B6B6B),
                                                focusedBorderColor = Color.Transparent,
                                                unfocusedBorderColor = Color.Transparent,
                                                cursorColor = Color(0xFF030303)
                                            ),
                                            shape = RoundedCornerShape(14.dp),
                                            visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
                                            singleLine = true,
                                            textStyle = androidx.compose.ui.text.TextStyle(color = Color(0xFF030303), fontSize = 20.sp, fontWeight = FontWeight.SemiBold),
                                            placeholder = {
                                                if (value.isEmpty()) {
                                                    Text(text = label, color = Color(0xFF6B6B6B), fontSize = 16.sp)
                                                }
                                            }
                                        )

                                        // small label pill (darker purple to stand out)
                                        Box(
                                            modifier = Modifier
                                                .offset(x = 12.dp, y = (-12).dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color(0xFF4B25D6))
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                                .zIndex(2f)
                                        ) {
                                            Text(label, fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
                                        }
                                    }
                                }

                                // Username
                                FieldWithLabel("username", username.value, { username.value = it })
                                Spacer(modifier = Modifier.height(12.dp))

                                // Email
                                FieldWithLabel("Email", gmail.value, { gmail.value = it })
                                Spacer(modifier = Modifier.height(12.dp))

                                // Password
                                FieldWithLabel("Password", password.value, { password.value = it }, isPassword = true)
                                Spacer(modifier = Modifier.height(12.dp))

                                // Confirm Password
                                FieldWithLabel("Confirm Password", passwordConfirm.value, { passwordConfirm.value = it }, isPassword = true)
                                Spacer(modifier = Modifier.height(12.dp))

                                // Display name
                                FieldWithLabel("Display name (optional)", displayName.value, { displayName.value = it })
                                Spacer(modifier = Modifier.height(12.dp))

                                // Phone
                                FieldWithLabel("Phone (optional)", phone.value, { phone.value = it })
                                Spacer(modifier = Modifier.height(12.dp))

                                // Address
                                FieldWithLabel("Address (optional)", address.value, { address.value = it })

                                Spacer(modifier = Modifier.height(18.dp))

                                // Register button
                                var localButtonModifier = Modifier.fillMaxWidth().height(56.dp)
                                localButtonModifier = localButtonModifier.onGloballyPositioned { coords ->
                                    val pos = coords.positionInRoot()
                                    val size = coords.size
                                    registerButtonBounds.value = Rect(pos.x, pos.y, pos.x + size.width.toFloat(), pos.y + size.height.toFloat())
                                }

                                Button(
                                    onClick = { if (canOpenRegister && gmail.value.isNotBlank() && !isRegistering.value) performSignUp() },
                                    modifier = localButtonModifier,
                                    shape = RoundedCornerShape(28.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E88E5), contentColor = Color.White),
                                    enabled = canOpenRegister && gmail.value.isNotBlank() && !isRegistering.value
                                ) {
                                    Text(if (isRegistering.value) "Registering..." else "Register", fontSize = 18.sp)
                                }

                                Spacer(modifier = Modifier.height(12.dp))
                                // Use clickable modifier directly on Text to bypass TextButton issues
                                Text(
                                    text = "Already have an Account? Login",
                                    color = Color.White.copy(alpha = 0.9f),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 12.dp)
                                        .clickable {
                                            Log.d("LoginScreen", "Register view: 'Already have an Account? Login' clicked - switching to Login")
                                            Toast.makeText(context, "Opening login form", Toast.LENGTH_SHORT).show()
                                            screenState.value = AuthScreenState.Login
                                        },
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )

                                status?.let {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(it, color = MaterialTheme.colorScheme.error)
                                }

                                Spacer(modifier = Modifier.height(24.dp))
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
                                        if (canLogin && username.value.trim().isNotBlank() && password.value.isNotBlank() && !isLoggingIn.value) {
                                            Log.d("LoginScreen", "Login button clicked - calling signIn")
                                            // Mark that a login was attempted this session
                                            loginAttempted.value = true
                                            // Disable further taps immediately
                                            isLoggingIn.value = true
                                            // viewModel handles coroutine scope and exceptions internally; no try/finally here
                                            viewModel.signIn(username.value.trim(), password.value)
                                        } else {
                                            Log.w("LoginScreen", "Login validation failed: username blank=${username.value.trim().isBlank()}, password blank=${password.value.isBlank()}, isLoggingIn=${isLoggingIn.value}")
                                        }
                                    },
                                    status = status,
                                    fieldColors = fieldColors,
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

    // Observe currentUser changes and navigate to Categories after a successful login attempt
    LaunchedEffect(currentUser) {
        if (loginAttempted.value && currentUser != 0L) {
            Log.d("LoginScreen", "Detected successful sign-in via currentUser=$currentUser — navigating to Categories")
            navController.navigate(Screen.CategoriesScreen.route) {
                popUpTo(Screen.LoginScreen.route) { inclusive = true }
            }
            // reset the flag so we don't auto-navigate again
            loginAttempted.value = false
        }
    }

    // Sync dialog visibility with ViewModel request
    LaunchedEffect(passwordUpdateRequest) {
        if (passwordUpdateRequest != null) {
            showPasswordUpdateDialog.value = true
        } else {
            // closed/cleared by VM (success or cancelled)
            showPasswordUpdateDialog.value = false
            isUpdatingPassword.value = false
            oldPasswordField.value = ""
            newPasswordField.value = ""
            newPasswordConfirmField.value = ""
        }
    }

    // Password update dialog
    if (showPasswordUpdateDialog.value && passwordUpdateRequest != null) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { if (!isUpdatingPassword.value) showPasswordUpdateDialog.value = false },
            title = { Text("Update password for ${passwordUpdateRequest!!.username}") },
            text = {
                Column {
                    Text("Please enter your old password, then choose a new password.")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = oldPasswordField.value,
                        onValueChange = { oldPasswordField.value = it },
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        label = { Text("Old password") }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newPasswordField.value,
                        onValueChange = { newPasswordField.value = it },
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        label = { Text("New password") }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newPasswordConfirmField.value,
                        onValueChange = { newPasswordConfirmField.value = it },
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        label = { Text("Confirm new password") }
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        // validate
                        val oldPw = oldPasswordField.value
                        val newPw = newPasswordField.value
                        val confirmPw = newPasswordConfirmField.value
                        if (newPw.length < 3) {
                            coroutineScope.launch { snackbarHostState.showSnackbar("New password too short") }
                            return@TextButton
                        }
                        if (newPw != confirmPw) {
                            coroutineScope.launch { snackbarHostState.showSnackbar("Passwords do not match") }
                            return@TextButton
                        }
                        // call ViewModel to update
                        isUpdatingPassword.value = true
                        viewModel.updatePassword(passwordUpdateRequest!!.username, oldPw, newPw)
                    },
                    enabled = !isUpdatingPassword.value
                ) {
                    if (isUpdatingPassword.value) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp))
                    } else {
                        Text("Update")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { if (!isUpdatingPassword.value) { showPasswordUpdateDialog.value = false; /* do not clear VM here */ } }, enabled = !isUpdatingPassword.value) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun WelcomeView(onLoginClick: () -> Unit, onSignUpClick: () -> Unit, onContinueGuest: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 380.dp)
            .padding(vertical = 12.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Placeholder for logo -> replaced with colored meal icon (plate + utensil) built from Boxes
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFFFFFFFF).copy(alpha = 0.08f)),
            contentAlignment = Alignment.Center
        ) {
            // Try to load an asset named "welcome_meal.png" from the assets folder first.
            val context = LocalContext.current
            val assetBitmap = remember {
                try {
                    context.assets.open("welcome_meal.png").use { stream ->
                        BitmapFactory.decodeStream(stream)
                    }
                } catch (e: Exception) {
                    null
                }
            }

            if (assetBitmap != null) {
                Image(
                    bitmap = assetBitmap.asImageBitmap(),
                    contentDescription = "Welcome meal",
                    modifier = Modifier.size(100.dp).clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                // If asset not present, try drawable resource named welcome_meal
                val resId = remember { context.resources.getIdentifier("welcome_meal", "drawable", context.packageName) }
                if (resId != 0) {
                    Image(
                        painter = painterResource(id = resId),
                        contentDescription = "Welcome meal",
                        modifier = Modifier.size(100.dp).clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    // Fallback: plate + utensil built from Boxes
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFFC107)),
                        contentAlignment = Alignment.Center
                    ) {
                        // Inner plate
                        Box(
                            modifier = Modifier
                                .size(54.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                        )

                        // Utensil (stylized): small rounded rectangle rotated slightly and offset to the right
                        Box(
                            modifier = Modifier
                                .size(width = 10.dp, height = 44.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF6D4C41))
                                .offset(x = 18.dp)
                                .rotate(18f)
                        ) { }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        Text("Welcome", color = Color.White, fontSize = 40.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(6.dp))
        Text("Login to your create account", color = Color.White.copy(alpha = 0.9f))

        Spacer(modifier = Modifier.height(28.dp))

        Button(
            onClick = onLoginClick,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E86AB))
        ) {
            Text("Login", fontSize = 18.sp)
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = onSignUpClick,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
        ) {
            Text("Sign Up", color = Color.White)
        }

        Spacer(modifier = Modifier.height(18.dp))

        TextButton(onClick = onContinueGuest) {
            Text("Continue as guest", color = Color.White.copy(alpha = 0.85f))
        }
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
    fieldColors: androidx.compose.material3.TextFieldColors,
    // New parameter to indicate login in progress; when true the action button will be disabled
    isLoggingIn: Boolean = false
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 380.dp)
            .padding(24.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Welcome", color = Color.White, fontSize = 40.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(6.dp))
        Text("Login to your create account", color = Color.White.copy(alpha = 0.95f))

        Spacer(modifier = Modifier.height(24.dp))

        // label above field (matches screenshot style)
        Text("username", color = Color(0xFFB29FE4), fontSize = 12.sp, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(
            value = username,
            onValueChange = onUsernameChange,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = fieldColors,
            shape = RoundedCornerShape(12.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))

        Text("Password", color = Color(0xFFB29FE4), fontSize = 12.sp, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(
            value = password,
            onValueChange = onPasswordChange,
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = fieldColors,
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            TextButton(onClick = { /* remember me toggle could go here */ }) { Text("Remember me", color = Color.White.copy(alpha = 0.9f)) }
            TextButton(onClick = { /* forgot password flow */ }) { Text("Forget Password?", color = Color.White.copy(alpha = 0.9f)) }
        }

        Spacer(modifier = Modifier.height(20.dp))
        Button(
            onClick = onLogin,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E88E5), contentColor = Color.White),
            enabled = !isLoggingIn
        ) {
            Text("Login", fontSize = 18.sp)
        }

        Spacer(modifier = Modifier.height(12.dp))
        // This button navigates back to the Register screen, so make the label clear
        TextButton(onClick = onBack) { Text("Don't have an account? Register", color = Color.White.copy(alpha = 0.9f)) }

        status?.let {
            Spacer(modifier = Modifier.height(12.dp))
            Text(it, color = MaterialTheme.colorScheme.error)
        }

        // Extra bottom spacing so the action button isn't obscured by system UI (navigation bar)
        Spacer(modifier = Modifier.height(48.dp))
    }
}

@Suppress("unused")
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
    fieldColors: androidx.compose.material3.TextFieldColors,
    isRegistering: Boolean,
    debugTapHandler: (() -> Unit)? = null,
    onButtonBoundsChange: ((Rect) -> Unit)? = null
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 380.dp)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Welcome", color = Color.White, fontSize = 40.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(6.dp))
        Text("Create your account", color = Color.White.copy(alpha = 0.95f))

        Spacer(modifier = Modifier.height(24.dp))

        Text("username", color = Color(0xFFB29FE4), fontSize = 12.sp, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = username, onValueChange = onUsernameChange, modifier = Modifier.fillMaxWidth().height(56.dp), colors = fieldColors, shape = RoundedCornerShape(12.dp))
        Spacer(modifier = Modifier.height(12.dp))

        Text("Email", color = Color(0xFFB29FE4), fontSize = 12.sp, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = gmail, onValueChange = onGmailChange, modifier = Modifier.fillMaxWidth().height(56.dp), colors = fieldColors, shape = RoundedCornerShape(12.dp))
        Spacer(modifier = Modifier.height(12.dp))

        Text("Password", color = Color(0xFFB29FE4), fontSize = 12.sp, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = password, onValueChange = onPasswordChange, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth().height(56.dp), colors = fieldColors, shape = RoundedCornerShape(12.dp))
        Spacer(modifier = Modifier.height(12.dp))

        Text("Confirm Password", color = Color(0xFFB29FE4), fontSize = 12.sp, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = passwordConfirm, onValueChange = onPasswordConfirmChange, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth().height(56.dp), colors = fieldColors, shape = RoundedCornerShape(12.dp))

        Spacer(modifier = Modifier.height(12.dp))
        Text("Display name (optional)", color = Color(0xFFB29FE4), fontSize = 12.sp, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = displayName, onValueChange = onDisplayNameChange, modifier = Modifier.fillMaxWidth().height(56.dp), colors = fieldColors, shape = RoundedCornerShape(12.dp))
        Spacer(modifier = Modifier.height(12.dp))

        Text("Phone (optional)", color = Color(0xFFB29FE4), fontSize = 12.sp, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = phone, onValueChange = onPhoneChange, modifier = Modifier.fillMaxWidth().height(56.dp), colors = fieldColors, shape = RoundedCornerShape(12.dp))
        Spacer(modifier = Modifier.height(12.dp))

        Text("Address (optional)", color = Color(0xFFB29FE4), fontSize = 12.sp, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = address, onValueChange = onAddressChange, modifier = Modifier.fillMaxWidth().height(56.dp), colors = fieldColors, shape = RoundedCornerShape(12.dp))

        Spacer(modifier = Modifier.height(16.dp))

        // Inline validation hints when register is disabled
        if (!canRegister) {
            if (gmail.isBlank()) {
                Text("Please enter your email.", color = Color(0xFFFFC107))
            }
            if (password.length < 6) {
                Text("Password must be at least 6 characters.", color = Color(0xFFFFC107))
            }
            if (password != passwordConfirm) {
                Text("Passwords do not match.", color = Color(0xFFFFC107))
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Attach a pointerInput on the button container so taps are detected even if the Button is disabled
        var localButtonModifier = Modifier.fillMaxWidth().height(56.dp)
        if (debugTapHandler != null) {
            localButtonModifier = localButtonModifier.pointerInput(Unit) {
                detectTapGestures(onTap = { debugTapHandler.invoke() })
            }
        }

        // Report the button bounds to the parent so root-level taps can be mapped to this button
        localButtonModifier = localButtonModifier.onGloballyPositioned { coords ->
            val pos = coords.positionInRoot()
            val size = coords.size
            onButtonBoundsChange?.invoke(Rect(pos.x, pos.y, pos.x + size.width.toFloat(), pos.y + size.height.toFloat()))
        }

        Button(onClick = onRegister, modifier = localButtonModifier, shape = RoundedCornerShape(28.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E88E5), contentColor = Color.White), enabled = canRegister && !isRegistering) {
            Text(if (isRegistering) "Registering..." else "Register", fontSize = 18.sp)
        }

        Spacer(modifier = Modifier.height(12.dp))
        TextButton(
            onClick = {
                Log.d("LoginScreen", "RegisterFormView: 'Already have an Account? Login' clicked - invoking onBack")
                onBack()
            },
            modifier = Modifier.fillMaxWidth().zIndex(3f)
        ) { Text("Already have an Account? Login", color = Color.White.copy(alpha = 0.9f)) }

        status?.let {
            Spacer(modifier = Modifier.height(12.dp))
            Text(it, color = MaterialTheme.colorScheme.error)
        }

        // Extra bottom spacing so the action button isn't obscured by system UI (navigation bar)
        Spacer(modifier = Modifier.height(48.dp))
    }
}
