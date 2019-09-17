package ru.quandastudio.lpsclient.core

import ru.quandastudio.lpsclient.model.AuthData
import ru.quandastudio.lpsclient.model.AuthType
import ru.quandastudio.lpsclient.model.PlayerData
import ru.quandastudio.lpsclient.core.Base64Ext.decodeBase64

sealed class LPSClientMessage {
    val action: String = (this::class.annotations.first { it is Action } as Action).name

    @Action("login")
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
    ) : LPSClientMessage() {
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

        fun getPlayerData(): PlayerData =
            PlayerData(AuthData(login, snUID, authType, ""))
                .also { pd ->
                    pd.canReceiveMessages = canReceiveMessages
                    pd.clientVersion = clientVersion
                    pd.clientBuild = clientBuild
                    pd.avatar = avatar?.decodeBase64()
                }
    }

    enum class PlayMode {
        RANDOM_PAIR,
        FRIEND
    }

    @Action("play")
    data class LPSPlay(
        val mode: PlayMode,
        val oppUid: Int?
    ) : LPSClientMessage()

    @Action("banlist")
    data class LPSBanList(
        val type: RequestType,
        val friendUid: Int? = null
    ) : LPSClientMessage()

    @Action("friends_list")
    object LPSFriendList : LPSClientMessage()

    enum class RequestType {
        QUERY_LIST,
        SEND,
        DELETE,
        ACCEPT,
        DENY
    }

    @Action("friend")
    data class LPSFriendAction(
        val type: RequestType,
        val oppUid: Int? = null
    ) : LPSClientMessage()

    @Action("ban")
    data class LPSBan(
        val type: String = "report",
        val targetId: Int? = null
    ) : LPSClientMessage()

    @Action("word")
    data class LPSWord(
        val word: String
    ) : LPSClientMessage()

    @Action("msg")
    data class LPSMsg(
        val msg: String
    ) : LPSClientMessage()

    @Action("fm_req_result")
    data class LPSFriendMode(
        val res: Int,
        val oppUid: Int
    ) : LPSClientMessage()

    @Action("leave")
    data class LPSLeave(val reason: String? = null) : LPSClientMessage()
}
