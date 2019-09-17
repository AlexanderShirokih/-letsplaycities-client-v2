package ru.quandastudio.lpsclient.core

import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import ru.quandastudio.lpsclient.AuthorizationException
import ru.quandastudio.lpsclient.LPSException
import ru.quandastudio.lpsclient.model.AuthData
import ru.quandastudio.lpsclient.model.PlayerData
import ru.quandastudio.lpsclient.socket.SocketObservable
import java.util.Base64
import java.util.concurrent.TimeUnit

class NetworkClient constructor(val isLocal: Boolean, host: String, port: Int = 62964) {

    private val json = JsonMessage()
    private val mSocket = SocketObservable(host, port)
    private val mSharedSocket: Observable<LPSMessage> = mSocket
        .doOnError {
            println("Error catched: $it")
            it.printStackTrace()
        }
        .map {
            when (it.state) {
                SocketObservable.State.DISCONNECTED -> LPSMessage.LPSLeaveMessage(false)
                SocketObservable.State.DATA -> json.read(it.data)
                SocketObservable.State.CONNECTED -> LPSMessage.LPSConnectedMessage
            }
        }
        .subscribeOn(Schedulers.io())
        .publish().refCount(1, TimeUnit.SECONDS)

    class AuthResult(
        val authData: AuthData,
        val newerBuild: Int
    )

    fun connect(): Observable<NetworkClient> {
        return mSharedSocket
            .filter { it is LPSMessage.LPSConnectedMessage }
            .map { this@NetworkClient }
    }

    fun disconnect() = mSocket.dispose()

    private fun requireConnection(): SocketObservable {
        if (!mSocket.isConnected()) {
            throw LPSException("requireConnection() called, socket is not connected")
        }
        return mSocket
    }

    fun getMessages(): Observable<LPSMessage> = mSharedSocket

    private fun sendMessage(message: LPSClientMessage) = requireConnection().sendData(json.write(message))

    private fun ByteArray.toBase64(): String {
        return Base64.getEncoder().encodeToString(this)
    }

    fun login(userData: PlayerData, fbToken: String): Maybe<AuthResult> {
        val ad = userData.authData
        return Observable
            .fromCallable {
                LPSClientMessage.LPSLogIn(
                    userData,
                    fbToken,
                    if (ad.userID > 0) ad.userID else null,
                    ad.accessHash,
                    userData.avatar?.toBase64()
                )
            }
            .doOnNext(::sendMessage)
            .flatMap { getMessages() }
            .firstElement()
            .flatMap {
                when (it) {
                    is LPSMessage.LPSBanned ->
                        Maybe.error(AuthorizationException(it.banReason, it.connError))
                    is LPSMessage.LPSLoggedIn -> {
                        ad.userID = it.userId
                        ad.accessHash = it.accHash
                        Maybe.just(AuthResult(ad, it.newerBuild))
                    }
                    else -> Maybe.error(LPSException("Waiting for LPSLoggedIn message, but $it received"))
                }
            }
    }

    fun play(isWaiting: Boolean, userId: Int?) {
        sendMessage(
            LPSClientMessage.LPSPlay(
                mode = if (isWaiting) LPSClientMessage.PlayMode.FRIEND else LPSClientMessage.PlayMode.RANDOM_PAIR,
                oppUid = if (isWaiting) userId!! else null
            )
        )
    }


    fun requestBlackList() {
        sendMessage(LPSClientMessage.LPSBanList(LPSClientMessage.RequestType.QUERY_LIST))
    }

    fun requestFriendsList() {
        sendMessage(LPSClientMessage.LPSFriendList)
    }

    fun deleteFriend(userId: Int) {
        sendMessage(
            LPSClientMessage.LPSFriendAction(
                type = LPSClientMessage.RequestType.DELETE,
                oppUid = userId
            )
        )
    }

    fun removeFromBanList(userId: Int) {
        sendMessage(
            LPSClientMessage.LPSBanList(
                type = LPSClientMessage.RequestType.DELETE,
                friendUid = userId
            )
        )
    }

    fun banUser(userId: Int) {
        sendMessage(LPSClientMessage.LPSBan(targetId = userId))
    }

    fun isConnected() = mSocket.isConnected()

    fun sendWord(word: String) {
        sendMessage(LPSClientMessage.LPSWord(word))
    }

    fun sendMessage(message: String) {
        sendMessage(LPSClientMessage.LPSMsg(message))
    }

    fun sendFriendRequest() {
        sendMessage(LPSClientMessage.LPSFriendAction(LPSClientMessage.RequestType.SEND))
    }

    fun sendFriendAcceptance(accepted: Boolean) {
        sendMessage(
            LPSClientMessage.LPSFriendAction(
                if (accepted) LPSClientMessage.RequestType.ACCEPT else LPSClientMessage.RequestType.DENY
            )
        )
    }

    fun sendFriendRequestResult(accepted: Boolean, userId: Int) {
        sendMessage(
            LPSClientMessage.LPSFriendMode(
                res = if (accepted) 1 else 2,
                oppUid = userId
            )
        )
    }

}