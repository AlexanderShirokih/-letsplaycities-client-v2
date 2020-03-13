package ru.quandastudio.lpsclient.model

/**
 * Connection results.
 * @see ru.quandastudio.lpsclient.NetworkRepository.connectToFriend
 * @see ru.quandastudio.lpsclient.NetworkRepository.play
 */
sealed class ConnectionResult {

    /**
     * Represents result when user successfully connects to opponent
     * @param oppData opponents [PlayerData]
     * @param isYouStarter `true` when player should make first move, `false` if opponent
     */
    class ConnectedToUser(
        val oppData: PlayerData,
        val isYouStarter: Boolean
    ) : ConnectionResult()

    /**
     * Represents result when server rejects friend game request by any reason
     * @param reason rejection reason
     * @param login opponent's login, `null` for [FriendModeResult.NO_USER]
     */
    class FriendModeRejected(
        val reason: FriendModeResult,
        val login: String?
    ) : ConnectionResult()
}