package com.petualangan.berhitung;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/**
 * Pet pendamping 3D bergaya Toon di samping kiri avatar (di dalam adegan 3D PutriGLView).
 * Gerakan sederhana: bernapas halus, bergoyang kiri-kanan, mengibas ekor/kepala, dan ikut melompat saat jawaban benar.
 */
final class ToonPetScene {
    static final String[] IDS = {
            "pet_alligator", "pet_gorilla", "pet_wolf", "pet_tiger",
            "pet_snow_leopard", "pet_reindeer", "pet_polar_bear"
    };
    static final String[] NAMES = {
            "Buaya Toon", "Gorila Gunung", "Serigala Kutub", "Harimau Toon",
            "Macan Tutul Salju", "Rusa Kutub", "Beruang Kutub"
    };
    static final String[] EMOJIS = {
            "🐊", "🦍", "🐺", "🐯", "🐆", "🦌", "🐻‍❄️"
    };
    static final String[] DESCS = {
            "Buaya ramah bergaya kartun yang suka bergoyang pelan",
            "Gorila gagah penjaga semangat belajar di samping avatar",
            "Serigala tangkas yang setia menemani petualangan",
            "Harimau belang pemberani yang siap melompat gembira",
            "Macan tutul salju lincah dari puncak pegunungan es",
            "Rusa kutub bertanduk megah yang anggun dan ceria",
            "Beruang kutub putih yang hangat dan bersahabat"
    };

    private FloatBuffer vb;
    private ShortBuffer ib;
    private int nIdx, prog, tex;
    private int uMVP, uM, uTP, uBreath, uS, aP, aN, aT;
    private final String assetId;
    private final long t0 = System.nanoTime();
    private final float[] model = new float[16], mv = new float[16], mvp = new float[16];

    private static byte[] readAll(InputStream in) throws java.io.IOException {
        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        byte[] buf = new byte[8192];
        int r;
        while ((r = in.read(buf)) > 0) bo.write(buf, 0, r);
        in.close();
        return bo.toByteArray();
    }

    ToonPetScene(Context ctx, String id) {
        this.assetId = id;
        try {
            byte[] b = readAll(ctx.getAssets().open("characters/" + id + ".bin"));
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

            InputStream imgIn = ctx.getAssets().open("characters/" + id + ".png");
            Bitmap bmp = BitmapFactory.decodeStream(imgIn);
            imgIn.close();
            int[] t = new int[1];
            GLES20.glGenTextures(1, t, 0);
            tex = t[0];
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, tex);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bmp, 0);
            bmp.recycle();
        } catch (Exception e) {
            nIdx = 0;
            return;
        }
        // Gerakan vertex ringan: ekor/belakang (z < 0) mengibas halus, dada/atas bernapas lembut
        String vs = "uniform mat4 uMVP,uM;uniform float uTP,uBreath;attribute vec3 aP,aN;attribute vec2 aT;varying vec2 vT;varying float vL;"
                + "void main(){vec3 p=aP;"
                + "float tw=clamp(-p.z*1.3,0.,1.);tw*=tw;"
                + "p.x+=0.032*sin(uTP-p.z*3.2)*tw;"
                + "float hw=clamp(p.z*1.2,0.,1.)*clamp(p.y,0.,1.);"
                + "p.y+=0.015*uBreath*hw;"
                + "p.x*=1.0+0.018*uBreath*clamp(p.y,0.2,0.8);"
                + "gl_Position=uMVP*vec4(p,1.);vT=aT;"
                + "vec3 n=normalize(mat3(uM)*aN);"
                + "vL=0.68+0.42*max(dot(n,normalize(vec3(-0.35,0.8,0.6))),0.);}";
        String fs = "precision mediump float;uniform sampler2D uS;varying vec2 vT;varying float vL;"
                + "void main(){vec3 c=texture2D(uS,vT).rgb;"
                + "gl_FragColor=vec4(clamp(c*vL*1.08,0.,1.),1.);}";
        prog = link(vs, fs);
        uMVP = GLES20.glGetUniformLocation(prog, "uMVP");
        uM = GLES20.glGetUniformLocation(prog, "uM");
        uTP = GLES20.glGetUniformLocation(prog, "uTP");
        uBreath = GLES20.glGetUniformLocation(prog, "uBreath");
        uS = GLES20.glGetUniformLocation(prog, "uS");
        aP = GLES20.glGetAttribLocation(prog, "aP");
        aN = GLES20.glGetAttribLocation(prog, "aN");
        aT = GLES20.glGetAttribLocation(prog, "aT");
    }

    String id() {
        return assetId;
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

    private static float smooth01(float a, float b, float x) {
        float k = Math.max(0f, Math.min(1f, (x - a) / (b - a)));
        return k * k * (3f - 2f * k);
    }

    void draw(float[] view, float[] proj) {
        if (nIdx == 0) return;
        long nn = System.nanoTime();
        float t = (nn - t0) / 1e9f;
        float ap = smooth01(0f, 0.7f, t);

        // Ikut bereaksi saat murid menjawab soal (membaca sinyal NagaScene.rType)
        float jumpY = 0f, pitchReact = 0f;
        int rt = NagaScene.rType;
        if (rt != 0) {
            float dur = rt == 1 ? 0.9f : 1.1f;
            float rp = (nn - NagaScene.rT0) / 1e9f / dur;
            if (rp >= 0f && rp < 1f) {
                if (rt == 1) {
                    jumpY = 8f * (float) Math.abs(Math.sin(rp * Math.PI * 2f)) * (1f - rp);
                    pitchReact = -8f * (float) Math.sin(rp * Math.PI);
                } else {
                    pitchReact = 10f * (float) Math.sin(rp * Math.PI);
                }
            }
        }

        // Gerakan idle sederhana: bernapas, bergoyang halus, dan sesekali melompat kecil tiap 6 detik
        float cyc = (t % 6f);
        float hop = cyc > 5.1f ? (float) Math.sin((cyc - 5.1f) / 0.9f * Math.PI) : 0f;
        float breath = (float) Math.sin(t * 2.4f);
        float swayX = 0.6f * (float) Math.sin(t * 1.1f);
        float yaw = 16f + 6f * (float) Math.sin(t * 0.75f);
        float roll = 1.8f * (float) Math.sin(t * 1.1f);
        float pitch = pitchReact - 3.5f * hop + 1.2f * (float) Math.sin(t * 1.8f);

        float baseScale = "pet_reindeer".equals(assetId) ? 38f
                : "pet_alligator".equals(assetId) ? 34f : 32f;
        float sc = baseScale * (0.3f + 0.7f * ap);

        Matrix.setIdentityM(model, 0);
        // Berdiri di sisi kiri avatar (-33), lantai y=-41, sedikit di depan (z=10)
        Matrix.translateM(model, 0, -33f + swayX, -41f + jumpY + 3.2f * hop, 10f);
        Matrix.rotateM(model, 0, yaw, 0f, 1f, 0f);
        Matrix.rotateM(model, 0, pitch, 1f, 0f, 0f);
        Matrix.rotateM(model, 0, roll, 0f, 0f, 1f);
        Matrix.scaleM(model, 0, sc, sc * (1f + 0.018f * breath), sc);
        Matrix.multiplyMM(mv, 0, view, 0, model, 0);
        Matrix.multiplyMM(mvp, 0, proj, 0, mv, 0);

        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        GLES20.glUseProgram(prog);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);
        GLES20.glUniformMatrix4fv(uMVP, 1, false, mvp, 0);
        GLES20.glUniformMatrix4fv(uM, 1, false, model, 0);
        GLES20.glUniform1f(uTP, t * 3.2f);
        GLES20.glUniform1f(uBreath, breath);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, tex);
        GLES20.glUniform1i(uS, 0);
        vb.position(0);
        GLES20.glVertexAttribPointer(aP, 3, GLES20.GL_FLOAT, false, 32, vb);
        GLES20.glEnableVertexAttribArray(aP);
        vb.position(3);
        GLES20.glVertexAttribPointer(aN, 3, GLES20.GL_FLOAT, false, 32, vb);
        GLES20.glEnableVertexAttribArray(aN);
        vb.position(6);
        GLES20.glVertexAttribPointer(aT, 2, GLES20.GL_FLOAT, false, 32, vb);
        GLES20.glEnableVertexAttribArray(aT);
        ib.position(0);
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, nIdx, GLES20.GL_UNSIGNED_SHORT, ib);
        GLES20.glDisableVertexAttribArray(aP);
        GLES20.glDisableVertexAttribArray(aN);
        GLES20.glDisableVertexAttribArray(aT);
    }
}
