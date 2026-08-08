package com.fy.batterywidget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Bundle
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

    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: Bundle
    ) {
        // Appelé quand l'utilisateur redimensionne le widget sur l'écran d'accueil
        updateWidget(context, appWidgetManager, appWidgetId)
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        for (id in appWidgetIds) {
            WidgetPrefs.removeStyle(context, id)
        }
    }

    private fun updateWidget(context: Context, appWidgetManager: AppWidgetManager, widgetId: Int) {
        val (rawVoltage, charging) = readBatteryStatus(context)
        val voltage = WidgetPrefs.smoothVoltage(context, rawVoltage)
        val percent = VoltageConverter.voltageToPercent(voltage)
        val style = WidgetPrefs.loadStyle(context, widgetId)

        val views = RemoteViews(context.packageName, R.layout.widget_layout)
        val percentLabel = if (charging) "%.1f %% ⚡".format(percent) else "%.1f %%".format(percent)
        views.setTextViewText(R.id.percent_text, percentLabel)
        views.setTextViewText(R.id.voltage_text, "%.3f V".format(voltage))

        val density = context.resources.displayMetrics.density
        val options = appWidgetManager.getAppWidgetOptions(widgetId)
        val widthDp = options?.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, 150) ?: 150
        val heightDp = options?.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, 50) ?: 50

        val iconW = ((widthDp * 0.6f) * density).toInt().coerceAtLeast((20 * density).toInt())
        val iconH = (heightDp * density).toInt().coerceAtLeast((16 * density).toInt())
        val icon = BatteryIconDrawer.draw(iconW, iconH, percent, charging, style)
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
     * ACTION_BATTERY_CHANGED (aucune permission requise, valeur instantanée,
     * fournie directement par la puce de gestion de batterie).
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
