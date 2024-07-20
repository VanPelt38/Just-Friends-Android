package com.example.justfriends.Features.ForgotPasswordFeature

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import com.example.justfriends.ui.theme.JustFriendsTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier

@Composable
fun ForgotPasswordView() {
    JustFriendsTheme {
        Scaffold(
            content = { paddingValues ->
                Column(
                    modifier = Modifier.padding(paddingValues)
                ) {
                    Text("I am awesome at Android")
                }
            }
        )
    }
}