package ru.quandastudio.lpsclient.model

data class Credentials(val userId: Int = 0, val hash: String = "") {

    fun isValid() = userId != 0 && hash.isNotEmpty()
}