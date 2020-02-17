package ru.quandastudio.lpsclient.model

data class PlayerData(
    val authData: AuthData,
    val clientVersion: String = "unk",
    val clientBuild: Int = 270,
    val canReceiveMessages: Boolean = false,
    val isFriend: Boolean = false,
    var pictureHash: String? = null
) {

    open class SimpleFactory {
        fun create(login: String): PlayerData {
            return PlayerData(AuthData(login, AuthType.Native, Credentials()))
        }
    }

}