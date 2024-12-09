package com.example.hobbyhub.chatroom.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.hobbyhub.chatroom.model.Friend
import com.example.hobbyhub.chatroom.model.Message
import com.example.hobbyhub.chatroom.view.model.Group
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.Blob
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore

class ChatViewModel : ViewModel() {

    private val col = Firebase.firestore.collection("chats")
    private val userCol = Firebase.firestore.collection("user")
    private val groupCol = Firebase.firestore.collection("groups")
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val currentUser: FirebaseUser? = auth.currentUser

    private val friends = MutableLiveData<List<Friend>>()
    private val messages = MutableLiveData<List<Message>>()
    private val groupMessages = MutableLiveData<List<Message>>()

    init {
        fetchFriends()
    }

    fun getFriends(): LiveData<List<Friend>> {
        return friends
    }

    fun getFriendSize(): Number {
        return friends.value?.size ?: 0
    }

    fun getMessagesWithFriend(friendId: String): LiveData<List<Message>> {
        loadMessages(friendId)
        return messages
    }

    private fun fetchFriends() {
        currentUser?.uid?.let { uid ->
            getUserDocumentReference(uid)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e("ChatViewModel", "Error fetching user data: $error")
                        return@addSnapshotListener
                    }

                    if (snapshot != null && snapshot.exists()) {
                        val friendsArray = snapshot["friends"] as? List<String> ?: emptyList()
                        fetchFriendDetailsWithLastMessage(friendsArray)
                    }
                }
        }
    }

    private fun fetchFriendDetailsWithLastMessage(friendUserIds: List<String>) {
        val friendList = mutableListOf<Friend>()

        for (friendUserId in friendUserIds) {
            getUserDocumentReference(friendUserId)
                .get()
                .addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.exists()) {
                        val name = documentSnapshot["name"] as? String ?: ""
                        val photo =
                            documentSnapshot["photo"] as? Blob ?: Blob.fromBytes(ByteArray(0))
                        fetchLastMessageTimestamp(friendUserId) { timestamp ->
                            val friend = Friend(friendUserId, name, photo, timestamp)
                            updateFriendList(friendList, friend)
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("ChatViewModel", "Error fetching friend details: $e")
                }
        }
    }

    private fun fetchLastMessageTimestamp(friendUserId: String, callback: (Long) -> Unit) {
        val chatRoomId = if (currentUser != null) {
            if (currentUser.uid < friendUserId) {
                "${currentUser.uid}_$friendUserId"
            } else {
                "${friendUserId}_${currentUser.uid}"
            }
        } else {
            return
        }

        col.document(chatRoomId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(1)
            .addSnapshotListener { querySnapshot, error ->
                if (error != null) {
                    Log.e("ChatViewModel", "Error fetching last message timestamp: $error")
                    callback(0L)
                    return@addSnapshotListener
                }

                if (querySnapshot != null && !querySnapshot.isEmpty) {
                    val lastMessage = querySnapshot.documents[0]
                    val timestamp = lastMessage.getLong("timestamp") ?: 0L
                    callback(timestamp)
                } else {
                    callback(0L) // No messages, return 0
                }
            }
    }

    private fun updateFriendList(friendList: MutableList<Friend>, updatedFriend: Friend) {
        val index = friendList.indexOfFirst { it.id == updatedFriend.id }
        if (index != -1) {
            friendList[index] = updatedFriend
        } else {
            friendList.add(updatedFriend)
        }

        // Only update LiveData if the friend list has changed
        val newFriendList = friendList.sortedByDescending { it.lastMessageTimestamp }
        if (friends.value != newFriendList) {
            friends.value = newFriendList
        }
    }

    private fun getUserDocumentReference(userDocumentId: String): DocumentReference {
        return userCol.document(userDocumentId)
    }

    private fun loadMessages(friendId: String) {
        val chatRoomId = if (currentUser != null) {
            if (currentUser.uid < friendId) {
                "${currentUser.uid}_$friendId"
            } else {
                "${friendId}_${currentUser.uid}"
            }
        } else {
            return
        }

        col.document(chatRoomId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { querySnapshot, error ->
                if (error != null) {
                    Log.e("ChatViewModel", "Error fetching messages: $error")
                    return@addSnapshotListener
                }

                val messageList = mutableListOf<Message>()
                querySnapshot?.documents?.forEach { document ->
                    val senderId = document.getString("senderId") ?: ""
                    val content = document.getString("content") ?: ""
                    val timestamp = document.getLong("timestamp") ?: 0
                    val type = document.getString("type") ?: "text" // Defaulting to "text"
                    val eventId = document.getString("eventId")
                    val eventDate = document.getString("eventDate")
                    val eventStartTime = document.getString("eventStartTime")
                    val eventEndTime = document.getString("eventEndTime")

                    Log.d(
                        "ChatViewModel",
                        "Loaded message: senderId=$senderId, type=$type, content=$content, eventId=$eventId"
                    )

                    val message = Message(
                        senderId,
                        content,
                        timestamp,
                        type,
                        eventId,
                        eventDate,
                        eventStartTime,
                        eventEndTime
                    )
                    messageList.add(message)
                }
                messages.value = messageList
            }
    }

    fun sendMessage(
        friendId: String,
        content: String,
        type: String = "text",
        eventId: String? = null,
        eventDate: String? = null,
        eventStartTime: String? = null,
        eventEndTime: String? = null
    ) {
        val chatRoomId = if (currentUser != null) {
            if (currentUser.uid < friendId) {
                "${currentUser.uid}_$friendId"
            } else {
                "${friendId}_${currentUser.uid}"
            }
        } else {
            return
        }

        currentUser?.uid?.let { uid ->
            val messageMap = hashMapOf(
                "senderId" to uid,
                "content" to content,
                "timestamp" to System.currentTimeMillis(),
                "type" to type
            )

            if (type == "event_invitation") {
                eventId?.let { messageMap["eventId"] = it }
                eventDate?.let { messageMap["eventDate"] = it }
                eventStartTime?.let { messageMap["eventStartTime"] = it }
                eventEndTime?.let { messageMap["eventEndTime"] = it }
            }

            col.document(chatRoomId)
                .collection("messages")
                .add(messageMap)
                .addOnSuccessListener {
                    Log.d("ChatViewModel", "Message sent successfully")
                }
                .addOnFailureListener { e ->
                    Log.e("ChatViewModel", "Error sending message: $e")
                }
        }
    }

    fun getTopTwoFriends(): Pair<Friend?, Friend?> {
        val friendList = friends.value ?: emptyList()

        // Check if there are at least two friends in the list
        return if (friendList.size >= 2) {
            Pair(friendList[0], friendList[1]) // Return the first two friends
        } else {
            // Return null for the second friend if there's only one friend or none
            Pair(friendList.getOrNull(0), null)
        }
    }

    fun getGroupMessages(groupId: String): LiveData<List<Message>> {
        loadGroupMessages(groupId)
        return groupMessages
    }

    private fun loadGroupMessages(groupId: String) {
        groupCol.document(groupId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { querySnapshot, error ->
                if (error != null) {
                    Log.e("ChatViewModel", "Error fetching group messages: $error")
                    return@addSnapshotListener
                }

                val messageList = mutableListOf<Message>()
                querySnapshot?.documents?.forEach { document ->
                    val senderId = document.getString("senderId") ?: ""
                    val content = document.getString("content") ?: ""
                    val timestamp = document.getLong("timestamp") ?: 0
                    val type = document.getString("type") ?: "text" // Defaulting to "text"

                    Log.d(
                        "ChatViewModel",
                        "Loaded group message: senderId=$senderId, type=$type, content=$content"
                    )

                    val message = Message(senderId, content, timestamp, type)
                    messageList.add(message)
                }
                groupMessages.value = messageList
            }
    }

    // Send a message to a group
    fun sendGroupMessage(groupId: String, content: String, type: String = "text") {
        currentUser?.uid?.let { uid ->
            val messageMap = hashMapOf(
                "senderId" to uid,
                "content" to content,
                "timestamp" to System.currentTimeMillis(),
                "type" to type
            )

            groupCol.document(groupId)
                .collection("messages")
                .add(messageMap)
                .addOnSuccessListener {
                    Log.d("ChatViewModel", "Group message sent successfully")
                }
                .addOnFailureListener { e ->
                    Log.e("ChatViewModel", "Error sending group message: $e")
                }
        }
    }

    // Create a new group
    fun createGroup(name: String, members: List<String>) {
        val groupId = groupCol.document().id // Automatically generate a new unique group ID
        val groupData = hashMapOf(
            "name" to name,
            "members" to members,
            "adminId" to currentUser?.uid, // Set the current user as the admin
            "createdAt" to System.currentTimeMillis()
        )

        groupCol.document(groupId)
            .set(groupData)
            .addOnSuccessListener {
                Log.d("ChatViewModel", "Group created successfully")
            }
            .addOnFailureListener { e ->
                Log.e("ChatViewModel", "Error creating group: $e")
            }
    }

    fun getGroups(): LiveData<List<Group>> {
        val groupsLiveData = MutableLiveData<List<Group>>()

        currentUser?.uid?.let { uid ->
            groupCol
                .whereArrayContains(
                    "members",
                    uid
                )  // Check if the current user is a member of the group
                .addSnapshotListener { querySnapshot, error ->
                    if (error != null) {
                        Log.e("ChatViewModel", "Error fetching groups: $error")
                        return@addSnapshotListener
                    }

                    val groupsList = mutableListOf<Group>()
                    querySnapshot?.documents?.forEach { document ->
                        val groupId = document.id
                        val groupName = document.getString("name") ?: ""
                        val members = document["members"] as? List<String> ?: emptyList()
                        val adminId = document.getString("adminId") ?: ""
                        val createdAt = document.getLong("createdAt") ?: 0L

                        val group = Group(groupId, groupName, members, adminId, createdAt)
                        groupsList.add(group)
                    }
                    groupsLiveData.value = groupsList
                }
        }
        return groupsLiveData
    }

}