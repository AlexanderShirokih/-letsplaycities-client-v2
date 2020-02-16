package ru.quandastudio.lpsclient.model

import ru.quandastudio.lpsclient.model.util.Utils

data class AuthData(
    /** User name */
    val login: String,
    /** Social network type */
    val snType: AuthType,
    /** InGame userId */
    val userID: Int,
    /** InGame hash */
    val accessHash: String
) {

    val hash: String
        get() = Utils.md5("$login,$userID,$accessHash,")

    fun save(saveProvider: SaveProvider) = saveProvider.save(this)

//    open class Factory {
//        fun create(name: String): AuthData {
//            return AuthData(name, "", AuthType.Native, "")
//        }
//    }

    interface SaveProvider {
        fun save(authData: AuthData)
    }
}