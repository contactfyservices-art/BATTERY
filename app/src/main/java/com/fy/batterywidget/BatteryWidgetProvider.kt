package com.fy.batterywidget

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.os.BatteryManager
import android.os.Build
import android.provider.Settings
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat

class BatteryWidgetProvider : AppWidgetProvider() {

    companion object {
        private const val PREFS = "battery_widget_prefs"
        private const val KEY_LAST_VOLTAGE = "last_voltage"
        private const val KEY_LAST_CHARGING = "last_charging"
        private const val KEY_SAVER_NOTIFIED = "saver_notified"
        private const val LOW_BATTERY_THRESHOLD = 20
        private const val RESET_THRESHOLD = 25
        private const val CHANNEL_ID = "battery_saver_channel"
        private const val NOTIF_ID = 1001
    }

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
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val (voltage, charging) = readBatteryStatusReliable(context, prefs)
        val percent = VoltageConverter.voltageToPercent(voltage)

        val views = RemoteViews(context.packageName, R.layout.widget_layout)
        views.setTextViewText(R.id.percent_text, "$percent %")
        views.setTextViewText(R.id.voltage_text, String.format("%.3f V", voltage))

        val density = context.resources.displayMetrics.density
        val iconW = (42 * density).toInt().coerceAtLeast(1)
        val iconH = (26 * density).toInt().coerceAtLeast(1)
        val icon = BatteryIconDrawer.draw(iconW, iconH, percent, charging)
        views.setImageViewBitmap(R.id.battery_icon, icon)

        // Tap-to-refresh : redéclenche immédiatement une mise à jour
        val refreshIntent = Intent(context, BatteryWidgetProvider::class.java).apply {
            action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, intArrayOf(widgetId))
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            widgetId,
            refreshIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)

        appWidgetManager.updateAppWidget(widgetId, views)

        handleLowBatteryAutomation(context, prefs, percent, charging)
    }

    /**
     * Lit la tension actuelle (V) et l'état de charge via l'intent collant
     * ACTION_BATTERY_CHANGED. Si la lecture est invalide (valeur -1 ou 0,
     * cas rare de glitch système), on réutilise la dernière valeur valide
     * connue au lieu d'afficher une donnée fausse (0 %, -- V).
     */
    private fun readBatteryStatusReliable(context: Context, prefs: SharedPreferences): Pair<Double, Boolean> {
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val batteryStatus = context.registerReceiver(null, filter)

        val voltageMv = batteryStatus?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1) ?: -1
        val status = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val chargingNow = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL

        val voltageNow = if (voltageMv > 0) voltageMv / 1000.0 else -1.0

        // Valeur plausible : entre 2.5V et 4.5V (bornes larges de sécurité)
        val isPlausible = voltageNow in 2.5..4.5

        return if (isPlausible) {
            prefs.edit()
                .putFloat(KEY_LAST_VOLTAGE, voltageNow.toFloat())
                .putBoolean(KEY_LAST_CHARGING, chargingNow)
                .apply()
            voltageNow to chargingNow
        } else {
            // Lecture douteuse : on garde la dernière valeur fiable connue
            val fallbackVoltage = prefs.getFloat(KEY_LAST_VOLTAGE, 0f).toDouble()
            val fallbackCharging = prefs.getBoolean(KEY_LAST_CHARGING, false)
            fallbackVoltage to fallbackCharging
        }
    }

    /**
     * Sous 20 % réel et hors charge : tente d'activer le mode économie d'énergie
     * directement (nécessite la permission WRITE_SECURE_SETTINGS, accordée une
     * seule fois via une commande ADB — voir README). Si la permission n'est pas
     * accordée, envoie une notification avec un raccourci vers l'écran d'activation.
     */
    private fun handleLowBatteryAutomation(context: Context, prefs: SharedPreferences, percent: Int, charging: Boolean) {
        if (charging) {
            prefs.edit().putBoolean(KEY_SAVER_NOTIFIED, false).apply()
            return
        }

        if (percent >= RESET_THRESHOLD) {
            prefs.edit().putBoolean(KEY_SAVER_NOTIFIED, false).apply()
            return
        }

        if (percent > LOW_BATTERY_THRESHOLD) return

        val alreadyHandled = prefs.getBoolean(KEY_SAVER_NOTIFIED, false)
        if (alreadyHandled) return

        val activatedDirectly = tryEnableBatterySaverDirectly(context)
        if (!activatedDirectly) {
            notifyToEnableBatterySaver(context, percent)
        }
        prefs.edit().putBoolean(KEY_SAVER_NOTIFIED, true).apply()
    }

    private fun tryEnableBatterySaverDirectly(context: Context): Boolean {
        return try {
            Settings.Global.putInt(context.contentResolver, "low_power", 1)
            true
        } catch (e: SecurityException) {
            false
        }
    }

    private fun notifyToEnableBatterySaver(context: Context, percent: Int) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Alerte batterie faible",
                NotificationManager.IMPORTANCE_HIGH
            )
            nm.createNotificationChannel(channel)
        }

        val settingsIntent = Intent(Settings.ACTION_BATTERY_SAVER_SETTINGS)
        settingsIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        val pendingIntent = PendingIntent.getActivity(
            context, 0, settingsIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_low_battery)
            .setContentTitle("Batterie réelle sous $percent %")
            .setContentText("Touche pour activer le mode économie d'énergie")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        nm.notify(NOTIF_ID, notification)
    }
}
