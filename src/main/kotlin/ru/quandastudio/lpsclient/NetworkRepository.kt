package ru.quandastudio.lpsclient

import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.ReplaySubject
import ru.quandastudio.lpsclient.core.LPSMessage
import ru.quandastudio.lpsclient.core.NetworkClient
import ru.quandastudio.lpsclient.model.BlackListItem
import ru.quandastudio.lpsclient.model.FriendInfo
import ru.quandastudio.lpsclient.model.PlayerData
import java.io.IOException
import java.util.concurrent.TimeUnit

class NetworkRepository(private val mNetworkClient: NetworkClient, private val token: Single<String>) {

    private val disposable = CompositeDisposable()

    private val inputMessageR = ReplaySubject.create<LPSMessage> {
        println("::Create")
        while (!it.isDisposed && mNetworkClient.isConnected()) {
            println("::Wait for msg")
            it.onNext(
                try {
                    mNetworkClient.readMessage()
                } catch (e: IOException) {
                    LPSMessage.LPSLeaveMessage(false)
                }
            )
        }
        println("::Completion")
        it.onComplete()
    }

    private val inputMessage: Observable<LPSMessage> by lazy {
        inputMessageR
            .subscribeOn(Schedulers.io())
            .onErrorReturn { LPSMessage.LPSLeaveMessage(false) }
            .publish().refCount(1, TimeUnit.SECONDS)
    }

    val isLocal = mNetworkClient.isLocal

    val words: Observable<LPSMessage.LPSWordMessage> =
        inputMessage.filter { it is LPSMessage.LPSWordMessage }.cast(LPSMessage.LPSWordMessage::class.java)

    val messages: Observable<LPSMessage.LPSMsgMessage> =
        inputMessage.filter { it is LPSMessage.LPSMsgMessage }.cast(LPSMessage.LPSMsgMessage::class.java)

    val leave: Maybe<LPSMessage.LPSLeaveMessage> =
        inputMessage.filter { it is LPSMessage.LPSLeaveMessage }.cast(LPSMessage.LPSLeaveMessage::class.java)
            .firstElement()

    val timeout: Maybe<LPSMessage> =
        inputMessage.filter { it is LPSMessage.LPSTimeoutMessage }.firstElement()

    val friendsRequest: Observable<LPSMessage.FriendRequest> =
        inputMessage.filter { it is LPSMessage.LPSFriendRequest }.cast(LPSMessage.LPSFriendRequest::class.java)
            .map { it.requestResult }

    val friendsModeRequest: Observable<LPSMessage.LPSFriendModeRequest> =
        inputMessage.filter { it is LPSMessage.LPSFriendModeRequest }.cast(LPSMessage.LPSFriendModeRequest::class.java)

    val kick: Maybe<LPSMessage.LPSBannedMessage> =
        inputMessage.filter { it is LPSMessage.LPSBannedMessage }.cast(LPSMessage.LPSBannedMessage::class.java)
            .firstElement()

    val disconnect: Maybe<LPSMessage.LPSLeaveMessage> by lazy {
        inputMessage.filter { it is LPSMessage.LPSLeaveMessage && !it.leaved }
            .cast(LPSMessage.LPSLeaveMessage::class.java)
            .firstElement()
    }

    private fun networkClient(): Single<NetworkClient> =
        Single.just(mNetworkClient)
            .subscribeOn(Schedulers.io())
            .doOnSuccess { checkConnection() }

    fun login(userData: PlayerData): Single<NetworkClient.AuthResult> {
        return networkClient()
            .map { it.login(userData, token.blockingGet()) }
    }

    class BannedPlayerException : Exception()

    private fun connectAndPlay(isWaiting: Boolean, friendId: Int?): Single<NetworkClient> {
        return networkClient()
            .doOnSuccess {
                println("We have NC!")
            }
            .doOnSuccess { client -> client.play(isWaiting, friendId) }
    }

    fun play(isWaiting: Boolean, friendId: Int?): Maybe<Pair<PlayerData, Boolean>> {
        return connectAndPlay(isWaiting, friendId)
            .toObservable()
            .flatMap {
                inputMessage.filter { msg: LPSMessage -> msg is LPSMessage.LPSPlayMessage }
                    .cast(LPSMessage.LPSPlayMessage::class.java)
            }
            .doOnNext {
                println("We have play msg!")
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
            .firstElement()
    }

    fun connectToFriend(): Maybe<Pair<PlayerData, Boolean>> {
        return networkClient()
            .toObservable()
            .takeUntil<LPSMessage.LPSFriendModeRequest> {
                inputMessage.filter { it is LPSMessage.LPSFriendModeRequest }
            }
            .flatMap {
                inputMessage.filter { it is LPSMessage.LPSPlayMessage }
                    .cast(LPSMessage.LPSPlayMessage::class.java)
            }
            .map { it.getPlayerData() to it.youStarter }
            .firstElement()
    }

    fun getBlackList(): Single<List<BlackListItem>> {
        return networkClient()
            .doOnSuccess { t -> t.requestBlackList() }
            .toObservable()
            .flatMap {
                inputMessage.filter { it is LPSMessage.LPSBannedListMessage }
                    .cast(LPSMessage.LPSBannedListMessage::class.java)
            }
            .firstOrError()
            .map { it.list }
    }

    fun getFriendsList(): Single<ArrayList<FriendInfo>> {
        return networkClient()
            .doOnSuccess { t -> t.requestFriendsList() }
            .toObservable()
            .flatMap {
                inputMessage.filter { it is LPSMessage.LPSFriendsList }.cast(LPSMessage.LPSFriendsList::class.java)
            }
            .firstOrError()
            .map { it.list }
    }

    fun deleteFriend(userId: Int): Completable {
        return networkClient()
            .doOnSuccess { mNetworkClient.deleteFriend(userId) }
            .ignoreElement()
    }

    fun removeFromBanList(userId: Int): Completable {
        return networkClient()
            .doOnSuccess { mNetworkClient.removeFromBanList(userId) }
            .ignoreElement()
    }

    fun disconnect() {
        Completable.fromRunnable(mNetworkClient::disconnect)
            .subscribeOn(Schedulers.io())
            .subscribe()
    }

    private fun checkConnection() {
        if (!mNetworkClient.isConnected()) {
            mNetworkClient.disconnect()
            mNetworkClient.connect()
        }
    }

    fun sendFriendRequestResult(result: Boolean, userId: Int): Completable {
        return networkClient()
            .subscribeOn(Schedulers.io())
            .doOnSuccess { it.sendFriendRequestResult(result, userId) }
            .ignoreElement()
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