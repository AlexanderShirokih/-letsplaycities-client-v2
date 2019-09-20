package ru.quandastudio.lpsclient.socket

import java.io.BufferedWriter
import java.net.Socket
import java.util.concurrent.ArrayBlockingQueue

class SenderThread(private val mSocket: Socket, private val mObserver: ThreadObserver) : Thread("SenderThread") {

    private val tasks: ArrayBlockingQueue<CharArray> = ArrayBlockingQueue(10)
    private val lock = Object()

    override fun run() {
        try {
            while (!isInterrupted && mSocket.isConnected) {
                val writer = mSocket.getOutputStream().bufferedWriter()
                sendPendingTasks(writer)
            }
        } catch (e: Exception) {
            if(e !is InterruptedException && !mObserver.isDisposed)
                mObserver.onError(e)
        }
    }

    private fun sendPendingTasks(writer: BufferedWriter) {
        if (tasks.isEmpty()) {
            synchronized(lock) {
                lock.wait()
            }
        }
        val task: CharArray = tasks.poll() ?: return

        writer.apply {
            write(task)
            flush()
        }
    }

    fun send(data: CharArray) {
        tasks.add(data)
        synchronized(lock) {
            lock.notifyAll()
        }
    }
}