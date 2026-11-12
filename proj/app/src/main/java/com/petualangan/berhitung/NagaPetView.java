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
    public NagaPetView(Context c, boolean inline) {
        super(c);
        setEGLContextClientVersion(2);
        setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        getHolder().setFormat(android.graphics.PixelFormat.TRANSLUCENT);
        if (!inline) setZOrderOnTop(true);
        r = new R(c, inline);
        setRenderer(r);
    }
    /** Tidak menangkap sentuhan: tombol di bawahnya tetap bisa ditekan. */
    @Override public boolean onTouchEvent(MotionEvent e) { return false; }

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
        final Context ctx;
        FloatBuffer vb; ShortBuffer ib; int nIdx, prog, tex;
        final float[] proj = new float[16], view = new float[16], model = new float[16], mv = new float[16], mvp = new float[16];
        long t0 = System.nanoTime();
        final boolean inl;
        R(Context c, boolean inline) { ctx = c; inl = inline; }
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
        }
        static int sh(int type, String s) { int h = GLES20.glCreateShader(type); GLES20.glShaderSource(h, s); GLES20.glCompileShader(h); return h; }

        public void onSurfaceChanged(GL10 g, int w, int h) {
            GLES20.glViewport(0, 0, w, h);
            Matrix.perspectiveM(proj, 0, 40f, (float) w / h, 0.1f, 10f);
        }

        public void onDrawFrame(GL10 g) {
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
            if (nIdx == 0) return;
            float t = (System.nanoTime() - t0) / 1e9f;
            // kamera di depan-atas naga (kepala naga menghadap -Z)
            Matrix.setLookAtM(view, 0, 0f, 0.7f, -1.2f, 0f, 0f, 0f, 0f, 1f, 0f);
            Matrix.setIdentityM(model, 0);
            Matrix.translateM(model, 0, 0f, 0.05f * (float) Math.sin(t * 2.2f), 0f);   // mengambang
            Matrix.rotateM(model, 0, 30f * (float) Math.sin(t * 0.8f), 0f, 1f, 0f);     // menoleh kiri-kanan
            Matrix.rotateM(model, 0, 6f * (float) Math.sin(t * 4.4f), 0f, 0f, 1f);      // sayap miring (kepak ringan)
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
            try { Thread.sleep(16); } catch (InterruptedException ignored) {}
        }
    }
}
