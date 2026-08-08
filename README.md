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
  service en arrière-plan permanent, voir plus bas).
- **Appuyer sur le widget** force une mise à jour instantanée.
- Couleur de l'icône : vert (> 40 %), orange (15–40 %), rouge (< 15 %), bleu
  si en charge.
- **Icône qui s'adapte** à la taille du widget quand tu le redimensionnes.
- **⚡ Éclair** affiché sur l'icône + dans le texte quand le chargeur est branché.
- **Pourcentage à une décimale** (ex: 73.4 %) au lieu d'un chiffre arrondi.
- **Lissage anti-bruit** : la tension est moyennée (lissage exponentiel) pour
  éviter que le % saute d'une lecture à l'autre à cause du bruit du capteur.

## Modèles de batterie

À l'ajout du widget (ou en le supprimant/re-ajoutant), un écran te demande de
choisir un style :
1. **Classique** — pilule avec borne, comme une batterie standard
2. **Minimaliste** — contour fin, épuré
3. **Segments** — 5 barres façon indicateur de signal
4. **Anneau** — cercle de progression

Le choix est mémorisé par widget (tu peux avoir deux widgets avec deux styles
différents sur le même écran).

## Consommation / ressources

Le widget n'utilise **aucun** service en arrière-plan, aucun wake lock, aucun
réseau, aucune géolocalisation. La seule permission déclarée
(`RECEIVE_BOOT_COMPLETED`) sert uniquement à réafficher le widget une fois au
redémarrage du téléphone — impact négligeable. Les mises à jour périodiques
sont gérées par le système Android lui-même (regroupées avec les autres
widgets, respectueuses du mode Doze). Il n'y a donc rien de plus à "arrêter"
pour l'alléger : c'est déjà la configuration la plus économe possible pour ce
type de widget.

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
