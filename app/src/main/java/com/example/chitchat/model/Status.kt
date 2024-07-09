package com.example.chitchat.model

import com.google.firebase.Timestamp

data class Status(
    var user : User = User(),
    var statusImageUri : String = "",
    var timeStamp : Timestamp = Timestamp.now()
)