package ru.quandastudio.lpsclient.core

import ru.quandastudio.lpsclient.model.*
import java.util.*
import kotlin.collections.ArrayList

sealed class LPSMessage {

    data class LPSLoggedIn(
        val userId: Int,
        val accHash: String,
        val newerBuild: Int
    ) : LPSMessage()

    data class LPSBanned(
        val banReason: String? = null,
        val connError: String? = null
    ) : LPSMessage()

    data class LPSPlayMessage(
        val canReceiveMessages: Boolean,
        val avatar: String? = null,
        val login: String,
        var oppUid: Int,
        var clientVersion: String,
        var clientBuild: Int,
        var isFriend: Boolean,
        var snUID: String?,
        var authType: AuthType?,
        var youStarter: Boolean,
        var banned: Boolean
    ) : LPSMessage() {

        private fun String.decodeBase64(): ByteArray = Base64.getDecoder().decode(this)

        fun getPlayerData() = PlayerData(
            AuthData(login, snUID ?: "", authType ?: AuthType.Native, "", userID = oppUid),
            avatar = avatar?.decodeBase64(),
            canReceiveMessages = canReceiveMessages,
            clientVersion = clientVersion,
            clientBuild = clientBuild,
            isFriend = isFriend,
            allowSendUID = true
        )
    }

    data class LPSWordMessage(
        val result: WordResult,
        val word: String
    ) : LPSMessage()

    data class LPSMsgMessage(
        val msg: String,
        val isSystemMsg: Boolean
    ) : LPSMessage()

    data class LPSLeaveMessage(val leaved: Boolean) : LPSMessage()

    data class LPSBannedMessage(
        val isBannedBySystem: Boolean = true,
        val description: String = ""
    ) : LPSMessage()

    data class LPSBannedListMessage(val list: List<BlackListItem>) : LPSMessage()

    data class LPSFriendsList(val list: ArrayList<FriendInfo>) : LPSMessage()

    data class LPSFriendModeRequest(
        val login: String? = null,
        val oppUid: Int? = null,
        val result: FriendModeResult
    ) : LPSMessage()

    enum class FriendRequest { NEW_REQUEST, ACCEPTED, DENIED }

    data class LPSFriendRequest(
        val requestResult: FriendRequest
    ) : LPSMessage()

    object LPSUnknownMessage : LPSMessage()

    object LPSTimeoutMessage : LPSMessage()

    object LPSConnectedMessage : LPSMessage()

}