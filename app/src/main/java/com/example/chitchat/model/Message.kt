package com.example.chitchat.model

import com.google.firebase.Timestamp

data class Message(
    var senderId : String = "",
    var message : String = "",
    var messageTimeStamp : Timestamp = Timestamp.now()
)