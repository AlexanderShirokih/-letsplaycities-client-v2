package ru.quandastudio.lpsclient.socket

import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Socket

class PureSocketObservable(host: String, port: Int = 62964) : SocketObservable(host, port) {

    private inner class SocketObserver : ThreadObserver {
        override fun isDisposed(): Boolean = mSocket.isClosed && !receiverThread.isAlive && !senderThread.isAlive

        override fun dispose() {
            if (!receiverThread.isInterrupted)
                receiverThread.interrupt()
            if (!senderThread.isInterrupted)
                senderThread.interrupt()
            if (!mSocket.isClosed)
                mSocket.close()
            observer?.onNext(
                StatefulData(
                    this@PureSocketObservable,
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
                        this@PureSocketObservable,
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

    override fun subscribe(host: String, port: Int, observer: Observer<in StatefulData>) {
        this.observer = observer
        observer.onSubscribe(threadObserver)

        try {
            if (mSocket.isClosed) {
                if (!threadObserver.isDisposed)
                    threadObserver.dispose()
                init()
            }

            val timeout = 15 * 60 * 1000
            mSocket.connect(InetSocketAddress(host, port), timeout)
            mSocket.soTimeout = timeout

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

    override fun sendData(data: CharArray) {
        if (mSocket.isConnected)
            senderThread.send(data)
    }

    override fun getDisposableSubscriber(): Disposable = threadObserver
}