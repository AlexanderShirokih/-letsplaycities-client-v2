package ru.quandastudio.lpsclient.socket

import io.reactivex.Observable
import io.reactivex.Observer
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Socket

class SocketObservable(private val host: String, private val port: Int) : Observable<SocketObservable.StatefulData>() {

    enum class State { CONNECTED, DATA, DISCONNECTED }

    class StatefulData(private val observable: SocketObservable, val state: State, val data: CharArray) {
        fun sendResponse(data: CharArray) = observable.sendData(data)
        val isData = state == State.DATA
    }

    private inner class SocketObserver : ThreadObserver {
        override fun isDisposed(): Boolean = mSocket.isClosed && !receiverThread.isAlive && !senderThread.isAlive

        override fun dispose() {
            println("DISPOSING!")
            receiverThread.interrupt()
            senderThread.interrupt()
            mSocket.close()
            observer?.onNext(
                StatefulData(
                    this@SocketObservable,
                    State.DISCONNECTED,
                    charArrayOf()
                )
            )
        }

        override fun onNext(data: CharArray) {
            println("ON next! hasObserver=${observer != null}")
            observer?.onNext(
                StatefulData(
                    this@SocketObservable,
                    State.DATA,
                    data
                )
            )
        }

        override fun onError(e: Exception) {
            observer?.onError(e)
        }
    }

    private val mSocket = Socket()
    private val threadObserver = SocketObserver()
    private val receiverThread = ReceiverThread(mSocket, threadObserver)
    private val senderThread = SenderThread(mSocket, threadObserver)
    private var observer: Observer<in StatefulData>? = null

    override fun subscribeActual(observer: Observer<in StatefulData>) {
        this.observer = observer
        observer.onSubscribe(threadObserver)
        println("Subscribe!")

        try {
            val timeout = 120 * 1000;
            mSocket.connect(InetSocketAddress(host, port), timeout)
            mSocket.soTimeout = timeout
            println("Connected!")

            receiverThread.start()
            senderThread.start()

            observer.onNext(
                StatefulData(
                    this,
                    State.CONNECTED,
                    charArrayOf()
                )
            )
        } catch (e: IOException) {
            println("We got an error: $e")
            observer.onNext(
                StatefulData(
                    this,
                    State.DISCONNECTED,
                    charArrayOf()
                )
            )
            observer.onError(e)
        }
    }

    fun sendData(data: CharArray) {
        println("sendData: ${data.contentToString()}")
        if (mSocket.isConnected)
            senderThread.send(data)
    }

    fun isConnected() = !threadObserver.isDisposed

    fun dispose() = threadObserver.dispose()
}