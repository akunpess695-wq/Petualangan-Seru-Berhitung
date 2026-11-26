package com.petualangan.berhitung;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
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
 * Karakter 3D Putra & Putri 360 derajat (rekonstruksi Multi-View Depan + Samping + Belakang).
 * Kompatibel penuh dengan semua GPU OpenGL ES 2.0 Android, mendukung animasi idle, tudung hewan, & ganti warna pakaian/jilbab/rambut.
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
    private int uMVP, uM, uHoodMode, uS, uHasShirt, uShirtCol, uLowerCol, uHasHair, uHairCol, uFsGender;
    private int aP, aN, aT;
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

        // Catatan penting GLSL ES 2.0: Jangan pernah mendeklarasikan nama uniform yang sama di VS (default highp)
        // dan FS (precision mediump), karena perbedaan presisi menyebabkan glLinkProgram gagal di GPU Android!
        String vs =
                "uniform mat4 uMVP;\n" +
                "uniform mat4 uM;\n" +
                "uniform float uHoodMode;\n" +
                "attribute vec3 aP;\n" +
                "attribute vec3 aN;\n" +
                "attribute vec2 aT;\n" +
                "varying vec2 vT;\n" +
                "varying float vL;\n" +
                "varying vec3 vP;\n" +
                "void main(){\n" +
                "  vP = aP;\n" +
                "  vec3 p = aP;\n" +
                "  if (uHoodMode > 0.5 && p.y > 58.0) {\n" +
                "    if (uHoodMode < 1.5) {\n" +
                "      if (p.y > 70.5 || p.z < 4.0 || abs(p.x) > 9.2) {\n" +
                "        p.y = 63.5 + (p.y - 63.5) * 0.46;\n" +
                "        p.x = p.x * 0.76;\n" +
                "        p.z = 0.5 + (p.z - 0.5) * 0.76;\n" +
                "      }\n" +
                "    } else {\n" +
                "      if (p.x < -11.2 && p.y < 68.0) {\n" +
                "        p = aP;\n" +
                "      } else if (p.y > 74.5 || p.z < 3.2 || abs(p.x) > 8.8) {\n" +
                "        p.y = 69.0 + (p.y - 69.0) * 0.76;\n" +
                "        p.x = p.x * 0.76;\n" +
                "        p.z = -3.5 + (p.z + 3.5) * 0.74;\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "  gl_Position = uMVP * vec4(p, 1.0);\n" +
                "  vT = aT;\n" +
                "  vec3 n = normalize(mat3(uM) * aN);\n" +
                "  if (n.z < 0.0) n = -n;\n" +
                "  vec3 L = normalize(vec3(-0.30, 0.70, 0.65));\n" +
                "  vL = 0.78 + 0.26 * max(dot(n, L), 0.0);\n" +
                "}\n";

        String fs =
                "precision mediump float;\n" +
                "uniform sampler2D uS;\n" +
                "uniform float uHasShirt;\n" +
                "uniform float uHasHair;\n" +
                "uniform float uFsGender;\n" +
                "uniform vec3 uShirtCol;\n" +
                "uniform vec3 uLowerCol;\n" +
                "uniform vec3 uHairCol;\n" +
                "varying vec2 vT;\n" +
                "varying float vL;\n" +
                "varying vec3 vP;\n" +
                "void main(){\n" +
                "  vec3 c = texture2D(uS, vT).rgb;\n" +
                "  float mx = max(c.r, max(c.g, c.b));\n" +
                "  float mn = min(c.r, min(c.g, c.b));\n" +
                "  float sat = mx - mn;\n" +
                "  if (uHasShirt > 0.5) {\n" +
                "    if (vP.y > 34.0 && vP.y < 55.5 && sat < 0.15 && mx > 0.62) {\n" +
                "      c = c * uShirtCol * 1.05;\n" +
                "    }\n" +
                "    if (vP.y > 7.0 && vP.y < 38.0 && (c.r - c.g) > 0.22 && (c.r - c.b) > 0.18) {\n" +
                "      float lm = (c.r * 0.4 + c.g * 0.4 + c.b * 0.2) * 1.6;\n" +
                "      c = clamp(uLowerCol * lm, 0.0, 1.0);\n" +
                "    }\n" +
                "  }\n" +
                "  if (uHasHair > 0.5) {\n" +
                "    if (uFsGender > 0.5) {\n" +
                "      if (vP.z > 4.5 && abs(vP.x) < 9.5 && vP.y < 74.5) {\n" +
                "        c = c;\n" +
                "      } else if (vP.y > 53.5 && sat < 0.15 && mx > 0.60) {\n" +
                "        c = c * uHairCol * 1.04;\n" +
                "      }\n" +
                "    } else {\n" +
                "      if (vP.z > 5.5 && abs(vP.x) < 8.8 && vP.y < 70.5) {\n" +
                "        c = c;\n" +
                "      } else if (vP.y > 61.0 && vP.y < 78.0 && mx < 0.36) {\n" +
                "        c = clamp(uHairCol * (0.55 + mx * 1.8), 0.0, 1.0);\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "  gl_FragColor = vec4(clamp(c * vL, 0.0, 1.0), 1.0);\n" +
                "}\n";

        prog = link(vs, fs);
        if (shaderOk) {
            uMVP = GLES20.glGetUniformLocation(prog, "uMVP");
            uM = GLES20.glGetUniformLocation(prog, "uM");
            uHoodMode = GLES20.glGetUniformLocation(prog, "uHoodMode");
            uS = GLES20.glGetUniformLocation(prog, "uS");
            uHasShirt = GLES20.glGetUniformLocation(prog, "uHasShirt");
            uShirtCol = GLES20.glGetUniformLocation(prog, "uShirtCol");
            uLowerCol = GLES20.glGetUniformLocation(prog, "uLowerCol");
            uHasHair = GLES20.glGetUniformLocation(prog, "uHasHair");
            uHairCol = GLES20.glGetUniformLocation(prog, "uHairCol");
            uFsGender = GLES20.glGetUniformLocation(prog, "uFsGender");
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

    private static float[] parseHex(String h) {
        if (h == null) return null;
        try {
            int c = Color.parseColor(h.trim());
            return new float[]{Color.red(c) / 255f, Color.green(c) / 255f, Color.blue(c) / 255f};
        } catch (Exception e) {
            return null;
        }
    }

    void draw(int g, float[] baseModel, float[] view, float[] proj, float elapsed, float jumpY, String pakaian, String rambut, boolean hasHood) {
        if (!ready(g)) return;

        float[] shirt = null, lower = null;
        if (pakaian != null && !"#FFFFFF,#FFFFFF,#C62828".equalsIgnoreCase(pakaian.trim())) {
            String[] ps = pakaian.split(",");
            if (ps.length > 0) shirt = parseHex(ps[0]);
            if (ps.length > 2) lower = parseHex(ps[2]);
            else if (shirt != null) lower = new float[]{shirt[0] * 0.45f, shirt[1] * 0.45f, shirt[2] * 0.52f};
        }
        float[] hair = parseHex(rambut);

        System.arraycopy(baseModel, 0, animModel, 0, 16);
        if (hasHood) {
            Matrix.translateM(animModel, 0, 0f, jumpY, 0f);
        } else {
            float breath = (float) Math.sin(elapsed * 2.3f);
            float sway = (float) Math.sin(elapsed * 1.15f);
            Matrix.translateM(animModel, 0, 0.35f * sway, jumpY, 0f);
            Matrix.rotateM(animModel, 0, 1.1f * sway, 0f, 0f, 1f);
            Matrix.rotateM(animModel, 0, 2.2f * (float) Math.sin(elapsed * 0.85f), 0f, 1f, 0f);
            Matrix.scaleM(animModel, 0, 1f + 0.008f * breath, 1f + 0.012f * breath, 1f + 0.008f * breath);
        }

        Matrix.multiplyMM(mv, 0, view, 0, animModel, 0);
        Matrix.multiplyMM(mvp, 0, proj, 0, mv, 0);

        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        GLES20.glUseProgram(prog);
        GLES20.glUniformMatrix4fv(uMVP, 1, false, mvp, 0);
        GLES20.glUniformMatrix4fv(uM, 1, false, animModel, 0);
        GLES20.glUniform1f(uHoodMode, hasHood ? (g == 0 ? 1f : 2f) : 0f);
        GLES20.glUniform1f(uFsGender, (float) g);
        if (shirt != null && lower != null) {
            GLES20.glUniform1f(uHasShirt, 1f);
            GLES20.glUniform3f(uShirtCol, shirt[0], shirt[1], shirt[2]);
            GLES20.glUniform3f(uLowerCol, lower[0], lower[1], lower[2]);
        } else {
            GLES20.glUniform1f(uHasShirt, 0f);
        }
        if (hair != null) {
            GLES20.glUniform1f(uHasHair, 1f);
            GLES20.glUniform3f(uHairCol, hair[0], hair[1], hair[2]);
        } else {
            GLES20.glUniform1f(uHasHair, 0f);
        }
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
