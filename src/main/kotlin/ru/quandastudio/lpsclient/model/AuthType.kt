package ru.quandastudio.lpsclient.model

enum class AuthType constructor(var snName: String) {

    Native("nv"), Google("gl"), Vkontakte("vk"), Odnoklassniki("ok"), Facebook("fb");

    fun type(): String {
        return snName
    }

    companion object {
        fun from(type: String): AuthType {
            for (v in values()) {
                if (v.snName == type) return v
            }
            throw IllegalArgumentException("Invalid AuthType value=$type")
        }

        fun from(index: Int) = values()[index]
    }
}