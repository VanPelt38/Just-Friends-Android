@file:OptIn(ExperimentalLayoutApi::class)

package com.example.justfriends.ReusableViews

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.justfriends.R

val brandPurple = Color(red = 19, green = 0, blue = 142)

@Composable
fun ProfileDisplay(
    name: String,
    age: String,
    gender: String,
    town: String?,
    occupation: String?,
    summary: String?,
    interests: List<String>,
    profilePicUrl: String?
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        Box(
            modifier = Modifier
                .size(160.dp)
                .clip(CircleShape)
                .background(Color.LightGray)
        ) {
            if (profilePicUrl != null) {
                Image(
                    painter = rememberAsyncImagePainter(model = Uri.parse(profilePicUrl)),
                    contentDescription = "Profile",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Icon(
                    imageVector = Icons.Outlined.Person,
                    contentDescription = "Profile",
                    tint = brandPurple,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
        Text(name, fontSize = 24.sp, color = Color.Black, style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(8.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(age, color = Color.Gray, fontSize = 14.sp)
            if (gender.isNotEmpty()) {
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    painter = painterResource(id = if (gender == "male") R.drawable.male_24px else R.drawable.female_24px),
                    contentDescription = gender,
                    tint = Color.Gray,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        val locationLine = listOfNotNull(
            town?.takeIf { it.isNotBlank() },
            occupation?.takeIf { it.isNotBlank() }
        ).joinToString(" · ")
        if (locationLine.isNotEmpty()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(locationLine, color = Color.Gray, fontSize = 14.sp)
        }

        Spacer(modifier = Modifier.height(20.dp))
        HorizontalDivider(color = Color.LightGray)
        Spacer(modifier = Modifier.height(16.dp))

        if (!summary.isNullOrBlank()) {
            ProfileSection(title = "About me") {
                Text(summary, color = Color.Black, fontSize = 15.sp)
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (interests.isNotEmpty()) {
            ProfileSection(title = "Interests") {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    interests.forEach { InterestChip(it) }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun ProfileSection(title: String, content: @Composable () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(title, fontSize = 16.sp, color = brandPurple, style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        content()
    }
}

@Composable
fun InterestChip(interest: String) {
    Box(
        modifier = Modifier
            .background(brandPurple.copy(alpha = 0.12f), RoundedCornerShape(16.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(interest, color = brandPurple, fontSize = 13.sp)
    }
}

@Composable
fun InterestToggleChip(interest: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .background(
                color = if (isSelected) brandPurple else Color(0xFFE0E0E0),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(
            text = interest,
            color = if (isSelected) Color.White else Color.DarkGray,
            fontSize = 13.sp
        )
    }
}
