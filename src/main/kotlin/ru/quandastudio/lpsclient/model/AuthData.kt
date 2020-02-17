package ru.quandastudio.lpsclient.model

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

    fun getCredentials() = Credentials(userID, accessHash)

    interface SaveProvider {
        fun save(authData: AuthData)
        fun load(): AuthData
    }
}