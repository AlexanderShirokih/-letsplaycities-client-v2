package ru.quandastudio.lpsclient.model

data class AuthData(
    /** User name */
    val login: String,
    /** Social network type */
    val snType: AuthType,
    /** UserId and access hash pair */
    val credentials: Credentials
) {

    interface SaveProvider {
        fun save(authData: AuthData)
        fun load(): AuthData
    }
}