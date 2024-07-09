package com.example.chitchat.State

enum class SuccessStatus{
    OTP_SENT,
    SIGN_IN_SUCCESSFUL
}

sealed class UiState{
    data object Loading : UiState()
    data class Success(val message : String? = null, val status : SuccessStatus? = null) : UiState()
    data class Error(val exception : Exception? = null, val message : String? = null) : UiState()
}