# 📱 Lab 21 – Capteurs embarqués Android

## 🎯 Objectif du laboratoire

Créer une application Android complète qui **exploite tous les capteurs physiques** d'un smartphone :

| Type de capteur | Ce qu'il mesure | Application dans le lab |
|-----------------|-----------------|------------------------|
| **Capteurs environnementaux** | Température, humidité, proximité, champ magnétique | Affichage en temps réel + graphe |
| **Capteurs de mouvement** | Accélération, gravité, rotation (gyroscope) | Mesure selon axes X, Y, Z |
| **Capteur de pas** | Nombre de pas depuis redémarrage | Compteur de pas + économie batterie |
| **Boussole** | Orientation (Nord, Est, Sud, Ouest) | Direction en degrés |
| **Reconnaissance d'activité** | Marche, saut, position stable | Classification via accéléromètre |

---

## 🧠 Concepts clés abordés

### 1. SensorManager

| Concept | Explication |
|---------|-------------|
| **SensorManager** | Service système qui donne accès à tous les capteurs du téléphone |
| `getSystemService(SENSOR_SERVICE)` | Récupère le gestionnaire de capteurs |
| `getSensorList(TYPE_ALL)` | Liste tous les capteurs disponibles |
| `getDefaultSensor(type)` | Récupère le capteur par défaut d'un type donné |

### 2. SensorEventListener

| Concept | Explication |
|---------|-------------|
| **SensorEventListener** | Interface pour recevoir les mesures des capteurs |
| `onSensorChanged()` | Appelée à chaque nouvelle mesure |
| `onAccuracyChanged()` | Appelée quand la précision du capteur change |
| `registerListener()` | Active l'écoute du capteur |
| `unregisterListener()` | Désactive l'écoute (**important pour la batterie**) |

### 3. Types de capteurs Android

| Type Android | Capteur physique | Valeurs |
|--------------|------------------|---------|
| `TYPE_AMBIENT_TEMPERATURE` | Thermomètre | 1 valeur (°C) |
| `TYPE_RELATIVE_HUMIDITY` | Hygromètre | 1 valeur (%) |
| `TYPE_PROXIMITY` | Capteur de proximité | 1 valeur (cm ou 0/1) |
| `TYPE_MAGNETIC_FIELD` | Magnétomètre | 3 valeurs (X, Y, Z en µT) |
| `TYPE_ACCELEROMETER` | Accéléromètre | 3 valeurs (X, Y, Z en m/s²) |
| `TYPE_GRAVITY` | Capteur de gravité | 3 valeurs (X, Y, Z) |
| `TYPE_GYROSCOPE` | Gyroscope | 3 valeurs (rad/s) |
| `TYPE_STEP_COUNTER` | Compteur de pas | 1 valeur (nombre depuis boot) |

### 4. Filtre passe-bas (reconnaissance d'activité)

```
Accéléromètre brut (x, y, z)
        ↓
gravity = ALPHA * gravity + (1 - ALPHA) * x   ← filtre passe-bas
        ↓
linearAcceleration = x - gravity              ← accélération sans gravité
        ↓
mouvement = sqrt(x² + y² + z²)               ← intensité du mouvement
        ↓
Analyse fenêtre glissante (30 valeurs)
        ↓
Classification : Marche / Saut / Stable
```

### 5. Boussole — fusion de capteurs

```
Accéléromètre (X, Y, Z) + Magnétomètre (X, Y, Z)
        ↓
getRotationMatrix() → matrice de rotation 3×3
        ↓
getOrientation()    → azimuth, pitch, roll
        ↓
azimuth (degrés)    → direction (Nord, Est, Sud, Ouest)
```

---

## 🏗️ Architecture de l'application

```
┌─────────────────────────────────────────────────────────────────────┐
│                           SensorApp                                 │
│                                                                     │
│  ┌───────────────────────────────────────────────────────────────┐  │
│  │                      MainActivity                             │  │
│  │  • Navigation Drawer pour accéder aux différents fragments    │  │
│  │  • Gère l'affichage et la navigation entre fragments          │  │
│  └───────────────────────────────────────────────────────────────┘  │
│                                                                     │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  ┌───────────┐  │
│  │SensorsList  │  │SensorGraph  │  │MotionSensor │  │  Compass  │  │
│  │Fragment     │  │Fragment     │  │Fragment     │  │  Fragment │  │
│  │             │  │             │  │             │  │           │  │
│  │• Liste des  │  │• Température│  │• Accéléro.  │  │• Orienta. │  │
│  │  capteurs   │  │• Humidité   │  │• Gravité    │  │  (0-360°) │  │
│  │• Caract.    │  │• Proximité  │  │• Gyroscope  │  │• Direction│  │
│  │  techniques │  │• Magnétique │  │  X, Y, Z    │  │           │  │
│  └─────────────┘  └─────────────┘  └─────────────┘  └───────────┘  │
│                                                                     │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  ┌───────────┐  │
│  │StepCounter  │  │Activity     │  │SensorFormat │  │LineChart  │  │
│  │Fragment     │  │Recognition  │  │ter (utils)  │  │View       │  │
│  │             │  │Fragment     │  │             │  │(views)    │  │
│  │• Compteur   │  │• Marche     │  │• Formatage  │  │• Graphe   │  │
│  │  de pas     │  │• Saut       │  │  des infos  │  │  temps    │  │
│  │• Économie   │  │• Stable     │  │  capteur    │  │  réel     │  │
│  │  batterie   │  │             │  │             │  │           │  │
│  └─────────────┘  └─────────────┘  └─────────────┘  └───────────┘  │
└─────────────────────────────────────────────────────────────────────┘
```

---

## 🔄 Pipeline des données

```
Utilisateur ouvre le menu → choisit un capteur
        ↓
MainActivity → openFragment() → fragment correspondant
        ↓
Fragment.onResume() → SensorManager.registerListener()
        ↓
Le système appelle onSensorChanged() à chaque nouvelle mesure
        ↓
Lecture des valeurs dans event.values[]
        ↓
Mise à jour de l'UI (TextView + LineChartView)
        ↓
Fragment.onPause() → unregisterListener()  ← économie batterie
```

---

## 📱 Fonctionnalités de l'application

| Fonctionnalité | Statut |
|----------------|--------|
| Liste de tous les capteurs disponibles | ✅ |
| Affichage des caractéristiques techniques | ✅ |
| Graphe température en temps réel | ✅ |
| Graphe humidité en temps réel | ✅ |
| Détection de proximité | ✅ |
| Graphe champ magnétique (norme) | ✅ |
| Accéléromètre (axes X, Y, Z + norme) | ✅ |
| Gravité (composante gravitationnelle) | ✅ |
| Gyroscope (taux de rotation) | ✅ |
| Compteur de pas (depuis redémarrage + session) | ✅ |
| Boussole (direction en degrés) | ✅ |
| Reconnaissance d'activité (marche/saut/stable) | ✅ |

---

## 💻 Extraits de code importants

### 1. Initialisation du SensorManager

```java
SensorManager sensorManager = (SensorManager)
        requireActivity().getSystemService(Context.SENSOR_SERVICE);

Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
```

### 2. Enregistrement de l'écoute

```java
@Override
public void onResume() {
    super.onResume();
    if (accelerometer != null) {
        sensorManager.registerListener(this, accelerometer,
                SensorManager.SENSOR_DELAY_NORMAL);
    }
}

@Override
public void onPause() {
    super.onPause();
    sensorManager.unregisterListener(this); // ⚠️ Économie batterie
}
```

### 3. Réception des mesures

```java
@Override
public void onSensorChanged(SensorEvent event) {
    float x = event.values[0]; // Axe X
    float y = event.values[1]; // Axe Y
    float z = event.values[2]; // Axe Z

    float magnitude = (float) Math.sqrt(x*x + y*y + z*z);

    textView.setText("X: " + x + "\nY: " + y + "\nZ: " + z);
    chartView.addValue(magnitude);
}
```

### 4. Filtre passe-bas — reconnaissance d'activité

```java
// Estimation de la gravité
gravity[0] = ALPHA * gravity[0] + (1 - ALPHA) * x;
gravity[1] = ALPHA * gravity[1] + (1 - ALPHA) * y;
gravity[2] = ALPHA * gravity[2] + (1 - ALPHA) * z;

// Accélération linéaire (sans gravité)
float linearX = x - gravity[0];
float linearY = y - gravity[1];
float linearZ = z - gravity[2];

// Intensité du mouvement
float movement = (float) Math.sqrt(
        linearX*linearX + linearY*linearY + linearZ*linearZ);
```

### 5. Boussole — fusion accéléromètre + magnétomètre

```java
float[] rotationMatrix = new float[9];
float[] orientation   = new float[3];

SensorManager.getRotationMatrix(rotationMatrix, null,
        gravityValues, magneticValues);
SensorManager.getOrientation(rotationMatrix, orientation);

float azimuthDegrees = (float) Math.toDegrees(orientation[0]);
if (azimuthDegrees < 0) azimuthDegrees += 360;
```

---

## 📋 AndroidManifest.xml

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <!-- Permission pour le compteur de pas (Android 10+) -->
    <uses-permission android:name="android.permission.ACTIVITY_RECOGNITION" />

    <application
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/Theme.SensorApp">

        <activity
            android:name=".MainActivity"
            android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>

    </application>

</manifest>
```

---

## 📦 Dépendances Gradle

```gradle
dependencies {
    implementation 'androidx.appcompat:appcompat:1.6.1'
    implementation 'com.google.android.material:material:1.10.0'
    implementation 'androidx.constraintlayout:constraintlayout:2.1.4'
    implementation 'androidx.recyclerview:recyclerview:1.3.2'
    implementation 'androidx.navigation:navigation-fragment:2.7.5'
    implementation 'androidx.navigation:navigation-ui:2.7.5'
    implementation 'androidx.lifecycle:lifecycle-extensions:2.2.0'
    implementation 'androidx.activity:activity:1.8.0'
}
```

---

## 📸 Captures d'écran

### Tests de l'application


<img width="296" height="614" alt="test5" src="https://github.com/user-attachments/assets/415fb92b-9ed6-4340-97ce-056709e38623" />
<img width="292" height="588" alt="test4" src="https://github.com/user-attachments/assets/3833972f-6290-4a34-9efc-2938d7bc8af0" />
<img width="300" height="617" alt="test3" src="https://github.com/user-attachments/assets/6320dde9-2ca5-47a0-a4be-9c3b7320846d" />
<img width="293" height="618" alt="test2" src="https://github.com/user-attachments/assets/bf1d557e-7a07-4353-aa04-e20395289495" />
<img width="291" height="616" alt="test1" src="https://github.com/user-attachments/assets/bb5b379a-85fc-472d-a435-31fdbffd9e50" />
<img width="280" height="567" alt="test9" src="https://github.com/user-attachments/assets/f5e4a3d9-320c-4ba8-a334-a5cbfa41b6ba" />
<img width="289" height="618" alt="test8" src="https://github.com/user-attachments/assets/de00ffd3-a2ee-4dcb-b841-32f412e9069c" />
<img width="285" height="615" alt="test7" src="https://github.com/user-attachments/assets/d78f4738-2fdc-4a6a-ade0-98a4a1efecb4" />
<img width="285" height="615" alt="test6" src="https://github.com/user-attachments/assets/6986a032-4d77-4734-8884-0e0e398349af" />






| Écran | Description |
|-------|-------------|
| Menu de navigation | Menu latéral avec toutes les options de capteurs |
| Liste des capteurs | Tous les capteurs avec leurs caractéristiques techniques |
| Graphe de température | Courbe en temps réel |
| Accéléromètre | Valeurs des axes X, Y, Z et norme |
| Boussole | Direction en degrés (Nord, Est, Sud, Ouest) |
| Compteur de pas | Pas depuis redémarrage + session en cours |
| Reconnaissance d'activité | Détection marche / saut / stable |

### AndroidManifest.xml

<img width="881" height="620" alt="manifest" src="https://github.com/user-attachments/assets/5d1ca3cb-dd48-4e07-bbc3-c7ab5e338024" />


### Dépendances (build.gradle)

<img width="867" height="716" alt="dependence" src="https://github.com/user-attachments/assets/6bad6cbe-537f-4f41-bbfe-5830f291bccd" />


---

## 🎬 Vidéo de démonstration





https://github.com/user-attachments/assets/485efabe-8899-4049-b3ba-3ab42a26d898





**Contenu de la vidéo :**
- Lancement de l'application
- Navigation dans le menu
- Affichage de la liste des capteurs
- Test du graphe de température
- Test de l'accéléromètre (inclinaison du téléphone)
- Test de la boussole (rotation du téléphone)
- Test du compteur de pas (marche)
- Test de la reconnaissance d'activité

---

## 📚 RÉCAPITULATIF – CE QUE J'AI APPRIS

---

### ✅ Synthèse du laboratoire

Ce laboratoire m'a permis de maîtriser l'ensemble des **capteurs embarqués Android** via le `SensorManager`, de la simple lecture de valeurs brutes à des traitements plus avancés comme la **fusion de capteurs** (boussole) et la **classification d'activité** par filtre passe-bas et fenêtre glissante.

---

### 📝 Les 7 points essentiels à retenir

| # | Point clé |
|---|-----------|
| 1 | **`SensorManager`** est le point d'entrée unique pour accéder à tous les capteurs |
| 2 | **`registerListener()`** active l'écoute, **`unregisterListener()`** la désactive → économie batterie |
| 3 | **`onSensorChanged()`** est appelée automatiquement à chaque nouvelle mesure |
| 4 | **`event.values[]`** contient les mesures (1 valeur ou 3 valeurs selon le capteur) |
| 5 | Le **filtre passe-bas** permet d'isoler la gravité de l'accélération linéaire |
| 6 | **`getRotationMatrix()` + `getOrientation()`** fusionne accéléromètre et magnétomètre pour la boussole |
| 7 | La **fenêtre glissante** (30 valeurs) lisse la classification pour la reconnaissance d'activité |

---

### 📊 Comparaison des délais d'écoute

| Délai | Constante | Usage recommandé |
|-------|-----------|-----------------|
| Plus rapide | `SENSOR_DELAY_FASTEST` | Jeux, réalité augmentée |
| Jeu | `SENSOR_DELAY_GAME` | Jeux interactifs |
| UI | `SENSOR_DELAY_UI` | Changements d'orientation |
| Normal | `SENSOR_DELAY_NORMAL` | Surveillance générale ✅ |

---

### 💡 Bonnes pratiques retenues

- [x] Toujours appeler `unregisterListener()` dans `onPause()` pour économiser la batterie
- [x] Vérifier que `getDefaultSensor()` ne retourne pas `null` avant d'enregistrer l'écoute
- [x] Utiliser `SENSOR_DELAY_NORMAL` par défaut (évite la surconsommation CPU/batterie)
- [x] Appliquer un filtre passe-bas pour lisser les données brutes de l'accéléromètre
- [x] Utiliser une fenêtre glissante pour la classification d'activité (évite les faux positifs)

---

### 🎯 Compétences acquises

| Compétence | Niveau |
|------------|--------|
| Accéder aux capteurs avec `SensorManager` | ✅ Maîtrisé |
| Implémenter `SensorEventListener` | ✅ Maîtrisé |
| Lire et afficher les valeurs brutes | ✅ Maîtrisé |
| Afficher un graphe en temps réel | ✅ Maîtrisé |
| Fusionner deux capteurs (boussole) | ✅ Maîtrisé |
| Appliquer un filtre passe-bas | ✅ Maîtrisé |
| Classifier une activité par fenêtre glissante | ✅ Maîtrisé |

---

### ✅ Vérification finale

- [x] La liste des capteurs affiche tous les capteurs disponibles
- [x] Le graphe de température s'affiche et évolue
- [x] Le graphe d'humidité s'affiche et évolue
- [x] La proximité détecte l'approche d'un objet
- [x] Le champ magnétique varie avec l'orientation
- [x] L'accéléromètre affiche les axes X, Y, Z
- [x] La gravité affiche les axes X, Y, Z
- [x] Le gyroscope affiche les axes X, Y, Z
- [x] Le compteur de pas augmente en marchant
- [x] La boussole indique la bonne direction
- [x] La reconnaissance d'activité détecte marche / saut / stable

---

### 👨‍💻 Auteur

| Élément | Information |
|---------|-------------|
| **Nom** | El Hachimi Abdelhamid |
| **GitHub** | [abdotranscript25](https://github.com/abdotranscript25) |
| **Lab** | Programmation Mobile - Lab 21 |

---

### 📅 Version

| Élément | Information |
|---------|-------------|
| **Date** | Mai 2026 |
| **Version** | 1.0 |
| **Statut** | ✅ Finalisé |
