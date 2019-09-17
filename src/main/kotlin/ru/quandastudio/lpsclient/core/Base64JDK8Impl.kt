package ru.quandastudio.lpsclient.core

import java.util.Base64

/**
 * Provides Base64 encoding/decoding using Java SDK
 */
class Base64JDK8Impl : Base64Provider {

    override fun decode(data: String): ByteArray = Base64.getDecoder().decode(data)

    override fun encode(data: ByteArray): String = Base64.getEncoder().encodeToString(data)

}