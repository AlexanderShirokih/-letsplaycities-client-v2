package ru.quandastudio.lpsclient.socket

import java.net.Socket
import java.net.SocketException

class ReceiverThread(private val mSocket: Socket, private val mObserver: ThreadObserver) : Thread("ReceiverThread") {

    override fun run() {
        try {
            println("Start reading thread")
            while (!isInterrupted && mSocket.isConnected) {
                println("Entering reading loop.")
                val reader = mSocket.getInputStream().bufferedReader()

                //size:[sizeInBytes][data]
                val line = reader.readLine()
                val size = line?.substring(5)?.toInt() ?: 0
                val buffer = CharArray(size)
                var total = 0
                while (total < size) {
                    val remaining = size - total
                    val read = reader.read(buffer, total, remaining)
                    if (read < 0) break;
                    total += read
                }
                mObserver.onNext(buffer)
            }
        } catch (e: SocketException) {
            // Ignore
        } catch (e: Exception) {
            if (e !is InterruptedException && !mObserver.isDisposed)
                mObserver.onError(e)
        } finally {
            println("Stop reading thread")
        }
    }

}