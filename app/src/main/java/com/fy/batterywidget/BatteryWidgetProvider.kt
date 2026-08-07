package com.fy.batterywidget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.widget.RemoteViews

class BatteryWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (widgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, widgetId)
        }
    }

    private fun updateWidget(context: Context, appWidgetManager: AppWidgetManager, widgetId: Int) {
        val (voltage, charging) = readBatteryStatus(context)
        val percent = VoltageConverter.voltageToPercent(voltage)

        val views = RemoteViews(context.packageName, R.layout.widget_layout)
        views.setTextViewText(R.id.percent_text, "$percent %")
        views.setTextViewText(R.id.voltage_text, String.format("%.3f V", voltage))

        val density = context.resources.displayMetrics.density
        val iconW = (40 * density).toInt().coerceAtLeast(1)
        val iconH = (24 * density).toInt().coerceAtLeast(1)
        val icon = BatteryIconDrawer.draw(iconW, iconH, percent, charging)
        views.setImageViewBitmap(R.id.battery_icon, icon)

        // Tap-to-refresh : redéclenche immédiatement une mise à jour
        val refreshIntent = Intent(context, BatteryWidgetProvider::class.java).apply {
            action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, intArrayOf(widgetId))
        }
        val pendingIntent = android.app.PendingIntent.getBroadcast(
            context,
            widgetId,
            refreshIntent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)

        appWidgetManager.updateAppWidget(widgetId, views)
    }

    /**
     * Lit la tension actuelle (V) et l'état de charge via l'intent collant
     * ACTION_BATTERY_CHANGED (aucune permission requise, valeur instantanée).
     */
    private fun readBatteryStatus(context: Context): Pair<Double, Boolean> {
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val batteryStatus = context.registerReceiver(null, filter)

        val voltageMv = batteryStatus?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1) ?: -1
        val status = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val charging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL

        val voltage = if (voltageMv > 0) voltageMv / 1000.0 else 0.0
        return voltage to charging
    }
}
