package ru.quandastudio.lpsclient

import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import ru.quandastudio.lpsclient.core.LPSMessage
import ru.quandastudio.lpsclient.core.NetworkClient
import ru.quandastudio.lpsclient.model.BlackListItem
import ru.quandastudio.lpsclient.model.FriendInfo
import ru.quandastudio.lpsclient.model.PlayerData
import java.util.concurrent.TimeUnit

class NetworkRepository(private val mNetworkClient: NetworkClient, private val token: Single<String>) {

    private val disposable = CompositeDisposable()

    private fun inputMessage(): Observable<LPSMessage> = mNetworkClient.getMessages()

    val isLocal = mNetworkClient.isLocal

    fun getWords(): Observable<LPSMessage.LPSWordMessage> =
        inputMessage().filter { it is LPSMessage.LPSWordMessage }.cast(LPSMessage.LPSWordMessage::class.java)

    fun getMessages(): Observable<LPSMessage.LPSMsgMessage> =
        inputMessage().filter { it is LPSMessage.LPSMsgMessage }.cast(LPSMessage.LPSMsgMessage::class.java)

    fun getLeave(): Maybe<LPSMessage.LPSLeaveMessage> =
        inputMessage().filter { it is LPSMessage.LPSLeaveMessage }.cast(LPSMessage.LPSLeaveMessage::class.java)
            .firstElement()

    fun getTimeout(): Maybe<LPSMessage> =
        inputMessage().filter { it is LPSMessage.LPSTimeoutMessage }.firstElement()

    fun getFriendsRequest(): Observable<LPSMessage.FriendRequest> =
        inputMessage().filter { it is LPSMessage.LPSFriendRequest }.cast(LPSMessage.LPSFriendRequest::class.java)
            .map { it.requestResult }

    fun getFriendsModeRequest(): Observable<LPSMessage.LPSFriendModeRequest> =
        inputMessage().filter { it is LPSMessage.LPSFriendModeRequest }.cast(LPSMessage.LPSFriendModeRequest::class.java)

    fun getKick(): Maybe<LPSMessage.LPSBannedMessage> =
        inputMessage().filter { it is LPSMessage.LPSBannedMessage }.cast(LPSMessage.LPSBannedMessage::class.java)
            .firstElement()

    fun getDisconnect(): Maybe<LPSMessage.LPSLeaveMessage> =
        inputMessage().filter { it is LPSMessage.LPSLeaveMessage && !it.leaved }
            .cast(LPSMessage.LPSLeaveMessage::class.java)
            .firstElement()

    private fun networkClient(): Observable<NetworkClient> =
        Observable.just(mNetworkClient)
            .subscribeOn(Schedulers.io())

    fun login(userData: PlayerData): Observable<NetworkClient.AuthResult> {
        return networkClient()
            .flatMap { it.connect() }
            .flatMapMaybe { it.login(userData, token.blockingGet()) }
    }

    class BannedPlayerException : Exception()

    fun play(isWaiting: Boolean, friendId: Int?): Observable<Pair<PlayerData, Boolean>> {
        return networkClient()
            .doOnNext { client -> client.play(isWaiting, friendId) }
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
                        Observable.just(0L).delay((0L..1L).random(), TimeUnit.SECONDS)
                    else
                        Observable.error(err)
                }
            }
            .map { it.getPlayerData() to it.youStarter }
    }

    fun connectToFriend(): Maybe<Pair<PlayerData, Boolean>> {
        return networkClient()
            .takeUntil<LPSMessage.LPSFriendModeRequest> {
                inputMessage().filter { it is LPSMessage.LPSFriendModeRequest }
            }
            .flatMap {
                inputMessage().filter { it is LPSMessage.LPSPlayMessage }
                    .cast(LPSMessage.LPSPlayMessage::class.java)
            }
            .map { it.getPlayerData() to it.youStarter }
            .firstElement()
    }

    fun getBlackList(): Single<List<BlackListItem>> {
        return networkClient()
            .doOnNext { t -> t.requestBlackList() }
            .flatMap {
                inputMessage().filter { it is LPSMessage.LPSBannedListMessage }
                    .cast(LPSMessage.LPSBannedListMessage::class.java)
            }
            .firstOrError()
            .map { it.list }
    }

    fun getFriendsList(): Single<ArrayList<FriendInfo>> {
        return networkClient()
            .doOnNext { t -> t.requestFriendsList() }
            .flatMap {
                inputMessage().filter { it is LPSMessage.LPSFriendsList }.cast(LPSMessage.LPSFriendsList::class.java)
            }
            .firstOrError()
            .map { it.list }
    }

    fun deleteFriend(userId: Int): Completable {
        return networkClient()
            .doOnNext { mNetworkClient.deleteFriend(userId) }
            .ignoreElements()
    }

    fun removeFromBanList(userId: Int): Completable {
        return networkClient()
            .doOnNext { mNetworkClient.removeFromBanList(userId) }
            .ignoreElements()
    }

    fun disconnect() {
        Completable.fromRunnable(mNetworkClient::disconnect)
            .subscribeOn(Schedulers.io())
            .subscribe()
    }

    fun sendFriendRequestResult(result: Boolean, userId: Int): Completable {
        return networkClient()
            .subscribeOn(Schedulers.io())
            .doOnNext { it.sendFriendRequestResult(result, userId) }
            .ignoreElements()
    }

    fun sendWord(city: String) {
        disposable.add(networkClient()
            .subscribe { client -> client.sendWord(city) })
    }

    fun sendMessage(message: String) {
        disposable.add(networkClient()
            .subscribe { client -> client.sendMessage(message) })
    }

    fun sendFriendRequest() {
        disposable.add(networkClient().subscribe { client -> client.sendFriendRequest() })
    }

    fun sendFriendAcceptance(accepted: Boolean) {
        disposable.add(networkClient().subscribe { client -> client.sendFriendAcceptance(accepted) })
    }

    fun banUser(userId: Int) {
        disposable.add(networkClient().subscribe { client -> client.banUser(userId) })
    }

}