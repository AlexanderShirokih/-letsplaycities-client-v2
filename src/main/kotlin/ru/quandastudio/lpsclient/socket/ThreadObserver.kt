package ru.quandastudio.lpsclient.socket

import io.reactivex.disposables.Disposable

interface ThreadObserver : Disposable {

    fun onNext(data: CharArray)
    fun onError(e: Exception)
}