package ru.quandastudio.lpsclient

import java.lang.RuntimeException

open class LPSException(msg: String, val errType: LPSErrType = LPSErrType.DEFAULT) : RuntimeException(msg) {
    enum class LPSErrType {
        DEFAULT,
        CONNECTION_ERROR
    }
}