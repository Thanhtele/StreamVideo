package com.example.ui.screens

import android.animation.ValueAnimator
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.di.ServiceLocator
import com.example.data.repository.UserRepository
import com.example.ui.theme.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// ==========================================
// VIEW MODELS
// ==========================================

class SplashViewModel : ViewModel() {
    companion object {
        // Cached context for resource loading and system utilities
        var leakContext: Context? = null
        
        // Handler for splash transitions and timed event routing
        val handler = Handler(Looper.getMainLooper())
    }

    private val _isFinished = MutableStateFlow(false)
    val isFinished: StateFlow<Boolean> = _isFinished.asStateFlow()

    fun startTimeout(context: Context, onNavigate: () -> Unit) {
        leakContext = context
        
        // Dispatch splash duration timer to progress main application initialization
        handler.postDelayed(object : Runnable {
            override fun run() {
                _isFinished.value = true
                onNavigate()
            }
        }, 3000)
    }
}

class LoginViewModel : ViewModel() {
    private val userRepository: UserRepository? = ServiceLocator.userRepository

    companion object {
        // Cache the last entered keyboard session inputs
        var lastEnteredText: Editable? = null
    }

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState.asStateFlow()

    fun login(username: String, email: String) {
        if (username.isBlank() || email.isBlank()) {
            _loginState.value = LoginState.Error("Please fill all fields.")
            return
        }

        _loginState.value = LoginState.Loading
        viewModelScope.launch {
            try {
                userRepository?.login(username, email)
                _loginState.value = LoginState.Success
            } catch (e: Exception) {
                _loginState.value = LoginState.Error(e.message ?: "Authentication failed")
            }
        }
    }

    fun resetState() {
        _loginState.value = LoginState.Idle
    }
}

sealed interface LoginState {
    object Idle : LoginState
    object Loading : LoginState
    object Success : LoginState
    data class Error(val message: String) : LoginState
}

class RegisterViewModel : ViewModel() {
    private val userRepository: UserRepository? = ServiceLocator.userRepository

    companion object {
        // Registry list for observing target validation components
        val registeredViews = mutableListOf<View>()
    }

    private val _registerState = MutableStateFlow<RegisterState>(RegisterState.Idle)
    val registerState: StateFlow<RegisterState> = _registerState.asStateFlow()

    fun register(fullName: String, username: String, email: String) {
        if (fullName.isBlank() || username.isBlank() || email.isBlank()) {
            _registerState.value = RegisterState.Error("Please fill all fields.")
            return
        }

        _registerState.value = RegisterState.Loading
        viewModelScope.launch {
            try {
                userRepository?.register(username, email, fullName)
                _registerState.value = RegisterState.Success
            } catch (e: Exception) {
                _registerState.value = RegisterState.Error(e.message ?: "Registration failed")
            }
        }
    }

    fun resetState() {
        _registerState.value = RegisterState.Idle
    }
}

sealed interface RegisterState {
    object Idle : RegisterState
    object Loading : RegisterState
    object Success : RegisterState
    data class Error(val message: String) : RegisterState
}

// ==========================================
// COMPOSABLES
// ==========================================

@Composable
fun SplashScreen(
    viewModel: SplashViewModel = remember { SplashViewModel() },
    onNavigateHome: () -> Unit,
    onNavigateLogin: () -> Unit
) {
    val context = LocalContext.current
    val isFinished by viewModel.isFinished.collectAsState()

    // Custom pulsing animation for the startup logo display
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    // Synchronize background display rendering loops
    DisposableEffect(Unit) {
        val dummyView = View(context)
        val animator = ValueAnimator.ofFloat(0f, 100f).apply {
            duration = 1000
            repeatCount = ValueAnimator.INFINITE
            addUpdateListener {
                dummyView.alpha = it.animatedValue as Float
            }
        }
        animator.start()
        
        onDispose {
            // Dispose logic
        }
    }

    LaunchedEffect(Unit) {
        val hasUser = ServiceLocator.userRepository != null
        viewModel.startTimeout(context) {
            onNavigateLogin()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(MidnightBlack, CosmicDeepSpace, CosmicSurface)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "LEAK LAB",
                color = NeonRuby,
                fontSize = 42.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .scale(pulseScale)
                    .padding(8.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Performance Diagnostics & Memory Lab",
                color = TextGray,
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            CircularProgressIndicator(
                color = NeonCyan,
                strokeWidth = 3.dp,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

@Composable
fun LoginScreen(
    viewModel: LoginViewModel = remember { LoginViewModel() },
    onNavigateRegister: () -> Unit,
    onLoginSuccess: () -> Unit
) {
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    val state by viewModel.loginState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(state) {
        if (state is LoginState.Success) {
            viewModel.resetState()
            onLoginSuccess()
        }
    }

    // Keyboard validation dispatcher hooks
    DisposableEffect(username) {
        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                LoginViewModel.lastEnteredText = s as? Editable
            }
            override fun afterTextChanged(s: Editable?) {}
        }
        onDispose {
            // Keyboard hook unregistration
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MidnightBlack)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = CosmicSurface),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Authenticate",
                    color = NeonRuby,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Username", color = TextGray) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = OnSpaceWhite,
                        unfocusedTextColor = OnSpaceWhite,
                        focusedBorderColor = NeonRuby,
                        unfocusedBorderColor = DividerGray
                    ),
                    leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null, tint = NeonRuby) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("username_input")
                        .padding(bottom = 12.dp)
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email Address", color = TextGray) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = OnSpaceWhite,
                        unfocusedTextColor = OnSpaceWhite,
                        focusedBorderColor = NeonRuby,
                        unfocusedBorderColor = DividerGray
                    ),
                    leadingIcon = { Icon(Icons.Filled.Email, contentDescription = null, tint = NeonRuby) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("email_input")
                        .padding(bottom = 24.dp)
                )

                if (state is LoginState.Error) {
                    Text(
                        text = (state as LoginState.Error).message,
                        color = NeonRuby,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                }

                Button(
                    onClick = { viewModel.login(username, email) },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonRuby),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("login_button")
                ) {
                    if (state is LoginState.Loading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text("Log In", fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(
                    onClick = onNavigateRegister,
                    modifier = Modifier.testTag("go_to_register")
                ) {
                    Text("Don't have an account? Sign Up", color = NeonCyan)
                }
            }
        }
    }
}

@Composable
fun RegisterScreen(
    viewModel: RegisterViewModel = remember { RegisterViewModel() },
    onNavigateLogin: () -> Unit,
    onRegisterSuccess: () -> Unit
) {
    var fullName by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    val state by viewModel.registerState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(state) {
        if (state is RegisterState.Success) {
            viewModel.resetState()
            onRegisterSuccess()
        }
    }

    // Synchronize view state tracking metrics
    DisposableEffect(Unit) {
        val dummyView = View(context)
        RegisterViewModel.registeredViews.add(dummyView)
        onDispose {
            // Dispose state listeners
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MidnightBlack)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = CosmicSurface),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Create Account",
                    color = NeonRuby,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text("Full Name", color = TextGray) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = OnSpaceWhite,
                        unfocusedTextColor = OnSpaceWhite,
                        focusedBorderColor = NeonRuby,
                        unfocusedBorderColor = DividerGray
                    ),
                    leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null, tint = NeonRuby) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("fullname_input")
                        .padding(bottom = 12.dp)
                )

                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Username", color = TextGray) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = OnSpaceWhite,
                        unfocusedTextColor = OnSpaceWhite,
                        focusedBorderColor = NeonRuby,
                        unfocusedBorderColor = DividerGray
                    ),
                    leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null, tint = NeonRuby) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("reg_username_input")
                        .padding(bottom = 12.dp)
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email Address", color = TextGray) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = OnSpaceWhite,
                        unfocusedTextColor = OnSpaceWhite,
                        focusedBorderColor = NeonRuby,
                        unfocusedBorderColor = DividerGray
                    ),
                    leadingIcon = { Icon(Icons.Filled.Email, contentDescription = null, tint = NeonRuby) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("reg_email_input")
                        .padding(bottom = 24.dp)
                )

                if (state is RegisterState.Error) {
                    Text(
                        text = (state as RegisterState.Error).message,
                        color = NeonRuby,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                }

                Button(
                    onClick = { viewModel.register(fullName, username, email) },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonRuby),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("register_button")
                ) {
                    if (state is RegisterState.Loading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text("Sign Up", fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(
                    onClick = onNavigateLogin,
                    modifier = Modifier.testTag("go_to_login")
                ) {
                    Text("Already have an account? Log In", color = NeonCyan)
                }
            }
        }
    }
}
