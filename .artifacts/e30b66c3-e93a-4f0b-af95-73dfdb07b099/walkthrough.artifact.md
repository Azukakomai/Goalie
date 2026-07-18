# Build APK Walkthrough

I have successfully built the debug APK for the **Goalie** (DailyTracker) app.

## Changes Made

To fix build errors and enable APK generation, I performed the following:

1.  **Fixed `settings.gradle.kts`**: Corrected `dependencyResolution` to `dependencyResolutionManagement`.
2.  **Added Missing Resources**: Created placeholder launcher icons and colors to resolve resource linking errors:
    *   `app/src/main/res/drawable/ic_launcher_foreground.xml`
    *   `app/src/main/res/values/colors.xml`
    *   `app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml`
    *   `app/src/main/res/mipmap-anydpi-v26/ic_launcher_round.xml`

## Build Command

You can build the debug APK using the following command in the terminal:

```bash
./gradlew :app:assembleDebug
```

## APK Location

The generated APK is located at:
[app-debug.apk](file:///C:/Users/User/OneDrive/Documents/Programming/Budgt/New%20folder/Goalie/app/build/outputs/apk/debug/app-debug.apk)

> [!TIP]
> If you need a **Release APK**, you should first configure signing in `app/build.gradle.kts` and then run `./gradlew :app:assembleRelease`.
