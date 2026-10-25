package com.pompi.emoji_sarj

/** Normalized mouth positions in the four atlas cells; shared by both layouts. */
object HoseGeometry {
    data class Point(val x: Float, val y: Float)
    private val mouths = arrayOf(Point(.927f, .74f), Point(.94f, .75f), Point(.94f, .75f), Point(.955f, .76f))
    fun mouth(frame: Int, left: Float, top: Float, width: Float, height: Float): Point {
        val anchor = mouths[frame.coerceIn(0, 3)]
        return Point(left + width * anchor.x, top + height * anchor.y)
    }
}
