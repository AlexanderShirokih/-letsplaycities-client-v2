package ru.quandastudio.lpsclient.model

data class HistoryInfo(
    val userId: Int,
    val login: String,
    val isFriend: Boolean,
    val startTime: Long,
    val duration: Int,
    val wordsCount: Int,
    val pictureHash: String
)