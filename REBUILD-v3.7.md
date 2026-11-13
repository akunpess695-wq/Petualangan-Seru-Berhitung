# Character Rebuild v3.7 — experimental, not final

> Status 6.28.0: model OBJ dapat dicoba lewat tombol **3D** di Toko Avatar. Avatar lama tetap
> menjadi bawaan dan tidak dihapus. Aset yang tidak dapat dimuat, tudung hewan, dan gaya rambut
> khusus otomatis kembali ke mesh prosedural lama.

This checkpoint adds optional external OBJ models rather than modifying the old procedural vertex arrays. The existing renderer, camera, legacy meshes, and game remain intact. The asset loader falls back to the original mesh on failure.

- `proj/art-source/characters/putra.obj`: editable source of the boy model, white shirt, red tie, **long red trousers**, shoes, sculpted face/hair.
- `proj/art-source/characters/putri.obj`: editable source of the girl model, white shirt, red tie, **long red skirt**, white hijab, shoes.
- `proj/app/src/main/assets/characters/`: runtime copies loaded by the optional 3D mode.
- `ExternalCharacterMesh.java`: OBJ parser for v/vn/f/usemtl; material palette, per-vertex gloss, outfit recoloring, negative indices, polygon triangulation, and the 16-bit ES2 index bound.
- `PutriGLView.java`: uses OBJ only when the new 3D mode is enabled and the current look is supported; otherwise it uses the procedural mesh.

## Important limits

This is an experimental geometry replacement, **not yet a production-ready human character**. It has no rig, skinning, textures, GLB/glTF parser, or facial animation. Shirt, trousers/skirt, hair/hijab recoloring is supported. Backpack and peci controls are hidden in the new mode; animal hoods and special hairstyles use the legacy mesh automatically. Runtime GPU rendering and Android APK compilation have NOT been verified locally.

To revert: tap **3D** again in Toko Avatar, or remove `proj/app/src/main/assets/characters/`; the loader falls back to the existing procedural characters.

To test on GitHub: extract the source ZIP and upload its contents, including `.github/workflows/build.yml`, to the repository root. The workflow builds with Gradle 8.9 and uploads the debug APK artifact. Do not treat a successful build as visual verification: check both avatars on a real Android device.
