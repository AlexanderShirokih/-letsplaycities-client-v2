package ru.quandastudio.lpsclient

import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.junit.Test
import ru.quandastudio.lpsclient.core.LPSMessage
import ru.quandastudio.lpsclient.core.NetworkClient
import ru.quandastudio.lpsclient.model.PlayerData
import java.util.concurrent.TimeUnit

internal class NetworkRepositoryTest {

    @Test
    fun test() {
        val shared = Observable.just(0)
            .subscribeOn(Schedulers.io())
            .doOnSubscribe { dis-> println("REAL Subscribed! to $dis" ) }
            .doOnDispose { println("REAL Disposed!!!!") }
            .publish().refCount(1, TimeUnit.SECONDS)
            .doOnSubscribe { dis-> println("Subscribed! to $dis" ) }
            .doOnDispose { println("Disposed!!!!") }
            .doOnNext { println("MSG=$it") }

        shared.filter { true }.subscribe()
        shared.filter { true }.subscribe()
    }

    @Test
    fun testPlayGame() {
        val networkClient = NetworkClient("localhost")
        val repository = NetworkRepository(networkClient, Single.just(""))
        val playerData = createPlayerData()

        val it = repository.login(playerData)
            .doOnSubscribe { println("Connecting to server...") }
            .doOnSuccess { println("Waiting for opponent...") }
            .observeOn(Schedulers.io())
            .flatMapMaybe { repository.play(false, null) }
            .observeOn(Schedulers.single())
            .blockingGet()
        println("Play as=${playerData.authData.login}, with=${it.first.authData.login}, starter=${it.second}")
    }

    private fun createPlayerData(): PlayerData {
        return PlayerData.Factory().create("UnitTest")
    }

}