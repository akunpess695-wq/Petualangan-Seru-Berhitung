package com.petualangan.berhitung;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.TimeZone;

/**
 * Data dan catatan kemajuan untuk Sambung Ayat + Kebun Hafalan (Juz 30).
 * Teks ayat dan terjemah dibaca dari assets/quran/juz30.json (teks Kemenag, tidak diubah).
 * Indeks surah 0 = An-Naba' (78) sampai 36 = An-Nas (114).
 */
final class SambungAyat {
    /** Juz yang sedang dipilih (30, 29, atau 28). Juz 30 memakai kunci simpanan lama "sa_". */
    static int juz = 30;
    static int JUMLAH_SURAH = 37;
    private static int pertama = 78;
    /** Satu ayat dianggap "dikuasai" bila dijawab benar di sekian hari yang berbeda. */
    static final int BUTUH_HARI = 3;
    /** Pohon "haus" bila sudah sekian hari tidak disiram (dilatih). */
    static final int HARI_HAUS = 7;

    private static final String[] NAMA30 = {
            "An-Naba'", "An-Nazi'at", "'Abasa", "At-Takwir", "Al-Infithar", "Al-Muthaffifin",
            "Al-Insyiqaq", "Al-Buruj", "Ath-Thariq", "Al-A'la", "Al-Ghasyiyah", "Al-Fajr",
            "Al-Balad", "Asy-Syams", "Al-Lail", "Adh-Dhuha", "Al-Insyirah", "At-Tin",
            "Al-'Alaq", "Al-Qadr", "Al-Bayyinah", "Az-Zalzalah", "Al-'Adiyat", "Al-Qari'ah",
            "At-Takatsur", "Al-'Ashr", "Al-Humazah", "Al-Fil", "Quraisy", "Al-Ma'un",
            "Al-Kautsar", "Al-Kafirun", "An-Nashr", "Al-Lahab", "Al-Ikhlas", "Al-Falaq", "An-Nas"
    };
    private static final String[] NAMA29 = {
            "Al-Mulk", "Al-Qalam", "Al-Haqqah", "Al-Ma'arij", "Nuh", "Al-Jinn",
            "Al-Muzzammil", "Al-Muddatsir", "Al-Qiyamah", "Al-Insan", "Al-Mursalat"
    };
    private static final String[] NAMA28 = {
            "Al-Mujadilah", "Al-Hasyr", "Al-Mumtahanah", "Ash-Shaff", "Al-Jumu'ah",
            "Al-Munafiqun", "At-Taghabun", "Ath-Thalaq", "At-Tahrim"
    };
    static String[] NAMA = NAMA30;

    /** Ganti juz aktif; data dimuat ulang saat load() berikutnya. */
    static synchronized void pilihJuz(int j) {
        juz = j == 29 ? 29 : j == 28 ? 28 : 30;
        NAMA = juz == 29 ? NAMA29 : juz == 28 ? NAMA28 : NAMA30;
        JUMLAH_SURAH = NAMA.length;
        pertama = juz == 29 ? 67 : juz == 28 ? 58 : 78;
        ayat = null;
        terjemah = null;
    }

    /** Berkas data juz ada di assets/quran/juz<N>.json? */
    static boolean ada(Context ctx, int j) {
        try {
            ctx.getAssets().open("quran/juz" + j + ".json").close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private static String[][] ayat;
    private static String[][] terjemah;

    private SambungAyat() {
    }

    static synchronized boolean load(Context ctx) {
        if (ayat != null) {
            return true;
        }
        try {
            InputStream in = ctx.getAssets().open("quran/juz" + juz + ".json");
            ByteArrayOutputStream bo = new ByteArrayOutputStream();
            byte[] buf = new byte[8192];
            int n;
            while ((n = in.read(buf)) > 0) {
                bo.write(buf, 0, n);
            }
            in.close();
            JSONArray root = new JSONArray(new String(bo.toByteArray(), StandardCharsets.UTF_8));
            if (root.length() != JUMLAH_SURAH) {
                return false;
            }
            String[][] a = new String[JUMLAH_SURAH][];
            String[][] t = new String[JUMLAH_SURAH][];
            for (int i = 0; i < JUMLAH_SURAH; i++) {
                JSONObject o = root.getJSONObject(i);
                JSONArray aa = o.getJSONArray("a");
                JSONArray tt = o.getJSONArray("t");
                a[i] = new String[aa.length()];
                t[i] = new String[aa.length()];
                for (int j = 0; j < aa.length(); j++) {
                    a[i][j] = aa.getString(j);
                    t[i][j] = j < tt.length() ? tt.getString(j) : "";
                }
            }
            ayat = a;
            terjemah = t;
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    static int jumlahAyat(int si) {
        return ayat[si].length;
    }

    static int nomorSurah(int si) {
        return pertama + si;
    }

    /** Teks ayat untuk ditampilkan. Tanda penanda ruku' (U+08D6) dibuang karena tidak ada di semua font HP. */
    static String ayat(int si, int ai) {
        return ayat[si][ai].replace("ࣖ", "").trim();
    }

    static String terjemah(int si, int ai) {
        return terjemah[si][ai];
    }

    // ---------- Kemajuan Kebun Hafalan ----------

    static int hariIni() {
        long now = System.currentTimeMillis();
        return (int) ((now + TimeZone.getDefault().getOffset(now)) / 86400000L);
    }

    private static String key(int si, int ai) {
        return (juz == 30 ? "sa_" : "sa" + juz + "_") + si + "_" + ai;
    }

    /** {jumlah hari benar, hari terakhir} */
    private static int[] baca(SharedPreferences p, int si, int ai) {
        try {
            String s = p.getString(key(si, ai), "");
            int k = s.indexOf(':');
            if (k > 0) {
                return new int[]{Integer.parseInt(s.substring(0, k)), Integer.parseInt(s.substring(k + 1))};
            }
        } catch (Exception e) {
            // abaikan data rusak
        }
        return new int[]{0, 0};
    }

    /** Dipanggil saat murid menjawab benar untuk ayat ke-ai. Hanya menambah bila harinya baru. */
    static void benar(SharedPreferences p, int si, int ai) {
        int[] r = baca(p, si, ai);
        int h = hariIni();
        if (r[1] == h) {
            return;
        }
        p.edit().putString(key(si, ai), Math.min(99, r[0] + 1) + ":" + h).apply();
    }

    static boolean dikuasai(SharedPreferences p, int si, int ai) {
        return baca(p, si, ai)[0] >= BUTUH_HARI;
    }

    static int jumlahDikuasai(SharedPreferences p, int si) {
        int m = 0;
        for (int ai = 0; ai < ayat[si].length; ai++) {
            if (dikuasai(p, si, ai)) {
                m++;
            }
        }
        return m;
    }

    /** Hari terakhir surah ini dilatih (0 bila belum pernah). */
    static int hariTerakhir(SharedPreferences p, int si) {
        int last = 0;
        for (int ai = 0; ai < ayat[si].length; ai++) {
            last = Math.max(last, baca(p, si, ai)[1]);
        }
        return last;
    }

    static boolean pernahLatih(SharedPreferences p, int si) {
        return hariTerakhir(p, si) > 0;
    }
}
