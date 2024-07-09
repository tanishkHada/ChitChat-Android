package com.example.chitchat.model

import com.google.firebase.Timestamp

data class ChatRoom(
    var chatRoomId: String = "",
    var user1: User = User(),
    var user2: User = User(),
    var lastMessage : String = "",
    var lastMessageSenderId : String = "",
    var lastMessageTimeStamp : Timestamp = Timestamp.now()
)
