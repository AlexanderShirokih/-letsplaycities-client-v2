package ru.quandastudio.lpsclient.model

data class PlayerData(
    val authData: AuthData,
    val versionInfo: VersionInfo,
    val canReceiveMessages: Boolean = false,
    val isFriend: Boolean = false,
    var pictureHash: String? = null
)