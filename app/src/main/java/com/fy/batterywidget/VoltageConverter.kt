package com.fy.batterywidget

/**
 * Convertit une tension de batterie (en Volts) en pourcentage estimé,
 * par interpolation linéaire sur la table de correspondance fournie par l'utilisateur.
 *
 * Pour AUGMENTER la précision : ajoute simplement plus de points dans `table`
 * (par ex. mesure ta tension à 90%, 70%, 50%, 20% avec un multimètre ou une autre
 * appli de référence, et ajoute ces couples ici). Plus il y a de points, plus
 * l'interpolation colle à la vraie courbe de décharge (qui n'est pas linéaire).
 */
object VoltageConverter {

    // Trié par tension croissante. (Volts, Pourcentage)
    private val table = listOf(
        3.20 to 0.0,
        3.30 to 10.0,
        3.60 to 30.0,
        3.80 to 60.0,
        4.00 to 80.0,
        4.20 to 100.0
    )

    /** Retourne un pourcentage avec une décimale, ex: 73.4 */
    fun voltageToPercent(voltage: Double): Double {
        if (voltage <= table.first().first) return table.first().second
        if (voltage >= table.last().first) return table.last().second

        for (i in 0 until table.size - 1) {
            val (v1, p1) = table[i]
            val (v2, p2) = table[i + 1]
            if (voltage in v1..v2) {
                val ratio = (voltage - v1) / (v2 - v1)
                val raw = p1 + ratio * (p2 - p1)
                return Math.round(raw * 10.0) / 10.0
            }
        }
        return 0.0
    }
}
