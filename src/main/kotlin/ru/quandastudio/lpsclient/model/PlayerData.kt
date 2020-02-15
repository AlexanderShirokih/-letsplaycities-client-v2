package ru.quandastudio.lpsclient.model

data class PlayerData(
    var authData: AuthData,
    var clientVersion: String = "unk",
    var clientBuild: Int = 270,
    var canReceiveMessages: Boolean = false,
    var allowSendUID: Boolean = false,
    var isFriend: Boolean = false,
    var pictureHash: String? = null
) {

    open class Factory {
        fun create(login: String): PlayerData {
            return PlayerData(AuthData.Factory().create(login))
        }
    }

}