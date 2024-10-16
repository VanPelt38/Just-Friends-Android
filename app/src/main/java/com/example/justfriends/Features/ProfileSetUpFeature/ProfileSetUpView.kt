@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.justfriends.Features.ProfileSetUpFeature

import android.app.DatePickerDialog
import android.widget.DatePicker
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.RadioButton
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.IconButton
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import com.example.justfriends.ui.theme.JustFriendsTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.justfriends.R
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import java.util.Calendar
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.layout.ContentScale
import coil.compose.rememberAsyncImagePainter

@Composable
fun ProfileSetUpView(viewModel: ProfileSetUpViewModel) {

 val mContext = LocalContext.current
    val mCalendar = Calendar.getInstance()
    val mYear = mCalendar.get(Calendar.YEAR)
    val mMonth = mCalendar.get(Calendar.MONTH)
    val mDay = mCalendar.get(Calendar.DAY_OF_MONTH)

    val mDatePickerDialog = DatePickerDialog(
        mContext,
        { _: DatePicker, mYear: Int, mMonth: Int, mDayOfMonth: Int ->
            viewModel.dobString.value = "$mDayOfMonth/${mMonth+1}/$mYear"
            val selectedDate = Calendar.getInstance().apply {
                set(mYear, mMonth, mDayOfMonth)
            }
            viewModel.dobDate = selectedDate.time
            Log.d("", "date chosen: ${viewModel.dobDate}")
        }, mYear, mMonth, mDay
    )
    val errorAlertStateView by viewModel.errorAlertState

    JustFriendsTheme {
        Box() {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(40.dp)
            ) {
                Spacer(modifier = Modifier.height(120.dp))
                Text(
                    "Let's get your profile set up...",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.Black,
                    fontSize = 30.sp
                )
                Spacer(modifier = Modifier.height(30.dp))
                Box(
                    contentAlignment = Alignment.BottomCenter
                ) {
                    SmileyFace(vm = viewModel)
                    ChoosePhotoButton(vm = viewModel)
                }
                Spacer(modifier = Modifier.height(50.dp))
                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "name:",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.Black,
                        fontSize = 26.sp
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    TextField(
                        value = viewModel.name.value,
                        onValueChange = { viewModel.name.value = it },
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
                Spacer(modifier = Modifier.height(30.dp))
                Row(
                    modifier = Modifier.align(Alignment.Start)
                ) {
                    Text(
                        "date of birth:",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.Black,
                        fontSize = 26.sp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    DatePickerButton(vm = viewModel, onClick = { mDatePickerDialog.show() })
                }
                Spacer(modifier = Modifier.height(30.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "gender:",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.Black,
                        fontSize = 26.sp
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    GenderButtons(vm = viewModel)
                }
                Spacer(modifier = Modifier.height(50.dp))
                Button(
                    onClick = {
                        viewModel.profileCompletePressed()
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
                        text = "ready",
                        color = Color.White,
                        style = MaterialTheme.typography.titleLarge,
                        fontSize = 20.sp
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
fun GenderButtons(vm: ProfileSetUpViewModel) {

    val selectedGender = vm.selectedGender
    val onOptionsSelected = { option: String -> vm.selectOption(option) }

    Row {
        vm.genderOptions.forEach { text ->
            Row(
                Modifier
                    .selectable(
                        selected = (text == selectedGender),
                        onClick = {
                            onOptionsSelected(text)
                        }
                    )
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = (text == selectedGender ),
                    onClick = { onOptionsSelected(text) }
                )
                Text(
                    text = text,

                )
            }

        }
    }
}

@Composable
fun DatePickerButton(
    vm: ProfileSetUpViewModel,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .drawBehind {
                val y = size.height
                drawLine(
                    brush = SolidColor(Color(red = 19, green = 0, blue = 142)),
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 8f
                )
            }
    ) {
        Button(

            onClick = {
            onClick()
            },
            modifier = Modifier
                .width(200.dp)
                .height(40.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                contentColor = Color.White
            )
        ) {
            Text(
                text = vm.dobString.value ?: "",
                color = Color.Black,
                style = MaterialTheme.typography.titleLarge,
                fontSize = 20.sp
            )
        }
    }

}

@Composable
fun SmileyFace(vm: ProfileSetUpViewModel) {
    val imageSize = 180.dp
    Box(
        modifier = Modifier
            .size(imageSize)
            .clip(CircleShape)
            .background(Color.White)
    ) {
        if (vm.uri == null) {
            Icon(
                painter = painterResource(id = R.drawable.profile_set_up_smiley),
                contentDescription = "Smiley",
                tint = Color(red = 19, green = 0, blue = 142),
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Image(
                painter = rememberAsyncImagePainter(model = vm.uri),
                contentDescription = "Profile Image",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
fun ChoosePhotoButton(vm: ProfileSetUpViewModel) {

    val singlePhotoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = {
            vm.uri = it
            vm.getImageExtension()
        }
    )

    Box(
        modifier = Modifier
            .size(46.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
    IconButton(
        onClick = {
            singlePhotoPicker.launch(PickVisualMediaRequest(
                ActivityResultContracts.PickVisualMedia.ImageOnly
            ))
        },
        modifier = Modifier
            .width(46.dp)
            .height(46.dp)
            .offset(y = 23.dp),
        colors = IconButtonDefaults.iconButtonColors(
            containerColor = Color.Black,
            contentColor = Color.White
        )
    ) {
        Icon(
            painter = painterResource(id = R.drawable.camera_image),
            contentDescription = "camera",
            tint = Color.White
        )
    }
}
}