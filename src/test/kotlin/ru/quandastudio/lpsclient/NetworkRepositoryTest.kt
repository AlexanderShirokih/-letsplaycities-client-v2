package ru.quandastudio.lpsclient

import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.junit.Test
import ru.quandastudio.lpsclient.core.NetworkClient
import ru.quandastudio.lpsclient.model.PlayerData

internal class NetworkRepositoryTest {

    @Test
    fun testPlayGame() {
        val networkClient = NetworkClient(false, "localhost")
        val repository = NetworkRepository(networkClient, Single.just("test"))
        val playerData = createPlayerData()

        val it = repository.login(playerData)
            .doOnSubscribe { println("Connecting to server...") }
            .doOnNext { println("LoggedIn: ${it.authData}") }
            .doOnNext { println("Waiting for opponent...") }
            .observeOn(Schedulers.io())
            .flatMap { repository.play(false, null) }
            .observeOn(Schedulers.single())
            .blockingFirst()
        println("Play as=${playerData.authData.login}, with=${it.first.authData.login}, starter=${it.second}")
    }

    private fun createPlayerData(): PlayerData {
        return PlayerData.Factory().create("UnitTest")
    }

}