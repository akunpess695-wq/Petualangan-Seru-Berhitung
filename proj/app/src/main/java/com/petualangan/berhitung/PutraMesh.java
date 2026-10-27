package com.petualangan.berhitung;

import java.util.ArrayList;

/**
 * Geometri putri untuk renderer OpenGL ES (Tahap E). Tanpa dependensi Android,
 * port dari tahap-E/geom.py + putri.py. Sumbu: y atas, depan +z, kaki di y=0.
 * Hasil: pos/nor/col (3 float per titik) dan idx (3 indeks per segitiga).
 */
final class PutraMesh {
    interface Pleat { double k(double ph, double y); }
    interface YOf { double[] range(double ph); }
    interface MaskFn { boolean cut(double ph, double th); }
    interface Deform { void apply(double[][][] P); }

    float[] pos, nor, col;
    int[] idx;
    int vertCount, triCount;

    // ---- penampung ----
    private final ArrayList<float[]> bp = new ArrayList<>(), bn = new ArrayList<>(), bc = new ArrayList<>();
    private final ArrayList<int[]> bi = new ArrayList<>();
    private int voff = 0;

    // ---- penanda bagian: tiap potongan geometri diberi nomor bagian supaya warnanya bisa diganti / disembunyikan
    // per tampilan tanpa mengubah mesh bersama (static di PutriGLView). ----
    static final int P_OTHER = 0, P_SHIRT = 1, P_TRIM = 2, P_BUTTON = 3, P_SLEEVE = 4, P_PANTS = 5, P_HAIR = 6, P_PECI = 7,
            P_HAIR_CREW = 8, P_HAIR_SPIKY = 9, P_HAIR_MESSY = 10, P_HAIR_DET = 11, P_HAIR_LOW = 12, P_HAIR_SIDE = 13, P_HAIR_CURL = 14, P_HAIR_CROWN = 15, P_CURL_LOW = 16;
    /** Gaya rambut (terlihat hanya bila peci dilepas). 0 = cangkang rambut bawaan (rapi). */
    static final int STYLE_DEFAULT = 0, STYLE_CREW = 1, STYLE_SPIKY = 2, STYLE_MESSY = 3, STYLE_SIDE = 4, STYLE_CURLY = 5,
            STYLE_HOOD = 6;   // tudung hewan dipakai: peci dan semua rambut disembunyikan (tudung digambar HoodMesh)
    private int cur = P_OTHER;                         // bagian untuk potongan yang dibuat berikutnya
    private final ArrayList<Integer> bpt = new ArrayList<>();
    byte[] part;                                       // bagian tiap titik (sama panjang dengan vertCount)

    // ---- gradasi rambut: pengali terang/gelap per titik (1 = warna asli). Hanya dipakai bagian rambut; warna dasar
    // tetap "#hex" pilihan pemain, jadi semua warna rambut otomatis ikut bergradasi (akar gelap, ujung terang, sorot). ----
    interface ShadeFn { double f(double[] p, double[] n, int i, int j, int nu, int nv); }
    private ShadeFn curShade = null;                   // dipakai grid() untuk potongan yang dibuat berikutnya
    private final ArrayList<float[]> bs = new ArrayList<>();
    float[] shade;                                     // pengali tiap titik (sama panjang dengan vertCount)
    private double tone = 1.0;                         // variasi nada per helai

    static final double PI = Math.PI;
    static final double[] WHITE = {0.95, 0.95, 0.98}, SKIN = {1.0, 0.878, 0.702}, MAR = {0.64, 0.12, 0.18},
            BAG = {0.75, 0.14, 0.16}, SHOE = {0.106, 0.106, 0.106}, BROWN = {0.31, 0.19, 0.14},
            PINK = {1.0, 0.76, 0.72}, HIJ = {0.93, 0.93, 0.97};
    static final double[] HC = {0, 68.5, 0.8}, HR = {12.6, 12.9, 12.5};
    // tabel: {y, rx, rz, cx, cz}
    static final double[][] TORSO = {{57, 6.0, 5.5, 0, 0}, {52.5, 10.6, 8.2, 0, 0}, {49, 11.6, 8.9, 0, -0.2}, {40, 11.3, 8.8, 0, -0.2}, {31, 10.9, 8.6, 0, -0.2}, {29, 10.9, 8.6, 0, -0.2}};
    // hijab = SATU cangkang: kubah kepala -> lebar di bahu -> ujung runcing depan & belakang
    static final double[][] HIJAB = {
            {89.5, 0.15, 0.15, 0, -3.5}, {89.0, 3.8, 4.0, 0, -3.5}, {88.0, 6.5, 6.8, 0, -3.5}, {86.5, 8.9, 9.4, 0, -3.5},
            {84.5, 11.2, 11.8, 0, -3.5}, {82.0, 13.1, 13.8, 0, -3.5}, {79.0, 14.7, 15.4, 0, -3.5}, {75.5, 15.7, 16.5, 0, -3.5},
            {72.0, 16.0, 16.8, 0, -3.5}, {68.0, 16.0, 16.8, 0, -3.6}, {64.0, 15.5, 16.2, 0, -4.0}, {60.0, 14.8, 15.0, 0, -4.3},
            {56.0, 13.9, 14.0, 0, -4.6}, {52.0, 15.8, 16.0, 0, -3.8}, {48.0, 17.2, 17.5, 0, -4.0}, {44.0, 17.6, 17.5, 0, -4.0},
            {40.0, 17.6, 17.5, 0, -4.0}};
    // penutup leher (bagian bawah hijab): menutup kepala dari bawah supaya kulit tidak terlihat lewat celah tepi hijab
    static final double[][] NECK = {{64, 12.2, 11.8, 0, -0.3}, {62, 11.3, 10.6, 0, -0.7}, {60, 9.9, 9.0, 0, -1.0}, {58, 9.2, 8.4, 0, -1.8}, {56, 9.5, 8.0, 0, -2.2}, {54, 10.4, 8.3, 0, -1.2}, {52.5, 10.7, 8.3, 0, -0.5}};
    static final double[][] SKIRT = {{31, 10.4, 8.4, 0, -0.2}, {29, 10.7, 8.7, 0, -0.2}, {20, 11.6, 9.4, 0, -0.4}, {10, 12.5, 10.1, 0, -0.7}, {5.2, 12.9, 10.4, 0, -0.8}};

    static final double P_TOP = 0.45;
    /** profil kepala: elipsoid, tetapi bagian atas lebih penuh (tengkorak lebar di bawah peci): f = cos(th)^p(th). */
    static double headP(double th) {
        if (th <= 0) return 1.0;
        double t = Math.min(1.0, th / 0.6); t = t * t * (3 - 2 * t);
        return 1.0 - (1.0 - P_TOP) * t;
    }
    static double headF(double th) { return Math.pow(Math.max(0, Math.cos(th)), headP(th)); }
    static double onHead(double x, double y, double off) {
        double sn = Math.max(-1, Math.min(1, (y - HC[1]) / HR[1]));
        double f = Math.max(1e-6, headF(Math.asin(sn)));
        double u = sq(x / (HR[0] * f));
        return HC[2] + HR[2] * f * Math.sqrt(Math.max(0, 1 - u)) + off;
    }
    static double sq(double a) { return a * a; }

    // ---- util vektor ----
    static double[] sub(double[] a, double[] b) { return new double[]{a[0] - b[0], a[1] - b[1], a[2] - b[2]}; }
    static double[] cross(double[] a, double[] b) {
        return new double[]{a[1] * b[2] - a[2] * b[1], a[2] * b[0] - a[0] * b[2], a[0] * b[1] - a[1] * b[0]};
    }
    static double[] unit(double[] a) {
        double l = Math.sqrt(a[0] * a[0] + a[1] * a[1] + a[2] * a[2]);
        if (l < 1e-9) l = 1;
        return new double[]{a[0] / l, a[1] / l, a[2] / l};
    }

    /** Interpolasi kubik (Hermite, tangen selisih-hingga) agar normal mulus antar baris tabel. */
    static double[] interp(double[][] tab, double y) {
        int n = tab.length;
        double[] r = new double[4];
        if (y >= tab[0][0]) { for (int k = 0; k < 4; k++) r[k] = tab[0][k + 1]; return r; }
        if (y <= tab[n - 1][0]) { for (int k = 0; k < 4; k++) r[k] = tab[n - 1][k + 1]; return r; }
        int a = 0;
        while (!(y <= tab[a][0] && y >= tab[a + 1][0])) a++;
        double h = tab[a][0] - tab[a + 1][0];
        double t = (tab[a][0] - y) / h;
        double t2 = t * t, t3 = t2 * t;
        double h00 = 2 * t3 - 3 * t2 + 1, h10 = t3 - 2 * t2 + t, h01 = -2 * t3 + 3 * t2, h11 = t3 - t2;
        for (int k = 0; k < 4; k++) {
            double p0 = tab[a][k + 1], p1 = tab[a + 1][k + 1];
            double m0 = tang(tab, a, k), m1 = tang(tab, a + 1, k);
            r[k] = h00 * p0 + h10 * h * m0 + h01 * p1 + h11 * h * m1; // m = turunan terhadap arah indeks (y menurun)
        }
        return r; // rx, rz, cx, cz
    }
    private static double tang(double[][] tab, int i, int k) {
        int n = tab.length;
        if (i == 0) return (tab[1][k + 1] - tab[0][k + 1]) / (tab[0][0] - tab[1][0]);
        if (i == n - 1) return (tab[n - 1][k + 1] - tab[n - 2][k + 1]) / (tab[n - 2][0] - tab[n - 1][0]);
        return (tab[i + 1][k + 1] - tab[i - 1][k + 1]) / (tab[i - 1][0] - tab[i + 1][0]);
    }

    // ---- primitif ----
    private void grid(double[][][] P, double[] colr, boolean closedU, boolean flip, boolean[][] keep) {
        int nu = P.length, nv = P[0].length;
        float[] fp = new float[nu * nv * 3], fn = new float[nu * nv * 3], fc = new float[nu * nv * 3], fs = new float[nu * nv];
        for (int i = 0; i < nu; i++) {
            for (int j = 0; j < nv; j++) {
                double[] du;
                if (closedU) du = sub(P[(i + 1) % nu][j], P[(i - 1 + nu) % nu][j]);
                else if (i == 0) du = sub(P[1][j], P[0][j]);
                else if (i == nu - 1) du = sub(P[nu - 1][j], P[nu - 2][j]);
                else du = sub(P[i + 1][j], P[i - 1][j]);
                double[] dv;
                if (j == 0) dv = sub(P[i][1], P[i][0]);
                else if (j == nv - 1) dv = sub(P[i][nv - 1], P[i][nv - 2]);
                else dv = sub(P[i][j + 1], P[i][j - 1]);
                double[] n = unit(cross(du, dv));
                if (flip) { n[0] = -n[0]; n[1] = -n[1]; n[2] = -n[2]; }
                fs[i * nv + j] = curShade == null ? 1f : (float) curShade.f(P[i][j], n, i, j, nu, nv);
                int o = (i * nv + j) * 3;
                for (int k = 0; k < 3; k++) { fp[o + k] = (float) P[i][j][k]; fn[o + k] = (float) n[k]; fc[o + k] = (float) colr[k]; }
            }
        }
        int ru = closedU ? nu : nu - 1;
        ArrayList<Integer> F = new ArrayList<>();
        for (int i = 0; i < ru; i++) {
            int i2 = (i + 1) % nu;
            for (int j = 0; j < nv - 1; j++) {
                if (keep != null && !keep[i][j]) continue;
                int a = i * nv + j, b = i2 * nv + j, c = i2 * nv + j + 1, d = i * nv + j + 1;
                if (flip) { add(F, a, c, b); add(F, a, d, c); } else { add(F, a, b, c); add(F, a, c, d); }
            }
        }
        int[] fi = new int[F.size()];
        for (int k = 0; k < fi.length; k++) fi[k] = F.get(k) + voff;
        bp.add(fp); bn.add(fn); bc.add(fc); bi.add(fi); bpt.add(cur); bs.add(fs);
        voff += nu * nv;
    }
    private static void add(ArrayList<Integer> f, int a, int b, int c) { f.add(a); f.add(b); f.add(c); }

    void ellipsoid(double[] c, double[] r, double[] colr, int nu, int nv, double th0, double th1, MaskFn mask, Deform deform) {
        double[][][] P = new double[nu][nv][3];
        double[] PH = new double[nu], TH = new double[nv];
        for (int i = 0; i < nu; i++) PH[i] = 2 * PI * i / nu;
        for (int j = 0; j < nv; j++) TH[j] = th0 + (th1 - th0) * j / (nv - 1);
        for (int i = 0; i < nu; i++)
            for (int j = 0; j < nv; j++) {
                P[i][j][0] = c[0] + r[0] * Math.cos(TH[j]) * Math.sin(PH[i]);
                P[i][j][1] = c[1] + r[1] * Math.sin(TH[j]);
                P[i][j][2] = c[2] + r[2] * Math.cos(TH[j]) * Math.cos(PH[i]);
            }
        if (deform != null) deform.apply(P);
        boolean[][] keep = null;
        if (mask != null) {
            keep = new boolean[nu][nv];
            for (int i = 0; i < nu; i++)
                for (int j = 0; j < nv; j++) keep[i][j] = true;
            for (int i = 0; i < nu; i++)
                for (int j = 0; j < nv - 1; j++) {
                    double pm = (i < nu - 1) ? (PH[i] + PH[i + 1]) / 2 : ((PH[nu - 1] + 2 * PI) + PH[0]) / 2;
                    double tm = (TH[j] + TH[j + 1]) / 2;
                    double w = ((pm + PI) % (2 * PI)); if (w < 0) w += 2 * PI; w -= PI;
                    keep[i][j] = !mask.cut(w, tm);
                }
        }
        grid(P, colr, true, false, keep);
    }
    void ellipsoid(double[] c, double[] r, double[] colr, int nu, int nv) { ellipsoid(c, r, colr, nu, nv, -PI / 2, PI / 2, null, null); }

    private static double sp(double a, double p) { return Math.signum(a) * Math.pow(Math.abs(a), p); }
    /** Stiker wajah: elips (rx, ry) di (cx, cy) yang mengikuti lengkung kepala, setebal 'thick' di atas permukaan + off. */
    void decal(double cx, double cy, double rx, double ry, double thick, double off, double[] colr, int nu, int nv) {
        ellipsoid(new double[]{cx, cy, 0}, new double[]{rx, ry, thick}, colr, nu, nv, -PI / 2, PI / 2, null, P -> {
            for (double[][] row : P)
                for (double[] v : row) v[2] = onHead(v[0], v[1], off) + Math.max(v[2], 0);
        });
    }

    void superbox(double[] c, double[] h, double[] colr, double e, int nu, int nv) {
        double[][][] P = new double[nu][nv][3];
        for (int i = 0; i < nu; i++)
            for (int j = 0; j < nv; j++) {
                double ph = 2 * PI * i / nu, th = -PI / 2 + PI * j / (nv - 1);
                P[i][j][0] = c[0] + h[0] * sp(Math.cos(th), e) * sp(Math.sin(ph), e);
                P[i][j][1] = c[1] + h[1] * sp(Math.sin(th), e);
                P[i][j][2] = c[2] + h[2] * sp(Math.cos(th), e) * sp(Math.cos(ph), e);
            }
        grid(P, colr, true, false, null);
    }

    void tube(double[][] path, double[][] rad, double[] colr, int ns) {
        int n = path.length;
        double[][] T = new double[n][];
        for (int k = 0; k < n; k++) {
            double[] g;
            if (k == 0) g = sub(path[1], path[0]);
            else if (k == n - 1) g = sub(path[n - 1], path[n - 2]);
            else { g = sub(path[k + 1], path[k - 1]); g[0] /= 2; g[1] /= 2; g[2] /= 2; }
            T[k] = unit(g);
        }
        double[][][] P = new double[ns][n][3];
        for (int k = 0; k < n; k++) {
            double[] t = T[k];
            double[] H = Math.abs(t[2]) > 0.7 ? new double[]{0, 1, 0} : new double[]{0, 0, 1};
            double[] a = unit(cross(t, H)), b = unit(cross(t, a));
            for (int i = 0; i < ns; i++) {
                double ang = 2 * PI * i / ns;
                for (int m = 0; m < 3; m++)
                    P[i][k][m] = path[k][m] + a[m] * rad[k][0] * Math.cos(ang) + b[m] * rad[k][1] * Math.sin(ang);
            }
        }
        grid(P, colr, true, false, null);
    }
    static double[][] rr(int n, double a, double b) { double[][] r = new double[n][]; for (int i = 0; i < n; i++) r[i] = new double[]{a, b}; return r; }

    void loft(double[][] tab, double[] colr, int nu, int ny, Pleat pleat, YOf yof) { loft(tab, colr, nu, ny, pleat, yof, null); }

    /** mask.cut(ph, y): ph di [-PI,PI) (0 = depan), y = tinggi; true = lubang (dibuang). */
    void loft(double[][] tab, double[] colr, int nu, int ny, Pleat pleat, YOf yof, MaskFn mask) {
        double[][][] P = new double[nu][ny][3];
        double[][] YY = new double[nu][ny];
        for (int i = 0; i < nu; i++) {
            double ph = 2 * PI * i / nu;
            double yt = tab[0][0], yb = tab[tab.length - 1][0];
            if (yof != null) { double[] r = yof.range(ph); yt = r[0]; yb = r[1]; }
            for (int j = 0; j < ny; j++) {
                double y = yt + (yb - yt) * j / (ny - 1);
                double[] q = interp(tab, y);
                double k = pleat == null ? 1.0 : pleat.k(ph, y);
                P[i][j][0] = q[2] + q[0] * k * Math.sin(ph);
                P[i][j][1] = y;
                P[i][j][2] = q[3] + q[1] * k * Math.cos(ph);
                YY[i][j] = y;
            }
        }
        boolean[][] keep = null;
        if (mask != null) {
            keep = new boolean[nu][ny];
            for (int i = 0; i < nu; i++)
                for (int j = 0; j < ny; j++) keep[i][j] = true;
            for (int i = 0; i < nu; i++)
                for (int j = 0; j < ny - 1; j++) {
                    double pm = 2 * PI * (i + 0.5) / nu;
                    double w = ((pm + PI) % (2 * PI)); if (w < 0) w += 2 * PI; w -= PI;
                    keep[i][j] = !mask.cut(w, (YY[i][j] + YY[i][j + 1]) / 2);
                }
        }
        grid(P, colr, true, false, keep);
    }

    /** Kepala/rambut: seperti ellipsoid tetapi profil atas penuh (headF). mask.cut(w, y): true = lubang. */
    void skull(double[] c, double[] r, double[] colr, int nu, int nv, double th0, MaskFn mask) {
        double[][][] P = new double[nu][nv][3];
        double[] PH = new double[nu], TH = new double[nv];
        for (int i = 0; i < nu; i++) PH[i] = 2 * PI * i / nu;
        for (int j = 0; j < nv; j++) TH[j] = th0 + (PI / 2 - th0) * j / (nv - 1);
        for (int i = 0; i < nu; i++)
            for (int j = 0; j < nv; j++) {
                double f = headF(TH[j]);
                P[i][j][0] = c[0] + r[0] * f * Math.sin(PH[i]);
                P[i][j][1] = c[1] + r[1] * Math.sin(TH[j]);
                P[i][j][2] = c[2] + r[2] * f * Math.cos(PH[i]);
            }
        boolean[][] keep = null;
        if (mask != null) {
            keep = new boolean[nu][nv];
            for (int i = 0; i < nu; i++)
                for (int j = 0; j < nv; j++) keep[i][j] = true;
            for (int i = 0; i < nu; i++)
                for (int j = 0; j < nv - 1; j++) {
                    double pm = 2 * PI * (i + 0.5) / nu;
                    double w = ((pm + PI) % (2 * PI)); if (w < 0) w += 2 * PI; w -= PI;
                    keep[i][j] = !mask.cut(w, (P[i][j][1] + P[i][j + 1][1]) / 2);
                }
        }
        grid(P, colr, true, false, keep);
    }

    /** Bidang datar per segi (low-poly): tiap kotak punya 4 titik sendiri dengan normal rata. P[i][j], i melingkar. */
    private void facet(double[][][] P, double[] colr) {
        int nu = P.length, nv = P[0].length;
        int cnt = nu * (nv - 1) * 4;
        float[] fp = new float[cnt * 3], fn = new float[cnt * 3], fc = new float[cnt * 3];
        int[] fi = new int[nu * (nv - 1) * 6];
        int q = 0;
        for (int i = 0; i < nu; i++) {
            int i2 = (i + 1) % nu;
            for (int j = 0; j < nv - 1; j++) {
                double[][] v = {P[i][j], P[i2][j], P[i2][j + 1], P[i][j + 1]};
                double[] n = unit(cross(sub(v[2], v[0]), sub(v[3], v[1])));
                for (int k = 0; k < 4; k++) {
                    int o = (q * 4 + k) * 3;
                    for (int m = 0; m < 3; m++) { fp[o + m] = (float) v[k][m]; fn[o + m] = (float) n[m]; fc[o + m] = (float) colr[m]; }
                }
                int base = voff + q * 4, t = q * 6;
                fi[t] = base; fi[t + 1] = base + 1; fi[t + 2] = base + 2; fi[t + 3] = base; fi[t + 4] = base + 2; fi[t + 5] = base + 3;
                q++;
            }
        }
        float[] fs1 = new float[cnt]; java.util.Arrays.fill(fs1, 1f);
        bp.add(fp); bn.add(fn); bc.add(fc); bi.add(fi); bpt.add(cur); bs.add(fs1);
        voff += cnt;
    }

    // ---- rakitan putra ----
    static final double[] EYE = {0.29, 0.15, 0.10}, EYE2 = {0.56, 0.33, 0.20}, PUPIL = {0.10, 0.05, 0.04}, LASH = {0.13, 0.07, 0.05}, LIP = {0.88, 0.31, 0.31};
    static final double[] HAIR = {0.357, 0.227, 0.180}, PBAG = {0.80, 0.18, 0.18}, PANTS = {0.608, 0.118, 0.176}, CAP = {0.92, 0.93, 0.97}, SOLE = {0.96, 0.96, 0.96};
    static final double[] HH = {13.0, 13.3, 13.0};            // jari-jari cangkang rambut (sedikit di luar kepala)
    static final double HAIR_W = Math.toRadians(66);          // lebar bukaan wajah pada rambut
    static final double[][] PANTSTOP = {{31, 10.4, 8.4, 0, -0.2}, {29, 10.6, 8.6, 0, -0.2}, {26, 10.9, 8.8, 0, -0.2}, {23.5, 11.0, 8.8, 0, -0.2}};
    // peci: {y, rx, rz, cx, cz}, tutup datar di atas, bibir tipis di bawah
    static final double[][] PECI = {{89.0, 0.0, 0.0, 0, 0.8}, {88.95, 10.0, 10.0, 0, 0.8}, {88.2, 11.1, 11.1, 0, 0.8}, {82.4, 12.2, 12.2, 0, 0.8}, {75.8, 12.9, 12.9, 0, 0.8}, {75.0, 12.8, 12.8, 0, 0.8}, {75.0, 11.0, 11.0, 0, 0.8}};

    /** garis rambut (batas bukaan wajah): y di tengah 74,2 dan turun ke 71,0 di pelipis (dahi dan alis terlihat, poni tipis di bawah peci). */
    static double hairlineY(double w) { return 74.2 - 3.2 * sq(w / HAIR_W); }
    /** tepi bawah rambut: di samping sampai telinga (menutupi bagian atas telinga, y=EAR_HAIR), makin ke belakang turun ke y=63. */
    static final double EAR_HAIR = 67.0;
    static double lowerY(double aw) {
        double d1 = Math.toRadians(100), d2 = Math.toRadians(140), a1 = Math.toRadians(88);
        if (aw >= d2) return 63.0;
        if (aw >= d1) return EAR_HAIR - (EAR_HAIR - 63.0) * (aw - d1) / (d2 - d1);
        if (aw >= a1) return EAR_HAIR;
        double t = Math.max(0, (aw - HAIR_W) / (a1 - HAIR_W)); t = t * t * (3 - 2 * t);   // cambang miring halus dari pelipis ke telinga
        return hairlineY(HAIR_W) + (EAR_HAIR - hairlineY(HAIR_W)) * t;
    }
    /** titik di permukaan cangkang rambut pada sudut w (0 = depan) dan tinggi y. */
    static double[] onHair(double w, double y) {
        double f = headF(Math.asin((y - HC[1]) / HH[1]));
        return new double[]{HH[0] * f * Math.sin(w), y, HC[2] + HH[2] * f * Math.cos(w)};
    }

    static PutraMesh build() {
        final PutraMesh M = new PutraMesh();
        // sepatu + kaki celana (celana panjang lurus, ujung menutup bagian atas sepatu)
        for (int sg = -1; sg <= 1; sg += 2) {
            double s = sg;
            M.cur = P_OTHER;
            M.ellipsoid(new double[]{s * 5.8, 0.5, -0.3}, new double[]{4.8, 4.5, 7.5}, SHOE, 28, 14, 0, PI / 2, null, null);
            M.ellipsoid(new double[]{s * 5.8, 0.6, -0.3}, new double[]{4.8, 0.6, 7.5}, SOLE, 28, 6, -PI / 2, 0, null, null);
            M.cur = P_PANTS;
            double[][] lp = {{s * 5.6, 24, 0}, {s * 5.7, 14, 0}, {s * 5.8, 4.6, 0}};
            double[][] lr = {{5.5, 7.2}, {6.0, 7.4}, {6.8, 7.6}};
            M.tube(lp, lr, PANTS, 24);
            M.ellipsoid(new double[]{s * 5.8, 4.6, 0}, new double[]{6.8, 2.0, 7.6}, PANTS, 24, 8, -PI / 2, 0, null, null); // tutup ujung kaki celana
        }
        // pinggul celana + penutup selangkangan (supaya tidak ada rongga dari bawah)
        M.cur = P_PANTS;
        M.loft(PANTSTOP, PANTS, 56, 12, null, null);
        M.ellipsoid(new double[]{0, 23.7, -0.2}, new double[]{9.6, 1.4, 7.6}, PANTS, 32, 8);
        // kemeja
        M.cur = P_SHIRT;
        M.loft(TORSO, WHITE, 48, 16, null, null);
        M.ellipsoid(new double[]{0, 57, 0}, new double[]{6.0, 0.8, 5.5}, WHITE, 32, 6, 0, PI / 2, null, null); // tutup atas kemeja
        M.cur = P_TRIM;
        double[] cq = interp(TORSO, 55.0);
        double[][] collar = new double[41][];
        for (int i = 0; i < 40; i++) { double t = 2 * PI * i / 40; collar[i] = new double[]{cq[2] + (cq[0] + 0.3) * Math.sin(t), 55.6, cq[3] + (cq[1] + 0.3) * Math.cos(t)}; }
        collar[40] = collar[0];
        M.tube(collar, rr(41, 0.75, 0.65), new double[]{0.86, 0.88, 0.95}, 10); // kerah
        double[][] plk = new double[10][];
        for (int i = 0; i < 10; i++) { double y = 54.5 - 25.0 * i / 9; plk[i] = new double[]{0, y, tz(0, y) - 0.2 + 0.35}; }
        M.tube(plk, rr(10, 0.45, 0.3), new double[]{0.80, 0.83, 0.92}, 6); // lajur kancing kemeja
        M.cur = P_BUTTON;
        for (int bi2 = 0; bi2 < 5; bi2++) { double y = 51.0 - 5.2 * bi2; M.ellipsoid(new double[]{0, y, tz(0, y) + 0.45}, new double[]{0.55, 0.55, 0.3}, new double[]{0.70, 0.74, 0.86}, 8, 6); }
        // lengan kemeja panjang
        for (int sg = -1; sg <= 1; sg += 2) {
            double s = sg;
            M.cur = P_SLEEVE;
            double[][] path = {{s * 10.8, 48.5, -0.6}, {s * 12.4, 42, -1.2}, {s * 13.8, 36, -1.8}, {s * 15.0, 30.0, -2.0}};
            double[] rd = {3.9, 3.8, 3.7, 3.9};
            double[][] rad = new double[4][];
            for (int i = 0; i < 4; i++) rad[i] = new double[]{rd[i], rd[i]};
            M.tube(path, rad, WHITE, 16);
            M.ellipsoid(new double[]{s * 10.8, 48.5, -0.6}, new double[]{3.9, 3.9, 3.9}, WHITE, 16, 10);
            double[][] cuff = new double[2][];
            cuff[0] = new double[]{s * 14.6, 30.6, -1.95}; cuff[1] = new double[]{s * 14.9, 29.2, -2.0};
            M.tube(cuff, new double[][]{{4.1, 4.1}, {4.2, 4.2}}, new double[]{0.86, 0.88, 0.95}, 16); // manset
            M.cur = P_OTHER;
            M.ellipsoid(new double[]{s * 15.0, 26.6, -0.4}, new double[]{2.7, 4.0, 2.5}, SKIN, 18, 12);
        }
        M.cur = P_OTHER;
        // ransel: badan + saku depan + kancing + tali
        M.superbox(new double[]{0, 38.5, -12.7}, new double[]{10.9, 12.5, 4.1}, PBAG, 0.32, 36, 20);
        M.superbox(new double[]{0, 35.3, -17.3}, new double[]{7.6, 5.3, 1.0}, PBAG, 0.30, 36, 20);
        M.superbox(new double[]{0, 38.2, -18.4}, new double[]{1.1, 1.0, 0.4}, new double[]{0.62, 0.62, 0.66}, 0.3, 16, 8);
        for (int sg = -1; sg <= 1; sg += 2) {
            double s = sg;
            double[][] pth = {{s * 9.6, 46, -9.4}, {s * 9.9, 51.5, -5.5}, {s * 10.1, 53.2, 0.0}, {s * 9.9, 51.0, tz(9.9, 51) + 0.9},
                    {s * 9.8, 45, tz(9.8, 45) + 0.9}, {s * 9.8, 40, tz(9.8, 40) + 0.9}, {s * 9.8, 36.5, tz(9.8, 36.5) + 0.9}};
            M.tube(pth, rr(7, 1.35, 0.5), PBAG, 8);
        }
        // kepala + wajah
        final int headStart = M.bp.size();
        M.skull(HC, HR, SKIN, 56, 32, -PI / 2, null);
        for (int sg = -1; sg <= 1; sg += 2) {
            double s = sg, ex = s * 5.9, ey = 66.6;
            M.decal(ex, ey + 0.2, 3.15, 3.55, 0.40, 0.03, new double[]{0.99, 0.96, 0.93}, 24, 14);
            M.decal(ex, ey - 0.1, 2.95, 3.4, 0.15, 0.43, EYE, 24, 14);
            M.decal(ex, ey - 1.2, 2.3, 1.75, 0.10, 0.50, EYE2, 20, 10);
            M.decal(ex, ey, 1.55, 2.1, 0.12, 0.58, PUPIL, 18, 10);
            M.decal(ex - 0.85, ey + 1.5, 0.95, 1.05, 0.10, 0.70, new double[]{1, 1, 1}, 12, 8);
            M.decal(ex + 1.0, ey - 1.3, 0.45, 0.48, 0.08, 0.70, new double[]{1, 1, 1}, 10, 6);
            int nl = 8;
            double[][] lash = new double[nl][], lrad = new double[nl][];
            for (int i = 0; i < nl; i++) {
                double u, r;
                u = Math.toRadians(168 - 156.0 * i / (nl - 1)); r = 0.26 * (0.55 + 0.45 * Math.sin(PI * i / (nl - 1)));  // garis kelopak tipis, tanpa jentikan
                double x = ex + s * 3.15 * Math.cos(u), y = ey + 0.2 + 3.55 * Math.sin(u);
                lash[i] = new double[]{x, y, onHead(x, y, 0.4)};
                lrad[i] = new double[]{r, r};
            }
            M.tube(lash, lrad, LASH, 6); // bulu mata atas tipis
            M.decal(s * 7.6, 61.8, 1.85, 1.3, 0.22, 0.05, PINK, 18, 10); // pipi (lembut, di bawah sudut luar mata)
            double[][] br = new double[4][], brr = new double[4][];
            if (sg == 1) M.decal(0, 62.6, 0.6, 0.45, 0.30, 0.05, new double[]{0.96, 0.78, 0.64}, 12, 8); // hidung kecil, sederhana
            double[] bx = {3.5, 5.0, 6.8, 8.4}, by = {70.6, 71.4, 71.3, 70.2}, bw = {0.22, 0.34, 0.32, 0.14};
            for (int i = 0; i < 4; i++) { br[i] = new double[]{s * bx[i], by[i], onHead(s * bx[i], by[i], 0.3)}; brr[i] = new double[]{bw[i], bw[i]}; }
            M.tube(br, brr, HAIR, 8); // alis tipis melengkung
        }
        double[][] arc = new double[7][];
        for (int i = 0; i < 7; i++) { double x = -2.35 + 4.7 * i / 6, y = 58.8 + 0.25 * x * x; arc[i] = new double[]{x, y, onHead(x, y, 0.3)}; }
        M.tube(arc, rr(7, 0.27, 0.27), LIP, 6); // senyum kecil
        // rambut: cangkang di atas kepala, bukaan wajah dipotong di depan; bagian bawah berhenti di y=63
        M.cur = P_HAIR;
        M.curShade = SHELL;
        final double th0 = Math.asin((63.0 - HC[1]) / HH[1]);
        M.skull(HC, HH, HAIR, 144, 64, th0, (w, y) -> {
            double aw = Math.abs(w);
            return aw < HAIR_W ? y < hairlineY(w) : y < lowerY(aw);
        });
        // telinga (terlihat di bawah tepi rambut samping)
        M.cur = P_OTHER;
        for (int sg = -1; sg <= 1; sg += 2)
            M.ellipsoid(new double[]{sg * 12.2, 65.4, 0.6}, new double[]{1.7, 3.0, 2.1}, SKIN, 16, 10);
        M.cur = P_HAIR;
        M.curShade = constShade(0.9);
        // tepi rambut (poni dan cambang) diberi tabung tipis supaya tepi tidak bergerigi
        double[][] fr = new double[17][], frr = new double[17][];
        for (int i = 0; i < 17; i++) { double w = -HAIR_W + 2 * HAIR_W * i / 16; fr[i] = onHair(w, hairlineY(w)); frr[i] = new double[]{0.42, 0.42}; }
        M.tube(fr, frr, HAIR, 8);
        for (int sg = -1; sg <= 1; sg += 2) {
            // tepi cambang: turun lurus dari ujung garis rambut lalu mengikuti tepi bawah ke belakang
            int nsb = 14;
            double[][] sb = new double[nsb][], sbr = new double[nsb][];
            for (int i = 0; i < nsb; i++) {
                double[] q;
                if (i < 4) q = onHair(sg * HAIR_W, hairlineY(HAIR_W) - (hairlineY(HAIR_W) - lowerY(HAIR_W)) * i / 3.0);
                else { double w = HAIR_W + (Math.toRadians(140) - HAIR_W) * (i - 4) / (nsb - 5.0); q = onHair(sg * w, lowerY(w)); }
                sb[i] = q; sbr[i] = new double[]{0.5, 0.5};
            }
            M.tube(sb, sbr, HAIR, 8);
        }
        M.curShade = null;
        // peci: bidang datar bersegi 12
        M.cur = P_PECI;
        int ns = 12;
        double[][][] pp = new double[ns][PECI.length][3];
        for (int i = 0; i < ns; i++) {
            double ph = 2 * PI * (i + 0.5) / ns;
            for (int j = 0; j < PECI.length; j++) {
                pp[i][j][0] = PECI[j][3] + PECI[j][1] * Math.sin(ph);
                pp[i][j][1] = PECI[j][0];
                pp[i][j][2] = PECI[j][4] + PECI[j][2] * Math.cos(ph);
            }
        }
        M.facet(pp, CAP);
        M.hairStyles();
        M.detail();
        M.lowHair();
        M.sideStyle();
        M.curlyStyle();
        M.curShade = null;
        M.cur = P_OTHER;
        M.scaleFrom(headStart, 1.2, 1.02, 1.15, 0, 56.0, 0.8); // kepala chibi: lebih lebar, sedikit lebih pendek
        M.finish();
        return M;
    }

    // ---- gaya rambut tambahan (tiap gaya = bagian sendiri; hanya yang dipilih yang digambar) ----
    private static double[] norm3(double x, double y, double z) {
        double l = Math.sqrt(x * x + y * y + z * z);
        if (l < 1e-9) l = 1;
        return new double[]{x / l, y / l, z / l};
    }
    /** arah keluar kira-kira (normal elipsoid cangkang rambut) di titik p */
    private static double[] outward(double[] p) {
        return norm3((p[0] - HC[0]) / sq(HH[0]), (p[1] - HC[1]) / sq(HH[1]), (p[2] - HC[2]) / sq(HH[2]));
    }
    /** Helai runcing/melengkung: tabung meruncing dari titik 'base' (agak terbenam) ke arah 'dir' sepanjang 'len'; bend = lengkung samping. */
    private void strand(double[] base, double[] dir, double len, double r0, double[] bend, int pts, double[] colr) {
        tone = 0.88 + 0.24 * hash01(base[0] * 13.1 + base[1] * 3.7 + base[2] * 7.7);
        double[][] path = new double[pts][], rad = new double[pts][];
        for (int k = 0; k < pts; k++) {
            double t = k == 0 ? -0.18 : (double) k / (pts - 1);
            double r = r0 * (k == 0 ? 1.0 : Math.max(0.06 / r0, 1.0 - 0.93 * t));
            path[k] = new double[]{base[0] + dir[0] * len * t + bend[0] * len * t * t,
                    base[1] + dir[1] * len * t + bend[1] * len * t * t,
                    base[2] + dir[2] * len * t + bend[2] * len * t * t};
            rad[k] = new double[]{r, r};
        }
        tube(path, rad, colr, 7);
    }
    private static final double[] NOBEND = {0, 0, 0};
    static double crewLine(double w) { return 76.2 - 3.2 * sq(w / HAIR_W); }
    static double crewLower(double aw) {
        double d1 = Math.toRadians(100), d2 = Math.toRadians(140);
        if (aw <= d1) return 73.0;
        if (aw >= d2) return 67.0;
        return 73.0 - 6.0 * (aw - d1) / (d2 - d1);
    }


    // ---- gradasi & detail rambut ----
    private static double hash01(double v) { double a = Math.sin(v * 12.9898 + 78.233) * 43758.5453; return a - Math.floor(a); }
    private static double smooth(double a, double b, double x) { double t = clamp((x - a) / (b - a)); return t * t * (3 - 2 * t); }
    private static ShadeFn constShade(final double v) { return (p, n, i, j, nu, nv) -> v; }
    /** goresan helai di permukaan cangkang pada sudut w, tinggi y (-1..1): melengkung ke bawah dari pusaran di puncak. */
    private static double streak(double w, double y) {
        double a = Math.sin(w * 24 + 2.6 * Math.sin(w * 5 + y * 0.35) + y * 0.22);
        double b = Math.sin(w * 37 - y * 0.5 + 1.3);
        double c = hash01(Math.floor((w + PI) * 24 / (2 * PI) * 2.0) * 1.7) - 0.5; // nada beda tiap kelompok helai
        return 0.55 * a + 0.25 * b + 0.5 * c;
    }
    /** Pengali cangkang: akar gelap di bawah -> terang di puncak, cincin sorot, goresan helai (juga menggoyang normal agar kilap bergaris). */
    private static double shellShade(double[] p, double[] n, double yLow, double ySpan) {
        double dx = p[0] - HC[0], dz = p[2] - HC[2], y = p[1];
        double w = Math.atan2(dx, dz);
        double u = clamp((y - yLow) / ySpan);
        double m = 0.62 + 0.30 * Math.pow(u, 0.85);
        double face = Math.max(0, Math.cos(w + 0.6));                  // sisi menghadap cahaya (kiri-depan)
        double yc = 77.0 + 1.3 * Math.cos(w + 0.6);
        m += 0.30 * Math.pow(face, 1.4) * Math.exp(-sq((y - yc) / 2.3)); // cincin sorot (angel ring)
        m += 0.07 * Math.exp(-sq((y - 76.0) / 2.6)) * Math.max(0, -Math.cos(w + 0.6)); // pantulan lembut sisi lawan
        double fade = 1.0 - smooth(77.0, 80.5, y);                     // di puncak kepala goresan memudar (hindari pola sunburst)
        double st = streak(w, y) * fade;
        m *= 1.0 + 0.10 * st;
        double k = 0.20 * st;                                          // goyang normal sepanjang busur
        double tx = Math.cos(w), tz = -Math.sin(w);
        n[0] += tx * k; n[2] += tz * k;
        double[] q = unit(n); n[0] = q[0]; n[1] = q[1]; n[2] = q[2];
        return m;
    }
    private static final ShadeFn SHELL = (p, n, i, j, nu, nv) -> shellShade(p, n, 64.0, 18.0);
    private static final ShadeFn CREWSHELL = (p, n, i, j, nu, nv) -> shellShade(p, n, 66.0, 16.0) * 0.97;
    /** helai/duri: akar gelap -> ujung terang; j = urutan sepanjang helai. */
    private final ShadeFn STRAND = (p, n, i, j, nu, nv) -> {
        double t = nv > 1 ? (double) j / (nv - 1) : 0;
        return tone * (0.68 + 0.50 * Math.pow(t, 0.8));
    };
    /** untaian rambut yang menempel di permukaan cangkang (bagian P_HAIR_DET): memberi volume berlapis. */
    private void lock(double w0, double y0, double yEnd, double r0, double drift, double toneV, int pts) { lock(w0, y0, yEnd, r0, drift, toneV, pts, 0.42); }
    private void lock(double w0, double y0, double yEnd, double r0, double drift, double toneV, int pts, double root) {
        double[][] path = new double[pts][], rad = new double[pts][];
        for (int k = 0; k < pts; k++) {
            double t = (double) k / (pts - 1);
            double y = Math.min(81.4, y0 + (yEnd - y0) * Math.pow(t, 0.85));
            double w = w0 + drift * Math.pow(t, 1.4);
            double[] b = onHair(w, y), o = outward(b);
            double env = Math.min(1.0, root + t * 5.0) * (1.0 - 0.62 * Math.pow(t, 2.4));  // akar tipis, tengah gemuk, ujung runcing
            double r = Math.max(0.12, r0 * env);
            double off = 0.18 + 0.30 * r;                                  // sebagian terbenam di cangkang
            path[k] = new double[]{b[0] + o[0] * off, b[1] + o[1] * off, b[2] + o[2] * off};
            rad[k] = new double[]{r, r};
        }
        tone = toneV;
        tube(path, rad, HAIR, 6);
    }
    /** untaian pusaran: berangkat dari titik pusat bidang atas (rata), melewati tepi bidang atas, lalu turun mengikuti cangkang. */
    private void crownLock(double w0, double yEnd, double r0, double drift, double toneV) {
        int pts = 9;
        double[][] path = new double[pts][], rad = new double[pts][];
        double[] top = onHair(w0 + 0.15 * drift, 81.3);
        for (int k = 0; k < pts; k++) {
            double t = (double) k / (pts - 1);
            double r = Math.max(0.12, r0 * Math.min(1.0, 0.8 + t * 4.0) * (1.0 - 0.6 * Math.pow(t, 2.4)));
            if (k == 0) path[k] = new double[]{HC[0] + 0.8, 82.1, HC[2] - 0.8};
            else if (k == 1) path[k] = new double[]{HC[0] + 0.8 + (top[0] - HC[0] - 0.8) * 0.55, 82.3, HC[2] - 0.8 + (top[2] - HC[2] + 0.8) * 0.55};
            else {
                double u = (double) (k - 2) / (pts - 3);
                double y = Math.min(81.3, 81.3 + (yEnd - 81.3) * Math.pow(u, 0.9));
                double w = w0 + drift * (0.15 + 0.85 * Math.pow(u, 1.3));
                double[] b = onHair(w, y), o = outward(b);
                double off = 0.18 + 0.30 * r + (k == 2 ? 0.5 : 0);
                path[k] = new double[]{b[0] + o[0] * off, b[1] + o[1] * off, b[2] + o[2] * off};
            }
            rad[k] = new double[]{r, r};
        }
        tone = toneV;
        tube(path, rad, HAIR, 6);
    }
    private void detail() {
        cur = P_HAIR_DET;
        curShade = (p, n, i, j, nu, nv) -> {
            double t = nv > 1 ? (double) j / (nv - 1) : 0;
            double base = 0.72 + 0.30 * Math.pow(t, 0.7);                 // akar lebih gelap, ujung lebih terang
            double side = n[0] * -0.45 + n[1] * 0.65 + n[2] * 0.62;      // sisi menghadap cahaya sedikit lebih terang
            return tone * base * (1.0 + 0.10 * Math.max(0, side));
        };
        java.util.Random r = new java.util.Random(23);
        int N = 76;
        for (int k = 0; k < N; k++) {
            double w0 = -PI + 2 * PI * (k + r.nextDouble() * 0.7) / N;
            double aw = Math.abs(w0);
            // tepi bawah di sudut ini (garis rambut di depan, tepi cambang di samping/belakang) + sedikit di atasnya
            double edge = aw < HAIR_W ? hairlineY(w0) : lowerY(aw);
            double yEnd = edge + 0.5 + r.nextDouble() * 1.4;
            double y0 = 76.0 + r.nextDouble() * 2.8;
            double drift = (w0 < 0 ? -1 : 1) * (0.10 + 0.30 * r.nextDouble());       // disisir menjauhi pusaran
            double toneV = (k % 3 == 0) ? 1.10 : (k % 3 == 1 ? 0.90 : 1.0) + (r.nextDouble() - 0.5) * 0.08;
            lock(w0, y0, yEnd, 1.05 + 0.40 * r.nextDouble(), drift, toneV, 9);
        }
        cur = P_HAIR_CROWN;
        // pusaran di puncak: untaian keluar dari satu titik, melengkung searah jarum jam, menutup bidang atas kepala
        for (int k = 0; k < 30; k++) {
            double w0 = 2 * PI * (k + r.nextDouble() * 0.6) / 30;
            double yEnd = 74.5 + r.nextDouble() * 3.5;
            double tn = (k % 3 == 0) ? 1.08 : (k % 3 == 1 ? 0.9 : 1.0);
            crownLock(w0, yEnd, 1.5 + 0.5 * r.nextDouble(), 0.75, tn);
        }
        cur = P_HAIR_DET;
        // helai pendek berlapis lebih rendah: menutup celah dan memberi kesan tebal di sisi dan belakang
        for (int k = 0; k < 28; k++) {
            double w0 = -PI + 2 * PI * (k + 0.35 + r.nextDouble() * 0.3) / 28;
            double aw = Math.abs(w0);
            double edge = aw < HAIR_W ? hairlineY(w0) : lowerY(aw);
            double ys = Math.min(78.0, edge + 7.5 + r.nextDouble() * 2.0);
            lock(w0, ys, edge + 0.4, 0.95 + 0.3 * r.nextDouble(), (k % 2 == 0 ? 1 : -1) * 0.12, 0.84 + 0.3 * r.nextDouble(), 7);
        }
        curShade = null;
        cur = P_OTHER;
    }

    /**
     * Rambut yang masih terlihat di bawah peci (tepi peci y=75): poni tipis di dahi, rambut samping dan belakang.
     * Bagian P_HAIR_LOW hanya digambar saat peci dipakai; semua untaian dimulai di bawah tepi peci.
     */
    private void lowHair() {
        cur = P_HAIR_LOW;
        curShade = (p, n, i, j, nu, nv) -> {
            double t = nv > 1 ? (double) j / (nv - 1) : 0;
            double base = 0.70 + 0.32 * Math.pow(t, 0.7);
            double side = n[0] * -0.45 + n[1] * 0.65 + n[2] * 0.62;
            return tone * base * (1.0 + 0.10 * Math.max(0, side));
        };
        java.util.Random r = new java.util.Random(57);
        int N = 84;
        for (int k = 0; k < N; k++) {
            double w0 = -PI + 2 * PI * (k + r.nextDouble() * 0.7) / N;
            double aw = Math.abs(w0);
            boolean front = aw < HAIR_W;
            double edge = front ? hairlineY(w0) : lowerY(aw);
            double y0 = 75.4 + r.nextDouble() * 0.4;                // berangkat dari bibir peci (y=75), tidak ada jeda
            double yEnd = edge + (front ? -0.2 : 0.4) + r.nextDouble() * (front ? 0.5 : 1.2);
            double drift = (w0 < 0 ? -1 : 1) * (0.05 + 0.16 * r.nextDouble());
            double toneV = (k % 3 == 0) ? 1.10 : (k % 3 == 1 ? 0.90 : 1.0) + (r.nextDouble() - 0.5) * 0.08;
            lock(w0, y0, yEnd, 0.85 + 0.35 * r.nextDouble(), drift, toneV, 6, 0.55);
        }
        // lapis kedua, lebih pendek, sedikit bergeser: memberi kesan tebal di sisi dan belakang
        for (int k = 0; k < 40; k++) {
            double w0 = -PI + 2 * PI * (k + 0.4 + r.nextDouble() * 0.3) / 40;
            double aw = Math.abs(w0);
            if (aw < HAIR_W) continue;
            double edge = lowerY(aw);
            lock(w0, 75.0, edge + 0.3, 0.8 + 0.3 * r.nextDouble(), (k % 2 == 0 ? 1 : -1) * 0.10, 0.84 + 0.3 * r.nextDouble(), 6, 0.55);
        }
        curShade = null;
        cur = P_OTHER;
    }

    // ---- gaya Belah Samping (acuan: lembar gaya rambut SD, no. 2) ----
    private void sideStyle() {
        cur = P_HAIR_SIDE;
        curShade = (p, n, i, j, nu, nv) -> {
            double t = nv > 1 ? (double) j / (nv - 1) : 0;
            double base = 0.70 + 0.34 * Math.pow(t, 0.75);
            double side = n[0] * -0.45 + n[1] * 0.65 + n[2] * 0.62;
            return tone * base * (1.0 + 0.12 * Math.max(0, side));
        };
        java.util.Random r = new java.util.Random(31);
        final double partW = 0.34;                        // garis belah sedikit di sebelah kanan tengah
        // jambul besar disisir dari garis belah ke sisi kiri (-x): menyapu dahi dan turun di pelipis
        int nb = 34;
        for (int k = 0; k < nb; k++) {
            double u = (k + r.nextDouble() * 0.6) / nb;          // 0 = depan, 1 = belakang di sepanjang garis belah
            double y0 = 75.3 + 6.3 * Math.pow(Math.min(1.0, u * 1.25), 0.8);
            double w0 = partW * (1 - 0.4 * u) + (u > 0.55 ? (u - 0.55) * 1.6 : 0);   // garis belah melengkung ke belakang
            double dw = -(0.85 + 1.15 * r.nextDouble() + 0.9 * u);
            double wEnd = w0 + dw;
            double aw = Math.abs(wEnd);
            double edge = aw < HAIR_W ? hairlineY(wEnd) : lowerY(aw);
            double yEnd = edge + 0.3 + r.nextDouble() * 1.8 + (u > 0.6 ? 2.0 : 0);
            double toneV = (k % 3 == 0) ? 1.10 : (k % 3 == 1 ? 0.90 : 1.0) + (r.nextDouble() - 0.5) * 0.08;
            lock(w0, y0, yEnd, 1.25 + 0.5 * r.nextDouble(), dw, toneV, 9, 0.5);
        }
        // sisi kecil (kanan, +x): pendek, disisir sedikit ke kanan
        for (int k = 0; k < 12; k++) {
            double u = (k + r.nextDouble() * 0.6) / 12;
            double y0 = 75.8 + 5.2 * Math.pow(Math.min(1.0, u * 1.2), 0.8);
            double w0 = partW * (1 - 0.4 * u) + (u > 0.55 ? (u - 0.55) * 1.6 : 0);
            double dw = 0.35 + 0.55 * r.nextDouble();
            double wEnd = w0 + dw;
            double aw = Math.abs(wEnd);
            double edge = aw < HAIR_W ? hairlineY(wEnd) : lowerY(aw);
            lock(w0, y0, edge + 0.6 + r.nextDouble() * 1.4 + (u > 0.6 ? 2.0 : 0), 1.1 + 0.3 * r.nextDouble(), dw,
                    (k % 2 == 0) ? 1.05 : 0.92, 8, 0.5);
        }
        // penutup puncak: untaian dari tengah bidang atas disisir ke kiri (belahan di kanan tengah)
        for (int k = 0; k < 16; k++) {
            double w0 = -0.15 - 2.9 * (k + r.nextDouble() * 0.6) / 16;
            crownLock(w0, 76.5 + r.nextDouble() * 3.0, 1.35 + 0.35 * r.nextDouble(), -0.5, (k % 3 == 0) ? 1.08 : (k % 3 == 1 ? 0.92 : 1.0));
        }
        for (int k = 0; k < 5; k++) {
            double w0 = 0.35 + 1.0 * (k + r.nextDouble() * 0.6) / 5;
            crownLock(w0, 77.5 + r.nextDouble() * 2.0, 1.2, 0.3, 1.0);
        }
        // garis belah: alur gelap tipis dari dahi ke belakang
        curShade = constShade(0.38);
        int np = 9;
        double[][] path = new double[np][], rad = new double[np][];
        for (int k = 0; k < np; k++) {
            double u = (double) k / (np - 1);
            double y = 75.3 + 6.3 * Math.pow(Math.min(1.0, u * 1.25), 0.8);
            double w = partW * (1 - 0.4 * u) + (u > 0.55 ? (u - 0.55) * 1.6 : 0);
            double[] b = onHair(w, Math.min(y, 81.2)), o = outward(b);
            path[k] = new double[]{b[0] + o[0] * 0.55, b[1] + o[1] * 0.55, b[2] + o[2] * 0.55};
            rad[k] = new double[]{0.42, 0.42};
        }
        tone = 1.0;
        tube(path, rad, HAIR, 6);
        double[][] tp = {{1.1, 82.25, HC[2] + 4.6}, {1.0, 82.5, HC[2] + 1.5}, {0.9, 82.5, HC[2] - 1.5}, {1.0, 82.25, HC[2] - 4.6}};
        double[][] tr = {{0.4, 0.4}, {0.4, 0.4}, {0.4, 0.4}, {0.4, 0.4}};
        tube(tp, tr, HAIR, 6);
        curShade = null;
        cur = P_OTHER;
    }

    // ---- gaya Ikal (acuan no. 3): gumpalan ikal kecil menutupi kepala ----
    private void curl(double w, double y, double rho, double toneV, int pts) {
        double[] b = onHair(w, y), o = outward(b);
        double[] e1 = norm3(o[2], 0, -o[0]);                 // tangen mendatar
        double[] e2 = new double[]{o[1] * e1[2] - o[2] * e1[1], o[2] * e1[0] - o[0] * e1[2], o[0] * e1[1] - o[1] * e1[0]};
        double ph = 6.283 * hash01(w * 7.3 + y * 1.9);
        double[][] path = new double[pts][], rad = new double[pts][];
        for (int k = 0; k < pts; k++) {
            double t = (double) k / (pts - 1);
            double th = ph + t * 2 * PI * 1.35;
            double rr = rho * (1.0 - 0.45 * t);
            double lift = 0.15 + 0.85 * Math.sin(PI * Math.min(1.0, t * 1.1)) + 0.6 * t;
            double c = Math.cos(th) * rr, sn = Math.sin(th) * rr;
            path[k] = new double[]{b[0] + o[0] * lift + e1[0] * c + e2[0] * sn,
                    b[1] + o[1] * lift + e1[1] * c + e2[1] * sn,
                    b[2] + o[2] * lift + e1[2] * c + e2[2] * sn};
            double rd = 0.95 * (1.0 - 0.45 * t * t);
            rad[k] = new double[]{rd, rd};
        }
        tone = toneV;
        tube(path, rad, HAIR, 5);
    }
    private void curlyStyle() {
        java.util.Random r = new java.util.Random(77);
        ShadeFn sf = (p, n, i, j, nu, nv) -> {
            double t = nv > 1 ? (double) j / (nv - 1) : 0;
            double base = 0.66 + 0.40 * Math.pow(t, 0.9);       // pangkal gelap, ujung ikal terang
            double side = n[0] * -0.45 + n[1] * 0.65 + n[2] * 0.62;
            return tone * base * (1.0 + 0.14 * Math.max(0, side));
        };
        final int COLS = 28;
        for (int row = 0; row < 10; row++) {
            double y = 63.5 + row * 2.3;
            if (y > 81.0) y = 81.0;
            for (int c = 0; c < COLS; c++) {
                double w = -PI + 2 * PI * (c + 0.5 * (row % 2)) / COLS + (r.nextDouble() - 0.5) * 0.08;
                double aw = Math.abs(w);
                double edge = aw < HAIR_W ? hairlineY(w) : lowerY(aw);
                if (y < edge + (aw < HAIR_W ? 0.2 : 0.9)) continue;   // depan: ikal sampai garis rambut agar tidak ada jeda dengan peci
                double yy = y + (r.nextDouble() - 0.5) * 0.8;
                if (yy > 81.4) continue;
                cur = yy <= 75.6 ? P_CURL_LOW : P_HAIR_CURL;   // sampai bibir peci: terlihat juga saat peci dipakai
                curShade = sf;
                curl(w, yy, 1.0 + 0.3 * r.nextDouble(), 0.86 + 0.28 * r.nextDouble(), 10);
            }
        }
        // puncak kepala: beberapa ikal di bidang atas
        cur = P_HAIR_CURL;
        curShade = sf;
        for (int ring = 0; ring < 2; ring++) {
            int nr = ring == 0 ? 6 : 12;
            double rr = ring == 0 ? 2.6 : 5.6;
            for (int k = 0; k < nr; k++) {
                double a = 2 * PI * (k + 0.5 * ring) / nr;
                double[] top = {HC[0] + rr * Math.sin(a), 82.0 - 0.3 * ring, HC[2] + rr * Math.cos(a)};
                curlFlat(top, 1.1 + 0.25 * r.nextDouble(), 0.9 + 0.2 * r.nextDouble());
            }
        }
        curlFlat(new double[]{HC[0], 82.2, HC[2]}, 1.3, 1.0);
        curShade = null;
        cur = P_OTHER;
    }
    /** ikal di bidang atas yang datar: lingkaran kecil mendatar yang naik sedikit. */
    private void curlFlat(double[] c0, double rho, double toneV) {
        int pts = 12;
        double[][] path = new double[pts][], rad = new double[pts][];
        double ph = 6.283 * hash01(c0[0] * 5.1 + c0[2] * 3.3);
        for (int k = 0; k < pts; k++) {
            double t = (double) k / (pts - 1);
            double th = ph + t * 2 * PI * 1.35;
            double rr = rho * (1.0 - 0.45 * t);
            path[k] = new double[]{c0[0] + Math.cos(th) * rr, c0[1] + 0.2 + 0.9 * Math.sin(PI * Math.min(1.0, t * 1.1)) + 0.5 * t, c0[2] + Math.sin(th) * rr};
            double rd = 0.95 * (1.0 - 0.45 * t * t);
            rad[k] = new double[]{rd, rd};
        }
        tone = toneV;
        tube(path, rad, HAIR, 5);
    }

    private void hairStyles() {
        // 1. Crew cut: pendek rapi, tepi lebih tinggi (telinga dan pelipis terbuka); menggantikan cangkang bawaan
        cur = P_HAIR_CREW;
        curShade = CREWSHELL;
        double th0 = Math.asin((64.5 - HC[1]) / HH[1]);
        skull(HC, HH, HAIR, 96, 44, th0, (w, y) -> {
            double aw = Math.abs(w);
            return aw < HAIR_W ? y < crewLine(w) : y < crewLower(aw);
        });
        curShade = constShade(0.86);
        double[][] fr = new double[17][], frr = new double[17][];
        for (int i = 0; i < 17; i++) { double w = -HAIR_W + 2 * HAIR_W * i / 16; fr[i] = onHair(w, crewLine(w)); frr[i] = new double[]{0.5, 0.5}; }
        tube(fr, frr, HAIR, 8);
        for (int sg = -1; sg <= 1; sg += 2) {
            int n = 16;
            double[][] sb = new double[n][], sbr = new double[n][];
            for (int i = 0; i < n; i++) {
                double w = HAIR_W + (PI - HAIR_W) * i / (n - 1.0);
                sb[i] = onHair(sg * w, crewLower(w)); sbr[i] = new double[]{0.5, 0.5};
            }
            tube(sb, sbr, HAIR, 8);
        }

        // cepak: helai pendek halus yang mengikuti pusaran di puncak, supaya tidak terlihat seperti helm polos
        curShade = (p, n, i, j, nu, nv) -> {
            double t = nv > 1 ? (double) j / (nv - 1) : 0;
            double lit = n[0] * -0.45 + n[1] * 0.65 + n[2] * 0.62;
            return tone * (0.80 + 0.24 * Math.pow(t, 0.8)) * (1.0 + 0.08 * Math.max(0, lit));
        };
        java.util.Random cr = new java.util.Random(91);
        int NC = 120;
        for (int k = 0; k < NC; k++) {
            double w0 = -PI + 2 * PI * (k + cr.nextDouble() * 0.7) / NC;
            double aw = Math.abs(w0);
            double edge = aw < HAIR_W ? crewLine(w0) : crewLower(aw);
            double y0 = Math.min(80.5, edge + 4.0 + cr.nextDouble() * 5.0);
            double yEnd = edge + 0.2 + cr.nextDouble() * 0.9;
            double drift = (w0 < 0 ? -1 : 1) * (0.06 + 0.16 * cr.nextDouble());
            lock(w0, y0, yEnd, 0.62 + 0.18 * cr.nextDouble(), drift, 0.92 + 0.16 * cr.nextDouble(), 6, 0.6);
        }
        for (int k = 0; k < 18; k++) {
            double w0 = 2 * PI * (k + cr.nextDouble() * 0.6) / 18;
            crownLock(w0, 77.5 + cr.nextDouble() * 2.0, 0.9 + 0.2 * cr.nextDouble(), 0.75, (k % 3 == 0) ? 1.06 : (k % 3 == 1 ? 0.92 : 1.0));
        }
        curShade = null;
        cur = P_OTHER;
    }
    private static double clamp(double a) { return a < 0 ? 0 : (a > 1 ? 1 : a); }
    private static double tz(double x, double y) { double[] q = interp(TORSO, y); return q[1] * Math.sqrt(Math.max(0, 1 - sq(x / q[0]))); }

    /** Skala non-seragam untuk semua bagian yang dibuat sejak indeks 'from' (kepala), dengan pivot (px,py,pz). */
    private void scaleFrom(int from, double sx, double sy, double sz, double px, double py, double pz) {
        for (int k = from; k < bp.size(); k++) {
            float[] p = bp.get(k), n = bn.get(k);
            for (int i = 0; i < p.length; i += 3) {
                p[i] = (float) (px + (p[i] - px) * sx);
                p[i + 1] = (float) (py + (p[i + 1] - py) * sy);
                p[i + 2] = (float) (pz + (p[i + 2] - pz) * sz);
                double a = n[i] / sx, b = n[i + 1] / sy, c = n[i + 2] / sz;
                double l = Math.sqrt(a * a + b * b + c * c);
                if (l < 1e-9) l = 1;
                n[i] = (float) (a / l); n[i + 1] = (float) (b / l); n[i + 2] = (float) (c / l);
            }
        }
    }

    /** Pemendekan kaki: y<=29 diskala 0,86; di atas 29 digeser turun 4,06 (proporsi seperti lembar desain). */
    private void shortenLegs() {
        final double K = 0.86, Y0 = 29.0, SH = Y0 * (1 - K);
        for (int k = 0; k < bp.size(); k++) {
            float[] p = bp.get(k), n = bn.get(k);
            for (int i = 0; i < p.length; i += 3) {
                double y = p[i + 1];
                if (y <= Y0) {
                    p[i + 1] = (float) (y * K);
                    double a = n[i], b = n[i + 1] / K, c = n[i + 2];
                    double l = Math.sqrt(a * a + b * b + c * c); if (l < 1e-9) l = 1;
                    n[i] = (float) (a / l); n[i + 1] = (float) (b / l); n[i + 2] = (float) (c / l);
                } else p[i + 1] = (float) (y - SH);
            }
        }
    }

    private void finish() {
        shortenLegs();
        int nv = 0, ni = 0;
        for (float[] a : bp) nv += a.length / 3;
        for (int[] a : bi) ni += a.length;
        pos = new float[nv * 3]; nor = new float[nv * 3]; col = new float[nv * 3]; idx = new int[ni];
        part = new byte[nv]; shade = new float[nv];
        int pv = 0, pi = 0;
        for (int k = 0; k < bp.size(); k++) {
            java.util.Arrays.fill(part, pv / 3, (pv + bp.get(k).length) / 3, (byte) (int) bpt.get(k));
            System.arraycopy(bs.get(k), 0, shade, pv / 3, bs.get(k).length);
            System.arraycopy(bp.get(k), 0, pos, pv, bp.get(k).length);
            System.arraycopy(bn.get(k), 0, nor, pv, bn.get(k).length);
            System.arraycopy(bc.get(k), 0, col, pv, bc.get(k).length);
            pv += bp.get(k).length;
            System.arraycopy(bi.get(k), 0, idx, pi, bi.get(k).length);
            pi += bi.get(k).length;
        }
        vertCount = nv; triCount = ni / 3;
    }


    // ---- pakaian / rambut / peci per tampilan (mesh bersama TIDAK diubah) ----
    /** "#RRGGBB" -> {r,g,b} 0..1, atau null bila bukan warna sah. */
    static double[] hex(String h) {
        if (h == null) return null;
        h = h.trim();
        if (h.length() != 7 || h.charAt(0) != '#') return null;
        try {
            int v = Integer.parseInt(h.substring(1), 16);
            return new double[]{((v >> 16) & 255) / 255.0, ((v >> 8) & 255) / 255.0, (v & 255) / 255.0};
        } catch (NumberFormatException e) { return null; }
    }
    private static double[] mix(double[] a, double[] b, double t) {
        return new double[]{a[0] + (b[0] - a[0]) * t, a[1] + (b[1] - a[1]) * t, a[2] + (b[2] - a[2]) * t};
    }
    /**
     * Salinan warna titik dengan pakaian dan rambut dipasang. pakaian = "badan,lengan[,celana]" (null = bawaan),
     * rambut = "#hex" (null = bawaan). Mengembalikan array BARU, jadi warna satu pemain tidak bocor ke pemain lain.
     */
    float[] outfitColors(String pakaian, String rambut) {
        float[] c = col.clone();
        double[] body = null, arm = null, pants = null;
        if (pakaian != null) {
            String[] p = pakaian.split(",");
            if (p.length > 0) body = hex(p[0]);
            if (p.length > 1) arm = hex(p[1]);
            if (p.length > 2) pants = hex(p[2]);
            if (arm == null) arm = body;
        }
        double[] hair = hex(rambut);
        double[] trim = body == null ? null : mix(body, new double[]{1, 1, 1}, 0.2);
        double[] btn = body == null ? null : mix(body, new double[]{0, 0, 0}, 0.2);
        for (int i = 0; i < vertCount; i++) {
            double[] k = null;
            switch (part[i]) {
                case P_SHIRT: k = body; break;
                case P_TRIM: k = trim; break;
                case P_BUTTON: k = btn; break;
                case P_SLEEVE: k = arm; break;
                case P_PANTS: k = pants; break;
                case P_HAIR: case P_HAIR_CREW: case P_HAIR_SPIKY: case P_HAIR_MESSY: case P_HAIR_DET: case P_HAIR_LOW: case P_HAIR_SIDE: case P_HAIR_CURL: case P_HAIR_CROWN: case P_CURL_LOW: {
                    // gradasi: warna dasar (pilihan pemain, atau bawaan) dikali pengali terang/gelap per titik
                    double[] bc0 = hair != null ? hair : new double[]{c[i * 3], c[i * 3 + 1], c[i * 3 + 2]};
                    double m = shade[i], sheen = Math.max(0, m - 1) * 0.30;
                    k = new double[]{Math.min(1, bc0[0] * m + sheen), Math.min(1, bc0[1] * m + sheen * 0.95), Math.min(1, bc0[2] * m + sheen * 0.85)};
                    break;
                }
                default: break;
            }
            if (k != null) { c[i * 3] = (float) k[0]; c[i * 3 + 1] = (float) k[1]; c[i * 3 + 2] = (float) k[2]; }
        }
        return c;
    }
    /** Indeks segitiga untuk satu tampilan (tanpa gaya rambut tambahan). */
    int[] indexFor(boolean showPeci) { return indexFor(showPeci, STYLE_DEFAULT); }
    /**
     * Indeks segitiga untuk satu tampilan. showPeci=false membuang peci; gaya rambut (STYLE_*) hanya digambar bila
     * peci dilepas, sebab rambut tertutup peci. Gaya crew cut menggantikan cangkang rambut bawaan.
     */
    int[] indexFor(boolean showPeci, int style) {
        boolean[] hide = new boolean[20];
        boolean st = !showPeci;
        hide[P_PECI] = !showPeci;
        hide[P_HAIR_SPIKY] = true;                          // gaya jabrik sudah tidak dipakai (tidak cocok untuk sekolah)
        hide[P_HAIR_CREW] = !(st && style == STYLE_CREW);
        hide[P_HAIR_MESSY] = !(st && style == STYLE_MESSY);
        boolean curly = style == STYLE_CURLY, side = style == STYLE_SIDE;
        // untaian volume: tidak dipakai crew cut (pendek rapi) dan ikal (diganti ikal sendiri)
        hide[P_HAIR_DET] = !(st && style != STYLE_CREW && !curly);
        hide[P_HAIR_CROWN] = !(st && (style == STYLE_DEFAULT || style == STYLE_MESSY));   // pusaran puncak: bukan untuk belah samping
        hide[P_HAIR_LOW] = !showPeci || curly;               // untaian di bawah peci (peci dipakai); ikal punya sendiri
        hide[P_HAIR_SIDE] = !(side && st);
        hide[P_HAIR_CURL] = !(curly && st);
        hide[P_CURL_LOW] = !curly;                           // ikal bagian bawah: terlihat juga di bawah peci
        if (st && style == STYLE_CREW) hide[P_HAIR] = true;
        if (style == STYLE_HOOD) {                           // tudung hewan: tanpa peci dan tanpa rambut
            hide[P_PECI] = true;
            for (int q = P_HAIR; q <= P_CURL_LOW; q++) if (q != P_PECI) hide[q] = true;
        }
        int n = 0;
        for (int t = 0; t < idx.length; t += 3) if (!hide[part[idx[t]]]) n += 3;
        int[] r = new int[n];
        int o = 0;
        for (int t = 0; t < idx.length; t += 3)
            if (!hide[part[idx[t]]]) { r[o++] = idx[t]; r[o++] = idx[t + 1]; r[o++] = idx[t + 2]; }
        return r;
    }

    /** Uji mandiri: java PutraMesh.java -> cetak jumlah dan checksum. */
    public static void main(String[] a) {
        PutraMesh m = build();
        double sx = 0, sy = 0, sz = 0;
        for (int i = 0; i < m.vertCount; i++) { sx += m.pos[i * 3]; sy += m.pos[i * 3 + 1]; sz += m.pos[i * 3 + 2]; }
        System.out.println("verts " + m.vertCount + " tris " + m.triCount);
        System.out.printf("sum %.3f %.3f %.3f%n", sx, sy, sz);
    }
}
