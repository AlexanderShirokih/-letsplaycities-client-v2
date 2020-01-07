package ru.quandastudio.lpsclient

import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.junit.Test
import ru.quandastudio.lpsclient.core.Base64JDK8Impl
import ru.quandastudio.lpsclient.core.NetworkClient
import ru.quandastudio.lpsclient.model.PlayerData

internal class NetworkRepositoryTest {

    @Test
    fun testPlayGame() {
        val networkClient = NetworkClient(Base64JDK8Impl(), false, NetworkClient.ConnectionType.PureSocket, "localhost")
        val repository = NetworkRepository(networkClient, Single.just("test"))
        val playerData = createPlayerData(true)

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

    private fun createPlayerData(useAvatar: Boolean): PlayerData {
        val playerData = PlayerData.Factory().create("UnitTest")
        val resourceStream = if (useAvatar) ClassLoader.getSystemResourceAsStream("test-avatar.png") else null
        resourceStream?.run {
            println("Reading avatar")
            playerData.avatar = readAllBytes()
        }
        return playerData
    }

}