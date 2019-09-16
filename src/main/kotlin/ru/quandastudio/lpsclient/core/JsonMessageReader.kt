package ru.quandastudio.lpsclient.core

import com.google.gson.Gson
import java.io.BufferedInputStream
import java.io.CharArrayReader

class JsonMessageReader(private val gson: Gson, private val inputStream: BufferedInputStream) {

    fun read(): LPSMessage {
        println("RD: ready")

        val reader = inputStream.bufferedReader()

        val size = reader.readLine().substring(5).toInt()
        val buffer = CharArray(size)


        println("RD: size=$size")
        reader.read(buffer)

        println("RD: parsed!")
        val v = gson.fromJson(CharArrayReader(buffer), LPSMessage::class.java)
        println("Got: $v")
        return v;
    }

    fun close() = inputStream.close()
}