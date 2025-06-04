package com.jeeps.gamecollector.deprecated.utils

import androidx.annotation.StringRes
import com.jeeps.gamecollector.R

enum class PlatformColor(
    val color: String,
    @StringRes val stringId: Int
) {
    NORMIE_WHITE("#ffffff", R.string.platform_color_normie_white),
    SWITCH_RED("#E60012", R.string.platform_color_switch_red),
    XBOX_GREEN("#107C10", R.string.platform_color_xbox_green),
    PLAYSTATION_BLUE("#0070D1", R.string.platform_color_playstation_blue);

    companion object {
        fun fromColor(color: String?): PlatformColor {
            return entries.firstOrNull { it.color == color } ?: NORMIE_WHITE
        }
    }
}
