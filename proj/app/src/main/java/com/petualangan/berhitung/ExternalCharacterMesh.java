package com.petualangan.berhitung;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

/** Avatar OBJ opsional. Kegagalan aset selalu berarti kembali ke mesh prosedural lama. */
final class ExternalCharacterMesh {
    static final int OTHER = 0, SHIRT = 1, SKIN = 2, EYE = 3, TIE = 4,
            PANTS = 5, SHOE = 6, HAIR = 7, HIJAB = 8, SKIRT = 9;

    final float[] pos, nor, col, gloss;
    final int[] indices;
    final byte[] part;
    final int vertexCount;

    private ExternalCharacterMesh(float[] p, float[] n, float[] c, int[] idx, byte[] parts, float[] gl) {
        pos = p;
        nor = n;
        col = c;
        indices = idx;
        part = parts;
        gloss = gl;
        vertexCount = p.length / 3;
    }

    static ExternalCharacterMesh tryLoad(Context ctx, int gender) {
        String file = gender == 0 ? "characters/putra.obj" : "characters/putri.obj";
        try (BufferedReader r = new BufferedReader(new InputStreamReader(ctx.getAssets().open(file), "UTF-8"))) {
            return parse(r);
        } catch (FileNotFoundException e) {
            return null;
        } catch (IOException e) {
            if (e.getMessage() != null && e.getMessage().contains("open failed")) return null;
            Log.w("CharacterMesh", "Cannot load " + file + "; using legacy mesh", e);
            return null;
        } catch (RuntimeException e) {
            Log.w("CharacterMesh", "Invalid " + file + "; using legacy mesh", e);
            return null;
        }
    }

    static ExternalCharacterMesh parse(BufferedReader r) throws IOException {
        ArrayList<float[]> vertices = new ArrayList<>(), normals = new ArrayList<>();
        ArrayList<Float> p = new ArrayList<>(), n = new ArrayList<>(), c = new ArrayList<>(), gl = new ArrayList<>();
        ArrayList<Integer> indices = new ArrayList<>();
        ArrayList<Byte> parts = new ArrayList<>();
        HashMap<String, Integer> seen = new HashMap<>();
        int material = OTHER;
        String line;
        while ((line = r.readLine()) != null) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("#")) continue;
            String[] a = line.split("\\s+");
            if (a[0].equals("v") && a.length >= 4) {
                vertices.add(new float[]{Float.parseFloat(a[1]), Float.parseFloat(a[2]), Float.parseFloat(a[3])});
            } else if (a[0].equals("vn") && a.length >= 4) {
                normals.add(new float[]{Float.parseFloat(a[1]), Float.parseFloat(a[2]), Float.parseFloat(a[3])});
            } else if (a[0].equals("usemtl") && a.length >= 2) {
                material = material(a[1]);
            } else if (a[0].equals("f") && a.length >= 4) {
                int[] face = new int[a.length - 1];
                for (int i = 1; i < a.length; i++) {
                    String key = a[i] + ":" + material;
                    Integer index = seen.get(key);
                    if (index == null) {
                        String[] fields = a[i].split("/", -1);
                        float[] v = vertices.get(objIndex(fields[0], vertices.size()));
                        float[] norm = {0, 0, 1};
                        if (fields.length > 2 && !fields[2].isEmpty()) {
                            norm = normals.get(objIndex(fields[2], normals.size()));
                        }
                        float[] color = palette(material);
                        for (int j = 0; j < 3; j++) {
                            p.add(v[j]);
                            n.add(norm[j]);
                            c.add(color[j]);
                        }
                        parts.add((byte) material);
                        gl.add(material == SKIN ? 0.065f : material == HAIR ? 0.085f : 0.02f);
                        index = p.size() / 3 - 1;
                        if (index >= 65535) throw new IllegalArgumentException("OBJ exceeds ES2 vertex limit");
                        seen.put(key, index);
                    }
                    face[i - 1] = index;
                }
                for (int i = 1; i < face.length - 1; i++) {
                    indices.add(face[0]);
                    indices.add(face[i]);
                    indices.add(face[i + 1]);
                }
            }
        }
        if (p.isEmpty() || indices.isEmpty()) throw new IllegalArgumentException("OBJ has no triangles");
        float[] gloss = new float[gl.size()];
        byte[] part = new byte[parts.size()];
        for (int i = 0; i < gl.size(); i++) {
            gloss[i] = gl.get(i);
            part[i] = parts.get(i);
        }
        return new ExternalCharacterMesh(toFloat(p), toFloat(n), toFloat(c), toInt(indices), part, gloss);
    }

    /** Warna pakaian/rambut tetap mengikuti Toko Avatar; geometri dan warna dasar OBJ tidak diubah. */
    float[] outfitColors(String pakaian, String rambut) {
        float[] c = col.clone();
        double[] body = null, lower = null;
        if (pakaian != null) {
            String[] p = pakaian.split(",");
            if (p.length > 0) body = hex(p[0]);
            if (p.length > 2) lower = hex(p[2]);
        }
        double[] hair = hex(rambut);
        for (int i = 0; i < vertexCount; i++) {
            double[] k = null;
            switch (part[i]) {
                case SHIRT:
                    k = body;
                    break;
                case PANTS:
                case SKIRT:
                    k = lower;
                    break;
                case HAIR:
                case HIJAB:
                    k = hair;
                    break;
                default:
                    break;
            }
            if (k != null) {
                c[i * 3] = (float) k[0];
                c[i * 3 + 1] = (float) k[1];
                c[i * 3 + 2] = (float) k[2];
            }
        }
        return c;
    }

    private static double[] hex(String h) {
        if (h == null || !h.matches("#[0-9a-fA-F]{6}")) return null;
        try {
            int v = Integer.parseInt(h.substring(1), 16);
            return new double[]{((v >> 16) & 255) / 255.0, ((v >> 8) & 255) / 255.0, (v & 255) / 255.0};
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static int objIndex(String s, int size) {
        int v = Integer.parseInt(s);
        int index = v > 0 ? v - 1 : size + v;
        if (index < 0 || index >= size) throw new IllegalArgumentException("OBJ index out of bounds");
        return index;
    }

    private static int material(String name) {
        String s = name.toLowerCase(Locale.ROOT);
        if (s.contains("hijab")) return HIJAB;
        if (s.contains("shirt")) return SHIRT;
        if (s.contains("skin")) return SKIN;
        if (s.contains("eye")) return EYE;
        if (s.contains("tie")) return TIE;
        if (s.contains("pants")) return PANTS;
        if (s.contains("skirt")) return SKIRT;
        if (s.contains("shoe")) return SHOE;
        if (s.contains("hair")) return HAIR;
        return OTHER;
    }

    private static float[] palette(int material) {
        switch (material) {
            case SKIN:
                return new float[]{1f, .77f, .60f};
            case SHIRT:
            case HIJAB:
                return new float[]{.94f, .94f, .96f};
            case TIE:
            case PANTS:
            case SKIRT:
                return new float[]{.66f, .09f, .16f};
            case HAIR:
                return new float[]{.20f, .12f, .09f};
            case SHOE:
                return new float[]{.18f, .19f, .22f};
            case EYE:
                return new float[]{.08f, .06f, .05f};
            default:
                return new float[]{.72f, .68f, .64f};
        }
    }

    private static float[] toFloat(ArrayList<Float> x) {
        float[] a = new float[x.size()];
        for (int i = 0; i < a.length; i++) a[i] = x.get(i);
        return a;
    }

    private static int[] toInt(ArrayList<Integer> x) {
        int[] a = new int[x.size()];
        for (int i = 0; i < a.length; i++) a[i] = x.get(i);
        return a;
    }
}
