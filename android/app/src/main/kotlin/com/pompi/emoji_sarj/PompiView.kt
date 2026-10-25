package com.pompi.emoji_sarj

import android.content.Context
import android.graphics.*
import android.os.SystemClock
import android.view.View
import kotlin.math.*

/** Shared sprite animation for the overlay, preview and Android screen saver. */
class PompiView(context: Context, private val large: Boolean) : View(context) {
    var level = 0
    var plugged = false
    var preview = false
    var animating = true
        set(value) { field = value; if (value) postInvalidateOnAnimation() }
    private val p = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
    private val born = SystemClock.uptimeMillis()
    private val atlas = BitmapFactory.decodeStream(context.assets.open("flutter_assets/assets/pompi-atlas.png"))
    private val green = Color.rgb(57, 255, 55)
    private val hose = Path()
    private fun drawHose(c: Canvas, from: HoseGeometry.Point, targetX: Float, targetY: Float, thickness: Float) {
        hose.reset()
        hose.moveTo(from.x, from.y)
        hose.cubicTo(from.x - thickness, from.y - thickness * 5, targetX, targetY + thickness * 8, targetX, targetY)
        p.shader = null; p.style = Paint.Style.STROKE; p.strokeCap = Paint.Cap.ROUND
        p.color = Color.rgb(65, 14, 18); p.strokeWidth = thickness * 1.6f; c.drawPath(hose, p)
        p.color = Color.rgb(255, 48, 33); p.strokeWidth = thickness; c.drawPath(hose, p)
        p.color = Color.rgb(255, 174, 118); p.strokeWidth = thickness * .25f; c.drawPath(hose, p)
        p.style = Paint.Style.FILL
    }
    private fun text(c: Canvas, value: String, x: Float, y: Float, size: Float, color: Int) {
        p.shader = null; p.color = color; p.style = Paint.Style.FILL; p.textSize = size
        p.typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL); p.textAlign = Paint.Align.CENTER
        c.drawText(value, x, y, p)
    }
    private fun sprite(c: Canvas, dst: RectF, frame: Int, alpha: Int = 255) {
        val w = atlas.width / 2; val h = atlas.height / 2
        val x = frame % 2 * w; val y = frame / 2 * h
        p.shader = null; p.alpha = alpha; p.style = Paint.Style.FILL
        c.drawBitmap(atlas, Rect(x, y, x + w, y + h), dst, p)
        p.alpha = 255
    }
    override fun onDraw(c: Canvas) {
        super.onDraw(c)
        val t = (SystemClock.uptimeMillis() - born) / 1000f
        val working = plugged && level < 100
        val wiping = working && t % 10f >= 7f
        val complete = plugged && level >= 100
        val speed = 4f + level.coerceIn(0, 100) * .06f
        val frame = if (complete) 3 else if (wiping) 2 else if (working && sin(t * speed) < 0) 1 else 0
        val bob = if (working && !wiping) sin(t * speed) * 1.7f else if (complete) abs(sin(t * 4f)) * -3f else 0f
        if (large) {
            c.drawColor(Color.rgb(3, 10, 28))
            p.shader = RadialGradient(width * .3f, height * .65f, max(width, height) * .75f,
                intArrayOf(Color.rgb(15, 44, 104), Color.rgb(3, 10, 28)), null, Shader.TileMode.CLAMP)
            c.drawRect(0f, 0f, width.toFloat(), height.toFloat(), p); p.shader = null
            val s = min(width / 320f, height / 510f)
            c.save()
            c.translate(width / 2f - 160f * s + sin(t / 19f) * 4f * s, height / 2f - 255f * s + cos(t / 23f) * 4f * s)
            c.scale(s, s)
            text(c, if (preview) "ÖNİZLEME" else "EMOJİ ŞARJ", 160f, 48f, 13f, Color.rgb(133, 194, 255))
            text(c, if (!plugged) "Kablo bekleniyor" else if (complete) "Tamamlandı, seviniyor!" else if (wiping) "Terini siliyor…" else "Şarj oluyor…", 160f, 103f, 23f, Color.WHITE)
            // A gently moving neon floor, without replacing any system lock controls.
            p.strokeWidth = 1f; p.color = Color.argb(65, 73, 158, 255)
            for (i in 0..5) c.drawLine(16f, 365f + i * 7f, 304f, 365f + i * 7f, p)
            drawBattery(c, 226f, 202f, 66f, 166f, t)
            // Join the actual sprite hose mouth to a visible socket on the battery.
            val mouth = HoseGeometry.mouth(frame, 0f, 160f + bob, 219f, 219f)
            drawHose(c, mouth, 228f, 352f, 6f)
            p.color = Color.LTGRAY; c.drawRoundRect(222f, 345f, 235f, 359f, 3f, 3f, p)
            p.color = Color.rgb(255, 48, 33); c.drawCircle(227f, 352f, 3f, p)
            sprite(c, RectF(0f, 160f + bob, 219f, 379f + bob), frame)
            text(c, "%$level", 160f, 427f, 37f, if (level < 20) Color.rgb(255, 99, 72) else green)
            text(c, if (preview) "Temsili pil seviyesi · Dokun ve geri dön" else "Pompi şarj arkadaşın", 160f, 471f, 12f, Color.LTGRAY)
            c.restore()
        } else {
            // A hose reaches the top boundary, just below the real system battery.
            // Android owns the status bar; we neither redraw nor replace its icon.
            drawHose(c, HoseGeometry.mouth(frame, 0f, bob, width.toFloat(), height.toFloat()), width * .96f, 0f, width * .035f)
            sprite(c, RectF(0f, bob, width.toFloat(), height.toFloat() + bob), frame)
        }
        if (animating && isShown && windowVisibility == VISIBLE) postInvalidateDelayed(33)
    }
    private fun drawBattery(c: Canvas, x: Float, y: Float, w: Float, h: Float, t: Float) {
        val r = RectF(x, y, x + w, y + h)
        p.style = Paint.Style.FILL; p.color = Color.rgb(22, 37, 66)
        c.drawRoundRect(r, 13f, 13f, p)
        p.color = Color.rgb(154, 230, 239); c.drawRoundRect(x + 21f, y - 10f, x + 45f, y + 2f, 4f, 4f, p)
        p.style = Paint.Style.STROKE; p.strokeWidth = 7f; p.color = Color.argb(55, 40, 255, 110)
        c.drawRoundRect(r, 13f, 13f, p)
        p.strokeWidth = 2f; p.color = Color.rgb(157, 244, 244); c.drawRoundRect(r, 13f, 13f, p)
        val fill = (h - 14) * level.coerceIn(0, 100) / 100f
        p.style = Paint.Style.FILL
        p.shader = LinearGradient(x, y, x + w, y, intArrayOf(Color.rgb(0, 155, 40), green, Color.rgb(199, 255, 76)), null, Shader.TileMode.CLAMP)
        c.drawRoundRect(x + 7, y + h - 7 - fill, x + w - 7, y + h - 7, 7f, 7f, p); p.shader = null
        p.color = Color.argb(200, 200, 255, 151)
        if (fill > 10) c.drawOval(x + 7, y + h - fill - 11, x + w - 7, y + h - fill - 3, p)
        if (plugged) {
            p.color = Color.argb(180, 230, 255, 170)
            for (i in 0..4) {
                val yy = (t * 16 + i * 29) % max(fill - 8, 1f)
                if (fill > 15) c.drawCircle(x + 17 + (i % 3) * 13f, y + h - 12 - yy, 2.5f, p)
            }
        }
        text(c, "ϟ", x + w / 2, y + h / 2 + 14, 42f, Color.WHITE)
    }
}
