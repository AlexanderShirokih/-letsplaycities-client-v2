package ru.quandastudio.lpsclient.socket

import java.io.BufferedWriter
import java.io.IOException
import java.net.Socket
import java.util.concurrent.ArrayBlockingQueue

class SenderThread(private val mSocket: Socket, private val mObserver: ThreadObserver) : Thread("SenderThread") {

    private val tasks: ArrayBlockingQueue<CharArray> = ArrayBlockingQueue(10)
    private val lock = Object()

    override fun run() {
        try {
            println("Start writing thread")
            while (!isInterrupted && mSocket.isConnected) {
                val writer = mSocket.getOutputStream().bufferedWriter()
                sendPendingTasks(writer)
            }
        } catch (e: InterruptedException) {
            // ThreadObserver should interrupt this thread
            mObserver.dispose()
        } catch (e: IOException) {
            mObserver.onError(e)
        } catch (e: NullPointerException) {
            mObserver.onError(e)
        } finally {
            println("Stop writing thread")
        }
    }

    private fun sendPendingTasks(writer: BufferedWriter) {
        if (tasks.isEmpty()) {
            synchronized(lock) {
                lock.wait()
            }
        }
        val task: CharArray = tasks.poll() ?: return
        writer.write(task)
        writer.flush()
    }

    fun send(data: CharArray) {
        tasks.add(data)
        synchronized(lock) {
            lock.notifyAll()
        }
    }
}