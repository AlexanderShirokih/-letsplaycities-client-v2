package ru.quandastudio.lpsclient.core

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type

class LPSMessageDeserializer : JsonDeserializer<LPSMessage> {
    private val lpsMessages = LPSMessage::class.sealedSubclasses

    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): LPSMessage {
        val action = json.asJsonObject["action"].asString

        val clz = lpsMessages.firstOrNull() {
            val annotation = (it.annotations.first { a -> a is Action } as Action)
            annotation.name.isNotEmpty() && annotation.name == action
        }

        return if (clz != null)
            context.deserialize(json, clz.java)
        else
            LPSMessage.LPSUnknownMessage
    }

}
