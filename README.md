# MainTask

Application Android de rappels pour tâches récurrentes.

## Fonctionnalités

- Liste de tâches avec compte à rebours glissant
- Notifications J-3 et Jour J à 11h
- Bouton ✓ pour relancer le compteur au moment de l'exécution
- Snooze configurable
- Création / modification / suppression de tâches
- Widget écran d'accueil (3 prochaines tâches)
- Export / Import JSON (backup)
- Dark mode automatique (selon le système)

## Stack technique

- **Language** : Kotlin
- **UI** : Jetpack Compose + Material 3
- **BDD** : Room (SQLite)
- **Notifications** : WorkManager
- **Widget** : Glance

## Installation

### Pré-requis

- JDK 17+
- Android SDK (API 26 minimum)
- Un device Android ou émulateur

### Compiler et installer

```bash
./gradlew installDebug
```

### Générer un APK release

```bash
./gradlew assembleRelease
```

## Licence

[MIT](LICENSE)
