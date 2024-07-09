package com.example.chitchat.viewmodel

import android.app.Activity
import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.chitchat.MyUtils.MyConstants
import com.example.chitchat.State.SuccessStatus
import com.example.chitchat.State.UiState
import com.example.chitchat.model.User
import com.google.firebase.FirebaseException
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    val auth: FirebaseAuth,
    val db: FirebaseFirestore
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState>(UiState.Success())
    val uiState : StateFlow<UiState> = _uiState

    var verificationCode: String = ""
    private var resendingToken: PhoneAuthProvider.ForceResendingToken? = null

    fun sendOTP(activity: Activity, phone: String, isResend: Boolean) {
        _uiState.value = UiState.Loading

        val phoneNumber = "+91$phone" // Replace with the user's phone number
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout for the code sent via SMS
            .setActivity(activity) // The activity to which the user is navigated to enter the code
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    // Auto-retrieval or instant verification has succeeded
                    // You can sign in the user here if needed

                    signInUser(credential)
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    // Verification failed
                    Log.d("myTag", e.message.toString())
                    _uiState.value = UiState.Error(message = "Verification failed")
                }

                override fun onCodeSent(
                    verificationId: String,
                    token: PhoneAuthProvider.ForceResendingToken
                ) {
                    // The SMS verification code has been sent to the provided phone number
                    // Save the verification ID and the token to use later
                    _uiState.value = UiState.Success(message = "OTP sent", status = SuccessStatus.OTP_SENT)
                    verificationCode = verificationId
                    resendingToken = token
                }
            }) // Implement PhoneAuthProvider.OnVerificationStateChangedCallbacks

        if (isResend && _uiState.value != UiState.Loading && resendingToken != null)
            PhoneAuthProvider.verifyPhoneNumber(
                options.setForceResendingToken(resendingToken!!).build()
            )
        else
            PhoneAuthProvider.verifyPhoneNumber(options.build())
    }

    fun signInUser(credential: PhoneAuthCredential) {
        _uiState.value = UiState.Loading

        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Sign in success, you can access the authenticated user via task.result.user
                    val user = task.result?.user
                    if(user != null)
                        checkUserExists(user)
                    else{
                        _uiState.value = UiState.Error(message = "Error signing in")
                        signOutUser()
                    }
                } else {
                    // Sign in failed
                    _uiState.value = UiState.Error(message = "Error signing in")
                }
            }
    }

    private fun checkUserExists(user : FirebaseUser){
        val userRef = db.collection(MyConstants.USER_NODE).document(user.uid)
        userRef.get()
            .addOnSuccessListener {document->
                if(document.exists()){
                    _uiState.value = UiState.Success(message = "Sign in successful", status = SuccessStatus.SIGN_IN_SUCCESSFUL)
                }else{
                    createUser(user)
                }
            }
            .addOnFailureListener{
                _uiState.value = UiState.Error(message = "Error signing in")
                signOutUser()
            }
    }

    private fun createUser(user : FirebaseUser){
        val saveUser = User(
            userId = user.uid,
            username = user.phoneNumber!!,
            phone = user.phoneNumber!!,
            imageUrl = "",
            createdTimeStamp = Timestamp.now()
        )

        db.collection(MyConstants.USER_NODE).document(user.uid).set(saveUser)
            .addOnSuccessListener {
                _uiState.value = UiState.Success(message = "Sign in successful", status = SuccessStatus.SIGN_IN_SUCCESSFUL)
            }
            .addOnFailureListener{
                _uiState.value = UiState.Error(message = "Error signing in")
                signOutUser()
            }
    }

    private fun signOutUser() {
        auth.signOut()
    }
}