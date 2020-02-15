package ru.quandastudio.lpsclient.model

import ru.quandastudio.lpsclient.model.util.Utils

data class AuthData(
    /** User name */
    var login: String,
    /** Social network ID */
    var snUID: String,
    /** Social network type */
    var snType: AuthType,
    /** Social network's token */
    var accessToken: String,
    /** InGame userId */
    var userID: Int = 0,
    /** InGame hash */
    var accessHash: String? = "--no hash--"
) {

    val hash: String
        get() = Utils.md5("$login,$snUID,$snType,")

    fun save(saveProvider: SaveProvider) = saveProvider.save(this)

    open class Factory {
        fun create(name: String): AuthData {
            return AuthData(name, "", AuthType.Native, "")
        }
    }

    interface SaveProvider {
        fun save(authData: AuthData)
    }
}