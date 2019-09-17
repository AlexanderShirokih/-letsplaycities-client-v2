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
    }

    private inner class SocketObserver : ThreadObserver {
        override fun isDisposed(): Boolean = mSocket.isClosed && !receiverThread.isAlive && !senderThread.isAlive

        override fun dispose() {
            println("Disposing!")

            if (!receiverThread.isInterrupted)
                receiverThread.interrupt()
            if (!senderThread.isInterrupted)
                senderThread.interrupt()
            if (!mSocket.isClosed)
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
            if (data.isEmpty())
                dispose()
            else
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

    private lateinit var mSocket: Socket
    private lateinit var receiverThread: ReceiverThread
    private lateinit var senderThread: SenderThread
    private val threadObserver = SocketObserver()
    private var observer: Observer<in StatefulData>? = null

    init {
        init()
    }

    private fun init() {
        mSocket = Socket()
        receiverThread = ReceiverThread(mSocket, threadObserver)
        senderThread = SenderThread(mSocket, threadObserver)
    }

    override fun subscribeActual(observer: Observer<in StatefulData>) {
        this.observer = observer
        observer.onSubscribe(threadObserver)
        println("Subscribe!")

        try {

            if (mSocket.isClosed) {
                if (!threadObserver.isDisposed)
                    threadObserver.dispose()
                init()
            }

            val timeout = 15 * 60 * 1000
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
        println("sendData: ${String(data)}")
        if (mSocket.isConnected)
            senderThread.send(data)
    }

    fun isConnected() = !threadObserver.isDisposed

    fun dispose() = threadObserver.dispose()
}