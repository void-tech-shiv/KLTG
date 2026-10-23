# GLACIA — Telegram-compatible Android client

An independent, community-modified Android Telegram client maintained at
[void-tech-shiv/KLTG](https://github.com/void-tech-shiv/KLTG).

## Development status
This branch is an initial upstream source import and GLACIA branding pass.
**It has NOT passed an Android build or on-device test.** Existing upstream
messaging UI and functionality are preserved in source, but some upstream files
are missing and need to be recreated from your own credentials and implementation.

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
