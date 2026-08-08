package com.fy.batterywidget

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF

enum class BatteryStyle {
    CLASSIQUE,   // pilule horizontale avec borne
    MINIMALISTE, // contour fin, sans borne dessinée séparément
    SEGMENTS,    // 5 barres façon indicateur de signal
    ANNEAU       // anneau circulaire de progression
}

object BatteryIconDrawer {

    fun draw(widthPx: Int, heightPx: Int, percent: Double, charging: Boolean, style: BatteryStyle): Bitmap {
        val bmp = Bitmap.createBitmap(widthPx.coerceAtLeast(1), heightPx.coerceAtLeast(1), Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        val p = percent.coerceIn(0.0, 100.0)

        when (style) {
            BatteryStyle.CLASSIQUE -> drawClassique(canvas, widthPx, heightPx, p, charging)
            BatteryStyle.MINIMALISTE -> drawMinimaliste(canvas, widthPx, heightPx, p, charging)
            BatteryStyle.SEGMENTS -> drawSegments(canvas, widthPx, heightPx, p, charging)
            BatteryStyle.ANNEAU -> drawAnneau(canvas, widthPx, heightPx, p, charging)
        }
        return bmp
    }

    private fun fillColor(percent: Double, charging: Boolean): Int = when {
        charging -> Color.parseColor("#2196F3")
        percent <= 15 -> Color.parseColor("#F44336")
        percent <= 40 -> Color.parseColor("#FF9800")
        else -> Color.parseColor("#4CAF50")
    }

    private fun drawBolt(canvas: Canvas, cx: Float, cy: Float, size: Float) {
        val path = Path()
        path.moveTo(cx + size * 0.10f, cy - size * 0.55f)
        path.lineTo(cx - size * 0.30f, cy + size * 0.05f)
        path.lineTo(cx - size * 0.02f, cy + size * 0.05f)
        path.lineTo(cx - size * 0.10f, cy + size * 0.55f)
        path.lineTo(cx + size * 0.30f, cy - size * 0.10f)
        path.lineTo(cx + size * 0.02f, cy - size * 0.10f)
        path.close()
        val boltPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = Color.parseColor("#FFEB3B")
        }
        val outline = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            color = Color.parseColor("#66000000")
            strokeWidth = size * 0.04f
        }
        canvas.drawPath(path, boltPaint)
        canvas.drawPath(path, outline)
    }

    private fun drawClassique(canvas: Canvas, w: Int, h: Int, percent: Double, charging: Boolean) {
        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            color = Color.WHITE
            strokeWidth = h * 0.08f
        }
        val nubWidth = w * 0.06f
        val bodyRight = w - nubWidth
        val inset = strokePaint.strokeWidth / 2f
        val bodyRect = RectF(inset, inset, bodyRight - inset, h - inset)
        val corner = h * 0.18f
        canvas.drawRoundRect(bodyRect, corner, corner, strokePaint)

        val nubPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE }
        val nubRect = RectF(bodyRight - inset, h * 0.30f, w.toFloat(), h * 0.70f)
        canvas.drawRoundRect(nubRect, corner * 0.4f, corner * 0.4f, nubPaint)

        val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = fillColor(percent, charging) }
        val margin = strokePaint.strokeWidth * 1.4f
        val maxFillWidth = bodyRect.width() - margin * 2f
        val fillWidth = maxFillWidth * (percent / 100f).toFloat()
        if (fillWidth > 0f) {
            val fillRect = RectF(bodyRect.left + margin, bodyRect.top + margin,
                bodyRect.left + margin + fillWidth, bodyRect.bottom - margin)
            canvas.drawRoundRect(fillRect, corner * 0.6f, corner * 0.6f, fillPaint)
        }
        if (charging) drawBolt(canvas, bodyRect.centerX(), bodyRect.centerY(), h * 0.9f)
    }

    private fun drawMinimaliste(canvas: Canvas, w: Int, h: Int, percent: Double, charging: Boolean) {
        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            color = Color.parseColor("#AAFFFFFF")
            strokeWidth = h * 0.05f
        }
        val inset = strokePaint.strokeWidth / 2f
        val rect = RectF(inset, inset, w - inset, h - inset)
        val corner = h * 0.30f
        canvas.drawRoundRect(rect, corner, corner, strokePaint)

        val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = fillColor(percent, charging) }
        val margin = strokePaint.strokeWidth * 1.2f
        val maxFillWidth = rect.width() - margin * 2f
        val fillWidth = maxFillWidth * (percent / 100f).toFloat()
        if (fillWidth > 0f) {
            val fillRect = RectF(rect.left + margin, rect.top + margin,
                rect.left + margin + fillWidth, rect.bottom - margin)
            canvas.drawRoundRect(fillRect, corner * 0.5f, corner * 0.5f, fillPaint)
        }
        if (charging) drawBolt(canvas, rect.centerX(), rect.centerY(), h * 0.85f)
    }

    private fun drawSegments(canvas: Canvas, w: Int, h: Int, percent: Double, charging: Boolean) {
        val segCount = 5
        val gap = w * 0.03f
        val segWidth = (w - gap * (segCount - 1)) / segCount
        val activeSegs = Math.ceil((percent / 100.0) * segCount).toInt().coerceIn(0, segCount)
        val color = fillColor(percent, charging)
        for (i in 0 until segCount) {
            val left = i * (segWidth + gap)
            val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.FILL
                this.color = if (i < activeSegs) color else Color.parseColor("#33FFFFFF")
            }
            val rect = RectF(left, 0f, left + segWidth, h.toFloat())
            canvas.drawRoundRect(rect, w * 0.02f, w * 0.02f, paint)
        }
        if (charging) drawBolt(canvas, w / 2f, h / 2f, h * 0.9f)
    }

    private fun drawAnneau(canvas: Canvas, w: Int, h: Int, percent: Double, charging: Boolean) {
        val size = minOf(w, h).toFloat()
        val strokeW = size * 0.14f
        val rect = RectF(strokeW / 2f, (h - size) / 2f + strokeW / 2f, size - strokeW / 2f, (h - size) / 2f + size - strokeW / 2f)

        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = strokeW
            color = Color.parseColor("#33FFFFFF")
        }
        canvas.drawOval(rect, bgPaint)

        val fgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = strokeW
            strokeCap = Paint.Cap.ROUND
            color = fillColor(percent, charging)
        }
        val sweep = 360f * (percent / 100f).toFloat()
        canvas.drawArc(rect, -90f, sweep, false, fgPaint)

        if (charging) drawBolt(canvas, rect.centerX(), rect.centerY(), size * 0.45f)
    }
}
