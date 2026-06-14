package com.example.justfriends.Features.ForgotPasswordFeature

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.justfriends.ui.theme.JustFriendsTheme

@Composable
fun ForgotPasswordView(viewModel: ForgotPasswordViewModel) {
    JustFriendsTheme {
        Box {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(40.dp)
            ) {
                Spacer(modifier = Modifier.height(120.dp))
                Text(
                    "Please enter your email address and we'll send you a link to reset your password.",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.Black,
                    fontSize = 30.sp
                )
                Spacer(modifier = Modifier.height(30.dp))
                OutlinedTextField(
                    value = viewModel.email.value,
                    onValueChange = { viewModel.email.value = it },
                    modifier = Modifier
                        .width(200.dp)
                        .height(50.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent
                    ),
                    textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 20.sp),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(50.dp))
                Button(
                    onClick = { viewModel.sendLink() },
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
                        text = "send link",
                        color = Color.White,
                        style = MaterialTheme.typography.titleLarge,
                        fontSize = 20.sp
                    )
                }
                Spacer(modifier = Modifier.height(30.dp))
                if (viewModel.showError.value) {
                    Text(
                        viewModel.errorMessage.value,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Red,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}
