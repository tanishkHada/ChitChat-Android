package com.example.chitchat.MyUI.Screens

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chitchat.Activities.ChatActivity
import com.example.chitchat.MyUI.components.commonImage
import com.example.chitchat.MyUtils.MyConstants
import com.example.chitchat.MyUtils.MyToast
import com.example.chitchat.MyUtils.Utilities
import com.example.chitchat.R
import com.example.chitchat.State.UiState
import com.example.chitchat.model.User
import com.example.chitchat.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatsScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val chatRooms by viewModel.chatRooms.collectAsState()
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

    var showDialog by remember {
        mutableStateOf(false)
    }

    Scaffold(
        floatingActionButton = {
            fab(context, showDialog, onFabClick = {
                showDialog = true
            }, onDismissDialog = {
                showDialog = false
            }, onClickAddChat = { partnerPhone ->
                viewModel.createChatRoom(partnerPhone)
                showDialog = false
            })
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (inProgress) {
                CircularProgressIndicator()
            } else {
                LazyColumn(
                    modifier = Modifier
                        .padding(top = 70.dp, bottom = 80.dp)
                        .weight(1f)
                ) {
                    items(chatRooms) { chatRoom ->
                        var chatUserUpdated by remember {
                            mutableStateOf<User?>(null)
                        }
                        val myUser: User
                        val chatUserDefault : User

                        if (chatRoom.user1.userId == viewModel.user.value?.userId) {
                            chatUserDefault = chatRoom.user2
                            myUser = chatRoom.user1
                        } else {
                            chatUserDefault = chatRoom.user1
                            myUser = chatRoom.user2
                        }

                        LaunchedEffect(chatUserUpdated) {
                            viewModel.getOtherUserData(chatUserDefault.userId, {
                                chatUserUpdated = it
                            }, {
                                chatUserUpdated = null
                            })
                        }

                        val timeDate = Utilities.formatTimestamp(chatRoom.lastMessageTimeStamp)

                        chatItem(
                            imageUrl = chatUserUpdated?.imageUrl ?: chatUserDefault.imageUrl,
                            username = chatUserUpdated?.username ?: chatUserDefault.username,
                            time = timeDate,
                            lastMessage = chatRoom.lastMessage,
                            myUserId = myUser.userId,
                            lastMessageSenderId = chatRoom.lastMessageSenderId
                        ) {
                            //start single chat activiy ans pass chatroom id or current chatroom object
                            val intent = Intent(context, ChatActivity::class.java).apply {
                                putExtra(MyConstants.MY_USER_ID, myUser.userId)
                                putExtra(MyConstants.PARTNER_ID, chatUserUpdated?.userId ?: chatUserDefault.userId)
                                putExtra(MyConstants.CHAT_ID, chatRoom.chatRoomId)
                                putExtra(MyConstants.USERNAME, chatUserUpdated?.username ?: chatUserDefault.username)
                                putExtra(MyConstants.IMAGE_URL, chatUserUpdated?.imageUrl ?: chatUserDefault.imageUrl)
                            }
                            context.startActivity(intent)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun fab(
    context: Context,
    showDialog: Boolean,
    onFabClick: () -> Unit,
    onDismissDialog: () -> Unit,
    onClickAddChat: (String) -> Unit
) {

    var phone by remember {
        mutableStateOf("")
    }

    if (showDialog) {
        AlertDialog(onDismissRequest = {
            onDismissDialog.invoke()
            phone = ""
        },
            confirmButton = {
                Button(onClick = {
                    if (phone.length != 10)
                        MyToast.showToast(context, "Enter phone with 10 digits")
                    else {
                        onClickAddChat(phone)
                        phone = ""
                    }
                }
                ) {
                    Text(text = "Add")
                }
            },
            title = {
                Text(text = "Add Chat")
            },
            text = {
                OutlinedTextField(
                    value = phone, onValueChange = {
                        phone = it
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    label = {
                        Text(text = "Enter Phone Number")
                    }
                )
            }
        )
    }

    FloatingActionButton(
        onClick = {
            onFabClick.invoke()
        },
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        modifier = Modifier.padding(bottom = 80.dp)
    ) {
        Icon(painter = painterResource(id = R.drawable.add_chat_icon), contentDescription = null, tint = MaterialTheme.colorScheme.primary)
    }
}

@Composable
fun chatItem(
    imageUrl: String,
    username: String,
    time: String,
    lastMessage: String,
    myUserId: String,
    lastMessageSenderId: String,
    onItemClick: () -> Unit
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .clickable {
                onItemClick.invoke()
            },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        commonImage(
            data = imageUrl,
            modifier = Modifier
                .padding(8.dp)
                .size(54.dp)
                .clip(CircleShape)
        )

        Column(
            modifier = Modifier
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
            Text(
                text = if (lastMessageSenderId == myUserId && lastMessage != "") "you : $lastMessage" else lastMessage,
                fontWeight = FontWeight.Normal,
                modifier = Modifier.padding(start = 5.dp, top = 4.dp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Text(
            text = time,
            fontSize = 12.sp,
            modifier = Modifier.padding(start = 2.dp, bottom = 15.dp, end = 15.dp)
        )
    }
}

