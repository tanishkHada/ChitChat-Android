package com.example.chitchat.MyUI.Screens

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chitchat.MyUI.components.commonImage
import com.example.chitchat.R
import com.example.chitchat.State.UiState
import com.example.chitchat.ui.theme.Purple40
import com.example.chitchat.ui.theme.Purple80
import com.example.chitchat.ui.theme.PurpleGrey40
import com.example.chitchat.viewmodel.ChatViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SingleChatScreen(
    myUserId: String,
    partnerId: String,
    chatRoomId: String,
    username: String,
    imageUrl: String,
    viewModel: ChatViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val messages by viewModel.messages.collectAsState()

    val context = LocalContext.current

    var inProgress by remember {
        mutableStateOf(false)
    }

    var reply by remember {
        mutableStateOf("")
    }

    // Scroll to last item in LazyColumn when messages change
    val listState = rememberLazyListState()
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.scrollToItem(messages.size - 1)
        }
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

    val onClickSend = {
        if (reply.isNotEmpty() && reply.isNotBlank())
            viewModel.sendReply(chatRoomId, myUserId, reply)
        reply = ""
    }

    LaunchedEffect(Unit) {
        viewModel.getChatMessages(chatRoomId)
    }

    Scaffold(
        topBar = {
            ChatTopAppBar(
                onBackClick = { (context as Activity).finish() },
                profileImageUrl = imageUrl,
                username = username
            )
        },
        content = {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(it),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
//                if (inProgress) {
//                    CircularProgressIndicator(modifier = Modifier.weight(1f))
//                } else {
                LazyColumn(
                    modifier = Modifier
                        .padding(top = 20.dp)
                        .weight(1f),
                    state = listState
                ) {
                    items(messages) { msg ->
                        val alignment =
                            if (msg.senderId == myUserId) Alignment.End else Alignment.Start
                        val bgColor: Color =
                            if (msg.senderId == myUserId) Purple40 else PurpleGrey40
                        val leftPadding = if (msg.senderId == myUserId) 80.dp else 8.dp
                        val rightPadding = if (msg.senderId == myUserId) 8.dp else 80.dp

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    top = 8.dp,
                                    bottom = 8.dp,
                                    start = leftPadding,
                                    end = rightPadding
                                ),
                            horizontalAlignment = alignment
                        ) {
                            Text(
                                text = msg.message,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(15.dp))
                                    .background(bgColor)
                                    .padding(10.dp),
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                replyBox(
                    reply, {
                        reply = it
                    },
                    onClickSend
                )
            }

        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatTopAppBar(onBackClick: () -> Unit, profileImageUrl: String, username: String) {
    TopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                commonImage(
                    data = profileImageUrl,
                    modifier = Modifier
                        .padding(start = 8.dp, end = 5.dp)
                        .size(50.dp)
                        .clip(CircleShape)
                )

                Text(
                    text = username,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    painter = painterResource(id = R.drawable.back_icon),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        },
        colors = TopAppBarDefaults.mediumTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            actionIconContentColor = Color.White
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun replyBox(reply: String, onReplyChange: (String) -> Unit, onClickSend: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .wrapContentHeight(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = reply,
            onValueChange = onReplyChange,
            placeholder = {
                Text(text = "Message")
            },
            shape = RoundedCornerShape(30.dp),
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(30.dp))
        )

        Box(
            modifier = Modifier
                .padding(start = 8.dp)
                .size(56.dp)
                .clip(CircleShape)
                .background(PurpleGrey40),
            contentAlignment = Alignment.Center
        ) {
            IconButton(
                onClick = onClickSend,
                modifier = Modifier.size(64.dp)
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(id = R.drawable.send_icon),
                    contentDescription = "",
                    tint = Purple80
                )
            }
        }
    }
}
