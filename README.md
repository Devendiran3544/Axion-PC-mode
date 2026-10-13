# Axion Desktop (prototype)

Starter Android Studio project for a hybrid phone-screen + external-display desktop launcher.

## Features in this prototype
- Desktop-style home screen with taskbar
- App drawer that launches installed apps
- Basic external-display detection using `DisplayManager`
- A separate simple desktop screen using Android's `Presentation` API
- GitHub Actions workflow to build a debug APK and publish it as a downloadable artifact

## Important limitations
- This ZIP contains source code, not a prebuilt APK. The current execution environment has no Android SDK/Gradle installation and cannot reach the internet to download build dependencies, so the APK could not be compiled here.
- External display output requires hardware/ROM support. Many phones do not support wired USB-C video output.
- `Presentation` displays this app's UI on the second display; it does not force other apps into independent desktop windows.
- System-wide AxionOS PC Mode features require framework/SystemUI changes or ROM integration.

## Build locally
1. Open this folder in Android Studio.
2. Install Android SDK Platform 36 and JDK 17.
3. Build > Build Bundle(s) / APK(s) > Build APK(s).

## Build APK using GitHub Actions
1. Create a new GitHub repository and upload the contents of this folder.
2. Open the repository's **Actions** tab.
3. Select **Build Axion Desktop APK** and click **Run workflow** (or push to `main`/`master`).
4. When the workflow completes, open the run and download the `AxionDesktop-debug-apk` artifact.
5. Extract the artifact ZIP to get `app-debug.apk`.
