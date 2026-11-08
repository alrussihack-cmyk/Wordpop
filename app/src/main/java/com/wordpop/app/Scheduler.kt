package com.wordpop.app

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.core.content.ContextCompat
import java.util.Calendar

object Scheduler {
    const val ACTION_TICK = "com.wordpop.app.TICK"

    /** Schedules the next word after the user's interval, skipping hours outside the active window. */
    fun scheduleNext(ctx: Context, from: Long = System.currentTimeMillis()) {
        val at = nextTime(SettingsStore(ctx), from)
        val am = ctx.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pending(ctx))
    }

    fun cancel(ctx: Context) {
        val am = ctx.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        am.cancel(pending(ctx))
    }

    private fun pending(ctx: Context): PendingIntent {
        val i = Intent(ctx, TickReceiver::class.java).setAction(ACTION_TICK)
        return PendingIntent.getBroadcast(
            ctx, 1, i, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    fun isActiveHour(s: SettingsStore, hour: Int): Boolean {
        val a = s.startHour
        val b = s.endHour
        return when {
            a == b -> true
            a < b -> hour in a until b
            else -> hour >= a || hour < b
        }
    }

    fun nextTime(s: SettingsStore, from: Long): Long {
        var t = from + s.intervalMin * 60_000L
        val cal = Calendar.getInstance().apply { timeInMillis = t }
        if (!isActiveHour(s, cal.get(Calendar.HOUR_OF_DAY))) {
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            var guard = 0
            while (!isActiveHour(s, cal.get(Calendar.HOUR_OF_DAY)) && guard < 48) {
                cal.add(Calendar.HOUR_OF_DAY, 1)
                guard++
            }
            t = cal.timeInMillis
        }
        return t
    }
}

fun sendToService(ctx: Context, action: String) {
    try {
        ContextCompat.startForegroundService(
            ctx, Intent(ctx, OverlayService::class.java).setAction(action)
        )
    } catch (e: Exception) {
        // Starting a foreground service from the background can be refused by the system.
    }
}

class TickReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        try {
            ContextCompat.startForegroundService(
                context,
                Intent(context, OverlayService::class.java).setAction(OverlayService.ACTION_TICK)
            )
        } catch (e: Exception) {
            // Keep the chain alive so the next tick can try again.
            if (SettingsStore(context).enabled) Scheduler.scheduleNext(context)
        }
    }
}

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val s = SettingsStore(context)
        if (s.enabled && Settings.canDrawOverlays(context)) {
            sendToService(context, OverlayService.ACTION_START)
        }
    }
}
