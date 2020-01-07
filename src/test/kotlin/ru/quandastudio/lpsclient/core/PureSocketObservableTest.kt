package ru.quandastudio.lpsclient.core

import org.junit.Test
import ru.quandastudio.lpsclient.socket.SocketObservable
import ru.quandastudio.lpsclient.socket.PureSocketObservable
import java.net.UnknownHostException

class PureSocketObservableTest {


    @Test
    fun testThrowUnknownHostException() {
        PureSocketObservable("ttt", 62964)
            .test()
            .assertError(UnknownHostException::class.java)
    }

    @Test
    fun testConnection() {
        PureSocketObservable("localhost", 62964)
            .test()
            .assertValue { it.state == SocketObservable.State.CONNECTED }
    }

    @Test
    fun testSendData() {
        PureSocketObservable("localhost", 62964)
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