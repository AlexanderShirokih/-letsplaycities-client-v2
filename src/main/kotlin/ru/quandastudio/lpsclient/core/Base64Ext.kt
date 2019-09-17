package ru.quandastudio.lpsclient.core

import java.util.*

object Base64Ext {

    fun String.decodeBase64(): ByteArray = Base64.getDecoder().decode(this)

    fun ByteArray.encodeBase64(): String = Base64.getEncoder().encodeToString(this)

}