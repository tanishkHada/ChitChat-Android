package com.example.chitchat.Activities

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.chitchat.MyUI.Screens.AuthScreen
import com.example.chitchat.ui.theme.ChitChatTheme
import com.example.chitchat.viewmodel.AuthViewModel
import com.google.firebase.auth.PhoneAuthProvider
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AuthenticationActivity : ComponentActivity() {
    private val viewmodel : AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ChitChatTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AuthScreen(viewmodel)
                }
            }
        }
    }

    fun onClickConfirmSignIn(enteredOTP : String){
        if(viewmodel.verificationCode == "")
            return

        val credential = PhoneAuthProvider.getCredential(viewmodel.verificationCode, enteredOTP)
        viewmodel.signInUser(credential)
    }
}
