package ru.quandastudio.lpsclient.socket

import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import tech.gusavila92.websocketclient.WebSocketClient
import java.lang.Exception
import java.net.URI

class WebSocketObservable(host: String, port: Int) : SocketObservable(host, port), Disposable {

    private var webSocketClient: WebSocketClient? = null

    private var isRunning = false

    override fun isDisposed(): Boolean = !isRunning && webSocketClient != null

    override fun dispose() {
        if (isRunning) {
            webSocketClient?.run {
                onCloseReceived()
                close()
            }
        }
    }

    override fun subscribe(host: String, port: Int, observer: Observer<in StatefulData>) {
        observer.onSubscribe(this)

        webSocketClient = object : WebSocketClient(URI("ws://$host:$port/game")) {
            override fun onOpen() {
                observer.onNext(
                    StatefulData(
                        this@WebSocketObservable,
                        State.CONNECTED,
                        charArrayOf()
                    )
                )
            }

            override fun onTextReceived(message: String) {
                observer.onNext(
                    StatefulData(
                        this@WebSocketObservable,
                        State.DATA,
                        message.toCharArray()
                    )
                )
            }

            override fun onBinaryReceived(data: ByteArray?) = Unit
            override fun onPongReceived(data: ByteArray?) = Unit
            override fun onPingReceived(data: ByteArray?) = Unit

            override fun onException(e: Exception) {
                onCloseReceived()
                observer.onError(e)
            }

            override fun onCloseReceived() {
                observer.onNext(
                    StatefulData(
                        this@WebSocketObservable,
                        State.DISCONNECTED,
                        charArrayOf()
                    )
                )
                isRunning = false
            }
        }.apply {
            setConnectTimeout(10000)
            setReadTimeout(120000)
            enableAutomaticReconnection(5000)
            connect()
        }
        isRunning = true
    }

    override fun sendData(data: CharArray) {
        if (isRunning)
            webSocketClient?.send(String(data))
    }

    override fun getDisposableSubscriber(): Disposable = this
}