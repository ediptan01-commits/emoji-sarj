package com.pompi.emoji_sarj

import android.content.*
import android.os.Build
import android.service.dreams.DreamService

/** Android starts this only after the user selects Pompi in Screen saver settings. */
class PompiDreamService : DreamService() {
    companion object { var visible = false; private set }
    private lateinit var character: PompiView
    private var registered = false
    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val state = ChargingState.from(intent)
            character.level = state.level; character.plugged = state.plugged
            if (!state.plugged) finish()
        }
    }
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        isInteractive = false
        isFullscreen = true
        isScreenBright = false
        character = PompiView(this, true).apply { animating = false }
        setContentView(character)
    }
    override fun onDreamingStarted() {
        super.onDreamingStarted()
        visible = true
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val sticky = if (Build.VERSION.SDK_INT >= 33) registerReceiver(receiver, filter, RECEIVER_NOT_EXPORTED) else registerReceiver(receiver, filter)
        registered = true
        val state = ChargingState.from(sticky)
        character.level = state.level; character.plugged = state.plugged
        character.animating = state.plugged
        if (!state.plugged) finish()
        refreshOverlay()
    }
    override fun onDreamingStopped() {
        visible = false
        character.animating = false
        if (registered) { unregisterReceiver(receiver); registered = false }
        refreshOverlay()
        super.onDreamingStopped()
    }
    override fun onDetachedFromWindow() {
        visible = false
        character.animating = false
        if (registered) { unregisterReceiver(receiver); registered = false }
        refreshOverlay()
        super.onDetachedFromWindow()
    }
    private fun refreshOverlay() {
        if (OverlayService.running) startService(Intent(this, OverlayService::class.java))
    }
}
