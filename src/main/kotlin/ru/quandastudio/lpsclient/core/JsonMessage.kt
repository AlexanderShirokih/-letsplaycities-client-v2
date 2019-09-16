package ru.quandastudio.lpsclient.core

import com.google.gson.GsonBuilder
import java.io.CharArrayReader

class JsonMessage() {
    //TODO: Inject
    private val gson = GsonBuilder()
        .registerTypeAdapter(LPSMessage::class.java, LPSMessageDeserializer())
        .create()

    fun write(msg: LPSClientMessage) = gson.toJson(msg).toCharArray()

    fun read(data: CharArray): LPSMessage = gson.fromJson(CharArrayReader(data), LPSMessage::class.java)
}