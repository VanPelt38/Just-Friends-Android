package com.example.justfriends.ReusableViews

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Settings

import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.justfriends.R

@Composable
fun TabBar(selectedTab: Int, onSelected: (Int) -> Unit) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(red = 19, green = 0, blue = 142)),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = { onSelected(0) },
            modifier = Modifier
                .width(46.dp)
                .height(46.dp),
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = Color.Transparent
            )
        ) {
            Icon(
                imageVector = Icons.Outlined.Home,
                contentDescription = "home",
                tint = if (selectedTab == 0) Color.White else Color.Gray
            )
        }
        IconButton(
            onClick = {
                onSelected(1)
            },
            modifier = Modifier
                .width(46.dp)
                .height(46.dp),
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = Color.Transparent
            )
        ) {
//            Icon(
//                imageVector = Icons.Outlined.Person,
//                contentDescription = "friends",
//                tint = if (selectedTab == 1) Color.White else Color.Gray
//            )
            Icon(
                painter = painterResource(id = R.drawable.group_24px),
                contentDescription = "friend",
                tint = Color.White
            )
        }
        IconButton(
            onClick = {
                onSelected(2)
            },
            modifier = Modifier
                .width(46.dp)
                .height(46.dp),
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = Color.Transparent
            )
        ) {
            Icon(
                imageVector = Icons.Outlined.Settings,
                contentDescription = "settings",
                tint = if (selectedTab == 2) Color.White else Color.Gray
            )
        }
    }
}