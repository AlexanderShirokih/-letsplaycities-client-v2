package ru.quandastudio.lpsclient.core

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class LPSMessageDeserializerTest {

    private lateinit var gson: Gson

    @Before
    fun setup() {
        gson = GsonBuilder()
            .registerTypeAdapter(LPSMessage::class.java, LPSMessageDeserializer())
            .create()
    }

    @Test
    fun testDeserializeMessage() {
        val msg = """
            {"userId":1,"accHash":"-remote-","newerBuild":1,"action":"logged_in"}
        """

        val output = gson.fromJson(msg, LPSMessage::class.java)

        assertTrue(output is LPSMessage.LPSLoggedIn)
        assertEquals("logged_in", output.action)
    }

}