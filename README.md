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
- Couleur de l'icône (dégradé) : vert (> 40 %), orange (15–40 %), rouge
  (< 15 %), bleu avec **éclair** si en charge.
- Fond totalement transparent, widget compact (ne prend que la place de son
  contenu) pour laisser de la place aux autres widgets sur l'écran d'accueil.
- Fiabilité : si une lecture de tension est aberrante (glitch système), le
  widget réaffiche la dernière valeur fiable connue au lieu d'un chiffre faux.

## Mode économie d'énergie automatique sous 20 %

Par défaut, Android interdit à une app normale d'activer le mode économie
d'énergie sans action de l'utilisateur. Le widget gère ça en deux temps :

1. **Automatique (optionnel, une seule manip)** : connecte ton téléphone à un
   PC avec le débogage USB activé, puis lance une fois :
   ```
   adb shell pm grant com.fy.batterywidget android.permission.WRITE_SECURE_SETTINGS
   ```
   À partir de là, le mode économie s'activera tout seul sous 20 % réel
   (hors charge), sans notification.
2. **Sans cette manip** : le widget envoie une notification "Batterie réelle
   sous 20 %" avec un raccourci direct vers l'écran d'activation — 1 tap
   suffit.

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
