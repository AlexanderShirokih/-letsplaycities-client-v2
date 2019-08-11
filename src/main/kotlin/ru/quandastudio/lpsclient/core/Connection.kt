package ru.quandastudio.lpsclient.core

import java.io.*
import java.net.InetAddress
import java.net.Socket

class Connection @Throws(IOException::class) constructor(host: String, port: Int) {
    private val bufferSize = 64 * 1024
    private var mInputStream: DataInputStream
    private var mWriter: LPSMessageWriter
    private var mSocket: Socket

    init {
        val ipAddress = InetAddress.getByName(host)
        mSocket = Socket(ipAddress, port)

        mInputStream = DataInputStream(BufferedInputStream(mSocket.getInputStream(), bufferSize))
        mWriter = LPSMessageWriter(DataOutputStream(BufferedOutputStream(mSocket.getOutputStream(), bufferSize)))
    }

    fun writer(): LPSMessageWriter = mWriter

    fun reader(): LPSMessageReader = LPSMessageReader(mInputStream)

    fun isConnected(): Boolean = mSocket.isConnected && !mSocket.isClosed

    fun disconnect() {
        try {
            mInputStream.close()
        } catch (e: IOException) {
        }
        try {
            mWriter.close()
        } catch (e: IOException) {
        }
        try {
            mSocket.close()
        } catch (e: IOException) {
        }
    }
}