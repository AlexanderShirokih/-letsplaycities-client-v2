package ru.quandastudio.lpsclient.core

import ru.quandastudio.lpsclient.model.Credentials

interface CredentialsProvider {

    /**
     * Call to reload credentials from disc
     */
    fun invalidate()

    /**
     * Returns actual credentials
     */
    fun getCredentials(): Credentials

}