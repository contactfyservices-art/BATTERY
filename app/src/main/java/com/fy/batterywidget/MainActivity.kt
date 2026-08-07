package com.fy.batterywidget

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Demande la permission de notification (nécessaire dès Android 13
        // pour pouvoir alerter en cas de batterie faible).
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 100
                )
            }
        }

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
                    "ou instantanément si tu appuies dessus.\n\n" +
                    "Un éclair apparaît sur l'icône quand le téléphone est en charge.\n\n" +
                    "Sous 20 % réel (hors charge), le widget essaie d'activer le mode économie " +
                    "d'énergie automatiquement, ou te notifie pour le faire en 1 tap si la " +
                    "permission spéciale n'a pas été accordée (voir README)."
            textSize = 15f
            setTextColor(Color.DKGRAY)
        }

        layout.addView(title)
        layout.addView(body)
        setContentView(layout)
    }
}
