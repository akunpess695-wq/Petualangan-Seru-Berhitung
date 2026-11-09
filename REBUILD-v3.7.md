# Character Rebuild v3.7 — experimental, not final

This checkpoint replaces both legacy avatar meshes at runtime with **actual new, external OBJ models**, rather than modifying the old procedural vertex arrays. The existing renderer, camera and game remain intact. The asset loader falls back to the original mesh on failure.

- `proj/app/src/main/assets/characters/putra.obj`: new boy model, white shirt, red tie, **long red trousers**, shoes, sculpted face/hair.
- `proj/app/src/main/assets/characters/putri.obj`: new girl model, white shirt, red tie, **long red skirt**, white hijab, shoes.
- `ExternalCharacterMesh.java`: OBJ parser for v/vn/f/usemtl; material-name palette, negative indices, polygon triangulation, 16-bit ES2 index bound. Missing/invalid assets fall back to original meshes.
- `PutriGLView.java`: loads optional replacement before uploading GPU buffers.

## Important limits

This is an experimental geometry replacement, **not yet a production-ready human character**. It has no rig, skinning, textures, GLB/glTF parser, or facial animation. Existing legacy outfit recoloring, hairstyle selection, backpack and animal hoods are not yet wired to the replacement topology; do not claim these features work on new avatars. Runtime GPU rendering and Android APK compilation have NOT been verified. OBJ parsing and mesh indices have been verified using a Java harness with Android API stubs.

To revert: remove the two OBJ assets; loader falls back to existing character code.

To test on GitHub: upload the ZIP and `.github/workflows/build.yml` to the repo root. The workflow extracts and builds the APK using Gradle 8.9, then uploads the debug APK artifact. Do not treat a successful build as a visual verification: check both avatars on a real Android device.
