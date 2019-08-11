package ru.quandastudio.lpsclient.model

enum class FriendModeResult {
    BUSY,
    OFFLINE,
    NOT_FRIEND,
    DENIED;

    companion object {
        fun from(index: Int) = values()[index]
    }
}

