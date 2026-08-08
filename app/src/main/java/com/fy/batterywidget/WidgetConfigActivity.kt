package com.fy.batterywidget

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.RadioGroup

class WidgetConfigActivity : Activity() {

    private var widgetId: Int = AppWidgetManager.INVALID_APPWIDGET_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setResult(Activity.RESULT_CANCELED)
        setContentView(R.layout.activity_config)

        widgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        if (widgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        val group = findViewById<RadioGroup>(R.id.style_group)
        findViewById<Button>(R.id.btn_confirm).setOnClickListener {
            val style = when (group.checkedRadioButtonId) {
                R.id.radio_minimaliste -> BatteryStyle.MINIMALISTE
                R.id.radio_segments -> BatteryStyle.SEGMENTS
                R.id.radio_anneau -> BatteryStyle.ANNEAU
                else -> BatteryStyle.CLASSIQUE
            }
            WidgetPrefs.saveStyle(this, widgetId, style)

            val appWidgetManager = AppWidgetManager.getInstance(this)
            val updateIntent = Intent(this, BatteryWidgetProvider::class.java).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, intArrayOf(widgetId))
            }
            sendBroadcast(updateIntent)

            val resultValue = Intent().apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
            }
            setResult(Activity.RESULT_OK, resultValue)
            finish()
        }
    }
}
