package ru.quandastudio.lpsclient.model

data class FriendInfo(
    var userId: Int,
    var login: String,
    var accepted: Boolean,
    val pictureHash: String?
)
