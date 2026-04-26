# Flixr – Android Application Firebase Setup Guide

## 1. Cloning the Repository

Clone the project repository and open it in Android Studio:

```bash
git clone https://github.com/YOUR-REPO-LINK.git
cd Flixr
```

---

## 2. Creating the Android Project (If Not Already Created)

If the project has not yet been created, follow these steps:

* Open Android Studio
* Select **New Project → Empty Activity**
* Set the following configuration:

  * Project Name: Flixr
  * Language: Kotlin
  * Package Name: `com.example.flixr`
  * Minimum SDK: API Level 24 or higher

It is essential that the package name matches the one used in Firebase.

---

## 3. Creating a Firebase Project

1. Navigate to the Firebase Console: [https://console.firebase.google.com](https://console.firebase.google.com)
2. Select **Add Project**
3. Enter the project name: Flixr
4. Optionally disable Google Analytics
5. Complete the project creation process

---

## 4. Connecting the Android Application to Firebase

Within the Firebase Console:

1. Select **Add App → Android**

2. Enter the package name:

   ```
   com.example.flixr
   ```

   This must exactly match the package name defined in Android Studio.

3. Register the application

4. Download the configuration file:

   ```
   google-services.json
   ```

---

## 5. Adding the Firebase Configuration File

Place the downloaded file in the following directory:

```
Flixr/
 └── app/
     └── google-services.json
```

Ensure that the file is placed inside the `app` directory and not at the root level.

---

## 6. Configuring Firebase Dependencies

### Project-level `build.gradle`

Add the following dependency:

```gradle
classpath 'com.google.gms:google-services:4.4.0'
```

---

### App-level `build.gradle`

Add the Google services plugin:

```gradle
plugins {
    id 'com.android.application'
    id 'org.jetbrains.kotlin.android'
    id 'com.google.gms.google-services'
}
```

Add the required Firebase libraries:

```gradle
dependencies {

    implementation platform('com.google.firebase:firebase-bom:32.7.0')

    implementation 'com.google.firebase:firebase-auth'
    implementation 'com.google.firebase:firebase-firestore'

}
```

After making these changes, synchronize the project using the "Sync Now" option.

---

## 7. Enabling Firebase Authentication

1. In the Firebase Console, navigate to **Authentication**
2. Select **Get Started**
3. Open the **Sign-in Method** tab
4. Enable **Email/Password authentication**

---

## 8. Enabling Cloud Firestore

1. Navigate to **Firestore Database**
2. Select **Create Database**
3. Choose **Start in Test Mode**
4. Select an appropriate region
5. Complete initialization

---

## 9. Verifying Firebase Integration

To confirm that Firebase has been successfully integrated, modify `MainActivity.kt` as follows:

```kotlin
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseApp

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        FirebaseApp.initializeApp(this)

        Log.d("FirebaseTest", "Firebase Connected Successfully!")
    }
}
```

Run the application and verify the output in Logcat. If the message appears, the connection has been successfully established.

---
ther development.
