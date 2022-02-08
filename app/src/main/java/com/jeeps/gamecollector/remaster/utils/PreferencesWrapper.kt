package com.jeeps.gamecollector.remaster.utils

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.jeeps.gamecollector.R

object PreferencesWrapper {

    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        prefs = context.getSharedPreferences(
            context.getString(R.string.shared_preferences_global),
            Context.MODE_PRIVATE
        )
    }

    fun save(key: String, value: Any) {
        val gson = Gson()
        val json = gson.toJson(value)
        prefs.edit()
            .putString(key, json)
            .apply()
    }

    fun <T> read(key: String, clazz: Class<T>): T? {
        val gson = Gson()
        val json = prefs.getString(key, "")
        return gson.fromJson(json, clazz)
    }
}