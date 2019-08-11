package ru.quandastudio.lpsclient.model.util

object Utils {

    fun md5(src: String): String {
        val md = java.security.MessageDigest.getInstance("MD5")
        val array = md.digest(src.toByteArray(charset("UTF-8")))
        val sb = StringBuilder()
        for (b in array) {
            sb.append(Integer.toHexString(b.toInt() and 0xFF or 0x100).substring(1, 3))
        }
        return sb.toString()
    }

}
