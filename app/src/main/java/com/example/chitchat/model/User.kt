package com.example.chitchat.model

import com.google.firebase.Timestamp

data class User(
    var userId : String = "",
    var username : String = "",
    var phone : String = "",
    var imageUrl : String = "",
    val createdTimeStamp : Timestamp = Timestamp.now()
)

