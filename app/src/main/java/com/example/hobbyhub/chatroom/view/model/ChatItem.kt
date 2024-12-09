package com.example.hobbyhub.chatroom.view.model

import com.example.hobbyhub.chatroom.model.Friend

sealed class ChatItem {
    data class FriendItem(val friend: Friend) : ChatItem()
    data class GroupItem(val group: Group) : ChatItem()
}
