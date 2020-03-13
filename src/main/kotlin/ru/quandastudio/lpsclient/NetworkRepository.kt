package ru.quandastudio.lpsclient

import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import ru.quandastudio.lpsclient.core.LPSMessage
import ru.quandastudio.lpsclient.core.NetworkClient
import ru.quandastudio.lpsclient.model.ConnectionResult
import ru.quandastudio.lpsclient.model.PlayerData
import java.util.concurrent.TimeUnit

class NetworkRepository(
    private val networkClient: NetworkClient,
    private val token: Single<String>
) {

    private fun inputMessage(): Observable<LPSMessage> = networkClient.getMessages()

    fun getWords(): Observable<LPSMessage.LPSWordMessage> =
        inputMessage().filter { it is LPSMessage.LPSWordMessage }.cast(LPSMessage.LPSWordMessage::class.java)

    fun getMessages(): Observable<LPSMessage.LPSMsgMessage> =
        inputMessage().filter { it is LPSMessage.LPSMsgMessage }.cast(LPSMessage.LPSMsgMessage::class.java)

    fun getLeave(): Maybe<LPSMessage.LPSLeaveMessage> =
        inputMessage().filter { it is LPSMessage.LPSLeaveMessage }.cast(LPSMessage.LPSLeaveMessage::class.java)
            .firstElement()

    fun getTimeout(): Maybe<LPSMessage> =
        inputMessage().filter { it is LPSMessage.LPSTimeoutMessage }.firstElement()

    fun getFriendsRequest(): Observable<LPSMessage.LPSFriendRequest> =
        inputMessage().filter { it is LPSMessage.LPSFriendRequest }.cast(LPSMessage.LPSFriendRequest::class.java)

    fun getFriendsModeRequest(): Observable<LPSMessage.LPSFriendModeRequest> =
        inputMessage().filter { it is LPSMessage.LPSFriendModeRequest }
            .cast(LPSMessage.LPSFriendModeRequest::class.java)

    fun getKick(): Maybe<LPSMessage.LPSBannedMessage> =
        inputMessage().filter { it is LPSMessage.LPSBannedMessage }.cast(LPSMessage.LPSBannedMessage::class.java)
            .firstElement()

    private fun networkClient(): Observable<NetworkClient> =
        Observable.just(networkClient)
            .subscribeOn(Schedulers.io())

    fun login(userData: PlayerData): Observable<NetworkClient.AuthResult> {
        return networkClient()
            .flatMap { it.connect() }
            .flatMapMaybe { it.login(userData, token.blockingGet()) }
    }

    class BannedPlayerException : Exception()

    /**
     * Sends play message to server.
     * @param friendId if `null` game will starts in random pair mode, if [friendId] is present,
     * game will starts in friend mode.
     */
    fun play(friendId: Int?): Observable<ConnectionResult.ConnectedToUser> {
        return networkClient()
            .doOnNext { client -> client.play(friendId) }
            .flatMap {
                inputMessage().filter { msg: LPSMessage -> msg is LPSMessage.LPSPlayMessage }
                    .cast(LPSMessage.LPSPlayMessage::class.java)
            }
            .flatMap {
                if (it.banned) Observable.error(BannedPlayerException()) else Observable.just(it)
            }
            .retryWhen { errors ->
                errors.flatMap { err ->
                    if (err is BannedPlayerException)
                        Observable.just(0L).delay((2L..5L).random(), TimeUnit.SECONDS)
                    else
                        Observable.error(err)
                }
            }
            .map { ConnectionResult.ConnectedToUser(it.getPlayerData(), it.youStarter) }
    }

    fun connectToFriend(): Maybe<ConnectionResult> {
        return networkClient()
            .flatMap { inputMessage() }
            .filter { it is LPSMessage.LPSFriendModeRequest || it is LPSMessage.LPSPlayMessage }
            .map { message ->
                when (message) {
                    is LPSMessage.LPSFriendModeRequest -> ConnectionResult.FriendModeRejected(
                        message.result,
                        message.login
                    )
                    is LPSMessage.LPSPlayMessage -> ConnectionResult.ConnectedToUser(
                        message.getPlayerData(),
                        message.youStarter
                    )
                    else -> throw LPSException("Unexpected message type in when() block")
                }
            }
            .firstElement()
    }

    fun disconnect() = networkClient.disconnect()

    fun acceptFriendRequest(userId: Int): Observable<NetworkRepository> {
        return networkClient()
            .subscribeOn(Schedulers.io())
            .doOnNext { it.acceptFriendRequest(userId) }
            .map { this }
    }

    fun sendWord(city: String): Completable {
        return networkClient()
            .doOnNext { client ->
                client.sendWord(city)
            }
            .ignoreElements()
    }

    fun sendMessage(message: String): Completable {
        return networkClient().doOnNext { client -> client.sendMessage(message) }.ignoreElements()
    }

    fun sendAdminCommand(command: String): Completable {
        return networkClient().doOnNext { client -> client.sendAdminCommand(command) }.ignoreElements()
    }

    fun sendFriendRequest(userId: Int): Completable {
        return networkClient().doOnNext { client -> client.sendFriendRequest(userId) }.ignoreElements()
    }

    fun banUser(userId: Int): Completable {
        return networkClient().doOnNext { client -> client.banUser(userId) }.ignoreElements()
    }

}