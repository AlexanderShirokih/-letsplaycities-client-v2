package ru.quandastudio.lpsclient.socket

import io.reactivex.Observer

class WebSocketObservable(host: String, port: Int) : SocketObservable(host, port) {

    override fun subscribe(host: String, port: Int, observer: Observer<in StatefulData>) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun sendData(data: CharArray) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun isConnected(): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun dispose() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}