package ru.quandastudio.lpsclient.model

data class PlayerData(
    val authData: AuthData,
    var clientVersion: String = "unk",
    var clientBuild: Int = 270,
    var canReceiveMessages: Boolean = false,
    var isFriend: Boolean = false,
    var pictureHash: String? = null
) {

    open class Factory {
        fun create(login: String): PlayerData {
            return PlayerData(AuthData(login, AuthType.Native, 0, ""))
        }
    }

}