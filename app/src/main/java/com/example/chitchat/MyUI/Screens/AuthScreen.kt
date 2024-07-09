package com.example.chitchat.MyUI.Screens

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.chitchat.Activities.AuthenticationActivity
import com.example.chitchat.Activities.MainActivity
import com.example.chitchat.MyUtils.MyToast
import com.example.chitchat.R
import com.example.chitchat.State.SuccessStatus
import com.example.chitchat.State.UiState
import com.example.chitchat.ui.theme.Purple80
import com.example.chitchat.viewmodel.AuthViewModel
import kotlinx.coroutines.delay

enum class Routes {
    PhoneAuthScreen,
    OTPScreen
}

@Composable
fun AuthScreen(viewModel: AuthViewModel) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
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
                MyToast.showToast(context, it)
            }
            (uiState as UiState.Success).status?.let {
                if(it == SuccessStatus.SIGN_IN_SUCCESSFUL) {
                    context.startActivity(Intent(context, MainActivity::class.java))
                    (context as Activity).finish()
                }
            }
        }

        is UiState.Error -> {
            inProgress = false
            (uiState as UiState.Error).message?.let {
                MyToast.showToast(context, it)
            }
        }
    }

    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Routes.PhoneAuthScreen.name) {
        composable(Routes.PhoneAuthScreen.name) {
            phoneAuthScreen(context) { phone ->
                navController.navigate("${Routes.OTPScreen.name}/$phone")
                viewModel.sendOTP(context as Activity, phone, false)
            }
        }
        composable("${Routes.OTPScreen.name}/{phone}") { backStackEntry ->
            val phone = backStackEntry.arguments?.getString("phone") ?: ""
            OTPScreen(inProgress, context, phone, { otp ->
                // Callbacks or actions for OTP screen
                (context as AuthenticationActivity).onClickConfirmSignIn(otp)
            }, { phone ->
                viewModel.sendOTP(context as Activity, phone, true)
            })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun phoneAuthScreen(context: Context, onClickSendOtp: (String) -> Unit) {
    var phone by remember {
        mutableStateOf("")
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(painter = painterResource(id = R.drawable.chitchat_icon), contentDescription = null,
            modifier = Modifier.size(200.dp).padding(20.dp), contentScale = ContentScale.FillBounds)

        OutlinedTextField(
            value = phone,
            onValueChange = {
                phone = it
            },
            label = {
                Text(text = "Phone")
            },
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Phone),
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .padding(horizontal = 40.dp),
            maxLines = 1,
            placeholder = {
                Text(text = "+91")
            }
        )

        Button(
            onClick = {
                if (phone.length != 10)
                    MyToast.showToast(context, "Enter phone with 10 digits")
                else
                    onClickSendOtp(phone)
            },
            modifier = Modifier
                .width(140.dp)
                .padding(top = 30.dp)
        ) {
            Text(text = "SEND OTP")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OTPScreen(
    inProgress: Boolean,
    context: Context,
    phone: String,
    onClickConfirm: (String) -> Unit,
    onResendOtp: (String) -> Unit
) {
    var otp by remember {
        mutableStateOf("")
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(painter = painterResource(id = R.drawable.chitchat_icon), contentDescription = null,
            modifier = Modifier.size(200.dp).padding(20.dp), contentScale = ContentScale.FillBounds)

        OutlinedTextField(
            value = otp,
            onValueChange = {
                otp = it
            },
            label = {
                Text(text = "Enter OTP")
            },
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Phone),
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .padding(horizontal = 40.dp),
            maxLines = 1
        )

        if (!inProgress) {
            Button(
                onClick = {
                    if (otp.length != 6)
                        MyToast.showToast(context, "OTP should of 6 digits")
                    else
                        onClickConfirm(otp)
                },
                modifier = Modifier
                    .width(140.dp)
                    .padding(top = 30.dp)
            )
            {
                Text(text = "CONFIRM")
            }
        }

        if (inProgress) {
            CircularProgressIndicator(
                modifier = Modifier.padding(top = 30.dp)
            )
        }

        resendTimerText(phone, onResendOtp)
    }
}

@Composable
fun resendTimerText(phone : String, onClickResend : (String) -> Unit) {
    var countDownSeconds by remember {
        mutableStateOf(30)
    }
    var isClickable by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(isClickable) {
        while (countDownSeconds > 0) {
            delay(1000)
            countDownSeconds--
        }
        isClickable = true
    }

    Text(
        text = if (isClickable) "Resend" else "Resend $countDownSeconds",
        color = if (isClickable) Purple80 else Color.Gray,
        modifier = Modifier
            .padding(top = 30.dp)
            .clickable {
                if (isClickable) {
                    countDownSeconds = 30
                    isClickable = false
                    //trigger resend action here
                    onClickResend(phone)
                }
            }
    )
}