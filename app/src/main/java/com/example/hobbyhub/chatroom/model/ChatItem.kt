package com.example.hobbyhub.chatroom.model

sealed class ChatItem {
    data class FriendItem(val friend: Friend) : ChatItem()
    data class GroupItem(val group: Group) : ChatItem()
}
