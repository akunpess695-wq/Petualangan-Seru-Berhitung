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

/** Naga pendamping yang digambar DI DALAM adegan 3D avatar (PutriGLView), memakai depth buffer yang sama,
 *  sehingga naga bisa lewat di belakang avatar. Semua pemanggilan di thread GL.
 *  Red Dragon by GadgetHamster (CC-BY 4.0). */
final class NagaScene {
    // reaksi (diatur dari NagaPetView.reactAll, thread UI)
    static volatile int rType = 0;
    static volatile long rT0 = 0;

    private FloatBuffer vb; private ShortBuffer ib;
    private int nIdx, prog, sprog, tex;
    private float ccx, ccy, ccz, halfX = 0.5f;
    private final long t0 = System.nanoTime();
    private long lastN = 0;
    private float hx = 0f, hz = 1f, pitchS = 0f, rollS = 0f;   // arah hadap (vektor) & sudut yang dihaluskan
    private final float[] model = new float[16], mv = new float[16], mvp = new float[16], vp = new float[16];

    NagaScene(Context ctx) {
        try {
            InputStream in = op(ctx, "dragon_pet.bin");
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
            float mnx = 1e9f, mxx = -1e9f, mny = 1e9f, mxy = -1e9f, mnz = 1e9f, mxz = -1e9f;
            for (int i = 0; i < nv; i++) {
                float x = vb.get(i * 8), y = vb.get(i * 8 + 1), z = vb.get(i * 8 + 2);
                if (x < mnx) mnx = x; if (x > mxx) mxx = x;
                if (y < mny) mny = y; if (y > mxy) mxy = y;
                if (z < mnz) mnz = z; if (z > mxz) mxz = z;
            }
            ccx = (mnx + mxx) / 2; ccy = (mny + mxy) / 2; ccz = (mnz + mxz) / 2; halfX = Math.max(0.01f, (mxx - mnx) / 2);
            Bitmap bmp = BitmapFactory.decodeStream(op(ctx, "dragon_pet.jpg"));
            int[] t = new int[1]; GLES20.glGenTextures(1, t, 0); tex = t[0];
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, tex);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bmp, 0); bmp.recycle();
        } catch (Exception e) { nIdx = 0; return; }
        String vs = "uniform mat4 uMVP,uM;uniform float uFl,uCx,uHx;attribute vec3 aP,aN;attribute vec2 aT;varying vec2 vT;varying float vL;"
            + "void main(){vec3 p=aP;float k=abs(p.x-uCx)/uHx;p.y+=uFl*uHx*k*k;gl_Position=uMVP*vec4(p,1.);vT=aT;"
            + "vec3 n=normalize(mat3(uM)*aN);vL=.55+.6*max(dot(n,normalize(vec3(.3,.8,.6))),0.);}";
        String fs = "precision mediump float;uniform sampler2D uS;varying vec2 vT;varying float vL;"
            + "void main(){vec4 c=texture2D(uS,vT);gl_FragColor=vec4(c.rgb*vL*1.15,1.);}";
        prog = link(vs, fs);
        String svs = "uniform mat4 uMVP;uniform float uSz;attribute vec3 aP;void main(){gl_Position=uMVP*vec4(aP,1.);gl_PointSize=uSz;}";
        String sfs = "precision mediump float;uniform float uA;void main(){vec2 d=gl_PointCoord-vec2(.5);float r=length(d)*2.;"
            + "if(r>1.)discard;gl_FragColor=vec4(1.,.92,.45,1.)*(1.-r*.5)*uA;}";
        sprog = link(svs, sfs);
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

    /** Lintasan (satuan dunia avatar: tinggi karakter ~90, pusat di y=0, kamera di +Z). Hasil: x,y,z, fl(terbang 1..0), sudut. */
    private static float[] path(float t) {
        float T1 = 9f, T2 = 2.8f;
        float u = t < T1 ? 0f : Math.min(1f, (t - T1) / T2);
        float e = u * u * (3f - 2f * u), fl = 1f - e;
        float ang = t * 6.2832f / 4.5f;
        float rx = 34f, rz = 46f;
        float x = fl * rx * (float) Math.cos(ang);
        float z = fl * rz * (float) Math.sin(ang);
        float y = 4f + 40f * e + fl * 12f * (float) Math.sin(ang * 1.0f + 1.2f);
        return new float[] { x, y, z, fl, ang };
    }

    void draw(float[] view, float[] proj) {
        if (nIdx == 0) return;
        float t = (System.nanoTime() - t0) / 1e9f;
        long nn = System.nanoTime();
        float dt = lastN == 0 ? 0.016f : Math.min(0.1f, (nn - lastN) / 1e9f); lastN = nn;
        float[] p0 = path(t), p1 = path(t + 0.05f);
        float fl = p0[3];
        float vx = (p1[0] - p0[0]) / 0.05f, vy = (p1[1] - p0[1]) / 0.05f, vz = (p1[2] - p0[2]) / 0.05f;
        float sp = (float) Math.sqrt(vx * vx + vz * vz);
        // arah hadap: searah terbang, makin menghadap kamera (+Z) saat hinggap
        float tx = sp > 1e-3f ? vx / sp : 0f, tz = sp > 1e-3f ? vz / sp : 1f;
        tx = tx * fl; tz = tz * fl + (1f - fl) * 1f;
        float a1 = 1f - (float) Math.exp(-dt * 4f);
        hx += (tx - hx) * a1; hz += (tz - hz) * a1;
        float yaw = (float) Math.toDegrees(Math.atan2(-hx, -hz));
        float pitchT = 18f * Math.max(-1f, Math.min(1f, vy / 25f)) * fl - (1f - fl) * 3f;
        float rollT = -16f * fl;
        pitchS += (pitchT - pitchS) * a1; rollS += (rollT - rollS) * a1;

        float dy = 0f, tilt = 0f, rp = -1f; int rt = rType;
        if (rt != 0) {
            float dur = rt == 1 ? 0.9f : 1.1f;
            rp = (nn - rT0) / 1e9f / dur;
            if (rp >= 1f) { rType = 0; rp = -1f; }
            else if (rt == 1) { dy = 12f * (float) Math.abs(Math.sin(rp * Math.PI * 2f)) * (1f - rp); tilt = -10f * (float) Math.sin(rp * Math.PI); }
            else { dy = -5f * (float) Math.sin(rp * Math.PI); tilt = 14f * (float) Math.sin(rp * Math.PI); }
        }
        float flap = (0.08f + 0.38f * fl) * (float) Math.sin(t * (2.5f + 7f * fl));
        float span = 44f + 16f * fl;                                  // lebar sayap (satuan dunia)
        float sc = span / (2f * halfX);
        float py = p0[1] + dy + (1f - fl) * 0.6f * (float) Math.sin(t * 2f);

        Matrix.setIdentityM(model, 0);
        Matrix.translateM(model, 0, p0[0], py, p0[2]);
        Matrix.rotateM(model, 0, yaw, 0f, 1f, 0f);
        Matrix.rotateM(model, 0, pitchS + tilt, 1f, 0f, 0f);
        Matrix.rotateM(model, 0, rollS, 0f, 0f, 1f);
        Matrix.scaleM(model, 0, sc, sc, sc);
        Matrix.translateM(model, 0, -ccx, -ccy, -ccz);
        Matrix.multiplyMM(mv, 0, view, 0, model, 0);
        Matrix.multiplyMM(mvp, 0, proj, 0, mv, 0);

        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        GLES20.glUseProgram(prog);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);
        GLES20.glUniformMatrix4fv(GLES20.glGetUniformLocation(prog, "uMVP"), 1, false, mvp, 0);
        GLES20.glUniformMatrix4fv(GLES20.glGetUniformLocation(prog, "uM"), 1, false, model, 0);
        GLES20.glUniform1f(GLES20.glGetUniformLocation(prog, "uFl"), flap);
        GLES20.glUniform1f(GLES20.glGetUniformLocation(prog, "uCx"), ccx);
        GLES20.glUniform1f(GLES20.glGetUniformLocation(prog, "uHx"), halfX);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, tex);
        GLES20.glUniform1i(GLES20.glGetUniformLocation(prog, "uS"), 0);
        int p = GLES20.glGetAttribLocation(prog, "aP"), n = GLES20.glGetAttribLocation(prog, "aN"), u = GLES20.glGetAttribLocation(prog, "aT");
        vb.position(0); GLES20.glVertexAttribPointer(p, 3, GLES20.GL_FLOAT, false, 32, vb); GLES20.glEnableVertexAttribArray(p);
        vb.position(3); GLES20.glVertexAttribPointer(n, 3, GLES20.GL_FLOAT, false, 32, vb); GLES20.glEnableVertexAttribArray(n);
        vb.position(6); GLES20.glVertexAttribPointer(u, 2, GLES20.GL_FLOAT, false, 32, vb); GLES20.glEnableVertexAttribArray(u);
        ib.position(0);
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, nIdx, GLES20.GL_UNSIGNED_SHORT, ib);
        GLES20.glDisableVertexAttribArray(p); GLES20.glDisableVertexAttribArray(n); GLES20.glDisableVertexAttribArray(u);

        if (rp >= 0f && rt == 1) sparkle(view, proj, p0[0], py, p0[2], rp);
    }

    private void sparkle(float[] view, float[] proj, float cx, float cy, float cz, float rp) {
        final int NSP = 10;
        float[] pts = new float[NSP * 3];
        for (int i = 0; i < NSP; i++) {
            float a = i * 6.2832f / NSP + 0.6f;
            float rad = 12f + 30f * rp;
            pts[i * 3] = cx + rad * (float) Math.cos(a);
            pts[i * 3 + 1] = cy + 10f + 22f * rp + 6f * (float) Math.sin(a * 2f);
            pts[i * 3 + 2] = cz + 8f * (float) Math.sin(a);
        }
        FloatBuffer pb = ByteBuffer.allocateDirect(pts.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        pb.put(pts).position(0);
        Matrix.multiplyMM(vp, 0, proj, 0, view, 0);
        GLES20.glUseProgram(sprog);
        GLES20.glDisable(GLES20.GL_DEPTH_TEST);
        GLES20.glUniformMatrix4fv(GLES20.glGetUniformLocation(sprog, "uMVP"), 1, false, vp, 0);
        GLES20.glUniform1f(GLES20.glGetUniformLocation(sprog, "uSz"), 18f + 14f * (1f - rp));
        GLES20.glUniform1f(GLES20.glGetUniformLocation(sprog, "uA"), 1f - rp);
        int ap = GLES20.glGetAttribLocation(sprog, "aP");
        GLES20.glVertexAttribPointer(ap, 3, GLES20.GL_FLOAT, false, 12, pb);
        GLES20.glEnableVertexAttribArray(ap);
        GLES20.glDrawArrays(GLES20.GL_POINTS, 0, NSP);
        GLES20.glDisableVertexAttribArray(ap);
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
    }
}
