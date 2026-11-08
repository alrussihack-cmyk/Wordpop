package com.wordpop.app

import android.content.Context

class SettingsStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences("wordpop", Context.MODE_PRIVATE)

    var enabled: Boolean
        get() = prefs.getBoolean("enabled", false)
        set(v) = prefs.edit().putBoolean("enabled", v).apply()

    /** Minutes between two words. */
    var intervalMin: Int
        get() = prefs.getInt("interval", 60)
        set(v) = prefs.edit().putInt("interval", v).apply()

    var startHour: Int
        get() = prefs.getInt("startHour", 9)
        set(v) = prefs.edit().putInt("startHour", v).apply()

    var endHour: Int
        get() = prefs.getInt("endHour", 22)
        set(v) = prefs.edit().putInt("endHour", v).apply()

    /** 0 = words and phrases, 1 = words only, 2 = phrases only. */
    var content: Int
        get() = prefs.getInt("content", 0)
        set(v) = prefs.edit().putInt("content", v).apply()

    /** Seconds the banner stays on screen. */
    var displaySec: Int
        get() = prefs.getInt("displaySec", 10)
        set(v) = prefs.edit().putInt("displaySec", v).apply()

    var autoSpeak: Boolean
        get() = prefs.getBoolean("autoSpeak", false)
        set(v) = prefs.edit().putBoolean("autoSpeak", v).apply()

    var lastWordId: Int
        get() = prefs.getInt("lastWordId", -1)
        set(v) = prefs.edit().putInt("lastWordId", v).apply()

    var learned: Set<Int>
        get() = (prefs.getStringSet("learned", null) ?: emptySet()).mapNotNull { it.toIntOrNull() }.toSet()
        set(v) = prefs.edit().putStringSet("learned", v.map { it.toString() }.toSet()).apply()

    fun markLearned(id: Int) {
        learned = learned + id
    }

    /** Words the user saved from the banner, got wrong in a quiz, or flagged as hard. */
    var hard: Set<Int>
        get() = (prefs.getStringSet("hard", null) ?: emptySet()).mapNotNull { it.toIntOrNull() }.toSet()
        set(v) = prefs.edit().putStringSet("hard", v.map { it.toString() }.toSet()).apply()

    fun addHard(id: Int) {
        hard = hard + id
    }

    fun removeHard(id: Int) {
        hard = hard - id
    }

    fun toggleHard(id: Int) {
        if (id in hard) removeHard(id) else addHard(id)
    }

    /** Optional Claude API key, only used to write new stories. Stays on this phone. */
    var aiKey: String
        get() = prefs.getString("aiKey", "") ?: ""
        set(v) = prefs.edit().putString("aiKey", v).apply()
}

fun intervalLabel(min: Int): String = when (min) {
    15 -> "كل 15 دقيقة"
    30 -> "كل نص ساعة"
    60 -> "كل ساعة"
    120 -> "كل ساعتين"
    180 -> "كل 3 ساعات"
    360 -> "كل 6 ساعات"
    else -> "كل $min دقيقة"
}

fun hourLabel(h: Int): String = when {
    h == 0 -> "12 ص"
    h < 12 -> "$h ص"
    h == 12 -> "12 م"
    else -> "${h - 12} م"
}
