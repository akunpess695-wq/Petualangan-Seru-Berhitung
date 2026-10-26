package com.petualangan.berhitung;

import android.content.Context;
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
 * Tampilan 3D putri (Tahap E). Pakai: addView(new PutriGLView(ctx)), dan teruskan
 * onResume()/onPause() dari Activity. Geser jari = putar. Latar opak putih (setBackgroundRgb untuk mengganti).
 * Tidak mengubah AvatarView lama.
 */
final class PutriGLView extends GLSurfaceView {
    private static PutriMesh sMesh; // dibuat sekali, dipakai ulang

    private volatile float yaw = 0f, pitch = 0f; // derajat
    private volatile float bgR = 1f, bgG = 1f, bgB = 1f; // warna latar (default putih = warna kartu)
    private float lastX, lastY;
    private final Renderer renderer = new Renderer();

    PutriGLView(Context ctx) {
        super(ctx);
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

    private final class Renderer implements GLSurfaceView.Renderer {
        private int prog, vbo, ibo, idxCount;
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

            if (sMesh == null) sMesh = PutriMesh.build();
            PutriMesh m = sMesh;
            float[] inter = new float[m.vertCount * 9];
            for (int i = 0; i < m.vertCount; i++) {
                System.arraycopy(m.pos, i * 3, inter, i * 9, 3);
                System.arraycopy(m.nor, i * 3, inter, i * 9 + 3, 3);
                System.arraycopy(m.col, i * 3, inter, i * 9 + 6, 3);
            }
            FloatBuffer fb = ByteBuffer.allocateDirect(inter.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
            fb.put(inter).position(0);
            short[] si = new short[m.idx.length];
            for (int i = 0; i < si.length; i++) si[i] = (short) m.idx[i];
            ShortBuffer ib = ByteBuffer.allocateDirect(si.length * 2).order(ByteOrder.nativeOrder()).asShortBuffer();
            ib.put(si).position(0);
            idxCount = si.length;

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
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
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
