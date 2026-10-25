package com.pompi.emoji_sarj

object ChargingPolicy {
    fun showSmall(plugged: Boolean, interactive: Boolean, locked: Boolean, largeVisible: Boolean): Boolean =
        plugged && interactive && !locked && !largeVisible
}
