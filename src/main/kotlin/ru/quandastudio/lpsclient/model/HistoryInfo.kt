package ru.quandastudio.lpsclient.model

import java.sql.Timestamp

data class HistoryInfo(
    val userId: Int,
    val login: String,
    val isFriend: Boolean,
    val creationDate: Timestamp,
    val duration: Int,
    val wordsCount: Int,
    val pictureHash: String?
)