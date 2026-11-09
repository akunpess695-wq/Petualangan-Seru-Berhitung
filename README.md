# Petualangan Seru Berhitung — v2 technical baseline

This package is based on the uploaded source repository. It retains the existing Java app, 3D avatar classes, local learning data, and assets. No game content has been removed.

## Changes
- Aligned Android versionName and APP_VERSION to 6.22.0 (previously 6.6.3 and 6.21.0).
- Replaced 41 KB GitHub Actions workflow that regenerated app sources with a small workflow that compiles the checked-in project from a ZIP.
- Added ZIP path traversal and integrity checks in CI.
- Uses Java 17, Gradle 8.9, Android Gradle Plugin 8.5.2 and compile SDK 34.
- Produces a debug APK as a GitHub Actions artifact; no release publication or automatic signing with private keys.

## Build from phone without manually extracting
Upload `Petualangan-Seru-Berhitung-v2-source.zip` to the repository root, then paste `Petualangan-Seru-Berhitung-v2-build.yml` content into `.github/workflows/build.yml`. Run Actions > Build Petualangan Seru Berhitung v2 > Run workflow. The runner extracts ZIP itself.

## Important
The uploaded original repository contains `proj/app/pb.keystore` and hardcoded debug signing credentials. This is not suitable for production release signing. This technical baseline preserves the file for debug upgrade compatibility; do not treat it as a secret. A future release should migrate to GitHub Secrets and a private release key.

The UI and game content are still those of the original app; this package does NOT claim to be a complete visual redesign. Android APK compilation and on-device testing were not performed in this environment.
