package com.petualangan.berhitung;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/** Godzilla mini yang berdiri di samping avatar (di dalam adegan 3D PutriGLView). Semua pemanggilan di thread GL.
 *  Aset: characters/godzilla_pet.bin + godzilla_pet.jpg (tinggi 1, kaki di y=0, hadap +z, ekor ke -z).
 *  Godzilla Minus Zero by Dola (CC-BY 4.0). */
final class GodzillaScene2 {
    private FloatBuffer vb; private ShortBuffer ib;
    private int nIdx, prog, tex;
    private int uMVP, uM, uId, uFr, uLit, uEd, uFl, uS, aP, aN, aT;
    private final long t0 = System.nanoTime();
    private final float[] model = new float[16], mv = new float[16], mvp = new float[16];

    private static byte[] readAll(InputStream in) throws java.io.IOException {
        java.io.ByteArrayOutputStream bo = new java.io.ByteArrayOutputStream();
        byte[] buf = new byte[8192];
        int r;
        while ((r = in.read(buf)) > 0) bo.write(buf, 0, r);
        in.close();
        return bo.toByteArray();
    }

    GodzillaScene2(Context ctx) {
        try {
            byte[] b = readAll(op(ctx, "godzilla_pet.bin"));
            ByteBuffer bb = ByteBuffer.wrap(b).order(ByteOrder.LITTLE_ENDIAN);
            bb.position(4);
            int nv = bb.getShort() & 0xFFFF, nt = bb.getInt();
            vb = ByteBuffer.allocateDirect(nv * 32).order(ByteOrder.nativeOrder()).asFloatBuffer();
            for (int i = 0; i < nv * 8; i++) vb.put(bb.getFloat());
            vb.position(0);
            nIdx = nt * 3;
            ib = ByteBuffer.allocateDirect(nIdx * 2).order(ByteOrder.nativeOrder()).asShortBuffer();
            for (int i = 0; i < nIdx; i++) ib.put(bb.getShort());
            ib.position(0);
            Bitmap bmp = BitmapFactory.decodeStream(op(ctx, "godzilla_pet.jpg"));
            int[] t = new int[1]; GLES20.glGenTextures(1, t, 0); tex = t[0];
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, tex);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bmp, 0); bmp.recycle();
        } catch (Exception e) { nIdx = 0; return; }
        // tanpa lekukan: model kaku, hanya gerak utuh + cahaya. vP = posisi model untuk menghitung urutan sirip
        String vs = "uniform mat4 uMVP,uM;attribute vec3 aP,aN;attribute vec2 aT;varying vec2 vT;varying float vL;varying vec3 vP;"
            + "void main(){vP=aP;gl_Position=uMVP*vec4(aP,1.);vT=aT;"
            + "vec3 n=normalize(mat3(uM)*aN);vL=.6+.55*max(dot(n,normalize(vec3(.3,.8,.6))),0.);}";
        // sirip biru (piksel dominan biru): menyala berurutan dari ujung ekor (s=0) ke leher (s=1); uFl = kilatan saat aum
        String fs = "precision mediump float;uniform sampler2D uS;uniform float uId,uFr,uLit,uEd,uFl;varying vec2 vT;varying float vL;varying vec3 vP;"
            + "void main(){vec3 c=texture2D(uS,vT).rgb;float g=max(c.b-c.r,0.);"
            + "float s=clamp((vP.z+.74)*.55+vP.y*.6,0.,1.);"
            + "float lit=uLit*(1.-smoothstep(uFr-.08,uFr,s));"
            + "float e=(s-uFr+.03)/.05;float edge=exp(-e*e)*uEd;"
            + "float m=uId+lit*1.6+edge*2.2+uFl*3.5;"
            + "gl_FragColor=vec4(c*vL*1.1+vec3(0.,.35,.6)*g*m+vec3(.5,.7,1.)*g*uFl*1.5,1.);}";
        prog = link(vs, fs);
        uMVP = GLES20.glGetUniformLocation(prog, "uMVP");
        uM = GLES20.glGetUniformLocation(prog, "uM");
        uId = GLES20.glGetUniformLocation(prog, "uId");
        uFr = GLES20.glGetUniformLocation(prog, "uFr");
        uLit = GLES20.glGetUniformLocation(prog, "uLit");
        uEd = GLES20.glGetUniformLocation(prog, "uEd");
        uFl = GLES20.glGetUniformLocation(prog, "uFl");
        uS = GLES20.glGetUniformLocation(prog, "uS");
        aP = GLES20.glGetAttribLocation(prog, "aP");
        aN = GLES20.glGetAttribLocation(prog, "aN");
        aT = GLES20.glGetAttribLocation(prog, "aT");
    }

    private static InputStream op(Context c, String n) throws java.io.IOException {
        try { return c.getAssets().open("characters/" + n); } catch (java.io.IOException e) { return c.getAssets().open(n); }
    }
    private static int sh(int type, String s) { int h = GLES20.glCreateShader(type); GLES20.glShaderSource(h, s); GLES20.glCompileShader(h); return h; }
    private static int link(String v, String f) {
        int p = GLES20.glCreateProgram();
        GLES20.glAttachShader(p, sh(GLES20.GL_VERTEX_SHADER, v));
        GLES20.glAttachShader(p, sh(GLES20.GL_FRAGMENT_SHADER, f));
        GLES20.glLinkProgram(p);
        return p;
    }
    private static final float CYC = 14f;
    private static final float[] KT = {0f, 8.5f, 11.5f, 12f, 12.35f, 14f}, KV = {0f, 0f, 3f, 3f, -6f, 0f};
    private static float kf(float c, float[] T, float[] V) {
        for (int i = 0; i < T.length - 1; i++)
            if (c <= T[i + 1]) { float k = smooth01(T[i], T[i + 1], c); return V[i] + (V[i + 1] - V[i]) * k; }
        return V[V.length - 1];
    }
    private static float smooth01(float a, float b, float x) { float k = Math.max(0f, Math.min(1f, (x - a) / (b - a))); return k * k * (3f - 2f * k); }

    /** Satuan dunia avatar: tinggi karakter ~90, pusat y=0 (lantai y=-47), kamera di +Z. */
    void draw(float[] view, float[] proj) {
        if (nIdx == 0) return;
        float t = (System.nanoTime() - t0) / 1e9f;
        float ap = smooth01(0f, 0.9f, t);                                   // muncul pelan dari kecil
        float c = t % CYC;
        float lean = kf(c, KT, KV);                                         // maju saat tarik napas, condong ke belakang saat aum
        float fr = c < 8.5f ? 0f : Math.min(1.1f, (c - 8.5f) / 2.5f * 1.1f);   // ujung cahaya merambat ekor -> leher
        float lit = c < 8.5f ? 0f : c < 12.3f ? 1f : 1f - smooth01(12.3f, 14f, c);
        float ed = (c >= 8.5f && c < 11.2f) ? 1f : 0f;
        float fl = c < 12f ? 0f : c < 12.3f ? smooth01(12f, 12.3f, c) : 1f - smooth01(12.3f, 14f, c);
        float id = 0.9f + 0.25f * (float) Math.sin(t * 1.3f) + 0.35f * lit * (float) Math.sin(t * 5f) * (c < 12f ? 1f : 0f);
        float sc = 36f * (0.3f + 0.7f * ap);
        Matrix.setIdentityM(model, 0);
        Matrix.translateM(model, 0, 34f + 0.4f * (float) Math.sin(t * 0.7f), -47f + 0.25f * (float) Math.sin(t * 1.2f), 8f);   // napas & bergeser pelan
        Matrix.rotateM(model, 0, -14f + 3f * (float) Math.sin(t * 0.4f), 0f, 1f, 0f);      // menghadap kamera, agak ke avatar
        Matrix.rotateM(model, 0, 1.4f * (float) Math.sin(t * 0.7f), 0f, 0f, 1f);           // menumpu kiri-kanan bergantian
        Matrix.rotateM(model, 0, lean, 1f, 0f, 0f);
        Matrix.scaleM(model, 0, sc, sc, sc);
        Matrix.multiplyMM(mv, 0, view, 0, model, 0);
        Matrix.multiplyMM(mvp, 0, proj, 0, mv, 0);

        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        GLES20.glUseProgram(prog);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);
        GLES20.glUniformMatrix4fv(uMVP, 1, false, mvp, 0);
        GLES20.glUniformMatrix4fv(uM, 1, false, model, 0);
        GLES20.glUniform1f(uId, id);
        GLES20.glUniform1f(uFr, fr);
        GLES20.glUniform1f(uLit, lit);
        GLES20.glUniform1f(uEd, ed);
        GLES20.glUniform1f(uFl, fl);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, tex);
        GLES20.glUniform1i(uS, 0);
        vb.position(0); GLES20.glVertexAttribPointer(aP, 3, GLES20.GL_FLOAT, false, 32, vb); GLES20.glEnableVertexAttribArray(aP);
        vb.position(3); GLES20.glVertexAttribPointer(aN, 3, GLES20.GL_FLOAT, false, 32, vb); GLES20.glEnableVertexAttribArray(aN);
        vb.position(6); GLES20.glVertexAttribPointer(aT, 2, GLES20.GL_FLOAT, false, 32, vb); GLES20.glEnableVertexAttribArray(aT);
        ib.position(0);
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, nIdx, GLES20.GL_UNSIGNED_SHORT, ib);
        GLES20.glDisableVertexAttribArray(aP); GLES20.glDisableVertexAttribArray(aN); GLES20.glDisableVertexAttribArray(aT);
    }
}
