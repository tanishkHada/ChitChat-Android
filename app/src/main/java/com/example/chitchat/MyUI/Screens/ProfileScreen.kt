package com.example.chitchat.MyUI.Screens

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.chitchat.Activities.AuthenticationActivity
import com.example.chitchat.MyUI.components.commonImage
import com.example.chitchat.State.UiState
import com.example.chitchat.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    val user by viewModel.user.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    var inProgress by remember {
        mutableStateOf(false)
    }

    var username by remember {
        mutableStateOf(user?.username ?: "")
    }

    var imageUri by remember {
        mutableStateOf("")
    }

    when (uiState) {
        is UiState.Loading -> {
            inProgress = true
        }

        is UiState.Success -> {
            inProgress = false
            (uiState as UiState.Success).message?.let {
                //MyToast.showToast(context, it)
            }
        }

        is UiState.Error -> {
            inProgress = false
            (uiState as UiState.Error).message?.let {
                //MyToast.showToast(context, it)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        if (inProgress)
            CircularProgressIndicator()
        else {
            profileImage(user?.imageUrl ?: "", imageUri) {
                imageUri = it?.toString() ?: ""
            }

            OutlinedTextField(
                value = username,
                onValueChange = {
                    username = it
                },
                label = { Text(text = "Username") },
                modifier = Modifier.padding(top = 20.dp)
            )

            OutlinedTextField(
                value = user?.phone ?: "",
                onValueChange = {},
                label = { Text(text = "Phone") },
                readOnly = true,
                modifier = Modifier.padding(top = 20.dp)
            )

            Button(
                onClick = {
                          viewModel.updateProfile(username, Uri.parse(imageUri))
                },
                modifier = Modifier.padding(top = 30.dp)
            ) {
                Text(text = "Update Profile")
            }

            Button(
                onClick = {
                    viewModel.signOutUser()
                    context.startActivity(Intent(context, AuthenticationActivity::class.java))
                    (context as Activity).finish()
                },
                modifier = Modifier.padding(top = 30.dp)
            ) {
                Text(text = "Log out")
            }
        }
    }
}

@Composable
fun profileImage(userImageUri : String, localImageUri: String, onGetResult: (Uri?) -> Unit) {

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = {
            onGetResult.invoke(it)
        })

    Box(modifier = Modifier.height(IntrinsicSize.Min)) {
        Column(
            modifier = Modifier
                .wrapContentWidth()
                .padding(10.dp)
                .clickable {
                    launcher.launch("image/*")
                },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                shape = CircleShape,
                modifier = Modifier
                    .padding(8.dp)
                    .size(200.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                commonImage(
                    data = if(localImageUri != "") localImageUri else userImageUri,
                    modifier = Modifier
                        .size(200.dp)
                        .clip(CircleShape)
                )
                Text(text = "Change profile picture")
            }
        }
    }
}