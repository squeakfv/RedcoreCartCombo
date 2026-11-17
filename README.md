# Redcore Cart Combo — Fabric 1.21.11 — v1.2.0

## Personnalisation intégrée
- F10 ouvre l'interface de configuration.
- Cliquer sur un bouton de touche, puis presser la nouvelle touche.
- Touches configurables : démarrer, annuler, ouvrir les paramètres et chacun des 9 slots de la barre rapide.
- Délais configurables individuellement en ticks : rail, wagonnet, allumage et arbalète.
- Options : afficher/masquer le HUD, restaurer ou non le slot d'origine, mettre le combo en pause quand un écran est ouvert.
- Les délais et options sont sauvegardés dans `config/redcore-cart-combo.properties`.
- Les touches sont enregistrées dans les options Minecraft/Fabric.

## Prérequis
- Minecraft Java 1.21.11
- Java 21
- Fabric Loader 0.18.x compatible
- Fabric API pour 1.21.11

## Compilation
Ouvrir le dossier dans IntelliJ IDEA puis exécuter `gradle build`.
Le JAR remappé sera dans `build/libs/`. Copier le JAR dans `.minecraft/mods/` ou ajouter le fichier comme version Fabric sur Modrinth.

## Utilisation
Mettre dans la barre rapide : rail, wagonnet TNT, briquet, arbalète déjà chargée.
Appuyer sur la touche d'activation (F8 par défaut), viser le bloc pour le rail, garder le viseur sur le rail pour poser le wagonnet, puis viser le wagonnet TNT pour l'allumer, puis viser la cible pour tirer.
F9 annule le combo (touche configurable). F10 ouvre les paramètres (touche configurable).

## Notes
Les touches des slots 1 à 9 sont les vrais raccourcis de barre rapide Minecraft et sont modifiées dans les options du client.
Le mod utilise les interactions normales du jeu ; le serveur décide si elles sont acceptées.
Les délais sont en ticks (20 ticks = environ 1 seconde). Certaines actions nécessitent toujours que le joueur vise manuellement la bonne cible.
Le projet source est fourni ; le JAR doit être compilé et testé localement avant utilisation.


## Portage 1.21.11

Le projet utilise Yarn 1.21.11+build.6, Fabric Loader 0.18.4, Fabric API 0.141.6+1.21.11 et Fabric Loom 1.14.10. Compilation avec Java 21 et Gradle 9.2 ou supérieur. Vérifier le lancement en jeu avant publication.

## Compilation automatique (GitHub Actions)

Le workflow `.github/workflows/build.yml` compile le mod avec Java 21 et Gradle 9.2 à chaque push ou lancement manuel. Après une compilation réussie, récupérer l'archive `redcore-cart-combo-fabric-1.21.11` dans l'onglet **Actions → workflow → Artifacts**. Le fichier `*-sources.jar` est le code source ; pour Modrinth, utiliser le JAR principal sans `-sources`.

## Compilation automatique (GitHub Actions)

Le workflow `.github/workflows/build.yml` compile le mod avec Java 21 et Gradle 9.2 à chaque push ou lancement manuel. Après une compilation réussie, récupérer l'archive `redcore-cart-combo-fabric-1.21.11` dans l'onglet **Actions → workflow → Artifacts**. Pour Modrinth, utiliser le JAR principal sans `-sources`.
