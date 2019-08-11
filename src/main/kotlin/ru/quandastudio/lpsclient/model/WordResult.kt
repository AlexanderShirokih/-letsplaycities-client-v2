package ru.quandastudio.lpsclient.model

enum class WordResult {
    RECEIVED, ACCEPTED, ALREADY, NO_WORD, WRONG_MOVE;

    companion object {
        fun from(index: Int) = values()[index]
    }
}
