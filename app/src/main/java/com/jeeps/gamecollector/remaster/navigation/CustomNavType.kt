package com.jeeps.gamecollector.remaster.navigation

import android.net.Uri
import androidx.navigation.NavType
import androidx.savedstate.SavedState
import com.jeeps.gamecollector.remaster.data.model.data.games.Game
import com.jeeps.gamecollector.remaster.data.model.data.platforms.Platform
import kotlinx.serialization.json.Json

object CustomNavType {

    val PlatformType = object : NavType<Platform?>(
        isNullableAllowed = true
    ) {
        override fun get(
            bundle: SavedState,
            key: String
        ): Platform? {
            return Json.decodeFromString(bundle.getString(key) ?: return null)
        }

        override fun put(
            bundle: SavedState,
            key: String,
            value: Platform?
        ) {
            bundle.putString(key, Json.encodeToString(value))
        }

        override fun parseValue(value: String): Platform? {
            return Json.decodeFromString(Uri.decode(value))
        }

        override fun serializeAsValue(value: Platform?): String {
            return Uri.encode(Json.encodeToString(value))
        }
    }

    val GameType = object : NavType<Game?>(
        isNullableAllowed = true
    ) {
        override fun get(
            bundle: SavedState,
            key: String
        ): Game? {
            return Json.decodeFromString(bundle.getString(key) ?: return null)
        }

        override fun put(
            bundle: SavedState,
            key: String,
            value: Game?
        ) {
            bundle.putString(key, Json.encodeToString(value))
        }

        override fun parseValue(value: String): Game? {
            return Json.decodeFromString(Uri.decode(value))
        }

        override fun serializeAsValue(value: Game?): String {
            return Uri.encode(Json.encodeToString(value))
        }
    }
}