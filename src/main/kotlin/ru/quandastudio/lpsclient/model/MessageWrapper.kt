package ru.quandastudio.lpsclient.model

import io.reactivex.Single
import ru.quandastudio.lpsclient.LPSException

class MessageWrapper<T>(val data: T?, val error: String?) {

    fun toSingle(): Single<T> = if (error != null) Single.error(LPSException(error)) else Single.just(data!!)

}