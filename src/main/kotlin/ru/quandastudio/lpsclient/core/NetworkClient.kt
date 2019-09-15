package ru.quandastudio.lpsclient.core

import ru.quandastudio.lpsclient.AuthorizationException
import ru.quandastudio.lpsclient.LPSException
import ru.quandastudio.lpsclient.model.AuthData
import ru.quandastudio.lpsclient.model.PlayerData
import java.io.*

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

    @Throws(AuthorizationException::class, LPSException::class)
    fun login(userData: PlayerData, fbToken: String): AuthResult {
        val ad = userData.authData

        val msgWriter = requireConnection()
            .writer()
            .writeByte(LPSv3Tags.ACTION_LOGIN, 4)
            .writeString(LPSv3Tags.LOGIN, ad.login)
            .writeString(LPSv3Tags.ACCESS_TOKEN, ad.accessToken)
            .writeByte(LPSv3Tags.SN, ad.snType.ordinal.toByte())
            .writeString(LPSv3Tags.SN_UID, ad.snUID)
            .writeChar(LPSv3Tags.CLIENT_BUILD, userData.clientBuild)
            .writeString(LPSv3Tags.CLIENT_VERSION, userData.clientVersion)
            .writeBool(LPSv3Tags.CAN_REC_MSG, userData.canReceiveMessages)
            .writeBool(LPSv3Tags.ALLOW_SEND_UID, userData.allowSendUID)
            .writeString(LPSv3Tags.FIREBASE_TOKEN, fbToken)

        if (ad.userID > 0) msgWriter.writeInt(LPSv3Tags.UID, ad.userID)
        if (ad.accessHash != null) msgWriter.writeString(LPSv3Tags.ACC_HASH, ad.accessHash!!)
        // NOTE: Now we can write only 64kB images
        userData.avatar?.run { if (this.isNotEmpty()) msgWriter.writeBytes(LPSv3Tags.AVATAR_PART0, userData.avatar!!) }

        msgWriter.buildAndFlush()

        val msgReader = requireConnection().reader()

        if (msgReader.getMasterTag() != LPSv3Tags.ACTION_LOGIN_RESULT)
            throw LPSException("Waiting for LOGIN_RESULT action, but action=${msgReader.getMasterTag()} received")

        if (msgReader.readBoolean(LPSv3Tags.ACTION_LOGIN_RESULT)) {
            ad.userID = msgReader.readInt(LPSv3Tags.S_UID)
            ad.accessHash = msgReader.readString(LPSv3Tags.S_ACC_HASH)
            val newerBuild = msgReader.readChar(LPSv3Tags.NEWER_BUILD)
            return AuthResult(ad, newerBuild)
        }

        val reason = msgReader.optString(LPSv3Tags.BAN_REASON)
        val connError = msgReader.optString(LPSv3Tags.CONNECTION_ERROR)
        throw AuthorizationException(reason, connError)
    }

    @Throws(IOException::class)
    fun play(isWaiting: Boolean, userId: Int?) {
        requireConnection()
            .writer()
            .writeByte(LPSv3Tags.ACTION_PLAY, if (isWaiting) LPSv3Tags.E_FRIEND_MODE else LPSv3Tags.E_RANDOM_PAIR_MODE)
            .writeInt(LPSv3Tags.OPP_UID, if (isWaiting) userId!! else 0)
            .buildAndFlush()
    }

    fun readMessage(): LPSMessage {
        return LPSMessage.from(requireConnection().reader())
    }

    fun requestBlackList() {
        requireConnection()
            .writer()
            .writeByte(LPSv3Tags.ACTION_QUERY_BANLIST, LPSv3Tags.E_QUERY_LIST)
            .buildAndFlush()
    }

    fun requestFriendsList() {
        requireConnection()
            .writer()
            .writeByte(LPSv3Tags.ACTION_QUERY_FRIEND_INFO, 1)
            .buildAndFlush()
    }

    fun deleteFriend(userId: Int) {
        requireConnection()
            .writer()
            .writeByte(LPSv3Tags.ACTION_FRIEND, LPSv3Tags.E_DELETE_REQUEST)
            .writeInt(LPSv3Tags.FRIEND_UID, userId)
            .buildAndFlush()
    }

    fun removeFromBanList(userId: Int) {
        requireConnection()
            .writer()
            .writeByte(LPSv3Tags.ACTION_QUERY_BANLIST, LPSv3Tags.E_DELETE_REQUEST)
            .writeInt(LPSv3Tags.FRIEND_UID, userId)
            .buildAndFlush()
    }

    fun banUser(userId: Int) {
        requireConnection()
            .writer()
            .writeByte(LPSv3Tags.ACTION_BAN, LPSv3Tags.E_USER_BAN)
            .writeInt(LPSv3Tags.S_UID, userId)
            .writeString(LPSv3Tags.BAN_REASON, "unimpl")
            .writeString(LPSv3Tags.ROOM_CONTENT, "unimpl")
            .buildAndFlush()
    }

    fun isConnected() = mConnection != null && mConnection!!.isConnected()

    fun sendWord(word: String) {
        requireConnection()
            .writer()
            .writeString(LPSv3Tags.ACTION_WORD, word)
            .buildAndFlush()
    }

    fun sendMessage(message: String) {
        requireConnection()
            .writer()
            .writeString(LPSv3Tags.ACTION_MSG, message)
            .buildAndFlush()
    }

    fun sendFriendRequest() {
        requireConnection()
            .writer()
            .writeByte(LPSv3Tags.ACTION_FRIEND, LPSv3Tags.E_SEND_REQUEST)
            .buildAndFlush()
    }

    fun sendFriendAcceptance(accepted: Boolean) {
        requireConnection()
            .writer()
            .writeByte(
                LPSv3Tags.ACTION_FRIEND,
                if (accepted) LPSv3Tags.E_ACCEPT_REQUSET else LPSv3Tags.E_DENY_REQUSET
            )
            .buildAndFlush()
    }

    fun sendFriendRequestResult(result: Boolean, userId: Int) {
        requireConnection()
            .writer()
            .writeByte(LPSv3Tags.ACTION_FM_REQ_RESULT, (if (result) 1 else 2).toByte())
            .writeInt(
                LPSv3Tags.SENDER_UID,
                userId
            ).buildAndFlush()
    }

}