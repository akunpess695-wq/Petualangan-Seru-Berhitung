package com.petualangan.berhitung;

import java.util.ArrayList;

/**
 * Geometri putri untuk renderer OpenGL ES (Tahap E). Tanpa dependensi Android,
 * port dari tahap-E/geom.py + putri.py. Sumbu: y atas, depan +z, kaki di y=0.
 * Hasil: pos/nor/col (3 float per titik) dan idx (3 indeks per segitiga).
 */
final class PutriMesh {
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

    // ---- penanda bagian (lihat PutraMesh): baju, lengan, rok, jilbab ----
    static final int P_OTHER = 0, P_SHIRT = 1, P_SLEEVE = 2, P_SKIRT = 3, P_HIJAB = 4;
    private int cur = P_OTHER;
    private final ArrayList<Integer> bpt = new ArrayList<>();
    byte[] part;

    static final double PI = Math.PI;
    static final double[] WHITE = {0.93, 0.93, 0.98}, SKIN = {1.0, 0.847, 0.694}, MAR = {0.68, 0.07, 0.15}, MARD = {0.55, 0.04, 0.11}, CUFF = {0.90, 0.90, 0.97}, MARUN = {0.64, 0.07, 0.17}, PIPE = {0.82, 0.82, 0.90}, BTN = {0.97, 0.97, 1.0}, CRIM = {0.80, 0.09, 0.20}, CRIMD = {0.66, 0.06, 0.16}, MAUVE = {0.60, 0.17, 0.33}, GOLD = {0.86, 0.69, 0.36},
            BAG = {0.75, 0.14, 0.16}, SHOE = {0.11, 0.11, 0.12}, BROWN = {0.31, 0.19, 0.14},
            PINK = {1.0, 0.72, 0.70}, HIJ = {0.92, 0.92, 0.98};
    static final double[] HC = {0, 68.5, 0.8}, HR = {12.6, 12.9, 12.5};
    // tabel: {y, rx, rz, cx, cz}
    static final double[][] TORSO = {{57, 6.0, 5.5, 0, 0}, {52.5, 10.6, 8.2, 0, 0}, {49, 11.6, 8.9, 0, -0.2}, {40, 11.3, 8.8, 0, -0.2}, {31, 10.9, 8.6, 0, -0.2}, {28.0, 11.3, 8.95, 0, -0.2}}; // ujung baju dikeluarkan, sedikit melebar menutup tepi atas rok
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

    static double onHead(double x, double y, double off) {
        double u = sq(x / HR[0]) + sq((y - HC[1]) / HR[1]);
        return HC[2] + HR[2] * Math.sqrt(Math.max(0, 1 - u)) + off;
    }
    static double sq(double a) { return a * a; }

    /** Tinggi tepi bawah jilbab pada sudut ph (0 = depan): ujung V depan sampai dada, tepi turun di bahu, segitiga di belakang. */
    static double hemY(double ph) {
        final double d0 = Math.toRadians(76), d1 = Math.toRadians(110);
        double d = Math.abs(((ph + PI) % (2 * PI)) - PI);
        if (d < d0) return 54.4 - 14.4 * (1 - d / d0);
        if (d < d1) { double t = (d - d0) / (d1 - d0); t = t * t * (3 - 2 * t); return 54.4 - 7.4 * t; }
        return 41.0 + 6.0 * Math.sin(PI - d) / Math.sin(PI - d1);
    }
    /** Stiker bulat di dada: mengikuti lengkung badan, setebal 'thick' di atas permukaan + off. */
    void torsoDecal(double cx, double cy, double rx, double ry, double thick, double off, double[] colr, int nu, int nv) {
        ellipsoid(new double[]{cx, cy, 0}, new double[]{rx, ry, thick}, colr, nu, nv, -PI / 2, PI / 2, null, P -> {
            for (double[][] row : P)
                for (double[] v : row) v[2] = tz(v[0], v[1]) + off + Math.max(v[2], 0);
        });
    }

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
        float[] fp = new float[nu * nv * 3], fn = new float[nu * nv * 3], fc = new float[nu * nv * 3];
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
        bp.add(fp); bn.add(fn); bc.add(fc); bi.add(fi); bpt.add(cur);
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

    // ---- rakitan putri ----
    static final double[] EYE = {0.29, 0.15, 0.10}, EYE2 = {0.56, 0.33, 0.20}, PUPIL = {0.10, 0.05, 0.04}, LASH = {0.13, 0.07, 0.05}, LIP = {0.88, 0.31, 0.31};

    static PutriMesh build() {
        final PutriMesh M = new PutriMesh();
        // kaki + sepatu (sepatu panjang, hampir menyentuh hem rok seperti gambar)
        for (int sg = -1; sg <= 1; sg += 2) {
            double s = sg;
            M.tube(new double[][]{{s * 5.0, 8, 0}, {s * 5.0, 3, 0}}, rr(2, 2.0, 2.0), WHITE, 14);
            M.ellipsoid(new double[]{s * 5.0, 0.5, -0.3}, new double[]{4.3, 4.5, 7.3}, SHOE, 28, 14, 0, PI / 2, null, null);
            M.ellipsoid(new double[]{s * 5.0, 0.5, -0.3}, new double[]{4.3, 0.5, 7.3}, SHOE, 28, 6, -PI / 2, 0, null, null);
        }
        M.cur = P_SKIRT;
        // rok panjang berlipit lembut (18 lipit), tertutup ujung baju di pinggang dan makin lebar ke bawah
        M.loft(SKIRT, MAR, 144, 28, (ph, y) -> {
            double t = (ph * 18 / (2 * PI)) % 1.0;
            double w = clamp((Math.abs(t - 0.5) * 2 - 0.3) / 0.4);
            w = w * w * (3 - 2 * w);
            double amp = y > 27 ? 0.0 : 0.11 * Math.min(1, (27 - y) / 7);
            return 1 + amp * (w - 0.5) * 2;
        }, null);
        // baju
        M.cur = P_SHIRT;
        M.loft(TORSO, WHITE, 48, 16, null, null);
        // plaket (lipatan kancing depan) dan kancing; hanya bagian di bawah ujung jilbab yang terlihat
        M.tube(new double[][]{{0, 45, tz(0, 45) + 0.25}, {0, 38, tz(0, 38) + 0.25}, {0, 29.5, tz(0, 29.5) + 0.25}}, rr(3, 0.55, 0.25), CUFF, 8);
        M.cur = P_OTHER;
        for (double by : new double[]{29.8}) M.ellipsoid(new double[]{0, by, tz(0, by) + 0.45}, new double[]{0.6, 0.6, 0.3}, BTN, 14, 8);
        M.cur = P_SHIRT;
        // lengan: lurus ke bawah, sedikit melebar di manset
        for (int sg = -1; sg <= 1; sg += 2) {
            double s = sg;
            M.cur = P_SLEEVE;
            double[][] path = {{s * 10.8, 48.5, -0.6}, {s * 12.4, 42, -1.2}, {s * 13.8, 36, -1.8}, {s * 15.0, 30.0, -2.0}};
            double[] rd = {3.9, 3.8, 3.7, 3.9};
            double[][] rad = new double[4][];
            for (int i = 0; i < 4; i++) rad[i] = new double[]{rd[i], rd[i]};
            M.tube(path, rad, WHITE, 16);
            M.ellipsoid(new double[]{s * 10.8, 48.5, -0.6}, new double[]{3.9, 3.9, 3.9}, WHITE, 16, 10);
            // manset: cincin sedikit lebih lebar dari lengan, menyempit di ujung, dengan satu kancing di sisi luar
            M.tube(new double[][]{{s * 14.7, 32.5, -1.95}, {s * 15.0, 29.5, -2.0}, {s * 15.0, 28.3, -2.0}}, new double[][]{{4.2, 4.2}, {4.25, 4.25}, {3.0, 3.0}}, CUFF, 16);
            M.cur = P_OTHER;
            M.ellipsoid(new double[]{s * 19.3, 30.8, -2.0}, new double[]{0.35, 0.6, 0.6}, BTN, 10, 6);
            M.ellipsoid(new double[]{s * 15.0, 26.6, -0.4}, new double[]{2.7, 4.0, 2.5}, SKIN, 18, 12);
        }
        M.cur = P_OTHER;
        // tas: badan + saku depan + kancing
        M.superbox(new double[]{0, 38.5, -12.7}, new double[]{10.9, 12.5, 4.1}, BAG, 0.32, 36, 20);
        M.superbox(new double[]{0, 35.3, -17.3}, new double[]{7.6, 5.3, 1.0}, BAG, 0.30, 36, 20);
        M.superbox(new double[]{0, 38.2, -18.4}, new double[]{1.1, 1.0, 0.4}, new double[]{0.62, 0.62, 0.66}, 0.3, 16, 8);
        // tali tas: dari sudut atas tas, lewat bahu, turun di dada
        for (int sg = -1; sg <= 1; sg += 2) {
            double s = sg;
            double[][] pth = {{s * 9.6, 46, -9.4}, {s * 9.9, 51.5, -5.5}, {s * 10.1, 53.2, 0.0}, {s * 9.9, 51.0, tz(9.9, 51) + 0.9},
                    {s * 9.8, 45, tz(9.8, 45) + 0.9}, {s * 9.8, 40, tz(9.8, 40) + 0.9}, {s * 9.8, 36.5, tz(9.8, 36.5) + 0.9}};
            M.tube(pth, rr(7, 1.35, 0.5), BAG, 8);
        }
        // kepala + wajah (mengikuti "Detail Wajah": mata besar berlapis, alis tebal, pipi lembut, senyum tipis)
        M.ellipsoid(HC, HR, SKIN, 48, 28);
        double[][] hl = new double[7][];
        for (int i = 0; i < 7; i++) { double x = -6.8 + 13.6 * i / 6, y = 80.2 - 0.05 * x * x; hl[i] = new double[]{x, y, onHead(x, y, 0.2)}; }
        M.tube(hl, rr(7, 0.5, 0.5), BROWN, 6); // garis rambut tipis di dahi
        for (int sg = -1; sg <= 1; sg += 2) {
            double s = sg, ex = s * 6.3, ey = 66.6;
            M.decal(ex, ey + 0.2, 2.8, 3.65, 0.40, 0.03, new double[]{0.99, 0.96, 0.93}, 24, 14);        // putih mata
            M.decal(ex, ey - 0.1, 2.65, 3.5, 0.15, 0.43, EYE, 24, 14);                            // iris gelap
            M.decal(ex, ey - 1.3, 2.05, 1.7, 0.10, 0.50, EYE2, 20, 10);                             // iris bawah lebih terang
            M.decal(ex, ey, 1.4, 1.95, 0.12, 0.58, PUPIL, 18, 10);                            // pupil
            M.decal(ex - 0.75, ey + 1.4, 0.8, 0.9, 0.10, 0.70, new double[]{1, 1, 1}, 12, 8);     // kilau besar
            M.decal(ex + 0.95, ey - 1.2, 0.42, 0.45, 0.08, 0.70, new double[]{1, 1, 1}, 10, 6);     // kilau kecil
            int nl = 8;
            double[][] lash = new double[nl][], lrad = new double[nl][];
            for (int i = 0; i < nl; i++) {
                double u, r;
                if (i < nl - 1) { u = Math.toRadians(172 - 158.0 * i / (nl - 2)); r = 0.28 + 0.27 * Math.sin(PI * i / (nl - 2)); }
                else { u = Math.toRadians(8); r = 0.18; }
                double x = ex + s * (i < nl - 1 ? 2.85 * Math.cos(u) : 4.0), y = ey + 0.2 + (i < nl - 1 ? 3.65 * Math.sin(u) : 1.5);
                lash[i] = new double[]{x, y, onHead(x, y, 0.4)};
                lrad[i] = new double[]{r, r};
            }
            M.tube(lash, lrad, LASH, 6); // bulu mata atas tebal + ujung melentik
            M.decal(s * 8.2, 61.9, 2.6, 1.6, 0.30, 0.05, PINK, 18, 10); // pipi lembut
            double[][] br = new double[4][], brr = new double[4][];
            double[] bx = {2.4, 4.4, 7.0, 9.8}, by = {72.9, 73.9, 73.6, 72.2}, bw = {0.6, 0.8, 0.75, 0.42};
            for (int i = 0; i < 4; i++) { br[i] = new double[]{s * bx[i], by[i], onHead(s * bx[i], by[i], 0.3)}; brr[i] = new double[]{bw[i], bw[i]}; }
            M.tube(br, brr, BROWN, 8); // alis tebal, puncak di sisi dalam
        }
        double[][] arc = new double[7][];
        for (int i = 0; i < 7; i++) {
            double x = -2.9 + 5.8 * i / 6, y = 60.4 + 0.1 * x * x;
            arc[i] = new double[]{x, y, onHead(x, y, 0.3)};
        }
        M.tube(arc, rr(7, 0.32, 0.3), LIP, 6); // senyum tipis

        // dasi marun: menjuntai dari balik ujung V jilbab, melebar lalu meruncing (hanya bagian bawahnya yang terlihat)
        M.cur = P_OTHER;
        double[] tieY = {44.0, 41.5, 39.0, 36.5, 34.0, 31.8};
        double[] tieW = {0.9, 0.85, 1.3, 1.8, 1.5, 0.35};
        double[][] tp = new double[tieY.length][], tr = new double[tieY.length][];
        for (int i = 0; i < tieY.length; i++) {
            tp[i] = new double[]{0, tieY[i], tz(0, tieY[i]) + 0.55};
            tr[i] = new double[]{tieW[i], 0.3};
        }
        M.tube(tp, tr, MARUN, 10);

        // lambang sekolah di saku dada kiri (kiri pemakai = +x): matahari kuning-oranye, cincin putih dan hijau, bumi biru
        M.cur = P_SHIRT;
        double lx = 5.8, ly = 36.8;
        M.torsoDecal(lx, ly, 3.0, 3.3, 0.05, 0.05, new double[]{0.88, 0.88, 0.95}, 24, 6); // tambalan saku
        M.cur = P_OTHER;
        for (int k = 0; k < 12; k++) {
            double a2 = 2 * PI * k / 12;
            M.torsoDecal(lx + 2.0 * Math.cos(a2), ly + 2.0 * Math.sin(a2), 0.5, 0.5, 0.06, 0.12, new double[]{1.0, 0.82, 0.12}, 10, 6);
        }
        M.torsoDecal(lx, ly, 2.0, 2.0, 0.06, 0.12, new double[]{1.0, 0.60, 0.10}, 24, 8);
        M.torsoDecal(lx, ly, 1.62, 1.62, 0.06, 0.20, new double[]{0.98, 0.98, 0.98}, 24, 8);
        M.torsoDecal(lx, ly, 1.40, 1.40, 0.06, 0.28, new double[]{0.15, 0.52, 0.22}, 24, 8);
        M.torsoDecal(lx, ly, 1.10, 1.10, 0.06, 0.36, new double[]{0.13, 0.40, 0.80}, 24, 8);
        M.torsoDecal(lx + 0.2, ly - 0.3, 0.7, 0.45, 0.05, 0.44, new double[]{0.22, 0.62, 0.27}, 16, 6);

        M.cur = P_HIJAB;
        M.loft(NECK, HIJ, 48, 14, null, null); // penutup leher putih
        // jilbab: satu cangkang putih (kubah -> bahu -> ujung V depan dan segitiga belakang), bukaan wajah dipotong
        final double yc = 68.3, hh = 12.4, pw = Math.toRadians(47);
        M.loft(HIJAB, HIJ, 216, 72, null, ph -> new double[]{89.5, hemY(ph)},
                (ph, y) -> sq(ph / pw) + sq((y - yc) / hh) < 1.0);
        double[][] bigTab = new double[HIJAB.length][];
        for (int i = 0; i < HIJAB.length; i++)
            bigTab[i] = new double[]{HIJAB[i][0], HIJAB[i][1] * 1.012, HIJAB[i][2] * 1.012, HIJAB[i][3], HIJAB[i][4]};
        // list marun di sepanjang tepi bawah (V depan, bahu, segitiga belakang)
        M.loft(bigTab, MARUN, 216, 8, null, ph -> new double[]{hemY(ph) + 3.2, hemY(ph)});
        // list marun di sekeliling wajah: pipa yang menebal di dahi dan pipi, menipis ke arah dagu (kain putih di bawah dagu)
        double[][] rim = new double[73][], rimO = new double[73][], rrad = new double[73][];
        for (int i = 0; i < 72; i++) {
            double t = 2 * PI * i / 72, ph = pw * Math.sin(t), y = yc + hh * Math.cos(t);
            double[] q = interp(HIJAB, y);
            rim[i] = new double[]{q[2] + q[0] * Math.sin(ph), y, q[3] + q[1] * Math.cos(ph)};
            double ph2 = pw * 1.09 * Math.sin(t), y2 = yc + hh * 1.07 * Math.cos(t);
            double[] q2 = interp(HIJAB, y2);
            rimO[i] = new double[]{q2[2] + q2[0] * 1.004 * Math.sin(ph2), y2, q2[3] + q2[1] * 1.004 * Math.cos(ph2)};
            double r = 0.75 + 0.6 * clamp((Math.cos(t) + 0.5) / 1.2);
            rrad[i] = new double[]{r, r};
        }
        rim[72] = rim[0]; rimO[72] = rimO[0]; rrad[72] = rrad[0];
        M.tube(java.util.Arrays.copyOfRange(rimO, 0, 26), java.util.Arrays.copyOfRange(rrad, 0, 26), MARUN, 10);
        M.tube(java.util.Arrays.copyOfRange(rimO, 47, 73), java.util.Arrays.copyOfRange(rrad, 47, 73), MARUN, 10);
        double[][] bot = java.util.Arrays.copyOfRange(rim, 25, 48);
        M.tube(bot, rr(bot.length, 0.75, 0.75), HIJ, 10);
        M.finish();
        return M;
    }
    private static double clamp(double a) { return a < 0 ? 0 : (a > 1 ? 1 : a); }
    private static double tz(double x, double y) { double[] q = interp(TORSO, y); return q[1] * Math.sqrt(Math.max(0, 1 - sq(x / q[0]))); }

    private void finish() {
        int nv = 0, ni = 0;
        for (float[] a : bp) nv += a.length / 3;
        for (int[] a : bi) ni += a.length;
        pos = new float[nv * 3]; nor = new float[nv * 3]; col = new float[nv * 3]; idx = new int[ni];
        part = new byte[nv];
        int pv = 0, pi = 0;
        for (int k = 0; k < bp.size(); k++) {
            java.util.Arrays.fill(part, pv / 3, (pv + bp.get(k).length) / 3, (byte) (int) bpt.get(k));
            System.arraycopy(bp.get(k), 0, pos, pv, bp.get(k).length);
            System.arraycopy(bn.get(k), 0, nor, pv, bn.get(k).length);
            System.arraycopy(bc.get(k), 0, col, pv, bc.get(k).length);
            pv += bp.get(k).length;
            System.arraycopy(bi.get(k), 0, idx, pi, bi.get(k).length);
            pi += bi.get(k).length;
        }
        vertCount = nv; triCount = ni / 3;
    }


    // ---- pakaian / jilbab per tampilan (mesh bersama TIDAK diubah) ----
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
    /**
     * Salinan warna titik. pakaian = "badan,lengan[,rok]" (null = bawaan), jilbab = "#hex" (null = bawaan).
     * Mengembalikan array BARU supaya warna satu pemain tidak bocor ke pemain lain.
     */
    float[] outfitColors(String pakaian, String jilbab) {
        float[] c = col.clone();
        double[] body = null, arm = null, skirt = null;
        if (pakaian != null) {
            String[] p = pakaian.split(",");
            if (p.length > 0) body = hex(p[0]);
            if (p.length > 1) arm = hex(p[1]);
            if (p.length > 2) skirt = hex(p[2]);
            if (arm == null) arm = body;
        }
        double[] hij = hex(jilbab);
        for (int i = 0; i < vertCount; i++) {
            double[] k = null;
            switch (part[i]) {
                case P_SHIRT: k = body; break;
                case P_SLEEVE: k = arm; break;
                case P_SKIRT: k = skirt; break;
                case P_HIJAB: k = hij; break;
                default: break;
            }
            if (k != null) { c[i * 3] = (float) k[0]; c[i * 3 + 1] = (float) k[1]; c[i * 3 + 2] = (float) k[2]; }
        }
        return c;
    }

    /** Indeks segitiga; showHijab=false membuang jilbab dan penutup leher (dipakai bila tudung hewan dipasang). */
    int[] indexFor(boolean showHijab) {
        if (showHijab) return idx;
        int n = 0;
        for (int t = 0; t < idx.length; t += 3) if (part[idx[t]] != P_HIJAB) n += 3;
        int[] r = new int[n];
        int o = 0;
        for (int t = 0; t < idx.length; t += 3)
            if (part[idx[t]] != P_HIJAB) { r[o++] = idx[t]; r[o++] = idx[t + 1]; r[o++] = idx[t + 2]; }
        return r;
    }

    /** Uji mandiri: java PutriMesh.java -> cetak jumlah dan checksum. */
    public static void main(String[] a) {
        PutriMesh m = build();
        double sx = 0, sy = 0, sz = 0;
        for (int i = 0; i < m.vertCount; i++) { sx += m.pos[i * 3]; sy += m.pos[i * 3 + 1]; sz += m.pos[i * 3 + 2]; }
        System.out.println("verts " + m.vertCount + " tris " + m.triCount);
        System.out.printf("sum %.3f %.3f %.3f%n", sx, sy, sz);
    }
}
