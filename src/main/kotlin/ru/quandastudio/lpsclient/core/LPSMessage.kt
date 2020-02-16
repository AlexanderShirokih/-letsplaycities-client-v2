package ru.quandastudio.lpsclient.core

import ru.quandastudio.lpsclient.model.*

open class LPSMessage {
    val action: String = (this::class.annotations.first { it is Action } as Action).name

    @Action("logged_in")
    data class LPSLoggedIn(
        val newerBuild: Int,
        val picHash: String
    ) : LPSMessage()

    @Action("login_error")
    data class LPSBanned(
        val banReason: String
    ) : LPSMessage()

    @Action("join")
    data class LPSPlayMessage(
        val canReceiveMessages: Boolean,
        val login: String,
        var oppUid: Int,
        var clientVersion: String,
        var clientBuild: Int,
        var isFriend: Boolean,
        var authType: AuthType,
        var youStarter: Boolean,
        val pictureHash: String?,
        var banned: Boolean = false
    ) : LPSMessage() {

        fun getPlayerData() = PlayerData(
            AuthData(login, authType, oppUid, ""),
            canReceiveMessages = canReceiveMessages,
            clientVersion = clientVersion,
            clientBuild = clientBuild,
            isFriend = isFriend,
            pictureHash = pictureHash
        )
    }

    @Action("word")
    data class LPSWordMessage(
        val result: WordResult,
        val word: String
    ) : LPSMessage()

    @Action("msg")
    data class LPSMsgMessage(
        val msg: String,
        val isSystemMsg: Boolean
    ) : LPSMessage()

    @Action("leave")
    data class LPSLeaveMessage(val leaved: Boolean) : LPSMessage()

    @Action("banned")
    data class LPSBannedMessage(
        val isBannedBySystem: Boolean = true,
        val description: String = ""
    ) : LPSMessage()

    @Action("fm_request")
    data class LPSFriendModeRequest(
        val login: String? = null,
        val oppUid: Int? = null,
        val result: FriendModeResult
    ) : LPSMessage()

    enum class FriendRequest { NEW_REQUEST, ACCEPTED, DENIED }

    @Action("friend_request")
    data class LPSFriendRequest(
        val result: FriendRequest,
        val uid: Int,
        val login: String
    ) : LPSMessage()

    @Action("timeout")
    object LPSTimeoutMessage : LPSMessage()

    @Action
    object LPSUnknownMessage : LPSMessage()

    @Action
    object LPSConnectedMessage : LPSMessage()

}