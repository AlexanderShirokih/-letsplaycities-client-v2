package ru.quandastudio.lpsclient.core

import kotlin.collections.ArrayList
import ru.quandastudio.lpsclient.model.*
import ru.quandastudio.lpsclient.core.Base64Ext.decodeBase64
import ru.quandastudio.lpsclient.core.Base64Ext.encodeBase64

open class LPSMessage {
    val action: String = (this::class.annotations.first { it is Action } as Action).name

    @Action("logged_in")
    data class LPSLoggedIn(
        val userId: Int,
        val accHash: String,
        val newerBuild: Int
    ) : LPSMessage()

    @Action("login_error")
    data class LPSBanned(
        val banReason: String? = null,
        val connError: String? = null
    ) : LPSMessage()

    @Action("join")
    data class LPSPlayMessage(
        val canReceiveMessages: Boolean,
        val login: String,
        var avatar: String? = null,
        var oppUid: Int,
        var clientVersion: String,
        var clientBuild: Int,
        var isFriend: Boolean,
        var snUID: String?,
        var authType: AuthType?,
        var youStarter: Boolean,
        var banned: Boolean = false
    ) : LPSMessage() {

        fun getPlayerData() = PlayerData(
            AuthData(login, snUID ?: "", authType ?: AuthType.Native, "", userID = oppUid),
            avatar = avatar?.decodeBase64(),
            canReceiveMessages = canReceiveMessages,
            clientVersion = clientVersion,
            clientBuild = clientBuild,
            isFriend = isFriend,
            allowSendUID = true
        )

        fun setAvatar(data: ByteArray?): LPSPlayMessage {
            avatar = data?.encodeBase64()
            return this
        }
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

    @Action("banlist")
    data class LPSBannedListMessage(val data: List<BlackListItem>) : LPSMessage()

    @Action("friends")
    data class LPSFriendsList(
        val data: ArrayList<FriendInfo>
    ) : LPSMessage()

    @Action("history")
    data class LPSHistoryList(
        val data: List<HistoryInfo>
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