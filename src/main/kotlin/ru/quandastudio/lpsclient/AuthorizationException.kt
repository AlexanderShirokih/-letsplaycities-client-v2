package ru.quandastudio.lpsclient

class AuthorizationException(val banReason: String?, val connectionError: String?) : Exception()