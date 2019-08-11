package ru.quandastudio.lpsclient.model

class PlayerData(var authData: AuthData) {
    var clientVersion: String = "unk"
    var clientBuild: Int = 80
    var canReceiveMessages: Boolean = false
    var allowSendUID: Boolean = false
    var isFriend: Boolean = false
    var avatar: ByteArray? = null

    override fun toString(): String {
        return "PlayerData{" +
                "clientVersion='$clientVersion', " +
                "clientBuild=$clientBuild, " +
                "canReceiveMessages=$canReceiveMessages, " +
                "allowSendUID=$allowSendUID, " +
                "avatar=${(if (avatar == null) "no" else "yes")}, " +
                "authData=$authData}"
    }

    open class Factory {
        fun create(login: String): PlayerData {
            return PlayerData(AuthData.Factory().create(login))
        }
    }

}