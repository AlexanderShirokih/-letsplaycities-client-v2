package ru.quandastudio.lpsclient.model

data class SignUpResponse(
    /** InGame user ID */
    val userId: Int,
    /** InGame user access hash */
    val accHash: String,
    /** User name */
    val name: String,
    /** Account state (ex: banned,ready, admin) */
    val state: State,
    /** Social network type */
    val authType: AuthType,
    /** User's picture md5 hash (if picture present) */
    val picHash: String?
)