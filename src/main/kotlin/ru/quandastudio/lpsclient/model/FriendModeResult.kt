package ru.quandastudio.lpsclient.model

/**
 * Enum that represents rejection reason
 */
enum class FriendModeResult {
    /**
     * Opponent currently in game with other player.
     */
    BUSY,

    /**
     * Opponent not in a friendship with current player
     */
    NOT_FRIEND,

    /**
     * Opponent decline game request
     */
    DENIED,

    /**
     * Opponent not found or banned
     */
    NO_USER;
}

