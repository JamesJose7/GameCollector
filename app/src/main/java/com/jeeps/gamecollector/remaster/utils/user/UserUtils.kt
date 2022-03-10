package com.jeeps.gamecollector.remaster.utils.user

import kotlin.random.Random

object UserUtils {
    fun generateRandomUsername(): String {
        return "user${Random.nextInt(999999)}"
    }

    fun convertEmailToUsername(email: String): String {
        return email
            .split("@")[0]
            .replaceAfterLast("\\.", "")
    }
}
