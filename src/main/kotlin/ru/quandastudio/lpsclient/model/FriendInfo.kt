package ru.quandastudio.lpsclient.model

data class FriendInfo(
    var userId: Int,
    var login: String,
    var accepted: Boolean,
    @Deprecated(message = "Deprecated param, will be removed in future releases")
    val pictureHash: String?
)
