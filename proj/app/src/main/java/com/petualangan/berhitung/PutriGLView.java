package com.petualangan.berhitung;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;
import android.view.MotionEvent;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Tampilan 3D putra / putri (Tahap E). Pakai: addView(new PutriGLView(ctx, gender)) (0 = putra, 1 = putri), dan teruskan
 * onResume()/onPause() dari Activity. Geser jari = putar. Latar opak putih; setBackgroundRgb = warna rata, setLook = pakaian/rambut(jilbab)/peci/latar gradasi.
 * Warna dan peci diterapkan pada SALINAN per tampilan, mesh Putra/Putri yang static tidak pernah diubah (tidak bocor antar pemain).
 * Tidak mengubah AvatarView lama.
 */
final class PutriGLView extends GLSurfaceView {
    private static PutriMesh sMesh;     // putri, dibuat sekali, dipakai ulang
    private static PutraMesh sMeshPutra; // putra
    private static final HoodMesh[][] sHood = new HoodMesh[2][HoodMesh.COUNT]; // tudung per gender dan hewan, dibuat saat dipakai
    private final int gender;            // 0 = putra, 1 = putri

    private volatile float yaw = 0f, pitch = 0f; // derajat
    private volatile float bgR = 1f, bgG = 1f, bgB = 1f; // warna latar (default putih = warna kartu)
    private volatile String lookPakaian, lookRambut;          // "badan,lengan[,celana/rok]" dan "#hex" (null = bawaan)
    private volatile boolean showTas = true;                  // ransel putra (boleh dilepas)
    private volatile boolean showPeci = true;                 // hanya berlaku untuk putra
    private volatile int hood = -1;                           // nomor tudung hewan (slot 0..24, lihat HoodMesh.used), -1 = tanpa tudung
    private volatile int hairStyle = 0;                       // PutraMesh.STYLE_*; terlihat hanya bila peci dilepas
    private volatile float[] bgGrad;                          // 6 warna rgb + jumlah ruas di [18]; null = latar rata
    private float lastX, lastY;
    private final Renderer renderer = new Renderer();

    PutriGLView(Context ctx) { this(ctx, 1); }

    PutriGLView(Context ctx, int gender) {
        super(ctx);
        this.gender = gender == 0 ? 0 : 1;
        setEGLContextClientVersion(2);
        setEGLConfigChooser(8, 8, 8, 0, 16, 0); // RGB + depth 16
        getHolder().setFormat(PixelFormat.OPAQUE); // latar opak (transparan menghasilkan kotak hitam)
        setRenderer(renderer);
        setRenderMode(RENDERMODE_WHEN_DIRTY);
    }

    /** Warna latar (0xRRGGBB); harus sama dengan kartu induk karena tampilan ini opak. Panggil sebelum tampil. */
    void setBackgroundRgb(int rgb) {
        bgR = ((rgb >> 16) & 255) / 255f; bgG = ((rgb >> 8) & 255) / 255f; bgB = (rgb & 255) / 255f;
    }

    /**
     * Pakaian, rambut (putra) / warna jilbab (putri), peci dan gaya rambut (putra), dan latar gradasi. Semua boleh null / default.
     * latar = daftar warna dipisah koma, "#4FC3F7,#E1F5FE" (kiri-atas ke kanan-bawah, sama dengan AvatarView), maks 6 warna.
     * Boleh dipanggil sebelum atau sesudah tampil.
     */
    /** Pasang tudung hewan (0..11) atau lepas (-1). Panggil sebelum setLook, yang memuat ulang mesh. */
    void setHood(int animal) { hood = HoodMesh.used(animal) ? animal : -1; }

    /** Pasang / lepas ransel putra. Panggil sebelum setLook, yang memuat ulang mesh. */
    void setTas(boolean on) { showTas = on; }

    void setLook(String pakaian, String rambut, String latar, boolean peci, int gayaRambut) {
        lookPakaian = pakaian; lookRambut = rambut; showPeci = peci; hairStyle = gayaRambut;
        bgGrad = parseGrad(latar);
        if (renderer.ready) queueEvent(renderer::uploadMesh);
        requestRender();
    }

    private static float[] parseGrad(String latar) {
        if (latar == null || latar.trim().isEmpty()) return null;
        String[] cs = latar.split(",");
        int n = Math.min(cs.length, 6);
        float[] g = new float[19];
        int ok = 0;
        for (int i = 0; i < n; i++) {
            try {
                int c = Color.parseColor(cs[i].trim());
                g[ok * 3] = Color.red(c) / 255f; g[ok * 3 + 1] = Color.green(c) / 255f; g[ok * 3 + 2] = Color.blue(c) / 255f;
                ok++;
            } catch (IllegalArgumentException e) { /* warna rusak dilewati */ }
        }
        if (ok == 0) return null;
        if (ok == 1) { // satu warna = latar rata
            for (int k = 1; k < 6; k++) { g[k * 3] = g[0]; g[k * 3 + 1] = g[1]; g[k * 3 + 2] = g[2]; }
            g[18] = 1f;
            return g;
        }
        for (int k = ok; k < 6; k++) { g[k * 3] = g[(ok - 1) * 3]; g[k * 3 + 1] = g[(ok - 1) * 3 + 1]; g[k * 3 + 2] = g[(ok - 1) * 3 + 2]; }
        g[18] = ok - 1;
        return g;
    }

    void setAngles(float yawDeg, float pitchDeg) {
        yaw = yawDeg;
        pitch = Math.max(-12f, Math.min(28f, pitchDeg));
        requestRender();
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        switch (e.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                lastX = e.getX(); lastY = e.getY();
                if (getParent() != null) getParent().requestDisallowInterceptTouchEvent(true);
                return true;
            case MotionEvent.ACTION_MOVE:
                float dx = e.getX() - lastX, dy = e.getY() - lastY;
                lastX = e.getX(); lastY = e.getY();
                setAngles(yaw + dx * 0.6f, pitch + dy * 0.2f);
                return true;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (getParent() != null) getParent().requestDisallowInterceptTouchEvent(false);
                return true;
        }
        return super.onTouchEvent(e);
    }

    private static final String VS =
            "uniform mat4 uMVP;\n" +
            "uniform mat3 uNM;\n" +
            "attribute vec3 aPos;\n" +
            "attribute vec3 aNor;\n" +
            "attribute vec3 aCol;\n" +
            "varying vec3 vN;\n" +
            "varying vec3 vC;\n" +
            "void main(){\n" +
            "  gl_Position = uMVP * vec4(aPos, 1.0);\n" +
            "  vN = uNM * aNor;\n" +
            "  vC = aCol;\n" +
            "}\n";

    // Sama dengan render.py: key (-0.45,0.65,0.62), fill (0.6,0.1,0.5), spekular 0.10^24
    private static final String FS =
            "precision mediump float;\n" +
            "varying vec3 vN;\n" +
            "varying vec3 vC;\n" +
            "void main(){\n" +
            "  vec3 n = normalize(vN);\n" +
            "  if (n.z < 0.0) n = -n;\n" +
            "  vec3 L1 = normalize(vec3(-0.45, 0.65, 0.62));\n" +
            "  vec3 L2 = normalize(vec3(0.6, 0.1, 0.5));\n" +
            "  vec3 Hh = normalize(L1 + vec3(0.0, 0.0, 1.0));\n" +
            "  float d1 = max(dot(n, L1), 0.0);\n" +
            "  float d2 = max(dot(n, L2), 0.0);\n" +
            "  float hemi = 0.5 + 0.5 * n.y;\n" +
            "  float sh = 0.50 + 0.18 * hemi + 0.50 * d1 + 0.12 * d2;\n" +
            "  float spec = 0.10 * pow(max(dot(n, Hh), 0.0), 24.0);\n" +
            "  vec3 c = vC * sh + vec3(spec) + (1.0 - sh) * vec3(-0.01, 0.0, 0.025);\n" +
            "  gl_FragColor = vec4(clamp(c, 0.0, 1.0), 1.0);\n" +
            "}\n";

    // Latar gradasi: bidang penuh di belakang avatar. t = 0 di kiri-atas, 1 di kanan-bawah; hingga 6 warna.
    private static final String BG_VS =
            "attribute vec2 aP;\n" +
            "varying float vT;\n" +
            "void main(){\n" +
            "  gl_Position = vec4(aP, 0.0, 1.0);\n" +
            "  vT = ((aP.x * 0.5 + 0.5) + (0.5 - aP.y * 0.5)) * 0.5;\n" +
            "}\n";
    private static final String BG_FS =
            "precision mediump float;\n" +
            "varying float vT;\n" +
            "uniform vec3 uC[6];\n" +
            "uniform float uSeg;\n" +
            "void main(){\n" +
            "  float t = clamp(vT, 0.0, 1.0) * uSeg;\n" +
            "  vec3 c = uC[0];\n" +
            "  for (int i = 0; i < 5; i++) {\n" +
            "    if (float(i) < uSeg && t >= float(i)) c = mix(uC[i], uC[i + 1], clamp(t - float(i), 0.0, 1.0));\n" +
            "  }\n" +
            "  gl_FragColor = vec4(c, 1.0);\n" +
            "}\n";

    private final class Renderer implements GLSurfaceView.Renderer {
        private int prog, vbo, ibo, idxCount;
        private int bgProg, bgP, bgC, bgSeg;
        private FloatBuffer bgQuad;
        volatile boolean ready;
        private int aPos, aNor, aCol, uMVP, uNM;
        private final float[] proj = new float[16], model = new float[16], view = new float[16];
        private final float[] mv = new float[16], mvp = new float[16], nm = new float[9];

        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig cfg) {
            GLES20.glClearColor(bgR, bgG, bgB, 1f);
            GLES20.glEnable(GLES20.GL_DEPTH_TEST);
            GLES20.glDisable(GLES20.GL_CULL_FACE); // dua sisi, seperti pratinjau
            prog = link(compile(GLES20.GL_VERTEX_SHADER, VS), compile(GLES20.GL_FRAGMENT_SHADER, FS));
            aPos = GLES20.glGetAttribLocation(prog, "aPos");
            aNor = GLES20.glGetAttribLocation(prog, "aNor");
            aCol = GLES20.glGetAttribLocation(prog, "aCol");
            uMVP = GLES20.glGetUniformLocation(prog, "uMVP");
            uNM = GLES20.glGetUniformLocation(prog, "uNM");

            bgProg = link(compile(GLES20.GL_VERTEX_SHADER, BG_VS), compile(GLES20.GL_FRAGMENT_SHADER, BG_FS));
            bgP = GLES20.glGetAttribLocation(bgProg, "aP");
            bgC = GLES20.glGetUniformLocation(bgProg, "uC");
            if (bgC < 0) bgC = GLES20.glGetUniformLocation(bgProg, "uC[0]"); // sebagian driver hanya mengenal nama elemen
            bgSeg = GLES20.glGetUniformLocation(bgProg, "uSeg");
            bgQuad = ByteBuffer.allocateDirect(8 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
            bgQuad.put(new float[]{-1f, -1f, 1f, -1f, -1f, 1f, 1f, 1f}).position(0);
            vbo = 0; ibo = 0;
            uploadMesh();
            ready = true;
        }

        /** Susun ulang buffer GPU dari mesh bersama + salinan warna/indeks untuk tampilan ini. Dipanggil di thread GL. */
        void uploadMesh() {
            float[] mpos, mnor, mcol;
            int[] midx;
            int mvc;
            if (gender == 0) {
                if (sMeshPutra == null) sMeshPutra = PutraMesh.build();
                PutraMesh m = sMeshPutra;
                mpos = m.pos; mnor = m.nor; midx = m.indexFor(showPeci, hood >= 0 ? PutraMesh.STYLE_HOOD : hairStyle, lookPakaian == null && hood < 0, showTas); mvc = m.vertCount;
                mcol = m.outfitColors(lookPakaian, lookRambut);
            } else {
                if (sMesh == null) sMesh = PutriMesh.build();
                PutriMesh m = sMesh;
                mpos = m.pos; mnor = m.nor; midx = m.idx; mvc = m.vertCount;
                mcol = m.outfitColors(lookPakaian, lookRambut);
            }
            // tudung hewan: titik dan indeksnya ditambahkan di belakang mesh badan (indeks tetap di bawah 65.536)
            HoodMesh hm = null;
            final int hd = hood;
            if (hd >= 0) {
                if (sHood[gender][hd] == null) sHood[gender][hd] = HoodMesh.build(hd, gender);
                hm = sHood[gender][hd];
                if (mvc + hm.vertCount > 65535) hm = null;   // pengaman batas short
            }
            int hvc = hm == null ? 0 : hm.vertCount;
            float[] inter = new float[(mvc + hvc) * 9];
            for (int i = 0; i < mvc; i++) {
                System.arraycopy(mpos, i * 3, inter, i * 9, 3);
                System.arraycopy(mnor, i * 3, inter, i * 9 + 3, 3);
                System.arraycopy(mcol, i * 3, inter, i * 9 + 6, 3);
            }
            if (hm != null) {
                for (int i = 0; i < hvc; i++) {
                    System.arraycopy(hm.pos, i * 3, inter, (mvc + i) * 9, 3);
                    System.arraycopy(hm.nor, i * 3, inter, (mvc + i) * 9 + 3, 3);
                    System.arraycopy(hm.col, i * 3, inter, (mvc + i) * 9 + 6, 3);
                }
                int[] all = new int[midx.length + hm.idx.length];
                System.arraycopy(midx, 0, all, 0, midx.length);
                for (int i = 0; i < hm.idx.length; i++) all[midx.length + i] = hm.idx[i] + mvc;
                midx = all;
            }
            FloatBuffer fb = ByteBuffer.allocateDirect(inter.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
            fb.put(inter).position(0);
            short[] si = new short[midx.length];
            for (int i = 0; i < si.length; i++) si[i] = (short) midx[i];
            ShortBuffer ib = ByteBuffer.allocateDirect(si.length * 2).order(ByteOrder.nativeOrder()).asShortBuffer();
            ib.put(si).position(0);
            idxCount = si.length;

            if (vbo != 0) GLES20.glDeleteBuffers(2, new int[]{vbo, ibo}, 0);
            int[] b = new int[2];
            GLES20.glGenBuffers(2, b, 0);
            vbo = b[0]; ibo = b[1];
            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo);
            GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, inter.length * 4, fb, GLES20.GL_STATIC_DRAW);
            GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, ibo);
            GLES20.glBufferData(GLES20.GL_ELEMENT_ARRAY_BUFFER, si.length * 2, ib, GLES20.GL_STATIC_DRAW);
        }

        @Override
        public void onSurfaceChanged(GL10 gl, int w, int h) {
            GLES20.glViewport(0, 0, w, h);
            float aspect = (float) w / Math.max(1, h);
            // tinggi pandang 108 satuan di jarak 320 (karakter ~ 90 satuan, pusat y=48)
            float fovy = (float) Math.toDegrees(2 * Math.atan(54.0 / 320.0));
            Matrix.perspectiveM(proj, 0, fovy, aspect, 100f, 600f);
            Matrix.setIdentityM(view, 0);
            Matrix.translateM(view, 0, 0f, 0f, -320f);
        }

        @Override
        public void onDrawFrame(GL10 gl) {
            GLES20.glClearColor(bgR, bgG, bgB, 1f);
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
            float[] g = bgGrad;
            if (g != null) {
                GLES20.glDisable(GLES20.GL_DEPTH_TEST);
                GLES20.glUseProgram(bgProg);
                GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
                GLES20.glUniform3fv(bgC, 6, g, 0);
                GLES20.glUniform1f(bgSeg, g[18]);
                GLES20.glEnableVertexAttribArray(bgP);
                GLES20.glVertexAttribPointer(bgP, 2, GLES20.GL_FLOAT, false, 0, bgQuad);
                GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
                GLES20.glDisableVertexAttribArray(bgP);
                GLES20.glEnable(GLES20.GL_DEPTH_TEST);
            }
            Matrix.setIdentityM(model, 0);
            Matrix.rotateM(model, 0, pitch, 1f, 0f, 0f);
            Matrix.rotateM(model, 0, yaw, 0f, 1f, 0f);
            Matrix.translateM(model, 0, 0f, -48f, 0f);
            Matrix.multiplyMM(mv, 0, view, 0, model, 0);
            Matrix.multiplyMM(mvp, 0, proj, 0, mv, 0);
            nm[0] = model[0]; nm[1] = model[1]; nm[2] = model[2];
            nm[3] = model[4]; nm[4] = model[5]; nm[5] = model[6];
            nm[6] = model[8]; nm[7] = model[9]; nm[8] = model[10];

            GLES20.glUseProgram(prog);
            GLES20.glUniformMatrix4fv(uMVP, 1, false, mvp, 0);
            GLES20.glUniformMatrix3fv(uNM, 1, false, nm, 0);
            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo);
            GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, ibo);
            int stride = 9 * 4;
            GLES20.glEnableVertexAttribArray(aPos);
            GLES20.glVertexAttribPointer(aPos, 3, GLES20.GL_FLOAT, false, stride, 0);
            GLES20.glEnableVertexAttribArray(aNor);
            GLES20.glVertexAttribPointer(aNor, 3, GLES20.GL_FLOAT, false, stride, 12);
            GLES20.glEnableVertexAttribArray(aCol);
            GLES20.glVertexAttribPointer(aCol, 3, GLES20.GL_FLOAT, false, stride, 24);
            GLES20.glDrawElements(GLES20.GL_TRIANGLES, idxCount, GLES20.GL_UNSIGNED_SHORT, 0);
            GLES20.glDisableVertexAttribArray(aPos);
            GLES20.glDisableVertexAttribArray(aNor);
            GLES20.glDisableVertexAttribArray(aCol);
        }

        private int compile(int type, String src) {
            int s = GLES20.glCreateShader(type);
            GLES20.glShaderSource(s, src);
            GLES20.glCompileShader(s);
            int[] ok = new int[1];
            GLES20.glGetShaderiv(s, GLES20.GL_COMPILE_STATUS, ok, 0);
            if (ok[0] == 0) Log.e("PutriGL", "shader gagal: " + GLES20.glGetShaderInfoLog(s));
            return s;
        }

        private int link(int vs, int fs) {
            int p = GLES20.glCreateProgram();
            GLES20.glAttachShader(p, vs);
            GLES20.glAttachShader(p, fs);
            GLES20.glLinkProgram(p);
            int[] ok = new int[1];
            GLES20.glGetProgramiv(p, GLES20.GL_LINK_STATUS, ok, 0);
            if (ok[0] == 0) Log.e("PutriGL", "link gagal: " + GLES20.glGetProgramInfoLog(p));
            return p;
        }
    }
}
