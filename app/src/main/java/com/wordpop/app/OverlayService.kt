package com.wordpop.app

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.provider.Settings
import android.speech.tts.TextToSpeech
import android.text.TextUtils
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import java.util.Calendar
import java.util.Locale
import kotlin.math.abs

/**
 * Foreground service that shows a small notification-like banner over other apps
 * every N minutes. Tapping the banner opens the app on the same word.
 */
class OverlayService : Service() {

    companion object {
        const val ACTION_START = "com.wordpop.app.START"
        const val ACTION_TICK = "com.wordpop.app.SERVICE_TICK"
        const val ACTION_SHOW_NOW = "com.wordpop.app.SHOW_NOW"
        const val EXTRA_WORD_ID = "word_id"
        private const val CHANNEL_ID = "wordpop_service"
        private const val NOTIF_ID = 1
    }

    private lateinit var wm: WindowManager
    private val handler = Handler(Looper.getMainLooper())
    private var banner: View? = null
    private var bannerParams: WindowManager.LayoutParams? = null
    private var tts: TextToSpeech? = null
    private var ttsReady = false
    private val dismissRunnable = Runnable { finishBanner() }

    override fun onCreate() {
        super.onCreate()
        wm = getSystemService(WINDOW_SERVICE) as WindowManager
        createChannel()
        startAsForeground()
        tts = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.US
                ttsReady = true
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startAsForeground()
        val settings = SettingsStore(this)
        when (intent?.action) {
            ACTION_SHOW_NOW -> showWord()
            ACTION_TICK -> {
                if (settings.enabled) {
                    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
                    if (Scheduler.isActiveHour(settings, hour)) showWord()
                    Scheduler.scheduleNext(this)
                } else {
                    stopForeground(STOP_FOREGROUND_REMOVE)
                    stopSelf()
                    return START_NOT_STICKY
                }
            }
            else -> {
                // ACTION_START, or the system restarting the service after it was killed.
                if (settings.enabled) {
                    Scheduler.scheduleNext(this)
                } else if (banner == null) {
                    stopForeground(STOP_FOREGROUND_REMOVE)
                    stopSelf()
                    return START_NOT_STICKY
                }
            }
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        removeBanner()
        tts?.shutdown()
        tts = null
        super.onDestroy()
    }

    // ---------- foreground notification ----------

    private fun createChannel() {
        val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        nm.createNotificationChannel(
            NotificationChannel(CHANNEL_ID, "ووردبوب", NotificationManager.IMPORTANCE_LOW)
        )
    }

    private fun startAsForeground() {
        val s = SettingsStore(this)
        val open = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE
        )
        val n: Notification = Notification.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("ووردبوب شغّال")
            .setContentText("كلمة جديدة " + intervalLabel(s.intervalMin))
            .setContentIntent(open)
            .setOngoing(true)
            .build()
        if (Build.VERSION.SDK_INT >= 34) {
            startForeground(NOTIF_ID, n, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        } else {
            startForeground(NOTIF_ID, n)
        }
    }

    // ---------- banner ----------

    private fun dp(v: Int): Int = (v * resources.displayMetrics.density).toInt()

    private fun statusBarHeight(): Int {
        val id = resources.getIdentifier("status_bar_height", "dimen", "android")
        return if (id > 0) resources.getDimensionPixelSize(id) else dp(24)
    }

    private fun showWord() {
        if (!Settings.canDrawOverlays(this)) return
        val s = SettingsStore(this)
        val w = WordRepo.pick(s)
        removeBanner()

        val view = buildBanner(w)
        val margin = dp(12)
        val lp = WindowManager.LayoutParams(
            resources.displayMetrics.widthPixels - 2 * margin,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        )
        lp.gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
        lp.y = statusBarHeight() + dp(8)

        try {
            wm.addView(view, lp)
        } catch (e: Exception) {
            return
        }
        banner = view
        bannerParams = lp
        handler.postDelayed(dismissRunnable, s.displaySec * 1000L)
        if (s.autoSpeak) speak(w.en)
    }

    private fun buildBanner(w: Word): View {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            layoutDirection = View.LAYOUT_DIRECTION_RTL
            setPadding(dp(12), dp(10), dp(12), dp(10))
            background = GradientDrawable().apply {
                setColor(Color.WHITE)
                cornerRadius = dp(22).toFloat()
                setStroke(dp(1), 0xFFEBE8FD.toInt())
            }
            elevation = dp(8).toFloat()
        }

        val icon = ImageView(this).apply {
            setImageResource(R.drawable.ic_logo)
            layoutParams = LinearLayout.LayoutParams(dp(36), dp(36))
        }

        val texts = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply {
                marginStart = dp(12)
                marginEnd = dp(12)
            }
        }
        val en = TextView(this).apply {
            text = w.en
            textSize = 20f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(0xFF14143A.toInt())
            textDirection = View.TEXT_DIRECTION_LTR
            gravity = Gravity.START
            maxLines = 1
            ellipsize = TextUtils.TruncateAt.END
        }
        val ar = TextView(this).apply {
            text = w.ar
            textSize = 14f
            setTextColor(0xFF4A4A72.toInt())
            gravity = Gravity.START
            maxLines = 1
            ellipsize = TextUtils.TruncateAt.END
        }
        texts.addView(en)
        texts.addView(ar)

        val speaker = ImageView(this).apply {
            setImageResource(R.drawable.ic_volume)
            setColorFilter(Color.WHITE)
            background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(0xFF5B4BFF.toInt())
            }
            setPadding(dp(11), dp(11), dp(11), dp(11))
            contentDescription = "نطق الكلمة"
            layoutParams = LinearLayout.LayoutParams(dp(44), dp(44))
            setOnClickListener { speak(w.en) }
        }

        val save = ImageView(this).apply {
            setImageResource(R.drawable.ic_bookmark)
            setColorFilter(0xFF3A2FD0.toInt())
            val bg = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(0xFFECE9FF.toInt())
            }
            background = bg
            setPadding(dp(10), dp(10), dp(10), dp(10))
            contentDescription = "ضيفها للكلمات الصعبة"
            layoutParams = LinearLayout.LayoutParams(dp(40), dp(40)).apply { marginEnd = dp(8) }
            setOnClickListener {
                SettingsStore(this@OverlayService).addHard(w.id)
                bg.setColor(0xFFD1F9EC.toInt())
                Toast.makeText(this@OverlayService, "اتضافت للكلمات الصعبة", Toast.LENGTH_SHORT).show()
            }
        }

        root.addView(icon)
        root.addView(texts)
        root.addView(save)
        root.addView(speaker)

        // Tap = open the app on this word, swipe sideways = dismiss.
        var downX = 0f
        root.setOnTouchListener { _, e ->
            when (e.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    downX = e.rawX
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    moveBanner((e.rawX - downX).toInt())
                    true
                }
                MotionEvent.ACTION_UP -> {
                    val dx = e.rawX - downX
                    when {
                        abs(dx) > dp(100) -> finishBanner()
                        abs(dx) < dp(8) -> openLearn(w)
                        else -> moveBanner(0)
                    }
                    true
                }
                MotionEvent.ACTION_CANCEL -> {
                    moveBanner(0)
                    true
                }
                else -> false
            }
        }
        return root
    }

    private fun moveBanner(x: Int) {
        val v = banner ?: return
        val lp = bannerParams ?: return
        lp.x = x
        try {
            wm.updateViewLayout(v, lp)
        } catch (e: Exception) {
        }
    }

    private fun openLearn(w: Word) {
        val i = Intent(this, MainActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            .putExtra(EXTRA_WORD_ID, w.id)
        try {
            startActivity(i)
        } catch (e: Exception) {
        }
        finishBanner()
    }

    private fun speak(text: String) {
        if (ttsReady) tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "wordpop")
    }

    private fun removeBanner() {
        handler.removeCallbacks(dismissRunnable)
        banner?.let {
            try {
                wm.removeView(it)
            } catch (e: Exception) {
            }
        }
        banner = null
        bannerParams = null
    }

    /** Removes the banner; if the feature is switched off (a one-off test), the service stops too. */
    private fun finishBanner() {
        removeBanner()
        if (!SettingsStore(this).enabled) {
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }
    }
}
