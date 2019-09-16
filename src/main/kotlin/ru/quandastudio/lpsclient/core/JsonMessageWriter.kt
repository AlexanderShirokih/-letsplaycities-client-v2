package ru.quandastudio.lpsclient.core

import com.google.gson.Gson
import java.io.BufferedOutputStream

class JsonMessageWriter(private val gson: Gson, private val outputStream: BufferedOutputStream) {
    private val writer = outputStream.bufferedWriter()

    fun close() = outputStream.close()

    fun send(msg: LPSClientMessage) {
        val json  = gson.toJson(msg)
        println("JSON: $json")
        writer.write("json")
        writer.write(json)
        writer.flush()
    }
}