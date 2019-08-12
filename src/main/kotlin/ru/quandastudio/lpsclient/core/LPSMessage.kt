package ru.quandastudio.lpsclient.core

import ru.quandastudio.lpsclient.LPSException
import ru.quandastudio.lpsclient.model.*
import java.nio.ByteBuffer
import kotlin.collections.ArrayList

sealed class LPSMessage {

    companion object {

        fun from(msgReader: LPSMessageReader): LPSMessage {
            return when (val action = msgReader.getMasterTag()) {
                LPSv3Tags.ACTION_JOIN -> LPSPlayMessage(msgReader)
                LPSv3Tags.ACTION_SYNC -> LPSSyncMessage(msgReader, action)
                LPSv3Tags.S_ACTION_WORD -> LPSWordMessage(msgReader, action)
                LPSv3Tags.S_ACTION_MSG -> LPSMsgMessage(msgReader, action)
                LPSv3Tags.S_ACTION_LEAVE -> LPSLeaveMessage(msgReader, action)
                LPSv3Tags.ACTION_TIMEOUT -> LPSTimeoutMessage
                LPSv3Tags.ACTION_BANNED -> LPSBannedMessage(msgReader, action)
                LPSv3Tags.ACTION_QUERY_BANLIST_RES -> LPSBannedListMessage(
                    msgReader,
                    action
                )
                LPSv3Tags.ACTION_FRIEND_MODE_REQ -> LPSFriendModeRequest(
                    msgReader,
                    action
                )
                LPSv3Tags.ACTION_FRIEND_REQUEST -> LPSFriendRequest(
                    msgReader,
                    action
                )
                LPSv3Tags.ACTION_QUERY_FRIEND_RES -> LPSFriendsList(
                    msgReader,
                    action
                )
                else -> LPSUnknownMessage
            }
        }
    }

    class LPSPlayMessage internal constructor(msgReader: LPSMessageReader) : LPSMessage() {
        val opponentPlayer: PlayerData
        var youStarter = false
        var banned = false

        init {
            var canReceiveMessages = false
            var avatar: ByteArray? = null
            var login = ""
            var tag = msgReader.nextTag()

            var clientVersion = "unk"
            var clientBuild = 80
            var isFriend = false

            var userID = 0
            var snUID = "0"
            var snType = AuthType.Native

            while (tag > 0) {
                when (tag) {
                    LPSv3Tags.ACTION_JOIN -> youStarter = msgReader.readBoolean(tag)
                    LPSv3Tags.S_CAN_REC_MSG -> canReceiveMessages = msgReader.readBoolean(tag)
                    LPSv3Tags.S_AVATAR_PART0 -> avatar = msgReader.readBytes(tag)
                    LPSv3Tags.OPP_LOGIN -> login = msgReader.readString(tag)
                    LPSv3Tags.OPP_CLIENT_VERSION -> clientVersion = msgReader.readString(tag)
                    LPSv3Tags.OPP_CLIENT_BUILD -> clientBuild = msgReader.readChar(tag)
                    LPSv3Tags.OPP_IS_FRIEND -> isFriend = msgReader.readBoolean(tag)
                    LPSv3Tags.S_OPP_UID -> userID = msgReader.readInt(tag)
                    LPSv3Tags.S_OPP_SN -> snType = AuthType.from(msgReader.readByte(tag).toInt())
                    LPSv3Tags.S_OPP_SNUID -> snUID = msgReader.readString(tag)
                    LPSv3Tags.S_BANNED_BY_OPP -> banned = msgReader.readBoolean(tag)
                }
                tag = msgReader.nextTag()
            }
            opponentPlayer = PlayerData(AuthData(login, snUID, snType, "").apply { this.userID = userID }).apply {
                this.canReceiveMessages = canReceiveMessages
                this.avatar = avatar
                this.clientVersion = clientVersion
                this.clientBuild = clientBuild
                this.isFriend = isFriend
                allowSendUID = true
            }
        }

    }

    class LPSSyncMessage internal constructor(msgReader: LPSMessageReader, action: Byte) : LPSMessage() {
        init {
            //Skip: time in seconds
            msgReader.readChar(action)
        }
    }

    class LPSWordMessage internal constructor(msgReader: LPSMessageReader, action: Byte) : LPSMessage() {
        val result = WordResult.from(msgReader.readByte(action).toInt())
        val word = msgReader.readString(LPSv3Tags.WORD)
    }

    class LPSMsgMessage internal constructor(msgReader: LPSMessageReader, action: Byte) : LPSMessage() {
        val message = msgReader.readString(action)
        val isSystemMsg = msgReader.readBoolean(LPSv3Tags.MSG_OWNER)
    }

    class LPSLeaveMessage internal constructor(msgReader: LPSMessageReader, action: Byte) : LPSMessage() {
        val leaved = msgReader.readBoolean(action)
    }

    class LPSBannedMessage internal constructor(msgReader: LPSMessageReader, action: Byte) : LPSMessage() {
        val isBannedBySystem = msgReader.readByte(action) == 2.toByte()
        val description = msgReader.optString(LPSv3Tags.S_BAN_REASON)
    }

    class LPSBannedListMessage internal constructor(msgReader: LPSMessageReader, action: Byte) : LPSMessage() {
        val list: ArrayList<BlackListItem>

        init {
            val size = msgReader.readChar(action)
            val names =
                msgReader.readString(LPSv3Tags.F_QUERY_NAMES).split("\\|\\|".toRegex()).dropLastWhile { it.isEmpty() }
                    .toTypedArray()
            val userIds = ByteBuffer.wrap(msgReader.readBytes(LPSv3Tags.F_QUERY_USER_IDS))

            list = ArrayList(size)
            for (i in 0 until size) {
                list.add(BlackListItem(names[i], userIds.int))
            }

            userIds.clear()
        }
    }

    class LPSFriendModeRequest internal constructor(msgReader: LPSMessageReader, action: Byte) : LPSMessage() {
        val login = msgReader.optString(LPSv3Tags.FRIEND_MODE_REQ_LOGIN)
        val userId = msgReader.optInt(LPSv3Tags.FRIEND_MODE_REQ_UID)
        val result = FriendModeResult.from(msgReader.readByte(action).toInt())
    }

    enum class FriendRequest { NEW_REQUEST, ACCEPTED, DENIED }

    class LPSFriendRequest internal constructor(msgReader: LPSMessageReader, action: Byte) : LPSMessage() {
        val requestResult =
            when (msgReader.readByte(action)) {
                LPSv3Tags.E_NEW_REQUEST -> FriendRequest.NEW_REQUEST
                LPSv3Tags.E_FRIEND_SAYS_YES -> FriendRequest.ACCEPTED
                LPSv3Tags.E_FRIEND_SAYS_NO -> FriendRequest.DENIED
                else -> throw LPSException("Invalid friend request")
            }
    }

    class LPSFriendsList internal constructor(msgReader: LPSMessageReader, action: Byte) : LPSMessage() {
        val list: ArrayList<FriendInfo>

        init {
            val size = msgReader.readChar(action)
            val names =
                msgReader.readString(LPSv3Tags.F_QUERY_NAMES).split("\\|\\|".toRegex()).dropLastWhile { it.isEmpty() }
                    .toTypedArray()
            val accept = msgReader.readBytes(LPSv3Tags.F_QUERY_USER_ACCEPT)
            val userIds = ByteBuffer.wrap(msgReader.readBytes(LPSv3Tags.F_QUERY_USER_IDS))

            list = ArrayList(size)
            for (i in 0 until size) {
                list.add(FriendInfo(userIds.int, names[i], accept[i] > 0))
            }

            userIds.clear()
        }
    }

    object LPSTimeoutMessage : LPSMessage()

    object LPSDisconnectMessage : LPSMessage()

    object LPSUnknownMessage : LPSMessage()
}