package ru.quandastudio.lpsclient.core

import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import ru.quandastudio.lpsclient.AuthorizationException
import ru.quandastudio.lpsclient.LPSException
import ru.quandastudio.lpsclient.model.AuthData
import ru.quandastudio.lpsclient.model.PlayerData
import ru.quandastudio.lpsclient.socket.SocketObservable
import ru.quandastudio.lpsclient.socket.PureSocketObservable
import ru.quandastudio.lpsclient.socket.WebSocketObservable
import java.util.concurrent.TimeUnit

class NetworkClient constructor(
    private val base64Provider: Base64Provider,
    val isLocal: Boolean,
    connectionType: ConnectionType,
    host: String,
    port: Int = 62964
) {
    enum class ConnectionType {
        PureSocket, WebSocket;

        fun createSocketObservable(host: String, port: Int): SocketObservable {
            return when (this) {
                PureSocket -> PureSocketObservable(host, port)
                WebSocket -> WebSocketObservable(host, port)
            }
        }
    }

    init {
        Base64Ext.installBase64(base64Provider)
    }

    private val json = JsonMessage()
    private val mSocket = connectionType.createSocketObservable(host, port)
    private val mSharedSocket: Observable<LPSMessage> = mSocket
        .doOnError {
            it.printStackTrace()
        }
        .map {
            when (it.state) {
                SocketObservable.State.DISCONNECTED -> LPSMessage.LPSLeaveMessage(false)
                SocketObservable.State.DATA -> json.readMessage(it.data)
                SocketObservable.State.CONNECTED -> LPSMessage.LPSConnectedMessage
            }
        }
        .subscribeOn(Schedulers.io())
        .publish().refCount(3, TimeUnit.SECONDS)

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
            throw LPSException(
                "requireConnection() called, socket is not connected",
                LPSException.LPSErrType.CONNECTION_ERROR
            )
        }
        return mSocket
    }

    fun getMessages(): Observable<LPSMessage> = mSharedSocket

    private fun sendMessage(message: LPSClientMessage) = requireConnection().sendData(json.write(message))

    private fun ByteArray.toBase64(): String = base64Provider.encode(this)

    fun login(userData: PlayerData, fbToken: String): Maybe<AuthResult> {
        val ad = userData.authData
        return Observable
            .fromCallable {
                LPSClientMessage.LPSLogIn(
                    pd = userData,
                    fbToken = fbToken,
                    userId = if (ad.userID > 0) ad.userID else null,
                    hash = ad.accessHash,
                    avatar = userData.avatar?.toBase64()
                )
            }
            .doOnNext(::sendMessage)
            .flatMap { getMessages() }
            .firstElement()
            .flatMap {
                when (it) {
                    is LPSMessage.LPSLeaveMessage ->
                        Maybe.error(LPSException("Cannot logIn on server"))
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
                result = if (accepted) 1 else 2,
                oppUid = userId
            )
        )
    }

    fun sendAdminCommand(command: String) {
        sendMessage(LPSClientMessage.LPSAdmin(command))
    }

}