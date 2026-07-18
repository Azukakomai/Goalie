# 📱 Daily Tracker — Android App

A native Android daily task management and habit consistency tracking app built with **Kotlin** and **Jetpack Compose**. Featuring a 7-day performance dashboard and a 30-day **GitHub-style contribution heatmap grid** where cell color intensity scales dynamically with daily task completion rates.

---

## 🛠️ Tech Stack

| Component | Technology | Description |
| :--- | :--- | :--- |
| **Language** | **Kotlin** (v2.1.0) | Modern, concise, type-safe programming language for Android |
| **UI Toolkit** | **Jetpack Compose** (v2024.12) | Android's modern declarative UI framework |
| **Design System** | **Material 3 (Material You)** | Dark theme UI with custom GitHub Emerald color palette |
| **Database** | **Room** (v2.6.1) | SQLite object mapping library for offline-first data persistence |
| **Architecture** | **MVVM + Clean Architecture** | ViewModel, StateFlow, Coroutines, and DAO separation |
| **Navigation** | **Compose Navigation** (v2.8.5) | Single-activity bottom navigation routing |
| **Build System** | **Gradle** (v8.11.1) | Dependency catalog via `libs.versions.toml` |

---

## ✨ Features & Concepts

### 1. Today's Tasks Screen (`TasksScreen`)
- **Daily Goal Tracker**: Visual linear progress bar showing completed tasks out of total daily tasks.
- **Add Tasks**: Modal dialog to add custom daily tasks.
- **Interactive Checklist**: Checkmark toggle with smooth text strikethrough and background color animation.
- **Task Deletion**: One-tap delete icon per task.
- **Sample Data Seeder**: Tap the ✨ icon to generate 30 days of randomized historical task data for testing analytics instantly.

### 2. 7-Day Performance Dashboard (`DashboardScreen` & `WeeklyChart`)
- **Overview Stat Cards**: Total tasks count, completed tasks count, and average 7-day completion rate %.
- **Daily Progress Bars**: Bar visualization for each of the last 7 days displaying exact completion ratios (e.g. `3/4`).

### 3. 30-Day Contribution Heatmap Grid (`HeatmapGrid`)
- Inspired by GitHub's contribution graph, condensed into a 30-square monthly grid.
- **5 Dynamic Color Levels**:
  - **Level 0 (Dark Surface `#161B22`)**: 0 tasks created / 0 completed.
  - **Level 1 (Darkest Emerald `#0E4429`)**: 1%–25% completed (e.g., 1 out of 4 tasks done).
  - **Level 2 (Medium Emerald `#006D32`)**: 26%–50% completed (e.g., 2 out of 4 tasks done).
  - **Level 3 (Bright Emerald `#26A641`)**: 51%–75% completed (e.g., 3 out of 4 tasks done).
  - **Level 4 (Neon Emerald `#39D353`)**: 76%–100% completed (e.g., 4 out of 4 tasks done).
- **Interactive Day Inspector**: Tapping any square highlights the date and displays exact stats (e.g. `Thu, Jul 16: 3/4 Done (75%)`).
- **Visual Legend**: GitHub-style `Less [■][■][■][■][■] More` scale indicator.

---

## 📂 Project Structure

```
New folder/
├── README.md
├── build.gradle.kts                   # Root Gradle build configuration
├── settings.gradle.kts                # Project & plugin repositories setup
├── gradle.properties                  # JVM & AndroidX settings
├── gradle/
│   ├── libs.versions.toml             # Centralized version catalog
│   └── wrapper/
│       └── gradle-wrapper.properties
└── app/
    ├── build.gradle.kts               # App module dependencies & SDK config
    └── src/main/
        ├── AndroidManifest.xml        # App declaration & theme bindings
        └── java/com/tasktracker/daily/
            ├── MainActivity.kt        # Main Activity & Jetpack Compose host
            ├── DailyTrackerApp.kt     # Application class initializing Room DB
            ├── data/
            │   ├── Task.kt            # Room DB Entity (id, title, isCompleted, dateEpochDay)
            │   ├── TaskDao.kt         # SQL Queries & Flows for task filtering
            │   └── AppDatabase.kt     # Room Database Singleton
            ├── viewmodel/
            │   └── TaskViewModel.kt   # StateFlow management & 30-day heatmap logic
            ├── ui/
            │   ├── theme/
            │   │   ├── Color.kt       # Emerald heatmap palette
            │   │   ├── Type.kt        # Material 3 typography
            │   │   └── Theme.kt       # Dark Theme definition
            │   ├── components/
            │   │   ├── TaskItem.kt
            │   │   ├── AddTaskDialog.kt
            │   │   ├── HeatmapGrid.kt
            │   │   └── WeeklyChart.kt
            │   ├── screens/
            │   │   ├── TasksScreen.kt
            │   │   └── DashboardScreen.kt
            │   └── navigation/
            │       └── AppNavigation.kt # Bottom Navigation Bar & NavHost
```

---

## 📲 How to Preview & Install

### Method 1: Using Android Studio (Recommended)

1. Open **Android Studio**.
2. Click **File → Open** (or **Open** from welcome screen) and select:
   `c:\Users\User\OneDrive\Documents\Programming\Budgt\New folder`
3. Wait a few moments for Gradle to sync dependencies.
4. **To Preview on Emulator**:
   - Select an Android Virtual Device (AVD) from the top device dropdown.
   - Click the green **Run ▶** button (or press `Shift + F10`).
5. **To Install on Your Android Phone**:
   - Enable **Developer Options** and **USB Debugging** on your Android phone.
   - Connect your phone to your PC via USB cable.
   - Select your phone from the device dropdown in Android Studio.
   - Click **Run ▶** — the app will compile, transfer, and open automatically on your phone!

---

### Method 2: Generate APK to Install Manually on Phone

If you prefer installing via APK file directly on your Android phone:

1. Open terminal in the project directory (`New folder`):
   ```powershell
   gradlew assembleDebug
   ```
2. Once complete, your APK file will be generated at:
   `app/build/outputs/apk/debug/app-debug.apk`
3. Transfer `app-debug.apk` to your phone (via USB cable, Google Drive, WhatsApp, or Telegram).
4. Tap the APK file on your phone to install it! (Allow "Install from unknown sources" if prompted).

---

### Method 3: Direct Install via ADB Command Line

If your phone is connected via USB and ADB is installed:
```powershell
adb install app/build/outputs/apk/debug/app-debug.apk
```
