package com.example.chitchat.Activities

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.chitchat.MyUI.Screens.SingleChatScreen
import com.example.chitchat.MyUtils.MyConstants
import com.example.chitchat.ui.theme.ChitChatTheme
import com.example.chitchat.viewmodel.ChatViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ChatActivity : ComponentActivity() {
    private val viewModel: ChatViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val myUserId = intent.getStringExtra(MyConstants.MY_USER_ID)
        val partnerId = intent.getStringExtra(MyConstants.PARTNER_ID)
        val chatRoomId = intent.getStringExtra(MyConstants.CHAT_ID)
        val username = intent.getStringExtra(MyConstants.USERNAME)
        val imageUrl = intent.getStringExtra(MyConstants.IMAGE_URL)

        setContent {
            ChitChatTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SingleChatScreen(
                        myUserId!!,
                        partnerId!!,
                        chatRoomId!!,
                        username!!,
                        imageUrl!!,
                        viewModel = viewModel
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        viewModel.cleanUp()
        super.onDestroy()
    }
}

