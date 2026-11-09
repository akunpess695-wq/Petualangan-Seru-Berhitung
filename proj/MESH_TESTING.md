# Mesh-only smoke test
Both PutraMesh.java and PutriMesh.java compile without Android dependencies.
Run `javac -d classes app/src/main/java/com/petualangan/berhitung/{PutraMesh,PutriMesh}.java`
The checkpoint was tested by constructing both meshes and checking finite vertex coordinates and index ranges.
This does not replace a real Android GPU preview or APK smoke test.
