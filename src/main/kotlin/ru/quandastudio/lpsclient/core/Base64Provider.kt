package ru.quandastudio.lpsclient.core

/**
 * Interface that provides base64 functions.
 * Standard implementation is Base65JDK8Impl.
 * We need to separate implementation because
 * standard impl may be unavailable on some Android devices.
 */
interface Base64Provider {

    fun encode(data: ByteArray): String

    fun decode(data: String): ByteArray
}