package ru.quandastudio.lpsclient.core

import ru.quandastudio.lpsclient.model.PlayerData
import ru.quandastudio.lpsclient.model.RequestType

open class LPSClientMessage {
    val action: String = (this::class.annotations.first { it is Action } as Action).name

    @Action("login")
    data class LPSLogIn(
        val version: Int = 5,
        val login: String,
        val clientBuild: Int,
        val clientVersion: String,
        val canReceiveMessages: Boolean,
        val firebaseToken: String,
        val uid: Int,
        val hash: String
    ) : LPSClientMessage() {
        constructor(pd: PlayerData, fbToken: String) : this(
            login = pd.authData.login,
            uid = pd.authData.credentials.userId,
            hash = pd.authData.credentials.hash,
            clientBuild = pd.clientBuild,
            clientVersion = pd.clientVersion,
            canReceiveMessages = pd.canReceiveMessages,
            firebaseToken = fbToken
        )
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
        val result: Int,
        val oppUid: Int
    ) : LPSClientMessage()

    @Action("admin")
    data class LPSAdmin(
        val command: String
    ) : LPSClientMessage()

    @Action("leave")
    data class LPSLeave(val reason: String? = null) : LPSClientMessage()
}
