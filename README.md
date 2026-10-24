# GLACIA — Telegram-compatible Android client

An independent, community-modified Android Telegram client maintained at
[void-tech-shiv/KLTG](https://github.com/void-tech-shiv/KLTG).

## Development status
This branch is an initial upstream source import and GLACIA branding pass.
**It has NOT passed an Android build or on-device test.** Existing upstream
messaging UI and functionality are preserved in source, but some upstream files
are missing and need to be recreated from your own credentials and implementation.

## Windows + JDK 25
The Gradle wrapper is now **9.1.0**, allowing Gradle to run with JetBrains Runtime / JDK 25. The Android compilation target is intentionally still Java 17; that does **not** require changing your system JDK. Gradle configuration and the complete APK build are separate tests.

In Android Studio, select the installed **JDK 25** for *Gradle JDK*. In the integrated PowerShell terminal (with Java 25 on PATH), run:

```powershell
git pull origin feat/glacia-full-client
powershell -NoProfile -ExecutionPolicy Bypass -File .\\scripts\\setup-android-sdk.ps1
.\\gradlew.bat --version
.\\gradlew.bat projects --stacktrace
```

The SDK setup script detects `ANDROID_HOME`, `ANDROID_SDK_ROOT` and the usual Windows user SDK folder. It writes an ignored `local.properties` file with a real `sdk.dir`, without committing a machine-specific path. If the SDK is in a custom directory, run:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\\scripts\\setup-android-sdk.ps1 -SdkPath "D:\\Android\\Sdk"
```

To find the real path, open **Android Studio → Tools → SDK Manager → Android SDK Location**. Install SDK Platform 36, NDK 27.2.12479018 and CMake 3.22.1 as indicated by the script. **Do not set `sdk.dir` to the Android Studio `jbr` folder; JDK and SDK are different.**

If PowerShell uses a different Java installation from Android Studio, check `java -version` and configure your terminal's `JAVA_HOME` for JDK 25. No API keys or signing keys should be committed.

## Local setup
1. Install Android Studio, Android SDK 36, Android NDK 27.2.12479018 and CMake 3.22.1.
2. Clone recursively: `git clone --recurse-submodules https://github.com/void-tech-shiv/KLTG.git`
   (check out the GLACIA PR branch before merging).
3. Initialize nested submodules: `git submodule update --init --recursive --depth 1`.
4. Supply **your own** Telegram application ID/hash using the project's `Extra.kt`
   integration. Never commit API credentials, signing keys, or Firebase JSON.
5. Configure your own Firebase projects if you need cloud push.
6. Generate your own signing key for distribution.
7. Open the repository root in Android Studio and Gradle Sync.

## Known blockers
- The upstream `Extra.kt` is ignored/not distributed; required by BuildVars.
- Several upstream Java/Kotlin helper files are listed in .gitignore and may be unavailable.
- Native submodules, Firebase JSON, and signing configuration need full verification.
- Review inherited billing/donations, remote update handlers, analytics and security
  functionality before shipping any APK.
- New icon artwork and full brand-resource audit still require verification.
- Not an official Telegram application.

## License and provenance
GPL-2.0; see [LICENSE](LICENSE) and [ATTRIBUTION.md](ATTRIBUTION.md).
All original notices and author copyright headers are retained where applicable.
GLACIA product names and links do not imply original authors' endorsement.
