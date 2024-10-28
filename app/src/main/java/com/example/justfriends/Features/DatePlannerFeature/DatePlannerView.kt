package com.example.justfriends.Features.DatePlannerFeature

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuItemColors
import androidx.compose.runtime.Composable
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.remember
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import android.app.Activity
import androidx.compose.runtime.*
import androidx.compose.ui.text.style.TextAlign
import com.example.justfriends.Features.ProfileSetUpFeature.ChoosePhotoButton
import com.example.justfriends.Features.ProfileSetUpFeature.DatePickerButton
import com.example.justfriends.Features.ProfileSetUpFeature.GenderButtons
import com.example.justfriends.ui.theme.JustFriendsTheme

@Composable
fun DatePlannerView(viewModel: DatePlannerViewModel) {

    val errorAlertStateView by viewModel.errorAlertState
    val activity = LocalContext.current as? Activity

    LaunchedEffect(Unit) {
        activity?.let {
            viewModel.setNavTitle()
            viewModel.requestLocationPermission(it)
        }
    }

    JustFriendsTheme {
        Box() {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(40.dp)
            ) {
                Spacer(modifier = Modifier.height(30.dp))
                Text(
                    "What would you like to do?",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.Black,
                    fontSize = 30.sp
                )
                Spacer(modifier = Modifier.height(50.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "I want to",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.Black,
                        fontSize = 20.sp
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    TextField(
                        value = viewModel.activity.value,
                        placeholder = { "e.g. ...go to the cinema" },
                        onValueChange = { viewModel.activity.value = it },
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
                }
                Spacer(modifier = Modifier.height(80.dp))
                Text(
                    "And when?",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.Black,
                    fontSize = 30.sp
                )
                Spacer(modifier = Modifier.height(50.dp))

                Box(
                    contentAlignment = Alignment.Center
                ) {
                    DateTimeOptionsList(vm = viewModel)
                }
                Spacer(modifier = Modifier.height(70.dp))
                Button(
                    onClick = {
                        viewModel.seeWhosAvailablePressed()
                    },
                    modifier = Modifier
                        .width(300.dp)
                        .height(60.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Black,
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = "see who's available",
                        color = Color.White,
                        style = MaterialTheme.typography.titleLarge,
                        fontSize = 25.sp
                    )
                }

            }
            errorAlertStateView?.let { error ->
                AlertDialog(onDismissRequest = {

                    viewModel.errorAlertState.value = null
                    viewModel.errorAlertStateTitle.value = null
                },
                    title = {Text(viewModel.errorAlertStateTitle.value ?: "")},
                    text = {Text(viewModel.errorAlertState.value ?: "error")},
                    confirmButton = {
                        Button(
                            onClick = {
                                viewModel.errorAlertState.value = null
                                viewModel.errorAlertStateTitle.value = null
                            }
                        ) {
                            Text("Okay")
                        }
                    })
            }
            if (viewModel.isLoading.value) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f))
                        .clickable { }
                        .align(Alignment.Center)
                ) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }
    }
}

@Composable
fun DateTimeOptionsList(vm: DatePlannerViewModel) {
    var expanded by remember { mutableStateOf(false) }
    var options = listOf("today", "tonight", "tomorrow", "this week", "this weekend", "next week", "next weekend")
    
    Box(
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = vm.selectedOption.value,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier
                .clickable { expanded = true }
                .padding(16.dp),
            fontSize = 20.sp
        )
        DropdownMenu(expanded = expanded,
            onDismissRequest = { expanded = false }) {
            for (option in options) {
                DropdownMenuItem(
                    text = {
                        Text(
                            text = option,
                            color = Color.Black
                        )

                           },
                    colors = MenuItemColors(
                        disabledTextColor = Color.Black,
                        textColor = Color.Black,
                        disabledLeadingIconColor = Color.Black,
                        disabledTrailingIconColor = Color.Black,
                        leadingIconColor = Color.Black,
                        trailingIconColor = Color.Black
                    ),
                    onClick = {
                        vm.selectedOption.value = option
                        expanded = false
                    }
                )
            }
        }
    }
            
}