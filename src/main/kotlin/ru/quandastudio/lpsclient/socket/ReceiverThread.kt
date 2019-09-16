package ru.quandastudio.lpsclient.socket

import java.io.IOException
import java.net.Socket
import java.net.SocketException
import java.net.SocketTimeoutException

class ReceiverThread(private val mSocket: Socket, private val mObserver: ThreadObserver) : Thread("ReceiverThread") {

    override fun run() {
        try {
            println("Start reading thread")
            while (!isInterrupted && mSocket.isConnected) {
                println("Entering reading loop.")
                val reader = mSocket.getInputStream().bufferedReader()

                //size:[sizeInBytes][data]
                val size = reader.readLine().substring(5).toInt()
                val buffer = CharArray(size)

                reader.read(buffer)
                mObserver.onNext(buffer)
            }
        } catch (e: InterruptedException) {
            // ThreadObserver should interrupt this thread
            mObserver.dispose()
        } catch (e: IOException) {
            when (e) {
                is SocketTimeoutException -> {
                    // Dispose connection by timeout
                    mObserver.dispose()
                }
                is SocketException -> {
                    // Ignore
                }
                else -> mObserver.onError(e)
            }
        } catch (e: NullPointerException) {
            mObserver.onError(e)
        } finally {
            println("Stop reading thread")
        }
    }

}