package ru.quandastudio.lpsclient.core

import kotlin.collections.ArrayList
import ru.quandastudio.lpsclient.model.*
import ru.quandastudio.lpsclient.core.Base64Ext.decodeBase64
import ru.quandastudio.lpsclient.core.Base64Ext.encodeBase64

sealed class LPSMessage(val action: String) {

    data class LPSLoggedIn(
        val userId: Int,
        val accHash: String,
        val newerBuild: Int
    ) : LPSMessage("logged_in")

    data class LPSBanned(
        val banReason: String? = null,
        val connError: String? = null
    ) : LPSMessage("login_error")

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
    ) : LPSMessage("join") {

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

    data class LPSWordMessage(
        val result: WordResult,
        val word: String
    ) : LPSMessage("word")

    data class LPSMsgMessage(
        val msg: String,
        val isSystemMsg: Boolean
    ) : LPSMessage("msg")

    data class LPSLeaveMessage(val leaved: Boolean) : LPSMessage("leave")

    data class LPSBannedMessage(
        val isBannedBySystem: Boolean = true,
        val description: String = ""
    ) : LPSMessage("banned")

    data class LPSBannedListMessage(val list: List<BlackListItem>) : LPSMessage("banlist")

    data class LPSFriendsList(val list: ArrayList<FriendInfo>) : LPSMessage("friends")

    data class LPSFriendModeRequest(
        val login: String? = null,
        val oppUid: Int? = null,
        val result: FriendModeResult
    ) : LPSMessage("fm_request")

    enum class FriendRequest { NEW_REQUEST, ACCEPTED, DENIED }

    data class LPSFriendRequest(
        val requestResult: FriendRequest
    ) : LPSMessage("friend_request")

    object LPSTimeoutMessage : LPSMessage("timeout")

    object LPSUnknownMessage : LPSMessage("")

    object LPSConnectedMessage : LPSMessage("")

}