package ru.quandastudio.lpsclient.model

data class PlayerData(
    val authData: AuthData,
    val versionInfo: VersionInfo,
    val canReceiveMessages: Boolean = false,
    val isFriend: Boolean = false,
    var pictureHash: String? = null
) {

    open class SimpleFactory {
        fun create(login: String, versionInfo: VersionInfo): PlayerData {
            return PlayerData(AuthData(login, AuthType.Native, Credentials()), versionInfo)
        }
    }

}