package com.example.chitchat.Activities

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.chitchat.MyUI.Screens.SingleStatusScreen
import com.example.chitchat.MyUtils.MyConstants
import com.example.chitchat.ui.theme.ChitChatTheme

class StatusActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

        val username = intent.getStringExtra(MyConstants.USERNAME)!!
        val imageUrl = intent.getStringExtra(MyConstants.IMAGE_URL)!!
        val statusList = intent.getStringArrayListExtra(MyConstants.STATUS_LIST)!!
        val timestampList = intent.getStringArrayListExtra(MyConstants.TIMESTAMP_LIST)!!

        setContent {
            ChitChatTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SingleStatusScreen(username, imageUrl, statusList, timestampList)
                }
            }
        }
    }
}
