package ru.quandastudio.lpsclient.core

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type
import kotlin.reflect.full.findAnnotation

class LPSClientMessageDeserializer : JsonDeserializer<LPSClientMessage> {
    private val lpsMessages = LPSClientMessage::class.nestedClasses

    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): LPSClientMessage {
        val action = json.asJsonObject.takeIf { it.has("action") }?.get("action")?.asString

        val clz = lpsMessages.firstOrNull() {
            val annotation = it.findAnnotation<Action>()
            annotation != null && annotation.name.isNotEmpty() && annotation.name == action
        }

        return if (clz != null)
            context.deserialize(json, clz.java)
        else
            LPSClientMessage.LPSLeave("Deserialization error!")
    }

}