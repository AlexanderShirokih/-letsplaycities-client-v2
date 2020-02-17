package ru.quandastudio.lpsclient.model

data class Credentials(
    /** InGame userId */
    val userId: Int = 0,
    /** InGame hash */
    val hash: String = ""
) {

    fun isValid() = userId != 0 && hash.isNotEmpty()
}