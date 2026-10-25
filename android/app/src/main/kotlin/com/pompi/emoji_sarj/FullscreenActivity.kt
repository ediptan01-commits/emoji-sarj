package com.pompi.emoji_sarj

import android.app.Activity
import android.os.Bundle
import android.view.WindowManager

class FullscreenActivity : Activity() {
    companion object { var visible = false; private set }
    private lateinit var character: PompiView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        character = PompiView(this, true).apply { preview = true; setOnClickListener { finish() } }
        setContentView(character)
    }
    override fun onResume() { super.onResume(); visible = true; character.animating = true; refreshOverlay() }
    override fun onPause() { visible = false; character.animating = false; refreshOverlay(); super.onPause() }
    private fun refreshOverlay() {
        if (OverlayService.running) startService(android.content.Intent(this, OverlayService::class.java))
    }
}
