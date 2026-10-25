package com.pompi.emoji_sarj

import android.app.*
import android.content.*
import android.content.pm.ServiceInfo
import android.graphics.PixelFormat
import android.os.*
import android.provider.Settings
import android.view.*

class OverlayService : Service() {
    companion object { var running = false; private set }
    private lateinit var wm: WindowManager
    private var character: PompiView? = null
    private var battery = ChargingState(false, 0)
    private var registered = false
    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == Intent.ACTION_BATTERY_CHANGED) battery = ChargingState.from(intent)
            update()
        }
    }
    override fun onCreate() {
        super.onCreate()
        wm = getSystemService(WindowManager::class.java)
        // Migrate the previous default, retaining positions explicitly customized by users.
        val prefs = getSharedPreferences("pompi", MODE_PRIVATE)
        if (!prefs.getBoolean("placementV2", false)) {
            val edit = prefs.edit().putBoolean("placementV2", true)
            if (prefs.getInt("right", 8) == 8) edit.putInt("right", 22)
            edit.apply()
        }
        getSystemService(NotificationManager::class.java).createNotificationChannel(
            NotificationChannel("pompi", "Pompi şarj arkadaşı", NotificationManager.IMPORTANCE_LOW)
        )
        val stop = PendingIntent.getService(this, 1, Intent(this, OverlayService::class.java).setAction("stop"), PendingIntent.FLAG_IMMUTABLE)
        val open = PendingIntent.getActivity(this, 2, Intent(this, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE)
        val notification = Notification.Builder(this, "pompi")
            .setSmallIcon(R.drawable.ic_pompi_notification)
            .setContentTitle("Pompi hazır")
            .setContentText("Şarjda ve kilit açıkken küçük karakter gösterilir.")
            .setContentIntent(open).setOngoing(true)
            .addAction(Notification.Action.Builder(null, "Durdur", stop).build()).build()
        if (Build.VERSION.SDK_INT >= 34) startForeground(41, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        else startForeground(41, notification)
        running = true
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_BATTERY_CHANGED)
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_SCREEN_OFF)
            addAction(Intent.ACTION_USER_PRESENT)
        }
        val sticky = if (Build.VERSION.SDK_INT >= 33) registerReceiver(receiver, filter, RECEIVER_NOT_EXPORTED) else registerReceiver(receiver, filter)
        registered = true
        battery = ChargingState.from(sticky)
    }
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == "stop") { stopSelf(); return START_NOT_STICKY }
        update()
        return START_NOT_STICKY
    }
    private fun hide() {
        character?.let { it.animating = false; runCatching { wm.removeView(it) } }
        character = null
    }
    private fun update() {
        if (!Settings.canDrawOverlays(this)) { hide(); stopSelf(); return }
        val power = getSystemService(PowerManager::class.java)
        val keyguard = getSystemService(KeyguardManager::class.java)
        if (!ChargingPolicy.showSmall(battery.plugged, power.isInteractive, keyguard.isKeyguardLocked, FullscreenActivity.visible || PompiDreamService.visible)) { hide(); return }
        val prefs = getSharedPreferences("pompi", MODE_PRIVATE)
        val density = resources.displayMetrics.density
        fun dp(n: Int) = (n * density).toInt()
        val side = dp(prefs.getInt("size", 48))
        val params = WindowManager.LayoutParams(side, side,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT).apply {
            gravity = Gravity.TOP or Gravity.RIGHT
            x = dp(prefs.getInt("right", 22))
            // Use screen coordinates and add the status-bar inset exactly once.
            val inset = if (Build.VERSION.SDK_INT >= 30) {
                setFitInsetsTypes(0)
                wm.maximumWindowMetrics.windowInsets.getInsetsIgnoringVisibility(WindowInsets.Type.statusBars() or WindowInsets.Type.displayCutout()).top
            } else {
                val resource = resources.getIdentifier("status_bar_height", "dimen", "android")
                if (resource > 0) resources.getDimensionPixelSize(resource) else dp(24)
            }
            y = (inset + dp(prefs.getInt("top", 0))).coerceAtLeast(0)
            alpha = 0.8f
        }
        try {
            val v = character ?: PompiView(this, false).also { wm.addView(it, params); character = it }
            v.level = battery.level; v.plugged = battery.plugged
            wm.updateViewLayout(v, params)
        } catch (_: RuntimeException) { hide(); stopSelf() }
    }
    override fun onDestroy() {
        hide()
        if (registered) unregisterReceiver(receiver)
        running = false
        stopForeground(STOP_FOREGROUND_REMOVE)
        super.onDestroy()
    }
    override fun onBind(intent: Intent?): IBinder? = null
}
