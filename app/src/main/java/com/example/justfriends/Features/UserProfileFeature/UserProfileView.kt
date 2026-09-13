package com.example.justfriends.Features.UserProfileFeature

import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.platform.LocalLifecycleOwner
import com.example.justfriends.R
import com.example.justfriends.ReusableViews.ProfileDisplay
import com.example.justfriends.ReusableViews.brandPurple
import com.example.justfriends.ui.theme.JustFriendsTheme

@Composable
fun UserProfileView(viewModel: UserProfileViewModel) {

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Reload profile whenever this screen resumes (e.g. returning from EditProfile)
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

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                val cursor = context.contentResolver.query(uri, null, null, null, null)
                var ext: String? = null
                cursor?.use {
                    val index = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (index != -1 && it.moveToFirst()) {
                        ext = it.getString(index).substringAfterLast('.', "")
                    }
                }
                viewModel.onPhotoSelected(uri, ext)
            }
        }
    )

    val errorAlert by viewModel.errorAlertState

    JustFriendsTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Profile photo with change button overlaid
                Box(contentAlignment = Alignment.BottomCenter) {
                    ProfileDisplay(
                        name = viewModel.name.value,
                        age = viewModel.age.value,
                        gender = viewModel.gender.value,
                        town = viewModel.town.value,
                        occupation = viewModel.occupation.value,
                        summary = viewModel.summary.value,
                        interests = viewModel.interests.value,
                        profilePicUrl = viewModel.profilePicUrl.value
                    )
                }

                // Camera button overlaid on the profile picture is tricky with ProfileDisplay,
                // so we place the edit photo button below the display
                IconButton(
                    onClick = {
                        photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    },
                    modifier = Modifier.size(40.dp),
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = Color.Black,
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.camera_image),
                        contentDescription = "Change photo",
                        tint = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = { viewModel.navigateToEditProfile() },
                    modifier = Modifier
                        .width(200.dp)
                        .height(44.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = brandPurple,
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        Icons.Filled.Edit,
                        contentDescription = "Edit",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Text("  Edit Profile", fontSize = 16.sp)
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
            onDismissRequest = {
                viewModel.errorAlertState.value = null
                viewModel.errorAlertStateTitle.value = null
            },
            title = { Text(viewModel.errorAlertStateTitle.value ?: "") },
            text = { Text(it) },
            confirmButton = {
                Button(onClick = {
                    viewModel.errorAlertState.value = null
                    viewModel.errorAlertStateTitle.value = null
                }) { Text("OK") }
            }
        )
    }
}
