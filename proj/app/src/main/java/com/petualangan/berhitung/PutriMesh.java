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
    static final double[] WHITE = {0.95, 0.95, 0.98}, SKIN = {1.0, 0.83, 0.68}, MAR = {0.50, 0.05, 0.13},
            BAG = {0.58, 0.08, 0.14}, SHOE = {0.13, 0.12, 0.14}, BROWN = {0.30, 0.15, 0.10},
            PINK = {1.0, 0.66, 0.66}, HIJ = {0.93, 0.93, 0.97};
    static final double[] HC = {0, 70.5, 0.5}, HR = {12.2, 13.2, 11.8};
    static final double[][] TORSO = {{62, 6.0, 5.5, 0, 0}, {56, 8.5, 6.5, 0, 0}, {52, 12.5, 8, 0, 0}, {46, 11.5, 7.6, 0, 0}, {38, 10, 7, 0, 0}, {34, 9.6, 6.8, 0, 0}};
    static final double[][] CAPE = {{62, 8.6, 8.0, 0, 0}, {58, 12.2, 10.2, 0, 0}, {54, 14.0, 10.4, 0, -0.3}, {48, 13.2, 9.6, 0, -0.3}, {42, 12.0, 9.0, 0, -0.3}, {36, 11.0, 8.4, 0, -0.2}};
    static final double[][] SKIRT = {{40, 10.2, 7.6, 0, 0}, {36, 10.4, 7.8, 0, 0}, {25, 11.6, 9.0, 0, 0}, {14, 13.0, 10.4, 0, 0}, {8, 13.6, 11, 0, 0}};

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

    static double[] interp(double[][] tab, double y) {
        int n = tab.length;
        double[] r = new double[4];
        int a;
        if (y >= tab[0][0]) a = -1;
        else if (y <= tab[n - 1][0]) a = -2;
        else { a = 0; while (!(y <= tab[a][0] && y >= tab[a + 1][0])) a++; }
        for (int k = 0; k < 4; k++) {
            if (a == -1) r[k] = tab[0][k + 1];
            else if (a == -2) r[k] = tab[n - 1][k + 1];
            else {
                double t = (tab[a][0] - y) / (tab[a][0] - tab[a + 1][0]);
                r[k] = tab[a][k + 1] + t * (tab[a + 1][k + 1] - tab[a][k + 1]);
            }
        }
        return r; // rx, rz, cx, cz
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

    void loft(double[][] tab, double[] colr, int nu, int ny, Pleat pleat, YOf yof) {
        double[][][] P = new double[nu][ny][3];
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
            }
        }
        grid(P, colr, true, false, null);
    }

    // ---- rakitan putri ----
    static PutriMesh build() {
        final PutriMesh M = new PutriMesh();
        for (int sg = -1; sg <= 1; sg += 2) {
            double s = sg;
            M.tube(new double[][]{{s * 4.6, 12, 0.5}, {s * 4.6, 4, 0.5}}, rr(2, 2.0, 2.0), WHITE, 14);
            M.ellipsoid(new double[]{s * 4.8, 2.4, 2.6}, new double[]{3.4, 2.5, 5.6}, SHOE, 28, 14, 0, PI / 2, null, null);
            M.ellipsoid(new double[]{s * 4.8, 1.2, 2.6}, new double[]{3.4, 1.2, 5.6}, SHOE, 28, 8, -PI / 2, 0, null, null);
        }
        M.loft(SKIRT, MAR, 112, 22, (ph, y) -> {
            double t = Math.abs(((ph * 18 / (2 * PI)) % 1.0) - 0.5) * 2;
            double amp = y > 36 ? 0.0 : 0.095 * Math.min(1, (36 - y) / 12);
            return 1 + amp * (t - 0.5) * 2;
        }, null);
        M.loft(TORSO, WHITE, 48, 16, null, null);
        for (int sg = -1; sg <= 1; sg += 2) {
            double s = sg;
            double[][] path = {{s * 13.2, 49, 0}, {s * 14.8, 43, 0.8}, {s * 16.0, 36, 2.0}, {s * 16.8, 31.0, 3.2}, {s * 17.1, 27.8, 3.5}};
            double[] rd = {4.2, 3.8, 3.5, 3.7, 4.0};
            double[][] rad = new double[5][];
            for (int i = 0; i < 5; i++) rad[i] = new double[]{rd[i], rd[i]};
            M.tube(path, rad, WHITE, 16);
            M.ellipsoid(new double[]{s * 17.1, 25.6, 3.5}, new double[]{2.9, 4.4, 2.5}, SKIN, 18, 12);
        }
        M.superbox(new double[]{0, 35.0, -11.6}, new double[]{9.4, 9.0, 3.8}, BAG, 0.32, 36, 20);
        M.superbox(new double[]{0, 37.5, -15.0}, new double[]{9.6, 6.2, 0.9}, BAG, 0.30, 36, 20);
        M.superbox(new double[]{0, 35.0, -16.1}, new double[]{1.6, 1.3, 0.5}, new double[]{0.62, 0.62, 0.66}, 0.3, 16, 8);
        for (int sg = -1; sg <= 1; sg += 2) {
            double s = sg;
            double[][] pth = {{s * 6.8, 42.5, -10.5}, {s * 9.2, 49, -8.0}, {s * 10.6, 53, -5.0}, {s * 11.4, 54, 0.5},
                    {s * 10.5, 49, tz(10.5, 49) + 1.0}, {s * 9.8, 42, tz(9.8, 42) + 1.0}, {s * 9.6, 36, tz(9.6, 36) + 0.9}};
            M.tube(pth, rr(7, 1.5, 0.55), BAG, 8);
        }
        // kepala + wajah
        M.ellipsoid(HC, HR, SKIN, 48, 28);
        for (int sg = -1; sg <= 1; sg += 2) {
            double s = sg, x = s * 5.0, y = 70.8;
            M.ellipsoid(new double[]{x, y, onHead(x, y, -0.3)}, new double[]{2.1, 2.8, 1.0}, BROWN, 16, 10);
            M.ellipsoid(new double[]{x + s * -0.35, y + 0.9, onHead(x, y, 0.2)}, new double[]{0.7, 0.8, 0.45}, new double[]{1, 1, 1}, 10, 6);
            M.ellipsoid(new double[]{s * 8.0, 65.8, onHead(8.0, 65.8, -0.4)}, new double[]{2.2, 1.5, 0.7}, PINK, 14, 8);
            M.tube(new double[][]{{s * 3.4, 75.6, onHead(3.4, 75.6, 0.4)}, {s * 5.2, 76.2, onHead(5.2, 76.2, 0.4)}, {s * 7.0, 75.6, onHead(7.0, 75.6, 0.4)}}, rr(3, 0.45, 0.45), BROWN, 6);
        }
        M.ellipsoid(new double[]{0, 67.8, onHead(0, 67.8, -0.2)}, new double[]{1.0, 0.9, 0.8}, new double[]{0.95, 0.72, 0.58}, 10, 6);
        double[][] arc = new double[7][];
        for (int i = 0; i < 7; i++) {
            double x = -3.0 + 6.0 * i / 6, y = 63.0 + 0.16 * x * x;
            arc[i] = new double[]{x, y, onHead(x, y, 0.3)};
        }
        M.tube(arc, rr(7, 0.4, 0.4), new double[]{0.85, 0.28, 0.30}, 6);
        // jilbab
        final double[] HJC = {0, 72, -1.0}, HJR = {15.2, 16.6, 14.8};
        final double p0 = 0, t0 = Math.toRadians(-22), pw = Math.toRadians(40), th_ = Math.toRadians(46);
        M.ellipsoid(HJC, HJR, HIJ, 96, 56, -PI / 2, PI / 2,
                (ph, th) -> sq((ph - p0) / pw) + sq((th - t0) / th_) < 1.0,
                P -> {
                    for (double[][] row : P)
                        for (double[] v : row) {
                            double back = clamp((-(v[2] - HJC[2]) / HJR[2] - 0.1) / 0.9);
                            double low = clamp((HJC[1] + 3 - v[1]) / HJR[1]);
                            double w = back * low;
                            v[1] -= 7.0 * Math.pow(w, 1.4);
                            v[0] *= 1 - 0.15 * w;
                            v[2] -= 0.5 * w;
                        }
                });
        double[][] rim = new double[57][];
        for (int i = 0; i < 56; i++) {
            double t = 2 * PI * i / 56, ph = p0 + pw * Math.sin(t), th = t0 + th_ * Math.cos(t);
            rim[i] = new double[]{HJC[0] + HJR[0] * Math.cos(th) * Math.sin(ph), HJC[1] + HJR[1] * Math.sin(th), HJC[2] + HJR[2] * Math.cos(th) * Math.cos(ph)};
        }
        rim[56] = rim[0];
        M.tube(rim, rr(57, 1.2, 1.2), new double[]{0.97, 0.97, 1.0}, 10);
        final double d0 = Math.toRadians(75);
        M.loft(CAPE, HIJ, 96, 18, null, ph -> {
            double d = Math.abs(((ph + PI) % (2 * PI)) - PI), db = PI - d, yb = 51.5;
            if (d < d0) yb -= 14 * (1 - d / d0);
            if (db < d0) yb -= 16 * (1 - db / d0);
            return new double[]{62, yb};
        });
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
