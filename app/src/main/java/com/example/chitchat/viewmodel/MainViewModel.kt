package com.example.chitchat.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import com.example.chitchat.MyUtils.MyConstants
import com.example.chitchat.State.UiState
import com.example.chitchat.model.ChatRoom
import com.example.chitchat.model.Status
import com.example.chitchat.model.User
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Filter
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.toObject
import com.google.firebase.firestore.toObjects
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.Date
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    val auth: FirebaseAuth,
    val db: FirebaseFirestore,
    val storage: FirebaseStorage
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState>(UiState.Success())
    val uiState: StateFlow<UiState> = _uiState

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user

    private val _chatRooms = MutableStateFlow<List<ChatRoom>>(listOf())
    val chatRooms: StateFlow<List<ChatRoom>> = _chatRooms

    private val _statuses = MutableStateFlow<List<Status>>(listOf())
    val statuses: StateFlow<List<Status>> = _statuses

    private var userListener: ListenerRegistration? = null
    private var chatRoomsListener: ListenerRegistration? = null
    private var chatRoomsListenerSecond: ListenerRegistration? = null
    private var statusListener: ListenerRegistration? = null

    init {
        getUserData()
    }

    private fun getUserData() {
        _uiState.value = UiState.Loading

        userListener = db.collection(MyConstants.USER_NODE).document(auth.uid!!)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    _uiState.value = UiState.Error(message = "Error getting user details")
                    return@addSnapshotListener
                }
                if (value != null) {
                    _user.value = value.toObject<User>()
                    _uiState.value = UiState.Success()

                    //initiating fetching tasks
                    getChatRooms()
                    getStatuses()
                }
            }
    }

    fun createChatRoom(partnerPhone: String) {
        val _partnerPhone = "+91$partnerPhone"
        if (_partnerPhone == user.value?.phone)
            return

        _uiState.value = UiState.Loading

        db.collection(MyConstants.CHATROOM_NODE).where(
            Filter.or(
                Filter.and(
                    Filter.equalTo("user1.phone", _partnerPhone),
                    Filter.equalTo("user2.phone", user.value?.phone)
                ),
                Filter.and(
                    Filter.equalTo("user1.phone", user.value?.phone),
                    Filter.equalTo("user2.phone", _partnerPhone)
                )
            )
        ).get().addOnSuccessListener {
            //if it is empty means the chatroom doesn't exist, so we will create one, after checking if the partner exists
            if (it.isEmpty) {
                //but we also have to check the user exists with the number we have typed to add or not
                db.collection(MyConstants.USER_NODE).whereEqualTo("phone", _partnerPhone).get()
                    .addOnSuccessListener {
                        //if it is empty means there is no user with phone number -> number
                        if (it.isEmpty) {
                            _uiState.value = UiState.Error(message = "User doesn't exists")
                        } else {
                            //we can add this user as the new chat for our user
                            val chatPartner = it.toObjects<User>()[0]
                            val id = db.collection(MyConstants.CHATROOM_NODE).document().id
                            val chatRoom = ChatRoom(
                                id,
                                user.value!!,
                                User(
                                    chatPartner.userId,
                                    chatPartner.username,
                                    chatPartner.phone,
                                    chatPartner.imageUrl,
                                    chatPartner.createdTimeStamp
                                ),
                                "",
                                (user.value as User).userId,
                                Timestamp.now()
                            )

                            db.collection(MyConstants.CHATROOM_NODE).document(id).set(chatRoom)
                                .addOnCompleteListener {
                                    if (it.isSuccessful) {
                                        _uiState.value = UiState.Success(message = "User added")
                                    } else {
                                        _uiState.value =
                                            UiState.Error(message = "Error adding user")
                                    }
                                }
                        }
                    }
            } else {
                _uiState.value = UiState.Error(message = "User already added")
            }
        }
    }

    fun getChatRooms() {
        _uiState.value = UiState.Loading

        chatRoomsListener = db.collection(MyConstants.CHATROOM_NODE).where(
            Filter.or(
                Filter.equalTo("user1.userId", user.value?.userId),
                Filter.equalTo("user2.userId", user.value?.userId)
            )
        ).addSnapshotListener { value, error ->
            if (error != null) {
                _uiState.value = UiState.Error(message = "Error loading chats")
            }
            if (value != null) {
                _chatRooms.value = value.documents.mapNotNull {
                    it.toObject<ChatRoom>()
                }.sortedByDescending { it.lastMessageTimeStamp }

                _uiState.value = UiState.Success()
            }
        }
    }

    fun updateProfile(username: String, imageUri: Uri) {
        if (user.value?.username == username && (imageUri.toString() == "" || user.value?.imageUrl == imageUri.toString()))
            return

        //if imageUri is "", means only username is changed
        if (imageUri.toString() == "")
            updateUserProfile(username, user.value?.imageUrl ?: "")
        else {
            _uiState.value = UiState.Loading
            //first upload image to firebase storage, then get its url, then update profile
            uploadImage(imageUri, {
                updateUserProfile(username, it)
            }) {
                _uiState.value = UiState.Error(message = "Error updating")
            }
        }
    }

    private fun uploadImage(imageUri: Uri, onSuccess: (String) -> Unit, onFailure: () -> Unit) {
        val storageRef = storage.reference
        val uuid = UUID.randomUUID()
        val imageRef = storageRef.child("images/$uuid")
        val uploadTask = imageRef.putFile(imageUri)

        uploadTask.addOnSuccessListener {
            // Get the download URL and pass it to the onSuccess callback
            imageRef.downloadUrl.addOnSuccessListener { uri ->
                _uiState.value = UiState.Success()
                onSuccess(uri.toString())
            }
        }.addOnFailureListener { exception ->
            onFailure.invoke()
        }
    }

    private fun updateUserProfile(username: String, imageUrl: String) {
        _uiState.value = UiState.Loading

        val userRef = db.collection(MyConstants.USER_NODE).document(auth.uid!!)

        val updates = mapOf(
            "username" to username,
            "imageUrl" to imageUrl
        )

        userRef.update(updates)
            .addOnSuccessListener {
                _uiState.value = UiState.Success(message = "Profile updated")
            }
            .addOnFailureListener { e ->
                _uiState.value = UiState.Error(message = "Error updating profile")
            }
    }

    fun uploadStatus(imageUri: Uri) {
        if (imageUri.toString() == "")
            return

        //first upload image to storage, then upload status
        _uiState.value = UiState.Loading

        uploadImage(imageUri, {
            uploadUserStatus(it)
        }, {
            _uiState.value = UiState.Error(message = "Error uploading status")
        })
    }

    private fun uploadUserStatus(statusImageUrl: String) {
        val status = Status(
            user = user.value!!,
            statusImageUri = statusImageUrl,
            timeStamp = Timestamp.now()
        )

        db.collection(MyConstants.STATUS_NODE).document().set(status).addOnCompleteListener {
            if (it.isSuccessful)
                _uiState.value = UiState.Success()
            else
                _uiState.value = UiState.Error()
        }
    }

    fun getStatuses() {
        _uiState.value = UiState.Loading

        val timeDelta =
            24L * 60 * 60 * 1000 //only getting statuses which are uploaded in last 24 hours
        val statusCutOffMillis =
            System.currentTimeMillis() - timeDelta //fetch status whose timestamp is > than this
        val statusCutOff = Timestamp(Date(statusCutOffMillis))

        chatRoomsListenerSecond = db.collection(MyConstants.CHATROOM_NODE).where(
            Filter.or(
                Filter.equalTo("user1.userId", user.value?.userId),
                Filter.equalTo("user2.userId", user.value?.userId)
            )
        ).addSnapshotListener { value, error ->
            if (error != null) {
                _uiState.value = UiState.Error(message = "Error loading status")
            }
            //by this we got all the users which are partner of my user, now add all those users' userId's and use it to fetch
            //their status
            if (value != null) {
                val myAndPartnersStatuses = arrayListOf(user.value?.userId)

                val chats = value.toObjects<ChatRoom>()
                chats.forEach {
                    if (it.user1.userId == user.value?.userId)
                        myAndPartnersStatuses.add(it.user2.userId)
                    else
                        myAndPartnersStatuses.add(it.user1.userId)
                }
                //now fetch status
                statusListener = db.collection(MyConstants.STATUS_NODE)
                    .whereGreaterThan("timeStamp", statusCutOff)
                    .whereIn("user.userId", myAndPartnersStatuses)
                    .addSnapshotListener { value, error ->
                        if (error != null) {
                            _uiState.value = UiState.Error(message = "Error loading status")
                        }

                        if (value != null) {
                            _statuses.value = value.toObjects<Status>()
                            _uiState.value = UiState.Success()
                        }
                    }
            }
        }
    }

    fun getOtherUserData(otherUserId: String, onSuccess: (User) -> Unit, onFailure: () -> Unit) {
        db.collection(MyConstants.USER_NODE).document(otherUserId)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    _uiState.value = UiState.Error(message = "Error getting user details")
                    onFailure.invoke()
                }
                if (value != null) {
                    val otherUser = value.toObject<User>()

                    if (otherUser != null) {
                        _uiState.value = UiState.Success()
                        onSuccess.invoke(otherUser)
                    } else {
                        _uiState.value = UiState.Error()
                        onFailure.invoke()
                    }
                }
            }
    }

    fun signOutUser() {
        auth.signOut()
    }

    fun cleanUp() {
        userListener = null
        chatRoomsListener = null
        chatRoomsListenerSecond = null
        statusListener = null
    }
}
