package ru.quandastudio.lpsclient.model

import ru.quandastudio.lpsclient.LPSException

class MessageWrapper<T>(val data: T?, val error: String?) {

    fun requireData(): T = if (error != null) throw LPSException(error) else data!!

}