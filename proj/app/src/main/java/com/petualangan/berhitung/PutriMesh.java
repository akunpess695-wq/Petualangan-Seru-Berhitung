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

    static final double PI = Math.PI;
    static final double[] WHITE = {0.95, 0.95, 0.98}, SKIN = {1.0, 0.847, 0.694}, MAR = {0.64, 0.12, 0.18},
            BAG = {0.75, 0.14, 0.16}, SHOE = {0.11, 0.11, 0.12}, BROWN = {0.31, 0.19, 0.14},
            PINK = {1.0, 0.72, 0.70}, HIJ = {0.93, 0.93, 0.97};
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

    static double onHead(double x, double y, double off) {
        double u = sq(x / HR[0]) + sq((y - HC[1]) / HR[1]);
        return HC[2] + HR[2] * Math.sqrt(Math.max(0, 1 - u)) + off;
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
        bp.add(fp); bn.add(fn); bc.add(fc); bi.add(fi);
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
        // rok lipit kotak, agak melebar di bawah
        M.loft(SKIRT, MAR, 112, 22, (ph, y) -> {
            double t = Math.abs(((ph * 16 / (2 * PI)) % 1.0) - 0.5) * 2;
            double amp = y > 29 ? 0.0 : 0.06 * Math.min(1, (29 - y) / 8);
            return 1 + amp * (t - 0.5) * 2;
        }, null);
        // baju
        M.loft(TORSO, WHITE, 48, 16, null, null);
        // lengan: lurus ke bawah, sedikit melebar di manset
        for (int sg = -1; sg <= 1; sg += 2) {
            double s = sg;
            double[][] path = {{s * 10.8, 48.5, -0.6}, {s * 12.4, 42, -1.2}, {s * 13.8, 36, -1.8}, {s * 15.0, 30.0, -2.0}};
            double[] rd = {3.9, 3.8, 3.7, 3.9};
            double[][] rad = new double[4][];
            for (int i = 0; i < 4; i++) rad[i] = new double[]{rd[i], rd[i]};
            M.tube(path, rad, WHITE, 16);
            M.ellipsoid(new double[]{s * 10.8, 48.5, -0.6}, new double[]{3.9, 3.9, 3.9}, WHITE, 16, 10);
            M.ellipsoid(new double[]{s * 15.0, 26.6, -0.4}, new double[]{2.7, 4.0, 2.5}, SKIN, 18, 12);
        }
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

        M.loft(NECK, HIJ, 48, 14, null, null); // penutup leher putih
        // jilbab: satu cangkang (kubah -> bahu -> ujung V depan dan belakang), bukaan wajah dipotong
        final double yc = 68.3, hh = 12.4, pw = Math.toRadians(47);
        final double d0 = Math.toRadians(76), d1 = Math.toRadians(110);
        M.loft(HIJAB, HIJ, 216, 72, null, ph -> {
            double d = Math.abs(((ph + PI) % (2 * PI)) - PI), yb;
            if (d < d0) yb = 54.4 - 10.4 * (1 - d / d0);                 // cape depan: V sampai dada
            else if (d < d1) { double t = (d - d0) / (d1 - d0); t = t * t * (3 - 2 * t); yb = 54.4 - 7.4 * t; } // samping: tepi turun menutup bahu
            else yb = 43.0 + 4.0 * Math.sin(PI - d) / Math.sin(PI - d1); // belakang: tepi landai ke ujung y=43
            return new double[]{89.5, yb};
        }, (ph, y) -> sq(ph / pw) + sq((y - yc) / hh) < 1.0);
        // bingkai bukaan (tepi jilbab yang tebal di sekeliling wajah)
        double[][] rim = new double[73][];
        for (int i = 0; i < 72; i++) {
            double t = 2 * PI * i / 72, ph = pw * Math.sin(t), y = yc + hh * Math.cos(t);
            double[] q = interp(HIJAB, y);
            rim[i] = new double[]{q[2] + q[0] * Math.sin(ph), y, q[3] + q[1] * Math.cos(ph)};
        }
        rim[72] = rim[0];
        M.tube(rim, rr(73, 0.75, 0.75), HIJ, 10);
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
        int pv = 0, pi = 0;
        for (int k = 0; k < bp.size(); k++) {
            System.arraycopy(bp.get(k), 0, pos, pv, bp.get(k).length);
            System.arraycopy(bn.get(k), 0, nor, pv, bn.get(k).length);
            System.arraycopy(bc.get(k), 0, col, pv, bc.get(k).length);
            pv += bp.get(k).length;
            System.arraycopy(bi.get(k), 0, idx, pi, bi.get(k).length);
            pi += bi.get(k).length;
        }
        vertCount = nv; triCount = ni / 3;
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
