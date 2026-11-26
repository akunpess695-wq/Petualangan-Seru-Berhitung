package com.petualangan.berhitung;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.util.Log;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/**
 * Karakter 3D Putra & Putri bertekstur dari lembar referensi (putra_photo.bin/.jpg & putri_photo.bin/.jpg).
 * Memakai arsitektur shader & buffer yang sama persis dengan NagaScene / GodzillaScene2 agar 100% kompatibel di semua GPU Android.
 */
final class PhotoAvatarScene {
    private final FloatBuffer[] vb = new FloatBuffer[2];
    private final ShortBuffer[] ib = new ShortBuffer[2];
    private final int[] vbo = new int[2];
    private final int[] ibo = new int[2];
    private final int[] nIdx = new int[2];
    private final int[] tex = new int[2];
    private int prog;
    private boolean shaderOk = false;
    private int uMVP, uM, uS, aP, aN, aT;
    private final float[] animModel = new float[16], mv = new float[16], mvp = new float[16];

    private static byte[] readAll(InputStream in) throws java.io.IOException {
        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        byte[] buf = new byte[8192];
        int r;
        while ((r = in.read(buf)) > 0) bo.write(buf, 0, r);
        in.close();
        return bo.toByteArray();
    }

    PhotoAvatarScene(Context ctx) {
        loadGender(ctx, 0, "putra_photo");
        loadGender(ctx, 1, "putri_photo");

        String vs = "uniform mat4 uMVP,uM;"
                + "attribute vec3 aP,aN;"
                + "attribute vec2 aT;"
                + "varying vec2 vT;"
                + "varying float vL;"
                + "void main(){"
                + "gl_Position=uMVP*vec4(aP,1.);"
                + "vT=aT;"
                + "vec3 n=normalize(mat3(uM)*aN);"
                + "vL=.74+.32*max(dot(n,normalize(vec3(-.3,.7,.65))),0.);"
                + "}";
        String fs = "precision mediump float;"
                + "uniform sampler2D uS;"
                + "varying vec2 vT;"
                + "varying float vL;"
                + "void main(){"
                + "vec4 c=texture2D(uS,vT);"
                + "gl_FragColor=vec4(clamp(c.rgb*vL,0.,1.),1.);"
                + "}";

        prog = link(vs, fs);
        if (shaderOk) {
            uMVP = GLES20.glGetUniformLocation(prog, "uMVP");
            uM = GLES20.glGetUniformLocation(prog, "uM");
            uS = GLES20.glGetUniformLocation(prog, "uS");
            aP = GLES20.glGetAttribLocation(prog, "aP");
            aN = GLES20.glGetAttribLocation(prog, "aN");
            aT = GLES20.glGetAttribLocation(prog, "aT");
        }
    }

    private void loadGender(Context ctx, int g, String base) {
        try {
            byte[] b = readAll(ctx.getAssets().open("characters/" + base + ".bin"));
            ByteBuffer bb = ByteBuffer.wrap(b).order(ByteOrder.LITTLE_ENDIAN);
            bb.position(4);
            int nv = bb.getShort() & 0xFFFF, nt = bb.getInt();
            FloatBuffer fbuf = ByteBuffer.allocateDirect(nv * 32).order(ByteOrder.nativeOrder()).asFloatBuffer();
            for (int i = 0; i < nv * 8; i++) fbuf.put(bb.getFloat());
            fbuf.position(0);
            vb[g] = fbuf;
            nIdx[g] = nt * 3;
            ShortBuffer sbuf = ByteBuffer.allocateDirect(nIdx[g] * 2).order(ByteOrder.nativeOrder()).asShortBuffer();
            for (int i = 0; i < nIdx[g]; i++) sbuf.put(bb.getShort());
            sbuf.position(0);
            ib[g] = sbuf;

            int[] bufs = new int[2];
            GLES20.glGenBuffers(2, bufs, 0);
            vbo[g] = bufs[0];
            ibo[g] = bufs[1];
            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo[g]);
            GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, nv * 32, fbuf, GLES20.GL_STATIC_DRAW);
            GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, ibo[g]);
            GLES20.glBufferData(GLES20.GL_ELEMENT_ARRAY_BUFFER, nIdx[g] * 2, sbuf, GLES20.GL_STATIC_DRAW);
            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
            GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);

            InputStream imgIn = ctx.getAssets().open("characters/" + base + ".jpg");
            Bitmap bmp = BitmapFactory.decodeStream(imgIn);
            imgIn.close();
            int[] t = new int[1];
            GLES20.glGenTextures(1, t, 0);
            tex[g] = t[0];
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, tex[g]);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bmp, 0);
            bmp.recycle();
        } catch (Exception e) {
            Log.e("PhotoAvatar", "loadGender gagal: " + base, e);
            nIdx[g] = 0;
        }
    }

    boolean ready(int g) {
        return shaderOk && g >= 0 && g < 2 && nIdx[g] > 0 && vbo[g] != 0;
    }

    void draw(int g, float[] baseModel, float[] view, float[] proj, float elapsed, float jumpY) {
        if (!ready(g)) return;

        // Gerakan idle halus lewat matriks model (100% aman di semua GPU): bernapas, goyang kiri-kanan, & lompat gembira
        float breath = (float) Math.sin(elapsed * 2.3f);
        float sway = (float) Math.sin(elapsed * 1.15f);
        System.arraycopy(baseModel, 0, animModel, 0, 16);
        Matrix.translateM(animModel, 0, 0.35f * sway, jumpY, 0f);
        Matrix.rotateM(animModel, 0, 1.2f * sway, 0f, 0f, 1f);
        Matrix.rotateM(animModel, 0, 2.0f * (float) Math.sin(elapsed * 0.85f), 0f, 1f, 0f);
        Matrix.scaleM(animModel, 0, 1f + 0.008f * breath, 1f + 0.012f * breath, 1f + 0.008f * breath);

        Matrix.multiplyMM(mv, 0, view, 0, animModel, 0);
        Matrix.multiplyMM(mvp, 0, proj, 0, mv, 0);

        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        GLES20.glUseProgram(prog);
        GLES20.glUniformMatrix4fv(uMVP, 1, false, mvp, 0);
        GLES20.glUniformMatrix4fv(uM, 1, false, animModel, 0);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, tex[g]);
        GLES20.glUniform1i(uS, 0);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo[g]);
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, ibo[g]);
        GLES20.glEnableVertexAttribArray(aP);
        GLES20.glVertexAttribPointer(aP, 3, GLES20.GL_FLOAT, false, 32, 0);
        GLES20.glEnableVertexAttribArray(aN);
        GLES20.glVertexAttribPointer(aN, 3, GLES20.GL_FLOAT, false, 32, 12);
        GLES20.glEnableVertexAttribArray(aT);
        GLES20.glVertexAttribPointer(aT, 2, GLES20.GL_FLOAT, false, 32, 24);
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, nIdx[g], GLES20.GL_UNSIGNED_SHORT, 0);
        GLES20.glDisableVertexAttribArray(aP);
        GLES20.glDisableVertexAttribArray(aN);
        GLES20.glDisableVertexAttribArray(aT);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);
    }

    private static int sh(int type, String s) {
        int h = GLES20.glCreateShader(type);
        GLES20.glShaderSource(h, s);
        GLES20.glCompileShader(h);
        int[] ok = new int[1];
        GLES20.glGetShaderiv(h, GLES20.GL_COMPILE_STATUS, ok, 0);
        if (ok[0] == 0) {
            Log.e("PhotoAvatar", "shader gagal: " + GLES20.glGetShaderInfoLog(h));
            return 0;
        }
        return h;
    }

    private int link(String v, String f) {
        int vs = sh(GLES20.GL_VERTEX_SHADER, v);
        int fs = sh(GLES20.GL_FRAGMENT_SHADER, f);
        if (vs == 0 || fs == 0) {
            shaderOk = false;
            return 0;
        }
        int p = GLES20.glCreateProgram();
        GLES20.glAttachShader(p, vs);
        GLES20.glAttachShader(p, fs);
        GLES20.glLinkProgram(p);
        int[] ok = new int[1];
        GLES20.glGetProgramiv(p, GLES20.GL_LINK_STATUS, ok, 0);
        if (ok[0] == 0) {
            Log.e("PhotoAvatar", "link gagal: " + GLES20.glGetProgramInfoLog(p));
            shaderOk = false;
            return 0;
        }
        shaderOk = true;
        return p;
    }
}
