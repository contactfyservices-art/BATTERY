package com.fy.batterywidget

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF

object BatteryIconDrawer {

    /**
     * Dessine une icône de batterie type "pilule" avec remplissage proportionnel
     * au pourcentage, et une couleur qui varie selon le niveau.
     */
    fun draw(widthPx: Int, heightPx: Int, percent: Int, charging: Boolean): Bitmap {
        val bmp = Bitmap.createBitmap(widthPx, heightPx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)

        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            color = Color.WHITE
            strokeWidth = heightPx * 0.08f
        }

        val nubWidth = widthPx * 0.06f
        val bodyRight = widthPx - nubWidth
        val inset = strokePaint.strokeWidth / 2f
        val bodyRect = RectF(inset, inset, bodyRight - inset, heightPx - inset)
        val corner = heightPx * 0.18f

        // Corps de la batterie (contour)
        canvas.drawRoundRect(bodyRect, corner, corner, strokePaint)

        // Borne (+) à droite
        val nubPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = Color.WHITE
        }
        val nubRect = RectF(
            bodyRight - inset,
            heightPx * 0.30f,
            widthPx.toFloat(),
            heightPx * 0.70f
        )
        canvas.drawRoundRect(nubRect, corner * 0.4f, corner * 0.4f, nubPaint)

        // Remplissage proportionnel
        val fillColor = when {
            charging -> Color.parseColor("#2196F3")
            percent <= 15 -> Color.parseColor("#F44336")
            percent <= 40 -> Color.parseColor("#FF9800")
            else -> Color.parseColor("#4CAF50")
        }
        val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = fillColor
        }

        val margin = strokePaint.strokeWidth * 1.4f
        val maxFillWidth = bodyRect.width() - margin * 2f
        val fillWidth = maxFillWidth * (percent.coerceIn(0, 100) / 100f)

        if (fillWidth > 0f) {
            val fillRect = RectF(
                bodyRect.left + margin,
                bodyRect.top + margin,
                bodyRect.left + margin + fillWidth,
                bodyRect.bottom - margin
            )
            val fillCorner = corner * 0.6f
            canvas.drawRoundRect(fillRect, fillCorner, fillCorner, fillPaint)
        }

        return bmp
    }
}
