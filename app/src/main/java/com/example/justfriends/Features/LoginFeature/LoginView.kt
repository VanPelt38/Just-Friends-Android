package com.example.justfriends.Features.LoginFeature

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextField
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.justfriends.ui.theme.JustFriendsTheme
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.Column
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.ui.text.input.VisualTransformation
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.material3.AlertDialog
import android.app.Application
import androidx.compose.foundation.border
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.navigation.NavController
import com.example.justfriends.Navigation.View

@Composable
fun LoginView(loginViewModel: LoginViewModel) {

    val errorAlertStateView by loginViewModel.errorAlertState

    JustFriendsTheme {
        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
                Gradient()
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(150.dp))
                Text("Just Friends.",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontSize = 50.sp
                )
                Spacer(modifier = Modifier.height(30.dp))
                Text("New friends. Lasting memories.",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    fontSize = 15.sp
                    )
                Spacer(modifier = Modifier.height(120.dp))
                OutlinedTextField(
                    value = loginViewModel.userEmail.value,
                    onValueChange = { loginViewModel.userEmail.value = it },
                            modifier = Modifier
                                .width(200.dp)
                                .height(45.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = Color.White,
                        focusedContainerColor = Color.White,
                        unfocusedBorderColor = Color.Transparent,
                        focusedBorderColor = Color.Transparent
                    ),
                    textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                    singleLine = true,
                    visualTransformation = VisualTransformation.None
                )
                Spacer(modifier = Modifier.height(30.dp))
                OutlinedTextField(value = loginViewModel.userPassword.value,
                    onValueChange = { loginViewModel.userPassword.value = it },
                    modifier = Modifier
                        .width(200.dp)
                        .height(45.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = Color.White,
                        focusedContainerColor = Color.White,
                        unfocusedBorderColor = Color.Transparent,
                        focusedBorderColor = Color.Transparent
                    ),
                    textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation()
                )
                Spacer(modifier = Modifier.height(30.dp))
                Button(
                    onClick = {
                        loginViewModel.signInPressed()
                    },
                    modifier = Modifier
                        .width(200.dp)
                        .height(40.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Black,
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = "sign in",
                        color = Color.White,
                        style = MaterialTheme.typography.titleLarge,
                        fontSize = 20.sp
                    )
                }
                Spacer(modifier = Modifier.height(30.dp))
                Button(
                    onClick = {
                        loginViewModel.signUpPressed()
                    },
                    modifier = Modifier
                        .width(200.dp)
                        .height(40.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color.Black
                    )
                ) {
                    Text(
                        text = "sign up",
                        color = Color.Black,
                        style = MaterialTheme.typography.titleLarge,
                        fontSize = 20.sp
                    )
                }
                Spacer(modifier = Modifier.height(30.dp))
                Button(
                    onClick = {
                        loginViewModel.forgotPasswordPressed()
                    },
                    modifier = Modifier
                        .width(400.dp)
                        .height(40.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent
                    )
                ) {
                    Text(
                        text = "forgotten your password?",
                        color = Color.Black
                    )
                }
            }

            errorAlertStateView?.let { error ->
                AlertDialog(onDismissRequest = {

                    loginViewModel.errorAlertState.value = null
                    loginViewModel.errorAlertStateTitle.value = null
                },
                    title = {Text(loginViewModel.errorAlertStateTitle.value ?: "")},
                    text = {Text(loginViewModel.errorAlertState.value ?: "error")},
                    confirmButton = {
                        Button(
                            onClick = {
                                loginViewModel.errorAlertState.value = null
                                loginViewModel.errorAlertStateTitle.value = null
                            }
                        ) {
                            Text("Okay")
                        }
                })
            }

        }
    }
}

@Composable
fun Gradient() {
    androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(Color(red = 19, green = 0, blue = 142), Color(red = 0.510f, green = 0.482f, blue = 1.0f, alpha = 1.0f))
            )
        )
    }
}
