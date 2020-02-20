package ru.quandastudio.lpsclient.core

import ru.quandastudio.lpsclient.model.Credentials

abstract class CredentialsProvider {

    private var credentials: Credentials? = null

    /**
     * Call to reload credentials from disc
     */
    fun invalidate() {
        credentials = null
    }

    /**
     * Returns actual credentials
     */
    fun getCredentials(): Credentials {
        if (credentials == null)
            credentials = loadCredentials()
        return credentials!!
    }

    abstract fun loadCredentials(): Credentials

    /**
     * Call to update internal state of Credentials
     */
    fun update(credentials: Credentials) {
        this.credentials = credentials
    }

}