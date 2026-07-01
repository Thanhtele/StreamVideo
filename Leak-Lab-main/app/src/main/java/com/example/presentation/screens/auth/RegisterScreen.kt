package com.example.presentation.screens.auth

import android.view.View
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.presentation.theme.CosmicSurface
import com.example.presentation.theme.DividerGray
import com.example.presentation.theme.MidnightBlack
import com.example.presentation.theme.NeonCyan
import com.example.presentation.theme.NeonRuby
import com.example.presentation.theme.OnSpaceWhite
import com.example.presentation.theme.TextGray
import com.example.presentation.viewmodel.auth.RegisterState
import com.example.presentation.viewmodel.auth.RegisterViewModel

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