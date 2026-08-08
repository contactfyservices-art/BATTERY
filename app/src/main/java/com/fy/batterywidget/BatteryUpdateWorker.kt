package com.fy.batterywidget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class BatteryUpdateWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val appWidgetManager = AppWidgetManager.getInstance(applicationContext)
        val ids = appWidgetManager.getAppWidgetIds(
            ComponentName(applicationContext, BatteryWidgetProvider::class.java)
        )
        for (id in ids) {
            BatteryWidgetProvider.updateWidget(applicationContext, appWidgetManager, id)
        }
        return Result.success()
    }
}
