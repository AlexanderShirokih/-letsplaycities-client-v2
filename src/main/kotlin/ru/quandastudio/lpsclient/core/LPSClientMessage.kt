package ru.quandastudio.lpsclient.core

import ru.quandastudio.lpsclient.model.AuthType
import ru.quandastudio.lpsclient.model.PlayerData

sealed class LPSClientMessage(val action: String) {

    data class LPSLogIn(
        val version: Int,
        val login: String,
        val accToken: String,
        val authType: AuthType,
        val snUID: String,
        val clientBuild: Int,
        val clientVersion: String,
        val canReceiveMessages: Boolean,
        val allowSendUID: Boolean,
        val firebaseToken: String,
        val uid: Int?,
        val hash: String?,
        val avatar: String?
    ) : LPSClientMessage("login") {
        constructor(pd: PlayerData, fbToken: String, userId: Int?, hash: String?, avatar: String?) : this(
            version = 4,
            login = pd.authData.login,
            accToken = pd.authData.accessToken,
            authType = pd.authData.snType,
            snUID = pd.authData.snUID,
            clientBuild = pd.clientBuild,
            clientVersion = pd.clientVersion,
            canReceiveMessages = pd.canReceiveMessages,
            allowSendUID = pd.allowSendUID,
            firebaseToken = fbToken,
            uid = userId,
            hash = hash,
            avatar = avatar
        )
    }

    enum class PlayMode {
        RANDOM_PAIR,
        FRIEND
    }

    data class LPSPlay(
        val mode: PlayMode,
        val oppUid: Int?
    ) : LPSClientMessage("play")

    data class LPSBanList(
        val type: RequestType,
        val friendUid: Int? = null
    ) : LPSClientMessage("banlist")

    object LPSFriendList : LPSClientMessage("friends_list")

    enum class RequestType {
        QUERY_LIST,
        SEND,
        DELETE,
        ACCEPT,
        DENY
    }

    data class LPSFriendAction(
        val type: RequestType,
        val oppUid: Int? = null
    ) : LPSClientMessage("friend")

    data class LPSBan(
        val type: String = "report",
        val targetId: Int? = null
    ) : LPSClientMessage("ban")

    data class LPSWord(
        val word: String
    ) : LPSClientMessage("word")

    data class LPSMsg(
        val msg: String
    ) : LPSClientMessage("msg")

    data class LPSFriendMode(
        val res: Int,
        val oppUid: Int
    ) : LPSClientMessage("fm_req_result")
}
