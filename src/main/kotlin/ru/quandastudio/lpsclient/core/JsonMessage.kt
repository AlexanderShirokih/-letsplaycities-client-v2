package ru.quandastudio.lpsclient.core

import com.google.gson.GsonBuilder
import java.io.CharArrayReader

class JsonMessage {

    companion object {
        private val gson = GsonBuilder()
            .registerTypeAdapter(LPSMessage::class.java, LPSMessageDeserializer())
            .registerTypeAdapter(LPSClientMessage::class.java, LPSClientMessageDeserializer())
            .create()
    }

    fun write(msg: LPSClientMessage) = gson.toJson(msg).toCharArray()

    fun readMessage(data: CharArray): LPSMessage = gson.fromJson(CharArrayReader(data), LPSMessage::class.java)

    fun write(msg: LPSMessage) = gson.toJson(msg).toCharArray()

    fun readClientMessage(data: CharArray): LPSClientMessage? =
        gson.fromJson(CharArrayReader(data), LPSClientMessage::class.java)

}