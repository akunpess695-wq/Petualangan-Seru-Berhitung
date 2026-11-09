# Petualangan Seru Berhitung v3.0 — Studio Lighting Build

## Implemented and verifiable
- Carries forward the original native Java OpenGL avatar meshes and costumes for boys and girls, including the existing long-trouser school uniform and hijab/long skirt.
- Replaces the previous avatar fragment shader with a warm key / cool fill / soft rim portrait-lighting shader for more dimensional rendering.
- Preserves touch rotation, idle animation, and avatar shop / currency / learning code.
- Increments versionName to 7.0.0 and versionCode fallback to 13.

## Not implemented (important)
This is **not** a replacement with new human-like rigged 3D models. It does not contain new facial rigging, eye blinking, independent limb animation, or realistic texture assets. Those require authored and licensed 3D models plus an import/animation pipeline. Do not describe this package as a complete character redesign.

## Build
Place `Petualangan-Seru-Berhitung-v3.0-source.zip` at repository root; paste `build.yml` into `.github/workflows/build.yml`. GitHub Actions extracts ZIP automatically. Debug APK signing may differ from the previous build; back up user data before uninstalling an installed version.

## Verification
ZIP structure, CRC, Java syntax of the standalone mesh classes, and YAML parsing are checked locally where tools permit. Android APK build and on-device rendering are **not** verified.
