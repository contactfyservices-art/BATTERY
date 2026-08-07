package com.fy.batterywidget

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 96, 48, 48)
        }

        val title = TextView(this).apply {
            text = "Batterie Réelle"
            textSize = 22f
            setTextColor(Color.BLACK)
        }

        val body = TextView(this).apply {
            text = "\nCette appli ne sert qu'à fournir un widget.\n\n" +
                    "Pour l'ajouter : reste appuyé sur ton écran d'accueil > Widgets > " +
                    "'Batterie (tension)'.\n\n" +
                    "Le widget affiche le % réel estimé à partir de la tension de la batterie, " +
                    "indépendamment du % (parfois faux) affiché par le système.\n\n" +
                    "Il se met à jour automatiquement toutes les 30 minutes (limite imposée par Android), " +
                    "ou instantanément si tu appuies dessus."
            textSize = 15f
            setTextColor(Color.DKGRAY)
        }

        layout.addView(title)
        layout.addView(body)
        setContentView(layout)
    }
}
