package com.pompi.emoji_sarj

import android.Manifest
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.provider.Settings
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel

class MainActivity : FlutterActivity() {
    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, "emoji_sarj/control").setMethodCallHandler { call, result ->
            val prefs = getSharedPreferences("pompi", MODE_PRIVATE)
            try {
                when (call.method) {
                    "status" -> {
                        val battery = ChargingState.from(registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED)))
                        result.success(mapOf("enabled" to OverlayService.running, "allowed" to Settings.canDrawOverlays(this), "plugged" to battery.plugged,
                            "right" to prefs.getInt("right", 8), "top" to prefs.getInt("top", 0), "size" to prefs.getInt("size", 48)))
                    }
                    "permission" -> { startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))); result.success(null) }
                    "start" -> {
                        if (!Settings.canDrawOverlays(this)) result.error("permission", "Önce üstte gösterme iznini aç.", null)
                        else {
                            if (Build.VERSION.SDK_INT >= 33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                                requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 7)
                            }
                            startForegroundService(Intent(this, OverlayService::class.java))
                            result.success(null)
                        }
                    }
                    "stop" -> { stopService(Intent(this, OverlayService::class.java)); result.success(null) }
                    "position" -> {
                        prefs.edit().putInt("right", (call.argument<Int>("right") ?: 8).coerceIn(0, 160))
                            .putInt("top", (call.argument<Int>("top") ?: 0).coerceIn(0, 200))
                            .putInt("size", (call.argument<Int>("size") ?: 48).coerceIn(32, 80)).apply()
                        if (OverlayService.running) startService(Intent(this, OverlayService::class.java))
                        result.success(null)
                    }
                    "dreamSettings" -> { startActivity(Intent(Settings.ACTION_DREAM_SETTINGS)); result.success(null) }
                    "preview" -> { startActivity(Intent(this, FullscreenActivity::class.java)); result.success(null) }
                    else -> result.notImplemented()
                }
            } catch (_: android.content.ActivityNotFoundException) {
                result.error("unavailable", "Bu telefonda ilgili Android ayarı bulunamadı.", null)
            } catch (_: RuntimeException) {
                result.error("android", "Android işlemi başlatamadı. İzinleri kontrol edip tekrar dene.", null)
            }
        }
    }
}
