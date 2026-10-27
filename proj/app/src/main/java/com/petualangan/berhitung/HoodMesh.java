package com.petualangan.berhitung;

import java.util.ArrayList;

/**
 * Tudung hewan 3D untuk putra DAN putri (putri tetap berhijab: tudung dipakai di atas kubah jilbab) (20 hewan sesuai lembar referensi "Detail Lengkap Tudung Hewan"; nomor sama dengan AVATARS / HOOD_MAIN di MainActivity).
 * Mesh tambahan: dibuat terpisah dari PutraMesh/PutriMesh, dipasang di kepala yang sama (koordinat akhir
 * masing-masing mesh), lalu digambar bersama oleh PutriGLView. Tanpa dependensi Android.
 * Bukaan wajah dinyatakan relatif terhadap kepala (sudut w, tinggi s = -1 dagu .. +1 puncak), jadi
 * bentuknya sama untuk kedua karakter walau proporsi kepala putra (chibi) dan putri berbeda.
 */
final class HoodMesh {
    static final int COUNT = 25;   // slot 0..24; 12 = tanpa tudung (tidak dipakai); 17..20 tidak dipakai (alias di MainActivity.HOOD_ALIAS)
    // 0 singa, 1 harimau, 2 panda, 3 kapibara, 4 katak, 5 rubah, 6 kelinci, 7 anjing, 8 kucing, 9 unicorn, 10 penguin, 11 ayam,
    // 12 (tanpa tudung), 13 domba, 14 buaya, 15 gajah, 16 zebra, 21 kuda, 22 sapi, 23 kura-kura, 24 beruang
    static final String[] MAIN = {"#F2B24C", "#F28C28", "#FFFFFF", "#9C6B45", "#5DBB4A", "#E8631C",
            "#F6EEF3", "#A8744A", "#9AA5AD", "#F3EAF7", "#37474F", "#FBF7F2", "#000000",
            "#F4EEE4", "#5CB847", "#8C8A93", "#F5F5F5", "#FBF7F2", "#9C6B45", "#7A4B2E", "#FBF7F2", "#A65E32", "#FFFFFF", "#58B84C", "#7A4B2E"};
    static final String[] ACC = {"#B5651D", "#212121", "#212121", "#D8B58C", "#FFFFFF", "#FFFFFF",
            "#F8BBD0", "#5D3A1E", "#6E7A85", "#F06292", "#FFFFFF", "#E53935", "#000000",
            "#E6D3B8", "#E8B93A", "#E3B7B0", "#212121", "#E53935", "#D8B58C", "#E2C6A0", "#E53935", "#4A2A17", "#212121", "#2F7D3A", "#E2C6A0"};

    /** Slot yang berisi hewan (12 dan 17..20 tidak). */
    static boolean used(int a) { return a >= 0 && a < COUNT && a != 12 && !(a >= 17 && a <= 20); }

    float[] pos, nor, col;
    int[] idx;
    int vertCount;

    private final ArrayList<float[]> bp = new ArrayList<>(), bn = new ArrayList<>(), bc = new ArrayList<>();
    private final ArrayList<int[]> bi = new ArrayList<>();
    private int voff = 0;

    // pusat kepala dan jari-jari CANGKANG tudung (kepala + tebal T)
    private static final double T = 2.0;
    private final double SB;                     // tepi bawah tudung (tinggi relatif)
    private final double S_OPEN;                 // di bawah ini bukaan lurus ke bawah (dagu bebas)
    private final double PW;                     // setengah lebar bukaan
    private final boolean putra;                 // kepala putra (profil headF)
    private final boolean hij;                   // putri berhijab: cangkang mengikuti kubah jilbab (PutriMesh.HIJAB)
    private final double cx, cy, cz, RX, RY, RZ;
    private final double sTop;                   // batas atas bukaan (dahi)
    private final double k;                      // skala aksesori
    private static final double HS = 1.11, HV = 1.09;  // jilbab -> tudung: skala mendatar dan tegak terhadap (0, 70, -3.5)

    private HoodMesh(int gender) {
        putra = gender == 0;
        hij = !putra;
        if (putra) {   // kepala putra setelah scaleFrom(1.2, 1.02, 1.15) dan shortenLegs (-4.06)
            cx = 0; cy = 56.0 + 12.5 * 1.02 - 4.06; cz = 0.8;
            RX = 12.6 * 1.2 + T; RY = 12.9 * 1.02 + T; RZ = 12.5 * 1.15 + T;
            sTop = 0.36; SB = -0.78; S_OPEN = -0.55; PW = Math.toRadians(62);
        } else {       // putri: tudung di atas jilbab (kubah jilbab: pusat (0, 70, -3.5), jari-jari kira-kira 16 x 19,5 x 16,8)
            cx = 0; cy = 70.0; cz = -3.5;
            RX = 16.0 * HS; RY = 19.5 * HV; RZ = 16.8 * HS;
            sTop = 0.46; SB = -0.56; S_OPEN = -0.30; PW = Math.toRadians(56);
        }
        k = RX / 17.0;
    }

    static HoodMesh build(int animal, int gender) {
        HoodMesh H = new HoodMesh(gender == 0 ? 0 : 1);   // 0 putra, 1 putri (berhijab)
        H.make(animal);
        H.finish();
        return H;
    }

    // ---------------- geometri dasar ----------------
    private double prof(double th) { return putra ? PutraMesh.headF(th) : Math.max(0, Math.cos(th)); }

    /** Lingkar cangkang pada tinggi relatif s: {y, jari-jari x, jari-jari z, pusat z}. */
    private double[] ring(double s) {
        if (!hij) { double f = prof(Math.asin(s)); return new double[]{cy + RY * s, RX * f, RZ * f, cz}; }
        double y = cy + RY * s, ys = cy + (y - cy) / HV;     // kembalikan ke tinggi jilbab
        double[] q = PutriMesh.interp(PutriMesh.HIJAB, ys);
        return new double[]{y, q[0] * HS, q[1] * HS, q[3]};
    }

    /** Titik di cangkang: sudut w (derajat, 0 = depan), tinggi relatif s, digeser 'out' ke arah luar. */
    private double[] pt(double wDeg, double s, double out) {
        s = Math.max(-0.999, Math.min(0.999, s));
        double w = Math.toRadians(wDeg);
        double[] g = ring(s);
        double x = cx + g[1] * Math.sin(w), y = g[0], z = g[3] + g[2] * Math.cos(w);
        if (out != 0) {
            double a = (x - cx) / (RX * RX), b = (y - cy) / (RY * RY), c = (z - cz) / (RZ * RZ);   // gradien elipsoid pendekatan
            double l = Math.sqrt(a * a + b * b + c * c);
            if (l > 1e-9) { x += out * a / l; y += out * b / l; z += out * c / l; }
        }
        return new double[]{x, y, z};
    }
    private static double[] add(double[] a, double x, double y, double z) { return new double[]{a[0] + x, a[1] + y, a[2] + z}; }
    private static double[] cross(double[] a, double[] b) { return new double[]{a[1] * b[2] - a[2] * b[1], a[2] * b[0] - a[0] * b[2], a[0] * b[1] - a[1] * b[0]}; }
    private static double[] unit(double[] a) { double l = Math.sqrt(a[0] * a[0] + a[1] * a[1] + a[2] * a[2]); if (l < 1e-12) return new double[]{0, 1, 0}; return new double[]{a[0] / l, a[1] / l, a[2] / l}; }
    private static double[] sub(double[] a, double[] b) { return new double[]{a[0] - b[0], a[1] - b[1], a[2] - b[2]}; }
    private static double[] col(String h) { double[] c = PutraMesh.hex(h); return c == null ? new double[]{1, 1, 1} : c; }

    /** Kisi titik G[i][j] -> segitiga. i melingkar bila closedU. keep[i][j] = false membuang sel. */
    private void grid(double[][][] G, double[] c, boolean closedU, boolean[][] keep) {
        int nu = G.length, nv = G[0].length;
        float[] p = new float[nu * nv * 3], n = new float[nu * nv * 3], cl = new float[nu * nv * 3];
        for (int i = 0; i < nu; i++)
            for (int j = 0; j < nv; j++) {
                double[] a = closedU ? G[(i + 1) % nu][j] : G[Math.min(i + 1, nu - 1)][j];
                double[] b = closedU ? G[(i - 1 + nu) % nu][j] : G[Math.max(i - 1, 0)][j];
                double[] u = sub(a, b), v = sub(G[i][Math.min(j + 1, nv - 1)], G[i][Math.max(j - 1, 0)]);
                double[] nn = unit(cross(u, v));
                int o = (i * nv + j) * 3;
                for (int m = 0; m < 3; m++) { p[o + m] = (float) G[i][j][m]; n[o + m] = (float) nn[m]; cl[o + m] = (float) c[m]; }
            }
        int cu = closedU ? nu : nu - 1;
        ArrayList<Integer> f = new ArrayList<>();
        for (int i = 0; i < cu; i++) {
            int i2 = (i + 1) % nu;
            for (int j = 0; j < nv - 1; j++) {
                if (keep != null && !keep[i][j]) continue;
                int a = voff + i * nv + j, b = voff + i2 * nv + j, c2 = voff + i2 * nv + j + 1, d = voff + i * nv + j + 1;
                f.add(a); f.add(b); f.add(c2); f.add(a); f.add(c2); f.add(d);
            }
        }
        int[] fi = new int[f.size()];
        for (int q = 0; q < fi.length; q++) fi[q] = f.get(q);
        bp.add(p); bn.add(n); bc.add(cl); bi.add(fi);
        voff += nu * nv;
    }

    private void ellipsoid(double[] c, double rx, double ry, double rz, double[] colr) { ellipsoidN(c, rx, ry, rz, colr, 14, 9); }
    private void ellipsoidN(double[] c, double rx, double ry, double rz, double[] colr, int nu, int nv) {
        double[][][] G = new double[nu][nv][3];
        for (int i = 0; i < nu; i++)
            for (int j = 0; j < nv; j++) {
                double ph = 2 * Math.PI * i / nu, th = -Math.PI / 2 + Math.PI * j / (nv - 1);
                G[i][j] = new double[]{c[0] + rx * Math.cos(th) * Math.sin(ph), c[1] + ry * Math.sin(th), c[2] + rz * Math.cos(th) * Math.cos(ph)};
            }
        grid(G, colr, true, null);
    }
    private void ball(double[] c, double r, double[] colr) { ellipsoid(c, r, r, r, colr); }

    private void tube(double[][] path, double[] rad, double[] colr, int ns) {
        int n = path.length;
        double[][][] G = new double[ns][n][3];
        for (int j = 0; j < n; j++) {
            double[] t = unit(sub(path[Math.min(j + 1, n - 1)], path[Math.max(j - 1, 0)]));
            double[] h = Math.abs(t[2]) > 0.7 ? new double[]{0, 1, 0} : new double[]{0, 0, 1};
            double[] a = unit(cross(t, h)), b = unit(cross(t, a));
            for (int i = 0; i < ns; i++) {
                double ang = 2 * Math.PI * i / ns;
                for (int m = 0; m < 3; m++) G[i][j][m] = path[j][m] + a[m] * rad[j] * Math.cos(ang) + b[m] * rad[j] * Math.sin(ang);
            }
        }
        grid(G, colr, true, null);
    }
    private void cone(double[] base, double[] tip, double r, double[] colr) {
        tube(new double[][]{base, tip}, new double[]{r, 0.08}, colr, 8);
    }

    // ---------------- cangkang tudung ----------------
    private boolean inWindow(double aw, double s) {
        if (aw >= PW) return false;
        if (s <= S_OPEN) return true;
        if (s >= sTop) return false;
        double q = (s - S_OPEN) / (sTop - S_OPEN);
        return aw < PW * Math.pow(Math.max(0, 1 - Math.pow(q, 4)), 0.25);
    }

    private void shell(double[] c) {
        int nu = 96, nv = 40;
        double th0 = Math.asin(SB);
        double[][][] G = new double[nu][nv][3];
        double[] TH = new double[nv];
        for (int j = 0; j < nv; j++) {
            double sj = hij ? SB + (1 - SB) * j / (nv - 1) : Math.sin(th0 + (Math.PI / 2 - th0) * j / (nv - 1));
            TH[j] = Math.asin(Math.min(1.0, sj));         // TH disimpan sebagai sudut; s = sin(TH)
        }
        for (int i = 0; i < nu; i++) {
            double ph = 2 * Math.PI * i / nu;
            for (int j = 0; j < nv; j++) {
                double[] g = ring(Math.min(0.9999, Math.sin(TH[j])));
                G[i][j] = new double[]{cx + g[1] * Math.sin(ph), g[0], g[3] + g[2] * Math.cos(ph)};
            }
        }
        boolean[][] keep = new boolean[nu][nv];
        for (int i = 0; i < nu; i++)
            for (int j = 0; j < nv; j++) {
                double pm = 2 * Math.PI * (i + 0.5) / nu;
                double w = ((pm + Math.PI) % (2 * Math.PI)); if (w < 0) w += 2 * Math.PI; w -= Math.PI;
                double sm = j < nv - 1 ? (Math.sin(TH[j]) + Math.sin(TH[j + 1])) / 2 : Math.sin(TH[j]);
                keep[i][j] = !inWindow(Math.abs(w), sm);
            }
        grid(G, c, true, keep);

        // bingkai bukaan wajah (tepi tudung yang tebal, rapi menutup tepi bergerigi)
        ArrayList<double[]> rim = new ArrayList<>();
        rim.add(pt(-Math.toDegrees(PW), SB, 0));
        rim.add(pt(-Math.toDegrees(PW), (SB + S_OPEN) / 2, 0));
        rim.add(pt(-Math.toDegrees(PW), S_OPEN, 0));
        for (int a = 1; a <= 12; a++) {   // sisi kiri naik ke dahi
            double q = Math.sin(Math.PI / 2 * a / 12.0);
            double lim = PW * Math.pow(Math.max(0, 1 - Math.pow(q, 4)), 0.25);
            rim.add(pt(-Math.toDegrees(lim), S_OPEN + q * (sTop - S_OPEN), 0));
        }
        for (int a = 11; a >= 0; a--) {   // sisi kanan turun
            double q = Math.sin(Math.PI / 2 * a / 12.0);
            double lim = PW * Math.pow(Math.max(0, 1 - Math.pow(q, 4)), 0.25);
            rim.add(pt(Math.toDegrees(lim), S_OPEN + q * (sTop - S_OPEN), 0));
        }
        rim.add(pt(Math.toDegrees(PW), (SB + S_OPEN) / 2, 0));
        rim.add(pt(Math.toDegrees(PW), SB, 0));
        double[][] rp = rim.toArray(new double[0][]);
        double[] rr = new double[rp.length];
        java.util.Arrays.fill(rr, 1.25 * k);
        tube(rp, rr, c, 8);

        // tepi bawah: dari sisi kanan, lewat belakang, ke sisi kiri
        int nb = 36;
        double[][] bot = new double[nb][];
        double[] br = new double[nb];
        for (int i = 0; i < nb; i++) {
            double w = Math.toDegrees(PW) + (360 - 2 * Math.toDegrees(PW)) * i / (nb - 1.0);
            bot[i] = pt(w, SB, 0);
            br[i] = 1.0 * k;
        }
        tube(bot, br, c, 8);
    }

    // ---------------- aksesori ----------------
    /** Titik aksesori: pada kubah jilbab tinggi di atas 0,5 dipadatkan supaya telinga tidak menumpuk di puncak. */
    private double[] pa(double wDeg, double s, double out) {
        if (hij) s = s >= 0.8 ? 0.70 + (s - 0.8) * 0.5 : (s >= 0.5 ? 0.54 + (s - 0.5) * 0.53 : s);
        return pt(wDeg, s, out);
    }
    private void roundEar(double wDeg, double s, double r, double[] outer, double[] inner) {
        double[] c = pa(wDeg, s, 0.25 * r);
        ellipsoid(c, r, r, r * 0.75, outer);
        if (inner != null) ellipsoid(add(c, 0, 0, r * 0.35), r * 0.58, r * 0.58, r * 0.55, inner);
    }
    private void pointEar(double wDeg, double s, double h, double wd, double[] outer, double[] inner) {
        double sg = Math.signum(wDeg);
        double[] b = pa(wDeg, s, -0.3 * wd);
        double[] tip = add(b, sg * 0.28 * h, 0.95 * h, -0.1 * h);
        cone(b, tip, wd, outer);
        if (inner != null) cone(add(b, 0, 0.1 * h, wd * 0.55), add(b, sg * 0.22 * h, 0.7 * h, wd * 0.2), wd * 0.55, inner);
    }
    private void stripeMeridian(double wDeg, double s0, double s1, double[] c) {
        int n = 6;
        double[][] p = new double[n][];
        double[] r = new double[n];
        for (int i = 0; i < n; i++) { p[i] = pa(wDeg, s0 + (s1 - s0) * i / (n - 1.0), 0.25); r[i] = 0.5 * k; }
        tube(p, r, c, 5);
    }
    private void stripeSide(double sg, double s, double w0, double w1, double[] c) {
        int n = 6;
        double[][] p = new double[n][];
        double[] r = new double[n];
        for (int i = 0; i < n; i++) { p[i] = pa(sg * (w0 + (w1 - w0) * i / (n - 1.0)), s, 0.25); r[i] = 0.55 * k; }
        tube(p, r, c, 5);
    }

    private static double[] sh(double[] c, double f) {
        return new double[]{Math.min(1, c[0] * f), Math.min(1, c[1] * f), Math.min(1, c[2] * f)};
    }
    /** Pita melintang di muka tudung dari sudut w0 ke w1 pada tinggi s. */
    private void band(double s, double w0, double w1, double r, double[] c) {
        int n = 9;
        double[][] p = new double[n][];
        double[] rr = new double[n];
        for (int i = 0; i < n; i++) { p[i] = pa(w0 + (w1 - w0) * i / (n - 1.0), s, 0.3 * k); rr[i] = r; }
        tube(p, rr, c, 5);
    }
    /** Garis melingkar di sisi (tebal r), dari sudut w0 ke w1 pada tinggi s. */
    private void stripeSideT(double sg, double s, double w0, double w1, double r, double[] c) {
        int n = 7;
        double[][] p = new double[n][];
        double[] rr = new double[n];
        for (int i = 0; i < n; i++) { p[i] = pa(sg * (w0 + (w1 - w0) * i / (n - 1.0)), s, -0.45 * r); rr[i] = r; }
        tube(p, rr, c, 5);
    }
    /** Garis tegak (tebal r) dari tinggi s0 ke s1 pada sudut w. */
    private void stripeMeridianT(double wDeg, double s0, double s1, double r, double[] c) {
        int n = 6;
        double[][] p = new double[n][];
        double[] rr = new double[n];
        for (int i = 0; i < n; i++) { p[i] = pa(wDeg, s0 + (s1 - s0) * i / (n - 1.0), -0.45 * r); rr[i] = r; }
        tube(p, rr, c, 5);
    }
    /** Bercak: kubah tipis menempel di cangkang (resolusi rendah). */
    private void patch(double wDeg, double s, double r, double[] c) {
        ellipsoidN(pt(wDeg, s, -0.3 * r), r, r, r * 0.9, c, 9, 6);
    }

    /** Hewan nomor 3 kapibara, 11 ayam, 13 domba, 14 buaya, 15 gajah, 16 zebra, 21 kuda, 22 sapi, 23 kura-kura, 24 beruang. */
    private void extra(int a, double[] main, double[] acc) {
        final double[] white = {1, 1, 1}, dark = {0.13, 0.13, 0.13};
        final double PWd = Math.toDegrees(PW);
        for (int sg = -1; sg <= 1; sg += 2) {
            switch (a) {
                case 3: { // kapibara: telinga kecil bulat + bulu pipi cokelat muda
                    roundEar(sg * 50, 0.8, 3.0 * k, main, sh(acc, 0.9));
                    ellipsoid(pa(sg * 78, -0.35, 0.3 * k), 1.8 * k, 4.4 * k, 3.0 * k, sh(main, 0.88));
                    break;
                }
                case 11: { // ayam: sayap kecil di sisi
                    ellipsoid(add(pa(sg * 84, -0.1, 0.3 * k), sg * 1.4 * k, 0, 0), 2.0 * k, 5.2 * k, 3.4 * k, sh(main, 0.95));
                    break;
                }
                case 13: { // domba: telinga bulat krem di sisi
                    ellipsoid(add(pa(sg * 88, -0.12, 0.3 * k), sg * 1.6 * k, 0, 0.8 * k), 2.3 * k, 4.8 * k, 3.4 * k, acc);
                    break;
                }
                case 14: { // buaya: mata menonjol di atas + lubang hidung
                    double[] e = pa(sg * 26, 0.84, 1.0 * k);
                    double[] f = unit(new double[]{0, 0.35, 1});
                    ball(e, 4.3 * k, main);
                    ball(add(e, f[0] * 2.8 * k, f[1] * 2.8 * k, f[2] * 2.8 * k), 2.9 * k, col("#F4E36B"));
                    ball(add(e, f[0] * 4.6 * k, f[1] * 4.6 * k, f[2] * 4.6 * k), 1.4 * k, dark);
                    ball(pt(sg * 6, 0.62, 0.3 * k), 0.8 * k, sh(main, 0.45));
                    break;
                }
                case 15: { // gajah: telinga lebar berisi merah muda
                    double[] c = add(pa(sg * 84, 0.3, 0.3 * k), sg * 2.4 * k, 0, 0);
                    ellipsoid(add(c, 0, 0, 1.2 * k), 3.2 * k, 8.2 * k, 7.0 * k, main);
                    ellipsoid(add(c, sg * 2.2 * k, 0, 1.8 * k), 1.3 * k, 6.0 * k, 5.0 * k, acc);
                    break;
                }
                case 16: { // zebra: telinga tegak putih berisi hitam + garis melingkar
                    pointEar(sg * 38, 0.86, 8.5 * k, 3.4 * k, main, acc);
                    for (double s : new double[]{0.72, 0.48, 0.24, 0.0, -0.24, -0.48})
                        if (s > SB + 0.1) stripeSideT(sg, s, 68, 172, 1.0 * k, acc);
                    stripeMeridianT(sg * 40, 0.52, 0.95, 0.8 * k, acc);
                    stripeMeridianT(sg * 13, 0.76, 0.97, 0.8 * k, acc);
                    break;
                }
                case 21: { // kuda: telinga tegak berisi krem
                    pointEar(sg * 36, 0.86, 7.5 * k, 3.6 * k, main, col("#EBCBA6"));
                    break;
                }
                case 22: { // sapi: tanduk kecil + telinga mendatar + bercak hitam
                    double[] b = pa(sg * 30, 0.9, 0);
                    cone(b, add(b, sg * 2.2 * k, 4.8 * k, -0.4 * k), 1.6 * k, col("#F3E6C8"));
                    double[] ec = add(pa(sg * 70, 0.62, 0.3 * k), sg * 3.4 * k, 0, 0);
                    ellipsoid(ec, 4.2 * k, 1.9 * k, 2.6 * k, main);
                    ellipsoid(add(ec, 0, 0, 0.9 * k), 2.9 * k, 1.1 * k, 1.7 * k, col("#F8BBD0"));
                    patch(sg * 84, -0.1, 3.8 * k, acc);
                    patch(sg * 125, 0.55, 3.8 * k, acc);
                    patch(sg * 155, -0.2, 3.4 * k, acc);
                    break;
                }
                case 23: { // kura-kura: sirip + garis krem di sisi
                    double[] c = add(pa(sg * 84, -0.42, 0.3 * k), sg * 2.5 * k, 0, 0.5 * k);
                    ellipsoid(c, 2.2 * k, 4.6 * k, 3.4 * k, sh(main, 1.18));
                    stripeSide(sg, 0.05, 66, 98, col("#F5E6A8"));
                    stripeSide(sg, -0.2, 66, 98, col("#F5E6A8"));
                    ellipsoid(pa(sg * 26, 0.68, 0.2 * k), 3.6 * k, 3.0 * k, 1.0 * k, sh(acc, 0.9));
                    break;
                }
                case 24: { // beruang: telinga bulat besar berisi krem
                    roundEar(sg * 44, 0.84, 5.6 * k, main, acc);
                    break;
                }
            }
        }
        switch (a) { // aksesori tengah
            case 11: { // ayam: jengger merah, paruh kuning, mata, pial
                double[][] cb = {{0, 0.62, 2.3}, {0, 0.8, 3.1}, {0, 0.97, 3.6}, {180, 0.92, 3.1}, {180, 0.66, 2.5}};
                for (double[] q : cb) ellipsoidN(add(pa(q[0], q[1], 0.2 * k), 0, 0.5 * q[2] * k, 0), q[2] * k, q[2] * k, q[2] * k, acc, 10, 6);
                double[] b = pa(0, sTop + 0.18, 0.3 * k);
                cone(b, add(b, 0, -0.8 * k, 5.0 * k), 2.2 * k, col("#FFC107"));
                cone(add(b, 0, -1.8 * k, 0.3 * k), add(b, 0, -2.2 * k, 3.8 * k), 1.5 * k, col("#F9A825"));
                ellipsoidN(add(pa(0, sTop + 0.03, 0.8 * k), 0, 0, 0.8 * k), 1.6 * k, 2.4 * k, 1.1 * k, acc, 8, 5);
                break;
            }
            case 13: { // domba: gumpalan wol
                double[][] rings = {{0.7, 3.4}, {0.42, 3.6}, {0.14, 3.6}, {-0.14, 3.6}, {-0.4, 3.4}};
                double[][] ws = {{-50, 50, -90, 90, -130, 130, 180}, {-55, 55, -90, 90, -125, 125, -160, 160},
                        {-112, 112, -148, 148, 180}, {-130, 130, -160, 160, 180}, {-115, 115, -145, 145, 180}};
                int n = 0;
                double[][] top = {{0, 0.96, 4.2}, {90, 0.9, 4.0}, {-90, 0.9, 4.0}, {180, 0.9, 4.0}};
                for (double[] q : top) ellipsoidN(pt(q[0], q[1], 0.3 * q[2] * k), q[2] * k, q[2] * k, q[2] * k * 0.9, main, 8, 5);
                for (int r = 0; r < rings.length; r++) {
                    if (rings[r][0] <= SB + 0.1) continue;
                    for (double w : ws[r]) {
                        double rad = rings[r][1] * k;
                        ellipsoidN(pt(w, rings[r][0], 0.3 * rad), rad, rad, rad * 0.9, (n++ % 2 == 0) ? main : sh(main, 0.94), 8, 5);
                    }
                }
                break;
            }
            case 14: { // buaya: gigi di tepi bukaan + tulang punggung kuning
                double[] tw = {-50, -38, -25, -12.5, 0, 12.5, 25, 38, 50};
                for (double w : tw) {
                    double q = Math.pow(Math.max(0, 1 - Math.pow(Math.abs(w) / PWd, 4)), 0.25);
                    double se = S_OPEN + q * (sTop - S_OPEN);
                    double[] b = pt(w, se + 0.03, 0.6 * k);
                    cone(b, add(b, 0, -3.0 * k, 0.9 * k), 1.1 * k, white);
                }
                double[][] rg = {{0, 0.72}, {0, 0.88}, {0, 0.99}, {180, 0.92}, {180, 0.7}, {180, 0.46}, {180, 0.22}, {180, -0.04}};
                for (double[] q : rg) {
                    double[] b = pt(q[0], q[1], 0.3 * k);
                    double back = q[0] == 180 ? -2.2 * k : 0;
                    cone(b, add(b, 0, 3.0 * k, back), 1.3 * k, acc);
                }
                break;
            }
            case 15: { // belalai pendek di dahi
                double[] b = pa(0, 0.92, 0.5 * k), p1 = pa(0, 0.72, 1.6 * k), p2 = pa(0, sTop + 0.14, 2.4 * k);
                double[][] p = {b, p1, p2, add(p2, 0, -1.8 * k, 1.0 * k)};
                tube(p, new double[]{2.5 * k, 2.3 * k, 2.0 * k, 1.7 * k}, sh(main, 0.95), 8);
                ball(p[3], 1.8 * k, sh(main, 0.95));
                break;
            }
            case 16: { // zebra: surai hitam dari puncak ke belakang
                double[] ms = {0.95, 0.75, 0.5, 0.25, 0.0, -0.25};
                for (double s : ms) if (s > SB + 0.1) ellipsoidN(pt(180, s, 0.5 * k), 2.4 * k, 2.4 * k, 2.4 * k, acc, 8, 5);
                ellipsoidN(pt(0, 0.93, 0.4 * k), 2.0 * k, 2.0 * k, 2.0 * k, acc, 8, 5);
                break;
            }
            case 21: { // kuda: surai dari dahi ke belakang + jambul
                double[][] mp = {pt(0, 0.9, 0.5 * k), pt(0, 0.99, 1.0 * k), pt(180, 0.99, 1.0 * k), pt(180, 0.75, 1.0 * k),
                        pt(180, 0.5, 1.0 * k), pt(180, 0.25, 1.0 * k), pt(180, 0.0, 1.0 * k), pt(180, -0.25, 1.0 * k)};
                tube(mp, new double[]{1.6 * k, 2.0 * k, 2.4 * k, 2.6 * k, 2.6 * k, 2.4 * k, 2.2 * k, 1.8 * k}, acc, 8);
                for (int q = -1; q <= 1; q++) {
                    double[] b = pa(q * 9, 0.88, 0.4 * k);
                    cone(b, add(b, q * 0.8 * k, -4.6 * k, 1.4 * k), 1.7 * k, acc);
                }
                break;
            }
            case 22: { // sapi: bercak di dahi
                patch(-24, 0.86, 3.0 * k, acc);
                patch(14, 0.97, 2.6 * k, acc);
                break;
            }
            case 23: { // kura-kura: perisai sisik
                ellipsoid(pa(0, 0.74, 0.2 * k), 5.2 * k, 3.8 * k, 1.1 * k, acc);
                ellipsoid(pa(0, 0.96, 0.2 * k), 6.0 * k, 2.0 * k, 6.0 * k, sh(acc, 0.92));
                break;
            }
            default: break;
        }
    }

    private void eyesOnly(double[] eye) {
        double s0 = sTop + 0.15;
        for (int sg = -1; sg <= 1; sg += 2) {
            double[] e = pa(sg * 19, s0 + 0.16, 0.5 * k);
            ellipsoidN(add(e, 0, 0, 0.5 * k), 1.0 * k, 1.3 * k, 0.9 * k, eye, 10, 6);
        }
    }

    /** Wajah hewan di dahi tudung: moncong, hidung, mata, kumis (resolusi rendah, kira-kira 450 titik). */
    private void face(double[] muz, double[] nose, double[] eye, boolean whisk, double mw) {
        double s0 = sTop + 0.15;
        double[] m = pa(0, s0, 0.3 * k);
        ellipsoidN(add(m, 0, 0, 0.5 * k), 6.0 * k * mw, 3.3 * k, 2.4 * k, muz, 12, 7);
        ellipsoidN(add(m, 0, 1.0 * k, 2.4 * k), 1.9 * k, 1.3 * k, 1.2 * k, nose, 10, 6);
        for (int sg = -1; sg <= 1; sg += 2) {
            double[] e = pa(sg * 19, s0 + 0.16, 0.5 * k);
            ellipsoidN(add(e, 0, 0, 0.5 * k), 1.0 * k, 1.3 * k, 0.9 * k, eye, 10, 6);
            if (whisk) {
                for (int q = 0; q < 2; q++) {
                    double[] b = add(m, sg * 3.6 * k, -0.9 * k - q * 1.0 * k, 2.2 * k);
                    tube(new double[][]{b, add(b, sg * 5.5 * k, (q == 0 ? 0.8 : -0.8) * k, -0.6 * k)}, new double[]{0.22 * k, 0.16 * k}, new double[]{0.15, 0.12, 0.1}, 4);
                }
            }
        }
    }
    private void faceFor(int a) {
        final double[] dk = {0.17, 0.11, 0.08}, pink = col("#F48FB1");
        switch (a) {
            case 0: face(col("#F7E6C4"), col("#5D3A1E"), dk, true, 1.0); break;
            case 1: face(col("#FFF3DF"), dk, dk, true, 1.0); break;
            case 2: { // panda: bercak mata hitam + hidung
                double s0 = sTop + 0.15;
                face(col("#FFFFFF"), dk, new double[]{1, 1, 1}, false, 0.9);
                for (int sg = -1; sg <= 1; sg += 2) {
                    double[] e = pa(sg * 19, s0 + 0.16, 0.4 * k);
                    ellipsoidN(add(e, 0, 0, 0.3 * k), 2.6 * k, 3.2 * k, 1.0 * k, dk, 10, 6);
                    ellipsoidN(add(e, -sg * 0.2 * k, 0.4 * k, 1.1 * k), 0.8 * k, 0.9 * k, 0.6 * k, new double[]{1, 1, 1}, 8, 5);
                }
                break;
            }
            case 5: face(col("#FFF1E0"), dk, dk, true, 1.15); break;
            case 6: face(col("#FFFFFF"), pink, dk, true, 0.85); break;
            case 7: face(col("#F5E6D3"), dk, dk, false, 1.0); break;
            case 8: face(col("#FFF1E0"), pink, dk, true, 0.9); break;
            case 3: face(col("#D8B58C"), dk, dk, false, 1.15); break;          // kapibara: moncong tumpul cokelat muda
            case 11: eyesOnly(dk); break;                                       // ayam: paruh dibuat di extra()
            case 13: face(col("#F4E8DA"), col("#C98B8B"), dk, false, 0.85); break;   // domba
            case 16: face(col("#E6E6E6"), dk, dk, false, 1.1); break;           // zebra
            case 21: face(col("#F3E1C8"), dk, dk, false, 1.15); break;          // kuda
            case 22: face(col("#FFC9D4"), col("#D6798F"), dk, false, 1.3); break;   // sapi
            case 24: face(col("#E2C6A0"), dk, dk, false, 1.0); break;
            default: break;
        }
    }

    private void make(int a) {
        if (!used(a)) a = 0;
        final double ev = hij ? 0.8 : 1.0;   // panjang telinga kelinci
        double[] main = col(MAIN[a]), acc = col(ACC[a]), white = {1, 1, 1}, inner = col("#FFE0B2");
        shell(main);
        if (a == 3 || a == 11 || a >= 13) { extra(a, main, acc); faceFor(a); return; }
        for (int sg = -1; sg <= 1; sg += 2) {
            switch (a) {
                case 0: // singa: telinga bulat + surai
                    roundEar(sg * 42, 0.86, 4.6 * k, acc, col("#F3D9A4"));
                    ball(pa(sg * 84, 0.45, 0.4 * k), 3.4 * k, acc);
                    ball(pa(sg * 86, 0.0, 0.4 * k), 3.4 * k, acc);
                    ball(pa(sg * 72, 0.78, 0.4 * k), 3.0 * k, acc);
                    for (int bw = 0; bw < 2; bw++)      // surai di belakang kepala (resolusi rendah supaya hemat titik)
                        for (int bs = 0; bs < 3; bs++)
                            ellipsoidN(pa(sg * (115 + bw * 45), 0.5 - bs * 0.45, 0.4 * k), 3.6 * k, 3.6 * k, 3.6 * k, bs == 1 ? sh(acc, 0.85) : acc, 8, 5);
                    ball(pa(sg * 80, -0.42, 0.4 * k), 3.0 * k, acc);
                    break;
                case 1: // harimau
                    roundEar(sg * 40, 0.86, 5.2 * k, main, col("#FFE9C7"));
                    stripeSide(sg, 0.1, 68, 98, acc);
                    stripeSide(sg, -0.25, 68, 98, acc);
                    stripeSide(sg, 0.6, 100, 172, acc);   // belang menyambung ke belakang
                    stripeSide(sg, 0.3, 100, 172, acc);
                    stripeSide(sg, -0.05, 100, 172, acc);
                    break;
                case 2: // panda
                    roundEar(sg * 44, 0.85, 5.6 * k, acc, col("#5A5A5A"));
                    ellipsoid(pa(sg * 76, 0.0, 0.3), 2.0 * k, 3.2 * k, 1.4 * k, acc);
                    break;
                case 4: { // katak: mata menonjol di atas
                    double[] e = pa(sg * 26, 0.84, 1.0 * k);
                    double[] f = unit(new double[]{0, 0.35, 1});
                    ball(e, 4.4 * k, main);
                    ball(add(e, f[0] * 2.9 * k, f[1] * 2.9 * k, f[2] * 2.9 * k), 3.0 * k, white);
                    ball(add(e, f[0] * 4.7 * k, f[1] * 4.7 * k, f[2] * 4.7 * k), 1.5 * k, new double[]{0.05, 0.05, 0.05});
                    break;
                }
                case 5: // rubah
                    pointEar(sg * 40, 0.84, 11 * k, 4.8 * k, main, inner);
                    ellipsoid(pa(sg * 70, -0.62, 0.3), 3.0 * k, 3.2 * k, 1.5 * k, white);
                    break;
                case 6: { // kelinci
                    double[] b = pa(sg * 28, 0.88, 0);
                    double[][] p = {b, add(b, sg * 1.5 * k, 7.5 * k * ev, -1 * k), add(b, sg * 3 * k, 15 * k * ev, -2.2 * k)};
                    tube(p, new double[]{3.0 * k, 3.1 * k, 2.6 * k}, main, 10);
                    ball(p[2], 2.3 * k, main);
                    double[][] q = {add(b, sg * 0.2 * k, 1.5 * k, 2.1 * k), add(b, sg * 1.4 * k, 7.7 * k * ev, 1.6 * k), add(b, sg * 2.7 * k, 13.8 * k * ev, 0.2 * k)};
                    tube(q, new double[]{1.6 * k, 1.7 * k, 1.3 * k}, acc, 8);
                    break;
                }
                case 7: { // anjing: telinga lipat di samping
                    double[] c = {cx + sg * (RX + 1.0 * k), cy - 0.05 * RY, cz - 0.5};
                    ellipsoid(c, 2.4 * k, 7.6 * k, 4.4 * k, acc);
                    break;
                }
                case 8: // kucing
                    stripeSide(sg, 0.55, 100, 170, acc);
                    stripeSide(sg, 0.2, 100, 170, acc);
                    pointEar(sg * 40, 0.84, 10 * k, 4.6 * k, main, col("#F4A6B0"));
                    break;
                case 9: // unicorn: telinga kecil
                    pointEar(sg * 42, 0.84, 5.5 * k, 3.0 * k, main, col("#F8BBD0"));
                    break;
                case 10: // penguin: pipi putih di sisi
                    ellipsoid(pa(sg * 74, -0.12, 0.3), 1.3 * k, 5.6 * k, 3.4 * k, white);
                    break;
            }
        }
        // aksesori tengah (tidak per sisi)
        if (a == 1 || a == 8) {
            double[] sc = acc;
            stripeMeridian(-11, 0.68, 0.94, sc);
            stripeMeridian(0, 0.7, 0.97, sc);
            stripeMeridian(11, 0.68, 0.94, sc);
        } else if (a == 9) {
            double[] b = pa(0, 0.9, 0);
            cone(add(b, 0, -1 * k, 0), add(b, 0, 11 * k, 2.2 * k), 2.3 * k, col("#FFD54F"));
            int n = 7;                       // surai merah muda di sisi kanan
            double[][] p = new double[n][];
            double[] r = new double[n];
            for (int i = 0; i < n; i++) { p[i] = pa(84, 0.8 - 1.3 * i / (n - 1.0), 0.8 * k); r[i] = 1.9 * k; }
            tube(p, r, acc, 7);
        } else if (a == 10) {
            double[] b = pa(0, 0.62, 0.2);
            cone(b, add(b, 0, -1.4 * k, 5.2 * k), 2.5 * k, col("#FFB300"));
        }
        faceFor(a);
    }

    private void finish() {
        int nv = 0, ni = 0;
        for (float[] a : bp) nv += a.length / 3;
        for (int[] a : bi) ni += a.length;
        pos = new float[nv * 3]; nor = new float[nv * 3]; col = new float[nv * 3]; idx = new int[ni];
        int pv = 0, pi = 0;
        for (int q = 0; q < bp.size(); q++) {
            System.arraycopy(bp.get(q), 0, pos, pv, bp.get(q).length);
            System.arraycopy(bn.get(q), 0, nor, pv, bn.get(q).length);
            System.arraycopy(bc.get(q), 0, col, pv, bc.get(q).length);
            pv += bp.get(q).length;
            System.arraycopy(bi.get(q), 0, idx, pi, bi.get(q).length);
            pi += bi.get(q).length;
        }
        vertCount = nv;
    }
}
