package com.example.chitchat.MyUI.Screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.chitchat.Activities.StatusActivity
import com.example.chitchat.MyUI.components.commonImage
import com.example.chitchat.MyUtils.MyConstants
import com.example.chitchat.MyUtils.Utilities
import com.example.chitchat.R
import com.example.chitchat.State.UiState
import com.example.chitchat.model.User
import com.example.chitchat.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatusScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val user by viewModel.user.collectAsState()
    val statuses by viewModel.statuses.collectAsState()

    val myStatuses = statuses
        .filter {
            it.user.userId == user?.userId
        }
        .sortedByDescending {
            it.timeStamp
        }

    val othersStatuses = statuses.filter {
        it.user.userId != user?.userId
    }

    var imageUri by remember {
        mutableStateOf("")
    }

    var inProgress by remember {
        mutableStateOf(false)
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

    Scaffold(
        floatingActionButton = {
            fab() {
                imageUri = it?.toString() ?: ""
                viewModel.uploadStatus(Uri.parse(imageUri))
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (inProgress)
                CircularProgressIndicator()
            else {
                if (statuses.isEmpty())
                    Text(text = "No status available")
                else {
                    if (myStatuses.isNotEmpty()) {
                        //here create myuser status item
                        val myUserStatusImageUriList: List<Pair<User, List<Pair<String, String>>>> =
                            Utilities.getStatusStructure(myStatuses).toList()

                        val username = viewModel.user.value!!.username
                        val latestStatusImageUri = myUserStatusImageUriList[0].second.last().first

                        Spacer(modifier = Modifier.padding(top = 70.dp))
                        
                        statusItem("You", latestStatusImageUri) {
                            //start single status activity here, pass list of all statusImageUris for this user
                            val (statusImageUris, formattedTimestamps) = myUserStatusImageUriList[0].second.extractLists()

                            val intent = Intent(context, StatusActivity::class.java).apply {
                                putExtra(MyConstants.USERNAME, "You")//myUserStatusImageUriList[0].first.username)
                                putExtra(MyConstants.IMAGE_URL, myUserStatusImageUriList[0].first.imageUrl)
                                putExtra(MyConstants.STATUS_LIST, ArrayList(statusImageUris))
                                putExtra(MyConstants.TIMESTAMP_LIST, ArrayList(formattedTimestamps))
                            }
                            context.startActivity(intent)
                        }

                        Divider(
                            color = Color.Gray,
                            thickness = 1.dp,
                            modifier = Modifier.padding(vertical = 10.dp)
                        )
                    }

                    //display other user statuses
                    val otherUsersStatusImageList: List<Pair<User, List<Pair<String, String>>>> =
                        Utilities.getStatusStructure(othersStatuses).toList()

                    val paddingTop = if(myStatuses.isNotEmpty()) 20.dp else 70.dp

                    LazyColumn(
                        modifier = Modifier
                            .padding(top = paddingTop, bottom = 80.dp)
                            .weight(1f)
                    ) {
                        items(otherUsersStatusImageList) { pairItem ->
                            var userUpdated by remember {
                                mutableStateOf<User?>(null)
                            }

                            val usernameDefault = pairItem.first.username
                            val latestStatusImageUri = pairItem.second.last().first

                            LaunchedEffect(userUpdated) {
                                viewModel.getOtherUserData(pairItem.first.userId, {
                                    userUpdated = it
                                }, {
                                    userUpdated = null
                                })
                            }

                            statusItem(
                                username = userUpdated?.username ?: usernameDefault,
                                imageUrl = latestStatusImageUri
                            ) {
                                //start single status activity here, pass list of all statusImageUris for this user
                                val (statusImageUris, formattedTimestamps) = pairItem.second.extractLists()

                                val intent = Intent(context, StatusActivity::class.java).apply {
                                    putExtra(MyConstants.USERNAME, pairItem.first.username)
                                    putExtra(MyConstants.IMAGE_URL, pairItem.first.imageUrl)
                                    putExtra(MyConstants.STATUS_LIST, ArrayList(statusImageUris))
                                    putExtra(MyConstants.TIMESTAMP_LIST, ArrayList(formattedTimestamps))
                                }
                                context.startActivity(intent)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun fab(onGetResult: (Uri?) -> Unit) {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = {
            onGetResult.invoke(it)
        })

    FloatingActionButton(
        onClick = {
            launcher.launch("image/*")
        },
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        modifier = Modifier.padding(bottom = 80.dp)
    ) {
        Icon(
            imageVector = ImageVector.vectorResource(R.drawable.upload_icon),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun statusItem(username: String, imageUrl: String, onItemClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(82.dp)
            .clickable {
                onItemClick.invoke()
            },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
//        commonImage(
//            data = imageUrl,
//            modifier = Modifier
//                .padding(8.dp)
//                .size(50.dp)
//                .clip(CircleShape)
//                .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
//        )
        Box(
            modifier = Modifier
                .padding(8.dp)
                .size(54.dp)
                .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                .padding(4.dp)  // Space between border and image
        ) {
            commonImage(
                data = imageUrl,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
            )
        }

        Column(
            modifier = Modifier
                .padding(start = 10.dp)
                .fillMaxWidth()
                .weight(1f)
        ) {
            Text(
                text = username,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 5.dp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// Extract lists from List<Pair<String, String>>
fun List<Pair<String, String>>.extractLists(): Pair<List<String>, List<String>> {
    val firstList = this.map { it.first }
    val secondList = this.map { it.second }
    return Pair(firstList, secondList)
}


