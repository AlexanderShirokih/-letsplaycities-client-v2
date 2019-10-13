package ru.quandastudio.lpsclient

interface ErrorListener {
    fun onError(ex: LPSException)
}