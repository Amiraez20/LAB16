# 📱 Lab Android — Foreground Service Timer

Application Android en **Java pur** démontrant l'utilisation d'un **Foreground Service** combiné à un **Bound Service** pour implémenter un timer persistant avec notification en temps réel.

---

## 🎬 Démonstration

[Screen_recording_20260523_180733.webm](https://github.com/user-attachments/assets/fb570df9-a944-41f3-88d1-419dec689df3)


---

## 🎯 Objectifs

- Créer un **Foreground Service** conforme aux règles Android 8.0+ et 14+
- Afficher une **notification persistante** mise à jour en temps réel
- Lier une Activity à un Service via un **Bound Service** (`IBinder`)
- Gérer le **cycle de vie** des Services (`onCreate`, `onStartCommand`, `onDestroy`)
- Mettre à jour l'UI depuis un thread de fond via un **Handler**

---

## 🏗️ Architecture

```
ServiceChronometreJava/
├── app/src/main/
│   ├── java/com/example/servicechronometrejava/
│   │   ├── MainActivity.java       # UI + connexion au service
│   │   └── TimerService.java       # Foreground + Bound Service
│   ├── res/layout/
│   │   └── activity_main.xml       # TextView + 2 boutons
│   └── AndroidManifest.xml         # Permissions + déclaration service
```

### Flux de communication

```
MainActivity
    │
    ├── startForegroundService() ──────► TimerService démarre
    │                                        │
    ├── bindService() ─────────────────► AgentLocal (IBinder)
    │       │                                │
    │   onServiceConnected()                 │
    │       │                                │
    └── Handler (UI thread)                  │
            └── getCompteur() ◄─────────────┘
                    │
                 tvTemps.setText()
```

---

## 🔑 Concepts clés

| Concept | Rôle |
|---|---|
| `START_STICKY` | Le service redémarre automatiquement si le système le tue |
| `startForeground()` | Obligatoire Android 8+ — empêche l'arrêt en arrière-plan |
| `IBinder / AgentLocal` | Communication bidirectionnelle Activity ↔ Service |
| `ScheduledExecutorService` | Thread sécurisé pour incrémenter le compteur chaque seconde |
| `Handler(getMainLooper())` | Seule façon correcte de modifier l'UI depuis un thread de fond |
| `unbindService()` | Appelé dans `onDestroy()` pour éviter les fuites mémoire |

---

## 🚀 Installation & Lancement

**Prérequis :** Android Studio, émulateur ou appareil API 24+

```bash
git clone https://github.com/ton-username/ServiceChronometreJava.git
```

1. Ouvrir le projet dans **Android Studio**
2. Lancer sur un émulateur **API 26+** (recommandé)
3. Cliquer **LANCER LE TIMER** → la notification apparaît
4. Quitter l'app → le timer continue en arrière-plan
5. Cliquer **STOPPER LE TIMER** → tout s'arrête proprement

---

## 📋 Permissions requises

```xml
<uses-permission android:name="android.permission.POST_NOTIFICATIONS"/>
<uses-permission android:name="android.permission.FOREGROUND_SERVICE"/>
<uses-permission android:name="android.permission.FOREGROUND_SERVICE_SPECIAL_USE"/>
```

---

## ⚠️ Problèmes courants

<details>
<summary>L'app crash au démarrage</summary>

- Vérifier que `FOREGROUND_SERVICE` et `FOREGROUND_SERVICE_SPECIAL_USE` sont déclarés dans le Manifest
- Utiliser `foregroundServiceType="specialUse"` (et non `dataSync` sur API 34+)

</details>

<details>
<summary>Le timer reste bloqué à 00:00</summary>

Le service tourne dans un thread séparé — il ne peut pas modifier l'UI directement.  
Solution : utiliser un `Handler` sur le **UI thread** qui interroge `getCompteur()` toutes les secondes.

</details>

<details>
<summary>La notification n'apparaît pas</summary>

Sur Android 13+ la permission `POST_NOTIFICATIONS` doit être accordée explicitement par l'utilisateur au runtime.

</details>

---
