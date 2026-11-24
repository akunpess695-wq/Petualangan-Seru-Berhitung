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
    private static final ExternalCharacterMesh[] sExternal = new ExternalCharacterMesh[2];
    private static final boolean[] sExternalTried = new boolean[2];
    private static final HoodMesh[][] sHood = new HoodMesh[2][HoodMesh.COUNT]; // tudung per gender dan hewan, dibuat saat dipakai
    private final int gender;            // 0 = putra, 1 = putri
    private volatile int curGender;      // dapat diganti langsung tanpa membuat ulang GLSurfaceView
    private volatile boolean useExternal = false; // model OBJ baru; mesh prosedural tetap menjadi cadangan

    private volatile float yaw = 0f, pitch = 0f; // derajat
    private volatile float bgR = 1f, bgG = 1f, bgB = 1f; // warna latar (default putih = warna kartu)
    private volatile String lookPakaian, lookRambut;          // "badan,lengan[,celana/rok]" dan "#hex" (null = bawaan)
    private volatile boolean showTas = true;                  // ransel putra (boleh dilepas)
    private volatile boolean showPeci = true;                 // hanya berlaku untuk putra
    private volatile int hood = -1;                           // nomor tudung hewan (slot 0..24, lihat HoodMesh.used), -1 = tanpa tudung
    private volatile int hairStyle = 0;                       // PutraMesh.STYLE_*; terlihat hanya bila peci dilepas
    private volatile float[] bgGrad;                          // 6 warna rgb + jumlah ruas di [18]; null = latar rata
    private float lastX, lastY;
    private volatile float zoom = 1f;
    private final android.view.ScaleGestureDetector scaleDetector;
    private final Renderer renderer = new Renderer();
    private volatile boolean nagaOn = false;   // naga pendamping di dalam adegan (terhalang avatar saat di belakang)
    private NagaScene naga;
    private volatile boolean gojiOn = false;   // Godzilla mini berdiri di samping avatar
    private GodzillaScene2 goji;
    private volatile String toonPetId = null;  // Pet toon 3D berdiri di samping kiri avatar
    private ToonPetScene toonPet;

    private void syncRenderMode() {
        boolean anim = nagaOn || gojiOn || (toonPetId != null && !toonPetId.isEmpty());
        setRenderMode(anim ? RENDERMODE_CONTINUOUSLY : RENDERMODE_WHEN_DIRTY);
        requestRender();
    }

    public void setToonPet(String id) {
        toonPetId = (id != null && !id.isEmpty()) ? id : null;
        syncRenderMode();
    }

    public void setGoji(boolean on) {
        gojiOn = on;
        syncRenderMode();
    }
    /** Hidupkan naga pendamping: mengitari avatar lalu hinggap di kepala. Panggil sebelum/sesudah dipasang. */
    public void setNaga(boolean on) {
        nagaOn = on;
        syncRenderMode();
    }

    PutriGLView(Context ctx) { this(ctx, 1); }

    PutriGLView(Context ctx, int gender) {
        super(ctx);
        this.gender = gender == 0 ? 0 : 1;
        this.curGender = this.gender;
        scaleDetector = new android.view.ScaleGestureDetector(ctx,
                new android.view.ScaleGestureDetector.SimpleOnScaleGestureListener() {
                    @Override public boolean onScale(android.view.ScaleGestureDetector detector) {
                        zoom = Math.max(0.8f, Math.min(1.85f, zoom * detector.getScaleFactor()));
                        requestRender();
                        return true;
                    }
                });
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

    /** Ganti gender karakter secara langsung tanpa membuat ulang GLSurfaceView. */
    void setGender(int g) {
        curGender = g == 0 ? 0 : 1;
    }

    /** Mode model OBJ baru. Tudung hewan dan gaya rambut khusus otomatis memakai mesh lama. */
    void setExternalModel(boolean on) { useExternal = on; }

    private ExternalCharacterMesh externalMesh() {
        int g = curGender;
        if (!sExternalTried[g]) {
            sExternal[g] = ExternalCharacterMesh.tryLoad(getContext(), g);
            sExternalTried[g] = true;
        }
        return sExternal[g];
    }

    void setLook(String pakaian, String rambut, String latar, boolean peci, int gayaRambut) {
        lookPakaian = pakaian; lookRambut = rambut; showPeci = peci; hairStyle = gayaRambut;
        bgGrad = parseGrad(latar);
        if (renderer.ready) queueEvent(renderer::uploadMesh);
        requestRender();
    }

    private float glossFor(int part) {
        if (curGender == 1) return part == PutriMesh.P_OTHER ? 0.065f : 0.02f;
        switch (part) {
            case PutraMesh.P_OTHER:
            case PutraMesh.P_ARM_SKIN:
                return 0.065f;
            case PutraMesh.P_HAIR:
            case PutraMesh.P_HAIR_CREW:
            case PutraMesh.P_HAIR_SPIKY:
            case PutraMesh.P_HAIR_MESSY:
            case PutraMesh.P_HAIR_DET:
            case PutraMesh.P_HAIR_LOW:
            case PutraMesh.P_HAIR_SIDE:
            case PutraMesh.P_HAIR_CURL:
            case PutraMesh.P_HAIR_CROWN:
            case PutraMesh.P_CURL_LOW:
            case PutraMesh.P_HAIR_BANG:
            case PutraMesh.P_HAIR_WAVY:
            case PutraMesh.P_HAIR_QUIFF:
            case PutraMesh.P_HAIR_BUN:
            case PutraMesh.P_HAIR_VOL:
            case PutraMesh.P_HAIR_VOLH:
                return 0.085f;
            default:
                return 0.02f;
        }
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

    void resetView() {
        zoom = 1f;
        setAngles(0f, 0f);
    }

    void setAngles(float yawDeg, float pitchDeg) {
        yaw = yawDeg;
        pitch = Math.max(-12f, Math.min(28f, pitchDeg));
        requestRender();
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        scaleDetector.onTouchEvent(e);
        if (e.getPointerCount() > 1 || e.getActionMasked() == MotionEvent.ACTION_POINTER_UP) {
            lastX = e.getX(); lastY = e.getY();
            return true;
        }
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
            "attribute float aGloss;\n" +
            "varying float vGloss;\n" +
            "varying vec3 vN;\n" +
            "varying vec3 vC;\n" +
            "void main(){\n" +
            "  gl_Position = uMVP * vec4(aPos, 1.0);\n" +
            "  vN = uNM * aNor;\n" +
            "  vC = aCol;\n" +
            "  vGloss = aGloss;\n" +
            "}\n";

    // Sama dengan render.py: key (-0.45,0.65,0.62), fill (0.6,0.1,0.5), spekular 0.10^24
    private static final String FS =
            "precision mediump float;\n" +
            "varying vec3 vN;\n" +
            "varying vec3 vC;\n" +
            "varying float vGloss;\n" +
            "void main(){\n" +
            "  vec3 n = normalize(vN);\n" +
            "  if (n.z < 0.0) n = -n;\n" +
            "  vec3 L1 = normalize(vec3(-0.45, 0.65, 0.62));\n" +
            "  vec3 L2 = normalize(vec3(0.6, 0.1, 0.5));\n" +
            "  vec3 Hh = normalize(L1 + vec3(0.0, 0.0, 1.0));\n" +
            "  float d1 = max(dot(n, L1), 0.0);\n" +
            "  float d2 = max(dot(n, L2), 0.0);\n" +
            "  float hemi = 0.5 + 0.5 * n.y;\n" +
            "  float sh = 0.48 + 0.16 * hemi + 0.36 * d1 + 0.10 * d2;\n" +
            "  float spec = vGloss * pow(max(dot(n, Hh), 0.0), 24.0);\n" +
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
        private int podVbo, podIbo, podIdxCount;
        private int bgProg, bgP, bgC, bgSeg;
        private FloatBuffer bgQuad;
        volatile boolean ready;
        private int aPos, aNor, aCol, aGloss, uMVP, uNM;
        private final float[] proj = new float[16], model = new float[16], view = new float[16], sceneView = new float[16], stageRot = new float[16];
        private final float[] mv = new float[16], mvp = new float[16], nm = new float[9];

        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig cfg) {
            naga = null;   // konteks GL baru: sumber daya naga dibuat ulang
            goji = null;
            toonPet = null;
            GLES20.glClearColor(bgR, bgG, bgB, 1f);
            GLES20.glEnable(GLES20.GL_DEPTH_TEST);
            GLES20.glDisable(GLES20.GL_CULL_FACE); // dua sisi, seperti pratinjau
            prog = link(compile(GLES20.GL_VERTEX_SHADER, VS), compile(GLES20.GL_FRAGMENT_SHADER, FS));
            aPos = GLES20.glGetAttribLocation(prog, "aPos");
            aNor = GLES20.glGetAttribLocation(prog, "aNor");
            aCol = GLES20.glGetAttribLocation(prog, "aCol");
            aGloss = GLES20.glGetAttribLocation(prog, "aGloss");
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
            podVbo = 0; podIbo = 0;
            uploadPodium();
            uploadMesh();
            ready = true;
        }

        /** Panggung bundar 3D (turntable podium) di bawah kaki karakter & pet (y = 0..-4.5). */
        private void uploadPodium() {
            final int N = 40;
            final float R1 = 49f, R2 = 52.5f, Y0 = -0.2f, Y1 = -4.2f;
            // Ring 0: center top, Ring 1: inner top (R1,Y0), Ring 2: outer top rim (R2,Y0), Ring 3: outer bottom (R2,Y1)
            int vc = 1 + N * 3;
            float[] pk = new float[vc * 10];
            // Center vertex (soft lavender-white platform center)
            pk[0] = 0f; pk[1] = Y0; pk[2] = 0f;
            pk[3] = 0f; pk[4] = 1f; pk[5] = 0f;
            pk[6] = 0.94f; pk[7] = 0.92f; pk[8] = 0.99f; pk[9] = 0.04f;
            for (int i = 0; i < N; i++) {
                double a = 2.0 * Math.PI * i / N;
                float cs = (float) Math.cos(a), sn = (float) Math.sin(a);
                // Ring 1: inner disc edge
                int o1 = (1 + i) * 10;
                pk[o1] = R1 * cs; pk[o1 + 1] = Y0; pk[o1 + 2] = R1 * sn;
                pk[o1 + 3] = 0f; pk[o1 + 4] = 1f; pk[o1 + 5] = 0f;
                pk[o1 + 6] = 0.86f; pk[o1 + 7] = 0.82f; pk[o1 + 8] = 0.96f; pk[o1 + 9] = 0.04f;
                // Ring 2: outer rim top (purple-gold accent ring)
                int o2 = (1 + N + i) * 10;
                pk[o2] = R2 * cs; pk[o2 + 1] = Y0 - 0.3f; pk[o2 + 2] = R2 * sn;
                pk[o2 + 3] = cs * 0.4f; pk[o2 + 4] = 0.91f; pk[o2 + 5] = sn * 0.4f;
                pk[o2 + 6] = 0.72f; pk[o2 + 7] = 0.63f; pk[o2 + 8] = 0.93f; pk[o2 + 9] = 0.06f;
                // Ring 3: cylinder base
                int o3 = (1 + N * 2 + i) * 10;
                pk[o3] = R2 * cs; pk[o3 + 1] = Y1; pk[o3 + 2] = R2 * sn;
                pk[o3 + 3] = cs; pk[o3 + 4] = 0f; pk[o3 + 5] = sn;
                pk[o3 + 6] = 0.56f; pk[o3 + 7] = 0.46f; pk[o3 + 8] = 0.82f; pk[o3 + 9] = 0.03f;
            }
            short[] si = new short[N * 3 * 5];
            int k = 0;
            for (int i = 0; i < N; i++) {
                int j = (i + 1) % N;
                // Top fan
                si[k++] = 0; si[k++] = (short) (1 + i); si[k++] = (short) (1 + j);
                // Rim quad
                int a1 = 1 + i, b1 = 1 + j, a2 = 1 + N + i, b2 = 1 + N + j;
                si[k++] = (short) a1; si[k++] = (short) a2; si[k++] = (short) b2;
                si[k++] = (short) a1; si[k++] = (short) b2; si[k++] = (short) b1;
                // Side quad
                int a3 = 1 + N * 2 + i, b3 = 1 + N * 2 + j;
                si[k++] = (short) a2; si[k++] = (short) a3; si[k++] = (short) b3;
                si[k++] = (short) a2; si[k++] = (short) b3; si[k++] = (short) b2;
            }
            podIdxCount = k;
            FloatBuffer fb = ByteBuffer.allocateDirect(pk.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
            fb.put(pk).position(0);
            ShortBuffer ib = ByteBuffer.allocateDirect(si.length * 2).order(ByteOrder.nativeOrder()).asShortBuffer();
            ib.put(si).position(0);
            int[] b = new int[2];
            GLES20.glGenBuffers(2, b, 0);
            podVbo = b[0]; podIbo = b[1];
            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, podVbo);
            GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, pk.length * 4, fb, GLES20.GL_STATIC_DRAW);
            GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, podIbo);
            GLES20.glBufferData(GLES20.GL_ELEMENT_ARRAY_BUFFER, si.length * 2, ib, GLES20.GL_STATIC_DRAW);
        }

        /** Susun ulang buffer GPU dari mesh bersama + salinan warna/indeks untuk tampilan ini. Dipanggil di thread GL. */
        void uploadMesh() {
            int g = curGender;
            float[] mpos, mnor, mcol, mgloss = null;
            byte[] parts;
            int[] midx;
            int mvc;
            boolean extEligible = useExternal && hood < 0
                    && (g == 1 || hairStyle == PutraMesh.STYLE_DEFAULT);
            ExternalCharacterMesh ext = extEligible ? externalMesh() : null;
            if (ext != null) {
                parts = null;
                mpos = ext.pos;
                mnor = ext.nor;
                midx = ext.indices;
                mvc = ext.vertexCount;
                mcol = ext.outfitColors(lookPakaian, lookRambut);
                mgloss = ext.gloss;
            } else if (g == 0) {
                if (sMeshPutra == null) sMeshPutra = PutraMesh.build();
                PutraMesh m = sMeshPutra;
                parts = m.part;
                mpos = m.pos; mnor = m.nor; midx = m.indexFor(showPeci, hood >= 0 ? PutraMesh.STYLE_HOOD : hairStyle, (lookPakaian == null || "#FFFFFF,#FFFFFF,#C62828".equalsIgnoreCase(lookPakaian)), showTas); mvc = m.vertCount;
                mcol = m.outfitColors(lookPakaian, lookRambut);
            } else {
                if (sMesh == null) sMesh = PutriMesh.build();
                PutriMesh m = sMesh;
                parts = m.part;
                mpos = m.pos; mnor = m.nor; midx = m.indexFor(true, showTas); mvc = m.vertCount; // ransel putri juga bisa dilepas
                mcol = m.outfitColors(lookPakaian, lookRambut);
            }
            // tudung hewan: titik dan indeksnya ditambahkan di belakang mesh badan (indeks tetap di bawah 65.536)
            HoodMesh hm = null;
            final int hd = hood;
            if (hd >= 0) {
                if (sHood[g][hd] == null) sHood[g][hd] = HoodMesh.build(hd, g);
                hm = sHood[g][hd];
            }
            int hvc = hm == null ? 0 : hm.vertCount;
            // Only upload referenced vertices. Avoid allocating a full interleaved
            // array for every hidden hairstyle and every unused hood vertex.
            if (hm != null) {
                int[] all = new int[midx.length + hm.idx.length];
                System.arraycopy(midx, 0, all, 0, midx.length);
                for (int i = 0; i < hm.idx.length; i++) all[midx.length + i] = hm.idx[i] + mvc;
                midx = all;
            }
            int[] remap = new int[mvc + hvc];
            java.util.Arrays.fill(remap, -1);
            int active = 0;
            for (int index : midx) {
                if (index < 0 || index >= remap.length) throw new IllegalStateException("Invalid mesh index: " + index);
                if (remap[index] == -1) remap[index] = active++;
            }
            if (active > 65536) {
                // Never cast an out-of-range index to short: it would corrupt geometry.
                if (hood >= 0) {
                    Log.w("Salamuda3D", "Hood exceeds 16-bit mesh capacity; showing avatar without hood");
                    hood = -1;
                    uploadMesh();
                    return;
                }
                throw new IllegalStateException("Mesh exceeds GLES2 16-bit index limit");
            }
            float[] packed = new float[active * 10];
            for (int old = 0; old < remap.length; old++) {
                int dst = remap[old];
                if (dst < 0) continue;
                int off = dst * 10;
                if (old < mvc) {
                    System.arraycopy(mpos, old * 3, packed, off, 3);
                    System.arraycopy(mnor, old * 3, packed, off + 3, 3);
                    System.arraycopy(mcol, old * 3, packed, off + 6, 3);
                    packed[off + 9] = mgloss != null ? mgloss[old] : glossFor(parts[old]);
                } else {
                    int hi = old - mvc;
                    System.arraycopy(hm.pos, hi * 3, packed, off, 3);
                    System.arraycopy(hm.nor, hi * 3, packed, off + 3, 3);
                    System.arraycopy(hm.col, hi * 3, packed, off + 6, 3);
                    packed[off + 9] = 0.025f;
                }
            }
            FloatBuffer fb = ByteBuffer.allocateDirect(packed.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
            fb.put(packed).position(0);
            short[] si = new short[midx.length];
            for (int i = 0; i < si.length; i++) si[i] = (short) remap[midx[i]];
            ShortBuffer ib = ByteBuffer.allocateDirect(si.length * 2).order(ByteOrder.nativeOrder()).asShortBuffer();
            ib.put(si).position(0);
            idxCount = si.length;

            if (vbo != 0) GLES20.glDeleteBuffers(2, new int[]{vbo, ibo}, 0);
            int[] b = new int[2];
            GLES20.glGenBuffers(2, b, 0);
            vbo = b[0]; ibo = b[1];
            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo);
            GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, packed.length * 4, fb, GLES20.GL_STATIC_DRAW);
            GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, ibo);
            GLES20.glBufferData(GLES20.GL_ELEMENT_ARRAY_BUFFER, si.length * 2, ib, GLES20.GL_STATIC_DRAW);
        }

        @Override
        public void onSurfaceChanged(GL10 gl, int w, int h) {
            GLES20.glViewport(0, 0, w, h);
            float aspect = (float) w / Math.max(1, h);
            // tinggi pandang 118 satuan di jarak 330 (cukup untuk tudung tinggi, naga di atas kepala, dan pet di kiri/kanan)
            float halfH = aspect < 1.05f ? (59f / Math.max(0.78f, aspect)) : 59f;
            float fovy = (float) Math.toDegrees(2 * Math.atan(halfH / 330.0));
            Matrix.perspectiveM(proj, 0, fovy, aspect, 80f, 750f);
            Matrix.setIdentityM(view, 0);
            Matrix.translateM(view, 0, 0f, 0f, -330f);
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
            Matrix.setIdentityM(stageRot, 0);
            Matrix.rotateM(stageRot, 0, pitch, 1f, 0f, 0f);
            Matrix.rotateM(stageRot, 0, yaw, 0f, 1f, 0f);
            Matrix.scaleM(stageRot, 0, zoom, zoom, zoom);
            Matrix.multiplyMM(sceneView, 0, view, 0, stageRot, 0);

            System.arraycopy(stageRot, 0, model, 0, 16);
            Matrix.translateM(model, 0, 0f, -47f, 0f);
            Matrix.multiplyMM(mv, 0, view, 0, model, 0);
            Matrix.multiplyMM(mvp, 0, proj, 0, mv, 0);
            float invZ = 1f / Math.max(0.01f, zoom);
            nm[0] = model[0] * invZ; nm[1] = model[1] * invZ; nm[2] = model[2] * invZ;
            nm[3] = model[4] * invZ; nm[4] = model[5] * invZ; nm[5] = model[6] * invZ;
            nm[6] = model[8] * invZ; nm[7] = model[9] * invZ; nm[8] = model[10] * invZ;

            GLES20.glUseProgram(prog);
            GLES20.glUniformMatrix4fv(uMVP, 1, false, mvp, 0);
            GLES20.glUniformMatrix3fv(uNM, 1, false, nm, 0);
            int stride = 10 * 4;
            if (podVbo != 0) {
                GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, podVbo);
                GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, podIbo);
                GLES20.glEnableVertexAttribArray(aGloss);
                GLES20.glVertexAttribPointer(aGloss, 1, GLES20.GL_FLOAT, false, stride, 36);
                GLES20.glEnableVertexAttribArray(aPos);
                GLES20.glVertexAttribPointer(aPos, 3, GLES20.GL_FLOAT, false, stride, 0);
                GLES20.glEnableVertexAttribArray(aNor);
                GLES20.glVertexAttribPointer(aNor, 3, GLES20.GL_FLOAT, false, stride, 12);
                GLES20.glEnableVertexAttribArray(aCol);
                GLES20.glVertexAttribPointer(aCol, 3, GLES20.GL_FLOAT, false, stride, 24);
                GLES20.glDrawElements(GLES20.GL_TRIANGLES, podIdxCount, GLES20.GL_UNSIGNED_SHORT, 0);
            }
            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo);
            GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, ibo);
            GLES20.glEnableVertexAttribArray(aGloss);
            GLES20.glVertexAttribPointer(aGloss, 1, GLES20.GL_FLOAT, false, stride, 36);
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
            GLES20.glDisableVertexAttribArray(aGloss);
            if (nagaOn) {
                if (naga == null) naga = new NagaScene(getContext());
                naga.draw(sceneView, proj);
            }
            if (gojiOn) {
                if (goji == null) goji = new GodzillaScene2(getContext());
                goji.draw(sceneView, proj);
            }
            String pid = toonPetId;
            if (pid != null) {
                if (toonPet == null || !pid.equals(toonPet.id())) {
                    toonPet = new ToonPetScene(getContext(), pid);
                }
                toonPet.draw(sceneView, proj);
            }
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
