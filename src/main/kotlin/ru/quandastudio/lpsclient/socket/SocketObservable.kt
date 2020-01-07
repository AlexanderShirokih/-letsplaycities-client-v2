package ru.quandastudio.lpsclient.socket

import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.disposables.Disposable

abstract class SocketObservable(private val host: String, private val port: Int) :
    Observable<SocketObservable.StatefulData>() {

    enum class State { CONNECTED, DATA, DISCONNECTED }

    class StatefulData(private val observable: SocketObservable, val state: State, val data: CharArray) {
        fun sendResponse(data: CharArray) = observable.sendData(data)
    }

    override fun subscribeActual(observer: Observer<in StatefulData>) = subscribe(host, port, observer)

    protected abstract fun subscribe(host: String, port: Int, observer: Observer<in StatefulData>)

    abstract fun sendData(data: CharArray)

    abstract fun getDisposableSubscriber(): Disposable

    fun isConnected(): Boolean = !getDisposableSubscriber().isDisposed

    fun disconnect() = getDisposableSubscriber().dispose()
}