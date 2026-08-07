package com.fy.batterywidget

/**
 * Convertit une tension de batterie (en Volts) en pourcentage estimé,
 * par interpolation linéaire sur la table de correspondance fournie par l'utilisateur :
 *
 * 100% -> 4.20V
 *  80% -> 4.00V
 *  60% -> 3.80V
 *  30% -> 3.60V
 *  10% -> 3.30V
 * <10% -> 3.20V (considéré comme 0%)
 */
object VoltageConverter {

    // Trié par tension croissante
    private val table = listOf(
        3.20 to 0,
        3.30 to 10,
        3.60 to 30,
        3.80 to 60,
        4.00 to 80,
        4.20 to 100
    )

    fun voltageToPercent(voltage: Double): Int {
        if (voltage <= table.first().first) return table.first().second
        if (voltage >= table.last().first) return table.last().second

        for (i in 0 until table.size - 1) {
            val (v1, p1) = table[i]
            val (v2, p2) = table[i + 1]
            if (voltage in v1..v2) {
                val ratio = (voltage - v1) / (v2 - v1)
                return (p1 + ratio * (p2 - p1)).toInt()
            }
        }
        return 0
    }
}
