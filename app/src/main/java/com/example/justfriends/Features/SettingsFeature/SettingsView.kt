package com.example.justfriends.Features.SettingsFeature

import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.justfriends.ui.theme.JustFriendsTheme
import com.google.firebase.auth.FirebaseAuth

@Composable
fun SettingsView(viewModel: SettingsViewModel, padding: PaddingValues) {

    LaunchedEffect(true) {
        viewModel.setScaffold()
    }

    val alertState by viewModel.alertState
    val context = LocalContext.current

    JustFriendsTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize()) {
                SettingsRow(title = "Distance Preferences") {
                    viewModel.navigateToDistancePreference()
                }
                SettingsDivider()
                SettingsRow(title = "Log Out") {
                    viewModel.confirmLogOut()
                }
                SettingsDivider()
                SettingsRow(title = "Delete Account", isDestructive = true) {
                    viewModel.confirmDeleteAccount()
                }
                SettingsDivider()
                SettingsRow(title = "Contact Us") {
                    val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:")
                        putExtra(Intent.EXTRA_EMAIL, arrayOf("justfriendshelpdesk@gmail.com"))
                        putExtra(Intent.EXTRA_SUBJECT, "Just Friends Support Issue")
                        putExtra(
                            Intent.EXTRA_TEXT,
                            "User ID: ${FirebaseAuth.getInstance().currentUser?.uid}, Version: ${Build.VERSION.RELEASE}"
                        )
                    }
                    if (emailIntent.resolveActivity(context.packageManager) != null) {
                        context.startActivity(emailIntent)
                    }
                }
                SettingsDivider()
            }

            if (viewModel.isLoading.value) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f))
                        .clickable { }
                ) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }
    }

    alertState?.let {
        AlertDialog(
            onDismissRequest = { viewModel.dismissAlert() },
            title = { Text(viewModel.alertTitle.value ?: "") },
            text = { Text(it) },
            confirmButton = {
                Button(onClick = {
                    viewModel.pendingAction?.invoke()
                    viewModel.dismissAlert()
                }) {
                    Text(viewModel.alertAccept.value ?: "OK")
                }
            },
            dismissButton = {
                Button(onClick = { viewModel.dismissAlert() }) {
                    Text(viewModel.alertDecline.value ?: "Cancel")
                }
            }
        )
    }
}

@Composable
fun SettingsRow(title: String, isDestructive: Boolean = false, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .clickable { onClick() }
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            fontSize = 18.sp,
            color = if (isDestructive) Color.Red else Color.Black
        )
    }
}

@Composable
fun SettingsDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 20.dp),
        color = Color.LightGray
    )
}
