package com.pompi.emoji_sarj

import android.app.Activity
import android.content.*
import android.os.Build
import android.os.Bundle
import android.view.WindowManager

/** User-opened charging scene; never starts itself over a secure lock screen. */
class FullscreenActivity : Activity() {
    companion object { var visible = false; private set }
    private lateinit var character: PompiView
    private var live = false
    private var registered = false
    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent != null) update(ChargingState.from(intent))
        }
    }
    private fun update(state: ChargingState) {
        character.level = state.level; character.plugged = state.plugged
        character.invalidate()
        if (!state.plugged) finish()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        live = intent.getBooleanExtra("live", false)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        if (live) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            window.attributes = window.attributes.apply { screenBrightness = .18f }
        }
        character = PompiView(this, true).apply {
            preview = !live
            if (preview) { level = 64; plugged = true }
            contentDescription = "Pompi şarj sahnesi. Çıkmak için dokun."
            setOnClickListener { finish() }
        }
        setContentView(character)
    }
    override fun onResume() {
        super.onResume()
        visible = true; character.animating = true
        if (live) {
            val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            val sticky = if (Build.VERSION.SDK_INT >= 33) registerReceiver(receiver, filter, RECEIVER_NOT_EXPORTED) else registerReceiver(receiver, filter)
            registered = true
            if (sticky != null) update(ChargingState.from(sticky))
        }
        refreshOverlay()
    }
    override fun onPause() {
        if (registered) { unregisterReceiver(receiver); registered = false }
        visible = false; character.animating = false
        refreshOverlay(); super.onPause()
    }
    private fun refreshOverlay() {
        if (OverlayService.running) runCatching { startService(Intent(this, OverlayService::class.java)) }
    }
}
