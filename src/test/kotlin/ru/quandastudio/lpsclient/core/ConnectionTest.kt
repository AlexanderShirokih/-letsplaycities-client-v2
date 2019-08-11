package ru.quandastudio.lpsclient.core

import org.junit.Test
internal class ConnectionTest {

    @Test
    fun testConnection() {
        val con = Connection("localhost", 62964)
        assert(con.isConnected())
        con.disconnect()
        assert(!con.isConnected())
    }

}