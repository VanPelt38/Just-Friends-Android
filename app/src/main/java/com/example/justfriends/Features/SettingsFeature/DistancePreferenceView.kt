@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.justfriends.Features.SettingsFeature

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.justfriends.ui.theme.JustFriendsTheme

private val brandColor = Color(red = 19, green = 0, blue = 142)

@Composable
fun DistancePreferenceView(viewModel: SettingsViewModel) {

    LaunchedEffect(true) {
        viewModel.loadDistancePreference()
    }

    JustFriendsTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(60.dp))
            Text(
                text = "${viewModel.distanceKm.value.toInt()} km",
                fontSize = 56.sp,
                color = brandColor
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Search radius",
                fontSize = 16.sp,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(32.dp))
            Slider(
                value = viewModel.distanceKm.value,
                onValueChange = { viewModel.distanceKm.value = it },
                valueRange = 1f..100f,
                modifier = Modifier.fillMaxWidth(),
                colors = SliderDefaults.colors(
                    thumbColor = brandColor,
                    activeTrackColor = brandColor,
                    inactiveTrackColor = Color.LightGray
                )
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("1 km", color = Color.Gray, fontSize = 13.sp)
                Text("100 km", color = Color.Gray, fontSize = 13.sp)
            }
            Spacer(modifier = Modifier.height(48.dp))
            Button(
                onClick = { viewModel.saveDistancePreference(viewModel.distanceKm.value) },
                modifier = Modifier
                    .width(200.dp)
                    .height(44.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = brandColor,
                    contentColor = Color.White
                )
            ) {
                Text("Save", fontSize = 18.sp)
            }
        }
    }
}
