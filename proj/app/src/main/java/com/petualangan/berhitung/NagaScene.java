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
    private float ccx, ccy, ccz, halfX = 0.5f, halfZ = 0.34f;
    private float phF = 0f, phT = 0f;                              // fase kepak sayap & ayun ekor (diintegralkan agar tidak melompat)
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
            halfZ = Math.max(0.01f, (mxz - mnz) / 2);
            Bitmap bmp = BitmapFactory.decodeStream(op(ctx, "dragon_pet.jpg"));
            int[] t = new int[1]; GLES20.glGenTextures(1, t, 0); tex = t[0];
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, tex);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bmp, 0); bmp.recycle();
        } catch (Exception e) { nIdx = 0; return; }
        String vs = "uniform mat4 uMVP,uM;uniform float uPh,uAmp,uTP,uTA,uTO,uCx,uHx,uCz,uHz;attribute vec3 aP,aN;attribute vec2 aT;varying vec2 vT;varying float vL;"
            + "void main(){vec3 p=aP;float k=abs(p.x-uCx)/uHx;"
            // sayap: gelombang kepak merambat dari pangkal ke ujung (ujung tertinggal), turun lebih kuat dari naik, ujung menyapu & menguncup
            + "float ph=uPh-k*1.1;float st=sin(ph)+.28*sin(2.*ph+.6);"
            + "p.y+=uAmp*uHx*(.15*k+.85*k*k)*st;"
            + "p.z+=uAmp*uHx*.22*k*k*sin(ph-.9);"
            + "p.x=uCx+(p.x-uCx)*(1.-.35*uAmp*k*max(st,0.));"
            // badan tengah: ekor melenggok (gelombang berjalan), kepala menyeimbangkan & mengangguk
            + "float b=1.-smoothstep(.04,.2,k);float zt=(p.z-uCz)/uHz;"
            + "float tw=clamp(zt,0.,1.);tw=tw*tw;float wv=uTP-zt*2.8;"
            + "p.x+=(uTA*sin(wv)+uTO)*uHz*tw*b;p.y+=uTA*.45*sin(wv+1.4)*uHz*tw*b;"
            + "float hw=clamp(-zt-.78,0.,.22)/.22;"
            + "p.x-=.3*uTA*sin(wv+.9)*uHz*hw*b;p.y+=uAmp*.35*uHz*hw*sin(uPh+1.5)*b;"
            + "gl_Position=uMVP*vec4(p,1.);vT=aT;"
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

    private static final float W = 6.2832f / 4f;            // laju putar dasar (rad/dtk): 1 putaran = 4 dtk
    private static final float D = 3.4f, EY = 46f;          // lama pendaratan (dtk), tinggi hinggap di atas kepala
    private static final float T1;                           // saat mulai mendarat: tepat ketika naga di BELAKANG avatar
    private static final float[] S, V0;                      // posisi & kecepatan naga saat mulai mendarat

    private static float angOf(float t) { return t * W + 0.35f * (float) Math.sin(t * 0.9f); }

    /** Orbit mengelilingi avatar. Sayap (lebar 40) dijauhkan dari badan: rx 44 -> ujung dalam >= 22 dari sumbu avatar. */
    private static float[] orb(float t) {
        float a = angOf(t);
        float rx = 44f + 2f * (float) Math.sin(t * 0.7f), rz = 52f + 2.5f * (float) Math.sin(t * 0.53f + 1f);
        return new float[] { rx * (float) Math.cos(a), 31f + 6f * (float) Math.sin(a + 1.2f) + 2f * (float) Math.sin(t * 1.7f),
                             rz * (float) Math.sin(a) };
    }

    static {
        // cari t (>= 7 dtk) pertama saat sudut orbit = 3pi/2 (z paling jauh = di belakang avatar, bergerak ke kanan)
        double base = 1.5 * Math.PI;
        int n = (int) Math.ceil((angOf(7f) - base) / (2 * Math.PI));
        double tgt = base + 2 * Math.PI * n, lo = 7, hi = 20;
        for (int i = 0; i < 60; i++) { double m = (lo + hi) / 2; if (angOf((float) m) < tgt) lo = m; else hi = m; }
        T1 = (float) hi;
        S = orb(T1);
        float[] o2 = orb(T1 + 0.01f);
        V0 = new float[] { (o2[0] - S[0]) / 0.01f, (o2[1] - S[1]) / 0.01f, (o2[2] - S[2]) / 0.01f };
    }

    /** Lintasan (satuan dunia avatar: tinggi karakter ~90, pusat di y=0, kamera di +Z).
     *  Orbit sampai T1 (naga di belakang avatar), lalu kurva Hermite halus: dari belakang, naik ke atas kepala, hinggap menghadap kamera.
     *  Hasil: x, y, z, fl (terbang 1..0), sudut, u (kemajuan mendarat 0..1), rate (laju putar relatif). */
    private static float[] path(float t) {
        if (t <= T1) {
            float[] o = orb(t);
            float rate = (W + 0.315f * (float) Math.cos(t * 0.9f)) / W;
            return new float[] { o[0], o[1], o[2], 1f, angOf(t), 0f, rate };
        }
        float s = Math.min(1f, (t - T1) / D);
        float h00 = 2 * s * s * s - 3 * s * s + 1, h10 = s * s * s - 2 * s * s + s, h01 = -2 * s * s * s + 3 * s * s, h11 = s * s * s - s * s;
        float ex = 0f, ey = EY, ez = 0f, vex = 0f, vey = 0f, vez = 12f;                 // titik akhir & kecepatan akhir (menuju kamera)
        float x = h00 * S[0] + h10 * D * V0[0] + h01 * ex + h11 * D * vex;
        float y = h00 * S[1] + h10 * D * V0[1] + h01 * ey + h11 * D * vey;
        float z = h00 * S[2] + h10 * D * V0[2] + h01 * ez + h11 * D * vez;
        float fl = 1f - smooth01(0.3f, 1f, s);
        float rate = (W + 0.315f * (float) Math.cos(T1 * 0.9f)) / W * (1f - s);
        return new float[] { x, y, z, fl, angOf(T1), s, rate };
    }

    private static float stroke(float ph) { return (float) (Math.sin(ph) + 0.28 * Math.sin(2 * ph + 0.6)); }
    private static float smooth01(float a, float b, float x) { float k = Math.max(0f, Math.min(1f, (x - a) / (b - a))); return k * k * (3f - 2f * k); }

    void draw(float[] view, float[] proj) {
        if (nIdx == 0) return;
        float t = (System.nanoTime() - t0) / 1e9f;
        long nn = System.nanoTime();
        float dt = lastN == 0 ? 0.016f : Math.min(0.1f, (nn - lastN) / 1e9f); lastN = nn;
        float[] p0 = path(t), p1 = path(t + 0.05f);
        float fl = p0[3], u = p0[5], rate = p0[6];
        float vx = (p1[0] - p0[0]) / 0.05f, vy = (p1[1] - p0[1]) / 0.05f, vz = (p1[2] - p0[2]) / 0.05f;
        float sp = (float) Math.sqrt(vx * vx + vz * vz);
        // arah hadap: searah terbang, makin menghadap kamera (+Z) saat hinggap
        float tx = sp > 1e-3f ? vx / sp : 0f, tz = sp > 1e-3f ? vz / sp : 1f;
        float wb = smooth01(0.55f, 1f, u);                           // mendarat: arah hadap pelan-pelan beralih menghadap kamera
        tx = tx * (1f - wb); tz = tz * (1f - wb) + wb;
        float a1 = 1f - (float) Math.exp(-dt * 4f);
        hx += (tx - hx) * a1; hz += (tz - hz) * a1;
        float yaw = (float) Math.toDegrees(Math.atan2(-hx, -hz));
        yaw += (1f - fl) * (9f * (float) Math.sin(t * 0.62f) + 3f * (float) Math.sin(t * 1.7f));   // hinggap: menoleh pelan ke kiri-kanan

        // fase pergerakan: terbang = kepak-kepak lalu meluncur; hinggap = diam, sesekali mengibas; mendarat = kepak kuat (flare)
        float burst = smooth01(0f, 1f, 0.5f + 0.5f * (float) Math.sin(t * 1.15f + 0.5f));
        float pulse = (float) Math.pow(Math.max(0.0, Math.sin(t * 1.25f)), 10.0);
        float flare = (float) Math.sin(Math.PI * smooth01(0.45f, 1f, u));
        float ampP = 0.05f + 0.15f * pulse, ampF = 0.17f + 0.19f * burst;
        float omP = 3.5f + 5f * pulse, omF = 6.2f + 3.6f * burst;
        float amp = ampP + (ampF - ampP) * fl + 0.16f * flare;
        float om = omP + (omF - omP) * fl + 5f * flare;

        float dy = 0f, tilt = 0f, rp = -1f; int rt = rType;
        if (rt != 0) {
            float dur = rt == 1 ? 0.9f : 1.1f;
            rp = (nn - rT0) / 1e9f / dur;
            if (rp >= 1f) { rType = 0; rp = -1f; }
            else if (rt == 1) {
                dy = 12f * (float) Math.abs(Math.sin(rp * Math.PI * 2f)) * (1f - rp); tilt = -10f * (float) Math.sin(rp * Math.PI);
                amp += 0.22f * (float) Math.sin(rp * Math.PI); om += 3f;                 // senang: sayap mengepak
            } else {
                dy = -5f * (float) Math.sin(rp * Math.PI); tilt = 14f * (float) Math.sin(rp * Math.PI);
                amp *= 1f - 0.65f * (float) Math.sin(rp * Math.PI);                        // sedih: sayap terkulai
            }
        }
        phF = (phF + dt * om) % 6.2831853f;
        phT = (phT + dt * (2.2f + 1.4f * fl)) % 6.2831853f;
        float tAmp = 0.16f + 0.10f * fl;
        float tOff = -0.10f * fl * rate;                              // saat berbelok, ekor tertinggal ke sisi luar

        // kemiringan: miring ke dalam belokan, naik-turun mengikuti ketinggian, flare saat mendarat; goyang halus mengikuti kepakan
        float pitchT = 18f * Math.max(-1f, Math.min(1f, vy / 25f)) * fl - (1f - fl) * 3f + 24f * flare;
        float rollT = -16f * fl * rate * rate;
        pitchS += (pitchT - pitchS) * a1; rollS += (rollT - rollS) * a1;
        float pitch = pitchS + tilt + 2.5f * fl * (float) Math.sin(phF + 0.4f);
        float roll = rollS + 2.2f * fl * (float) Math.sin(phF);

        float ap = smooth01(0f, 0.8f, t);                             // muncul pelan dari kecil
        float span = (40f + 4f * (1f - fl)) * (0.35f + 0.65f * ap);        // lebar sayap (satuan dunia)
        float sc = span / (2f * halfX) * (1f + (1f - fl) * 0.012f * (float) Math.sin(t * 2.6f));   // hinggap: bernapas
        // badan naik saat sayap menekan ke bawah, turun saat sayap terangkat; melayang saat hinggap
        float bob = -1.6f * fl * (amp / 0.36f) * stroke(phF - 0.4f)
                  + (1f - fl) * (1.0f * (float) Math.sin(t * 1.9f) + 0.4f * (float) Math.sin(t * 3.3f));
        float py = p0[1] + dy + bob;

        Matrix.setIdentityM(model, 0);
        Matrix.translateM(model, 0, p0[0], py, p0[2]);
        Matrix.rotateM(model, 0, yaw, 0f, 1f, 0f);
        Matrix.rotateM(model, 0, pitch, 1f, 0f, 0f);
        Matrix.rotateM(model, 0, roll, 0f, 0f, 1f);
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
        GLES20.glUniform1f(GLES20.glGetUniformLocation(prog, "uPh"), phF);
        GLES20.glUniform1f(GLES20.glGetUniformLocation(prog, "uAmp"), amp);
        GLES20.glUniform1f(GLES20.glGetUniformLocation(prog, "uTP"), phT);
        GLES20.glUniform1f(GLES20.glGetUniformLocation(prog, "uTA"), tAmp);
        GLES20.glUniform1f(GLES20.glGetUniformLocation(prog, "uTO"), tOff);
        GLES20.glUniform1f(GLES20.glGetUniformLocation(prog, "uCx"), ccx);
        GLES20.glUniform1f(GLES20.glGetUniformLocation(prog, "uHx"), halfX);
        GLES20.glUniform1f(GLES20.glGetUniformLocation(prog, "uCz"), ccz);
        GLES20.glUniform1f(GLES20.glGetUniformLocation(prog, "uHz"), halfZ);
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
