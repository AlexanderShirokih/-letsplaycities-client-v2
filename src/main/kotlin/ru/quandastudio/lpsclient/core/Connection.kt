package ru.quandastudio.lpsclient.core

import com.google.gson.GsonBuilder
import java.io.*
import java.net.InetAddress
import java.net.Socket

class Connection @Throws(IOException::class) constructor(host: String, port: Int) {
    private val gson = GsonBuilder()
        .registerTypeAdapter(LPSMessage::class.java, LPSMessageDeserializer())
        .create()

    private val bufferSize = 64 * 1024
    private var reader: JsonMessageReader
    private var writer: JsonMessageWriter
    private var mSocket: Socket

    init {
        val ipAddress = InetAddress.getByName(host)
        mSocket = Socket(ipAddress, port)

        reader = JsonMessageReader(gson, BufferedInputStream(mSocket.getInputStream(), bufferSize))
        writer = JsonMessageWriter(gson, BufferedOutputStream(mSocket.getOutputStream(), bufferSize))
    }

    fun writer(): JsonMessageWriter = writer

    fun reader(): JsonMessageReader = reader

    fun isConnected(): Boolean = mSocket.isConnected && !mSocket.isClosed

    fun disconnect() {
        try {
            writer.close()
        } catch (e: IOException) {
        }
        try {
            reader.close()
        } catch (e: IOException) {
        }
        try {
            mSocket.close()
        } catch (e: IOException) {
        }
    }
}