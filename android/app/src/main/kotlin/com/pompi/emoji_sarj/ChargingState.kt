package com.pompi.emoji_sarj

import android.content.Intent
import android.os.BatteryManager

data class ChargingState(val plugged: Boolean, val level: Int) {
    companion object {
        fun from(intent: Intent?): ChargingState {
            val scale = intent?.getIntExtra(BatteryManager.EXTRA_SCALE, 100) ?: 100
            val raw = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, 0) ?: 0
            return ChargingState(
                (intent?.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0) ?: 0) != 0,
                if (scale > 0) (raw * 100 / scale).coerceIn(0, 100) else 0
            )
        }
    }
}
