package ru.quandastudio.lpsclient.core

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class LPSClientMessageDeserializerTest {

    private lateinit var gson: Gson

    @Before
    fun setup() {
        gson = GsonBuilder()
            .registerTypeAdapter(LPSClientMessage::class.java, LPSClientMessageDeserializer())
            .create()
    }

    @Test
    fun testDeserializeMessage() {
        val msg = """
              {"word":"test","action":"word"}
        """

        val output = gson.fromJson(msg, LPSClientMessage::class.java)

        assertTrue(output is LPSClientMessage.LPSWord)
        assertEquals("word", output.action)
    }

}