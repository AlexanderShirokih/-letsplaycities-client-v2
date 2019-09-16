package ru.quandastudio.lpsclient.core

import org.junit.Test
import ru.quandastudio.lpsclient.socket.SocketObservable
import java.net.UnknownHostException

class SocketObservableTest {


    @Test
    fun testThrowUnknownHostException() {
        SocketObservable("ttt", 62964)
            .test()
            .assertError(UnknownHostException::class.java)
    }

    @Test
    fun testConnection() {
        SocketObservable("localhost", 62964)
            .test()
            .assertValue { it.state == SocketObservable.State.CONNECTED }
    }

    @Test
    fun testSendData() {
        SocketObservable("localhost", 62964)
            .filter { it.state == SocketObservable.State.CONNECTED }
            .doOnNext {
                it.sendResponse(
                    """
                    {
                    "action": "login",
                    "version": 4,
                    "login" : "test",
                    "clientVersion": "4",
                    "clientBuild": 4
                    }
                """.trimIndent().toCharArray()
                )
            }
    }
}