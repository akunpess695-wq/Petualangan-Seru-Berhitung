package com.petualangan.berhitung;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/**
 * Karakter 3D Putra & Putri bertekstur kualitas tinggi hasil rekonstruksi 3D dari lembar desain karakter.
 * Aset: assets/characters/putra_photo.bin + putra_photo.jpg dan putri_photo.bin + putri_photo.jpg.
 * Mendukung animasi bernapas, goyang badan halus, lambaian tangan, lompat gembira, serta pewarnaan pakaian/jilbab di shader!
 */
final class PhotoAvatarScene {
    private final FloatBuffer[] vb = new FloatBuffer[2];
    private final ShortBuffer[] ib = new ShortBuffer[2];
    private final int[] nIdx = new int[2];
    private final int[] tex = new int[2];
    private int prog;
    private int uMVP, uNM, uT, uJump, uS, uHasShirt, uShirtCol, uLowerCol, uHasHair, uHairCol, uGender;
    private int aP, aN, aT;
    private final long t0 = System.nanoTime();

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

        String vs = "uniform mat4 uMVP;\n"
                + "uniform mat3 uNM;\n"
                + "uniform float uT, uJump, uGender;\n"
                + "attribute vec3 aP, aN;\n"
                + "attribute vec2 aT;\n"
                + "varying vec2 vT;\n"
                + "varying vec3 vN;\n"
                + "varying vec3 vPos;\n"
                + "void main(){\n"
                + "  vec3 p = aP;\n"
                + "  float br = sin(uT * 2.3);\n"
                + "  float sw = sin(uT * 1.15);\n"
                + "  float torsoW = smoothstep(24.0, 52.0, p.y) * (1.0 - smoothstep(52.0, 64.0, p.y));\n"
                + "  p.z += 0.30 * br * torsoW;\n"
                + "  p.x *= 1.0 + 0.012 * br * torsoW;\n"
                + "  float upW = smoothstep(26.0, 78.0, p.y);\n"
                + "  p.x += 0.50 * sw * upW;\n"
                + "  p.y += 0.32 * br * smoothstep(28.0, 72.0, p.y) + uJump;\n"
                + "  if (uGender > 0.5 && p.x < -11.0 && p.y > 46.0 && p.y < 72.0) {\n"
                + "    float wv = smoothstep(-11.0, -18.0, p.x);\n"
                + "    p.y += 1.1 * sin(uT * 4.2) * wv;\n"
                + "    p.x += 0.6 * cos(uT * 4.2) * wv;\n"
                + "  }\n"
                + "  vPos = aP;\n"
                + "  vT = aT;\n"
                + "  vN = uNM * aN;\n"
                + "  gl_Position = uMVP * vec4(p, 1.0);\n"
                + "}\n";

        String fs = "precision mediump float;\n"
                + "uniform sampler2D uS;\n"
                + "uniform float uHasShirt, uHasHair, uGender;\n"
                + "uniform vec3 uShirtCol, uLowerCol, uHairCol;\n"
                + "varying vec2 vT;\n"
                + "varying vec3 vN;\n"
                + "varying vec3 vPos;\n"
                + "void main(){\n"
                + "  vec3 c = texture2D(uS, vT).rgb;\n"
                + "  float maxC = max(c.r, max(c.g, c.b));\n"
                + "  float minC = min(c.r, min(c.g, c.b));\n"
                + "  float sat = maxC - minC;\n"
                + "  if (uHasShirt > 0.5) {\n"
                + "    if (vPos.y > 33.0 && vPos.y < 56.5 && vPos.z > -4.5 && sat < 0.14 && maxC > 0.62) {\n"
                + "      c = c * uShirtCol * 1.06;\n"
                + "    }\n"
                + "    if (vPos.y > 7.5 && vPos.y < 38.5 && c.r > 0.40 && (c.r - c.g) > 0.20 && (c.r - c.b) > 0.20) {\n"
                + "      float lum = dot(c, vec3(0.35, 0.45, 0.20)) * 1.55;\n"
                + "      c = clamp(uLowerCol * lum, 0.0, 1.0);\n"
                + "    }\n"
                + "  }\n"
                + "  if (uHasHair > 0.5) {\n"
                + "    if (uGender > 0.5) {\n"
                + "      bool onHijab = (vPos.y > 53.0 && sat < 0.14 && maxC > 0.60 && !(vPos.z > 6.0 && abs(vPos.x) < 10.5 && vPos.y < 75.5));\n"
                + "      if (onHijab) c = c * uHairCol * 1.05;\n"
                + "    } else {\n"
                + "      bool onHair = (vPos.y > 62.0 && vPos.y < 78.5 && maxC < 0.35 && !(vPos.z > 8.0 && abs(vPos.x) < 9.5 && vPos.y < 71.0));\n"
                + "      if (onHair) c = clamp(uHairCol * (0.55 + maxC * 1.8), 0.0, 1.0);\n"
                + "    }\n"
                + "  }\n"
                + "  vec3 n = normalize(vN);\n"
                + "  if (n.z < 0.0) n = -n;\n"
                + "  vec3 L1 = normalize(vec3(-0.35, 0.65, 0.68));\n"
                + "  float d1 = max(dot(n, L1), 0.0);\n"
                + "  float sh = 0.80 + 0.24 * d1;\n"
                + "  float rim = pow(clamp(1.0 - n.z, 0.0, 1.0), 3.0) * 0.10;\n"
                + "  gl_FragColor = vec4(clamp(c * sh + vec3(rim), 0.0, 1.0), 1.0);\n"
                + "}\n";

        prog = link(vs, fs);
        uMVP = GLES20.glGetUniformLocation(prog, "uMVP");
        uNM = GLES20.glGetUniformLocation(prog, "uNM");
        uT = GLES20.glGetUniformLocation(prog, "uT");
        uJump = GLES20.glGetUniformLocation(prog, "uJump");
        uS = GLES20.glGetUniformLocation(prog, "uS");
        uHasShirt = GLES20.glGetUniformLocation(prog, "uHasShirt");
        uShirtCol = GLES20.glGetUniformLocation(prog, "uShirtCol");
        uLowerCol = GLES20.glGetUniformLocation(prog, "uLowerCol");
        uHasHair = GLES20.glGetUniformLocation(prog, "uHasHair");
        uHairCol = GLES20.glGetUniformLocation(prog, "uHairCol");
        uGender = GLES20.glGetUniformLocation(prog, "uGender");
        aP = GLES20.glGetAttribLocation(prog, "aP");
        aN = GLES20.glGetAttribLocation(prog, "aN");
        aT = GLES20.glGetAttribLocation(prog, "aT");
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
            nIdx[g] = 0;
        }
    }

    boolean ready(int g) {
        return g >= 0 && g < 2 && nIdx[g] > 0;
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

    void draw(int g, float[] mvp, float[] nm, float elapsed, float jumpY, String pakaian, String rambut) {
        if (!ready(g)) return;
        float[] shirt = null, lower = null;
        if (pakaian != null && !"#FFFFFF,#FFFFFF,#C62828".equalsIgnoreCase(pakaian.trim())) {
            String[] ps = pakaian.split(",");
            if (ps.length > 0) shirt = parseHex(ps[0]);
            if (ps.length > 2) lower = parseHex(ps[2]);
            else if (shirt != null) lower = new float[]{shirt[0] * 0.45f, shirt[1] * 0.45f, shirt[2] * 0.52f};
        }
        float[] hair = parseHex(rambut);

        GLES20.glUseProgram(prog);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);
        GLES20.glUniformMatrix4fv(uMVP, 1, false, mvp, 0);
        GLES20.glUniformMatrix3fv(uNM, 1, false, nm, 0);
        GLES20.glUniform1f(uT, elapsed);
        GLES20.glUniform1f(uJump, jumpY);
        GLES20.glUniform1f(uGender, g);
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

        FloatBuffer fbuf = vb[g];
        ShortBuffer sbuf = ib[g];
        fbuf.position(0);
        GLES20.glVertexAttribPointer(aP, 3, GLES20.GL_FLOAT, false, 32, fbuf);
        GLES20.glEnableVertexAttribArray(aP);
        fbuf.position(3);
        GLES20.glVertexAttribPointer(aN, 3, GLES20.GL_FLOAT, false, 32, fbuf);
        GLES20.glEnableVertexAttribArray(aN);
        fbuf.position(6);
        GLES20.glVertexAttribPointer(aT, 2, GLES20.GL_FLOAT, false, 32, fbuf);
        GLES20.glEnableVertexAttribArray(aT);
        sbuf.position(0);
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, nIdx[g], GLES20.GL_UNSIGNED_SHORT, sbuf);
        GLES20.glDisableVertexAttribArray(aP);
        GLES20.glDisableVertexAttribArray(aN);
        GLES20.glDisableVertexAttribArray(aT);
    }

    private static int sh(int type, String s) {
        int h = GLES20.glCreateShader(type);
        GLES20.glShaderSource(h, s);
        GLES20.glCompileShader(h);
        return h;
    }

    private static int link(String v, String f) {
        int p = GLES20.glCreateProgram();
        GLES20.glAttachShader(p, sh(GLES20.GL_VERTEX_SHADER, v));
        GLES20.glAttachShader(p, sh(GLES20.GL_FRAGMENT_SHADER, f));
        GLES20.glLinkProgram(p);
        return p;
    }
}
