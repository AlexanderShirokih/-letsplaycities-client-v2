package ru.quandastudio.lpsclient.core

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type

class LPSMessageDeserializer : JsonDeserializer<LPSMessage> {
    private val mapping = mapOf(
        "logged_in" to LPSMessage.LPSLoggedIn::class.java,
        "login_error" to LPSMessage.LPSBanned::class.java,
        "join" to LPSMessage.LPSPlayMessage::class.java,
        "word" to LPSMessage.LPSWordMessage::class.java,
        "msg" to LPSMessage.LPSMsgMessage::class.java,
        "leave" to LPSMessage.LPSLeaveMessage::class.java,
        "banned" to LPSMessage.LPSBannedMessage::class.java,
        "banlist" to LPSMessage.LPSBannedListMessage::class.java,
        "friends" to LPSMessage.LPSFriendsList::class.java,
        "fm_request" to LPSMessage.LPSFriendModeRequest::class.java,
        "friend_request" to LPSMessage.LPSFriendRequest::class.java,
        "timeout" to LPSMessage.LPSTimeoutMessage::class.java
    )

    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): LPSMessage {
        println("INPUT: $json")

        val action = json.asJsonObject["action"].asString
        val clz = mapping[action]
        val res: LPSMessage = if (clz != null)
            context.deserialize(json, clz)
        else
            LPSMessage.LPSUnknownMessage
        println("RES: $res")
        return res
    }

}
