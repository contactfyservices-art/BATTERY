# Batterie Réelle — Widget Android

Widget qui affiche le **pourcentage réel** de charge estimé à partir de la
**tension** de la batterie (lue directement via `BatteryManager`), au lieu du
pourcentage système parfois faux après une réparation du circuit de charge.

## Table de conversion utilisée

| Tension | % estimé |
|---------|----------|
| 4.20 V  | 100 %    |
| 4.00 V  | 80 %     |
| 3.80 V  | 60 %     |
| 3.60 V  | 30 %     |
| 3.30 V  | 10 %     |
| 3.20 V  | < 10 % (0 %) |

Le pourcentage est interpolé linéairement entre ces points.
👉 Pour ajuster la table (si ta batterie a d'autres seuils), modifie le
fichier `app/src/main/java/com/fy/batterywidget/VoltageConverter.kt`.

## Fonctionnement du widget

- Mise à jour automatique **toutes les 30 minutes** (c'est le minimum
  autorisé par Android pour les widgets — impossible d'aller plus vite sans
  service en arrière-plan permanent).
- **Appuyer sur le widget** force une mise à jour instantanée.
- Couleur de l'icône : vert (> 40 %), orange (15–40 %), rouge (< 15 %), bleu
  si en charge.

## Comment obtenir l'APK

1. Pousse ce dossier sur GitHub (nouveau repo, upload via l'interface web).
2. Va dans l'onglet **Actions** du repo → le workflow "Build APK" se lance
   automatiquement.
3. Une fois terminé (coche verte), ouvre le run → section **Artifacts** en
   bas de page → télécharge `BatteryVoltageWidget-debug` (fichier .zip
   contenant l'APK).
4. Installe l'APK sur ton téléphone (autoriser "sources inconnues" si demandé).
5. Reste appuyé sur l'écran d'accueil → Widgets → "Batterie (tension)" → glisse-le
   sur l'écran d'accueil.

## Limite technique importante

Android interdit aux widgets de s'auto-actualiser en dessous de 30 minutes
sans service permanent (notification fixe). Le tap-to-refresh contourne ça
pour un contrôle instantané à la demande.
