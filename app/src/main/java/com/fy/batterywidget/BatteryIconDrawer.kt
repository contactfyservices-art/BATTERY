package com.fy.batterywidget

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader

object BatteryIconDrawer {

    /**
     * Dessine une icône de batterie stylée : contour fin, remplissage en dégradé,
     * ombre portée pour rester lisible sur n'importe quel fond d'écran, et un
     * éclair superposé quand le téléphone est en charge.
     */
    fun draw(widthPx: Int, heightPx: Int, percent: Int, charging: Boolean): Bitmap {
        val bmp = Bitmap.createBitmap(widthPx, heightPx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)

        val strokeWidth = heightPx * 0.09f
        val nubWidth = widthPx * 0.07f
        val bodyRight = widthPx - nubWidth
        val inset = strokeWidth / 2f + heightPx * 0.03f
        val bodyRect = RectF(inset, inset, bodyRight - inset, heightPx - inset)
        val corner = heightPx * 0.22f

        // Ombre portée douce derrière tout le pictogramme (lisibilité sur fond clair ou chargé)
        val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            color = Color.argb(140, 0, 0, 0)
            this.strokeWidth = strokeWidth * 1.9f
        }
        canvas.drawRoundRect(bodyRect, corner, corner, shadowPaint)

        // Contour blanc net
        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            color = Color.WHITE
            this.strokeWidth = strokeWidth
        }
        canvas.drawRoundRect(bodyRect, corner, corner, strokePaint)

        // Borne (+) à droite, avec petite ombre aussi
        val nubRect = RectF(
            bodyRight - inset,
            heightPx * 0.28f,
            widthPx.toFloat(),
            heightPx * 0.72f
        )
        val nubShadow = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = Color.argb(140, 0, 0, 0)
        }
        canvas.drawRoundRect(
            RectF(nubRect.left - 1.5f, nubRect.top - 1.5f, nubRect.right + 1.5f, nubRect.bottom + 1.5f),
            corner * 0.4f, corner * 0.4f, nubShadow
        )
        val nubPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = Color.WHITE
        }
        canvas.drawRoundRect(nubRect, corner * 0.4f, corner * 0.4f, nubPaint)

        // Couleurs du dégradé selon l'état
        val (colorStart, colorEnd) = when {
            charging -> Color.parseColor("#42A5F5") to Color.parseColor("#1565C0")
            percent <= 15 -> Color.parseColor("#EF5350") to Color.parseColor("#B71C1C")
            percent <= 40 -> Color.parseColor("#FFA726") to Color.parseColor("#E65100")
            else -> Color.parseColor("#66BB6A") to Color.parseColor("#2E7D32")
        }

        val margin = strokeWidth * 1.5f
        val maxFillWidth = bodyRect.width() - margin * 2f
        val fillWidth = maxFillWidth * (percent.coerceIn(0, 100) / 100f)

        if (fillWidth > 0f) {
            val fillRect = RectF(
                bodyRect.left + margin,
                bodyRect.top + margin,
                bodyRect.left + margin + fillWidth,
                bodyRect.bottom - margin
            )
            val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.FILL
                shader = LinearGradient(
                    fillRect.left, fillRect.top, fillRect.right, fillRect.bottom,
                    colorStart, colorEnd, Shader.TileMode.CLAMP
                )
            }
            val fillCorner = corner * 0.55f
            canvas.drawRoundRect(fillRect, fillCorner, fillCorner, fillPaint)
        }

        // Éclair de charge, dessiné par-dessus, bien contrasté
        if (charging) {
            val boltPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.FILL
                color = Color.WHITE
            }
            val boltOutline = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                color = Color.argb(160, 0, 0, 0)
                this.strokeWidth = heightPx * 0.035f
            }
            val cx = bodyRect.centerX()
            val cy = bodyRect.centerY()
            val h = bodyRect.height()
            val w = h * 0.62f
            val bolt = Path().apply {
                moveTo(cx + w * 0.18f, cy - h * 0.42f)
                lineTo(cx - w * 0.28f, cy + h * 0.06f)
                lineTo(cx - w * 0.02f, cy + h * 0.06f)
                lineTo(cx - w * 0.18f, cy + h * 0.42f)
                lineTo(cx + w * 0.28f, cy - h * 0.06f)
                lineTo(cx + w * 0.02f, cy - h * 0.06f)
                close()
            }
            canvas.drawPath(bolt, boltOutline)
            canvas.drawPath(bolt, boltPaint)
        }

        return bmp
    }
}
