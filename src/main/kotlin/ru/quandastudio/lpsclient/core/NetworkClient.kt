package ru.quandastudio.lpsclient.core

import ru.quandastudio.lpsclient.AuthorizationException
import ru.quandastudio.lpsclient.LPSException
import ru.quandastudio.lpsclient.model.AuthData
import ru.quandastudio.lpsclient.model.PlayerData
import java.io.*
import java.util.*

class NetworkClient constructor(val isLocal: Boolean, private val host: String, private val port: Int = 62964) {

    private var mConnection: Connection? = null

    class AuthResult(
        val authData: AuthData,
        val newerBuild: Int
    )

    @Throws(IOException::class)
    fun connect() {
        mConnection = Connection(host, port)
    }

    private fun requireConnection(): Connection {
        if (mConnection == null) {
            throw LPSException("requireConnection() called, but mConnection is null")
        }
        return mConnection!!
    }

    fun disconnect() {
        mConnection?.disconnect()
        mConnection = null
    }

    private fun ByteArray.toBase64(): String {
        return Base64.getEncoder().encodeToString(this)
    }

    @Throws(AuthorizationException::class, LPSException::class)
    fun login(userData: PlayerData, fbToken: String): AuthResult {
        val ad = userData.authData

        val login = LPSClientMessage.LPSLogIn(
            userData,
            fbToken,
            if (ad.userID > 0) ad.userID else null,
            ad.accessHash,
            userData.avatar?.toBase64()
        )

        requireConnection()
            .writer()
            .send(login)

        val loginMsg = requireConnection().reader().read()

        if (loginMsg is LPSMessage.LPSBanned)
            throw AuthorizationException(loginMsg.banReason, loginMsg.connError)

        if (loginMsg is LPSMessage.LPSLoggedIn) {
            ad.userID = loginMsg.userId
            ad.accessHash = loginMsg.accHash
            return AuthResult(ad, loginMsg.newerBuild)
        }

        throw LPSException("Waiting for LPSLoggedIn message, but $loginMsg received")
    }

    @Throws(IOException::class)
    fun play(isWaiting: Boolean, userId: Int?) {
        val play = LPSClientMessage.LPSPlay(
            mode = if (isWaiting) LPSClientMessage.PlayMode.FRIEND else LPSClientMessage.PlayMode.RANDOM_PAIR,
            oppUid = if (isWaiting) userId!! else null
        )
        requireConnection().writer().send(play)
    }

    fun readMessage(): LPSMessage = requireConnection().reader().read()

    fun requestBlackList() {
        requireConnection()
            .writer()
            .send(LPSClientMessage.LPSBanList(LPSClientMessage.RequestType.QUERY_LIST))
    }

    fun requestFriendsList() {
        requireConnection()
            .writer()
            .send(LPSClientMessage.LPSFriendList)
    }

    fun deleteFriend(userId: Int) {
        requireConnection()
            .writer()
            .send(
                LPSClientMessage.LPSFriendAction(
                    type = LPSClientMessage.RequestType.DELETE,
                    oppUid = userId
                )
            )
    }

    fun removeFromBanList(userId: Int) {
        requireConnection()
            .writer()
            .send(
                LPSClientMessage.LPSBanList(
                    type = LPSClientMessage.RequestType.DELETE,
                    friendUid = userId
                )
            )
    }

    fun banUser(userId: Int) {
        requireConnection()
            .writer()
            .send(LPSClientMessage.LPSBan(targetId = userId))
    }

    fun isConnected() = mConnection != null && mConnection!!.isConnected()

    fun sendWord(word: String) {
        requireConnection()
            .writer()
            .send(LPSClientMessage.LPSWord(word))
    }

    fun sendMessage(message: String) {
        requireConnection()
            .writer()
            .send(LPSClientMessage.LPSMsg(message))
    }

    fun sendFriendRequest() {
        requireConnection()
            .writer()
            .send(LPSClientMessage.LPSFriendAction(LPSClientMessage.RequestType.SEND))
    }

    fun sendFriendAcceptance(accepted: Boolean) {
        requireConnection()
            .writer()
            .send(
                LPSClientMessage.LPSFriendAction(
                    if (accepted) LPSClientMessage.RequestType.ACCEPT else LPSClientMessage.RequestType.DENY
                )
            )
    }

    fun sendFriendRequestResult(accepted: Boolean, userId: Int) {
        requireConnection()
            .writer()
            .send(
                LPSClientMessage.LPSFriendMode(
                    res = if (accepted) 1 else 2,
                    oppUid = userId
                )
            )
    }

}