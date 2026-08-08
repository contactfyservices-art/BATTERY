package com.fy.batterywidget

import android.content.Context

object WidgetPrefs {
    private const val PREFS = "battery_widget_prefs"
    private const val KEY_STYLE_PREFIX = "style_"
    private const val KEY_LAST_VOLTAGE = "last_smoothed_voltage"

    fun saveStyle(context: Context, widgetId: Int, style: BatteryStyle) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_STYLE_PREFIX + widgetId, style.name)
            .apply()
    }

    fun loadStyle(context: Context, widgetId: Int): BatteryStyle {
        val name = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_STYLE_PREFIX + widgetId, BatteryStyle.CLASSIQUE.name)
        return try {
            BatteryStyle.valueOf(name ?: BatteryStyle.CLASSIQUE.name)
        } catch (e: IllegalArgumentException) {
            BatteryStyle.CLASSIQUE
        }
    }

    fun removeStyle(context: Context, widgetId: Int) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .remove(KEY_STYLE_PREFIX + widgetId)
            .apply()
    }

    /**
     * Lissage exponentiel (EMA) de la tension pour réduire le bruit de mesure
     * instantané du capteur, sans retarder significativement la vraie tendance.
     */
    fun smoothVoltage(context: Context, rawVoltage: Double): Double {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val previous = prefs.getFloat(KEY_LAST_VOLTAGE, -1f).toDouble()
        val alpha = 0.5
        val smoothed = if (previous < 0) rawVoltage else (alpha * rawVoltage + (1 - alpha) * previous)
        prefs.edit().putFloat(KEY_LAST_VOLTAGE, smoothed.toFloat()).apply()
        return smoothed
    }
}
