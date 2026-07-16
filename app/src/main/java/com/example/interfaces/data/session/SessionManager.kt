package com.example.interfaces.data.session

import android.content.Context
import androidx.core.content.edit

object SessionManager {
    private const val PREFS_NAME = "SessionPrefs"
    private const val KEY_USER_ID = "session_user_id"
    private const val KEY_USER_NAME = "session_user_name"
    private const val KEY_USER_EMAIL = "session_user_email"

    fun save(context: Context, userId: Long, userName: String, userEmail: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putLong(KEY_USER_ID, userId)
            .putString(KEY_USER_NAME, userName)
            .putString(KEY_USER_EMAIL, userEmail)
            .apply()
    }

    fun getUserId(context: Context): Long {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getLong(KEY_USER_ID, -1L)
    }

    fun getUserName(context: Context): String {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_USER_NAME, "") ?: ""
    }

    fun clear(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .clear()
            .apply()
    }
}
