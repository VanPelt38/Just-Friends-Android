@file:OptIn(ExperimentalLayoutApi::class)

package com.example.justfriends.Features.UserProfileFeature

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.platform.LocalLifecycleOwner
import com.example.justfriends.ReusableViews.InterestToggleChip
import com.example.justfriends.ReusableViews.brandPurple
import com.example.justfriends.ui.theme.JustFriendsTheme

@Composable
fun EditProfileView(viewModel: EditProfileViewModel, onSaved: () -> Unit) {

    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.setScaffold()
                viewModel.loadProfile()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(viewModel.savedSuccessfully.value) {
        if (viewModel.savedSuccessfully.value) {
            viewModel.savedSuccessfully.value = false
            onSaved()
        }
    }

    val errorAlert by viewModel.errorAlertState

    JustFriendsTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                EditField(label = "Town", value = viewModel.town.value) {
                    viewModel.town.value = it
                }
                Spacer(modifier = Modifier.height(12.dp))
                EditField(label = "Profession", value = viewModel.occupation.value) {
                    viewModel.occupation.value = it
                }
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = viewModel.summary.value,
                    onValueChange = { viewModel.summary.value = it },
                    label = { Text("About me") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    maxLines = 5
                )

                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    "Interests (max 5)",
                    style = MaterialTheme.typography.titleMedium,
                    color = brandPurple,
                    fontSize = 16.sp
                )
                Text(
                    "${viewModel.selectedInterests.value.size}/5 selected",
                    color = Color.Gray,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(12.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    viewModel.allInterests.forEach { interest ->
                        InterestToggleChip(
                            interest = interest,
                            isSelected = interest in viewModel.selectedInterests.value,
                            onClick = { viewModel.toggleInterest(interest) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
                HorizontalDivider(color = Color.LightGray)
                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { viewModel.saveProfile() },
                    modifier = Modifier
                        .width(200.dp)
                        .height(44.dp)
                        .align(Alignment.CenterHorizontally),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = brandPurple,
                        contentColor = Color.White
                    )
                ) {
                    Text("Save Profile", fontSize = 16.sp)
                }
                Spacer(modifier = Modifier.height(80.dp))
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

    errorAlert?.let {
        AlertDialog(
            onDismissRequest = { viewModel.errorAlertState.value = null },
            text = { Text(it) },
            confirmButton = {
                Button(onClick = { viewModel.errorAlertState.value = null }) { Text("OK") }
            }
        )
    }
}

@Composable
private fun EditField(label: String, value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true
    )
}
