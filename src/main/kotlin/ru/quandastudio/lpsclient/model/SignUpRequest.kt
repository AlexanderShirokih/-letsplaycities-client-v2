package ru.quandastudio.lpsclient.model

data class SignUpRequest(
    /** User name */
    val login: String,
    /** Social newtork type */
    val authType: AuthType,
    /** Firebase token */
    val firebaseToken: String,
    /** Social network access token */
    val accToken: String,
    /** Social network user ID */
    val snUID: String
) {
    /** Protocol version */
    val version = 5
}