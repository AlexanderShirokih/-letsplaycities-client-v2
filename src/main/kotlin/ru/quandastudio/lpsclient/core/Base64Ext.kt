package ru.quandastudio.lpsclient.core

object Base64Ext {
    /**
     * Replace implementation for android.
     */
    private lateinit var base64: Base64Provider

    fun installBase64(base64Provider: Base64Provider) {
        base64 = base64Provider
    }

    fun String.decodeBase64(): ByteArray = base64.decode(this)

    fun ByteArray.encodeBase64(): String = base64.encode(this)

}