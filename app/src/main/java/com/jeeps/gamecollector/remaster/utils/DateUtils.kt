package com.jeeps.gamecollector.remaster.utils

import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

fun getCurrentTimeInUtcString(): String {
    val pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
    val nowInUtc = OffsetDateTime.now(ZoneOffset.UTC)
    return nowInUtc.format(
        DateTimeFormatter.ofPattern(pattern)
    )
}