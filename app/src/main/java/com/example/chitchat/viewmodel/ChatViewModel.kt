package com.example.chitchat.viewmodel

import androidx.lifecycle.ViewModel
import com.example.chitchat.MyUtils.MyConstants
import com.example.chitchat.State.UiState
import com.example.chitchat.model.Message
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.toObject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    val auth: FirebaseAuth,
    val db: FirebaseFirestore
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState>(UiState.Success())
    val uiState: StateFlow<UiState> = _uiState

    private val _messages = MutableStateFlow<List<Message>>(listOf())
    val messages: StateFlow<List<Message>> = _messages

    private var messagesListener: ListenerRegistration? = null

    fun getChatMessages(chatRoomId: String) {
        _uiState.value = UiState.Loading

        messagesListener = db.collection(MyConstants.CHATROOM_NODE).document(chatRoomId)
            .collection(MyConstants.MESSAGES_NODE).addSnapshotListener { value, error ->
            if (error != null)
                _uiState.value = UiState.Error()
            if (value != null) {
                _messages.value = value.documents.mapNotNull {
                    it.toObject<Message>()
                }.sortedBy { it.messageTimeStamp }
                _uiState.value = UiState.Success()
            }
        }
    }

    fun sendReply(chatRoomId: String, myUserId: String, reply: String) {
        _uiState.value = UiState.Loading
        //first update chatroom, then from there update messages
        updateChatRoom(chatRoomId, myUserId, reply)
    }

    private fun updateChatRoom(chatRoomId: String, senderId: String, message: String) {
        val timestamp = Timestamp.now()

        // Create a map of the fields to be updated
        val updates = mapOf(
            "lastMessage" to message,
            "lastMessageSenderId" to senderId,
            "lastMessageTimeStamp" to timestamp
        )

        // Reference to the chat room document
        val chatRoomRef = db.collection(MyConstants.CHATROOM_NODE).document(chatRoomId)

        // Update the fields
        chatRoomRef.update(updates)
            .addOnSuccessListener {
                // Handle success
                _uiState.value = UiState.Success()
                sendMessage(chatRoomId, message, senderId, timestamp)
            }
            .addOnFailureListener { e ->
                // Handle failure
                _uiState.value = UiState.Error()
            }
    }

    private fun sendMessage(
        chatRoomId: String,
        message: String,
        senderId: String,
        timeStamp: Timestamp
    ) {
        val msg = Message(
            senderId,
            message,
            timeStamp
        )

        db.collection(MyConstants.CHATROOM_NODE).document(chatRoomId)
            .collection(MyConstants.MESSAGES_NODE).document().set(msg).addOnCompleteListener {
                if (it.isSuccessful)
                    _uiState.value = UiState.Success()
                else
                    _uiState.value = UiState.Error()
            }
    }

    fun cleanUp() {
        messagesListener = null
    }
}