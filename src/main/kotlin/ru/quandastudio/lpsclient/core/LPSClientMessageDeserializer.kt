package ru.quandastudio.lpsclient.core

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type

class LPSClientMessageDeserializer : JsonDeserializer<LPSClientMessage> {
    private val lpsMessages = LPSClientMessage::class.sealedSubclasses

    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): LPSClientMessage {
        val action = json.asJsonObject["action"].asString

        val clz = lpsMessages.firstOrNull() {
            val annotation = (it.annotations.first { a -> a is Action } as Action)
            annotation.name.isNotEmpty() && annotation.name == action
        }

        return if (clz != null)
            context.deserialize(json, clz.java)
        else
            LPSClientMessage.LPSLeave("Deserialization error!")
    }

}