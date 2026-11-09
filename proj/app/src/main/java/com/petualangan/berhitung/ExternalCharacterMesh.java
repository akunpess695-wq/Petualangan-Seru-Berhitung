package com.petualangan.berhitung;

import android.content.Context;
import android.util.Log;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

/** Optional OBJ bridge for artist-authored avatars. ES2 unsigned-short index limit is enforced. */
final class ExternalCharacterMesh {
    final float[] pos, nor, col;
    final int[] indices;
    final int vertexCount;
    private ExternalCharacterMesh(float[] p, float[] n, float[] c, int[] idx) {
        pos=p; nor=n; col=c; indices=idx; vertexCount=p.length/3;
    }
    static ExternalCharacterMesh tryLoad(Context ctx, int gender) {
        String file = gender == 0 ? "characters/putra.obj" : "characters/putri.obj";
        try (BufferedReader r = new BufferedReader(new InputStreamReader(ctx.getAssets().open(file), "UTF-8"))) {
            return parse(r);
        } catch (java.io.FileNotFoundException e) {
            return null;
        } catch (java.io.IOException e) {
            // Android AssetManager uses IOException for missing asset, too.
            if (e.getMessage() != null && e.getMessage().contains("open failed")) return null;
            Log.w("CharacterMesh", "Cannot load " + file + "; using legacy mesh", e);
            return null;
        } catch (RuntimeException e) {
            Log.w("CharacterMesh", "Invalid " + file + "; using legacy mesh", e);
            return null;
        }
    }
    static ExternalCharacterMesh parse(BufferedReader r) throws java.io.IOException {
        ArrayList<float[]> vertices=new ArrayList<>(), normals=new ArrayList<>();
        ArrayList<Float> p=new ArrayList<>(), n=new ArrayList<>(), c=new ArrayList<>();
        ArrayList<Integer> indices=new ArrayList<>();
        HashMap<String,Integer> seen=new HashMap<>();
        float[] color={0.82f,0.42f,0.37f};
        String line;
        while ((line=r.readLine())!=null) {
            line=line.trim();
            if (line.isEmpty() || line.startsWith("#")) continue;
            String[] a=line.split("\\s+");
            if (a[0].equals("v") && a.length>=4) {
                vertices.add(new float[]{Float.parseFloat(a[1]),Float.parseFloat(a[2]),Float.parseFloat(a[3])});
            } else if (a[0].equals("vn") && a.length>=4) {
                normals.add(new float[]{Float.parseFloat(a[1]),Float.parseFloat(a[2]),Float.parseFloat(a[3])});
            } else if (a[0].equals("usemtl") && a.length>=2) {
                color=palette(a[1]);
            } else if (a[0].equals("f") && a.length>=4) {
                int[] face=new int[a.length-1];
                for(int i=1;i<a.length;i++) {
                    String key=a[i]+":"+color[0]+":"+color[1]+":"+color[2];
                    Integer index=seen.get(key);
                    if(index==null) {
                        String[] fields=a[i].split("/",-1);
                        int vi=objIndex(fields[0],vertices.size());
                        float[] v=vertices.get(vi);
                        float[] norm={0,0,1};
                        if(fields.length>2 && !fields[2].isEmpty()) norm=normals.get(objIndex(fields[2],normals.size()));
                        for(int j=0;j<3;j++){p.add(v[j]);n.add(norm[j]);c.add(color[j]);}
                        index=p.size()/3-1;
                        if(index>=65535) throw new IllegalArgumentException("OBJ exceeds ES2 vertex limit");
                        seen.put(key,index);
                    }
                    face[i-1]=index;
                }
                for(int i=1;i<face.length-1;i++) {indices.add(face[0]);indices.add(face[i]);indices.add(face[i+1]);}
            }
        }
        if(p.isEmpty()||indices.isEmpty()) throw new IllegalArgumentException("OBJ has no triangles");
        return new ExternalCharacterMesh(toFloat(p),toFloat(n),toFloat(c),toInt(indices));
    }
    private static int objIndex(String s,int size) {
        int v=Integer.parseInt(s);
        int index=v>0?v-1:size+v;
        if(index<0||index>=size) throw new IllegalArgumentException("OBJ index out of bounds");
        return index;
    }
    private static float[] palette(String name) {
        String s=name.toLowerCase(Locale.ROOT);
        if(s.contains("skin"))return new float[]{1f,.77f,.60f};
        if(s.contains("shirt")||s.contains("hijab"))return new float[]{.94f,.94f,.96f};
        if(s.contains("red")||s.contains("skirt")||s.contains("pants")||s.contains("tie"))return new float[]{.66f,.09f,.16f};
        if(s.contains("hair"))return new float[]{.20f,.12f,.09f};
        if(s.contains("shoe"))return new float[]{.18f,.19f,.22f};
        if(s.contains("eye"))return new float[]{.08f,.06f,.05f};
        return new float[]{.72f,.68f,.64f};
    }
    private static float[] toFloat(ArrayList<Float> x){float[] a=new float[x.size()];for(int i=0;i<a.length;i++)a[i]=x.get(i);return a;}
    private static int[] toInt(ArrayList<Integer> x){int[] a=new int[x.size()];for(int i=0;i<a.length;i++)a[i]=x.get(i);return a;}
}
