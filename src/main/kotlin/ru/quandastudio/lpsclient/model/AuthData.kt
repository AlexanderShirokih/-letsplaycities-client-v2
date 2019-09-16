package ru.quandastudio.lpsclient.model

import ru.quandastudio.lpsclient.model.util.Utils

data class AuthData(
    var login: String,
    var snUID: String,
    var snType: AuthType,
    var accessToken: String,
    var userID: Int = 0,
    var accessHash: String? = "--no hash--"
) {

    val hash: String
        get() = Utils.md5("$login,$snUID,$snType,")

    override fun toString(): String {
        return "AuthData(login='$login', snUID='$snUID', snName='$snType', accessToken=$accessToken, accessHash=$accessHash, userID=$userID)"
    }

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