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
final class GodzillaScene {
    private FloatBuffer vb; private ShortBuffer ib;
    private int nIdx, prog, tex;
    private final long t0 = System.nanoTime();
    private final float[] model = new float[16], mv = new float[16], mvp = new float[16];

    GodzillaScene(Context ctx) {
        try {
            InputStream in = op(ctx, "godzilla_pet.bin");
            byte[] b = new byte[in.available()]; int n = 0;
            while (n < b.length) { int k = in.read(b, n, b.length - n); if (k < 0) break; n += k; }
            in.close();
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
        // hanya ekor yang melenggok: gelombang pelan dan kecil, makin ke ujung makin besar (z<0)
        String vs = "uniform mat4 uMVP,uM;uniform float uTP;attribute vec3 aP,aN;attribute vec2 aT;varying vec2 vT;varying float vL;"
            + "void main(){vec3 p=aP;"
            + "float tw=clamp(-p.z/.7,0.,1.);tw*=tw;p.x+=.035*sin(uTP-p.z*3.)*tw;"
            + "gl_Position=uMVP*vec4(p,1.);vT=aT;"
            + "vec3 n=normalize(mat3(uM)*aN);vL=.6+.55*max(dot(n,normalize(vec3(.3,.8,.6))),0.);}";
        // duri biru berdenyut (piksel yang dominan biru), lebih terang saat aum
        String fs = "precision mediump float;uniform sampler2D uS;uniform float uGl;varying vec2 vT;varying float vL;"
            + "void main(){vec3 c=texture2D(uS,vT).rgb;float g=max(c.b-c.r,0.);"
            + "gl_FragColor=vec4(c*vL*1.1+vec3(0.,.35,.6)*g*uGl,1.);}";
        prog = link(vs, fs);
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
    private static float smooth01(float a, float b, float x) { float k = Math.max(0f, Math.min(1f, (x - a) / (b - a))); return k * k * (3f - 2f * k); }

    /** Satuan dunia avatar: tinggi karakter ~90, pusat y=0 (lantai y=-47), kamera di +Z. */
    void draw(float[] view, float[] proj) {
        if (nIdx == 0) return;
        float t = (System.nanoTime() - t0) / 1e9f;
        float ap = smooth01(0f, 0.9f, t);                                   // muncul pelan dari kecil
        float cyc = (t % 12f) / 12f;                                        // aum tiap 12 detik
        float ro = cyc < 0.7f ? 0f : (float) Math.sin(Math.PI * (cyc - 0.7f) / 0.3f);
        ro = ro * ro * (3f - 2f * ro);                                      // naik-turun halus
        float sc = 36f * (0.3f + 0.7f * ap);
        Matrix.setIdentityM(model, 0);
        Matrix.translateM(model, 0, 34f + 0.4f * (float) Math.sin(t * 0.7f), -47f, 8f);    // berat badan bergeser pelan
        Matrix.rotateM(model, 0, -14f + 4f * (float) Math.sin(t * 0.4f), 0f, 1f, 0f);      // menghadap kamera, agak ke avatar
        Matrix.rotateM(model, 0, 1.6f * (float) Math.sin(t * 0.7f), 0f, 0f, 1f);           // menumpu kiri-kanan bergantian
        Matrix.rotateM(model, 0, -5f * ro, 1f, 0f, 0f);                                    // aum: condong ke belakang
        Matrix.scaleM(model, 0, sc, sc, sc);
        Matrix.multiplyMM(mv, 0, view, 0, model, 0);
        Matrix.multiplyMM(mvp, 0, proj, 0, mv, 0);

        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        GLES20.glUseProgram(prog);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);
        GLES20.glUniformMatrix4fv(GLES20.glGetUniformLocation(prog, "uMVP"), 1, false, mvp, 0);
        GLES20.glUniformMatrix4fv(GLES20.glGetUniformLocation(prog, "uM"), 1, false, model, 0);
        GLES20.glUniform1f(GLES20.glGetUniformLocation(prog, "uTP"), t * 1.1f);
        GLES20.glUniform1f(GLES20.glGetUniformLocation(prog, "uGl"), 1.2f + 0.5f * (float) Math.sin(t * 2.4f) + 2.5f * ro);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, tex);
        GLES20.glUniform1i(GLES20.glGetUniformLocation(prog, "uS"), 0);
        int p = GLES20.glGetAttribLocation(prog, "aP"), n = GLES20.glGetAttribLocation(prog, "aN"), uv = GLES20.glGetAttribLocation(prog, "aT");
        vb.position(0); GLES20.glVertexAttribPointer(p, 3, GLES20.GL_FLOAT, false, 32, vb); GLES20.glEnableVertexAttribArray(p);
        vb.position(3); GLES20.glVertexAttribPointer(n, 3, GLES20.GL_FLOAT, false, 32, vb); GLES20.glEnableVertexAttribArray(n);
        vb.position(6); GLES20.glVertexAttribPointer(uv, 2, GLES20.GL_FLOAT, false, 32, vb); GLES20.glEnableVertexAttribArray(uv);
        ib.position(0);
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, nIdx, GLES20.GL_UNSIGNED_SHORT, ib);
        GLES20.glDisableVertexAttribArray(p); GLES20.glDisableVertexAttribArray(n); GLES20.glDisableVertexAttribArray(uv);
    }
}
