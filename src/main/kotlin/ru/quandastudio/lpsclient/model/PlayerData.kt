package ru.quandastudio.lpsclient.model

data class PlayerData(
    var authData: AuthData,
    var clientVersion: String = "unk",
    var clientBuild: Int = 80,
    var canReceiveMessages: Boolean = false,
    var allowSendUID: Boolean = false,
    var isFriend: Boolean = false,
    var avatar: ByteArray? = null
) {

    override fun toString(): String {
        return "PlayerData{" +
                "clientVersion='$clientVersion', " +
                "clientBuild=$clientBuild, " +
                "canReceiveMessages=$canReceiveMessages, " +
                "allowSendUID=$allowSendUID, " +
                "avatar=${(if (avatar == null) "no" else "yes")}, " +
                "authData=$authData}"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PlayerData

        if (authData != other.authData) return false
        if (clientVersion != other.clientVersion) return false
        if (clientBuild != other.clientBuild) return false
        if (canReceiveMessages != other.canReceiveMessages) return false
        if (allowSendUID != other.allowSendUID) return false
        if (isFriend != other.isFriend) return false
        if (avatar != null) {
            if (other.avatar == null) return false
        } else if (other.avatar != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = authData.hashCode()
        result = 31 * result + clientVersion.hashCode()
        result = 31 * result + clientBuild
        result = 31 * result + canReceiveMessages.hashCode()
        result = 31 * result + allowSendUID.hashCode()
        result = 31 * result + isFriend.hashCode()
        result = 31 * result + (avatar?.contentHashCode() ?: 0)
        return result
    }

    open class Factory {
        fun create(login: String): PlayerData {
            return PlayerData(AuthData.Factory().create(login))
        }
    }

}