package com.petualangan.berhitung; // sesuaikan dengan paket MainActivity.java

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.view.MotionEvent;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/** Naga merah kecil (pet). Aset: assets/dragon_pet.bin + assets/dragon_pet.jpg.
 *  Red Dragon by GadgetHamster (CC-BY 4.0) - tampilkan di layar Kredit. */
public class NagaPetView extends GLSurfaceView {
    private final R r;
    public NagaPetView(Context c) { this(c, false); }
    /** inline=true: dipasang di dalam kartu (latar ungu muda, bukan melayang). */
    public NagaPetView(Context c, boolean inline) { this(c, inline ? 1 : 0); }
    /** mode 0 = melayang di atas semua; 1 = di dalam kartu (latar ungu); 2 = pendamping transparan di atas avatar. */
    public NagaPetView(Context c, int mode) {
        super(c);
        setEGLContextClientVersion(2);
        setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        getHolder().setFormat(android.graphics.PixelFormat.TRANSLUCENT);
        if (mode == 0) setZOrderOnTop(true);
        else if (mode == 2) setZOrderMediaOverlay(true);
        r = new R(c, mode == 1, mode == 2);
        setRenderer(r);
        synchronized (ALL) { ALL.add(new java.lang.ref.WeakReference<NagaPetView>(this)); }
    }

    private static final java.util.ArrayList<java.lang.ref.WeakReference<NagaPetView>> ALL =
            new java.util.ArrayList<java.lang.ref.WeakReference<NagaPetView>>();

    /** Reaksi naga: benar = lompat kecil + kilau; salah = menunduk pelan (tanpa hukuman). */
    public void react(boolean benar) {
        r.reactType = benar ? 1 : 2;
        r.reactT0 = System.nanoTime();
    }
    /** Semua naga yang sedang hidup bereaksi (overlay maupun pendamping). Aman dipanggil dari thread UI. */
    public static void reactAll(boolean benar) {
        synchronized (ALL) {
            for (int i = ALL.size() - 1; i >= 0; i--) {
                NagaPetView v = ALL.get(i).get();
                if (v == null) ALL.remove(i); else v.react(benar);
            }
        }
    }
    /** Tidak menangkap sentuhan: tombol di bawahnya tetap bisa ditekan. */
    @Override public boolean onTouchEvent(MotionEvent e) { return false; }

    /** Naga melayang kecil di pojok kanan bawah, dipasang di decorView (bertahan antar layar), tidak menangkap sentuhan. */
    public static NagaPetView overlay(android.app.Activity a) {
        float d = a.getResources().getDisplayMetrics().density;
        NagaPetView v = new NagaPetView(a, false);
        android.widget.FrameLayout.LayoutParams p = new android.widget.FrameLayout.LayoutParams(
                (int) (120 * d), (int) (84 * d), android.view.Gravity.BOTTOM | android.view.Gravity.END);
        p.bottomMargin = (int) (64 * d);
        ((android.view.ViewGroup) a.getWindow().getDecorView()).addView(v, p);
        return v;
    }

    /** Pasang pet di pojok kanan bawah. Ketuk = tampil kredit; tekan lama = sembunyikan/tampilkan (tersimpan). */
    public static void attach(final android.app.Activity a) {
        final android.content.SharedPreferences sp = a.getSharedPreferences("naga_pet", 0);
        final float d = a.getResources().getDisplayMetrics().density;
        final android.widget.FrameLayout box = new android.widget.FrameLayout(a);
        final NagaPetView v = new NagaPetView(a);
        box.addView(v);
        final android.view.View tap = new android.view.View(a);
        box.addView(tap);
        tap.setOnClickListener(new android.view.View.OnClickListener() { public void onClick(android.view.View x) {
            android.widget.Toast.makeText(a, "Naga Merah - Red Dragon by GadgetHamster (CC-BY 4.0)", android.widget.Toast.LENGTH_LONG).show(); } });
        tap.setOnLongClickListener(new android.view.View.OnLongClickListener() { public boolean onLongClick(android.view.View x) {
            v.setVisibility(android.view.View.GONE); sp.edit().putBoolean("hide", true).apply();
            android.widget.Toast.makeText(a, "Naga disembunyikan (buka lagi lewat pengaturan)", android.widget.Toast.LENGTH_SHORT).show(); return true; } });
        if (sp.getBoolean("hide", false)) box.setVisibility(android.view.View.GONE);
        a.addContentView(box, new android.widget.FrameLayout.LayoutParams((int) (160 * d), (int) (110 * d),
            android.view.Gravity.BOTTOM | android.view.Gravity.END));
    }
    /** Panggil dari tombol pengaturan untuk menampilkan kembali naga. */
    public static void tampilkanLagi(android.app.Activity a) {
        a.getSharedPreferences("naga_pet", 0).edit().putBoolean("hide", false).apply();
        a.recreate();
    }

    static class R implements Renderer {
        void drawSparkle(float rp) {
            float[] pts = new float[NSP * 3];
            for (int i = 0; i < NSP; i++) {
                float a = i * 6.2832f / NSP + 0.6f;
                float rad = 0.25f + 0.55f * rp;
                pts[i * 3] = sx + sk * rad * (float) Math.cos(a);
                pts[i * 3 + 1] = sy + sk * (0.25f + 0.45f * rp + 0.12f * (float) Math.sin(a * 2f));
                pts[i * 3 + 2] = 0.15f * (float) Math.sin(a);
            }
            FloatBuffer pb = ByteBuffer.allocateDirect(pts.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
            pb.put(pts).position(0);
            float[] m2 = new float[16];
            Matrix.multiplyMM(m2, 0, proj, 0, view, 0);
            GLES20.glUseProgram(sprog);
            GLES20.glDisable(GLES20.GL_DEPTH_TEST);
            GLES20.glUniformMatrix4fv(GLES20.glGetUniformLocation(sprog, "uMVP"), 1, false, m2, 0);
            GLES20.glUniform1f(GLES20.glGetUniformLocation(sprog, "uSz"), 14f + 10f * (1f - rp));
            GLES20.glUniform1f(GLES20.glGetUniformLocation(sprog, "uA"), 1f - rp);
            int ap = GLES20.glGetAttribLocation(sprog, "aP");
            GLES20.glVertexAttribPointer(ap, 3, GLES20.GL_FLOAT, false, 12, pb);
            GLES20.glEnableVertexAttribArray(ap);
            GLES20.glDrawArrays(GLES20.GL_POINTS, 0, NSP);
            GLES20.glDisableVertexAttribArray(ap);
            GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        }

        final Context ctx;
        FloatBuffer vb; ShortBuffer ib; int nIdx, prog, tex;
        final float[] proj = new float[16], view = new float[16], model = new float[16], mv = new float[16], mvp = new float[16];
        long t0 = System.nanoTime();
        volatile int reactType = 0;      // 0 diam, 1 benar, 2 salah
        volatile long reactT0 = 0;
        int sprog;
        static final int NSP = 10;
        final boolean inl;
        final boolean comp;
        float asp = 1.2f;
        float ccx = 0, ccy = 0, ccz = 0, halfX = 0.5f;   // pusat & setengah lebar model (untuk mode pendamping)
        float sx = 0, sy = 0, sk = 1;                    // posisi & skala kilau
        R(Context c, boolean inline, boolean companion) { ctx = c; inl = inline; comp = companion; }
        /** Lintasan mode pendamping: mengitari avatar 2 putaran, menukik, lalu hinggap di kepala. */
        float cx(float t) { float[] r = path(t); return r[0]; }
        float[] path(float t) {
            float T1 = 9f, T2 = 2.6f;
            float u = t < T1 ? 0f : Math.min(1f, (t - T1) / T2);
            float e = u * u * (3f - 2f * u), fl = 1f - e;
            float ang = t * 6.2832f / 4.5f;
            float rx = Math.max(0.3f, asp - 0.5f), ry = 0.55f;
            float cyp = 0.0f + 0.80f * e;
            return new float[] { fl * rx * (float) Math.cos(ang), cyp + fl * ry * (float) Math.sin(ang), fl, ang };
        }
        /** Cari di assets/characters/ dulu, lalu assets/. */
        InputStream op(String n) throws java.io.IOException {
            try { return ctx.getAssets().open("characters/" + n); } catch (java.io.IOException e) { return ctx.getAssets().open(n); }
        }

        public void onSurfaceCreated(GL10 g, EGLConfig cfg) {
            try {
                InputStream in = op("dragon_pet.bin");
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
                    if (x < mnx) mnx = x; if (x > mxx) mxx = x; if (y < mny) mny = y; if (y > mxy) mxy = y; if (z < mnz) mnz = z; if (z > mxz) mxz = z;
                }
                ccx = (mnx + mxx) / 2; ccy = (mny + mxy) / 2; ccz = (mnz + mxz) / 2; halfX = Math.max(0.01f, (mxx - mnx) / 2);
                Bitmap bmp = BitmapFactory.decodeStream(op("dragon_pet.jpg"));
                int[] t = new int[1]; GLES20.glGenTextures(1, t, 0); tex = t[0];
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, tex);
                GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
                GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
                GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bmp, 0); bmp.recycle();
            } catch (Exception e) { nIdx = 0; return; }
            String vs = "uniform mat4 uMVP,uM;attribute vec3 aP,aN;attribute vec2 aT;varying vec2 vT;varying float vL;"
                + "void main(){gl_Position=uMVP*vec4(aP,1.);vT=aT;vec3 n=normalize(mat3(uM)*aN);"
                + "vL=.55+.6*max(dot(n,normalize(vec3(.3,.8,-.6))),0.);}";
            String fs = "precision mediump float;uniform sampler2D uS;varying vec2 vT;varying float vL;"
                + "void main(){vec4 c=texture2D(uS,vT);gl_FragColor=vec4(c.rgb*vL*1.15,1.);}";
            prog = GLES20.glCreateProgram();
            GLES20.glAttachShader(prog, sh(GLES20.GL_VERTEX_SHADER, vs));
            GLES20.glAttachShader(prog, sh(GLES20.GL_FRAGMENT_SHADER, fs));
            GLES20.glLinkProgram(prog);
            if (inl) GLES20.glClearColor(0.93f, 0.91f, 1f, 1f); else GLES20.glClearColor(0, 0, 0, 0);
            GLES20.glEnable(GLES20.GL_DEPTH_TEST);
            String svs = "uniform mat4 uMVP;uniform float uSz;attribute vec3 aP;void main(){gl_Position=uMVP*vec4(aP,1.);gl_PointSize=uSz;}";
            String sfs = "precision mediump float;uniform float uA;void main(){vec2 d=gl_PointCoord-vec2(.5);float r=length(d)*2.;"
                + "if(r>1.)discard;gl_FragColor=vec4(1.,.92,.45,1.)*(1.-r*.5)*uA;}";
            sprog = GLES20.glCreateProgram();
            GLES20.glAttachShader(sprog, sh(GLES20.GL_VERTEX_SHADER, svs));
            GLES20.glAttachShader(sprog, sh(GLES20.GL_FRAGMENT_SHADER, sfs));
            GLES20.glLinkProgram(sprog);
        }
        static int sh(int type, String s) { int h = GLES20.glCreateShader(type); GLES20.glShaderSource(h, s); GLES20.glCompileShader(h); return h; }

        public void onSurfaceChanged(GL10 g, int w, int h) {
            GLES20.glViewport(0, 0, w, h);
            asp = (float) w / h;
            if (comp) Matrix.orthoM(proj, 0, -asp, asp, -1f, 1f, 0.1f, 10f);
            else Matrix.perspectiveM(proj, 0, 40f, (float) w / h, 0.1f, 10f);
        }

        public void onDrawFrame(GL10 g) {
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
            if (nIdx == 0) return;
            float t = (System.nanoTime() - t0) / 1e9f;
            // kamera di depan-atas naga (kepala naga menghadap -Z)
            Matrix.setLookAtM(view, 0, 0f, 0.7f, -1.2f, 0f, 0f, 0f, 0f, 1f, 0f);
            Matrix.setIdentityM(model, 0);
            float dy = 0f, tilt = 0f, rp = -1f;
            if (reactType != 0) {
                float dur = reactType == 1 ? 0.9f : 1.1f;
                rp = (System.nanoTime() - reactT0) / 1e9f / dur;
                if (rp >= 1f) { reactType = 0; rp = -1f; }
                else if (reactType == 1) { dy = 0.28f * (float) Math.abs(Math.sin(rp * Math.PI * 2f)) * (1f - rp); tilt = -10f * (float) Math.sin(rp * Math.PI); }
                else { dy = -0.12f * (float) Math.sin(rp * Math.PI); tilt = 14f * (float) Math.sin(rp * Math.PI); }
            }
            if (comp) {
                Matrix.setLookAtM(view, 0, 0f, 0.6f, -3f, 0f, 0f, 0f, 0f, 1f, 0f);
                float[] ps = path(t), p2 = path(t + 0.04f);
                float fl = ps[2];
                float vx = (p2[0] - ps[0]) / 0.04f;
                float yaw = 65f * Math.max(-1f, Math.min(1f, vx / 0.6f)) + (1f - fl) * 8f * (float) Math.sin(t * 1.3f);
                float sc = (0.55f + 0.25f * fl) / (2f * halfX) * (1f - 0.18f * fl * (float) Math.sin(ps[3]));
                float py = ps[1] + dy * 0.45f + (1f - fl) * 0.012f * (float) Math.sin(t * 2f);
                sx = ps[0]; sy = py; sk = 0.45f;
                Matrix.translateM(model, 0, ps[0], py, 0f);
                Matrix.rotateM(model, 0, yaw, 0f, 1f, 0f);
                Matrix.rotateM(model, 0, tilt, 1f, 0f, 0f);
                Matrix.rotateM(model, 0, (3f + 5f * fl) * (float) Math.sin(t * (1.5f + 3f * fl)), 0f, 0f, 1f);
                Matrix.scaleM(model, 0, sc, sc, sc);
                Matrix.translateM(model, 0, -ccx, -ccy, -ccz);
            } else {
                sx = 0; sy = 0; sk = 1;
                Matrix.translateM(model, 0, 0f, dy + 0.05f * (float) Math.sin(t * 2.2f), 0f);
                Matrix.rotateM(model, 0, tilt, 1f, 0f, 0f);
                Matrix.rotateM(model, 0, 30f * (float) Math.sin(t * 0.8f), 0f, 1f, 0f);
                Matrix.rotateM(model, 0, 6f * (float) Math.sin(t * 4.4f), 0f, 0f, 1f);
            }
            Matrix.multiplyMM(mv, 0, view, 0, model, 0);
            Matrix.multiplyMM(mvp, 0, proj, 0, mv, 0);
            GLES20.glUseProgram(prog);
            GLES20.glUniformMatrix4fv(GLES20.glGetUniformLocation(prog, "uMVP"), 1, false, mvp, 0);
            GLES20.glUniformMatrix4fv(GLES20.glGetUniformLocation(prog, "uM"), 1, false, model, 0);
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, tex);
            GLES20.glUniform1i(GLES20.glGetUniformLocation(prog, "uS"), 0);
            int p = GLES20.glGetAttribLocation(prog, "aP"), n = GLES20.glGetAttribLocation(prog, "aN"), u = GLES20.glGetAttribLocation(prog, "aT");
            vb.position(0); GLES20.glVertexAttribPointer(p, 3, GLES20.GL_FLOAT, false, 32, vb); GLES20.glEnableVertexAttribArray(p);
            vb.position(3); GLES20.glVertexAttribPointer(n, 3, GLES20.GL_FLOAT, false, 32, vb); GLES20.glEnableVertexAttribArray(n);
            vb.position(6); GLES20.glVertexAttribPointer(u, 2, GLES20.GL_FLOAT, false, 32, vb); GLES20.glEnableVertexAttribArray(u);
            GLES20.glDrawElements(GLES20.GL_TRIANGLES, nIdx, GLES20.GL_UNSIGNED_SHORT, ib);
            if (rp >= 0f && reactType == 1) drawSparkle(rp);
            try { Thread.sleep(16); } catch (InterruptedException ignored) {}
        }
    }
}
