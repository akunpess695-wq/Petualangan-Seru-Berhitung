package com.petualangan.berhitung;

import android.app.Activity;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.AlertDialog;
import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Vibrator;
import android.view.MotionEvent;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.LinearGradient;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.InputFilter;
import android.text.TextPaint;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

public class MainActivity extends Activity {

    private static final String APP_NAME = "Salamuda Quest";
    private static final String BANK_FILE = "bank.json";
    private static final int REQ_IMPORT = 41;
    private static final int REQ_BACKUP_SAVE = 42;
    private static final int REQ_BACKUP_OPEN = 43;
    private static final String APP_VERSION = "6.29.0";
    // Tahap E: putra dan putri di Toko Avatar memakai 3D sungguhan (PutriGLView). false = kembali ke AvatarView lama.
    private static final boolean USE_PUTRI_GL = true;
    private PutriGLView putriGL;
    private static final String STATS_KEY = "laporan_v1";
    private static final int STATS_MAX_ITEMS = 500;
    private static final String[] OP_NAME = {"Tambah", "Kurang", "Kali", "Bagi", "Campur"};
    private static final int TOTAL = 10; // Peta Petualangan dan latihan tabel selalu 10 soal
    private static final int MAX_Q = 20;
    // Jumlah soal per permainan menurut kelas (Peta Petualangan tetap 10)
    private static final int[] KELAS_Q = {10, 10, 15, 15, 20, 20};
    // Waktu per soal bawaan untuk mode berwaktu (detik). Guru tetap bisa mengubahnya.
    private static final int[] KELAS_SECS = {20, 15, 12, 12, 10, 10};
    private static final int MAX_PLAYERS = 4;
    private static final int WRAP = ViewGroup.LayoutParams.WRAP_CONTENT;
    private static final int MATCH = ViewGroup.LayoutParams.MATCH_PARENT;

    private static final int ORANGE = Color.parseColor("#FB8C00");
    private static final int ORANGE_DARK = Color.parseColor("#E65100");
    private static final int GREEN = Color.parseColor("#43A047");
    private static final int RED = Color.parseColor("#E53935");
    private static final int BLUE = Color.parseColor("#1E88E5");
    private static final int PURPLE = Color.parseColor("#8E24AA");
    private static final int TEAL = Color.parseColor("#00897B");
    private static final int PINK = Color.parseColor("#D81B60");
    private static final int DARK = Color.parseColor("#4E342E");
    private static final int GRAY = Color.parseColor("#ECEFF1");
    private static final int GRAY_D = Color.parseColor("#CFD8DC");
    private static final int GOLD = Color.parseColor("#FFB300");
    private static final int SILVER = Color.parseColor("#90A4AE");
    private static final int BRONZE = Color.parseColor("#A1887F");
    private static final int INDIGO = Color.parseColor("#5C6BC0");
    // Token desain premium: permukaan hangat, tinta lembut, dan aksen emas.
    private static final int CREAM = Color.parseColor("#FFF8EC");
    private static final int CREAM_DEEP = Color.parseColor("#FFEFD6");
    private static final int INK = Color.parseColor("#2E241F");
    private static final int INK_SOFT = Color.parseColor("#866F63");
    private static final int GOLD_DEEP = Color.parseColor("#C58A1A");
    private static final int HAIRLINE = Color.argb(64, 197, 138, 26);

    // Tingkat kesulitan = kelas SD (indeks 0..5 untuk kelas 1..6)
    private static final String[] KELAS_LABEL = {"1", "2", "3", "4", "5", "6"};
    private static final String[] KELAS_NAMES = {
            "Kelas 1", "Kelas 2", "Kelas 3", "Kelas 4", "Kelas 5", "Kelas 6"
    };
    private static final String[] KELAS_DESC = {
        "Sampai 10 \u2022 tambah dan kurang",
        "Sampai 10 \u2022 tambah dan kurang",
        "Sampai 100\u2013500 \u2022 kali-bagi sampai 70",
        "Sampai 500\u20131.000 \u2022 kali-bagi sampai 100",
        "Sampai 1.000\u201310.000 \u2022 kali-bagi 2 angka",
        "Sampai 10.000\u2013100.000 \u2022 kali-bagi 3 angka"
    };
    private static final int[] KELAS_COLOR = {GREEN, TEAL, BLUE, PURPLE, ORANGE, RED};
    private static final int[] KELAS_LIVES = {5, 5, 5, 5, 5, 5};
    // Penjumlahan dan pengurangan: batas bilangan di soal 1 dan di soal 10
    private static final int[] ADD_LO = {5, 7, 100, 500, 1000, 10000};
    private static final int[] ADD_HI = {10, 10, 500, 1000, 10000, 100000};
    // Isi Titik dan 3 Bilangan: batas bilangan di soal 1 dan soal 10 (Kelas 1-2 lebih ringan)
    private static final int[] EASY_LO = {6, 7, 100, 500, 1000, 10000};
    private static final int[] EASY_HI = {10, 10, 500, 1000, 10000, 100000};
    // Perkalian dan pembagian: faktor a dan b; batas atas naik dari soal 1 ke soal 10
    private static final int[] MUL_A_LO = {1, 2, 2, 2, 11, 101};
    private static final int[] MUL_A_S = {2, 3, 4, 6, 20, 200};
    private static final int[] MUL_A_E = {3, 5, 7, 10, 99, 500};
    private static final int[] MUL_B_LO = {1, 1, 2, 2, 2, 11};
    private static final int[] MUL_B_S = {2, 5, 6, 8, 5, 20};
    private static final int[] MUL_B_E = {6, 10, 10, 10, 30, 50};
    // Soal 3 bilangan Kelas 5-6: faktor kecil x, y dan bilangan z (naik dari soal 1 ke soal 10)
    private static final int[] X_S = {0, 0, 0, 0, 4, 10};
    private static final int[] X_E = {0, 0, 0, 0, 12, 25};
    private static final int[] Y_S = {0, 0, 0, 0, 3, 5};
    private static final int[] Y_E = {0, 0, 0, 0, 9, 15};
    private static final int[] Z_S = {0, 0, 0, 0, 50, 500};
    private static final int[] Z_E = {0, 0, 0, 0, 1000, 6000};
    private static final int[] PLAYER_COLORS = {BLUE, PINK, GREEN, PURPLE};
    // ---------- Pasangan bawaan untuk game Cocokkan Pasangan (buatan Claude, perlu dicek guru) ----------
    private static final String[][] M_ID_1 = {
            {"besar", "kecil"},
            {"panjang", "pendek"},
            {"tinggi", "rendah"},
            {"panas", "dingin"},
            {"siang", "malam"},
            {"naik", "turun"},
            {"buka", "tutup"},
            {"masuk", "keluar"},
            {"terang", "gelap"},
            {"banyak", "sedikit"}
    };
    private static final String[][] M_ID_2 = {
            {"rajin", "malas"},
            {"bersih", "kotor"},
            {"kuat", "lemah"},
            {"cepat", "lambat"},
            {"kaya", "miskin"},
            {"jauh", "dekat"},
            {"tebal", "tipis"},
            {"basah", "kering"},
            {"berani", "takut"},
            {"gemuk", "kurus"}
    };
    private static final String[][] M_ID_3 = {
            {"pandai", "pintar"},
            {"cantik", "indah"},
            {"murid", "siswa"},
            {"ayah", "bapak"},
            {"ibu", "bunda"},
            {"cepat", "lekas"},
            {"senang", "gembira"},
            {"sedih", "pilu"},
            {"kecil", "mungil"},
            {"marah", "geram"}
    };
    private static final String[][] M_EN_1 = {
            {"\uD83D\uDC31", "cat"},
            {"\uD83D\uDC36", "dog"},
            {"\uD83C\uDF4E", "apple"},
            {"\uD83C\uDF4C", "banana"},
            {"\uD83D\uDC1F", "fish"},
            {"\uD83D\uDC26", "bird"},
            {"\uD83D\uDC2E", "cow"},
            {"\uD83D\uDC38", "frog"},
            {"\uD83C\uDF19", "moon"},
            {"\uD83C\uDF33", "tree"},
            {"\uD83C\uDFE0", "house"},
            {"\uD83D\uDCD6", "book"},
            {"\uD83D\uDE97", "car"},
            {"\u2B50", "star"}
    };
    private static final String[][] M_EN_2 = {
            {"red", "merah"},
            {"blue", "biru"},
            {"green", "hijau"},
            {"yellow", "kuning"},
            {"white", "putih"},
            {"black", "hitam"},
            {"one", "satu"},
            {"two", "dua"},
            {"three", "tiga"},
            {"four", "empat"}
    };
    private static final String[][] M_EN_3 = {
            {"mother", "ibu"},
            {"father", "ayah"},
            {"teacher", "guru"},
            {"student", "murid"},
            {"school", "sekolah"},
            {"water", "air"},
            {"chair", "kursi"},
            {"table", "meja"},
            {"door", "pintu"},
            {"window", "jendela"},
            {"eat", "makan"},
            {"sleep", "tidur"}
    };

    // Pasangan bergambar (gambar ke kata), buatan Claude, perlu dicek guru.
    // Dipakai bergantian dengan pasangan kata biasa di Cocokkan Pasangan (bukan di Peta Petualangan).
    private static final String[][] M_ID_1G = {
            {"\uD83D\uDC31", "kucing"},
            {"\uD83D\uDC36", "anjing"},
            {"\uD83D\uDC26", "burung"},
            {"\uD83D\uDC1F", "ikan"},
            {"\uD83D\uDC14", "ayam"},
            {"\uD83D\uDC04", "sapi"},
            {"\uD83D\uDC38", "katak"},
            {"\uD83D\uDC30", "kelinci"},
            {"\uD83C\uDF4C", "pisang"},
            {"\uD83C\uDF4E", "apel"},
            {"\uD83C\uDF49", "semangka"},
            {"\uD83C\uDF47", "anggur"},
            {"\uD83C\uDF4A", "jeruk"},
            {"\uD83C\uDFE0", "rumah"},
            {"\uD83D\uDE97", "mobil"},
            {"\uD83D\uDCD6", "buku"},
            {"\u26BD", "bola"},
            {"\uD83C\uDF19", "bulan"},
            {"\uD83C\uDF33", "pohon"}
    };
    private static final String[][] M_EN_2G = {
            {"\uD83D\uDC40", "eyes"},
            {"\uD83D\uDC42", "ear"},
            {"\uD83D\uDC43", "nose"},
            {"\uD83D\uDC44", "mouth"},
            {"\u270B", "hand"},
            {"\uD83E\uDDB6", "foot"},
            {"\uD83E\uDDB7", "tooth"},
            {"\uD83C\uDF92", "bag"},
            {"\u270F\uFE0F", "pencil"},
            {"\u2702\uFE0F", "scissors"},
            {"\uD83D\uDCCF", "ruler"},
            {"\uD83D\uDCDA", "books"},
            {"\uD83D\uDD14", "bell"},
            {"\u23F0", "clock"}
    };
    private static final String[][] M_EN_3G = {
            {"\uD83C\uDF5E", "bread"},
            {"\uD83E\uDD5A", "egg"},
            {"\uD83C\uDF5A", "rice"},
            {"\uD83E\uDD5B", "milk"},
            {"\uD83D\uDE8C", "bus"},
            {"\uD83D\uDEB2", "bicycle"},
            {"\u26F5", "boat"},
            {"\u2708\uFE0F", "plane"},
            {"\u2600\uFE0F", "sun"},
            {"\uD83C\uDF08", "rainbow"},
            {"\uD83C\uDF3B", "flower"},
            {"\uD83D\uDC18", "elephant"},
            {"\uD83D\uDC12", "monkey"},
            {"\uD83D\uDC2F", "tiger"}
    };

    // Indeks 0..24 = slot tudung (12 = tanpa tudung). Nomor slot lama dipertahankan supaya pilihan tersimpan tetap aman;
    // 17..20 tidak dipakai lagi (lihat HOOD_ALIAS).
    private static final String[] AVATARS = {
            "\uD83E\uDD81", "\uD83D\uDC2F", "\uD83D\uDC3C", "\uD83D\uDC39", "\uD83D\uDC38", "\uD83E\uDD8A",
            "\uD83D\uDC30", "\uD83D\uDC36", "\uD83D\uDC31", "\uD83E\uDD84", "\uD83D\uDC27", "\uD83D\uDC14",
            "\uD83D\uDE42",
            // 13 domba, 14 buaya, 15 gajah, 16 zebra, 17..20 alias, 21 kuda, 22 sapi, 23 kura-kura, 24 beruang
            "\uD83D\uDC11", "\uD83D\uDC0A", "\uD83D\uDC18", "\uD83E\uDD93",
            "\uD83D\uDC14", "\uD83D\uDC39", "\uD83D\uDC3B", "\uD83D\uDC14",
            "\uD83D\uDC34", "\uD83D\uDC2E", "\uD83D\uDC22", "\uD83D\uDC3B"
    };
    private final int[] avatar = new int[MAX_PLAYERS];
    // 0 = putra, 1 = putri (dipakai avatar 3D)
    private final int[] genderOf = new int[MAX_PLAYERS];

    private String av(int i) {
        return AVATARS[Math.max(0, Math.min(AVATARS.length - 1, avatar[i]))];
    }

    private static final String[] PLAYER_ICON = {
            "\uD83D\uDC31", "\uD83D\uDC36", "\uD83D\uDC30", "\uD83D\uDC3C"
    };
    private static final int[] OPT_COLORS = {BLUE, PURPLE, TEAL, PINK};
    // Lencana (penghargaan): ikon, nama, syarat, warna
    private static final String[] BADGE_ICON = {
            "\uD83C\uDF31", "\u2B50", "\uD83D\uDCAF", "\u26A1", "\uD83D\uDD25",
            "\uD83C\uDF1F", "\uD83C\uDFC6", "\uD83E\uDDE9", "\uD83D\uDC65",
            "\uD83D\uDDFA\uFE0F", "\uD83C\uDFF0", "\uD83D\uDC8E"
    };
    private static final String[] BADGE_NAME = {
            "Pemula", "Bintang Tiga", "Sempurna", "Kilat", "Rajin",
            "Kolektor Bintang", "Pemecah Rekor", "Pasangan Hebat", "Teman Main",
            "Penjelajah", "Penakluk Dunia", "Bintang Penuh"
    };
    private static final String[] BADGE_DESC = {
            "Selesaikan 1 permainan",
            "Dapat 3 bintang dalam satu permainan",
            "Jawab semua soal dengan benar",
            "Jawab semua benar saat berwaktu",
            "Selesaikan 10 permainan",
            "Kumpulkan 30 bintang",
            "Pecahkan rekor skormu sendiri",
            "Cocokkan semua pasangan tanpa salah",
            "Main bersama teman",
            "Selesaikan 5 level di Peta Petualangan",
            "Selesaikan semua level di satu dunia",
            "Raih 3 bintang di semua level satu dunia"
    };
    private static final int[] BADGE_COLOR = {GREEN, GOLD, PINK, ORANGE, RED, INDIGO, PURPLE, TEAL, BLUE,
            ORANGE_DARK, TEAL, INDIGO};
    // Huruf bulat yang ramah anak (Android 9 ke atas); di HP lama otomatis memakai huruf biasa
    private float ui = 1f;
    private static final Typeface ROUND_BOLD = Typeface.create("sans-serif-rounded", Typeface.BOLD);
    // Mata pelajaran: 0 Matematika, 1 Bahasa Indonesia, 2 Bahasa Inggris
    private static final String[] SUBJ_NAMES = {"Matematika", "Bahasa Indonesia", "Bahasa Inggris"};
    private static final String[] SUBJ_ICON = {"\uD83D\uDD22", "\uD83D\uDCD6", "\uD83C\uDF0D"};
    private static final int[] SUBJ_COLOR = {ORANGE, TEAL, BLUE};
    // Warna kartu pelajaran di menu utama (v6.17.0): gelap secukupnya agar teks putih terbaca (kontras >= 4,5:1).
    // Matematika = tanah liat, Indonesia = daun, Inggris = air.
    private static final int[] MENU_SUBJ_COLOR = {Color.parseColor("#BF360C"),
            Color.parseColor("#33691E"), Color.parseColor("#00695C")};

    // Batas waktu per soal yang bisa diatur (detik)
    private static final int SECS_MIN = 3;
    private static final int SECS_MAX = 120;

    // Efek suara dibuat dari nada sederhana: {frekuensi Hz, lama ms}; 0 Hz = diam
    private static final int SFX_RATE = 22050;
    private static final int SFX_TAP = 0;
    private static final int SFX_CORRECT = 1;
    private static final int SFX_WRONG = 2;
    private static final int SFX_TIMEOUT = 3;
    private static final int SFX_TICK = 4;
    private static final int SFX_WIN = 5;
    private static final int SFX_RECORD = 6;
    private static final int SFX_LOSE = 7;
    private static final int SFX_OK = 8;
    private static final int[][][] SFX_NOTES = {
        {{880, 22}},
        {{784, 80}, {1047, 170}},
        {{233, 140}, {175, 240}},
        {{262, 110}, {0, 30}, {196, 280}},
        {{1300, 28}},
        {{523, 100}, {659, 100}, {784, 100}, {1047, 340}},
        {{523, 80}, {659, 80}, {784, 80}, {1047, 80}, {784, 60}, {1047, 80}, {1319, 420}},
        {{392, 170}, {330, 170}, {262, 400}},
        {{523, 110}, {659, 110}, {784, 280}}
    };
    private static final int[] CONFETTI_COLORS = {ORANGE, GREEN, BLUE, PURPLE, PINK, GOLD, RED, TEAL};

    private static class Q {
        String text;
        String full;
        String[] sopts;
        String expl;
        int answer;
        int correctIdx;
        int trap = -1;
        String key = "";
        final int[] opts = new int[4];
    }

    // Soal dari guru (pilihan ganda)
    private static class B {
        int subj;
        int kelas;
        String text;
        final String[] opt = new String[4];
        int key;
        String expl = "";
    }

    private interface Pick {
        void on(int i);
    }

    // Hujan confetti di layar hasil
    private static class Confetti extends View {
        private final int n;
        private final float[] x;
        private final float[] y;
        private final float[] vx;
        private final float[] vy;
        private final float[] size;
        private final float[] rot;
        private final float[] vrot;
        private final int[] color;
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Random r = new Random();
        private final float density;
        private boolean ready;
        private long start;
        private long last;

        Confetti(Context ctx, int count) {
            super(ctx);
            n = count;
            x = new float[n];
            y = new float[n];
            vx = new float[n];
            vy = new float[n];
            size = new float[n];
            rot = new float[n];
            vrot = new float[n];
            color = new int[n];
            density = ctx.getResources().getDisplayMetrics().density;
        }

        @Override
        protected void onDraw(Canvas c) {
            int w = getWidth();
            int h = getHeight();
            if (w == 0 || h == 0) {
                postInvalidateOnAnimation();
                return;
            }
            long now = System.nanoTime();
            if (!ready) {
                ready = true;
                start = now;
                last = now;
                for (int i = 0; i < n; i++) {
                    x[i] = r.nextFloat() * w;
                    y[i] = -r.nextFloat() * h * 0.5f;
                    vx[i] = (r.nextFloat() - 0.5f) * w * 0.3f;
                    vy[i] = h * (0.22f + r.nextFloat() * 0.3f);
                    size[i] = density * (5f + r.nextFloat() * 6f);
                    rot[i] = r.nextFloat() * 360f;
                    vrot[i] = (r.nextFloat() - 0.5f) * 720f;
                    color[i] = CONFETTI_COLORS[r.nextInt(CONFETTI_COLORS.length)];
                }
            }
            float dt = (now - last) / 1e9f;
            if (dt > 0.05f) {
                dt = 0.05f;
            }
            last = now;
            boolean alive = false;
            for (int i = 0; i < n; i++) {
                x[i] += vx[i] * dt;
                y[i] += vy[i] * dt;
                vx[i] *= (1f - 0.4f * dt);
                rot[i] += vrot[i] * dt;
                if (y[i] < h + size[i]) {
                    alive = true;
                }
                if (y[i] < -size[i] || y[i] > h + size[i]) {
                    continue;
                }
                paint.setColor(color[i]);
                c.save();
                c.rotate(rot[i], x[i], y[i]);
                c.drawRect(x[i] - size[i] / 2f, y[i] - size[i] / 3f,
                        x[i] + size[i] / 2f, y[i] + size[i] / 3f, paint);
                c.restore();
            }
            if (alive && now - start < 7000000000L) {
                postInvalidateOnAnimation();
            } else {
                final ViewGroup p = (ViewGroup) getParent();
                if (p != null) {
                    post(() -> p.removeView(this));
                }
            }
        }
    }

    private final Random rnd = new Random();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private SharedPreferences prefs;
    private boolean sfxOn = true;
    // Belajar santai: nyawa tidak berkurang dan skor tidak masuk rekor
    private boolean relaxed = false;
    // Lencana yang baru didapat di permainan yang baru selesai
    private ArrayList<Integer> freshBadges = new ArrayList<>();
    private Vibrator vib;
    private final AudioTrack[] sfxTracks = new AudioTrack[SFX_NOTES.length];

    // Pengaturan
    private int screen;
    private int mode;
    private int kelas = 0;
    private boolean timed = false;
    private int secs = 10;
    private int np = 1;
    private final String[] names = new String[MAX_PLAYERS];
    private EditText[] nameFields;

    // Hasil per pemain
    private final Q[] qs = new Q[MAX_Q];
    private final int[] pScore = new int[MAX_PLAYERS];
    private final int[] pCorrect = new int[MAX_PLAYERS];
    // Catatan tiap soal per pemain: 0 belum dijawab, 1 benar, 2 salah, 3 waktu habis
    private final int[][] pRes = new int[MAX_PLAYERS][MAX_Q];
    private final int[][] pPick = new int[MAX_PLAYERS][MAX_Q];
    private boolean rekorNow;
    private int reviewPlayer;
    private final ArrayList<B> bank = new ArrayList<>();
    private String importMsg = "";
    // Laporan guru: hitungan benar/salah per topik dan per soal (disimpan di prefs laporan_v1)
    private final HashMap<String, int[]> stTopic = new HashMap<>();
    private final HashMap<String, int[]> stItem = new HashMap<>();
    private final HashMap<String, String> stText = new HashMap<>();
    private long stSince = 0L;
    private int stTotal = 0;
    private boolean stDirty = false;
    private int rpSubj = -1;
    private int rpKelas = -1;
    private String pendingBackup = "";
    private String pendingRestore = "";
    private int total = TOTAL;
    private final ArrayList<String> lastKeys = new ArrayList<>();
    private int subj = 0;
    private int cur;

    // Giliran berjalan
    private int qi;
    private int score;
    private int correct;
    private int lives;
    private int timeLeft;
    private boolean locked;
    private boolean gamePaused;
    private boolean resumeNext;
    private boolean dialogOpen;
    private AlertDialog exitDialog;
    private AlertDialog menuDialog;
    private AlertDialog pickDialog;

    // Cocokkan Pasangan
    private String[][] matchPairs;
    private int matchSubj;
    private int matchN;
    private int matchDone;
    private int matchWrong;
    private int selL = -1;
    private int selR = -1;
    private boolean matchBusy;
    private boolean[] matchedFlag;
    private TextView[] cardL;
    private TextView[] cardR;
    private TextView tvMatchInfo;
    private String matchHint = "";

    // Tampilan permainan
    private TextView tvStep;
    private TextView tvScore;
    private TextView tvHearts;
    private TextView tvTimer;
    private TextView tvQuestion;
    private TextView tvFeedback;
    private FrameLayout timerBox;
    private View timerFill;
    private LinearLayout cardQ;
    private final View[] dots = new View[MAX_Q];
    private final Button[] opts = new Button[4];
    private Button correctBtn;

    private final Runnable tick = new Runnable() {
        @Override
        public void run() {
            if (locked) {
                return;
            }
            timeLeft--;
            if (timeLeft <= 0) {
                timeout();
            } else {
                showTime();
                if (timeLeft <= 3) {
                    sfx(SFX_TICK);
                    pop(tvTimer, 1.25f);
                }
                handler.postDelayed(this, 1000);
            }
        }
    };

    // =====================================================================
    // Tampilan menurut perangkat (versi 6.0.2): HP, tablet/panel, TV
    // =====================================================================

    // 1 = HP, 2 = tablet/panel, 3 = TV (hasil gabungan pilihan guru dan deteksi perangkat)
    private int tampilProfile = 1;
    private static final String[] TAMPIL_NAMES = {"Otomatis", "HP", "Tablet / Panel", "TV"};

    // Profil otomatis. Layar 960x540dp (panel atau TV Full HD) juga dianggap besar,
    // padahal lebar terkecilnya hanya 540dp; HP paling tinggi sekitar 430dp pada sisi pendeknya.
    static int autoProfile(boolean tv, int smallestDp, int widthDp, int heightDp) {
        if (tv) {
            return 3;
        }
        int mx = Math.max(widthDp, heightDp);
        int mn = Math.min(widthDp, heightDp);
        if (smallestDp >= 600 || (mx >= 900 && mn >= 500)) {
            return 2;
        }
        return 1;
    }

    static float clampF(float v, float lo, float hi) {
        return Math.max(lo, Math.min(hi, v));
    }

    // Faktor pembesaran huruf dan tombol supaya terbaca dari jauh
    static float profileUi(int profile, int widthDp, int heightDp) {
        if (profile == 1) {
            return 1f;
        }
        if (profile == 2) {
            boolean landscape = widthDp * 10 >= heightDp * 13;
            return landscape ? clampF(heightDp / 600f, 1f, 1.6f) : clampF(heightDp / 700f, 1f, 1.5f);
        }
        return clampF(heightDp / 560f, 1.2f, 1.8f);
    }

    // Layar lebar dan mendatar: menu dan layar soal dibuat dua kolom
    static boolean wideLayout(int widthPx, int heightPx, int minWidthPx) {
        return widthPx >= minWidthPx && widthPx * 10 >= heightPx * 13;
    }

    // Profil HP selalu satu kolom; tablet/panel dan TV memakai dua kolom bila layarnya lebar
    static boolean wideFor(int profile, int widthPx, int heightPx, int minWidthPx) {
        return profile != 1 && wideLayout(widthPx, heightPx, minWidthPx);
    }

    private boolean isTvDevice() {
        try {
            android.app.UiModeManager um = (android.app.UiModeManager) getSystemService(Context.UI_MODE_SERVICE);
            if (um != null && um.getCurrentModeType() == android.content.res.Configuration.UI_MODE_TYPE_TELEVISION) {
                return true;
            }
        } catch (Exception e) {
            // lanjut ke pemeriksaan berikutnya
        }
        try {
            return getPackageManager().hasSystemFeature("android.software.leanback");
        } catch (Exception e) {
            return false;
        }
    }

    private void applyDisplayProfile() {
        android.content.res.Configuration cf = getResources().getConfiguration();
        int p = prefs.getInt("tampil", 0);
        if (p < 1 || p > 3) {
            p = autoProfile(isTvDevice(), cf.smallestScreenWidthDp, cf.screenWidthDp, cf.screenHeightDp);
        }
        tampilProfile = p;
        ui = profileUi(p, cf.screenWidthDp, cf.screenHeightDp);
        setRequestedOrientation(p == 1 ? android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                : (p == 2 ? android.content.pm.ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                : android.content.pm.ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE));
    }

    private void setScreen(int s) {
        stopSurahAudio();
        stopMic();
        screen = s;
        if (s == 0) {
            int p = tampilProfile;
            setRequestedOrientation(p == 1 ? android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                    : (p == 2 ? android.content.pm.ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                    : android.content.pm.ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE));
        } else {
            // Layar permainan tidak dibangun ulang saat rotasi; kunci ke arah saat ini.
            setRequestedOrientation(android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LOCKED);
        }
    }

    @Override
    public void onConfigurationChanged(android.content.res.Configuration nc) {
        super.onConfigurationChanged(nc);
        if (prefs == null) {
            return;
        }
        if (screen != 0) {
            setRequestedOrientation(android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LOCKED);
            return;
        }
        applyDisplayProfile();
        boolean dialogUp = (menuDialog != null && menuDialog.isShowing())
                || (pickDialog != null && pickDialog.isShowing());
        if (!dialogUp) {
            showMenu();
        }
    }

    private boolean isWide() {
        android.util.DisplayMetrics m = getResources().getDisplayMetrics();
        return wideFor(tampilProfile, m.widthPixels, m.heightPixels, dp(720));
    }

    // Dua kolom berdampingan di tengah layar (dipakai menu dan layar soal pada layar lebar)
    private void wrapTwo(LinearLayout left, LinearLayout right) {
        ScrollView sv = new ScrollView(this);
        sv.setFillViewport(true);
        sv.setBackground(background());
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.HORIZONTAL);
        root.setGravity(Gravity.CENTER_VERTICAL);
        root.addView(left, new LinearLayout.LayoutParams(0, WRAP, 1f));
        root.addView(right, new LinearLayout.LayoutParams(0, WRAP, 1f));
        FrameLayout fl = new FrameLayout(this);
        int maxW = Math.min(getResources().getDisplayMetrics().widthPixels, dp(1240));
        fl.addView(root, new FrameLayout.LayoutParams(maxW, WRAP, Gravity.CENTER));
        sv.addView(fl);
        setContentView(sv);
        sv.setAlpha(0f);
        sv.setTranslationY(dp(12));
        sv.animate().alpha(1f).translationY(0f).setDuration(200)
                .setInterpolator(new DecelerateInterpolator()).start();
    }

    private void dialogTampilan() {
        LinearLayout b = dialogBody();
        int cur = prefs.getInt("tampil", 0);
        for (int i = 0; i < TAMPIL_NAMES.length; i++) {
            final int idx = i;
            boolean on = (i == cur) || (i == 0 && (cur < 1 || cur > 3));
            Button t = button(TAMPIL_NAMES[i], on ? PURPLE : GRAY, 16);
            t.setTextColor(on ? Color.WHITE : DARK);
            flat(t);
            t.setOnClickListener(v -> {
                prefs.edit().putInt("tampil", idx).apply();
                if (menuDialog != null) {
                    menuDialog.setOnDismissListener(null);
                    menuDialog.dismiss();
                }
                recreate();
            });
            b.addView(t, lp(dp(50), i == 0 ? 0 : 8));
        }
        boolean auto = cur < 1 || cur > 3;
        TextView info = text("Sekarang: " + TAMPIL_NAMES[tampilProfile] + (auto ? " (otomatis)" : " (pilihan manual)")
                + "\nOtomatis mengikuti perangkat. Pilih manual kalau tampilannya kurang pas. "
                + "Aplikasi akan dimulai ulang.", 13, DARK, false);
        info.setGravity(Gravity.START);
        b.addView(info, lp(WRAP, 10));
        showDialogView("Tampilan layar", b);
    }

    // v6.21.0: satu pintu untuk hal yang bukan untuk anak dan jarang dipakai
    private void dialogPengaturan() {
        LinearLayout b = dialogBody();
        Button tp = button("\uD83D\uDCFA Tampilan layar", PURPLE, 16);
        flat(tp);
        tp.setContentDescription("Tampilan layar, sekarang " + TAMPIL_NAMES[tampilProfile]);
        tp.setOnClickListener(v -> dialogTampilan());
        b.addView(tp, lp(dp(52), 0));
        Button gr = button((hasPin() ? "\uD83D\uDD12" : "\uD83D\uDCDA") + " Soal Guru", Color.parseColor("#5C6BC0"), 16);
        flat(gr);
        gr.setOnClickListener(v -> {
            if (menuDialog != null) {
                menuDialog.setOnDismissListener(null);
                menuDialog.dismiss();
            }
            openGuru();
        });
        b.addView(gr, lp(dp(52), 10));
        if (prefs != null && !prefs.getBoolean("nagaPakai", false)) {
            Button ng = button("\uD83D\uDC09 Tampilkan naga", Color.parseColor("#E53935"), 16);
            flat(ng);
            ng.setOnClickListener(v -> {
                prefs.edit().putBoolean("nagaPakai", true).apply();
                syncNaga(true);
                Toast.makeText(this, "Naga muncul lagi!", Toast.LENGTH_SHORT).show();
                if (menuDialog != null) menuDialog.dismiss();
            });
            b.addView(ng, lp(dp(52), 10));
        }
        b.addView(text(hasPin() ? "Soal Guru dikunci PIN." : "Soal Guru belum diberi PIN.", 13, DARK, false),
                lp(WRAP, 8));
        showDialogView("Pengaturan", b);
    }

    // v6.21.0: pemain, waktu, dan nyawa digabung menjadi satu pintu "Cara main"
    private void dialogCaraMain() {
        LinearLayout b = dialogBody();
        Button pm = button("\uD83D\uDC65 Pemain: " + (np == 1 ? "Sendiri" : np + " pemain"), PLAYER_COLORS[0], 16);
        flat(pm);
        pm.setOnClickListener(v -> dialogPemain());
        b.addView(pm, lp(dp(52), 0));
        Button wk = button("\u23F1\uFE0F Waktu: " + (timed ? secsNow() + " detik" : "Bebas"), PURPLE, 16);
        flat(wk);
        wk.setOnClickListener(v -> dialogWaktu());
        b.addView(wk, lp(dp(52), 10));
        Button ny = button(relaxed ? "\uD83D\uDC9A Santai, tanpa nyawa" : "\u2764\uFE0F Ada nyawa", relaxed ? GREEN : RED, 16);
        flat(ny);
        ny.setOnClickListener(v -> dialogNyawa());
        b.addView(ny, lp(dp(52), 10));
        showDialogView("Cara main", b);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = getSharedPreferences("berhitung", MODE_PRIVATE);
        // HP tetap tegak; tablet/panel mengikuti arah layar; TV mendatar. Huruf dan tombol
        // diperbesar sesuai tinggi layar agar terbaca dari belakang kelas (lihat applyDisplayProfile)
        applyDisplayProfile();
        vib = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        for (int i = 0; i < SFX_NOTES.length; i++) {
            sfxTrack(i);
        }
        loadBank();
        loadStats();
        showMenu();
    }

    @Override
    protected void onPause() {
        stopSurahAudio();
        stopMic();
        saveStats();
        if (screen == 2) {
            handler.removeCallbacksAndMessages(null);
        }
        pauseGame();
        if (putriGL != null) putriGL.onPause();
        if (nagaOverlay != null) nagaOverlay.onPause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (putriGL != null) putriGL.onResume();
        if (nagaOverlay != null) nagaOverlay.onResume();
        resumeGame();
    }

    @Override
    protected void onDestroy() {
        stopSurahAudio();
        stopMic();
        handler.removeCallbacksAndMessages(null);
        for (int i = 0; i < sfxTracks.length; i++) {
            if (sfxTracks[i] != null) {
                try {
                    sfxTracks[i].release();
                } catch (Exception e) {
                    // abaikan
                }
                sfxTracks[i] = null;
            }
        }
        if (pickDialog != null && pickDialog.isShowing()) {
            pickDialog.dismiss();
        }
        if (menuDialog != null && menuDialog.isShowing()) {
            menuDialog.setOnDismissListener(null);
            menuDialog.dismiss();
        }
        if (exitDialog != null && exitDialog.isShowing()) {
            exitDialog.dismiss();
        }
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (screen == 0) {
            super.onBackPressed();
        } else if (screen == 1) {
            confirmExit();
        } else if (screen == 5) {
            if (np == 1) {
                renderSoloResult();
            } else {
                showRanking();
            }
        } else if ((screen == 2 || screen == 9) && advLevel >= 0) {
            showPeta();
        } else if (screen == 8 && mushafOn) {
            kebunNoGrow = true;
            showSambung();
        } else if (screen == 2 && sambungOn()) {
            kebunNoGrow = true;
            showSambung();
        } else if (screen == 11) {
            showGuru();
        } else {
            saveNames();
            showMenu();
        }
    }

    // ---------- Efek suara, getar, dan gerak ----------

    private static short[] synth(int[][] notes, boolean buzz) {
        int total = 0;
        for (int[] nt : notes) {
            total += SFX_RATE * nt[1] / 1000;
        }
        short[] out = new short[total];
        int pos = 0;
        for (int[] nt : notes) {
            int len = SFX_RATE * nt[1] / 1000;
            double f = nt[0];
            for (int k = 0; k < len; k++) {
                if (f > 0) {
                    double ph = 2 * Math.PI * f * k / SFX_RATE;
                    double v = buzz ? Math.sin(ph) + 0.5 * Math.sin(3 * ph) + 0.3 * Math.sin(5 * ph)
                            : Math.sin(ph) + 0.3 * Math.sin(2 * ph);
                    double env = Math.min(1.0, k / (SFX_RATE * 0.004))
                            * Math.min(1.0, (len - k) / (SFX_RATE * 0.01))
                            * Math.exp(-2.2 * k / len);
                    out[pos + k] = (short) (v * env * 9000);
                }
            }
            pos += len;
        }
        return out;
    }

    private AudioTrack sfxTrack(int id) {
        if (sfxTracks[id] == null) {
            try {
                short[] pcm = synth(SFX_NOTES[id], id == SFX_WRONG);
                AudioTrack t = new AudioTrack(AudioManager.STREAM_MUSIC, SFX_RATE,
                        AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT,
                        pcm.length * 2, AudioTrack.MODE_STATIC);
                t.write(pcm, 0, pcm.length);
                sfxTracks[id] = t;
            } catch (Exception e) {
                return null;
            }
        }
        return sfxTracks[id];
    }

    private void sfx(int id) {
        if (!sfxOn) {
            return;
        }
        AudioTrack t = sfxTrack(id);
        if (t == null) {
            return;
        }
        try {
            t.stop();
            t.reloadStaticData();
            t.play();
        } catch (Exception e) {
            // Suara gagal diputar: abaikan saja
        }
    }

    private void buzz(long ms) {
        if (!sfxOn || vib == null) {
            return;
        }
        try {
            vib.vibrate(ms);
        } catch (Exception e) {
            // Getar tidak tersedia: abaikan saja
        }
    }

    // Membesar sebentar lalu memantul kembali
    private void pop(final View v, float peak) {
        if (v == null) {
            return;
        }
        v.animate().cancel();
        v.animate().setStartDelay(0).scaleX(peak).scaleY(peak).setDuration(110)
                .setInterpolator(new DecelerateInterpolator())
                .withEndAction(() -> v.animate().scaleX(1f).scaleY(1f).setDuration(240)
                        .setInterpolator(new OvershootInterpolator(3f)).start())
                .start();
    }

    // Bergetar ke kiri-kanan
    private void shake(View v) {
        if (v == null) {
            return;
        }
        v.animate().cancel();
        float a = dp(10);
        float b = dp(8);
        float c = dp(4);
        ObjectAnimator.ofFloat(v, View.TRANSLATION_X, 0f, -a, a, -b, b, -c, c, 0f)
                .setDuration(420).start();
    }

    // Kartu soal meluncur keluar ke kiri sebelum soal berikutnya muncul
    private void exitCard() {
        handler.postDelayed(() -> {
            if (cardQ != null) {
                cardQ.animate().setStartDelay(0).translationX(-dp(70)).alpha(0f).setDuration(220)
                        .setInterpolator(new AccelerateInterpolator()).start();
            }
        }, 760);
    }

    private void launchConfetti(int n) {
        ViewGroup root = findViewById(android.R.id.content);
        if (root != null) {
            root.addView(new Confetti(this, n), new FrameLayout.LayoutParams(MATCH, MATCH));
        }
    }

    // Efek layar hasil (solo): judul turun, skor menghitung naik, bintang muncul satu per satu
    private void playResultFx(TextView title, TextView[] st, final int stars, final TextView sc,
                              TextView ben, TextView rekorPill, final boolean rekor) {
        title.setTranslationY(-dp(50));
        title.setAlpha(0f);
        title.animate().translationY(0f).alpha(1f).setDuration(450)
                .setInterpolator(new OvershootInterpolator(1.8f)).start();

        ben.setAlpha(0f);
        ben.animate().alpha(1f).setStartDelay(300).setDuration(400).start();

        sc.setText("Skor: 0");
        ValueAnimator va = ValueAnimator.ofInt(0, pScore[0]);
        va.setDuration(Math.min(1400L, 400L + pScore[0] * 8L));
        va.setStartDelay(250);
        va.addUpdateListener(a -> sc.setText("Skor: " + a.getAnimatedValue()));
        va.start();

        for (int i = 0; i < 3; i++) {
            long delay = 350 + 380L * i;
            boolean filled = i < stars;
            st[i].setScaleX(0f);
            st[i].setScaleY(0f);
            st[i].setAlpha(0f);
            android.view.animation.Interpolator ip;
            if (filled) {
                ip = new OvershootInterpolator(3.5f);
            } else {
                ip = new DecelerateInterpolator();
            }
            st[i].animate().scaleX(1f).scaleY(1f).alpha(1f).setStartDelay(delay)
                    .setDuration(filled ? 480 : 250).setInterpolator(ip).start();
            if (filled) {
                handler.postDelayed(() -> {
                    sfx(SFX_CORRECT);
                    buzz(30);
                }, delay);
            }
        }

        long end = 350 + 380L * stars + 200;
        handler.postDelayed(() -> {
            if (rekor) {
                sfx(SFX_RECORD);
                buzz(90);
            } else if (stars >= 3) {
                sfx(SFX_WIN);
                buzz(60);
            } else if (stars >= 1) {
                sfx(SFX_OK);
            } else {
                sfx(SFX_LOSE);
            }
            if (stars >= 2 || rekor) {
                launchConfetti(stars >= 3 || rekor ? 130 : 80);
            }
        }, end);

        if (rekorPill != null) {
            rekorPill.setScaleX(0f);
            rekorPill.setScaleY(0f);
            rekorPill.animate().scaleX(1f).scaleY(1f).setStartDelay(end).setDuration(500)
                    .setInterpolator(new OvershootInterpolator(3f)).start();
        }
    }

    // ---------- Helper tampilan ----------

    private int dp(int v) {
        return Math.round(v * getResources().getDisplayMetrics().density * ui);
    }

    private GradientDrawable box(int color, int radiusDp) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(color);
        g.setCornerRadius(dp(radiusDp));
        return g;
    }

    private GradientDrawable outline(int color, int radiusDp) {
        GradientDrawable g = box(Color.WHITE, radiusDp);
        g.setStroke(dp(2), color);
        return g;
    }

    // Permukaan kartu premium: putih ke krem dengan garis rambut emas.
    private GradientDrawable premiumSurface(int radiusDp) {
        GradientDrawable g = new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{Color.WHITE, CREAM});
        g.setCornerRadius(dp(radiusDp));
        g.setStroke(dp(1), HAIRLINE);
        return g;
    }

    // Tombol utama: gradasi tiga tingkat + garis tepi putih tipis agar terlihat mengkilap.
    private GradientDrawable premiumButtonBg(int color, int radiusDp) {
        GradientDrawable g = new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{mixColor(color, Color.WHITE, 0.24f), color, mixColor(color, Color.BLACK, 0.18f)});
        g.setCornerRadius(dp(radiusDp));
        g.setStroke(dp(2), Color.argb(150, 255, 255, 255));
        return g;
    }

    private GradientDrawable optionBg(int color) {
        if (plainUi) {
            boolean okC = color == GREEN;
            boolean badC = color == RED;
            GradientDrawable pg = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                    okC ? new int[]{Color.parseColor("#F1FAF0"), Color.parseColor("#DDF0DA")}
                    : badC ? new int[]{Color.parseColor("#FFF3F3"), Color.parseColor("#FFE0E0")}
                    : new int[]{Color.WHITE, Color.parseColor("#FFF7E3")});
            pg.setCornerRadius(dp(22));
            pg.setStroke(dp(2), okC ? GREEN : badC ? RED : Color.parseColor("#E8D7A8"));
            return pg;
        }
        GradientDrawable g = new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{mixColor(color, Color.WHITE, 0.16f), color, mixColor(color, Color.BLACK, 0.14f)});
        g.setCornerRadius(dp(20));
        g.setStroke(dp(2), Color.argb(130, 255, 255, 255));
        return g;
    }

    private GradientDrawable timerBg(int color) {
        GradientDrawable g = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{mixColor(color, Color.WHITE, 0.28f), color, mixColor(color, Color.BLACK, 0.14f)});
        g.setCornerRadius(dp(20));
        return g;
    }

    private GradientDrawable pillBg(int color) {
        GradientDrawable g = new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{mixColor(color, Color.WHITE, 0.2f), color});
        g.setCornerRadius(dp(20));
        g.setStroke(dp(1), Color.argb(110, 255, 255, 255));
        return g;
    }

    // Tombol sekunder: permukaan krem dengan garis emas, untuk aksi yang tidak dominan.
    private GradientDrawable ghostButtonBg() {
        GradientDrawable g = new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{Color.WHITE, CREAM_DEEP});
        g.setCornerRadius(dp(22));
        g.setStroke(dp(2), GOLD);
        return g;
    }

    private GradientDrawable medallionBg(int color, int strokeDp) {
        GradientDrawable g = new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{mixColor(color, Color.WHITE, 0.28f), color, mixColor(color, Color.BLACK, 0.2f)});
        g.setShape(GradientDrawable.OVAL);
        g.setStroke(dp(strokeDp), Color.argb(200, 255, 255, 255));
        return g;
    }

    private boolean menuBgOn = false;

    private Drawable background() {
        android.graphics.Bitmap bg = assetBmp("img/bg_game.jpg");
        if (bg != null) {
            return new ImgBgDrawable(bg);
        }
        return menuBgOn ? new MenuBgDrawable() : new BgDrawable();
    }

    private final java.util.HashMap<String, android.graphics.Bitmap> bmpCache = new java.util.HashMap<>();

    private android.graphics.Bitmap assetBmp(String name) {
        android.graphics.Bitmap b = bmpCache.get(name);
        if (b == null && !bmpCache.containsKey(name)) {
            try (java.io.InputStream in = getAssets().open(name)) {
                b = android.graphics.BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                b = null;
            }
            if (b == null) {
                try {
                    String[] parts = name.contains("bg_game") ? ImgData.BG : ImgData.BTN;
                    StringBuilder sb = new StringBuilder();
                    for (String x : parts) {
                        sb.append(x);
                    }
                    byte[] raw = android.util.Base64.decode(sb.toString(), android.util.Base64.DEFAULT);
                    b = android.graphics.BitmapFactory.decodeByteArray(raw, 0, raw.length);
                } catch (Exception e) {
                    b = null;
                }
            }
            bmpCache.put(name, b);
        }
        return b;
    }

    // Latar gambar (isi penuh, dipotong tengah)
    private static class ImgBgDrawable extends Drawable {
        private final android.graphics.Bitmap bm;
        private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);

        ImgBgDrawable(android.graphics.Bitmap b) {
            bm = b;
        }

        @Override
        public void draw(Canvas cv) {
            Rect r = getBounds();
            if (r.width() <= 0 || r.height() <= 0) {
                return;
            }
            float sc = Math.max(r.width() / (float) bm.getWidth(), r.height() / (float) bm.getHeight());
            float sw = r.width() / sc;
            float sh = r.height() / sc;
            Rect src = new Rect((int) ((bm.getWidth() - sw) / 2), (int) ((bm.getHeight() - sh) / 2),
                    (int) ((bm.getWidth() + sw) / 2), (int) ((bm.getHeight() + sh) / 2));
            cv.drawBitmap(bm, src, r, p);
        }

        @Override
        public void setAlpha(int a) {
            p.setAlpha(a);
        }

        @Override
        public void setColorFilter(android.graphics.ColorFilter cf) {
            p.setColorFilter(cf);
        }

        @Override
        public int getOpacity() {
            return android.graphics.PixelFormat.OPAQUE;
        }
    }

    // Latar belakang: gradasi hangat, lingkaran lembut, dan simbol belajar yang samar
    private class BgDrawable extends Drawable {
        private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        // 13 simbol belajar pertama, sisanya hiasan alam (daun, bunga, awan, kupu-kupu)
        private final int mathSyms = 13;
        private final String[] sym = {"+", "\u2212", "\u00D7", "\u00F7", "=", "?",
                "A", "B", "C", "1", "2", "3", "\u2605",
                "\uD83C\uDF3F", "\uD83C\uDF43", "\uD83C\uDF31", "\uD83C\uDF3B", "\u2601\uFE0F",
                "\uD83E\uDD8B", "\uD83C\uDF33", "\uD83C\uDF43", "\uD83C\uDF3F", "\uD83C\uDF31"};
        private final int[] pal = {
                Color.argb(46, 67, 160, 71), Color.argb(40, 0, 137, 123),
                Color.argb(40, 30, 136, 229), Color.argb(46, 124, 179, 66),
                Color.argb(40, 251, 140, 0)};

        @Override
        public void draw(Canvas cv) {
            Rect r = getBounds();
            int w = r.width();
            int h = r.height();
            if (w <= 0 || h <= 0) {
                return;
            }
            p.setStyle(Paint.Style.FILL);
            p.setShader(new LinearGradient(0, 0, 0, h, Color.parseColor("#E3F2FD"),
                    Color.parseColor("#E8F5E9"), Shader.TileMode.CLAMP));
            cv.drawRect(r, p);
            p.setShader(null);

            p.setColor(Color.argb(46, 255, 213, 79));
            cv.drawCircle(w * 0.92f, h * 0.06f, w * 0.34f, p);
            p.setColor(Color.argb(36, 102, 187, 106));
            cv.drawCircle(w * 0.02f, h * 0.45f, w * 0.30f, p);
            p.setColor(Color.argb(40, 76, 175, 80));
            cv.drawCircle(w * 0.98f, h * 0.92f, w * 0.42f, p);

            p.setTypeface(Typeface.DEFAULT_BOLD);
            p.setTextAlign(Paint.Align.CENTER);
            float cell = dp(74);
            int cols = (int) (w / cell) + 1;
            int rows = (int) (h / cell) + 1;
            Random rr = new Random(11);
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    float x = j * cell + (i % 2 == 1 ? cell / 2f : 0f) + cell * (0.2f + rr.nextFloat() * 0.6f);
                    float y = i * cell + cell * (0.3f + rr.nextFloat() * 0.6f);
                    p.setColor(pal[rr.nextInt(pal.length)]);
                    p.setTextSize(dp(18 + rr.nextInt(14)));
                    cv.save();
                    cv.rotate(rr.nextInt(50) - 25, x, y);
                    int si = rr.nextInt(sym.length);
                    if (si >= mathSyms) {
                        p.setAlpha(95);
                    }
                    cv.drawText(sym[si], x, y, p);
                    cv.restore();
                }
            }
        }

        @Override
        public void setAlpha(int alpha) {
        }

        @Override
        public void setColorFilter(ColorFilter cf) {
        }

        @Override
        public int getOpacity() {
            return PixelFormat.OPAQUE;
        }
    }

    // Kotak berwarna dengan lingkaran lembut sebagai hiasan
    private static int mixColor(int a, int b, float t) {
        return Color.argb(255,
                (int) (Color.red(a) + (Color.red(b) - Color.red(a)) * t),
                (int) (Color.green(a) + (Color.green(b) - Color.green(a)) * t),
                (int) (Color.blue(a) + (Color.blue(b) - Color.blue(a)) * t));
    }

    /** Sentuhan menekan: sedikit mengecil lalu memantul kembali; klik tetap jalan. */
    private void pressable(View v) {
        v.setOnTouchListener((x, e) -> {
            int act = e.getActionMasked();
            if (act == MotionEvent.ACTION_DOWN) {
                x.animate().setStartDelay(0).scaleX(0.97f).scaleY(0.97f).setDuration(80)
                        .setInterpolator(new DecelerateInterpolator()).start();
            } else if (act == MotionEvent.ACTION_UP || act == MotionEvent.ACTION_CANCEL) {
                x.animate().setStartDelay(0).scaleX(1f).scaleY(1f).setDuration(180)
                        .setInterpolator(new OvershootInterpolator(2f)).start();
            }
            return false;
        });
    }

    /** Anak-anak kolom muncul bergantian dari bawah (kesan halus saat menu dibuka). */
    private void animateIn(LinearLayout col) {
        for (int i = 0; i < col.getChildCount(); i++) {
            View ch = col.getChildAt(i);
            ch.setAlpha(0f);
            ch.setTranslationY(dp(18));
            ch.animate().alpha(1f).translationY(0f).setStartDelay(60L + i * 55L).setDuration(320)
                    .setInterpolator(new DecelerateInterpolator()).start();
        }
    }

    /** Judul bagian dengan garis tipis di kiri dan kanan. */
    private LinearLayout sectionTitle(String s) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        int lineC = Color.argb(46, 78, 52, 46);
        View l1 = new View(this);
        l1.setBackground(box(lineC, 2));
        View l2 = new View(this);
        l2.setBackground(box(lineC, 2));
        LinearLayout.LayoutParams a = new LinearLayout.LayoutParams(0, dp(2), 1f);
        a.rightMargin = dp(12);
        LinearLayout.LayoutParams b = new LinearLayout.LayoutParams(0, dp(2), 1f);
        b.leftMargin = dp(12);
        row.addView(l1, a);
        row.addView(text(s, 16, DARK, true), new LinearLayout.LayoutParams(WRAP, WRAP));
        row.addView(l2, b);
        return row;
    }

    /** Chip statistik ringkas: ikon dalam medali kecil dan satu nilai. */
    private LinearLayout statChip(String icon, String value, int accent) {
        LinearLayout t = new LinearLayout(this);
        t.setOrientation(LinearLayout.HORIZONTAL);
        t.setGravity(Gravity.CENTER_VERTICAL);
        t.setPadding(dp(8), dp(4), dp(12), dp(4));
        t.setBackground(premiumSurface(20));
        t.setElevation(dp(3));
        TextView ic = text(icon, 14, Color.WHITE, false);
        ic.setBackground(medallionBg(accent, 1));
        LinearLayout.LayoutParams icp = new LinearLayout.LayoutParams(dp(28), dp(28));
        icp.rightMargin = dp(6);
        t.addView(ic, icp);
        TextView n = text(value, 13, INK, true);
        t.addView(n, new LinearLayout.LayoutParams(WRAP, WRAP));
        return t;
    }

    /** Kartu putih kecil: ikon berwarna dalam lingkaran dan satu label. */
    private LinearLayout miniCard(String icon, String label, int accent) {
        LinearLayout t = new LinearLayout(this);
        t.setOrientation(LinearLayout.HORIZONTAL);
        t.setGravity(Gravity.CENTER_VERTICAL);
        t.setPadding(dp(10), 0, dp(10), 0);
        t.setBackground(premiumSurface(24));
        t.setElevation(dp(4));
        TextView ic = text(icon, 18, Color.WHITE, false);
        ic.setBackground(medallionBg(accent, 2));
        LinearLayout.LayoutParams icp = new LinearLayout.LayoutParams(dp(36), dp(36));
        icp.rightMargin = dp(8);
        t.addView(ic, icp);
        TextView n = text(label, 12, DARK, true);
        n.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
        n.setMaxLines(2);
        t.addView(n, new LinearLayout.LayoutParams(0, WRAP, 1f));
        t.setClickable(true);
        t.setFocusable(true);
        pressable(t);
        return t;
    }

    /** Spanduk judul menu: gradasi zamrud ke langit, cahaya dari atas, lingkaran kaca, kilau emas, garis tepi tipis. */
    private class HeroDrawable extends Drawable {
        private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);

        @Override
        public void draw(Canvas cv) {
            RectF rf = new RectF(getBounds());
            float rad = dp(32);
            p.setStyle(Paint.Style.FILL);
            p.setShader(new LinearGradient(rf.left, rf.top, rf.right, rf.bottom,
                    new int[]{Color.parseColor("#06683F"), Color.parseColor("#0E9A8E"), Color.parseColor("#2AA9E8"), Color.parseColor("#6FD0F7")},
                    new float[]{0f, 0.4f, 0.75f, 1f}, Shader.TileMode.CLAMP));
            cv.drawRoundRect(rf, rad, rad, p);
            p.setShader(null);
            Path clip = new Path();
            clip.addRoundRect(rf, rad, rad, Path.Direction.CW);
            cv.save();
            cv.clipPath(clip);
            p.setShader(new LinearGradient(0f, rf.top, 0f, rf.top + rf.height() * 0.5f,
                    Color.argb(64, 255, 255, 255), Color.argb(0, 255, 255, 255), Shader.TileMode.CLAMP));
            cv.drawRect(rf.left, rf.top, rf.right, rf.top + rf.height() * 0.5f, p);
            p.setShader(null);
            p.setColor(Color.argb(34, 255, 255, 255));
            cv.drawCircle(rf.right - dp(10), rf.top + dp(30), dp(120), p);
            p.setColor(Color.argb(26, 255, 255, 255));
            cv.drawCircle(rf.left - dp(10), rf.bottom - dp(10), dp(100), p);
            p.setColor(Color.argb(60, 255, 235, 150));
            cv.drawCircle(rf.left + dp(48), rf.top + dp(40), dp(9), p);
            cv.drawCircle(rf.right - dp(54), rf.bottom - dp(46), dp(6), p);
            cv.drawCircle(rf.right - dp(86), rf.top + dp(26), dp(4), p);
            // ombak halus di dasar spanduk
            Path wv = new Path();
            float wy = rf.bottom - dp(24);
            wv.moveTo(rf.left, rf.bottom);
            wv.lineTo(rf.left, wy);
            for (float x = rf.left; x <= rf.right; x += dp(4)) {
                wv.lineTo(x, wy + (float) Math.sin((x - rf.left) / dp(40) * 6.28318f) * dp(5));
            }
            wv.lineTo(rf.right, rf.bottom);
            wv.close();
            p.setColor(Color.argb(30, 255, 255, 255));
            cv.drawPath(wv, p);
            p.setColor(Color.argb(210, 255, 248, 200));
            drawSparkle(cv, rf.right - dp(34), rf.top + dp(26), dp(9), p);
            drawSparkle(cv, rf.right - dp(72), rf.bottom - dp(36), dp(5), p);
            drawSparkle(cv, rf.left + dp(118), rf.top + dp(16), dp(4), p);
            cv.restore();
            RectF in = new RectF(rf);
            in.inset(1f, 1f);
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(Math.max(1, dp(1)));
            p.setColor(Color.argb(90, 255, 255, 255));
            cv.drawRoundRect(in, rad, rad, p);
            p.setStyle(Paint.Style.FILL);
        }

        @Override
        public void getOutline(Outline outline) {
            outline.setRoundRect(getBounds(), dp(32));
            outline.setAlpha(1f);
        }

        @Override
        public void setAlpha(int alpha) {
        }

        @Override
        public void setColorFilter(ColorFilter cf) {
        }

        @Override
        public int getOpacity() {
            return PixelFormat.TRANSLUCENT;
        }
    }

    private class TileDrawable extends Drawable {
        private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final int base;

        TileDrawable(int color) {
            base = color;
        }

        @Override
        public void draw(Canvas cv) {
            RectF rf = new RectF(getBounds());
            float rad = dp(22);
            p.setStyle(Paint.Style.FILL);
            p.setShader(new LinearGradient(rf.left, rf.top, rf.right, rf.bottom,
                    mixColor(base, Color.WHITE, 0.14f), mixColor(base, Color.BLACK, 0.16f), Shader.TileMode.CLAMP));
            cv.drawRoundRect(rf, rad, rad, p);
            p.setShader(null);
            Path clip = new Path();
            clip.addRoundRect(rf, rad, rad, Path.Direction.CW);
            cv.save();
            cv.clipPath(clip);
            p.setColor(Color.argb(40, 255, 255, 255));
            cv.drawCircle(rf.right - dp(18), rf.top + dp(4), dp(54), p);
            p.setColor(Color.argb(28, 255, 255, 255));
            cv.drawCircle(rf.right - dp(76), rf.bottom + dp(6), dp(40), p);
            cv.drawCircle(rf.left + dp(10), rf.bottom - dp(2), dp(28), p);
            cv.restore();
            RectF in = new RectF(rf);
            in.inset(1f, 1f);
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(Math.max(1, dp(1)));
            p.setColor(Color.argb(70, 255, 255, 255));
            cv.drawRoundRect(in, rad, rad, p);
            p.setStyle(Paint.Style.FILL);
        }

        @Override
        public void getOutline(Outline outline) {
            outline.setRoundRect(getBounds(), dp(22));
            outline.setAlpha(1f);
        }

        @Override
        public void setAlpha(int alpha) {
        }

        @Override
        public void setColorFilter(ColorFilter cf) {
        }

        @Override
        public int getOpacity() {
            return PixelFormat.TRANSLUCENT;
        }
    }

    private TextView text(String s, int sp, int color, boolean bold) {
        TextView t = new TextView(this);
        t.setText(s);
        t.setTextSize(sp * ui);
        t.setTextColor(color);
        t.setGravity(Gravity.CENTER);
        if (bold) {
            t.setTypeface(ROUND_BOLD);
        }
        return t;
    }

    private TextView heading(String s) {
        TextView t = text(s, 16, DARK, true);
        t.setGravity(Gravity.START);
        return t;
    }

    private TextView pill(String s, int color) {
        TextView t = text(s, 15, Color.WHITE, true);
        t.setBackground(pillBg(color));
        t.setPadding(dp(14), dp(6), dp(14), dp(6));
        return t;
    }

    private Button button(String s, int color, int sp) {
        Button b = new Button(this);
        b.setText(s);
        b.setAllCaps(false);
        b.setTextSize(sp * ui);
        b.setTextColor(Color.WHITE);
        b.setTypeface(ROUND_BOLD);
        b.setBackground(premiumButtonBg(color, 22));
        b.setOnTouchListener((v, e) -> {
            int act = e.getActionMasked();
            if (act == MotionEvent.ACTION_DOWN) {
                if (v.getTag() == null) {
                    sfx(SFX_TAP);
                }
                v.animate().setStartDelay(0).scaleX(0.95f).scaleY(0.95f).setDuration(80)
                        .setInterpolator(new DecelerateInterpolator()).start();
            } else if (act == MotionEvent.ACTION_UP || act == MotionEvent.ACTION_CANCEL) {
                v.animate().setStartDelay(0).scaleX(1f).scaleY(1f).setDuration(180)
                        .setInterpolator(new OvershootInterpolator(2f)).start();
            }
            return false;
        });
        return b;
    }

    private void flat(Button b) {
        b.setMinWidth(0);
        b.setMinimumWidth(0);
        b.setMinHeight(0);
        b.setMinimumHeight(0);
        b.setPadding(0, 0, 0, 0);
    }

    private LinearLayout column() {
        LinearLayout l = new LinearLayout(this);
        l.setOrientation(LinearLayout.VERTICAL);
        l.setGravity(Gravity.CENTER_HORIZONTAL);
        l.setPadding(dp(18), dp(28), dp(18), dp(24));
        return l;
    }

    private LinearLayout card() {
        LinearLayout l = new LinearLayout(this);
        l.setOrientation(LinearLayout.VERTICAL);
        l.setPadding(dp(16), dp(14), dp(16), dp(16));
        l.setBackground(premiumSurface(26));
        l.setElevation(dp(6));
        return l;
    }

    // Naga pendamping: melayang di atas layar bila dipakai (dipasang di decorView agar tidak hilang saat ganti layar)
    private NagaPetView nagaOverlay;
    private boolean nagaHide = false;

    private void syncNaga(boolean show) {
        boolean want = show && prefs != null && prefs.getBoolean("nagaPakai", false);
        if (want && nagaOverlay == null) {
            nagaOverlay = NagaPetView.overlay(this);
        }
        if (nagaOverlay != null) {
            nagaOverlay.setVisibility(want ? View.VISIBLE : View.GONE);
        }
    }

    // Layar Sambung Ayat: latar putih polos, pilihan putih berbingkai abu-abu
    private boolean plainUi;

    private void wrap(LinearLayout c) {
        wrapBg(c, plainUi ? new android.graphics.drawable.ColorDrawable(Color.WHITE) : background());
    }

    private void wrapBg(LinearLayout c, Drawable bg) {
        syncNaga(!nagaHide);
        nagaHide = false;
        ScrollView sv = new ScrollView(this);
        sv.setFillViewport(true);
        sv.setBackground(bg);
        if (getResources().getDisplayMetrics().widthPixels > dp(700)) {
            // Layar lebar (TV/tablet): isi dibuat di tengah dengan lebar terbatas
            FrameLayout fl = new FrameLayout(this);
            fl.addView(c, new FrameLayout.LayoutParams(dp(640), WRAP, Gravity.CENTER_HORIZONTAL));
            sv.addView(fl);
        } else {
            sv.addView(c);
        }
        setContentView(sv);
        sv.setAlpha(0f);
        sv.setTranslationY(dp(12));
        sv.animate().alpha(1f).translationY(0f).setDuration(200)
                .setInterpolator(new DecelerateInterpolator()).start();
    }

    private LinearLayout.LayoutParams lp(int height, int topDp) {
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(MATCH, height);
        p.topMargin = dp(topDp);
        return p;
    }

    private LinearLayout.LayoutParams lpw(int topDp) {
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(WRAP, WRAP);
        p.topMargin = dp(topDp);
        return p;
    }

    private LinearLayout.LayoutParams weight(int leftDp, int rightDp) {
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0, MATCH, 1f);
        p.leftMargin = dp(leftDp);
        p.rightMargin = dp(rightDp);
        return p;
    }

    private LinearLayout.LayoutParams fixed(int widthDp) {
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(dp(widthDp), MATCH);
        p.leftMargin = dp(3);
        p.rightMargin = dp(3);
        return p;
    }

    private LinearLayout chips(String[] labels, int sel, int[] colors, final Pick pick) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        for (int i = 0; i < labels.length; i++) {
            final int idx = i;
            boolean on = (i == sel);
            Button b = button(labels[i], on ? colors[i] : GRAY, 13);
            b.setTextColor(on ? Color.WHITE : DARK);
            flat(b);
            row.addView(b, weight(i == 0 ? 0 : 3, i == labels.length - 1 ? 0 : 3));
            b.setOnClickListener(v -> {
                saveNames();
                pick.on(idx);
            });
        }
        return row;
    }

    private String bestKey() {
        return "best_k" + kelas + (mode == 7 ? "_b" + subj + (alamOn() ? "a" : "") + (tebakOn() ? "g" : "") + (sambungOn() ? (urut ? "ur" : "s") + (SambungAyat.juz != 30 ? "j" + SambungAyat.juz : "") : "") : "") + (timed ? "_t" : "");
    }

    private String timeLabel() {
        return timed ? "Berwaktu " + secsNow() + " detik" : "Tanpa waktu";
    }

    private String modeLabel() {
        if (advLevel >= 0) {
            return "Petualangan \u2022 " + timeLabel();
        }
        String base = mode == 7 ? (alamOn() ? "Kuis Alam \u2022 " : tebakOn() ? "Tebak Gambar \u2022 " : sambungOn() ? (urut ? "Urut Ayat \u2022 " : "Sambung Ayat \u2022 ") : "") + SUBJ_NAMES[subj] + " \u2022 " + timeLabel() : timeLabel();
        return (relaxed && mode != 7) ? base + " \u2022 Santai" : base;
    }

    // Permainan tanpa nyawa: soal guru selalu begitu, soal hitung kalau mode santai menyala
    private boolean noLives() {
        return mode == 7 || relaxed || advLevel >= 0;
    }

    // Skor hanya masuk rekor kalau tidak dalam mode santai (soal guru selalu dihitung)
    private boolean countsForRecord() {
        return advLevel < 0 && (mode == 7 || !relaxed);
    }

    private String optText(Q q, int i) {
        return q.sopts != null ? q.sopts[i] : fmt(q.opts[i]);
    }

    private int bankOptionSize(int len) {
        return len <= 10 ? 20 : len <= 24 ? 16 : 13;
    }

    private void saveNames() {
        if (nameFields == null) {
            return;
        }
        SharedPreferences.Editor ed = prefs.edit();
        for (int i = 0; i < nameFields.length; i++) {
            String s = nameFields[i].getText().toString().trim();
            if (s.isEmpty()) {
                s = "Pemain " + (i + 1);
            }
            names[i] = s;
            ed.putString("name" + i, s);
        }
        ed.apply();
    }

    // ---------- Menu ----------

    private void loadPrefs() {
        kelas = Math.max(0, Math.min(5, prefs.getInt("kelas", 0)));
        timed = prefs.getBoolean("timed", false);
        secs = Math.max(SECS_MIN, Math.min(SECS_MAX, prefs.getInt("secs", 10)));
        np = Math.max(1, Math.min(MAX_PLAYERS, prefs.getInt("np", 1)));
        sfxOn = prefs.getBoolean("sfx", true);
        relaxed = prefs.getBoolean("relaxed", false);
        for (int i = 0; i < MAX_PLAYERS; i++) {
            names[i] = prefs.getString("name" + i, "Pemain " + (i + 1));
            avatar[i] = HOOD_ALIAS[Math.max(0, Math.min(AVATARS.length - 1, prefs.getInt("avatar" + i, i)))];
            genderOf[i] = prefs.getInt("gender" + i, 0) == 1 ? 1 : 0;
        }
    }

    private void showMenu() {
        handler.removeCallbacksAndMessages(null);
        plainUi = false;
        setScreen(0);
        advLevel = -1;
        nameFields = null;
        loadPrefs();

        LinearLayout c = column();
        final boolean wide = isWide();
        LinearLayout cr = wide ? column() : null;
        LinearLayout rt = wide ? cr : c;

        // Baris ikon: suara dan tampilan di kiri, avatar di kanan
        LinearLayout bar = new LinearLayout(this);
        bar.setOrientation(LinearLayout.HORIZONTAL);
        bar.setGravity(Gravity.CENTER_VERTICAL);
        TextView snd = text(sfxOn ? "\uD83D\uDD0A" : "\uD83D\uDD07", 22, INK, false);
        snd.setBackground(premiumSurface(26));
        snd.setContentDescription(sfxOn ? "Suara menyala" : "Suara mati");
        snd.setElevation(dp(3));
        snd.setOnClickListener(v -> {
            prefs.edit().putBoolean("sfx", !sfxOn).apply();
            showMenu();
            sfx(SFX_CORRECT);
        });
        bar.addView(snd, new LinearLayout.LayoutParams(dp(52), dp(52)));
        // v6.21.0: satu pintu Pengaturan (tampilan layar, Soal Guru); ikon TV dan Soal Guru tidak lagi di menu utama
        TextView dsp = text("\u2699\uFE0F", 22, INK, false);
        dsp.setBackground(premiumSurface(26));
        dsp.setContentDescription("Pengaturan");
        dsp.setElevation(dp(3));
        dsp.setOnClickListener(v -> dialogPengaturan());
        LinearLayout.LayoutParams dspLp = new LinearLayout.LayoutParams(dp(52), dp(52));
        dspLp.leftMargin = dp(10);
        bar.addView(dsp, dspLp);
        bar.addView(new View(this), new LinearLayout.LayoutParams(0, 1, 1f));
        // Koin dan avatar dulu ada di kanan atas; keduanya hanya pintu ke Toko Avatar, kini cukup lewat kartu Toko Avatar
        c.addView(bar, lp(WRAP, 0));

        // Spanduk judul: logo di kiri, teks di kanan
        LinearLayout hero = new LinearLayout(this);
        hero.setOrientation(LinearLayout.HORIZONTAL);
        hero.setGravity(Gravity.CENTER_VERTICAL);
        hero.setPadding(dp(16), dp(20), dp(14), dp(20));
        hero.setBackground(new HeroDrawable());
        hero.setElevation(dp(8));
        hero.setOnLongClickListener(v -> {
            openAdmin();
            return true;
        });
        ImageView logo = new ImageView(this);
        logo.setImageResource(R.drawable.logo);
        logo.setScaleType(ImageView.ScaleType.FIT_CENTER);
        logo.setPadding(dp(7), dp(7), dp(7), dp(7));
        GradientDrawable lg = box(Color.WHITE, 26);
        lg.setStroke(dp(3), Color.parseColor("#FFD54F"));
        logo.setBackground(lg);
        logo.setElevation(dp(4));
        final float logoRad = dp(26);
        logo.setOutlineProvider(new android.view.ViewOutlineProvider() {
            @Override
            public void getOutline(View v, Outline o) {
                o.setRoundRect(0, 0, v.getWidth(), v.getHeight(), logoRad);
            }
        });
        logo.setClipToOutline(true);
        LinearLayout.LayoutParams logoLp = new LinearLayout.LayoutParams(dp(88), dp(88));
        logoLp.rightMargin = dp(14);
        hero.addView(logo, logoLp);
        LinearLayout htx = new LinearLayout(this);
        htx.setOrientation(LinearLayout.VERTICAL);
        TextView ttl = text(APP_NAME, 22, Color.WHITE, true);
        ttl.setGravity(Gravity.START);
        ttl.setSingleLine(true);
        ttl.setShadowLayer(dp(3), 0f, dp(2), Color.argb(90, 0, 0, 0));
        htx.addView(ttl, new LinearLayout.LayoutParams(WRAP, WRAP));
        TextView inst = text("SD Alam Muhammadiyah Kandangan", 14, Color.WHITE, true);
        inst.setGravity(Gravity.START);
        htx.addView(inst, lpw(6));
        hero.addView(htx, new LinearLayout.LayoutParams(0, WRAP, 1f));
        c.addView(hero, lp(WRAP, 12));

        // Ringkasan progres: bintang dan koin tampil sebagai chip premium di bawah spanduk
        LinearLayout stats = new LinearLayout(this);
        stats.setOrientation(LinearLayout.HORIZONTAL);
        stats.setGravity(Gravity.CENTER_VERTICAL);
        stats.addView(statChip("\u2B50", String.valueOf(totalStars()), GOLD_DEEP),
                new LinearLayout.LayoutParams(0, dp(40), 1f));
        LinearLayout.LayoutParams coinLp = new LinearLayout.LayoutParams(0, dp(40), 1f);
        coinLp.leftMargin = dp(8);
        stats.addView(statChip("\uD83D\uDCB0", String.valueOf(coins()), ORANGE_DARK), coinLp);
        c.addView(stats, lp(WRAP, 10));

        // Kelas dan Cara main: dua pil. Pemain, waktu, dan nyawa dulu tiga kontrol terpisah; kini satu pintu "Cara main"
        LinearLayout opt = new LinearLayout(this);
        opt.setOrientation(LinearLayout.HORIZONTAL);
        opt.setGravity(Gravity.CENTER_VERTICAL);
        String caraMain = (np == 1 ? "Sendiri" : np + " pemain")
                + " \u2022 " + (timed ? secsNow() + " detik" : "Bebas")
                + " \u2022 " + (relaxed ? "Santai" : "Ada nyawa");
        String[] segTxt = {"\uD83C\uDF93 " + KELAS_NAMES[kelas], caraMain};
        for (int sgi = 0; sgi < 2; sgi++) {
            final int sg = sgi;
            LinearLayout seg = new LinearLayout(this);
            seg.setOrientation(LinearLayout.HORIZONTAL);
            seg.setGravity(Gravity.CENTER);
            seg.setPadding(dp(10), 0, dp(10), 0);
            GradientDrawable sgBg = sg == 0
                    ? new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                    new int[]{mixColor(KELAS_COLOR[kelas], Color.WHITE, 0.22f), KELAS_COLOR[kelas]})
                    : premiumSurface(26);
            sgBg.setCornerRadius(dp(26));
            sgBg.setStroke(dp(sg == 0 ? 3 : 1),
                    sg == 0 ? Color.argb(130, 255, 255, 255) : Color.argb(50, 96, 125, 139));
            seg.setBackground(sgBg);
            seg.setElevation(dp(4));
            TextView lbl = text(segTxt[sg], wide ? 16 : 13, sg == 0 ? Color.WHITE : DARK, true);
            lbl.setMaxLines(2);
            seg.addView(lbl, new LinearLayout.LayoutParams(0, WRAP, 1f));
            TextView chev = text("\u203A", 20, sg == 0 ? Color.WHITE : Color.parseColor("#607D8B"), true);
            LinearLayout.LayoutParams chevLp = new LinearLayout.LayoutParams(WRAP, WRAP);
            chevLp.leftMargin = dp(4);
            seg.addView(chev, chevLp);
            seg.setContentDescription(sg == 0 ? "Kelas: " + KELAS_NAMES[kelas] : "Cara main: " + caraMain);
            seg.setClickable(true);
            seg.setFocusable(true);
            pressable(seg);
            seg.setOnClickListener(v -> {
                sfx(SFX_TAP);
                if (sg == 0) {
                    dialogKelas();
                } else {
                    dialogCaraMain();
                }
            });
            LinearLayout.LayoutParams segLp = new LinearLayout.LayoutParams(0, MATCH, sg == 0 ? 0.8f : 1.4f);
            if (sg > 0) {
                segLp.leftMargin = dp(8);
            }
            opt.addView(seg, segLp);
        }
        c.addView(opt, lp(dp(56), 16));

        // Peta Petualangan
        LinearLayout mp = petaCard();
        mp.setOnClickListener(v -> showPeta());
        rt.addView(mp, lp(dp(152), wide ? 0 : 16));

        // Pilih pelajaran
        rt.addView(sectionTitleLeaf("Pilih pelajaran"), lp(WRAP, wide ? 16 : 20));
        for (int s = 0; s < 3; s++) {
            final int sj = s;
            int n = countBank(s, kelas);
            String sub;
            if (s == 0) {
                sub = n > 0 ? "Latihan hitung + " + n + " soal guru" : "Latihan hitung";
            } else {
                boolean pr = matchAvailable(sj);
                sub = n > 0 ? n + " soal" + (pr ? " + Cocokkan Pasangan" : "")
                        : (pr ? "Cocokkan Pasangan" : "Belum ada soal");
                if (s == 1) {
                    sub = "Kuis" + (pr ? " \u2022 Pasangan" : "")
                            + (susunAvailable(0) ? " \u2022 Susun Kata" : "")
                            + (susunAvailable(1) ? " \u2022 Susun Kalimat" : "")
                            + " \u2022 Alam";
                } else {
                    sub = "Kuis" + (pr ? " \u2022 Pasangan" : "") + " \u2022 Alam";
                }
            }
            LinearLayout t = subjectTile(SUBJ_ICON[s], SUBJ_NAMES[s], sub, SUBJ_COLOR[s]);
            swapTileIcon(t, new String[]{"menu_cube", "menu_abc", "menu_globe"}[s]);
            natureTile(t, MENU_SUBJ_COLOR[s], s);
            t.setOnClickListener(v -> openSubject(sj));
            rt.addView(t, lp(dp(100), 14));
        }

        // Toko Avatar (kartu merah muda dengan koin)
        LinearLayout tk = subjectTile("\uD83D\uDECD\uFE0F", "Toko Avatar",
                "\uD83D\uDCB0 " + coins() + " koin \u2022 ganti penampilan", PINK);
        swapTileIcon(tk, "menu_shop");
        natureTile(tk, PINK, 3);
        tk.setOnClickListener(v -> showToko());
        c.addView(tk, lp(dp(84), 18));

        // Sambung Ayat + Kebun Hafalan (Juz 30)
        android.graphics.Bitmap saBmp = assetBmp("img/btn_sambung_ayat.png");
        if (saBmp != null) {
            ImageView saImg = new ImageView(this);
            saImg.setImageBitmap(saBmp);
            saImg.setScaleType(ImageView.ScaleType.FIT_XY);
            saImg.setContentDescription("Sambung Ayat");
            saImg.setOnClickListener(v -> showSambung());
            saImg.setOnTouchListener((v, ev) -> {
                int a = ev.getActionMasked();
                if (a == MotionEvent.ACTION_DOWN) {
                    v.animate().scaleX(0.96f).scaleY(0.96f).setDuration(90).start();
                } else if (a == MotionEvent.ACTION_UP || a == MotionEvent.ACTION_CANCEL) {
                    v.animate().scaleX(1f).scaleY(1f).setDuration(120).start();
                }
                return false;
            });
            // napas pelan agar terasa hidup
            ObjectAnimator saBreath = ObjectAnimator.ofPropertyValuesHolder(saImg,
                    android.animation.PropertyValuesHolder.ofFloat(View.ROTATION, -0.7f, 0.7f));
            saBreath.setDuration(1500);
            saBreath.setInterpolator(new android.view.animation.AccelerateDecelerateInterpolator());
            saBreath.setRepeatMode(ValueAnimator.REVERSE);
            saBreath.setRepeatCount(ValueAnimator.INFINITE);
            loopWhileAttached(saImg, saBreath);
            c.addView(saImg, lp(dp(96), 18));
        } else {
            LinearLayout sa = miniCard("\uD83C\uDF33", "Sambung Ayat \u2022 Kebun Hafalan", GREEN);
            sa.setOnClickListener(v -> showSambung());
            c.addView(sa, lp(dp(58), 18));
        }

        // Rekor & Lencana (Soal Guru pindah ke Pengaturan, bukan untuk anak)
        LinearLayout rk = miniCard("\uD83C\uDFC5", "Rekor & Lencana", Color.parseColor("#F9A825"));
        rk.setOnClickListener(v -> showRekor());
        c.addView(rk, lp(dp(58), 22));

        c.setFocusableInTouchMode(true);
        c.requestFocus();
        menuBgOn = true;
        if (wide) {
            wrapTwo(c, cr);
            animateIn(cr);
        } else {
            wrap(c);
        }
        menuBgOn = false;
        animateIn(c);
    }

    // ---------- Gaya premium: kaca berkilau, kilau bergerak, bayangan berwarna ----------

    private void drawSparkle(Canvas cv, float x, float y, float r, Paint pt) {
        Path sp = new Path();
        sp.moveTo(x, y - r);
        sp.quadTo(x, y, x + r, y);
        sp.quadTo(x, y, x, y + r);
        sp.quadTo(x, y, x - r, y);
        sp.quadTo(x, y, x, y - r);
        sp.close();
        cv.drawPath(sp, pt);
    }

    /** Bayangan berwarna (Android 9 ke atas); di HP lama bayangan biasa. */
    private void glow(View v, int color) {
        // Sengaja kosong: bayangan berwarna membuat kartu tampak pucat di sebagian HP (Xiaomi/MIUI).
        // Bayangan bawaan sistem dipakai saja.
    }

    private void loopWhileAttached(final View v, final android.animation.Animator an) {
        v.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
            @Override
            public void onViewAttachedToWindow(View x) {
                if (!an.isStarted()) {
                    an.start();
                }
            }

            @Override
            public void onViewDetachedFromWindow(View x) {
                an.cancel();
            }
        });
    }

    private void premium(View t, int color, int radDp, long delay) {
        final PremiumTile d = new PremiumTile(color, radDp);
        t.setBackground(d);
        // Elevasi sistem DIMATIKAN: renderer bayangan (MIUI) menggambar persegi terang di dalam kartu
        // (inset = radius sudut). Bayangan digambar sendiri di PremiumTile.draw().
        t.setElevation(0f);
        t.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
            @Override
            public void onViewAttachedToWindow(View x) {
                // Bayangan sendiri digambar di luar batas view, jadi induk tidak boleh memotongnya.
                android.view.ViewParent pp = x.getParent();
                for (int i = 0; i < 3 && pp instanceof ViewGroup; i++) {
                    ViewGroup g = (ViewGroup) pp;
                    g.setClipChildren(false);
                    pp = g.getParent();
                }
            }

            @Override
            public void onViewDetachedFromWindow(View x) {
            }
        });
        final ValueAnimator va = ValueAnimator.ofFloat(0f, 3.2f);
        va.setDuration(5200);
        va.setStartDelay(delay);
        va.setRepeatCount(ValueAnimator.INFINITE);
        va.setInterpolator(new LinearInterpolator());
        va.addUpdateListener(a -> {
            d.shine = (Float) a.getAnimatedValue();
            d.invalidateSelf();
        });
        loopWhileAttached(t, va);
    }

    private void floatAnim(View v, int ampDp, long dur) {
        ObjectAnimator a = ObjectAnimator.ofFloat(v, View.TRANSLATION_Y, 0f, -dp(ampDp));
        a.setDuration(dur);
        a.setRepeatMode(ValueAnimator.REVERSE);
        a.setRepeatCount(ValueAnimator.INFINITE);
        a.setInterpolator(new android.view.animation.AccelerateDecelerateInterpolator());
        loopWhileAttached(v, a);
    }

    private void pulse(View v) {
        ObjectAnimator a = ObjectAnimator.ofPropertyValuesHolder(v,
                android.animation.PropertyValuesHolder.ofFloat(View.SCALE_X, 1f, 1.14f),
                android.animation.PropertyValuesHolder.ofFloat(View.SCALE_Y, 1f, 1.14f));
        a.setDuration(850);
        a.setRepeatMode(ValueAnimator.REVERSE);
        a.setRepeatCount(ValueAnimator.INFINITE);
        a.setInterpolator(new android.view.animation.AccelerateDecelerateInterpolator());
        loopWhileAttached(v, a);
    }

    /** Gambar bergerak pelan (zoom masuk-keluar) seperti kamera sinematik. */
    private void kenBurns(View v) {
        ObjectAnimator a = ObjectAnimator.ofPropertyValuesHolder(v,
                android.animation.PropertyValuesHolder.ofFloat(View.SCALE_X, 1f, 1.1f),
                android.animation.PropertyValuesHolder.ofFloat(View.SCALE_Y, 1f, 1.1f));
        a.setDuration(6000);
        a.setRepeatMode(ValueAnimator.REVERSE);
        a.setRepeatCount(ValueAnimator.INFINITE);
        a.setInterpolator(new android.view.animation.AccelerateDecelerateInterpolator());
        loopWhileAttached(v, a);
    }

    /** Kartu kaca: gradasi tiga tahap, pantulan cahaya di atas, kilau yang menyapu, garis tepi terang. */
    private class PremiumTile extends Drawable {
        private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final int base;
        private final int radDp;
        float shine = -1f;

        PremiumTile(int color, int radiusDp) {
            base = color;
            radDp = radiusDp;
        }

        @Override
        public void draw(Canvas cv) {
            RectF rf = new RectF(getBounds());
            float rad = dp(radDp);
            float w = rf.width();
            float h = rf.height();
            p.setStyle(Paint.Style.FILL);
            p.setShader(null);
            // Bayangan lembut buatan sendiri (berlapis, tanpa blur) di bawah dan sekitar kartu.
            int shadowBase = mixColor(base, Color.BLACK, 0.55f);
            for (int i = 9; i >= 1; i--) {
                float g = dp(1) * i * 0.9f;
                p.setColor(Color.argb(9 + (9 - i) * 2, Color.red(shadowBase), Color.green(shadowBase),
                        Color.blue(shadowBase)));
                RectF sr = new RectF(rf.left - g * 0.6f, rf.top - g * 0.15f + dp(2),
                        rf.right + g * 0.6f, rf.bottom + g + dp(2));
                cv.drawRoundRect(sr, rad + g * 0.6f, rad + g * 0.6f, p);
            }
            p.setColor(Color.BLACK); // alpha paint harus 255, kalau tidak gradien kartu ikut transparan
            p.setShader(new LinearGradient(rf.left, rf.top, rf.right, rf.bottom,
                    new int[]{mixColor(base, Color.WHITE, 0.26f), base, mixColor(base, Color.BLACK, 0.24f)},
                    new float[]{0f, 0.5f, 1f}, Shader.TileMode.CLAMP));
            cv.drawRoundRect(rf, rad, rad, p);
            p.setShader(null);
            Path clip = new Path();
            clip.addRoundRect(rf, rad, rad, Path.Direction.CW);
            cv.save();
            cv.clipPath(clip);
            p.setShader(new LinearGradient(0f, rf.top, 0f, rf.top + h * 0.5f,
                    Color.argb(58, 255, 255, 255), Color.argb(0, 255, 255, 255), Shader.TileMode.CLAMP));
            cv.drawOval(new RectF(rf.left - w * 0.15f, rf.top - h * 0.9f, rf.right + w * 0.15f,
                    rf.top + h * 0.38f), p);
            p.setShader(null);
            p.setColor(Color.argb(34, 255, 255, 255));
            cv.drawCircle(rf.right - dp(16), rf.bottom - dp(6), dp(58), p);
            p.setColor(Color.argb(24, 255, 255, 255));
            cv.drawCircle(rf.left + dp(30), rf.bottom + dp(10), dp(40), p);
            p.setColor(Color.argb(200, 255, 255, 255));
            drawSparkle(cv, rf.right - dp(74), rf.top + dp(18), dp(5), p);
            drawSparkle(cv, rf.right - dp(44), rf.bottom - dp(20), dp(3), p);
            if (shine >= 0f && shine <= 1f) {
                float cx = rf.left - h + (w + 2 * h) * shine;
                float bw = w * 0.16f;
                cv.save();
                cv.rotate(18f, rf.centerX(), rf.centerY());
                p.setShader(new LinearGradient(cx, 0f, cx + bw, 0f,
                        new int[]{Color.argb(0, 255, 255, 255), Color.argb(110, 255, 255, 255),
                                Color.argb(0, 255, 255, 255)}, null, Shader.TileMode.CLAMP));
                cv.drawRect(cx, rf.top - h, cx + bw, rf.bottom + h, p);
                p.setShader(null);
                cv.restore();
            }
            cv.restore();
            RectF in = new RectF(rf);
            in.inset(1.5f, 1.5f);
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(Math.max(2f, dp(2) * 0.8f));
            p.setShader(new LinearGradient(0f, rf.top, 0f, rf.bottom,
                    Color.argb(210, 255, 255, 255), Color.argb(40, 255, 255, 255), Shader.TileMode.CLAMP));
            cv.drawRoundRect(in, rad, rad, p);
            p.setShader(null);
            p.setStyle(Paint.Style.FILL);
        }

        @Override
        public void getOutline(Outline outline) {
            outline.setRoundRect(getBounds(), dp(radDp));
            outline.setAlpha(1f);
        }

        @Override
        public void setAlpha(int alpha) {
        }

        @Override
        public void setColorFilter(ColorFilter cf) {
        }

        @Override
        public int getOpacity() {
            return PixelFormat.TRANSLUCENT;
        }
    }

    /**
     * Kartu menu bertema alam (v6.17.0): warna tenang, tepi bawah tebal seperti tanah, dan satu motif alam
     * per kartu (kerikil, daun, ombak, koin). Tanpa kilau bergerak supaya Peta Petualangan jadi satu-satunya sorotan.
     */
    private void natureTile(LinearLayout t, int color, int kind) {
        t.setBackground(new NatureTile(color, kind));
        t.setElevation(0f);
        if (t.getChildCount() > 1 && t.getChildAt(1) instanceof LinearLayout) {
            LinearLayout col = (LinearLayout) t.getChildAt(1);
            if (col.getChildCount() > 1 && col.getChildAt(1) instanceof TextView) {
                TextView sub = (TextView) col.getChildAt(1);
                sub.setTextSize(14f);
                sub.setMaxLines(2);
                sub.setTextColor(Color.WHITE);
            }
        }
    }

    private class NatureTile extends Drawable {
        private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final int base;
        private final int kind;

        NatureTile(int color, int k) {
            base = color;
            kind = k;
        }

        @Override
        public void draw(Canvas cv) {
            RectF all = new RectF(getBounds());
            float rad = dp(24);
            float ledge = dp(4);
            p.setShader(null);
            p.setStyle(Paint.Style.FILL);
            p.setColor(mixColor(base, Color.BLACK, 0.32f));
            cv.drawRoundRect(all, rad, rad, p);
            RectF rf = new RectF(all.left, all.top, all.right, all.bottom - ledge);
            p.setShader(new LinearGradient(0f, rf.top, 0f, rf.bottom,
                    mixColor(base, Color.WHITE, 0.12f), base, Shader.TileMode.CLAMP));
            cv.drawRoundRect(rf, rad, rad, p);
            p.setShader(null);
            Path clip = new Path();
            clip.addRoundRect(rf, rad, rad, Path.Direction.CW);
            cv.save();
            cv.clipPath(clip);
            float cx = rf.right - dp(78);
            float cy = rf.centerY();
            p.setColor(Color.argb(40, 255, 255, 255));
            if (kind == 0) {
                // kerikil: tiga bulatan berbeda ukuran
                cv.drawCircle(cx, cy + dp(6), dp(24), p);
                cv.drawCircle(cx + dp(34), cy - dp(10), dp(15), p);
                cv.drawCircle(cx - dp(30), cy - dp(14), dp(10), p);
            } else if (kind == 1) {
                // daun dengan tulang tengah
                float r = dp(26);
                Path leaf = new Path();
                leaf.moveTo(cx - r, cy + r);
                leaf.quadTo(cx - r, cy - r * 1.3f, cx + r, cy - r);
                leaf.quadTo(cx + r, cy + r * 1.3f, cx - r, cy + r);
                leaf.close();
                cv.drawPath(leaf, p);
                p.setStyle(Paint.Style.STROKE);
                p.setStrokeWidth(dp(2));
                p.setColor(Color.argb(70, 255, 255, 255));
                cv.drawLine(cx - r, cy + r, cx + r * 0.6f, cy - r * 0.6f, p);
                p.setStyle(Paint.Style.FILL);
            } else if (kind == 2) {
                // ombak: tiga garis bergelombang
                p.setStyle(Paint.Style.STROKE);
                p.setStrokeWidth(dp(3));
                p.setStrokeCap(Paint.Cap.ROUND);
                for (int i = 0; i < 3; i++) {
                    Path w = new Path();
                    w.moveTo(cx - dp(60), cy - dp(18) + i * dp(18));
                    for (int sg = 0; sg < 4; sg++) {
                        w.rQuadTo(dp(15), (sg % 2 == 0) ? -dp(8) : dp(8), dp(30), 0f);
                    }
                    cv.drawPath(w, p);
                }
                p.setStyle(Paint.Style.FILL);
            } else {
                // koin: dua lingkaran bertumpuk
                p.setStyle(Paint.Style.STROKE);
                p.setStrokeWidth(dp(3));
                cv.drawCircle(cx, cy, dp(24), p);
                cv.drawCircle(cx + dp(30), cy + dp(8), dp(16), p);
                p.setStyle(Paint.Style.FILL);
            }
            cv.restore();
            p.setStyle(Paint.Style.FILL);
        }

        @Override
        public void getOutline(Outline outline) {
            outline.setRoundRect(getBounds(), dp(24));
            outline.setAlpha(1f);
        }

        @Override
        public void setAlpha(int alpha) {
        }

        @Override
        public void setColorFilter(ColorFilter cf) {
        }

        @Override
        public int getOpacity() {
            return PixelFormat.TRANSLUCENT;
        }
    }

    /** Latar menu: langit pagi, matahari berpendar, awan, bukit, dan daun di sudut layar. */
    private class MenuBgDrawable extends Drawable {
        private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);

        private void cloud(Canvas cv, float x, float y, float r) {
            p.setColor(Color.argb(170, 255, 255, 255));
            cv.drawCircle(x, y, r, p);
            cv.drawCircle(x + r * 0.9f, y + r * 0.15f, r * 0.8f, p);
            cv.drawCircle(x - r * 0.9f, y + r * 0.2f, r * 0.7f, p);
            cv.drawRoundRect(new RectF(x - r * 1.6f, y + r * 0.2f, x + r * 1.7f, y + r * 0.95f),
                    r * 0.4f, r * 0.4f, p);
        }

        private void hill(Canvas cv, int w, int h, float base, float amp, int color, float phase) {
            Path hp = new Path();
            hp.moveTo(0f, h);
            for (float x = 0f; x <= w; x += dp(6)) {
                hp.lineTo(x, h * base + (float) Math.sin(x / w * 6.28318f + phase) * h * amp * 0.3f);
            }
            hp.lineTo(w, h);
            hp.close();
            p.setColor(color);
            cv.drawPath(hp, p);
        }

        private void leaf(Canvas cv, float x, float y, float len, float angle, int color) {
            cv.save();
            cv.translate(x, y);
            cv.rotate(angle);
            Path lf = new Path();
            lf.moveTo(0f, 0f);
            lf.quadTo(len * 0.5f, -len * 0.34f, len, 0f);
            lf.quadTo(len * 0.5f, len * 0.34f, 0f, 0f);
            lf.close();
            p.setStyle(Paint.Style.FILL);
            p.setColor(color);
            cv.drawPath(lf, p);
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(Math.max(1f, dp(1)));
            p.setColor(Color.argb(90, 255, 255, 255));
            cv.drawLine(0f, 0f, len * 0.92f, 0f, p);
            p.setStyle(Paint.Style.FILL);
            cv.restore();
        }

        private void cluster(Canvas cv, float x, float y, float len, float a0) {
            leaf(cv, x, y, len, a0, Color.argb(200, 46, 125, 50));
            leaf(cv, x, y, len * 0.85f, a0 + 28f, Color.argb(190, 67, 160, 71));
            leaf(cv, x, y, len * 0.7f, a0 + 54f, Color.argb(180, 102, 187, 106));
        }

        @Override
        public void draw(Canvas cv) {
            Rect r = getBounds();
            int w = r.width();
            int h = r.height();
            if (w <= 0 || h <= 0) {
                return;
            }
            p.setStyle(Paint.Style.FILL);
            p.setShader(new LinearGradient(0f, 0f, 0f, h,
                    new int[]{Color.parseColor("#A8DCF8"), Color.parseColor("#D6EFFB"),
                            Color.parseColor("#EAF7EF"), Color.parseColor("#FFF8E6")},
                    new float[]{0f, 0.3f, 0.62f, 1f}, Shader.TileMode.CLAMP));
            cv.drawRect(r, p);
            p.setShader(new android.graphics.RadialGradient(w * 0.86f, h * 0.05f, w * 0.6f,
                    new int[]{Color.argb(190, 255, 240, 170), Color.argb(0, 255, 240, 170)}, null,
                    Shader.TileMode.CLAMP));
            cv.drawCircle(w * 0.86f, h * 0.05f, w * 0.6f, p);
            p.setShader(null);
            cloud(cv, w * 0.20f, h * 0.10f, w * 0.10f);
            cloud(cv, w * 0.72f, h * 0.17f, w * 0.07f);
            hill(cv, w, h, 0.90f, 0.12f, Color.argb(120, 129, 199, 132), 0f);
            hill(cv, w, h, 0.945f, 0.08f, Color.argb(150, 76, 175, 80), 1.7f);
            cluster(cv, -w * 0.02f, -h * 0.01f, w * 0.30f, 14f);
            cluster(cv, w * 1.02f, -h * 0.01f, w * 0.26f, 100f);
            cluster(cv, -w * 0.02f, h * 1.01f, w * 0.30f, -80f);
            cluster(cv, w * 1.02f, h * 1.01f, w * 0.30f, -170f);
            p.setColor(Color.argb(210, 255, 255, 255));
            drawSparkle(cv, w * 0.30f, h * 0.05f, dp(6), p);
            drawSparkle(cv, w * 0.93f, h * 0.30f, dp(5), p);
            drawSparkle(cv, w * 0.07f, h * 0.60f, dp(4), p);
        }

        @Override
        public void setAlpha(int alpha) {
        }

        @Override
        public void setColorFilter(ColorFilter cf) {
        }

        @Override
        public int getOpacity() {
            return PixelFormat.OPAQUE;
        }
    }

    // ---------- Pembantu tampilan menu baru ----------

    /** Gambar dari res/drawable (mis. menu_island, menu_cube); null bila berkasnya belum ada. */
    // ---- Aset gambar Kebun Hafalan (res/drawable-nodpi). Bila berkas tidak ada, tampilan lama dipakai. ----
    private final HashMap<String, android.graphics.Bitmap> kBmpCache = new HashMap<>();
    private final Paint kPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private final RectF kRf = new RectF();
    private final Rect kSrc = new Rect();

    private android.graphics.Bitmap kBmp(String n) {
        if (kBmpCache.containsKey(n)) {
            return kBmpCache.get(n);
        }
        android.graphics.Bitmap b = null;
        int id = getResources().getIdentifier(n, "drawable", getPackageName());
        if (id != 0) {
            try {
                b = android.graphics.BitmapFactory.decodeResource(getResources(), id);
            } catch (Throwable e) {
                b = null;
            }
        }
        kBmpCache.put(n, b);
        return b;
    }

    private boolean kCover(Canvas c, String n, float w, float h, float anchorY) {
        android.graphics.Bitmap b = kBmp(n);
        if (b == null) {
            return false;
        }
        float s = Math.max(w / b.getWidth(), h / b.getHeight());
        float dw = b.getWidth() * s;
        float dh = b.getHeight() * s;
        float t = -(dh - h) * anchorY;
        kRf.set((w - dw) / 2f, t, (w - dw) / 2f + dw, t + dh);
        c.drawBitmap(b, null, kRf, kPaint);
        return true;
    }

    private void kPiece(Canvas c, android.graphics.Bitmap b, int sy0, int sy1, float l, float t, float r, float bt) {
        kSrc.set(0, sy0, b.getWidth(), sy1);
        kRf.set(l, t, r, bt);
        c.drawBitmap(b, kSrc, kRf, kPaint);
    }

    private View kIconView(String name, View fallback) {
        Drawable d = artDrawable(name);
        if (d == null) {
            return fallback;
        }
        ImageView iv = new ImageView(this);
        iv.setImageDrawable(d);
        iv.setScaleType(ImageView.ScaleType.FIT_CENTER);
        return iv;
    }

    /** Tunas untuk benih/tunas (tahap 0-1); pohon tetap digambar kode. */
    private View seedIcon(int stage) {
        View f = new SeedIcon(this, stage);
        return stage <= 1 ? kIconView("ic_tunas", f) : f;
    }

    /** Ikon gambar di kiri teks; teks diganti (tanpa emoji) hanya bila gambarnya ada. */
    private void iconLeft(TextView t, String name, int hDp, String plain) {
        Drawable d = artDrawable(name);
        if (d == null) {
            return;
        }
        int iw = Math.max(1, d.getIntrinsicWidth());
        int ih = Math.max(1, d.getIntrinsicHeight());
        d.setBounds(0, 0, dp(hDp) * iw / ih, dp(hDp));
        t.setCompoundDrawables(d, null, null, null);
        t.setCompoundDrawablePadding(dp(6));
        t.setText(plain);
    }

    private Drawable artDrawable(String name) {
        int id = getResources().getIdentifier(name, "drawable", getPackageName());
        return id != 0 ? getResources().getDrawable(id, null) : null;
    }

    /** Ganti ikon emoji di kiri kartu dengan gambar (bila berkasnya ada); gambar melayang pelan. */
    private void swapTileIcon(LinearLayout t, String artName) {
        Drawable d = artDrawable(artName);
        if (d == null) {
            return;
        }
        t.removeViewAt(0);
        ImageView iv = new ImageView(this);
        iv.setImageDrawable(d);
        iv.setScaleType(ImageView.ScaleType.FIT_CENTER);
        LinearLayout.LayoutParams lpi = new LinearLayout.LayoutParams(dp(96), dp(76));
        lpi.rightMargin = dp(8);
        lpi.leftMargin = dp(-8);
        t.addView(iv, 0, lpi);
        floatAnim(iv, 3, 1900L);
    }

    /** Judul bagian dengan daun di kiri dan kanan serta garis tipis di ujung. */
    private LinearLayout sectionTitleLeaf(String s) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        int lineC = Color.argb(120, 197, 138, 26);
        View l1 = new View(this);
        l1.setBackground(box(lineC, 2));
        View l2 = new View(this);
        l2.setBackground(box(lineC, 2));
        LinearLayout.LayoutParams a = new LinearLayout.LayoutParams(0, dp(2), 1f);
        a.rightMargin = dp(8);
        LinearLayout.LayoutParams b = new LinearLayout.LayoutParams(0, dp(2), 1f);
        b.leftMargin = dp(8);
        row.addView(l1, a);
        row.addView(text("\uD83C\uDF3F " + s + " \uD83C\uDF3F", 18, INK, true),
                new LinearLayout.LayoutParams(WRAP, WRAP));
        row.addView(l2, b);
        return row;
    }

    /** Kartu Peta Petualangan: pulau di kiri, nama dunia, titik kemajuan level, dan panah. */
    private LinearLayout petaCard() {
        LinearLayout t = new LinearLayout(this);
        t.setOrientation(LinearLayout.HORIZONTAL);
        t.setGravity(Gravity.CENTER_VERTICAL);
        t.setPadding(dp(8), dp(8), dp(12), dp(8));
        premium(t, Color.parseColor("#0B6FB8"), 30, 700L);

        Drawable art = artDrawable("menu_island");
        if (art != null) {
            FrameLayout frame = new FrameLayout(this);
            final float rad = dp(24);
            frame.setOutlineProvider(new android.view.ViewOutlineProvider() {
                @Override
                public void getOutline(View v, Outline o) {
                    o.setRoundRect(0, 0, v.getWidth(), v.getHeight(), rad);
                }
            });
            frame.setClipToOutline(true);
            ImageView iv = new ImageView(this);
            iv.setImageDrawable(art);
            iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
            frame.addView(iv, new FrameLayout.LayoutParams(MATCH, MATCH));
            kenBurns(iv);
            t.addView(frame, new LinearLayout.LayoutParams(dp(124), MATCH));
        } else {
            TextView em = text("\uD83C\uDFDD\uFE0F", 52, Color.WHITE, false);
            em.setBackground(box(Color.argb(60, 255, 255, 255), 24));
            t.addView(em, new LinearLayout.LayoutParams(dp(124), MATCH));
        }

        LinearLayout col = new LinearLayout(this);
        col.setOrientation(LinearLayout.VERTICAL);
        col.setGravity(Gravity.CENTER_VERTICAL);
        TextView n = text("Peta Petualangan", 22, Color.WHITE, true);
        n.setGravity(Gravity.START);
        n.setMaxLines(2);
        n.setShadowLayer(dp(2), 0f, dp(1), Color.argb(80, 0, 0, 0));
        col.addView(n, new LinearLayout.LayoutParams(WRAP, WRAP));
        TextView sub = text(advSummary(), 14, Color.parseColor("#E1F5FE"), false);
        sub.setGravity(Gravity.START);
        sub.setMaxLines(2);
        col.addView(sub, lpw(2));

        // Titik kemajuan: emas = level selesai, biru muda = belum, bintang di level terakhir
        LinearLayout dots = new LinearLayout(this);
        dots.setOrientation(LinearLayout.HORIZONTAL);
        dots.setGravity(Gravity.CENTER_VERTICAL);
        dots.setPadding(dp(8), dp(5), dp(8), dp(5));
        dots.setBackground(box(Color.argb(70, 0, 70, 140), 14));
        int total = LEVELS[kelas].length;
        for (int l = 0; l < total; l++) {
            if (l == total - 1 && total > 1) {
                TextView star = text("\u2605", 11, Color.parseColor("#FFC107"), true);
                dots.addView(star, new LinearLayout.LayoutParams(WRAP, WRAP));
            } else {
                GradientDrawable dg = new GradientDrawable();
                dg.setShape(GradientDrawable.OVAL);
                dg.setColor(advStarsOf(kelas, l) >= 1 ? Color.parseColor("#FFC107")
                        : Color.parseColor("#9FD8F7"));
                LinearLayout.LayoutParams dlp = new LinearLayout.LayoutParams(dp(7), dp(7));
                dlp.rightMargin = dp(3);
                View dv = new View(this);
                dv.setBackground(dg);
                dots.addView(dv, dlp);
            }
        }
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.addView(dots, new LinearLayout.LayoutParams(WRAP, WRAP));
        TextView go = text("\u203A", 22, Color.WHITE, true);
        go.setBackground(box(Color.argb(90, 255, 255, 255), 15));
        LinearLayout.LayoutParams golp = new LinearLayout.LayoutParams(dp(30), dp(30));
        golp.leftMargin = dp(6);
        row.addView(go, golp);
        pulse(go);
        col.addView(row, lpw(8));

        LinearLayout.LayoutParams colLp = new LinearLayout.LayoutParams(0, WRAP, 1f);
        colLp.leftMargin = dp(10);
        t.addView(col, colLp);
        t.setClickable(true);
        t.setFocusable(true);
        pressable(t);
        return t;
    }

    // ---------- Rekor skor tertinggi (disimpan di HP ini) ----------

    private String recordLine(String label, int k, String subjPart) {
        int free = prefs.getInt("best_k" + k + subjPart, 0);
        int timedBest = prefs.getInt("best_k" + k + subjPart + "_t", 0);
        if (free <= 0 && timedBest <= 0) {
            return "";
        }
        String s = label + ": ";
        if (free > 0) {
            s += "tanpa waktu " + free;
        }
        if (timedBest > 0) {
            s += (free > 0 ? " \u2022 " : "") + "berwaktu " + timedBest;
        }
        return s;
    }

    // ---------- PIN guru ----------
    // PIN 4 angka menjaga halaman Soal Guru. Yang disimpan hanya sidik (hash), bukan PIN-nya.
    // Kode cadangan (8 huruf/angka) dibuat saat PIN disimpan dan dipakai kalau PIN lupa.
    // Ini kunci kelas, bukan pengaman bank: PIN bisa direset juga lewat hapus data aplikasi.

    private static final String CODE_CHARS = "23456789ABCDEFGHJKMNPQRSTUVWXYZ";
    private static final int PIN_MAX_FAIL = 5;
    private static final long PIN_LOCK_MS = 60000L;

    private String sha(String x) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] d = md.digest(x.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte bt : d) {
                sb.append(String.format(Locale.ROOT, "%02x", bt & 0xff));
            }
            return sb.toString();
        } catch (Exception e) {
            return "";
        }
    }

    private String pinSalt() {
        String sl = prefs.getString("pin_salt", "");
        if (sl.isEmpty()) {
            sl = sha(System.nanoTime() + ":" + rnd.nextLong()).substring(0, 16);
            prefs.edit().putString("pin_salt", sl).apply();
        }
        return sl;
    }

    private boolean hasPin() {
        return !prefs.getString("pin_hash", "").isEmpty();
    }

    private boolean pinOk(String pin) {
        return sha(pinSalt() + ":pin:" + pin).equals(prefs.getString("pin_hash", ""));
    }

    private String normCode(String x) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < x.length(); i++) {
            char ch = Character.toUpperCase(x.charAt(i));
            if (Character.isLetterOrDigit(ch)) {
                sb.append(ch);
            }
        }
        return sb.toString();
    }

    private String newCode() {
        java.security.SecureRandom sr = new java.security.SecureRandom();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            if (i == 4) {
                sb.append('-');
            }
            sb.append(CODE_CHARS.charAt(sr.nextInt(CODE_CHARS.length())));
        }
        return sb.toString();
    }

    // Simpan PIN baru dan kode cadangan baru; mengembalikan kode untuk dicatat guru
    private String savePin(String pin) {
        String code = newCode();
        String salt = pinSalt();
        prefs.edit()
                .putString("pin_hash", sha(salt + ":pin:" + pin))
                .putString("rec_hash", sha(salt + ":rec:" + normCode(code)))
                .putInt("pin_fail", 0)
                .putLong("pin_lock", 0L)
                .apply();
        return code;
    }

    private void clearPin() {
        prefs.edit().remove("pin_hash").remove("rec_hash").putInt("pin_fail", 0)
                .putLong("pin_lock", 0L).apply();
    }

    // Pintu masuk Soal Guru
    private void openGuru() {
        importMsg = "";
        if (!hasPin()) {
            dialogBuatPin(true);
            return;
        }
        long left = prefs.getLong("pin_lock", 0L) - System.currentTimeMillis();
        if (left > 0) {
            Toast.makeText(this, "Terlalu banyak salah. Coba lagi " + (left / 1000 + 1) + " detik lagi.",
                    Toast.LENGTH_LONG).show();
            return;
        }
        dialogMasukPin();
    }

    private EditText pinField(String hint) {
        EditText e = new EditText(this);
        e.setHint(hint);
        e.setInputType(android.text.InputType.TYPE_CLASS_NUMBER
                | android.text.InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        e.setFilters(new InputFilter[]{new InputFilter.LengthFilter(4)});
        e.setSingleLine(true);
        e.setGravity(Gravity.CENTER);
        e.setTextSize(24 * ui);
        e.setTextColor(DARK);
        e.setBackground(outline(PURPLE, 12));
        return e;
    }

    private LinearLayout cardBody(String title) {
        LinearLayout c = new LinearLayout(this);
        c.setOrientation(LinearLayout.VERTICAL);
        c.setPadding(dp(18), dp(16), dp(18), dp(16));
        c.addView(text(title, 22, ORANGE_DARK, true), lp(WRAP, 0));
        return c;
    }

    // Kotak kecil di tengah layar dengan keyboard yang bisa muncul
    private AlertDialog showCard(LinearLayout c, int widthDp, boolean cancelable) {
        if (pickDialog != null && pickDialog.isShowing()) {
            pickDialog.dismiss();
        }
        ScrollView sv = new ScrollView(this);
        sv.addView(c);
        AlertDialog d = new AlertDialog.Builder(this).setView(sv).create();
        d.setCancelable(cancelable);
        d.setCanceledOnTouchOutside(cancelable);
        if (d.getWindow() != null) {
            d.getWindow().clearFlags(android.view.WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    | android.view.WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
            d.getWindow().setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
                    | android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        }
        d.show();
        if (d.getWindow() != null) {
            d.getWindow().setBackgroundDrawable(box(Color.WHITE, 24));
            int maxW = getResources().getDisplayMetrics().widthPixels - dp(24);
            d.getWindow().setLayout(Math.min(dp(widthDp), maxW), ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        pickDialog = d;
        return d;
    }

    private void dialogMasukPin() {
        LinearLayout c = cardBody("PIN guru");
        TextView info = text("Masukkan PIN untuk membuka Soal Guru.", 13, DARK, false);
        info.setGravity(Gravity.START);
        c.addView(info, lp(WRAP, 8));
        final EditText p = pinField("PIN 4 angka");
        c.addView(p, lp(dp(54), 12));
        final TextView err = text(" ", 13, RED, true);
        c.addView(err, lp(WRAP, 8));
        Button masuk = button("Masuk", GREEN, 18);
        flat(masuk);
        c.addView(masuk, lp(dp(52), 10));
        Button lupa = button("Lupa PIN?", GRAY, 14);
        lupa.setTextColor(DARK);
        flat(lupa);
        c.addView(lupa, lp(dp(44), 8));
        Button batal = button("Batal", GRAY, 14);
        batal.setTextColor(DARK);
        flat(batal);
        c.addView(batal, lp(dp(44), 8));
        final AlertDialog d = showCard(c, 340, true);
        masuk.setOnClickListener(v -> {
            if (pinOk(p.getText().toString())) {
                prefs.edit().putInt("pin_fail", 0).apply();
                d.dismiss();
                importMsg = "";
                showGuru();
                return;
            }
            int f = prefs.getInt("pin_fail", 0) + 1;
            if (f >= PIN_MAX_FAIL) {
                prefs.edit().putInt("pin_fail", 0)
                        .putLong("pin_lock", System.currentTimeMillis() + PIN_LOCK_MS).apply();
                d.dismiss();
                Toast.makeText(this, "PIN salah " + PIN_MAX_FAIL + " kali. Coba lagi 1 menit lagi.",
                        Toast.LENGTH_LONG).show();
            } else {
                prefs.edit().putInt("pin_fail", f).apply();
                err.setText("PIN salah. Sisa percobaan: " + (PIN_MAX_FAIL - f));
                p.setText("");
            }
        });
        lupa.setOnClickListener(v -> {
            d.dismiss();
            dialogLupaPin();
        });
        batal.setOnClickListener(v -> d.dismiss());
    }

    private void dialogLupaPin() {
        LinearLayout c = cardBody("Lupa PIN?");
        TextView info = text("Masukkan kode cadangan yang dicatat saat PIN dibuat. "
                + "Kalau kode hilang, hapus data aplikasi lewat Pengaturan HP "
                + "(soal guru ikut terhapus).", 13, DARK, false);
        info.setGravity(Gravity.START);
        c.addView(info, lp(WRAP, 8));
        final EditText e = new EditText(this);
        e.setHint("Kode cadangan");
        e.setInputType(android.text.InputType.TYPE_CLASS_TEXT
                | android.text.InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS
                | android.text.InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        e.setFilters(new InputFilter[]{new InputFilter.LengthFilter(12)});
        e.setSingleLine(true);
        e.setGravity(Gravity.CENTER);
        e.setTextSize(20 * ui);
        e.setTextColor(DARK);
        e.setBackground(outline(PURPLE, 12));
        c.addView(e, lp(dp(54), 12));
        final TextView err = text(" ", 13, RED, true);
        c.addView(err, lp(WRAP, 8));
        Button lanjut = button("Lanjut", GREEN, 18);
        flat(lanjut);
        c.addView(lanjut, lp(dp(52), 10));
        Button batal = button("Batal", GRAY, 14);
        batal.setTextColor(DARK);
        flat(batal);
        c.addView(batal, lp(dp(44), 8));
        final AlertDialog d = showCard(c, 340, true);
        lanjut.setOnClickListener(v -> {
            String h = sha(pinSalt() + ":rec:" + normCode(e.getText().toString()));
            if (!prefs.getString("rec_hash", "").isEmpty() && h.equals(prefs.getString("rec_hash", ""))) {
                d.dismiss();
                dialogBuatPin(false);
            } else {
                err.setText("Kode cadangan tidak cocok.");
            }
        });
        batal.setOnClickListener(v -> d.dismiss());
    }

    // Buat PIN pertama (firstTime) atau ganti PIN
    private void dialogBuatPin(final boolean firstTime) {
        LinearLayout c = cardBody(hasPin() ? "PIN baru" : "Buat PIN guru");
        TextView info = text("PIN 4 angka supaya murid tidak bisa membuka Soal Guru. Hanya guru yang perlu tahu.",
                13, DARK, false);
        info.setGravity(Gravity.START);
        c.addView(info, lp(WRAP, 8));
        final EditText p1 = pinField("PIN baru");
        final EditText p2 = pinField("Ulangi PIN");
        c.addView(p1, lp(dp(54), 12));
        c.addView(p2, lp(dp(54), 8));
        final TextView err = text(" ", 13, RED, true);
        c.addView(err, lp(WRAP, 8));
        Button ok = button("Simpan PIN", GREEN, 18);
        flat(ok);
        c.addView(ok, lp(dp(52), 10));
        Button skip = button(firstTime ? "Lewati (tanpa PIN)" : "Batal", GRAY, 14);
        skip.setTextColor(DARK);
        flat(skip);
        c.addView(skip, lp(dp(44), 8));
        final AlertDialog d = showCard(c, 340, true);
        ok.setOnClickListener(v -> {
            String a1 = p1.getText().toString();
            String a2 = p2.getText().toString();
            if (a1.length() != 4) {
                err.setText("PIN harus 4 angka.");
                return;
            }
            if (!a1.equals(a2)) {
                err.setText("Kedua PIN tidak sama.");
                return;
            }
            String code = savePin(a1);
            d.dismiss();
            dialogKodeCadangan(code);
        });
        skip.setOnClickListener(v -> {
            d.dismiss();
            if (firstTime) {
                importMsg = "";
                showGuru();
            }
        });
    }

    // Kode cadangan hanya tampil sekali, setelah PIN disimpan
    private void dialogKodeCadangan(String code) {
        LinearLayout c = cardBody("Catat kode cadangan");
        TextView cd = text(code, 32, PURPLE, true);
        cd.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
        cd.setBackground(box(GRAY, 16));
        cd.setPadding(dp(8), dp(14), dp(8), dp(14));
        c.addView(cd, lp(WRAP, 14));
        TextView info = text("Tulis kode ini di kertas dan simpan di tempat aman, jauh dari murid. "
                + "Kode dipakai kalau PIN lupa, dan hanya tampil sekarang. "
                + "Kalau kode tidak sempat dicatat, buat PIN baru lewat tombol Ganti PIN.",
                13, DARK, false);
        info.setGravity(Gravity.START);
        c.addView(info, lp(WRAP, 12));
        Button ok = button("Sudah saya catat", GREEN, 18);
        flat(ok);
        c.addView(ok, lp(dp(52), 14));
        final AlertDialog d = showCard(c, 340, false);
        ok.setOnClickListener(v -> {
            d.dismiss();
            importMsg = "Berhasil: PIN guru disimpan.";
            showGuru();
        });
    }

    // ---------- Pilihan nyawa (kotak dari tombol di menu) ----------

    private void dialogNyawa() {
        LinearLayout b = dialogBody();
        b.addView(chips(new String[]{"Ada nyawa", "Santai"}, relaxed ? 1 : 0,
                new int[]{RED, GREEN}, kn -> {
                    relaxed = (kn == 1);
                    prefs.edit().putBoolean("relaxed", relaxed).apply();
                    dialogNyawa();
                }), lp(dp(50), 0));
        b.addView(text(relaxed
                ? "Belajar santai: nyawa tidak berkurang, soal jalan terus sampai selesai. "
                + "Skor tidak masuk rekor."
                : "Nyawa berkurang setiap salah. Kalau nyawa habis, permainan berhenti.",
                13, DARK, false), lp(WRAP, 8));
        showDialogView("Nyawa", b);
    }

    // ---------- Lencana dan bintang terkumpul (disimpan di HP ini) ----------

    private boolean hasBadge(int i) {
        return prefs.getBoolean("badge" + i, false);
    }

    private void giveBadge(int i, ArrayList<Integer> fresh) {
        if (hasBadge(i)) {
            return;
        }
        prefs.edit().putBoolean("badge" + i, true).apply();
        fresh.add(i);
    }

    private int totalStars() {
        return prefs.getInt("stars_total", 0);
    }

    // Dipanggil sekali setiap satu permainan selesai.
    // kind: 0 kuis main sendiri, 1 kuis bergantian, 2 Cocokkan Pasangan
    private ArrayList<Integer> awardBadges(int kind, int stars, boolean perfect, boolean rekorBroken) {
        ArrayList<Integer> fresh = new ArrayList<>();
        int games = prefs.getInt("games_total", 0) + 1;
        int sum = prefs.getInt("stars_total", 0) + stars;
        prefs.edit().putInt("games_total", games).putInt("stars_total", sum).apply();
        // Koin: 10 per bintang, bonus untuk nilai sempurna dan rekor baru
        int gain = stars > 0 ? stars * 10 + (perfect ? 10 : 0) + (rekorBroken ? 5 : 0) : 0;
        lastCoinGain = gain;
        if (gain > 0) {
            prefs.edit().putInt("coins", prefs.getInt("coins", 0) + gain).apply();
        }
        giveBadge(0, fresh);
        if (stars >= 3) {
            giveBadge(1, fresh);
        }
        if (kind == 0 && perfect) {
            giveBadge(2, fresh);
            if (timed) {
                giveBadge(3, fresh);
            }
        }
        if (games >= 10) {
            giveBadge(4, fresh);
        }
        if (sum >= 30) {
            giveBadge(5, fresh);
        }
        if (rekorBroken) {
            giveBadge(6, fresh);
        }
        if (kind == 2 && perfect) {
            giveBadge(7, fresh);
        }
        if (kind == 1) {
            giveBadge(8, fresh);
        }
        return fresh;
    }

    // Kartu "Lencana baru!" di layar hasil
    private void addFreshBadges(LinearLayout c) {
        if (lastCoinGain > 0) {
            LinearLayout cc = card();
            cc.setGravity(Gravity.CENTER_HORIZONTAL);
            cc.addView(text("\uD83D\uDCB0 +" + lastCoinGain + " koin!", 20, ORANGE_DARK, true), lp(WRAP, 0));
            cc.addView(text("Total " + coins() + " koin", 13, DARK, false), lp(WRAP, 4));
            Button sh = button("\uD83D\uDECD\uFE0F Belanja di Toko Avatar", PINK, 15);
            flat(sh);
            sh.setElevation(dp(3));
            cc.addView(sh, lp(dp(46), 10));
            sh.setOnClickListener(v -> showToko());
            c.addView(cc, lp(WRAP, 14));
        }
        if (freshBadges == null || freshBadges.isEmpty()) {
            return;
        }
        LinearLayout cd = card();
        cd.addView(text("\uD83C\uDF89 Lencana baru!", 18, PURPLE, true), lp(WRAP, 0));
        for (int n = 0; n < freshBadges.size(); n++) {
            int b = freshBadges.get(n);
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            TextView ic = text(BADGE_ICON[b], 26, Color.WHITE, false);
            ic.setBackground(box(BADGE_COLOR[b], 24));
            row.addView(ic, new LinearLayout.LayoutParams(dp(48), dp(48)));
            LinearLayout col = new LinearLayout(this);
            col.setOrientation(LinearLayout.VERTICAL);
            TextView nm = text(BADGE_NAME[b], 16, DARK, true);
            nm.setGravity(Gravity.START);
            TextView ds = text(BADGE_DESC[b], 12, DARK, false);
            ds.setGravity(Gravity.START);
            col.addView(nm, new LinearLayout.LayoutParams(WRAP, WRAP));
            col.addView(ds, new LinearLayout.LayoutParams(WRAP, WRAP));
            LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(0, WRAP, 1f);
            cp.leftMargin = dp(12);
            row.addView(col, cp);
            cd.addView(row, lp(WRAP, 10));
            ic.setScaleX(0f);
            ic.setScaleY(0f);
            ic.animate().scaleX(1f).scaleY(1f).setStartDelay(700 + 250L * n).setDuration(450)
                    .setInterpolator(new OvershootInterpolator(3f)).start();
        }
        c.addView(cd, lp(WRAP, 14));
    }

    // Papan semua lencana (3 x 3) di layar Rekor; yang belum didapat berupa gembok
    private void addBadgeBoard(LinearLayout c) {
        int earned = 0;
        for (int i = 0; i < BADGE_NAME.length; i++) {
            if (hasBadge(i)) {
                earned++;
            }
        }
        LinearLayout bd = card();
        bd.addView(heading("\uD83C\uDFC5 Lencana (" + earned + " dari " + BADGE_NAME.length + ")"),
                lp(WRAP, 0));
        TextView tot = text("\uD83C\uDF1F Total bintang: " + totalStars(), 14, DARK, false);
        tot.setGravity(Gravity.START);
        bd.addView(tot, lp(WRAP, 6));
        for (int r = 0; r < 4; r++) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            for (int q = 0; q < 3; q++) {
                final int b = r * 3 + q;
                boolean on = hasBadge(b);
                LinearLayout cell = new LinearLayout(this);
                cell.setOrientation(LinearLayout.VERTICAL);
                cell.setGravity(Gravity.CENTER);
                cell.setPadding(dp(4), dp(4), dp(4), dp(4));
                cell.setBackground(box(on ? BADGE_COLOR[b] : GRAY, 16));
                cell.addView(text(on ? BADGE_ICON[b] : "\uD83D\uDD12", 26, Color.WHITE, false),
                        new LinearLayout.LayoutParams(WRAP, WRAP));
                TextView nm = text(BADGE_NAME[b], 11, on ? Color.WHITE : DARK, true);
                nm.setMaxLines(2);
                cell.addView(nm, new LinearLayout.LayoutParams(WRAP, WRAP));
                cell.setOnClickListener(v -> Toast.makeText(this,
                        BADGE_NAME[b] + ": " + BADGE_DESC[b], Toast.LENGTH_SHORT).show());
                row.addView(cell, weight(q == 0 ? 0 : 4, q == 2 ? 0 : 4));
            }
            bd.addView(row, lp(dp(88), r == 0 ? 10 : 8));
        }
        c.addView(bd, lp(WRAP, 14));
    }

    private void showRekor() {
        handler.removeCallbacksAndMessages(null);
        setScreen(8);
        nameFields = null;

        LinearLayout c = column();
        c.addView(text("\uD83C\uDFC6 Rekor & Lencana", 28, ORANGE_DARK, true), lp(WRAP, 0));
        c.addView(text("Skor tertinggi main sendiri dan lencana di HP ini", 14, DARK, false), lp(WRAP, 6));
        addBadgeBoard(c);

        for (int k = 0; k < 6; k++) {
            ArrayList<String> lines = new ArrayList<>();
            String[] parts = {
                    recordLine("Matematika", k, ""),
                    recordLine("Matematika (soal guru)", k, "_b0"),
                    recordLine("Bahasa Indonesia", k, "_b1"),
                    recordLine("Bahasa Inggris", k, "_b2"),
                    recordLine("Kuis Alam: Matematika", k, "_b0a"),
                    recordLine("Kuis Alam: Indonesia", k, "_b1a"),
                    recordLine("Kuis Alam: Inggris", k, "_b2a")
            };
            for (String p : parts) {
                if (!p.isEmpty()) {
                    lines.add(p);
                }
            }
            LinearLayout cd = card();
            LinearLayout head = new LinearLayout(this);
            head.setOrientation(LinearLayout.HORIZONTAL);
            head.setGravity(Gravity.CENTER_VERTICAL);
            head.addView(pill(KELAS_NAMES[k], KELAS_COLOR[k]), new LinearLayout.LayoutParams(WRAP, WRAP));
            cd.addView(head, lp(WRAP, 0));
            if (lines.isEmpty()) {
                TextView none = text("Belum ada rekor", 14, DARK, false);
                none.setGravity(Gravity.START);
                cd.addView(none, lp(WRAP, 8));
            } else {
                for (String ln : lines) {
                    TextView tv = text(ln, 15, DARK, true);
                    tv.setGravity(Gravity.START);
                    cd.addView(tv, lp(WRAP, 8));
                }
            }
            c.addView(cd, lp(WRAP, 14));
        }

        Button back = button("Kembali", GRAY, 14);
        back.setTextColor(DARK);
        c.addView(back, lp(dp(46), 18));
        back.setOnClickListener(v -> showMenu());

        wrap(c);
    }

    private LinearLayout subjectTile(String icon, String label, String sub, int color) {
        LinearLayout t = new LinearLayout(this);
        t.setOrientation(LinearLayout.HORIZONTAL);
        t.setGravity(Gravity.CENTER_VERTICAL);
        t.setPadding(dp(18), 0, dp(18), 0);
        t.setBackground(new TileDrawable(color));
        t.setElevation(dp(5));
        TextView ic = text(icon, 30, Color.WHITE, false);
        ic.setBackground(box(Color.argb(70, 255, 255, 255), 28));
        LinearLayout.LayoutParams icp = new LinearLayout.LayoutParams(dp(56), dp(56));
        icp.rightMargin = dp(14);
        t.addView(ic, icp);
        LinearLayout col = new LinearLayout(this);
        col.setOrientation(LinearLayout.VERTICAL);
        TextView n = text(label, 22, Color.WHITE, true);
        n.setGravity(Gravity.START);
        TextView s = text(sub, 13, Color.parseColor("#FFF3E0"), false);
        s.setGravity(Gravity.START);
        col.addView(n, new LinearLayout.LayoutParams(WRAP, WRAP));
        col.addView(s, new LinearLayout.LayoutParams(WRAP, WRAP));
        t.addView(col, new LinearLayout.LayoutParams(0, WRAP, 1f));
        TextView go = text("\u203A", 26, Color.WHITE, true);
        go.setBackground(box(Color.argb(60, 255, 255, 255), 17));
        go.setGravity(Gravity.CENTER);
        t.addView(go, new LinearLayout.LayoutParams(dp(34), dp(34)));
        t.setClickable(true);
        t.setFocusable(true);
        pressable(t);
        return t;
    }

    private boolean quizAvailable(int s) {
        return countBank(s, kelas) > 0 || s == 1 || s == 2;
    }

    private void openSubject(int s) {
        if (s == 0) {
            showMath();
            return;
        }
        if (s == 1 || s == 2) {
            // Indonesia dan Inggris selalu punya Kuis Alam, jadi pilihan permainan selalu tampil
            dialogMode(s);
            return;
        }
        boolean q = quizAvailable(s);
        boolean m = matchAvailable(s);
        boolean sk = s == 1 && susunAvailable(0);
        boolean sl = s == 1 && susunAvailable(1);
        int n = (q ? 1 : 0) + (m ? 1 : 0) + (sk ? 1 : 0) + (sl ? 1 : 0);
        if (n == 0) {
            Toast.makeText(this, "Belum ada soal " + SUBJ_NAMES[s] + " untuk " + KELAS_NAMES[kelas]
                    + ". Guru bisa menambahkannya lewat tombol Soal dari Guru.", Toast.LENGTH_LONG).show();
        } else if (n == 1) {
            if (q) {
                startBank(s);
            } else if (m) {
                startMatch(s);
            } else if (sk) {
                startSusun(0);
            } else {
                startSusun(1);
            }
        } else {
            dialogMode(s);
        }
    }

    private void startBank(int s) {
        if (countBank(s, kelas) == 0 && s != 1 && s != 2) {
            showMenu();
            return;
        }
        subj = s;
        advPart = -1;
        alam = false;
        tebak = false;
        sambung = false;
        startGame(7);
    }

    // ---------- Menu Matematika ----------

    private void showMath() {
        handler.removeCallbacksAndMessages(null);
        setScreen(6);
        advLevel = -1;
        nameFields = null;

        LinearLayout c = column();
        c.addView(text("\uD83D\uDD22 Matematika", 26, ORANGE_DARK, true), lp(WRAP, 0));
        c.addView(pill(KELAS_NAMES[kelas], KELAS_COLOR[kelas]), lpw(10));
        c.addView(text(KELAS_DESC[kelas] + " \u2022 "
                + (relaxed ? "tanpa nyawa" : KELAS_LIVES[kelas] + " nyawa"),
                13, DARK, false), lp(WRAP, 10));

        c.addView(text("Pilih permainan", 16, DARK, true), lp(WRAP, 16));
        LinearLayout r1 = new LinearLayout(this);
        r1.setOrientation(LinearLayout.HORIZONTAL);
        c.addView(r1, lp(dp(96), 10));
        r1.addView(tile("+", "Tambah", ORANGE, 0), weight(0, 6));
        r1.addView(tile("\u2212", "Kurang", BLUE, 1), weight(6, 0));
        if (kelas >= 2) {
            // Kali dan Bagi disembunyikan di Kelas 1-2
            LinearLayout r2 = new LinearLayout(this);
            r2.setOrientation(LinearLayout.HORIZONTAL);
            c.addView(r2, lp(dp(96), 12));
            r2.addView(tile("\u00D7", "Kali", GREEN, 2), weight(0, 6));
            r2.addView(tile("\u00F7", "Bagi", TEAL, 3), weight(6, 0));
        }
        LinearLayout r3 = new LinearLayout(this);
        r3.setOrientation(LinearLayout.HORIZONTAL);
        c.addView(r3, lp(dp(96), 12));
        r3.addView(tile("\uD83C\uDFB2", "Campur", PURPLE, 4), weight(0, 6));
        r3.addView(tile("+\u2212\u00D7", "3 Bilangan", PINK, 5), weight(6, 0));
        LinearLayout r4 = new LinearLayout(this);
        r4.setOrientation(LinearLayout.HORIZONTAL);
        c.addView(r4, lp(dp(96), 12));
        if (countBank(0, kelas) > 0) {
            r4.addView(tile("?", "Isi Titik", INDIGO, 6), weight(0, 6));
            LinearLayout tg = tile("\uD83D\uDCDD", "Soal Guru", RED, 7);
            tg.setOnClickListener(v -> startBank(0));
            r4.addView(tg, weight(6, 0));
        } else {
            r4.addView(tile("?", "Isi Titik", INDIGO, 6), weight(0, 0));
        }
        LinearLayout r5 = new LinearLayout(this);
        r5.setOrientation(LinearLayout.HORIZONTAL);
        c.addView(r5, lp(dp(96), 12));
        LinearLayout ta = tile("\uD83C\uDF31", "Cerita Alam", GREEN, 7);
        ta.setOnClickListener(v -> startAlam(0));
        r5.addView(ta, weight(0, 0));

        Button back = button("Kembali", GRAY, 14);
        back.setTextColor(DARK);
        c.addView(back, lp(dp(46), 22));
        back.setOnClickListener(v -> showMenu());

        wrap(c);
    }

    // ---------- Soal dari guru: simpan, impor, tampilan ----------

    private int countBank(int s, int k0) {
        int n = 0;
        for (B b : bank) {
            if (b.subj == s && b.kelas == k0 + 1) {
                n++;
            }
        }
        return n;
    }

    private String readAll(InputStream in) throws IOException {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buf = new byte[4096];
            int n;
            int size = 0;
            while ((n = in.read(buf)) != -1) {
                out.write(buf, 0, n);
                size += n;
                if (size > 3000000) {
                    throw new IOException("Berkas terlalu besar");
                }
            }
            return new String(out.toByteArray(), StandardCharsets.UTF_8);
        } finally {
            in.close();
        }
    }

    private void loadBank() {
        bank.clear();
        try {
            JSONArray arr = new JSONArray(readAll(openFileInput(BANK_FILE)));
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.getJSONObject(i);
                B b = new B();
                b.subj = o.getInt("s");
                b.kelas = o.getInt("k");
                b.text = o.getString("q");
                JSONArray op = o.getJSONArray("o");
                for (int j = 0; j < 4; j++) {
                    b.opt[j] = op.getString(j);
                }
                b.key = o.getInt("a");
                b.expl = o.optString("e", "");
                bank.add(b);
            }
        } catch (Exception e) {
            // Belum ada berkas soal atau berkas rusak: mulai dari kosong
        }
    }

    private boolean saveBank() {
        try {
            JSONArray arr = new JSONArray();
            for (B b : bank) {
                JSONObject o = new JSONObject();
                o.put("s", b.subj);
                o.put("k", b.kelas);
                o.put("q", b.text);
                JSONArray op = new JSONArray();
                for (int j = 0; j < 4; j++) {
                    op.put(b.opt[j]);
                }
                o.put("o", op);
                o.put("a", b.key);
                o.put("e", b.expl);
                arr.put(o);
            }
            OutputStream out = openFileOutput(BANK_FILE, MODE_PRIVATE);
            try {
                out.write(arr.toString().getBytes(StandardCharsets.UTF_8));
            } finally {
                out.close();
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void pickFile() {
        Intent it = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        it.addCategory(Intent.CATEGORY_OPENABLE);
        it.setType("*/*");
        try {
            startActivityForResult(it, REQ_IMPORT);
        } catch (Exception e) {
            importMsg = "Tidak bisa membuka pemilih berkas di HP ini.";
            showGuru();
        }
    }

    @Override
    protected void onActivityResult(int req, int res, Intent data) {
        super.onActivityResult(req, res, data);
        if (req == REQ_BACKUP_SAVE) {
            String payload = pendingBackup;
            pendingBackup = "";
            if (res != RESULT_OK || data == null || data.getData() == null || payload.isEmpty()) {
                return;
            }
            writeBackup(data.getData(), payload);
            return;
        }
        if (req == REQ_BACKUP_OPEN) {
            if (res != RESULT_OK || data == null || data.getData() == null) {
                return;
            }
            readBackup(data.getData());
            return;
        }
        if (req != REQ_IMPORT || res != RESULT_OK || data == null || data.getData() == null) {
            return;
        }
        importFrom(data.getData());
    }

    private void importFrom(Uri uri) {
        String s;
        try {
            s = readAll(getContentResolver().openInputStream(uri));
        } catch (Exception e) {
            importMsg = "Gagal membaca berkas. Pastikan berkasnya CSV.";
            showGuru();
            return;
        }
        importMsg = importCsv(s);
        showGuru();
    }

    private int cnt(String s, char ch) {
        int n = 0;
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == ch) {
                n++;
            }
        }
        return n;
    }

    private ArrayList<ArrayList<String>> parseCsv(String s, char d) {
        ArrayList<ArrayList<String>> rows = new ArrayList<>();
        ArrayList<String> row = new ArrayList<>();
        StringBuilder f = new StringBuilder();
        boolean inQ = false;
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            if (inQ) {
                if (ch == '"') {
                    if (i + 1 < s.length() && s.charAt(i + 1) == '"') {
                        f.append('"');
                        i++;
                    } else {
                        inQ = false;
                    }
                } else {
                    f.append(ch);
                }
            } else if (ch == '"') {
                inQ = true;
            } else if (ch == d) {
                row.add(f.toString());
                f.setLength(0);
            } else if (ch == '\n' || ch == '\r') {
                if (ch == '\r' && i + 1 < s.length() && s.charAt(i + 1) == '\n') {
                    i++;
                }
                row.add(f.toString());
                f.setLength(0);
                rows.add(row);
                row = new ArrayList<>();
            } else {
                f.append(ch);
            }
        }
        if (f.length() > 0 || !row.isEmpty()) {
            row.add(f.toString());
            rows.add(row);
        }
        return rows;
    }

    private int col(ArrayList<String> head, String name) {
        for (int i = 0; i < head.size(); i++) {
            if (head.get(i).trim().toLowerCase(Locale.ROOT).equals(name)) {
                return i;
            }
        }
        return -1;
    }

    private String cell(ArrayList<String> row, int idx) {
        if (idx < 0 || idx >= row.size()) {
            return "";
        }
        return row.get(idx).trim();
    }

    private boolean isBlank(ArrayList<String> row) {
        for (String x : row) {
            if (!x.trim().isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private int parseSubject(String m) {
        String x = m.toLowerCase(Locale.ROOT);
        if (x.contains("indo")) {
            return 1;
        }
        if (x.contains("ing") || x.contains("eng")) {
            return 2;
        }
        if (x.contains("mat") || x.contains("hitung")) {
            return 0;
        }
        return -1;
    }

    private int parseKelas(String k) {
        StringBuilder dg = new StringBuilder();
        for (int i = 0; i < k.length(); i++) {
            if (Character.isDigit(k.charAt(i))) {
                dg.append(k.charAt(i));
            }
        }
        if (dg.length() != 1) {
            return 0;
        }
        return dg.charAt(0) - '0';
    }

    private B toBank(ArrayList<String> row, int[] c) {
        int sj = parseSubject(cell(row, c[0]));
        int kl = parseKelas(cell(row, c[1]));
        String q = cell(row, c[2]);
        if (sj < 0 || kl < 1 || kl > 6 || q.isEmpty()) {
            return null;
        }
        B b = new B();
        b.subj = sj;
        b.kelas = kl;
        b.text = q;
        for (int j = 0; j < 4; j++) {
            b.opt[j] = cell(row, c[3 + j]);
            if (b.opt[j].isEmpty()) {
                return null;
            }
        }
        String k = cell(row, c[7]);
        int key = -1;
        if (!k.isEmpty() && k.length() <= 2
                && "ABCD".indexOf(Character.toUpperCase(k.charAt(0))) >= 0
                && (k.length() == 1 || !Character.isLetterOrDigit(k.charAt(1)))) {
            key = "ABCD".indexOf(Character.toUpperCase(k.charAt(0)));
        } else {
            for (int j = 0; j < 4; j++) {
                if (k.equalsIgnoreCase(b.opt[j])) {
                    key = j;
                }
            }
        }
        if (key < 0) {
            return null;
        }
        b.key = key;
        b.expl = cell(row, c[8]);
        return b;
    }

    private int findBank(B b) {
        for (int i = 0; i < bank.size(); i++) {
            B x = bank.get(i);
            if (x.subj == b.subj && x.kelas == b.kelas && x.text.equalsIgnoreCase(b.text)) {
                return i;
            }
        }
        return -1;
    }

    private String importCsv(String s) {
        if (s.length() > 0 && s.charAt(0) == '\uFEFF') {
            s = s.substring(1);
        }
        int nl = s.indexOf('\n');
        String first = nl >= 0 ? s.substring(0, nl) : s;
        char d = ',';
        int most = cnt(first, ',');
        if (cnt(first, ';') > most) {
            d = ';';
            most = cnt(first, ';');
        }
        if (cnt(first, '\t') > most) {
            d = '\t';
        }
        ArrayList<ArrayList<String>> rows = parseCsv(s, d);
        if (rows.isEmpty()) {
            return "Berkas kosong.";
        }
        ArrayList<String> head = rows.get(0);
        String[] heads = {"mapel", "kelas", "soal", "a", "b", "c", "d", "kunci", "pembahasan"};
        int[] c = new int[9];
        for (int i = 0; i < 9; i++) {
            c[i] = col(head, heads[i]);
        }
        for (int i = 0; i < 8; i++) {
            if (c[i] < 0) {
                return "Judul kolom tidak cocok. Pakai template: mapel, kelas, soal, a, b, c, d, kunci, "
                        + "pembahasan. Lalu simpan sebagai CSV.";
            }
        }

        int added = 0;
        int updated = 0;
        int skipped = 0;
        StringBuilder where = new StringBuilder();
        for (int r = 1; r < rows.size(); r++) {
            ArrayList<String> row = rows.get(r);
            if (isBlank(row)) {
                continue;
            }
            B b = toBank(row, c);
            if (b == null) {
                if (skipped < 5) {
                    if (where.length() > 0) {
                        where.append(", ");
                    }
                    where.append(r + 1);
                }
                skipped++;
                continue;
            }
            int at = findBank(b);
            if (at >= 0) {
                bank.set(at, b);
                updated++;
            } else {
                bank.add(b);
                added++;
            }
        }
        if (added + updated > 0 && !saveBank()) {
            return "Gagal menyimpan soal di HP ini.";
        }
        if (added + updated == 0 && skipped == 0) {
            return "Tidak ada soal di berkas.";
        }
        StringBuilder msg = new StringBuilder();
        if (added + updated > 0) {
            msg.append("Berhasil: ").append(added).append(" soal baru");
            if (updated > 0) {
                msg.append(", ").append(updated).append(" diperbarui");
            }
            msg.append(".");
        } else {
            msg.append("Tidak ada soal yang masuk.");
        }
        if (skipped > 0) {
            msg.append(" ").append(skipped).append(" baris dilewati (baris ").append(where);
            msg.append("). Cek mapel, kelas 1-6, soal, 4 pilihan, dan kunci A-D.");
        }
        return msg.toString();
    }

    private void buildBank() {
        if (advLevel >= 0) {
            buildBuiltin();
            return;
        }
        if (alamOn()) {
            buildAlam();
            return;
        }
        if (sambungOn()) {
            buildSambung();
            return;
        }
        if (tebakOn()) {
            buildTebak();
            return;
        }
        if ((subj == 1 || subj == 2) && countBank(subj, kelas) == 0) {
            // Belum ada soal guru: pakai soal bawaan Bahasa Indonesia atau Inggris untuk kelas ini
            advPart = -1;
            buildBuiltin();
            return;
        }
        ArrayList<B> pool = new ArrayList<>();
        for (B b : bank) {
            if (b.subj == subj && b.kelas == kelas + 1) {
                pool.add(b);
            }
        }
        Collections.shuffle(pool, rnd);
        total = Math.min(qCount(), pool.size());
        for (int i = 0; i < total; i++) {
            B b = pool.get(i);
            Q q = new Q();
            q.text = b.text;
            q.full = b.text;
            q.sopts = b.opt;
            q.correctIdx = b.key;
            q.expl = b.expl;
            qs[i] = q;
        }
    }

    private void showGuru() {
        handler.removeCallbacksAndMessages(null);
        setScreen(7);
        nameFields = null;

        LinearLayout c = column();
        c.addView(text("\uD83D\uDCDA Soal dari Guru", 26, ORANGE_DARK, true), lp(WRAP, 0));

        if (!importMsg.isEmpty()) {
            TextView m = text(importMsg, 14, Color.WHITE, true);
            m.setBackground(box(importMsg.startsWith("Berhasil") ? GREEN : RED, 16));
            m.setPadding(dp(14), dp(10), dp(14), dp(10));
            c.addView(m, lp(WRAP, 14));
        }

        LinearLayout cs = card();
        cs.addView(heading("Soal yang tersimpan"), lp(WRAP, 0));
        if (bank.isEmpty()) {
            cs.addView(text("Belum ada soal.", 14, DARK, false), lp(WRAP, 8));
        } else {
            for (int s = 0; s < 3; s++) {
                StringBuilder sb = new StringBuilder();
                for (int k = 0; k < 6; k++) {
                    int n = countBank(s, k);
                    if (n > 0) {
                        if (sb.length() > 0) {
                            sb.append(" \u2022 ");
                        }
                        sb.append("Kelas ").append(k + 1).append(": ").append(n);
                    }
                }
                TextView t = text(SUBJ_NAMES[s] + "\n" + (sb.length() > 0 ? sb.toString() : "-"),
                        13, DARK, false);
                t.setGravity(Gravity.START);
                cs.addView(t, lp(WRAP, 8));
            }
        }
        c.addView(cs, lp(WRAP, 14));

        Button imp = button("Impor soal dari file", BLUE, 20);
        imp.setElevation(dp(4));
        c.addView(imp, lp(dp(60), 16));
        imp.setOnClickListener(v -> pickFile());

        LinearLayout ch = card();
        ch.addView(heading("Cara membuat file soal"), lp(WRAP, 0));
        TextView how = text("1. Buka file template_soal.xlsx di Excel atau Google Sheets.\n"
                + "2. Isi satu baris untuk satu soal.\n"
                + "3. Simpan sebagai CSV (UTF-8).\n"
                + "4. Kirim file ke HP ini, lalu tekan Impor.\n\n"
                + "Kolom: mapel, kelas, soal, a, b, c, d, kunci (A/B/C/D), pembahasan (boleh kosong).\n"
                + "Mapel: Matematika, Bahasa Indonesia, atau Bahasa Inggris. Kelas: 1 sampai 6.\n"
                + "Pilihan jawaban sebaiknya singkat. Soal yang sama akan diperbarui, bukan dobel.",
                13, DARK, false);
        how.setGravity(Gravity.START);
        ch.addView(how, lp(WRAP, 8));
        c.addView(ch, lp(WRAP, 14));

        LinearLayout clap = card();
        clap.addView(heading("Laporan guru"), lp(WRAP, 0));
        TextView lapInfo = text(stTotal > 0
                ? "Sudah ada " + stTotal + " jawaban murid tercatat. Lihat topik yang perlu diulang dan soal yang paling sering salah."
                : "Belum ada data. Laporan terisi otomatis setelah murid bermain.",
                13, DARK, false);
        lapInfo.setGravity(Gravity.START);
        clap.addView(lapInfo, lp(WRAP, 8));
        Button lap = button("Buka laporan", PURPLE, 18);
        clap.addView(lap, lp(dp(50), 10));
        lap.setOnClickListener(v -> showLaporan());
        c.addView(clap, lp(WRAP, 14));

        LinearLayout ccad = card();
        ccad.addView(heading("Cadangan data"), lp(WRAP, 0));
        TextView cadInfo = text("Simpan soal guru, rekor, bintang, lencana, pengaturan, dan laporan ke satu file. "
                + "Berguna kalau ganti HP atau data aplikasi terhapus. PIN guru tidak ikut disimpan.",
                13, DARK, false);
        cadInfo.setGravity(Gravity.START);
        ccad.addView(cadInfo, lp(WRAP, 8));
        Button cadSave = button("Simpan cadangan ke file", BLUE, 16);
        ccad.addView(cadSave, lp(dp(48), 10));
        cadSave.setOnClickListener(v -> startBackupSave());
        Button cadOpen = button("Pulihkan dari file cadangan", ORANGE, 16);
        ccad.addView(cadOpen, lp(dp(48), 8));
        cadOpen.setOnClickListener(v -> startBackupOpen());
        c.addView(ccad, lp(WRAP, 14));

        LinearLayout cpin = card();
        cpin.addView(heading("PIN guru"), lp(WRAP, 0));
        TextView pinInfo = text(hasPin()
                ? "PIN aktif. Murid tidak bisa membuka halaman ini tanpa PIN."
                : "Belum ada PIN. Siapa pun yang menekan Soal Guru bisa membuka halaman ini.",
                13, DARK, false);
        pinInfo.setGravity(Gravity.START);
        cpin.addView(pinInfo, lp(WRAP, 8));
        Button gpin = button(hasPin() ? "Ganti PIN" : "Buat PIN", BLUE, 16);
        cpin.addView(gpin, lp(dp(46), 10));
        gpin.setOnClickListener(v -> dialogBuatPin(false));
        if (hasPin()) {
            Button hpin = button("Hapus PIN", RED, 14);
            cpin.addView(hpin, lp(dp(42), 8));
            hpin.setOnClickListener(v -> new AlertDialog.Builder(this)
                    .setTitle("Hapus PIN guru?")
                    .setMessage("Setelah itu murid bisa membuka Soal Guru tanpa PIN.")
                    .setPositiveButton("Hapus", (dl, w) -> {
                        clearPin();
                        importMsg = "Berhasil: PIN guru dihapus.";
                        showGuru();
                    })
                    .setNegativeButton("Batal", null)
                    .show());
        }
        c.addView(cpin, lp(WRAP, 14));

        if (!bank.isEmpty()) {
            Button del = button("Hapus semua soal guru", RED, 14);
            c.addView(del, lp(dp(46), 16));
            del.setOnClickListener(v -> new AlertDialog.Builder(this)
                    .setTitle("Hapus semua soal guru?")
                    .setMessage("Semua soal yang sudah diimpor akan dihapus dari HP ini.")
                    .setPositiveButton("Hapus", (dl, w) -> {
                        bank.clear();
                        saveBank();
                        importMsg = "Berhasil: semua soal guru dihapus.";
                        showGuru();
                    })
                    .setNegativeButton("Batal", null)
                    .show());
        }

        Button back = button("Kembali", GRAY, 14);
        back.setTextColor(DARK);
        c.addView(back, lp(dp(46), 14));
        back.setOnClickListener(v -> showMenu());

        wrap(c);
    }

    // =====================================================================
    // Laporan guru dan cadangan data (versi 6.0.1)
    // =====================================================================

    private String cutText(String s, int n) {
        if (s == null) {
            return "";
        }
        String t = s.replace('\n', ' ').trim();
        return t.length() <= n ? t : t.substring(0, n - 1) + "\u2026";
    }

    private String fmtDate(long ms) {
        return new SimpleDateFormat("d MMM yyyy", new Locale("id", "ID")).format(new Date(ms));
    }

    private void loadStats() {
        stTopic.clear();
        stItem.clear();
        stText.clear();
        stSince = 0L;
        stTotal = 0;
        stDirty = false;
        String raw = prefs.getString(STATS_KEY, "");
        if (raw.isEmpty()) {
            return;
        }
        try {
            JSONObject o = new JSONObject(raw);
            stSince = o.optLong("since", 0L);
            stTotal = o.optInt("n", 0);
            JSONObject t = o.optJSONObject("t");
            if (t != null) {
                Iterator<String> it = t.keys();
                while (it.hasNext()) {
                    String k = it.next();
                    JSONArray a = t.getJSONArray(k);
                    stTopic.put(k, new int[]{a.getInt(0), a.getInt(1)});
                }
            }
            JSONObject m = o.optJSONObject("i");
            if (m != null) {
                Iterator<String> it2 = m.keys();
                while (it2.hasNext()) {
                    String k = it2.next();
                    JSONArray a = m.getJSONArray(k);
                    stItem.put(k, new int[]{a.getInt(0), a.getInt(1)});
                    stText.put(k, a.optString(2, ""));
                }
            }
        } catch (Exception e) {
            // Data laporan rusak: mulai dari kosong
            stTopic.clear();
            stItem.clear();
            stText.clear();
            stSince = 0L;
            stTotal = 0;
        }
    }

    private void pruneStats() {
        while (stItem.size() > STATS_MAX_ITEMS) {
            String worst = null;
            int worstVal = Integer.MAX_VALUE;
            for (Map.Entry<String, int[]> e : stItem.entrySet()) {
                int[] v = e.getValue();
                int val = v[1] * 100 + v[0] + v[1];
                if (val < worstVal) {
                    worstVal = val;
                    worst = e.getKey();
                }
            }
            if (worst == null) {
                return;
            }
            stItem.remove(worst);
            stText.remove(worst);
        }
    }

    private void saveStats() {
        if (!stDirty) {
            return;
        }
        try {
            pruneStats();
            JSONObject o = new JSONObject();
            o.put("v", 1);
            o.put("since", stSince);
            o.put("n", stTotal);
            JSONObject t = new JSONObject();
            for (Map.Entry<String, int[]> e : stTopic.entrySet()) {
                t.put(e.getKey(), new JSONArray().put(e.getValue()[0]).put(e.getValue()[1]));
            }
            o.put("t", t);
            JSONObject m = new JSONObject();
            for (Map.Entry<String, int[]> e : stItem.entrySet()) {
                String tx = stText.get(e.getKey());
                m.put(e.getKey(), new JSONArray().put(e.getValue()[0]).put(e.getValue()[1])
                        .put(tx == null ? "" : tx));
            }
            o.put("i", m);
            prefs.edit().putString(STATS_KEY, o.toString()).apply();
            stDirty = false;
        } catch (Exception e) {
            // Gagal menyimpan laporan: abaikan, permainan tidak terganggu
        }
    }

    private void clearStats() {
        stTopic.clear();
        stItem.clear();
        stText.clear();
        stSince = 0L;
        stTotal = 0;
        stDirty = false;
        prefs.edit().remove(STATS_KEY).apply();
    }

    private void bumpStat(HashMap<String, int[]> m, String k, boolean ok) {
        int[] v = m.get(k);
        if (v == null) {
            v = new int[2];
            m.put(k, v);
        }
        v[ok ? 0 : 1]++;
    }

    private void touchStats() {
        if (stSince == 0L) {
            stSince = System.currentTimeMillis();
        }
        stTotal++;
        stDirty = true;
    }

    // Nama operasi bila kunci soal hanya punya satu tanda operasi (+ - x bagi), selain itu kosong
    private String opName(String key) {
        int cnt = 0;
        String name = "";
        for (int i = 1; i < key.length(); i++) {
            char ch = key.charAt(i);
            if (ch == '+') {
                cnt++;
                name = OP_NAME[0];
            } else if (ch == '-') {
                cnt++;
                name = OP_NAME[1];
            } else if (ch == '\u00D7') {
                cnt++;
                name = OP_NAME[2];
            } else if (ch == '\u00F7') {
                cnt++;
                name = OP_NAME[3];
            }
        }
        return cnt == 1 ? name : "";
    }

    private String topicOf(Q q) {
        if (mode == 8) {
            return "Tabel " + tableN;
        }
        if (mode == 7) {
            return alamOn() ? "Kuis Alam" : tebakOn() ? "Tebak Gambar" : sambungOn() ? (urut ? "Urut Ayat" : "Sambung Ayat") : "Kuis soal";
        }
        if (mode == 5) {
            return "3 Bilangan";
        }
        if (mode == 6) {
            return "Isi Titik";
        }
        String op = opName(q.key);
        if (!op.isEmpty()) {
            return op;
        }
        return OP_NAME[Math.max(0, Math.min(4, mode))];
    }

    // Kunci soal untuk daftar "paling sering salah". Soal hitung hanya dicatat bila berupa fakta dasar
    // (tambah-kurang sampai 20, kali-bagi tabel sampai 12), supaya daftar tidak penuh angka acak.
    private String itemKeyOf(Q q) {
        if (mode == 8) {
            // Samakan dengan kunci mode hitung: "7x8" jadi 7\u00D78, "56 bagi 7" jadi 56\u00F77
            try {
                String tk = q.key;
                int x = tk.indexOf('x');
                int pos = x > 0 ? x : tk.indexOf('d');
                int n = Integer.parseInt(tk.substring(1, pos));
                int m2 = Integer.parseInt(tk.substring(pos + 1));
                if (x > 0) {
                    return Math.min(n, m2) + "\u00D7" + Math.max(n, m2);
                }
                return (n * m2) + "\u00F7" + n;
            } catch (Exception e) {
                return q.key;
            }
        }
        if (mode == 7) {
            if (alamOn() && subj == 0) {
                return "";
            }
            return "q:" + cutText(q.text, 100);
        }
        if (mode == 5 || q.key == null || q.key.isEmpty()) {
            return "";
        }
        String k = q.key;
        int pos = -1;
        char op = 0;
        for (int i = 1; i < k.length(); i++) {
            char ch = k.charAt(i);
            if (ch == '+' || ch == '-' || ch == '\u00D7' || ch == '\u00F7') {
                if (pos >= 0) {
                    return "";
                }
                pos = i;
                op = ch;
            }
        }
        if (pos < 0) {
            return "";
        }
        int a;
        int b;
        try {
            a = Integer.parseInt(k.substring(0, pos));
            b = Integer.parseInt(k.substring(pos + 1));
        } catch (Exception e) {
            return "";
        }
        boolean basic;
        if (op == '+' || op == '-') {
            basic = a <= 20 && b <= 20;
        } else if (op == '\u00D7') {
            basic = a <= 12 && b <= 12;
        } else {
            basic = b >= 1 && b <= 12 && a % b == 0 && a / b <= 12;
        }
        return basic ? k : "";
    }

    private String itemTextOf(Q q) {
        if (mode == 7) {
            return cutText(q.text, 90) + " \u2192 " + cutText(optText(q, q.correctIdx), 40);
        }
        return cutText(q.full != null ? q.full : q.text, 80);
    }

    private void recordAnswer(Q q, boolean ok) {
        if (ok && sambungOn()) {
            sambungCatat(q);
        }
        try {
            int sj = (mode == 7) ? subj : 0;
            bumpStat(stTopic, sj + "|" + kelas + "|" + topicOf(q), ok);
            String ik = itemKeyOf(q);
            if (!ik.isEmpty()) {
                String gk = sj + "|" + kelas + "|" + ik;
                bumpStat(stItem, gk, ok);
                if (!stText.containsKey(gk)) {
                    stText.put(gk, itemTextOf(q));
                }
            }
            touchStats();
        } catch (Exception e) {
            // Pencatatan laporan tidak boleh mengganggu permainan
        }
    }

    private void recordMatch(int pi, boolean ok) {
        try {
            String left = matchPairs[pi][0];
            String right = matchPairs[pi][1];
            bumpStat(stTopic, matchSubj + "|" + kelas + "|Cocokkan Pasangan", ok);
            String gk = matchSubj + "|" + kelas + "|p:" + cutText(left, 60);
            bumpStat(stItem, gk, ok);
            if (!stText.containsKey(gk)) {
                stText.put(gk, cutText(left, 50) + " \u2194 " + cutText(right, 50));
            }
            touchStats();
        } catch (Exception e) {
            // abaikan
        }
    }

    // Kunci laporan berbentuk "mapel|kelas|isi". Hasil: {mapel, kelas, awal isi} atau null bila rusak.
    private int[] headOf(String k) {
        int i1 = k.indexOf('|');
        if (i1 < 1) {
            return null;
        }
        int i2 = k.indexOf('|', i1 + 1);
        if (i2 < 0) {
            return null;
        }
        try {
            int sj = Integer.parseInt(k.substring(0, i1));
            int kl = Integer.parseInt(k.substring(i1 + 1, i2));
            if (sj < 0 || sj > 2 || kl < 0 || kl > 5) {
                return null;
            }
            return new int[]{sj, kl, i2 + 1};
        } catch (Exception e) {
            return null;
        }
    }

    private boolean passFilter(int[] h) {
        return h != null && (rpSubj < 0 || h[0] == rpSubj) && (rpKelas < 0 || h[1] == rpKelas);
    }

    private static class Rank {
        String name = "";
        String sub = "";
        int b;
        int s;
    }

    private ArrayList<Rank> rankTopics() {
        ArrayList<Rank> out = new ArrayList<>();
        for (Map.Entry<String, int[]> e : stTopic.entrySet()) {
            int[] h = headOf(e.getKey());
            int[] v = e.getValue();
            if (!passFilter(h) || v[0] + v[1] < 5) {
                continue;
            }
            Rank r = new Rank();
            r.name = SUBJ_NAMES[h[0]] + " \u2022 Kelas " + (h[1] + 1) + " \u2022 " + e.getKey().substring(h[2]);
            r.b = v[0];
            r.s = v[1];
            out.add(r);
        }
        Collections.sort(out, new Comparator<Rank>() {
            @Override
            public int compare(Rank x, Rank y) {
                double px = (double) x.b / (x.b + x.s);
                double py = (double) y.b / (y.b + y.s);
                if (px != py) {
                    return px < py ? -1 : 1;
                }
                return (y.b + y.s) - (x.b + x.s);
            }
        });
        return out;
    }

    private ArrayList<Rank> rankItems() {
        ArrayList<Rank> out = new ArrayList<>();
        for (Map.Entry<String, int[]> e : stItem.entrySet()) {
            int[] h = headOf(e.getKey());
            int[] v = e.getValue();
            if (!passFilter(h) || v[1] < 2) {
                continue;
            }
            Rank r = new Rank();
            String tx = stText.get(e.getKey());
            r.name = (tx == null || tx.isEmpty()) ? e.getKey().substring(h[2]) : tx;
            r.sub = SUBJ_NAMES[h[0]] + " \u2022 Kelas " + (h[1] + 1);
            r.b = v[0];
            r.s = v[1];
            out.add(r);
        }
        Collections.sort(out, new Comparator<Rank>() {
            @Override
            public int compare(Rank x, Rank y) {
                if (x.s != y.s) {
                    return y.s - x.s;
                }
                double px = (double) x.b / (x.b + x.s);
                double py = (double) y.b / (y.b + y.s);
                return px < py ? -1 : (px > py ? 1 : 0);
            }
        });
        return out;
    }

    private int pctColor(int pct) {
        return pct < 60 ? RED : (pct < 80 ? ORANGE : GREEN);
    }

    private LinearLayout barView(int pct, int color) {
        LinearLayout r = new LinearLayout(this);
        r.setOrientation(LinearLayout.HORIZONTAL);
        r.setBackground(box(GRAY_D, 5));
        View f = new View(this);
        f.setBackground(box(color, 5));
        r.addView(f, new LinearLayout.LayoutParams(0, MATCH, Math.max(1, pct)));
        r.addView(new View(this), new LinearLayout.LayoutParams(0, MATCH, Math.max(0, 100 - pct)));
        return r;
    }

    private void addRankRow(LinearLayout parent, String name, String sub, int pct) {
        TextView t = text(name, 14, DARK, true);
        t.setGravity(Gravity.START);
        parent.addView(t, lp(WRAP, 12));
        TextView u = text(sub, 12, DARK, false);
        u.setGravity(Gravity.START);
        parent.addView(u, lp(WRAP, 2));
        parent.addView(barView(pct, pctColor(pct)), lp(dp(8), 5));
    }

    private String laporanText() {
        StringBuilder sb = new StringBuilder();
        sb.append("Laporan ").append(APP_NAME);
        if (stSince > 0) {
            sb.append(" (sejak ").append(fmtDate(stSince)).append(")");
        }
        sb.append("\nJawaban tercatat: ").append(stTotal);
        if (rpSubj >= 0) {
            sb.append("\nMapel: ").append(SUBJ_NAMES[rpSubj]);
        }
        if (rpKelas >= 0) {
            sb.append("\nKelas: ").append(rpKelas + 1);
        }
        ArrayList<Rank> tp = rankTopics();
        sb.append("\n\nTopik yang perlu diulang:");
        if (tp.isEmpty()) {
            sb.append("\n(belum cukup data)");
        }
        for (int i = 0; i < tp.size() && i < 8; i++) {
            Rank r = tp.get(i);
            sb.append("\n").append(i + 1).append(". ").append(r.name).append(": ")
                    .append(100 * r.b / (r.b + r.s)).append("% benar (").append(r.b)
                    .append(" dari ").append(r.b + r.s).append(")");
        }
        ArrayList<Rank> its = rankItems();
        sb.append("\n\nSoal paling sering salah:");
        if (its.isEmpty()) {
            sb.append("\n(belum ada)");
        }
        for (int i = 0; i < its.size() && i < 10; i++) {
            Rank r = its.get(i);
            sb.append("\n").append(i + 1).append(". ").append(r.name).append(" \u2014 salah ")
                    .append(r.s).append(" dari ").append(r.b + r.s).append(" (").append(r.sub).append(")");
        }
        return sb.toString();
    }

    private void showLaporan() {
        handler.removeCallbacksAndMessages(null);
        setScreen(11);
        nameFields = null;

        LinearLayout c = column();
        c.addView(text("\uD83D\uDCCA Laporan Guru", 26, ORANGE_DARK, true), lp(WRAP, 0));
        String since = stSince > 0 ? "Data sejak " + fmtDate(stSince) + " \u2022 " : "";
        c.addView(text(since + stTotal + " jawaban tercatat", 13, DARK, false), lp(WRAP, 6));

        LinearLayout cf = card();
        cf.addView(heading("Tampilkan"), lp(WRAP, 0));
        cf.addView(chips(new String[]{"Semua", "Mat", "Indo", "Ing"}, rpSubj + 1,
                new int[]{DARK, ORANGE, TEAL, BLUE}, idx1 -> {
                    rpSubj = idx1 - 1;
                    showLaporan();
                }), lp(dp(44), 10));
        cf.addView(chips(new String[]{"Semua", "1", "2", "3", "4", "5", "6"}, rpKelas + 1,
                new int[]{DARK, GREEN, TEAL, BLUE, PURPLE, ORANGE, RED}, idx2 -> {
                    rpKelas = idx2 - 1;
                    showLaporan();
                }), lp(dp(44), 8));
        c.addView(cf, lp(WRAP, 14));

        int tb = 0;
        int ts = 0;
        for (Map.Entry<String, int[]> e : stTopic.entrySet()) {
            if (passFilter(headOf(e.getKey()))) {
                tb += e.getValue()[0];
                ts += e.getValue()[1];
            }
        }
        LinearLayout cr = card();
        cr.addView(heading("Ringkasan"), lp(WRAP, 0));
        if (tb + ts == 0) {
            TextView none = text("Belum ada data untuk pilihan ini. Laporan terisi otomatis setelah murid bermain.",
                    13, DARK, false);
            none.setGravity(Gravity.START);
            cr.addView(none, lp(WRAP, 8));
        } else {
            int pct = 100 * tb / (tb + ts);
            cr.addView(text(pct + "% jawaban benar", 22, pctColor(pct), true), lp(WRAP, 8));
            cr.addView(text(tb + " benar \u2022 " + ts + " salah dari " + (tb + ts) + " jawaban",
                    13, DARK, false), lp(WRAP, 2));
            cr.addView(barView(pct, pctColor(pct)), lp(dp(10), 8));
        }
        c.addView(cr, lp(WRAP, 14));

        LinearLayout ct = card();
        ct.addView(heading("Topik yang perlu diulang"), lp(WRAP, 0));
        ArrayList<Rank> tp = rankTopics();
        if (tp.isEmpty()) {
            TextView none = text("Belum cukup data. Topik muncul setelah minimal 5 jawaban.", 13, DARK, false);
            none.setGravity(Gravity.START);
            ct.addView(none, lp(WRAP, 8));
        }
        for (int i = 0; i < tp.size() && i < 8; i++) {
            Rank r = tp.get(i);
            int pct = 100 * r.b / (r.b + r.s);
            addRankRow(ct, r.name, pct + "% benar (" + r.b + " dari " + (r.b + r.s) + ")", pct);
        }
        c.addView(ct, lp(WRAP, 14));

        LinearLayout ci = card();
        ci.addView(heading("Soal yang paling sering salah"), lp(WRAP, 0));
        ArrayList<Rank> its = rankItems();
        if (its.isEmpty()) {
            TextView none = text("Belum ada soal yang salah 2 kali atau lebih.", 13, DARK, false);
            none.setGravity(Gravity.START);
            ci.addView(none, lp(WRAP, 8));
        }
        for (int i = 0; i < its.size() && i < 10; i++) {
            Rank r = its.get(i);
            int pct = 100 * r.b / (r.b + r.s);
            addRankRow(ci, r.name, "Salah " + r.s + " dari " + (r.b + r.s) + " \u2022 " + r.sub, pct);
        }
        c.addView(ci, lp(WRAP, 14));

        Button share = button("Bagikan rekap", BLUE, 16);
        c.addView(share, lp(dp(48), 16));
        share.setOnClickListener(v -> {
            try {
                Intent it = new Intent(Intent.ACTION_SEND);
                it.setType("text/plain");
                it.putExtra(Intent.EXTRA_SUBJECT, "Laporan " + APP_NAME);
                it.putExtra(Intent.EXTRA_TEXT, laporanText());
                startActivity(Intent.createChooser(it, "Bagikan rekap"));
            } catch (Exception e) {
                Toast.makeText(this, "Tidak ada aplikasi untuk membagikan.", Toast.LENGTH_LONG).show();
            }
        });

        Button del = button("Hapus data laporan", RED, 14);
        c.addView(del, lp(dp(46), 12));
        del.setOnClickListener(v -> new AlertDialog.Builder(this)
                .setTitle("Hapus data laporan?")
                .setMessage("Semua hitungan benar dan salah akan dihapus. Soal guru dan rekor tidak ikut terhapus.")
                .setPositiveButton("Hapus", (dl, w) -> {
                    clearStats();
                    showLaporan();
                })
                .setNegativeButton("Batal", null)
                .show());

        Button back = button("Kembali", GRAY, 14);
        back.setTextColor(DARK);
        c.addView(back, lp(dp(46), 12));
        back.setOnClickListener(v -> showGuru());

        wrap(c);
    }

    // ---------- Cadangan data ----------

    private boolean protectedKey(String k) {
        return k.startsWith("pin_") || k.equals("rec_hash");
    }

    private JSONArray bankJson() throws Exception {
        JSONArray arr = new JSONArray();
        for (B b : bank) {
            JSONObject o = new JSONObject();
            o.put("s", b.subj);
            o.put("k", b.kelas);
            o.put("q", b.text);
            JSONArray op = new JSONArray();
            for (int j = 0; j < 4; j++) {
                op.put(b.opt[j]);
            }
            o.put("o", op);
            o.put("a", b.key);
            o.put("e", b.expl);
            arr.put(o);
        }
        return arr;
    }

    // Isi cadangan: semua pengaturan dan kemajuan (kecuali PIN guru) dan soal guru
    private String buildBackup() {
        try {
            JSONObject o = new JSONObject();
            o.put("app", "BelajarBersama");
            o.put("format", 1);
            o.put("versi", APP_VERSION);
            o.put("dibuat", System.currentTimeMillis());
            JSONObject p = new JSONObject();
            for (Map.Entry<String, ?> e : prefs.getAll().entrySet()) {
                String k = e.getKey();
                Object v = e.getValue();
                if (protectedKey(k) || v == null) {
                    continue;
                }
                JSONObject item = new JSONObject();
                if (v instanceof Boolean) {
                    item.put("t", "b");
                    item.put("v", ((Boolean) v).booleanValue());
                } else if (v instanceof Integer) {
                    item.put("t", "i");
                    item.put("v", ((Integer) v).intValue());
                } else if (v instanceof Long) {
                    item.put("t", "l");
                    item.put("v", ((Long) v).longValue());
                } else if (v instanceof Float) {
                    item.put("t", "f");
                    item.put("v", ((Float) v).doubleValue());
                } else if (v instanceof String) {
                    item.put("t", "s");
                    item.put("v", (String) v);
                } else {
                    continue;
                }
                p.put(k, item);
            }
            o.put("prefs", p);
            o.put("bank", bankJson());
            return o.toString();
        } catch (Exception e) {
            return "";
        }
    }

    private void startBackupSave() {
        saveStats();
        pendingBackup = buildBackup();
        if (pendingBackup.isEmpty()) {
            importMsg = "Gagal menyiapkan cadangan.";
            showGuru();
            return;
        }
        Intent it = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        it.addCategory(Intent.CATEGORY_OPENABLE);
        it.setType("application/json");
        it.putExtra(Intent.EXTRA_TITLE, "cadangan_belajar_bersama_"
                + new SimpleDateFormat("yyyyMMdd", Locale.US).format(new Date()) + ".json");
        try {
            startActivityForResult(it, REQ_BACKUP_SAVE);
        } catch (Exception e) {
            pendingBackup = "";
            importMsg = "Tidak bisa membuka pemilih berkas di HP ini.";
            showGuru();
        }
    }

    private void writeBackup(Uri uri, String payload) {
        try {
            OutputStream out = getContentResolver().openOutputStream(uri, "wt");
            if (out == null) {
                throw new IOException("Tidak bisa menulis berkas");
            }
            try {
                out.write(payload.getBytes(StandardCharsets.UTF_8));
            } finally {
                out.close();
            }
            importMsg = "Berhasil: cadangan disimpan (" + bank.size() + " soal guru).";
        } catch (Exception e) {
            importMsg = "Gagal menyimpan cadangan. Coba pilih lokasi lain.";
        }
        showGuru();
    }

    private void startBackupOpen() {
        Intent it = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        it.addCategory(Intent.CATEGORY_OPENABLE);
        it.setType("*/*");
        try {
            startActivityForResult(it, REQ_BACKUP_OPEN);
        } catch (Exception e) {
            importMsg = "Tidak bisa membuka pemilih berkas di HP ini.";
            showGuru();
        }
    }

    private void readBackup(Uri uri) {
        String s;
        try {
            s = readAll(getContentResolver().openInputStream(uri));
        } catch (Exception e) {
            importMsg = "Gagal membaca berkas cadangan.";
            showGuru();
            return;
        }
        try {
            JSONObject o = new JSONObject(s);
            if (!"BelajarBersama".equals(o.optString("app")) || o.optInt("format", 0) != 1
                    || o.optJSONObject("prefs") == null) {
                throw new Exception("bukan cadangan");
            }
            pendingRestore = s;
            JSONArray bk = o.optJSONArray("bank");
            int nb = bk == null ? 0 : bk.length();
            long made = o.optLong("dibuat", 0L);
            new AlertDialog.Builder(this)
                    .setTitle("Pulihkan cadangan?")
                    .setMessage("Cadangan tanggal " + (made > 0 ? fmtDate(made) : "?") + " (versi "
                            + o.optString("versi", "?") + ") berisi " + nb + " soal guru.\n\n"
                            + "Rekor, bintang, lencana, pengaturan, laporan, dan soal guru di HP ini "
                            + "akan DIGANTI. PIN guru tidak berubah.")
                    .setPositiveButton("Pulihkan", (dl, w) -> applyRestore())
                    .setNegativeButton("Batal", (dl, w) -> pendingRestore = "")
                    .show();
        } catch (Exception e) {
            pendingRestore = "";
            importMsg = "Berkas ini bukan cadangan " + APP_NAME + ".";
            showGuru();
        }
    }

    private void applyRestore() {
        String s = pendingRestore;
        pendingRestore = "";
        try {
            JSONObject o = new JSONObject(s);
            if (!"BelajarBersama".equals(o.optString("app")) || o.optInt("format", 0) != 1) {
                throw new Exception("bukan cadangan");
            }
            JSONObject p = o.getJSONObject("prefs");
            ArrayList<B> nb = new ArrayList<>();
            JSONArray bk = o.optJSONArray("bank");
            if (bk != null) {
                for (int i = 0; i < bk.length(); i++) {
                    try {
                        JSONObject q = bk.getJSONObject(i);
                        B b = new B();
                        b.subj = q.getInt("s");
                        b.kelas = q.getInt("k");
                        b.text = q.getString("q");
                        JSONArray op = q.getJSONArray("o");
                        for (int j = 0; j < 4; j++) {
                            b.opt[j] = op.getString(j);
                        }
                        b.key = q.getInt("a");
                        b.expl = q.optString("e", "");
                        if (b.subj < 0 || b.subj > 2 || b.kelas < 1 || b.kelas > 6
                                || b.key < 0 || b.key > 3) {
                            continue;
                        }
                        nb.add(b);
                    } catch (Exception e) {
                        // soal rusak dilewati
                    }
                }
            }
            // Semua bacaan dan pemeriksaan dilakukan sebelum commit, jadi kalau gagal data lama utuh
            SharedPreferences.Editor ed = prefs.edit();
            for (String k : new ArrayList<String>(prefs.getAll().keySet())) {
                if (!protectedKey(k)) {
                    ed.remove(k);
                }
            }
            Iterator<String> it = p.keys();
            while (it.hasNext()) {
                String k = it.next();
                if (protectedKey(k)) {
                    continue;
                }
                JSONObject item = p.getJSONObject(k);
                String t = item.getString("t");
                if (t.equals("b")) {
                    ed.putBoolean(k, item.getBoolean("v"));
                } else if (t.equals("i")) {
                    ed.putInt(k, item.getInt("v"));
                } else if (t.equals("l")) {
                    ed.putLong(k, item.getLong("v"));
                } else if (t.equals("f")) {
                    ed.putFloat(k, (float) item.getDouble("v"));
                } else if (t.equals("s")) {
                    ed.putString(k, item.getString("v"));
                }
            }
            ed.commit();
            bank.clear();
            bank.addAll(nb);
            saveBank();
            loadStats();
            Toast.makeText(this, "Data berhasil dipulihkan.", Toast.LENGTH_LONG).show();
            recreate();
        } catch (Exception e) {
            importMsg = "Gagal memulihkan cadangan. Data di HP ini tidak diubah.";
            showGuru();
        }
    }

    private LinearLayout tile(String sym, String label, int color, final int m) {
        LinearLayout t = new LinearLayout(this);
        t.setOrientation(LinearLayout.VERTICAL);
        t.setGravity(Gravity.CENTER);
        t.setBackground(new TileDrawable(color));
        t.setElevation(dp(5));
        t.addView(text(sym, 36, Color.WHITE, true), new LinearLayout.LayoutParams(WRAP, WRAP));
        t.addView(text(label, 17, Color.WHITE, true), new LinearLayout.LayoutParams(WRAP, WRAP));
        t.setClickable(true);
        t.setOnClickListener(v -> startGame(m));
        return t;
    }

    // ---------- Pengaturan ----------

    // ---------- Kotak pengaturan (dibuka dari ikon di menu) ----------

    private LinearLayout dialogBody() {
        LinearLayout b = new LinearLayout(this);
        b.setOrientation(LinearLayout.VERTICAL);
        return b;
    }

    private void showDialogView(String title, LinearLayout body) {
        if (menuDialog != null && menuDialog.isShowing()) {
            menuDialog.setOnDismissListener(null);
            menuDialog.dismiss();
        }
        LinearLayout c = new LinearLayout(this);
        c.setOrientation(LinearLayout.VERTICAL);
        c.setPadding(dp(18), dp(16), dp(18), dp(16));
        c.addView(text(title, 22, ORANGE_DARK, true), lp(WRAP, 0));
        c.addView(body, lp(WRAP, 12));
        Button ok = button("Selesai", GREEN, 18);
        flat(ok);
        c.addView(ok, lp(dp(52), 16));
        ScrollView sv = new ScrollView(this);
        sv.addView(c);
        final AlertDialog d = new AlertDialog.Builder(this).setView(sv).create();
        menuDialog = d;
        d.setOnDismissListener(dl -> {
            saveNames();
            nameFields = null;
            showMenu();
        });
        ok.setOnClickListener(v -> d.dismiss());
        if (d.getWindow() != null) {
            d.getWindow().clearFlags(android.view.WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    | android.view.WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
            d.getWindow().setSoftInputMode(
                    android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        }
        d.show();
        if (d.getWindow() != null) {
            d.getWindow().setBackgroundDrawable(box(Color.WHITE, 24));
            int maxW = getResources().getDisplayMetrics().widthPixels - dp(24);
            d.getWindow().setLayout(Math.min(dp(380), maxW), ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

    private void dialogKelas() {
        LinearLayout b = dialogBody();
        for (int r = 0; r < 2; r++) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            for (int q = 0; q < 3; q++) {
                final int k = r * 3 + q;
                boolean on = (k == kelas);
                Button t = button(KELAS_LABEL[k], on ? KELAS_COLOR[k] : GRAY, 26);
                t.setTextColor(on ? Color.WHITE : DARK);
                flat(t);
                t.setOnClickListener(v -> {
                    prefs.edit().putInt("kelas", k).apply();
                    menuDialog.dismiss();
                });
                row.addView(t, weight(q == 0 ? 0 : 4, q == 2 ? 0 : 4));
            }
            b.addView(row, lp(dp(72), r == 0 ? 0 : 8));
        }
        showDialogView("Kelas berapa?", b);
    }

    private void dialogPemain() {
        LinearLayout b = dialogBody();
        b.addView(chips(new String[]{"1", "2", "3", "4"}, np - 1, PLAYER_COLORS, k1 -> {
            np = k1 + 1;
            prefs.edit().putInt("np", np).apply();
            dialogPemain();
        }), lp(dp(50), 0));
        b.addView(text(np == 1 ? "Main sendiri" : np + " pemain, main bergantian di HP ini",
                13, DARK, false), lp(WRAP, 8));
        if (np > 1) {
            nameFields = new EditText[np];
            for (int i = 0; i < np; i++) {
                EditText e = new EditText(this);
                e.setText(names[i]);
                e.setHint("Nama pemain " + (i + 1));
                e.setSingleLine(true);
                e.setFilters(new InputFilter[]{new InputFilter.LengthFilter(10)});
                e.setTextSize(16 * ui);
                e.setTextColor(DARK);
                e.setPadding(dp(14), 0, dp(14), 0);
                e.setBackground(outline(PLAYER_COLORS[i], 12));
                b.addView(e, lp(dp(46), 8));
                nameFields[i] = e;
            }
        } else {
            nameFields = null;
        }
        showDialogView("Siapa yang main?", b);
    }

    private void dialogWaktu() {
        LinearLayout b = dialogBody();
        b.addView(chips(new String[]{"Tanpa waktu", "Berwaktu"}, timed ? 1 : 0,
                new int[]{PURPLE, PURPLE}, k3 -> {
                    timed = (k3 == 1);
                    prefs.edit().putBoolean("timed", timed).apply();
                    dialogWaktu();
                }), lp(dp(50), 0));
        if (timed) {
            LinearLayout srow = new LinearLayout(this);
            srow.setOrientation(LinearLayout.HORIZONTAL);
            b.addView(srow, lp(dp(52), 12));
            srow.addView(stepButton("\u22125", -5), fixed(52));
            srow.addView(stepButton("\u22121", -1), fixed(52));
            srow.addView(text(secsNow() + " detik", 18, PURPLE, true),
                    new LinearLayout.LayoutParams(0, MATCH, 1f));
            srow.addView(stepButton("+1", 1), fixed(52));
            srow.addView(stepButton("+5", 5), fixed(52));
            b.addView(text("Waktu untuk tiap soal", 12, DARK, false), lp(WRAP, 6));
        } else {
            b.addView(text("Menjawab tanpa batas waktu", 13, DARK, false), lp(WRAP, 8));
        }
        showDialogView("Waktu menjawab", b);
    }

    // Waktu per soal: pilihan guru kalau pernah diubah, kalau belum memakai waktu bawaan kelas
    private int secsNow() {
        return prefs.contains("secs") ? secs : KELAS_SECS[kelas];
    }

    private Button stepButton(String label, final int delta) {
        Button b = button(label, ORANGE, 14);
        flat(b);
        b.setOnClickListener(v -> {
            secs = Math.max(SECS_MIN, Math.min(SECS_MAX, secsNow() + delta));
            prefs.edit().putInt("secs", secs).apply();
            dialogWaktu();
        });
        return b;
    }

    // ---------- Panel Admin (untuk menguji toko) ----------
    // Dibuka dengan menekan lama spanduk judul di menu utama, lalu dikunci PIN admin
    // 8 sampai 12 angka. Hash PIN disimpan dengan kunci pin_adm_* (tidak ikut cadangan).
    // Salah 3 kali: terkunci 1 menit, salah 6 kali: 2 menit, dan seterusnya dua kali lipat
    // sampai maksimal 24 jam. Tidak ada kode pemulihan: kalau lupa, hapus data aplikasi.
    private static final int ADM_PIN_MIN = 8;

    private String admHash(String pin) {
        return sha(pinSalt() + ":adm:" + pin);
    }

    // Mengembalikan alasan PIN terlalu lemah, atau null kalau PIN cukup kuat
    private String admWeak(String pin) {
        int n = pin.length();
        if (n < ADM_PIN_MIN) {
            return "Minimal " + ADM_PIN_MIN + " angka.";
        }
        boolean same = true;
        boolean up = true;
        boolean down = true;
        for (int i = 1; i < n; i++) {
            int d = pin.charAt(i) - pin.charAt(i - 1);
            if (d != 0) {
                same = false;
            }
            if (d != 1) {
                up = false;
            }
            if (d != -1) {
                down = false;
            }
        }
        if (same || up || down) {
            return "PIN terlalu mudah ditebak (angka sama atau berurutan).";
        }
        for (int ch = 1; ch <= 4; ch++) {
            if (n % ch == 0) {
                StringBuilder sb = new StringBuilder();
                for (int k = 0; k < n / ch; k++) {
                    sb.append(pin, 0, ch);
                }
                if (sb.toString().equals(pin)) {
                    return "PIN terlalu mudah ditebak (pola berulang).";
                }
            }
        }
        return null;
    }

    private String waitText(long ms) {
        long s = (ms + 999) / 1000;
        if (s < 120) {
            return s + " detik";
        }
        if (s < 7200) {
            return (s / 60) + " menit";
        }
        return (s / 3600) + " jam";
    }

    private EditText adminField(String hint) {
        EditText e = pinField(hint);
        e.setFilters(new InputFilter[]{new InputFilter.LengthFilter(12)});
        e.setTextSize(20 * ui);
        return e;
    }

    private void openAdmin() {
        long left = prefs.getLong("pin_adm_lock", 0L) - System.currentTimeMillis();
        if (left > 0) {
            Toast.makeText(this, "Panel Admin terkunci. Coba lagi " + waitText(left) + " lagi.",
                    Toast.LENGTH_LONG).show();
            return;
        }
        if (prefs.getString("pin_adm_hash", "").isEmpty()) {
            dialogBuatAdmin(false);
        } else {
            dialogMasukAdmin();
        }
    }

    private void dialogBuatAdmin(final boolean ganti) {
        LinearLayout c = cardBody(ganti ? "Ganti PIN admin" : "Buat PIN admin");
        TextView info = text("Pakai " + ADM_PIN_MIN + " sampai 12 angka yang sulit ditebak, bukan tanggal lahir "
                + "atau nomor berurutan. Catat di tempat aman, karena tidak ada kode pemulihan.", 13, DARK, false);
        info.setGravity(Gravity.START);
        c.addView(info, lp(WRAP, 8));
        final EditText p1 = adminField("PIN baru");
        c.addView(p1, lp(dp(54), 12));
        final EditText p2 = adminField("Ulangi PIN");
        c.addView(p2, lp(dp(54), 8));
        final TextView err = text(" ", 13, RED, true);
        c.addView(err, lp(WRAP, 8));
        Button simpan = button("Simpan", GREEN, 18);
        flat(simpan);
        c.addView(simpan, lp(dp(52), 10));
        Button batal = button("Batal", GRAY, 14);
        batal.setTextColor(DARK);
        flat(batal);
        c.addView(batal, lp(dp(44), 8));
        final AlertDialog d = showCard(c, 340, true);
        simpan.setOnClickListener(v -> {
            String a = p1.getText().toString();
            String why = admWeak(a);
            if (why != null) {
                err.setText(why);
                return;
            }
            if (!a.equals(p2.getText().toString())) {
                err.setText("Kedua PIN tidak sama.");
                return;
            }
            prefs.edit().putString("pin_adm_hash", admHash(a)).putInt("pin_adm_fail", 0)
                    .putLong("pin_adm_lock", 0L).apply();
            d.dismiss();
            Toast.makeText(this, "PIN admin tersimpan.", Toast.LENGTH_SHORT).show();
            dialogAdmin();
        });
        batal.setOnClickListener(v -> {
            d.dismiss();
            if (ganti) {
                dialogAdmin();
            }
        });
    }

    private void dialogMasukAdmin() {
        LinearLayout c = cardBody("PIN admin");
        TextView info = text("Masukkan PIN admin untuk membuka Panel Admin.", 13, DARK, false);
        info.setGravity(Gravity.START);
        c.addView(info, lp(WRAP, 8));
        final EditText p = adminField("PIN admin");
        c.addView(p, lp(dp(54), 12));
        final TextView err = text(" ", 13, RED, true);
        c.addView(err, lp(WRAP, 8));
        Button masuk = button("Masuk", GREEN, 18);
        flat(masuk);
        c.addView(masuk, lp(dp(52), 10));
        Button batal = button("Batal", GRAY, 14);
        batal.setTextColor(DARK);
        flat(batal);
        c.addView(batal, lp(dp(44), 8));
        final AlertDialog d = showCard(c, 340, true);
        masuk.setOnClickListener(v -> {
            String a = p.getText().toString();
            if (admHash(a).equals(prefs.getString("pin_adm_hash", ""))) {
                prefs.edit().putInt("pin_adm_fail", 0).apply();
                d.dismiss();
                dialogAdmin();
                return;
            }
            int f = prefs.getInt("pin_adm_fail", 0) + 1;
            prefs.edit().putInt("pin_adm_fail", f).apply();
            p.setText("");
            if (f % 3 == 0) {
                long ms = Math.min(86400000L, 60000L << Math.min(f / 3 - 1, 10));
                prefs.edit().putLong("pin_adm_lock", System.currentTimeMillis() + ms).apply();
                d.dismiss();
                Toast.makeText(this, "PIN salah " + f + " kali. Terkunci " + waitText(ms) + ".",
                        Toast.LENGTH_LONG).show();
            } else {
                err.setText("PIN salah. " + (3 - f % 3) + " percobaan lagi sebelum terkunci.");
            }
        });
        batal.setOnClickListener(v -> d.dismiss());
    }

    private void dialogAdmin() {
        LinearLayout c = cardBody("\uD83D\uDEE0\uFE0F Panel Admin");
        TextView info = text("Untuk menguji toko. Tidak mengubah bintang dan lencana.", 12, DARK, false);
        c.addView(info, lp(WRAP, 4));
        c.addView(pill("\uD83D\uDCB0 " + coins() + " koin", ORANGE), lpw(8));

        int[] adds = {100, 500, 1000};
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        for (int i = 0; i < adds.length; i++) {
            final int n = adds[i];
            Button b = button("+" + n, GREEN, 15);
            flat(b);
            b.setOnClickListener(v -> {
                prefs.edit().putInt("coins", coins() + n).apply();
                dialogAdmin();
            });
            row.addView(b, weight(i == 0 ? 0 : 4, 0));
        }
        c.addView(row, lp(dp(48), 12));

        Button all = button("\uD83D\uDD13 Buka semua barang", BLUE, 15);
        flat(all);
        c.addView(all, lp(dp(48), 10));
        all.setOnClickListener(v -> {
            SharedPreferences.Editor ed = prefs.edit();
            for (int i = 0; i < ITEMS.length; i++) {
                ed.putBoolean("own" + i, true);
            }
            ed.apply();
            Toast.makeText(this, "Semua barang terbuka.", Toast.LENGTH_SHORT).show();
            dialogAdmin();
        });

        Button reset = button("\uD83E\uDDF9 Reset koin dan koleksi", RED, 15);
        flat(reset);
        c.addView(reset, lp(dp(48), 10));
        reset.setOnClickListener(v -> new AlertDialog.Builder(this)
                .setMessage("Hapus semua koin, barang, dan dandanan avatar di HP ini?")
                .setPositiveButton("Reset", (dd, w) -> {
                    SharedPreferences.Editor ed = prefs.edit();
                    ed.putInt("coins", 0);
                    for (int i = 0; i < ITEMS.length; i++) {
                        ed.remove("own" + i);
                    }
                    for (int pl = 0; pl < MAX_PLAYERS; pl++) {
                        for (int sl = 0; sl < 7; sl++) {
                            ed.remove("eq" + pl + "_" + sl);
                        }
                        ed.remove("peciOff" + pl);
                        ed.remove("tasOff" + pl);
                    }
                    ed.apply();
                    dialogAdmin();
                })
                .setNegativeButton("Batal", null)
                .show());

        Button ganti = button("\uD83D\uDD11 Ganti PIN admin", PURPLE, 14);
        flat(ganti);
        c.addView(ganti, lp(dp(44), 10));
        Button tutup = button("Tutup", GRAY, 14);
        tutup.setTextColor(DARK);
        flat(tutup);
        c.addView(tutup, lp(dp(44), 8));
        final AlertDialog d = showCard(c, 340, false);
        ganti.setOnClickListener(v -> {
            d.dismiss();
            dialogBuatAdmin(true);
        });
        tutup.setOnClickListener(v -> {
            d.dismiss();
            showMenu();
        });
    }

    // ---------- Koin dan Toko Avatar ----------
    // Koin didapat dari bintang tiap permainan. Barang dan koin disimpan di HP ini
    // (ikut tercadangkan). Nomor urut barang = id yang tersimpan, jadi barang baru
    // hanya boleh DITAMBAH di akhir daftar, jangan disisipkan atau diurutkan ulang.
    // Kolom: slot (0 topi, 1 wajah, 2 pegang, 3 teman, 4 latar, 5 pakaian, 6 rambut), ikon, nama, harga.
    // Untuk latar, "ikon" berisi daftar warna gradasi. Untuk pakaian: warna badan,warna lengan. Untuk rambut: warna.
    private static final Object[][] ITEMS = {
            {0, "\uD83D\uDC52", "Topi Jerami", 35},
            {0, "\uD83C\uDF80", "Pita Cantik", 40},
            {0, "\uD83C\uDF38", "Bunga Sakura", 50},
            {0, "\uD83C\uDF93", "Topi Wisuda", 60},
            {0, "\u26D1\uFE0F", "Helm Penolong", 80},
            {0, "\uD83C\uDFA9", "Topi Sulap", 120},
            {0, "\uD83C\uDF1F", "Bintang Emas", 250},
            {0, "\uD83D\uDC51", "Mahkota Raja", 400},
            {1, "\uD83D\uDC53", "Kacamata Pintar", 40},
            {1, "\u2764\uFE0F", "Pipi Hati", 70},
            {1, "\uD83D\uDD76\uFE0F", "Kacamata Keren", 90},
            {1, "\uD83C\uDFAD", "Topeng Teater", 180},
            {2, "\u26BD", "Bola Sepak", 50},
            {2, "\uD83C\uDFC0", "Bola Basket", 50},
            {2, "\uD83C\uDFA8", "Palet Lukis", 70},
            {2, "\uD83D\uDD2D", "Teropong", 120},
            {2, "\uD83C\uDFB8", "Gitar Rock", 150},
            {2, "\u2694\uFE0F", "Pedang Ksatria", 220},
            {2, "\uD83D\uDE80", "Roket Angkasa", 300},
            {3, "\uD83D\uDC23", "Anak Ayam", 80},
            {3, "\uD83D\uDC22", "Kura-kura", 100},
            {3, "\uD83D\uDC1D", "Lebah Madu", 120},
            {3, "\uD83E\uDD8B", "Kupu-kupu", 150},
            {3, "\uD83D\uDC7B", "Hantu Lucu", 250},
            {3, "\uD83E\uDD16", "Robot Teman", 350},
            {3, "\uD83D\uDC09", "Naga Mini", 700},
            {3, "\uD83E\uDD84", "Unicorn Ajaib", 900},
            {4, "#4FC3F7,#E1F5FE", "Langit Biru", 30},
            {4, "#66BB6A,#E8F5E9", "Hutan Hijau", 40},
            {4, "#F06292,#F8BBD0", "Permen Pink", 100},
            {4, "#FF7043,#FFCA28", "Senja Jingga", 100},
            {4, "#311B92,#7E57C2", "Galaksi", 300},
            {4, "#E53935,#FB8C00,#FDD835,#43A047,#1E88E5,#8E24AA", "Pelangi", 500},
            {4, "#FFD54F,#FF8F00", "Emas Sultan", 1000},
            {5, "#1E88E5,#1E88E5", "Hoodie Biru", 60},
            {5, "#E53935,#E53935", "Jaket Merah", 90},
            {5, "#43A047,#43A047", "Jaket Hijau", 90},
            {5, "#FAFAFA,#212121", "Hoodie Panda", 150},
            {5, "#C9A06B,#C9A06B", "Seragam Pramuka", 120},
            {6, "#212121", "Rambut Cepak Hitam", 30},
            {6, "#8B3A2F", "Rambut Cepak Kemerahan", 40},
            {6, "#5D4037", "Rambut Belah Samping", 50},
            {6, "#5D4037", "Rambut Ikal Ringan", 80},
            {6, "#A0643C", "Rambut Pendek Rapi", 60},
  {5, "#F8BBD0,#F8BBD0,#AD1457", "Tunik Pink", 80},
  {5, "#B39DDB,#B39DDB,#5E35B1", "Gamis Ungu", 100},
  {5, "#C9A06B,#C9A06B,#8D6E63", "Pramuka Putri", 120},
  {5, "#1565C0,#1565C0,#0D47A1", "Batik Biru", 110},
  {5, "#80CBC4,#80CBC4,#00695C", "Tunik Mint", 90},
  {5, "#FB8C00,#FB8C00,#1565C0", "Kaos Olahraga", 70},
  {5, "#ECEFF1,#ECEFF1,#37474F", "Baju Koko", 100},
  {6, "#F8BBD0", "Jilbab Pink", 40},
  {6, "#90CAF9", "Jilbab Biru Muda", 40},
  {6, "#A5D6A7", "Jilbab Hijau Muda", 50},
  {6, "#CE93D8", "Jilbab Ungu", 50},
  {6, "#BCAAA4", "Jilbab Cokelat", 60},
  // 56-59: rambut putra gaya baru (lembar acuan no. 5-8); nomor barang tetap, tambahan HARUS di akhir daftar
  {6, "#6D4C41", "Rambut Bergelombang", 90},
  {6, "#8D5A3C", "Rambut Messy", 100},
  {6, "#3E2723", "Rambut Quiff", 110},
  {6, "#5D4037", "Rambut Ikat Kecil", 120},
  // Stage 2: append-only IDs; keep old saved equipment mappings stable.
  {5, "#FFFFFF,#FFFFFF,#C62828", "Seragam SD Putra", 0},
  {5, "#FFFFFF,#FFFFFF,#C62828", "Seragam SD Putri", 0}
    };
    private static final int ITEM_GALAKSI = 31;
    // Gender tiap barang, urut sama dengan ITEMS: u = semua, b = putra, g = putri.
    // Untuk memindah barang ke gender lain, cukup ubah hurufnya di sini.
    private static final String ITEM_GENDER =
  "bggubbub" + "ugbu" + "bugubbu" + "uuuuuuuu" + "uuuuuuu" + "bbbbb" + "bbbbb" + "ggggg" + "bb" + "ggggg" + "bbbb" + "bg";

    private static int itGender(int i) {
        if (i < 0 || i >= ITEM_GENDER.length()) {
  return -1;
        }
        char ch = ITEM_GENDER.charAt(i);
        return ch == 'b' ? 0 : (ch == 'g' ? 1 : -1);
    }

    private static boolean itFits(int i, int g) {
        int ig = itGender(i);
        return ig < 0 || ig == g;
    }
    private static final String[] RAR_NAME = {"Biasa", "Keren", "Epik", "Legendaris"};
    private static final int[] RAR_COLOR = {SILVER, BLUE, PURPLE, GOLD};
    // Avatar Studio: Teman has its own category, separate from Suasana. Item IDs remain stable.
    private static final String[] SHOP_TAB_ICON = {
            "\uD83C\uDFA9", "\uD83D\uDD76\uFE0F", "\uD83C\uDFA8", "\uD83D\uDC55", "\uD83D\uDC87", "\uD83D\uDC23"};
    private static final String[] SHOP_TAB_NAME = {"Kepala", "Aksesori", "Suasana", "Pakaian", "Rambut", "Teman"};

    private int lastCoinGain = 0;
    private int shopPl = 0;
    private int shopTab = 0;

    private static int itSlot(int i) {
        return (Integer) ITEMS[i][0];
    }

    private static String itIcon(int i) {
        return (String) ITEMS[i][1];
    }

    private static String itName(int i) {
        return (String) ITEMS[i][2];
    }

    private static int itPrice(int i) {
        return (Integer) ITEMS[i][3];
    }

    private static int itRar(int i) {
        int p = itPrice(i);
        return p < 70 ? 0 : p < 200 ? 1 : p < 600 ? 2 : 3;
    }

    private boolean owns(int i) {
        return prefs.getBoolean("own" + i, false);
    }

    private int coins() {
        return prefs.getInt("coins", 0);
    }

    private int eq(int pl, int slot) {
        return prefs.getInt("eq" + pl + "_" + slot, -1);
    }

    /** Barang yang sah dipakai pemain pl di slot (dimiliki, slot dan gender cocok), atau -1. */
    private int eqValid(int pl, int slot) {
        int e = eq(pl, slot);
        return (e >= 0 && e < ITEMS.length && itSlot(e) == slot && owns(e) && itFits(e, genderOf[pl])) ? e : -1;
    }

    /** Peci putra: bawaan dipakai; pemain boleh melepasnya lewat tombol di Toko Avatar (gratis, tersimpan per pemain). */
    private boolean peciOff(int pl) {
        return prefs.getBoolean("peciOff" + pl, false);
    }

    /** Ransel putra: bawaan dipakai; pemain boleh melepasnya lewat tombol Tas di Toko Avatar (tersimpan per pemain). Putri juga (ransel putri adalah bagian terpisah di PutriMesh). */
    private boolean tasOff(int pl) {
        return prefs.getBoolean("tasOff" + pl, false);
    }

    /** Model OBJ baru bersifat opsional; avatar prosedural lama tetap tersimpan dan menjadi bawaan. */
    private boolean avatarExt() {
        return prefs.getBoolean("avatarExt", false);
    }

    /**
     * Gaya rambut 3D putra menurut nomor barang (nomor barang tetap, lihat catatan ITEMS):
     * 39 Cepak Hitam dan 40 Cepak Kemerahan = cepak, 41 Belah Samping, 42 Ikal Ringan, 56 Bergelombang, 57 Messy, 58 Quiff, 59 Ikat Kecil. 43 Pendek Rapi = bentuk bawaan.
     */
    private static int hairStyleOf(int item) {
        switch (item) {
            case 39: return PutraMesh.STYLE_CREW;
            case 40: return PutraMesh.STYLE_CREW;
            case 41: return PutraMesh.STYLE_SIDE;
            case 42: return PutraMesh.STYLE_CURLY;
            case 56: return PutraMesh.STYLE_WAVY;
            case 57: return PutraMesh.STYLE_MESSY;
            case 58: return PutraMesh.STYLE_QUIFF;
            case 59: return PutraMesh.STYLE_BUN;
            default: return PutraMesh.STYLE_DEFAULT;
        }
    }

    /** Pasang pakaian, rambut (putra) / warna jilbab (putri), latar, dan peci milik pemain pl ke tampilan 3D. */
    private void applyLook3D(PutriGLView v, int pl) {
        int ba = eqValid(pl, 5), hr = eqValid(pl, 6), bg = eqValid(pl, 4);
        v.setExternalModel(avatarExt());
        v.setHood(isHood(avatar[pl]) ? avatar[pl] : -1); // tudung hewan 3D (putra dan putri)
        v.setTas(!tasOff(pl));
        v.setLook(ba >= 0 ? itIcon(ba) : null, hr >= 0 ? itIcon(hr) : null, bg >= 0 ? itIcon(bg) : null,
                genderOf[pl] != 0 || !peciOff(pl), (genderOf[pl] == 0 && hr >= 0) ? hairStyleOf(hr) : PutraMesh.STYLE_DEFAULT);
    }

    private GradientDrawable bgDrawable(int i, float radiusPx) {
        String[] cs = itIcon(i).split(",");
        int[] cols = new int[cs.length];
        for (int k = 0; k < cs.length; k++) {
            cols[k] = Color.parseColor(cs[k]);
        }
        GradientDrawable g = new GradientDrawable(GradientDrawable.Orientation.TL_BR, cols);
        g.setCornerRadius(radiusPx);
        return g;
    }

    // Avatar balok (gaya blocky): tudung hewan + rambut + pakaian + topi + wajah + barang pegang + teman + latar
    // Semua digambar dengan kode (tanpa file gambar). Gambar dibuat di kotak 100 x 100 satuan.
    // mode 0 = seluruh badan, 1 = zoom kepala, 2 = zoom badan dan tangan kanan (untuk pratinjau barang).
    private static final int NO_HOOD = 12;
    // 20 hewan sesuai lembar referensi "Detail Lengkap Tudung Hewan": 0 singa, 1 harimau, 2 panda, 3 kapibara, 4 katak, 5 rubah,
    // 6 kelinci, 7 anjing, 8 kucing, 9 unicorn, 10 penguin, 11 ayam, 12 = tanpa tudung, 13 domba, 14 buaya, 15 gajah, 16 zebra,
    // 21 kuda, 22 sapi, 23 kura-kura, 24 beruang. Slot 17..20 tidak dipakai. Warna disalin di HoodMesh.MAIN/ACC: ubah keduanya bersama.
    private static final String[] HOOD_MAIN = {"#F2B24C", "#F28C28", "#FFFFFF", "#9C6B45", "#5DBB4A", "#E8631C",
            "#F6EEF3", "#A8744A", "#9AA5AD", "#F3EAF7", "#37474F", "#FBF7F2", "#000000",
            "#F4EEE4", "#5CB847", "#8C8A93", "#F5F5F5", "#FBF7F2", "#9C6B45", "#7A4B2E", "#FBF7F2", "#A65E32", "#FFFFFF", "#58B84C", "#7A4B2E"};
    private static final String[] HOOD_ACC = {"#B5651D", "#212121", "#212121", "#D8B58C", "#FFFFFF", "#FFFFFF",
            "#F8BBD0", "#5D3A1E", "#6E7A85", "#F06292", "#FFFFFF", "#E53935", "#000000",
            "#E6D3B8", "#E8B93A", "#E3B7B0", "#212121", "#E53935", "#D8B58C", "#E2C6A0", "#E53935", "#4A2A17", "#212121", "#2F7D3A", "#E2C6A0"};
    private static final String[] HOOD_NAMES = {"Singa", "Harimau", "Panda", "Kapibara", "Katak", "Rubah",
            "Kelinci", "Anjing", "Kucing", "Unicorn", "Penguin", "Ayam", "Tanpa tudung",
            "Domba", "Buaya", "Gajah", "Zebra", "Ayam", "Kapibara", "Beruang", "Ayam", "Kuda", "Sapi", "Kura-kura", "Beruang"};
    // Slot lama yang tidak dipakai lagi dialihkan ke hewan terdekat (dipakai saat memuat pilihan tersimpan). Idempoten.
    private static final int[] HOOD_ALIAS = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 11, 3, 24, 11, 21, 22, 23, 24};
    // Urutan kotak di toko mengikuti lembar referensi; "tanpa tudung" selalu di kotak terakhir.
    private static final int[] HOOD_ORDER = {0, 1, 2, 15, 4, 5, 6, 24, 21, 22, 13, 8, 7, 11, 10, 23, 3, 9, 16, 14, 12};
    private static boolean isHood(int a) {
        return a >= 0 && a < AVATARS.length && a != NO_HOOD && !(a >= 17 && a <= 20);
    }

    // ---------- Avatar 3D tahap 1: data mesh balok ----------
    private static final int MAXF = 1400;
    // Sisi balok: depan, belakang, kanan, kiri, atas (indeks sudut: bit0=x, bit1=y, bit2=z)
    private static final int[][] BOX_FACES = {{4, 5, 7, 6}, {1, 0, 2, 3}, {5, 1, 3, 7}, {0, 4, 6, 2}, {0, 1, 5, 4}};
    private static final float[][] BOX_NORM = {{0f, 0f, 1f}, {0f, 0f, -1f}, {1f, 0f, 0f}, {-1f, 0f, 0f}, {0f, -1f, 0f}};

    private class AvatarView extends View {
        private final int animal;
        private final int[] it = new int[7];
        private final int mode;
        private final boolean tile;
        private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final RectF rf = new RectF();
        private final Path path = new Path();
        private float u = 1f;
        private float ox = 0f;
        private float oy = 0f;
        // Sudut putar (derajat) mengelilingi sumbu Y. Mode 0 memakai renderer 3D balok (drawChar3D).
        private float avatarAngle = 0f;
        private float lastTouchX = 0f;
        private final long animEpoch = System.currentTimeMillis();
        // Animasi idle (L2): waktu, goyang badan, ayunan lengan, gerak kepala.
        private float animT = 0f;
        private float swayDeg = 0f;
        private float swayAmp = 1f;
        private float headBob = 0f;
        private float armSwing = 0f;
        private float yShift = 0f;
        private float zShift = 0f;
        private boolean touching = false;

        private boolean isBlinking() {
            if (mode != 0) return false;
            long phase = (System.currentTimeMillis() - animEpoch) % 4100L;
            return phase < 145L || (phase > 260L && phase < 330L);
        }

        AvatarView(Context ctx, int animal, int[] items, int mode, boolean tile) {
            super(ctx);
            this.animal = animal;
            for (int k = 0; k < 7; k++) {
                it[k] = items[k];
            }
            this.mode = mode;
            this.tile = tile;
            p.setTextAlign(Paint.Align.CENTER);
        }

        private int col(String hex) {
            return Color.parseColor(hex);
        }

        private float fx(float v) {
            return ox + v * u;
        }

        private float fy(float v) {
            return oy + v * u;
        }

        private void outline(Canvas c) {
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(0.9f * u);
            p.setColor(0x44000000);
        }

        private void rect(Canvas c, float l, float t, float r, float b, float rad, int fill) {
            rf.set(fx(l), fy(t), fx(r), fy(b));
            p.setStyle(Paint.Style.FILL);
            p.setColor(fill);
            c.drawRoundRect(rf, rad * u, rad * u, p);
            outline(c);
            c.drawRoundRect(rf, rad * u, rad * u, p);
            p.setStyle(Paint.Style.FILL);
        }

        private void flat(Canvas c, float l, float t, float r, float b, float rad, int fill) {
            rf.set(fx(l), fy(t), fx(r), fy(b));
            p.setStyle(Paint.Style.FILL);
            p.setColor(fill);
            c.drawRoundRect(rf, rad * u, rad * u, p);
        }

        private void dot(Canvas c, float cx, float cy, float r, int fill) {
            p.setStyle(Paint.Style.FILL);
            p.setColor(fill);
            c.drawCircle(fx(cx), fy(cy), r * u, p);
        }

        private void ball(Canvas c, float cx, float cy, float r, int fill) {
            dot(c, cx, cy, r, fill);
            outline(c);
            c.drawCircle(fx(cx), fy(cy), r * u, p);
            p.setStyle(Paint.Style.FILL);
        }

        private void tri(Canvas c, float x1, float y1, float x2, float y2, float x3, float y3, int fill) {
            path.reset();
            path.moveTo(fx(x1), fy(y1));
            path.lineTo(fx(x2), fy(y2));
            path.lineTo(fx(x3), fy(y3));
            path.close();
            p.setStyle(Paint.Style.FILL);
            p.setColor(fill);
            c.drawPath(path, p);
        }

        private void line(Canvas c, float x1, float y1, float x2, float y2, float w, int color) {
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(w * u);
            p.setStrokeCap(Paint.Cap.ROUND);
            p.setColor(color);
            c.drawLine(fx(x1), fy(y1), fx(x2), fy(y2), p);
            p.setStyle(Paint.Style.FILL);
        }

        private void emoji(Canvas c, String t, float cx, float cy, float size) {
            p.setStyle(Paint.Style.FILL);
            p.setColor(Color.BLACK);
            p.setTextSize(size * u);
            Paint.FontMetrics fm = p.getFontMetrics();
            c.drawText(t, fx(cx), fy(cy) - (fm.ascent + fm.descent) / 2f, p);
        }

        // ---------- Renderer 3D balok (tahap 1) ----------
        // Kepala, badan, tangan, kaki = balok sungguhan (8 titik sudut, putar sumbu Y,
        // sedikit miring ke bawah, proyeksi perspektif, buang sisi belakang, urut kedalaman).
        // Wajah, baju, rambut, tudung, topi, barang pegang masih "stiker" 2D yang menempel
        // di sisi depan balok. Tahap berikutnya mengubahnya menjadi balok 3D.
        private final float[] pxs = new float[8];
        private final float[] pys = new float[8];
        private final float[] pzs = new float[8];
        private final float[] fq = new float[MAXF * 8];
        private final float[] fsrc = new float[MAXF * 4];
        private final float[] fz = new float[MAXF];
        private final int[] fc = new int[MAXF];
        private final int[] fk = new int[MAXF];
        private final int[] fo = new int[MAXF];
        private final boolean[] fsm = new boolean[MAXF];
        private final float[] flit = new float[MAXF];
        private boolean curHijab = false;
        private int curSkin = 0;
        private final float[][] rpx = new float[4][24];
        private final float[][] rpy = new float[4][24];
        private final float[][] rpz = new float[4][24];
        private final float[][] rox = new float[4][24];
        private final float[][] roz = new float[4][24];
        private final float[] roy = new float[4];
        private final float[] srcPts = new float[8];
        private final android.graphics.Matrix decalM = new android.graphics.Matrix();
        private int fn = 0;
        int gender = 0;
        boolean tasOff = false;   // ransel dilepas (putra)
        private boolean srcOn = false;
        private final float[] ovr = new float[4];

        // Atur persegi sumber stiker untuk balok berikutnya (koordinat gambar 2D asli).
        private void srcRect(float l, float t, float r, float b) {
            ovr[0] = l;
            ovr[1] = t;
            ovr[2] = r;
            ovr[3] = b;
            srcOn = true;
        }
        private float c3t = 1f;
        private float s3t = 0f;
        private float c3p = 1f;
        private float s3p = 0f;
        private int curHid = -1;
        private int curHairC = 0;
        private int curHijabC = 0;
        private boolean curHood = false;
        private boolean curSkipFace = false; private float zBias = 0f; private boolean skipTop = false;
        // Penempelan: balok hiasan (rambut, tali tas) tidak boleh digambar sebelum permukaan induknya.
        private boolean parOn = false;
        private int parAnchor = -1;
        private final float[] par = new float[6];
        private final float[] ppz = new float[8];
        private void attach(float x0, float y0, float x1, float y1, float z0, float z1, int anchor) {
            par[0] = x0; par[1] = y0; par[2] = x1; par[3] = y1; par[4] = z0; par[5] = z1;
            parAnchor = anchor;
            parOn = true;
        }
        private void detach() {
            parOn = false;
            parAnchor = -1;
        }
        private float curHt = 13.5f;
        // Tahap B: skala kepala (chibi) terhadap titik leher, dan skala global agar tinggi total tetap muat.
        // Tahap C: kepala dikecilkan (1.14 -> 0.95) dan diangkat HEAD_LIFT agar leher terlihat; badan dan lengan lebih panjang.
        private static final float HEAD_SCALE = 0.95f;
        private static final float HEAD_LIFT = 3f;
        private static final float NECK_Y = 42f;
        private static final float GROUND_Y = 89f;
        private static final float TORSO_BOT = 66f;
        private static final float HAND_TOP = 60f;
        private static final float HAND_BOT = 67f;
        private float hs = 1f;
        private float gs = 1f;
        // Cincin untuk addLoft (bentuk jilbab meruncing): maksimal 6 cincin.
        private final float[][] lpx = new float[6][24];
        private final float[][] lpy = new float[6][24];
        private final float[][] lpz = new float[6][24];
        private final float[][] lox = new float[6][24];
        private final float[][] loz = new float[6][24];

        private int shade(int color, float k) {
            int r = Math.min(255, (int) (Color.red(color) * k));
            int g = Math.min(255, (int) (Color.green(color) * k));
            int b = Math.min(255, (int) (Color.blue(color) * k));
            return Color.rgb(r, g, b);
        }

        // Campur warna ke putih sebesar t (kilap spekular).
        private int lighten(int color, float t) {
            if (t <= 0.004f) {
                return color;
            }
            int r = (int) (Color.red(color) + (255 - Color.red(color)) * t);
            int g = (int) (Color.green(color) + (255 - Color.green(color)) * t);
            int b = (int) (Color.blue(color) + (255 - Color.blue(color)) * t);
            return Color.rgb(Math.min(255, r), Math.min(255, g), Math.min(255, b));
        }

        // kind: 0 polos, 1 kepala, 2 badan, 3 lengan kanan (barang pegang)
        private void addBox(float x0, float y0, float x1, float y1, float z0, float z1,
                            int colSide, int colTop, int colBack, int kind) {
            for (int i = 0; i < 8; i++) {
                float x = ((i & 1) != 0 ? x1 : x0) - 50f;
                float y = ((i & 2) != 0 ? y1 : y0) - 50f + yShift;
                float z = ((i & 4) != 0 ? z1 : z0) + zShift;
                if (hs != 1f) {
                    x *= hs;
                    y = (NECK_Y - 50f) + (y - (NECK_Y - 50f)) * hs;
                    z *= hs;
                }
                float xr = x * c3t + z * s3t;
                float zr = -x * s3t + z * c3t;
                float y2 = y * c3p - zr * s3p;
                float z2 = y * s3p + zr * c3p;
                float sc = 260f / (260f - z2);
                pxs[i] = fx(50f + xr * sc * gs);
                pys[i] = fy(GROUND_Y + (50f + y2 * sc - GROUND_Y) * gs);
                pzs[i] = z2;
            }
            if (parOn) {
                for (int i = 0; i < 8; i++) {
                    float px = ((i & 1) != 0 ? par[2] : par[0]) - 50f;
                    float py = ((i & 2) != 0 ? par[3] : par[1]) - 50f + yShift;
                    float pz = ((i & 4) != 0 ? par[5] : par[4]) + zShift;
                    if (hs != 1f) {
                        px *= hs;
                        py = (NECK_Y - 50f) + (py - (NECK_Y - 50f)) * hs;
                        pz *= hs;
                    }
                    float pzr = -px * s3t + pz * c3t;
                    ppz[i] = py * s3p + pzr * c3p;
                }
            }
            for (int f = 0; f < 5 && fn < MAXF; f++) {
                if (f == 4 && skipTop) {
                    continue;
                }
                int[] q = BOX_FACES[f];
                float area = 0f;
                for (int k = 0; k < 4; k++) {
                    int a = q[k];
                    int b = q[(k + 1) & 3];
                    area += pxs[a] * pys[b] - pxs[b] * pys[a];
                }
                if (area <= 0f) {
                    continue;
                }
                float nx = BOX_NORM[f][0];
                float ny = BOX_NORM[f][1];
                float nz = BOX_NORM[f][2];
                float nxr = nx * c3t + nz * s3t;
                float nzr = -nx * s3t + nz * c3t;
                float ny2 = ny * c3p - nzr * s3p;
                float nz2 = ny * s3p + nzr * c3p;
                float lit = Math.max(0f, nxr * -0.45f + ny2 * -0.7f + nz2 * 0.62f);
                int base = f == 4 ? colTop : (f == 1 ? colBack : colSide);
                int i = fn++;
                fc[i] = shade(base, 0.64f + 0.52f * lit);
                fk[i] = f == 0 ? kind : 0;
                fsm[i] = false;
                fz[i] = (pzs[q[0]] + pzs[q[1]] + pzs[q[2]] + pzs[q[3]]) / 4f + zBias;
                if (parOn) {
                    boolean outer = f == 0 ? z1 >= par[5] - 0.001f
                            : f == 1 ? z0 <= par[4] + 0.001f
                            : f == 2 ? x1 >= par[2] - 0.001f
                            : f == 3 ? x0 <= par[0] + 0.001f
                            : y0 <= par[1] + 0.001f;
                    int ref = outer ? f : parAnchor;
                    if (ref >= 0) {
                        int[] rq = BOX_FACES[ref];
                        float pd = (ppz[rq[0]] + ppz[rq[1]] + ppz[rq[2]] + ppz[rq[3]]) / 4f;
                        float own = fz[i] - zBias;
                        if (own < pd + 0.05f) {
                            fz[i] = pd + 0.05f + 0.0008f * own + zBias;
                        }
                    }
                }
                for (int k = 0; k < 4; k++) {
                    fq[i * 8 + k * 2] = pxs[q[k]];
                    fq[i * 8 + k * 2 + 1] = pys[q[k]];
                }
                fsrc[i * 4] = srcOn ? ovr[0] : x0;
                fsrc[i * 4 + 1] = srcOn ? ovr[1] : y0;
                fsrc[i * 4 + 2] = srcOn ? ovr[2] : x1;
                fsrc[i * 4 + 3] = srcOn ? ovr[3] : y1;
            }
            srcOn = false;
        }

        // Balok bersudut bulat (kepala L3): penampang kotak membulat (sudut 3 segmen) dengan bevel atas dan bawah.
        // Stiker wajah (kind 1) dipetakan per sisi depan sehingga ikut melengkung mengikuti kepala.
        private void addRBox(float x0, float y0, float x1, float y1, float z0, float z1,
                             float r, float tT, float hT, float tB, float hB,
                             int colSide, int colTop, int colBack, int kind) {
            final int ns = 4;
            final int n = 4 * (ns + 1);
            roy[0] = y0;
            roy[1] = y0 + hT;
            roy[2] = y1 - hB;
            roy[3] = y1;
            for (int k = 0; k < 4; k++) {
                float inset = k == 0 ? tT : (k == 3 ? tB : 0f);
                float rr = Math.max(r - inset, 0.3f);
                for (int cn = 0; cn < 4; cn++) {
                    float ccx = (cn == 0 || cn == 1) ? x1 - r : x0 + r;
                    float ccz = (cn == 0 || cn == 3) ? z1 - r : z0 + r;
                    float a0 = cn == 0 ? 90f : (cn == 1 ? 0f : (cn == 2 ? -90f : 180f));
                    float a1 = a0 - 90f;
                    for (int sg = 0; sg <= ns; sg++) {
                        double ang = Math.toRadians(a0 + (a1 - a0) * sg / ns);
                        int idx = cn * (ns + 1) + sg;
                        float ox2 = ccx + rr * (float) Math.cos(ang);
                        float oz2 = ccz + rr * (float) Math.sin(ang);
                        rox[k][idx] = ox2;
                        roz[k][idx] = oz2;
                        float x = ox2 - 50f;
                        float y = roy[k] - 50f + yShift;
                        float z = oz2 + zShift;
                        if (hs != 1f) {
                            x *= hs;
                            y = (NECK_Y - 50f) + (y - (NECK_Y - 50f)) * hs;
                            z *= hs;
                        }
                        float xr = x * c3t + z * s3t;
                        float zr = -x * s3t + z * c3t;
                        float y2 = y * c3p - zr * s3p;
                        float z2 = y * s3p + zr * c3p;
                        float sc = 260f / (260f - z2);
                        rpx[k][idx] = fx(50f + xr * sc * gs);
                        rpy[k][idx] = fy(GROUND_Y + (50f + y2 * sc - GROUND_Y) * gs);
                        rpz[k][idx] = z2;
                    }
                }
            }
            for (int b = 0; b < 3; b++) {
                for (int a = 0; a < n && fn < MAXF; a++) {
                    int j = (a + 1) % n;
                    float area = 0f;
                    area += rpx[b][a] * rpy[b][j] - rpx[b][j] * rpy[b][a];
                    area += rpx[b][j] * rpy[b + 1][j] - rpx[b + 1][j] * rpy[b][j];
                    area += rpx[b + 1][j] * rpy[b + 1][a] - rpx[b + 1][a] * rpy[b + 1][j];
                    area += rpx[b + 1][a] * rpy[b][a] - rpx[b][a] * rpy[b + 1][a];
                    if (area <= 0f) {
                        continue;
                    }
                    float ex = rox[b][j] - rox[b][a];
                    float ez = roz[b][j] - roz[b][a];
                    float len = (float) Math.sqrt(ex * ex + ez * ez);
                    if (len < 0.0001f) {
                        len = 1f;
                    }
                    float hnx = -ez / len;
                    float hnz = ex / len;
                    float nx = hnx;
                    float ny = 0f;
                    float nz = hnz;
                    if (b == 0) {
                        nx = hnx * hT;
                        nz = hnz * hT;
                        ny = -tT;
                    } else if (b == 2) {
                        nx = hnx * hB;
                        nz = hnz * hB;
                        ny = tB;
                    }
                    float nl = (float) Math.sqrt(nx * nx + ny * ny + nz * nz);
                    if (nl < 0.0001f) {
                        nl = 1f;
                    }
                    nx /= nl;
                    ny /= nl;
                    nz /= nl;
                    float nxr = nx * c3t + nz * s3t;
                    float nzr = -nx * s3t + nz * c3t;
                    float ny2 = ny * c3p - nzr * s3p;
                    float nz2 = ny * s3p + nzr * c3p;
                    float lit = Math.max(0f, nxr * -0.45f + ny2 * -0.7f + nz2 * 0.62f);
                    int base = b == 0 ? colTop : (hnz < -0.5f ? colBack : colSide);
                    int i = fn++;
                    // Kilap plastik: highlight spekular (vektor setengah dari cahaya dan arah pandang).
                    float sp = Math.max(0f, nxr * -0.243f + ny2 * -0.378f + nz2 * 0.894f);
                    float spec = (float) Math.pow(sp, 18.0) * 0.30f;
                    fc[i] = lighten(shade(base, 0.64f + 0.52f * lit), spec);
                    flit[i] = 0.64f + 0.52f * lit;
                    fz[i] = (rpz[b][a] + rpz[b][j] + rpz[b + 1][j] + rpz[b + 1][a]) / 4f + zBias;
                    fsm[i] = true;
                    fq[i * 8] = rpx[b][a];
                    fq[i * 8 + 1] = rpy[b][a];
                    fq[i * 8 + 2] = rpx[b][j];
                    fq[i * 8 + 3] = rpy[b][j];
                    fq[i * 8 + 4] = rpx[b + 1][j];
                    fq[i * 8 + 5] = rpy[b + 1][j];
                    fq[i * 8 + 6] = rpx[b + 1][a];
                    fq[i * 8 + 7] = rpy[b + 1][a];
                    boolean decalFace = kind != 0 && b == 1 && hnz > 0.2f && srcOn;
                    fk[i] = decalFace ? kind : 0;
                    float bw = x1 - x0;
                    float bh = y1 - y0;
                    if (decalFace && bw > 0f && bh > 0f) {
                        fsrc[i * 4] = ovr[0] + (rox[1][a] - x0) / bw * (ovr[2] - ovr[0]);
                        fsrc[i * 4 + 1] = ovr[1] + (roy[1] - y0) / bh * (ovr[3] - ovr[1]);
                        fsrc[i * 4 + 2] = ovr[0] + (rox[1][j] - x0) / bw * (ovr[2] - ovr[0]);
                        fsrc[i * 4 + 3] = ovr[1] + (roy[2] - y0) / bh * (ovr[3] - ovr[1]);
                    } else {
                        fsrc[i * 4] = x0;
                        fsrc[i * 4 + 1] = y0;
                        fsrc[i * 4 + 2] = x1;
                        fsrc[i * 4 + 3] = y1;
                    }
                }
            }
            // tutup atas (kipas segitiga dari tengah)
            float ccx2 = (x0 + x1) / 2f - 50f;
            float ccz2 = (z0 + z1) / 2f + zShift;
            float cy = y0 - 50f + yShift;
            if (hs != 1f) {
                ccx2 *= hs;
                cy = (NECK_Y - 50f) + (cy - (NECK_Y - 50f)) * hs;
                ccz2 *= hs;
            }
            float cxr = ccx2 * c3t + ccz2 * s3t;
            float czr = -ccx2 * s3t + ccz2 * c3t;
            float cy2 = cy * c3p - czr * s3p;
            float cz2 = cy * s3p + czr * c3p;
            float csc = 260f / (260f - cz2);
            float cpx = fx(50f + cxr * csc * gs);
            float cpy = fy(GROUND_Y + (50f + cy2 * csc - GROUND_Y) * gs);
            float capLit = Math.max(0f, (-1f * c3p + 0f) * -0.7f + (-1f * s3p) * 0.62f);
            for (int a = 0; a < n && fn < MAXF; a++) {
                int j = (a + 1) % n;
                float area = cpx * rpy[0][j] - rpx[0][j] * cpy
                        + rpx[0][j] * rpy[0][a] - rpx[0][a] * rpy[0][j]
                        + rpx[0][a] * cpy - cpx * rpy[0][a];
                if (area <= 0f) {
                    continue;
                }
                int i = fn++;
                fc[i] = shade(colTop, 0.64f + 0.52f * capLit);
                fz[i] = (cz2 + rpz[0][j] + rpz[0][a]) / 3f + zBias;
                fsm[i] = true;
                fk[i] = 0;
                fq[i * 8] = cpx;
                fq[i * 8 + 1] = cpy;
                fq[i * 8 + 2] = rpx[0][j];
                fq[i * 8 + 3] = rpy[0][j];
                fq[i * 8 + 4] = rpx[0][a];
                fq[i * 8 + 5] = rpy[0][a];
                fq[i * 8 + 6] = rpx[0][a];
                fq[i * 8 + 7] = rpy[0][a];
                fsrc[i * 4] = x0;
                fsrc[i * 4 + 1] = y0;
                fsrc[i * 4 + 2] = x1;
                fsrc[i * 4 + 3] = y1;
            }
            srcOn = false;
        }

        // Tahap C: bentuk "loft" = tabung membulat yang penampangnya (lebar, dalam, radius) berubah di tiap cincin.
        // Dipakai untuk drape jilbab: jatuh dari kepala, melebar di bahu, lalu meruncing ke dada.
        // ly: tinggi cincin (naik ke bawah), lxa/lxb: tepi kiri/kanan, lza/lzb: tepi belakang/depan, lr: radius sudut.
        private void addLoft(float[] ly, float[] lxa, float[] lxb, float[] lza, float[] lzb, float[] lr, int nr, int color) {
            final int ns = 4;
            final int n = 4 * (ns + 1);
            for (int k = 0; k < nr; k++) {
                float rr = Math.max(0.3f, Math.min(lr[k], Math.min((lxb[k] - lxa[k]) / 2f, (lzb[k] - lza[k]) / 2f) - 0.01f));
                for (int cn = 0; cn < 4; cn++) {
                    float ccx = (cn == 0 || cn == 1) ? lxb[k] - rr : lxa[k] + rr;
                    float ccz = (cn == 0 || cn == 3) ? lzb[k] - rr : lza[k] + rr;
                    float a0 = cn == 0 ? 90f : (cn == 1 ? 0f : (cn == 2 ? -90f : 180f));
                    float a1 = a0 - 90f;
                    for (int sg = 0; sg <= ns; sg++) {
                        double ang = Math.toRadians(a0 + (a1 - a0) * sg / ns);
                        int idx = cn * (ns + 1) + sg;
                        float ox2 = ccx + rr * (float) Math.cos(ang);
                        float oz2 = ccz + rr * (float) Math.sin(ang);
                        lox[k][idx] = ox2;
                        loz[k][idx] = oz2;
                        float x = ox2 - 50f;
                        float y = ly[k] - 50f + yShift;
                        float z = oz2 + zShift;
                        float xr = x * c3t + z * s3t;
                        float zr = -x * s3t + z * c3t;
                        float y2 = y * c3p - zr * s3p;
                        float z2 = y * s3p + zr * c3p;
                        float sc = 260f / (260f - z2);
                        lpx[k][idx] = fx(50f + xr * sc * gs);
                        lpy[k][idx] = fy(GROUND_Y + (50f + y2 * sc - GROUND_Y) * gs);
                        lpz[k][idx] = z2;
                    }
                }
            }
            for (int b = 0; b < nr - 1; b++) {
                float dy = ly[b + 1] - ly[b];
                float ew = ((lxb[b + 1] - lxa[b + 1]) - (lxb[b] - lxa[b])) * 0.5f;
                float ed = ((lzb[b + 1] - lza[b + 1]) - (lzb[b] - lza[b])) * 0.5f;
                for (int a = 0; a < n && fn < MAXF; a++) {
                    int j = (a + 1) % n;
                    float area = 0f;
                    area += lpx[b][a] * lpy[b][j] - lpx[b][j] * lpy[b][a];
                    area += lpx[b][j] * lpy[b + 1][j] - lpx[b + 1][j] * lpy[b][j];
                    area += lpx[b + 1][j] * lpy[b + 1][a] - lpx[b + 1][a] * lpy[b + 1][j];
                    area += lpx[b + 1][a] * lpy[b][a] - lpx[b][a] * lpy[b + 1][a];
                    if (area <= 0f) {
                        continue;
                    }
                    float ex = lox[b][j] - lox[b][a];
                    float ez = loz[b][j] - loz[b][a];
                    float len = (float) Math.sqrt(ex * ex + ez * ez);
                    if (len < 0.0001f) {
                        len = 1f;
                    }
                    float hnx = -ez / len;
                    float hnz = ex / len;
                    // normal dinding miring: melebar ke bawah -> menghadap sedikit ke atas (y turun ke bawah, jadi negatif)
                    float nx = hnx * dy;
                    float ny = -(ew * Math.abs(hnx) + ed * Math.abs(hnz));
                    float nz = hnz * dy;
                    float nl = (float) Math.sqrt(nx * nx + ny * ny + nz * nz);
                    if (nl < 0.0001f) {
                        nl = 1f;
                    }
                    nx /= nl;
                    ny /= nl;
                    nz /= nl;
                    float nxr = nx * c3t + nz * s3t;
                    float nzr = -nx * s3t + nz * c3t;
                    float ny2 = ny * c3p - nzr * s3p;
                    float nz2 = ny * s3p + nzr * c3p;
                    float lit = Math.max(0f, nxr * -0.45f + ny2 * -0.7f + nz2 * 0.62f);
                    // bayangan lekuk kain: bagian bawah drape sedikit lebih gelap (ambient occlusion palsu)
                    float ao = 1f - 0.07f * b;
                    float sp = Math.max(0f, nxr * -0.243f + ny2 * -0.378f + nz2 * 0.894f);
                    float spec = (float) Math.pow(sp, 18.0) * 0.18f;
                    int i = fn++;
                    fc[i] = lighten(shade(color, (0.64f + 0.52f * lit) * ao), spec);
                    flit[i] = 0.64f + 0.52f * lit;
                    fz[i] = (lpz[b][a] + lpz[b][j] + lpz[b + 1][j] + lpz[b + 1][a]) / 4f + zBias;
                    fsm[i] = true;
                    fk[i] = 0;
                    fq[i * 8] = lpx[b][a];
                    fq[i * 8 + 1] = lpy[b][a];
                    fq[i * 8 + 2] = lpx[b][j];
                    fq[i * 8 + 3] = lpy[b][j];
                    fq[i * 8 + 4] = lpx[b + 1][j];
                    fq[i * 8 + 5] = lpy[b + 1][j];
                    fq[i * 8 + 6] = lpx[b + 1][a];
                    fq[i * 8 + 7] = lpy[b + 1][a];
                    fsrc[i * 4] = lxa[b];
                    fsrc[i * 4 + 1] = ly[b];
                    fsrc[i * 4 + 2] = lxb[b];
                    fsrc[i * 4 + 3] = ly[b + 1];
                }
            }
            srcOn = false;
        }

        // Gambar stiker 2D di sisi depan sebuah balok, ikut miring dan menyempit mengikuti perspektif.
        private void decal(Canvas c, int i, int kind) {
            srcPts[0] = fx(fsrc[i * 4]);
            srcPts[1] = fy(fsrc[i * 4 + 1]);
            srcPts[2] = fx(fsrc[i * 4 + 2]);
            srcPts[3] = fy(fsrc[i * 4 + 1]);
            srcPts[4] = fx(fsrc[i * 4 + 2]);
            srcPts[5] = fy(fsrc[i * 4 + 3]);
            srcPts[6] = fx(fsrc[i * 4]);
            srcPts[7] = fy(fsrc[i * 4 + 3]);
            if (!decalM.setPolyToPoly(srcPts, 0, fq, i * 8, 4)) {
                return;
            }
            c.save();
            c.concat(decalM);
            if (fsm[i]) {
                c.clipRect(srcPts[0] - 0.5f, srcPts[1], srcPts[4] + 0.5f, srcPts[5]);
            }
            if (kind == 11) {
            } else if (kind == 1) {
                if (curHijab) {
                    drawHijabOpening(c, shade(curSkin, flit[i]));
                }
                drawFaceAll(c);
                if (!curHijab) {
                    // Bayangan rahang tipis (hanya renderer 3D) supaya ada kesan dagu.
                    rf.set(fx(33f), fy(43.6f), fx(67f), fy(46f));
                    p.setStyle(Paint.Style.FILL);
                    p.setColor(0x1F5D2E1A);
                    c.drawRect(rf, p);
                }
            } else if (kind == 4) {
                drawHat(c, it[0], curHt);
            } else if (kind == 2) {
                if (it[5] >= 0) {
                    drawClothes(c, it[5]);
                }
                // v6.20.1: lambang hanya ada di seragam sekolah (digambar lewat addBox di drawChar3D), tidak ada lencana lama di sini
            } else if (kind == 3) {
                drawHeld(c, it[2]);
            }
            c.restore();
        }

        private void flushFaces(Canvas c) {
            for (int i = 0; i < fn; i++) {
                fo[i] = i;
            }
            for (int i = 1; i < fn; i++) {
                int key = fo[i];
                int j = i - 1;
                while (j >= 0 && fz[fo[j]] > fz[key]) {
                    fo[j + 1] = fo[j];
                    j--;
                }
                fo[j + 1] = key;
            }
            for (int n = 0; n < fn; n++) {
                int i = fo[n];
                int o = i * 8;
                path.reset();
                path.moveTo(fq[o], fq[o + 1]);
                path.lineTo(fq[o + 2], fq[o + 3]);
                path.lineTo(fq[o + 4], fq[o + 5]);
                path.lineTo(fq[o + 6], fq[o + 7]);
                path.close();
                p.setStyle(Paint.Style.FILL);
                p.setAlpha(255);
                // Gradasi lembut pada tiap sisi balok agar bentuk terasa seperti mainan 3D.
                float minX = Math.min(Math.min(fq[o], fq[o + 2]), Math.min(fq[o + 4], fq[o + 6]));
                float maxX = Math.max(Math.max(fq[o], fq[o + 2]), Math.max(fq[o + 4], fq[o + 6]));
                float minY = Math.min(Math.min(fq[o + 1], fq[o + 3]), Math.min(fq[o + 5], fq[o + 7]));
                float maxY = Math.max(Math.max(fq[o + 1], fq[o + 3]), Math.max(fq[o + 5], fq[o + 7]));
                p.setShader(new LinearGradient(minX, minY, maxX + 1f, maxY + 1f,
                        shade(fc[i], fsm[i] ? 1.07f : 1.17f), shade(fc[i], fsm[i] ? 0.91f : 0.86f), Shader.TileMode.CLAMP));
                c.drawPath(path, p);
                p.setShader(null);
                p.setStyle(Paint.Style.STROKE);
                p.setStrokeWidth(0.45f * u);
                p.setStrokeJoin(Paint.Join.ROUND);
                p.setColor(fsm[i] ? fc[i] : shade(fc[i], 0.78f));
                c.drawPath(path, p);
                p.setStyle(Paint.Style.FILL);
                if (fk[i] != 0) {
                    decal(c, i, fk[i]);
                    // Stiker (mis. pipi 0x77) meninggalkan alpha di cat; kembalikan agar sisi berikutnya tidak tembus.
                    p.setAlpha(255);
                    p.setStyle(Paint.Style.FILL);
                    p.setShader(null);
                }
            }
        }

        // Balok simetris kiri-kanan (pusat kepala x = 50).
        private void symBox(float xa, float xb, float ya, float yb, float z0, float z1, int c) {
            addBox(xa, ya, xb, yb, z0, z1, c, c, c, 0);
            addBox(100f - xb, ya, 100f - xa, yb, z0, z1, c, c, c, 0);
        }

        // Tanda tipis di muka tudung (depan dahi), simetris.
        private void frontMark(float xa, float xb, float ya, float yb, int c) {
            symBox(xa, xb, ya, yb, 19.7f, 20.7f, c);
        }

        // Tanda tipis di muka tudung, satu balok (tengah).
        private void frontMarkC(float xa, float xb, float ya, float yb, int c) {
            addBox(xa, ya, xb, yb, 19.7f, 20.7f, c, c, c, 0);
        }

        // Tanda tipis di puncak tudung.
        private void topMark(float xa, float xb, float z0, float z1, int c) {
            addBox(xa, 2.7f, xb, 4.2f, z0, z1, c, c, c, 0);
        }

        // Telinga / tanduk / surai tudung hewan sebagai balok 3D (20 hewan, 12 = tanpa tudung)
        private void addEars(int a, int main, int acc, float hx0, float hx1, float hy0, float hy1) {
            final int dk = shade(main, 0.62f);
            final int blk = col("#212121");
            final int wht = Color.WHITE;
            switch (a) {
                case 0: // singa
                    symBox(hx0 - 3f, hx0 + 6f, hy0 - 9f, hy0 - 1f, -4.5f, 4.5f, acc);
                    symBox(hx0 - 0.5f, hx0 + 3.5f, hy0 - 6.5f, hy0 - 2f, 2.5f, 4.9f, shade(acc, 1.35f));
                    symBox(hx0 - 8f, hx0 - 2f, hy0 + 1f, hy0 + 22f, -13f, 15f, acc);
                    symBox(hx0 - 10f, hx0 - 5f, hy0 + 8f, hy0 + 28f, -9f, 12f, shade(acc, 0.82f));
                    addBox(42f, hy0 - 8f, 58f, hy0 - 3f, 6f, 15f, acc, acc, acc, 0);
                    break;
                case 1: // harimau
                    symBox(hx0 - 3f, hx0 + 6f, hy0 - 9f, hy0 - 1f, -4.5f, 4.5f, main);
                    symBox(hx0 - 0.5f, hx0 + 3.5f, hy0 - 6.5f, hy0 - 2f, 2.5f, 4.9f, col("#FFE9C7"));
                    topMark(39f, 41.2f, -9f, 9f, acc);
                    topMark(48.9f, 51.1f, -9f, 9f, acc);
                    topMark(58.8f, 61f, -9f, 9f, acc);
                    frontMark(43f, 45.2f, 4.3f, 7.6f, acc);
                    frontMarkC(48.8f, 51.2f, 4.3f, 7.6f, acc);
                    symBox(27.4f, 28.7f, 11f, 13f, -6f, 14f, acc);
                    symBox(27.4f, 28.7f, 17f, 19f, -6f, 14f, acc);
                    symBox(27.4f, 28.7f, 23f, 25f, -6f, 14f, acc);
                    symBox(27.6f, 34f, 25f, 33f, 8f, 20.6f, wht);
                    break;
                case 2: // panda
                    symBox(hx0 - 3f, hx0 + 6f, hy0 - 9f, hy0 - 1f, -4.5f, 4.5f, acc);
                    symBox(hx0 - 0.5f, hx0 + 3.5f, hy0 - 6.5f, hy0 - 2f, 2.5f, 4.9f, col("#5A5A5A"));
                    symBox(27.4f, 28.7f, 14f, 27f, 2f, 17f, acc);
                    break;
                case 3: // kapibara: telinga kecil bulat
                    symBox(hx0 - 1f, hx0 + 6f, hy0 - 7f, hy0 - 1f, -4f, 4f, main);
                    symBox(hx0 + 1f, hx0 + 4.5f, hy0 - 5.5f, hy0 - 2f, 2.5f, 4.4f, acc);
                    break;
                case 4: // katak
                    symBox(hx0 + 2f, hx0 + 13f, hy0 - 9f, hy0 - 1f, -5f, 5f, main);
                    symBox(hx0 + 4f, hx0 + 11f, hy0 - 8f, hy0 - 2f, 1.5f, 5.4f, wht);
                    symBox(hx0 + 6f, hx0 + 9f, hy0 - 6.5f, hy0 - 3f, 5.3f, 5.8f, blk);
                    topMark(44f, 48f, -6f, -2f, shade(main, 0.78f));
                    topMark(55f, 58f, 2f, 6f, shade(main, 0.78f));
                    topMark(38f, 41f, 5f, 9f, shade(main, 0.78f));
                    break;
                case 5: // rubah
                    symBox(hx0 - 1f, hx0 + 7f, hy0 - 13f, hy0 - 1f, -3f, 3f, main);
                    symBox(hx0 + 0.5f, hx0 + 6f, hy0 - 13f, hy0 - 9.5f, -3.1f, 3.1f, col("#3E2723"));
                    symBox(hx0 + 1.5f, hx0 + 5f, hy0 - 10f, hy0 - 3f, 2.5f, 3.4f, col("#FFE0B2"));
                    symBox(27.5f, 34f, 25f, 33f, 8f, 20.6f, wht);
                                        break;
                case 6: // kelinci
                    symBox(hx0 + 5f, hx0 + 12f, hy0 - 22f, hy0 - 1f, -3f, 3f, main);
                    symBox(hx0 + 6.5f, hx0 + 10.5f, hy0 - 19f, hy0 - 3f, 2.6f, 3.4f, acc);
                    symBox(27.6f, 33f, 27f, 33f, 8f, 20.6f, wht);
                    break;
                case 7: // anjing
                    symBox(hx0 - 7f, hx0 - 1f, hy0 + 2f, hy0 + 24f, -5f, 5f, acc);
                    symBox(hx0 - 8f, hx0 - 6.5f, hy0 + 4f, hy0 + 20f, -3.5f, 3.5f, shade(acc, 0.8f));
                    frontMarkC(47f, 53f, 4.3f, 7.8f, col("#F5E6D3"));
                    topMark(36f, 44f, 2f, 12f, acc);
                    break;
                case 8: // kucing
                    symBox(hx0 - 1f, hx0 + 7f, hy0 - 13f, hy0 - 1f, -3f, 3f, main);
                    symBox(hx0 + 0.5f, hx0 + 5.5f, hy0 - 10f, hy0 - 2f, 2.5f, 3.4f, col("#F4A6B0"));
                    topMark(42f, 44f, -8f, 8f, acc);
                    topMark(48.5f, 51.5f, -8f, 8f, acc);
                    topMark(56f, 58f, -8f, 8f, acc);
                    frontMark(44f, 46f, 4.3f, 7.6f, acc);
                    frontMarkC(49f, 51f, 4.3f, 7.6f, acc);
                    symBox(27.4f, 28.7f, 12f, 13.6f, -6f, 14f, acc);
                    symBox(27.4f, 28.7f, 18f, 19.6f, -6f, 14f, acc);
                    break;
                case 9: { // unicorn
                    int gold = col("#FFD54F");
                    symBox(hx0, hx0 + 7f, hy0 - 8f, hy0 - 1f, -3f, 3f, main);
                    symBox(hx0 + 1f, hx0 + 5.5f, hy0 - 6f, hy0 - 2f, 2.5f, 3.4f, col("#F8BBD0"));
                    addBox(47f, hy0 - 15f, 53f, hy0 - 1f, -3f, 3f, gold, gold, gold, 0);
                    addBox(48.4f, hy0 - 19f, 51.6f, hy0 - 14f, -1.6f, 1.6f, col("#FFE082"), col("#FFE082"), col("#FFE082"), 0);
                    addBox(46.8f, hy0 - 11f, 53.2f, hy0 - 9.5f, -3.2f, 3.2f, col("#FFA000"), col("#FFA000"), col("#FFA000"), 0);
                    addBox(46.8f, hy0 - 6.5f, 53.2f, hy0 - 5f, -3.2f, 3.2f, col("#FFA000"), col("#FFA000"), col("#FFA000"), 0);
                    addBox(hx1 + 0.5f, hy0 - 2f, hx1 + 6f, hy0 + 26f, -12f, 8f, acc, acc, acc, 0);
                    addBox(hx1 + 3f, hy0 + 6f, hx1 + 8f, hy0 + 30f, -14f, 4f, col("#B39DDB"), col("#B39DDB"), col("#B39DDB"), 0);
                    break;
                }
                case 10: // penguin
                    symBox(hx0 + 1f, hx0 + 7f, hy0 - 9f, hy0 - 1f, -2.5f, 2.5f, col("#FFCA28"));
                    symBox(27.4f, 28.7f, 17f, 30f, 2f, 18f, wht);
                    frontMark(37f, 45f, 6f, 9.5f, wht);
                    addBox(46f, 6f, 54f, 12f, 20f, 24f, col("#FFB300"), col("#FFB300"), col("#FFB300"), 0);
                    break;
                case 11: { // ayam: jengger merah, paruh kuning
                    int yel = col("#FFC107");
                    addBox(46f, hy0 - 8f, 54f, hy0 - 1f, -4f, 8f, acc, acc, acc, 0);
                    addBox(47.5f, hy0 - 11f, 52.5f, hy0 - 6f, -2f, 5f, acc, acc, acc, 0);
                    addBox(47f, 8f, 53f, 12.5f, 20f, 23.5f, yel, yel, yel, 0);
                    addBox(48.5f, 12.5f, 51.5f, 15.5f, 20.5f, 22f, acc, acc, acc, 0);
                    symBox(37.5f, 39.8f, 6f, 8.6f, 19.8f, 20.8f, col("#2B1B12"));
                    break;
                }
                case 13: { // domba: gumpalan wol + telinga krem
                    int wool = shade(main, 0.94f);
                    addBox(37f, hy0 - 6f, 63f, hy0 - 1f, -12f, 10f, main, main, main, 0);
                    addBox(43f, hy0 - 9f, 57f, hy0 - 5f, -8f, 8f, wool, wool, wool, 0);
                    symBox(hx0 - 3f, hx0 + 1f, hy0, hy0 + 18f, -11f, 11f, wool);
                    symBox(hx0 - 7f, hx0 - 3f, hy0 + 10f, hy0 + 20f, -4f, 4f, acc);
                    break;
                }
                case 14: { // buaya: mata menonjol, gigi di depan, tulang punggung kuning
                    int eye = col("#F4E36B");
                    symBox(hx0 + 2f, hx0 + 13f, hy0 - 9f, hy0 - 1f, -5f, 5f, main);
                    symBox(hx0 + 4f, hx0 + 11f, hy0 - 8f, hy0 - 2f, 1.5f, 5.4f, eye);
                    symBox(hx0 + 6f, hx0 + 9f, hy0 - 6.5f, hy0 - 3f, 5.3f, 5.8f, blk);
                    topMark(46f, 54f, -8f, 8f, acc);
                    symBox(38f, 41f, 13f, 16f, 19.8f, 21f, wht);
                    symBox(43f, 46f, 13f, 16f, 19.8f, 21f, wht);
                    frontMarkC(48.5f, 51.5f, 13f, 16f, wht);
                    break;
                }
                case 15: // gajah
                    symBox(hx0 - 12f, hx0 - 1f, hy0 + 1f, hy0 + 22f, -6f, 8f, main);
                    symBox(hx0 - 12.8f, hx0 - 11.4f, hy0 + 4f, hy0 + 18f, -3f, 6f, acc);
                    // tiga garis kerut horizontal di dahi (bukan belalai tegak)
                    addBox(41.5f, 6f, 58.5f, 7.1f, 19.8f, 20.6f, shade(main, 0.74f), shade(main, 0.74f), shade(main, 0.74f), 0);
                    addBox(40.5f, 9.6f, 59.5f, 10.7f, 19.8f, 20.6f, shade(main, 0.74f), shade(main, 0.74f), shade(main, 0.74f), 0);
                    addBox(41.5f, 13.2f, 58.5f, 14.3f, 19.8f, 20.6f, shade(main, 0.74f), shade(main, 0.74f), shade(main, 0.74f), 0);
                    break;
                case 16: // zebra: telinga tegak, garis hitam putih
                    symBox(hx0 - 1f, hx0 + 7f, hy0 - 13f, hy0 - 1f, -3f, 3f, main);
                    symBox(hx0 + 0.5f, hx0 + 5.5f, hy0 - 10f, hy0 - 2f, 2.5f, 3.4f, acc);
                    topMark(40f, 42f, -8f, 8f, acc);
                    topMark(48.5f, 51.5f, -8f, 8f, acc);
                    topMark(58f, 60f, -8f, 8f, acc);
                    frontMark(43f, 45f, 4.3f, 7.6f, acc);
                    frontMarkC(49f, 51f, 4.3f, 7.6f, acc);
                    symBox(27.4f, 28.7f, 12f, 13.6f, -6f, 14f, acc);
                    symBox(27.4f, 28.7f, 18f, 19.6f, -6f, 14f, acc);
                    symBox(27.4f, 28.7f, 24f, 25.6f, -6f, 14f, acc);
                    break;
                case 21: // kuda: telinga tegak, surai dan jambul cokelat tua
                    symBox(hx0 + 1f, hx0 + 7f, hy0 - 12f, hy0 - 1f, -3f, 3f, main);
                    symBox(hx0 + 2f, hx0 + 5.5f, hy0 - 9f, hy0 - 2f, 2.5f, 3.4f, col("#EBCBA6"));
                    addBox(47f, hy0 - 5f, 53f, hy0 - 1f, -12f, 8f, acc, acc, acc, 0);
                    addBox(46f, hy0 + 2f, 54f, hy0 + 22f, -16f, -10f, acc, acc, acc, 0);
                    frontMarkC(46f, 54f, 4.3f, 8f, acc);
                    break;
                case 22: { // sapi: tanduk kecil, telinga mendatar, bercak hitam
                    symBox(hx0 + 2f, hx0 + 5f, hy0 - 8f, hy0 - 1f, -2f, 2f, col("#F3E6C8"));
                    symBox(hx0 - 6f, hx0 - 1f, hy0 + 6f, hy0 + 11f, -3f, 3f, main);
                    symBox(hx0 - 6.4f, hx0 - 5f, hy0 + 7f, hy0 + 10f, -2f, 2f, col("#F8BBD0"));
                    topMark(40f, 46f, -6f, 2f, acc);
                    topMark(55f, 59f, 2f, 8f, acc);
                    symBox(27.4f, 28.7f, 12f, 20f, -6f, 12f, acc);
                    symBox(27.4f, 28.7f, 26f, 32f, -2f, 8f, acc);
                    break;
                }
                case 24: // beruang
                    symBox(hx0 - 1f, hx0 + 9f, hy0 - 10f, hy0 - 1f, -5f, 5f, main);
                    symBox(hx0 + 1.5f, hx0 + 6.5f, hy0 - 7.5f, hy0 - 2f, 4f, 5.4f, acc);
                    break;
                case 23: // kura-kura
                    topMark(44f, 56f, -6f, 6f, acc);
                    topMark(36f, 42f, -6f, 6f, shade(acc, 0.88f));
                    topMark(58f, 64f, -6f, 6f, shade(acc, 0.88f));
                    frontMarkC(43f, 57f, 5f, 13f, acc);
                    frontMark(36f, 42f, 6f, 12f, shade(acc, 0.88f));
                    symBox(hx0 - 7f, hx0 - 1f, hy0 + 16f, hy0 + 30f, -4f, 8f, main);
                    break;
                default:
                    break;
            }
            face3d(a);
        }

        // Wajah hewan di dahi tudung (avatar balok): moncong, hidung, mata, kumis.
        private void face3d(int a) {
            int muz;
            int nose = col("#3A2418");
            boolean whisk = true;
            switch (a) {
                case 0: muz = col("#F7E6C4"); nose = col("#5D3A1E"); break;
                case 1: muz = col("#FFF3DF"); break;
                case 2: muz = Color.WHITE; whisk = false; break;
                case 5: muz = col("#FFF1E0"); break;
                case 6: muz = Color.WHITE; nose = col("#F48FB1"); break;
                case 7: muz = col("#F5E6D3"); whisk = false; break;
                case 8: muz = col("#FFF1E0"); nose = col("#F48FB1"); break;
                case 3: muz = col("#D8B58C"); whisk = false; break;
                case 13: muz = col("#F4E8DA"); nose = col("#C98B8B"); whisk = false; break;
                case 16: muz = col("#E6E6E6"); whisk = false; break;
                case 21: muz = col("#F3E1C8"); whisk = false; break;
                case 22: muz = col("#FFC9D4"); nose = col("#D6798F"); whisk = false; break;
                case 24: muz = col("#E2C6A0"); whisk = false; break;
                default: return;
            }
            int dk = col("#2B1B12");
            if (a == 2) {
                symBox(36f, 42f, 6f, 12f, 19.8f, 20.8f, dk);
            } else {
                symBox(37.5f, 39.8f, 6f, 8.6f, 19.8f, 20.8f, dk);
            }
            addBox(43f, 8.8f, 57f, 14f, 19.7f, 20.9f, muz, muz, muz, 0);
            addBox(47.8f, 9.6f, 52.2f, 11.8f, 20.8f, 21.8f, nose, nose, nose, 0);
            if (whisk) {
                symBox(34.5f, 42.5f, 10.8f, 11.4f, 20.3f, 20.9f, dk);
                symBox(34.5f, 42.5f, 12.8f, 13.4f, 20.3f, 20.9f, dk);
            }
        }

        // Avatar chibi anak SD: seragam putih-merah, putra / putri (kerudung), rambut dan tudung 3D.
        private void drawChar3D(Canvas c) {
            final int skin = col("#F6D3A8");
            final boolean hood = isHood(animal);
            final boolean girl = gender == 1;
            final boolean hijab = girl && !hood;
            curHood = hood;
            curHijab = hijab; // v6.13.4: kepala berukuran sama dengan putra, berwarna kain; wajah = bukaan oval (stiker)
            curSkin = skin;
            curHt = hood ? 8f : 13.5f;
            curHid = it[6];
            curHairC = curHid >= 0 ? col(itIcon(curHid)) : col("#4E342E");

            yShift = 0f;
            zShift = 0f;
            hs = 1f;
            gs = 0.94f;
            headBob = (float) Math.sin(animT * 2.0f + 0.5f) * 0.45f;
            armSwing = (float) Math.sin(animT * 1.4f) * 1.8f;
            float th = (float) Math.toRadians(avatarAngle + swayDeg);
            float pitch = (float) Math.toRadians(-16f);
            c3t = (float) Math.cos(th);
            s3t = (float) Math.sin(th);
            c3p = (float) Math.cos(pitch);
            s3p = (float) Math.sin(pitch);

            // Bayangan lembut (gradasi radial) di bawah kaki, sedikit menyempit saat badan naik.
            p.setStyle(Paint.Style.FILL);
            float shW = 31f * gs - headBob * 1.2f;
            float shCx = fx(50f);
            float shCy = fy(91.5f);
            float shRx = shW * u;
            float shRy = 6.2f * u;
            p.setShader(new android.graphics.RadialGradient(shCx, shCy, shRx,
                    new int[]{0x55000000, 0x22000000, 0x00000000},
                    new float[]{0f, 0.6f, 1f}, Shader.TileMode.CLAMP));
            c.save();
            c.translate(0f, shCy);
            c.scale(1f, shRy / shRx);
            c.translate(0f, -shCy);
            c.drawCircle(shCx, shCy, shRx, p);
            c.restore();
            p.setShader(null);
            if (it[3] >= 0) {
                float hop = Math.abs((float) Math.sin(animT * 3.0f)) * 1.4f;
                emoji(c, itIcon(it[3]), 14f, 78f - hop, 24f);
            }

            int shirt = col("#F3E6EC");
            int slv = shirt;
            boolean longSleeve = false;
            int cl = it[5];
            if (cl >= 0) {
                String[] cs = itIcon(cl).split(",");
                shirt = col(cs[0]);
                slv = col(cs[1]);
                longSleeve = true;
            }
            int red = col("#A11F2B"); if (cl >= 0) { String[] cs3 = itIcon(cl).split(","); if (cs3.length > 2) { red = col(cs3[2]); } }
            // v6.18.0: Putra tanpa baju dari toko memakai seragam SD Alam Muhammadiyah Kandangan:
            // kemeja putih, celana merah marun, kain leher marun bersimpul emas, lambang sekolah di dada.
            final boolean seragam = !hood && cl < 0;   // putra dan putri
            if (seragam) {
                shirt = col("#F8F8F8");
                slv = shirt;
                red = col("#9B1E2D");
            }
            int shoe = col("#212121");
            final float hx0 = 31f;
            final float hx1 = 69f;
            final float hy0 = 8f;
            final float hy1 = 42f;
            final float hz = 17f;
            float armLen = (longSleeve || girl) ? 19f : 11f; // v6.14.0: putri selalu berlengan panjang (hanya telapak tangan terlihat)

            fn = 0;
            // Tas sekolah 3D di punggung. Kedalaman negatif membuatnya berada di belakang badan.
            // Tali bahu digambar sebagai balok tipis di depan dada agar tetap terlihat saat diputar.
            int bagMain = col("#2E7D32");
            int bagTop = col("#43A047");
            int bagDark = col("#1B5E20");
            // v6.14.0: untuk putri tas turun (y +5) dan mundur (z -3.5) agar berada di atas kain kerudung.
            final float bdy = girl ? 5f : 0f;
            final float bdz = girl ? -3.5f : 0f;
            if (!tasOff) {
            addBox(39f, 45f + bdy, 61f, 61f + bdy, -13.5f + bdz, -7.2f + bdz, bagMain, bagTop, bagDark, 0);
            addBox(42f, 43.5f + bdy, 58f, 48f + bdy, -14.5f + bdz, -7.5f + bdz, bagTop, bagTop, bagMain, 0);
            }
            // Tali bahu + tali dada menempel di depan badan (induk: badan, permukaan depan).
            attach(36f, 42f, 64f, TORSO_BOT, -7f, 7f, 0);
            if (!(girl && !hood) && !tasOff) {
            addBox(38f, 42.5f, 41.5f, 59f, 7.05f, 7.9f, bagDark, bagDark, bagDark, 0);
            addBox(58.5f, 42.5f, 62f, 59f, 7.05f, 7.9f, bagDark, bagDark, bagDark, 0);
            addBox(41.5f, 55.3f, 58.5f, 57.3f, 7.05f, 7.6f, col("#A5D6A7"), col("#A5D6A7"), col("#2E7D32"), 0);
            }
            if (seragam) {
                int mar = col("#8E1B2E");
                int marD = col("#6D1422");
                int gold = col("#D4A82A");
                if (!girl) {
                // ujung kain leher menjuntai di dada (makin ke bawah makin sempit); putri berjilbab tanpa kain leher
                addBox(44.5f, 44.5f, 55.5f, 48.5f, 7.05f, 7.7f, mar, mar, marD, 0);
                addBox(46f, 48.5f, 54f, 52.5f, 7.05f, 7.7f, mar, mar, marD, 0);
                addBox(47.6f, 52.5f, 52.4f, 55.3f, 7.05f, 7.7f, mar, mar, marD, 0);
                // simpul emas di bawah leher
                addBox(47.2f, 44.8f, 52.8f, 47.8f, 7.7f, 8.4f, gold, gold, gold, 0);
                }
                // saku dada kiri pemakai (tepi jahitan + garis lipatan atas) dan lambang di dalamnya:
                // bintang kuning-jingga, cincin putih, bola bumi hijau dengan bagian biru
                int seam = col("#C9CDE0");
                addBox(52.2f, 55.0f, 58.4f, 55.4f, 7.05f, 7.35f, seam, seam, seam, 0);
                addBox(52.2f, 55.0f, 52.6f, 64.2f, 7.05f, 7.35f, seam, seam, seam, 0);
                addBox(58.0f, 55.0f, 58.4f, 64.2f, 7.05f, 7.35f, seam, seam, seam, 0);
                addBox(52.2f, 63.8f, 58.4f, 64.2f, 7.05f, 7.35f, seam, seam, seam, 0);
                addBox(52.6f, 56.6f, 58.0f, 56.9f, 7.05f, 7.3f, seam, seam, seam, 0);
                int org = col("#F5A623");
                int wht = col("#FFFFFF");
                int grn = col("#2E9E4F");
                int blu = col("#1E4FCC");
                addBox(53.2f, 57.6f, 58f, 62.4f, 7.05f, 7.5f, org, org, org, 0);
                addBox(53.7f, 58.1f, 57.5f, 61.9f, 7.5f, 7.8f, wht, wht, wht, 0);
                addBox(54.3f, 58.7f, 56.9f, 61.3f, 7.8f, 8f, grn, grn, grn, 0);
                addBox(55.6f, 58.7f, 56.9f, 60f, 8f, 8.2f, blu, blu, blu, 0);
            }
            detach();
            // kaki / rok + sepatu
            float shoeTop = 83f;
            if (girl) {
                // Rok v6.14.0: rok panjang A-line sampai mata kaki, kaki tidak terlihat (hanya sepatu).
                addRBox(36f, 64.5f, 64f, 77f, -7.2f, 7.2f, 6.5f, 1f, 2f, 1f, 2f, red, red, red, 0);
                addRBox(34f, 73f, 66f, 84.4f, -8.2f, 8.2f, 7f, 0.8f, 1.2f, 0.8f, 1.2f, red, red, red, 0);
            } else {
                addRBox(38f, 65f, 49.7f, 83f, -5.5f, 5.5f, 5f, 1f, 2f, 1f, 2f, red, red, red, 0);
                addRBox(50.3f, 65f, 62f, 83f, -5.5f, 5.5f, 5f, 1f, 2f, 1f, 2f, red, red, red, 0);
            }
            int sole = col("#F5F5F5");
            addRBox(37f, shoeTop, 49.7f, 86.6f, -5.5f, 9f, 4f, 0.8f, 1.5f, 0.5f, 1f, shoe, shoe, shoe, 0);
            addRBox(36.6f, 86.6f, 50.1f, 89f, -5.9f, 9.4f, 3f, 0.5f, 0.6f, 0.5f, 0.6f, sole, sole, sole, 0);
            addRBox(50.3f, shoeTop, 63f, 86.6f, -5.5f, 9f, 4f, 0.8f, 1.5f, 0.5f, 1f, shoe, shoe, shoe, 0);
            addRBox(49.9f, 86.6f, 63.4f, 89f, -5.9f, 9.4f, 3f, 0.5f, 0.6f, 0.5f, 0.6f, sole, sole, sole, 0);
            // lengan membulat: lengan baju, lengan bawah, dan telapak tangan
            zShift = armSwing;
            addArmL(25.6f, 34.6f, armLen, skin, slv, 0);
            zShift = -armSwing;
            addArmL(65.4f, 74.4f, armLen, skin, slv, 3);
            zShift = 0f;
            // badan
            if (!(girl && !hood)) {
                // leher (di balik jilbab pada putri berjilbab)
                // Leher lebih ramping dan sedikit lebih gelap (bayangan di bawah dagu).
                int neck = shade(skin, 0.9f);
                addRBox(45f, 36f, 55f, 45f, -4.2f, 4.2f, 4f, 0.5f, 1f, 0.5f, 1f, neck, neck, neck, 0);
                if (seragam) {
                    // lingkar kain leher marun di pangkal leher
                    int collar = col("#8E1B2E");
                    addBox(43.8f, 39.5f, 56.2f, 44.8f, -5.4f, 5.4f, collar, collar, shade(collar, 0.85f), 0);
                }
            }
            srcRect(34f, 44f, 66f, 68f);
            addRBox(36f, 42f, 64f, TORSO_BOT, -7f, 7f, 6.5f, 1.2f, 2.2f, 1f, 2f, shirt, shirt, shirt, 2); if (!girl) { zBias = 6f; skipTop = true; addBox(35.5f, TORSO_BOT - 3.4f, 64.5f, TORSO_BOT + 0.6f, -7.5f, 7.5f, col("#2B2B2B"), col("#2B2B2B"), col("#2B2B2B"), 0); skipTop = false; zBias = 0f; }
            {
                // Bahu: pelat membulat yang menyambung badan ke pangkal lengan (kesan bahu dan tidak "kotak").
                addRBox(33.2f, 41.6f, 66.8f, 49.5f, -5.6f, 5.6f, 4.8f, 1.5f, 2f, 1f, 2f, shirt, shirt, shirt, 0);
            }
            if (girl) {
                // v6.14.0: kain kerudung jatuh menutup leher, bahu, dada atas dan punggung atas (aurat tertutup).
                int wh = (curHid >= 0) ? curHairC : col("#F7F7F7");
                int whd = shade(wh, 0.93f);
                addRBox(34f, 38.5f, 66f, 55f, -10f, 10.5f, 8f, 4f, 4f, 3f, 4f, wh, shade(wh, 1.04f), whd, 0);
            }
            // kepala (ikut bergerak halus naik-turun mengikuti napas)
            yShift = headBob - HEAD_LIFT;
            hs = HEAD_SCALE;
            int hc = hood ? col(HOOD_MAIN[animal]) : (hijab ? ((curHid >= 0) ? curHairC : col("#F7F7F7")) : curHairC);
            curHijabC = hc;
            float m = hood ? 3f : 1.5f;
            {
                srcRect(33f, 14f, 67f, 46f);
                addRBox(hx0, hy0, hx1, hy1, -hz, hz, hijab ? 11.5f : 10f, hijab ? 5f : 4f, hijab ? 4.5f : 3.5f, 3f, 3f, hijab ? hc : skin, hc, hc, 1);
            }
            attach(hx0, hy0, hx1, hy1, -hz, hz, -1);
            if (!hijab) {
            // penutup atas + poni (stiker topi menempel di sisi depan balok ini)
            srcRect(33f, 14f, 67f, 46f);
            addBox(hx0 - m, hy0 - m - 1.5f, hx1 + m, hy0 + 7f, -hz - m, hz + m, hc, hc, hc, 0);
            // belakang kepala
            float backBottom = hood ? hy1 - 6f : hijab ? hy1 + 12f : hy0 + 24f;
            addBox(hx0 - m, hy0 - m, hx1 + m, backBottom, -hz - m, -hz + 3f, hc, hc, hc, 0);
            // sisi kepala
            float sideBottom = hood ? hy1 - 10f : hijab ? hy1 + 6f : hy0 + 14f;
            float sideZ1 = (hood || hijab) ? hz + m : hz - 7f;
            addBox(hx0 - m, hy0 - m, hx0 + 3f, sideBottom, -hz - m, sideZ1, hc, hc, hc, 0);
            addBox(hx1 - 3f, hy0 - m, hx1 + m, sideBottom, -hz - m, sideZ1, hc, hc, hc, 0);
            }
            if (hijab) {
                // Kerudung v6.13.4: kepala sendiri berwarna kain (wajah = bukaan oval), ditambah kain belakang yang jatuh ke bahu.
                addRBox(hx0 - 0.5f, hy0 + 3f, hx1 + 0.5f, hy1 - 1f, -hz - 1.5f, -4f, 9f, 0f, 0f, 3f, 5f, shade(hc, 0.96f), hc, shade(hc, 0.92f), 0);
            }
            if (hood) {
                // sedikit poni di dahi di bawah tudung
                int bangC = (girl && curHid < 0) ? col("#F7F7F7") : curHairC; addBox(hx0 + 3f, hy0 + 7f, hx1 - 3f, hy0 + 11f, hz - 0.5f, hz + 0.8f, bangC, bangC, bangC, 0);
                addEars(animal, hc, col(HOOD_ACC[animal]), hx0, hx1, hy0, hy1);
            } else if (!hijab) {
                if (curHid == 41) {
                    // belah samping: poni besar menyapu dahi ke kiri, sisi kanan pendek
                    addBox(hx0 - m, hy0 + 7f, hx1 - 9f, hy0 + 14f, hz - 1f, hz + m, hc, hc, hc, 0);
                    addBox(hx1 - 9f, hy0 + 7f, hx1 + m, hy0 + 10.5f, hz - 1f, hz + m, hc, hc, hc, 0);
                } else if (curHid == 42) {
                    // ikal: gumpalan kecil di atas kepala
                    for (int k = 0; k < 4; k++) {
                        float cx = 37f + k * 8.7f;
                        addBox(cx - 4f, hy0 - 6.5f, cx + 4f, hy0 - 1.5f, -5.5f, 5.5f, hc, hc, hc, 0);
                    }
                    addBox(hx0 - m, hy0 + 7f, hx1 + m, hy0 + 12f, hz - 1f, hz + m, hc, hc, hc, 0);
                } else {
                    addBox(hx0 - m, hy0 + 7f, hx1 + m, hy0 + 12f, hz - 1f, hz + m, hc, hc, hc, 0);
                }
            }
            float hairLift = 0f;
            if (!hood && !hijab && (curHid < 0 || curHid == 39)) {
                addCurlyHair(hx0, hx1, hy0, hy1, hz, m, hc);
                hairLift = 5f;
            }
            detach();
            curSkipFace = it[1] == 8 || it[1] == 10;
            if (it[0] >= 0) {
                addHat3D(it[0], hy0 - m - 1.5f - hairLift);
            }
            if (curSkipFace) {
                boolean cover = hood; zBias = c3t > 0.2f ? 40f : 0f; addGlasses3D(it[1], hx0, hx1, hz, (cover ? m + 0.9f : 0.9f), cover ? -4f : 0f, (cover ? hz + m + 0.5f : hz + 1.2f)); zBias = 0f;
            }
            yShift = 0f;
            hs = 1f;
            flushFaces(c);
        }

        // Lengan membulat: telapak tangan, lengan bawah (bila lengan baju pendek), lalu lengan baju.
        // kind 3 = stiker barang pegang (lengan kanan).
        private void addArmL(float x0, float x1, float armLen, int skinC, int slvC, int kind) {
            // Tahap C: lengan lebih panjang dan renggang dari badan; tangan lebih ramping dan menggantung di pinggul.
            addRBox(x0 + 0.5f, HAND_TOP, x1 - 0.5f, HAND_BOT, -3.9f, 3.9f, 3.9f, 1f, 2f, 1f, 2f, skinC, skinC, skinC, 0);
            if (42.4f + armLen < HAND_TOP - 1.5f) {
                addRBox(x0 + 1f, 42.4f + armLen - 0.5f, x1 - 1f, HAND_TOP + 2f, -3.4f, 3.4f, 3.4f, 0.5f, 1f, 0.5f, 1f, skinC, skinC, skinC, 0);
            }
            if (kind == 3) {
                srcRect(66f, 45f, 76f, armLen > 14f ? 61f : 53f);
            }
            addRBox(x0, 42.4f, x1, 42.4f + armLen, -4.7f, 4.7f, 4.5f, 1f, 2f, 0.5f, 1f, slvC, slvC, slvC, kind);
        }

        private void bx(float x0, float y0, float x1, float y1, float z0, float z1, int color) {
            addBox(x0, y0, x1, y1, z0, z1, color, color, color, 0);
        }

        // Topi sebagai balok 3D; yT = permukaan atas kepala/penutup kepala
        private void addHat3D(int id, float yT) {
            if (id == 0) {
                int straw = col("#F6D77A");
                bx(40f, yT - 9f, 60f, yT, -10f, 10f, straw);
                bx(39.6f, yT - 4f, 60.4f, yT - 0.5f, -10.4f, 10.4f, col("#E53935"));
                bx(29f, yT - 1f, 71f, yT + 2f, -21f, 21f, straw);
            } else if (id == 1) {
                int pk = col("#F06292");
                bx(52f, yT - 5f, 60f, yT + 1f, -2f, 2f, pk);
                bx(62f, yT - 5f, 70f, yT + 1f, -2f, 2f, pk);
                bx(59f, yT - 4f, 63f, yT, -3f, 3f, col("#C2185B"));
            } else if (id == 2) {
                int pt = col("#F8BBD0");
                bx(54.5f, yT - 3f, 58.5f, yT + 1f, -2f, 2f, pt);
                bx(61.5f, yT - 3f, 65.5f, yT + 1f, -2f, 2f, pt);
                bx(58f, yT - 6.5f, 62f, yT - 2.5f, -2f, 2f, pt);
                bx(58f, yT + 0.5f, 62f, yT + 4.5f, -2f, 2f, pt);
                bx(58.5f, yT - 2f, 61.5f, yT + 1f, -2.6f, 2.6f, col("#FFC107"));
            } else if (id == 3) {
                bx(38f, yT - 5f, 62f, yT, -11f, 11f, col("#212121"));
                bx(28f, yT - 8f, 72f, yT - 5f, -22f, 22f, col("#263238"));
                int gold = col("#FFC107");
                bx(64.5f, yT - 7.5f, 67f, yT + 5f, -1f, 1f, gold);
                bx(64f, yT + 5f, 67.5f, yT + 8f, -1.5f, 1.5f, gold);
            } else if (id == 4) {
                bx(35f, yT - 9f, 65f, yT + 1f, -19f, 19f, col("#E53935"));
                bx(33f, yT, 67f, yT + 3f, -19f, 24f, col("#C62828"));
                bx(47.5f, yT - 7f, 52.5f, yT - 2f, 19f, 19.8f, Color.WHITE);
            } else if (id == 5) {
                int blk = col("#212121");
                bx(40f, yT - 14f, 60f, yT, -10f, 10f, blk);
                bx(39.6f, yT - 6f, 60.4f, yT - 2f, -10.4f, 10.4f, col("#C62828"));
                bx(34f, yT - 1f, 66f, yT + 2f, -20f, 20f, blk);
            } else if (id == 6) {
                int gold = col("#FFC107");
                bx(47f, yT - 15f, 53f, yT - 1f, -2.5f, 2.5f, gold);
                bx(41f, yT - 11f, 59f, yT - 5f, -2.5f, 2.5f, gold);
            } else if (id == 7) {
                int gold = col("#FFC107");
                bx(37f, yT - 4f, 63f, yT + 1f, -13f, 13f, gold);
                bx(37f, yT - 10f, 43f, yT - 4f, -4f, 4f, gold);
                bx(47f, yT - 12f, 53f, yT - 4f, -4f, 4f, gold);
                bx(57f, yT - 10f, 63f, yT - 4f, -4f, 4f, gold);
                bx(48.5f, yT - 3f, 51.5f, yT, 13f, 14.2f, col("#D32F2F"));
            }
        }

        // Kacamata 3D: bingkai di depan wajah + gagang ke samping kepala
        private void addGlasses3D(int id, float hx0, float hx1, float hz, float off, float zb, float zf) {
            float ey = 28f;
            float[] ex = {42.8f, 57.2f};
            if (id == 8) {
                int fr = col("#263238");
                for (int k = 0; k < 2; k++) {
                    float cx = ex[k];
                    bx(cx - 5.5f, ey - 4.2f, cx + 5.5f, ey - 3f, hz, hz + 1.2f, fr);
                    bx(cx - 5.5f, ey + 3f, cx + 5.5f, ey + 4.2f, hz, hz + 1.2f, fr);
                    bx(cx - 5.5f, ey - 4.2f, cx - 4.3f, ey + 4.2f, hz, hz + 1.2f, fr);
                    bx(cx + 4.3f, ey - 4.2f, cx + 5.5f, ey + 4.2f, hz, hz + 1.2f, fr);
                }
                bx(48.3f, ey - 1.5f, 51.7f, ey - 0.3f, hz, hz + 1.2f, fr);
                bx(hx0 - off, ey - 1.5f, hx0 - off + 1.2f, ey - 0.3f, zb, zf, fr);
                bx(hx1 + off - 1.2f, ey - 1.5f, hx1 + off, ey - 0.3f, zb, zf, fr);
                if (curHijab) {
                    bx(hx0 - off, ey - 1.5f, 37.3f, ey - 0.3f, hz, hz + 1.2f, fr);
                    bx(62.7f, ey - 1.5f, hx1 + off, ey - 0.3f, hz, hz + 1.2f, fr);
                }
            } else {
                int dk = col("#111111");
                for (int k = 0; k < 2; k++) {
                    float cx = ex[k];
                    bx(cx - 6.5f, ey - 4.2f, cx + 6.5f, ey + 4f, hz, hz + 1.2f, dk);
                }
                bx(48.5f, ey - 2.5f, 51.5f, ey - 1f, hz, hz + 1.2f, dk);
                bx(hx0 - off, ey - 2.5f, hx0 - off + 1.2f, ey - 1f, zb, zf, dk);
                bx(hx1 + off - 1.2f, ey - 2.5f, hx1 + off, ey - 1f, zb, zf, dk);
                if (curHijab) {
                    bx(hx0 - off, ey - 2.5f, 36.3f, ey - 1f, hz, hz + 1.2f, dk);
                    bx(63.7f, ey - 2.5f, hx1 + off, ey - 1f, hz, hz + 1.2f, dk);
                }
            }
        }

        // Rambut ikal voxel: kumpulan kubus kecil (atas, belakang, samping, poni bergerigi)
        private void addCurlyHair(float hx0, float hx1, float hy0, float hy1, float hz, float m, int hc) {
            int hl = shade(hc, 1.18f);
            int hd = shade(hc, 0.8f);
            int[] tone = {hc, hl, hd};
            float yT = hy0 - m - 1.5f;
            float xl = hx0 - m;
            float xr = hx1 + m;
            float zb = -hz - m;
            float zf = hz + m;
            float cw = (xr - xl) / 6f;
            float cd = (zf - zb) / 5f;
            // lapisan atas
            for (int i = 0; i < 6; i++) {
                for (int j = 0; j < 5; j++) {
                    float h = 3f + ((i * 7 + j * 13) % 5) / 4f * 3f;
                    if ((i == 0 || i == 5) && (j == 0 || j == 4)) {
                        h -= 1.5f;
                    }
                    int cc = tone[(i + j * 2) % 3];
                    float x0 = xl + i * cw - 0.4f;
                    float z0 = zb + j * cd - 0.4f;
                    addBox(x0, yT - h, x0 + cw + 0.8f, yT + 1.5f, z0, z0 + cd + 0.8f, cc, cc, cc, 0);
                }
            }
            // belakang kepala (dua baris)
            for (int i = 0; i < 6; i++) {
                for (int r = 0; r < 2; r++) {
                    int cc = tone[(i + r) % 3];
                    float x0 = xl + i * cw - 0.3f;
                    float y0 = hy0 + 8f + r * 8f;
                    addBox(x0, y0, x0 + cw + 0.6f, y0 + 8.4f, zb - 3f, zb + 5f, cc, cc, cc, 0);
                }
            }
            // samping kiri dan kanan
            for (int side = 0; side < 2; side++) {
                float sx0 = side == 0 ? xl - 3f : xr - 3.5f;
                float sx1 = side == 0 ? xl + 3.5f : xr + 3f;
                for (int r = 0; r < 2; r++) {
                    for (int k = 0; k < 2; k++) {
                        int cc = tone[(side + r + k) % 3];
                        float y0 = hy0 + 6f + r * 7f;
                        float z0 = -14f + k * 10f;
                        addBox(sx0, y0, sx1, y0 + 7.4f, z0, z0 + 10.4f, cc, cc, cc, 0);
                    }
                }
            }
            // poni bergerigi di dahi
            for (int i = 0; i < 6; i++) {
                float bot = 16f + ((i * 5 + 2) % 4) * 1.4f;
                int cc = tone[(i * 2) % 3];
                float x0 = xl + i * cw;
                addBox(x0, hy0 + 2f, x0 + cw, bot, hz - 2.5f, hz + 3.5f, cc, cc, cc, 0);
            }
        }

        // Bukaan wajah jilbab: kulit berbentuk persegi membulat dengan garis tepi searah warna jilbab
        private void drawHijabOpening(Canvas c, int skinCol) {
            // Bukaan wajah oval membulat dengan tepi kain tipis (gelap di luar, terang di dalam)
            float rr = 12f * u;
            rf.set(fx(37.5f), fy(19f), fx(62.5f), fy(42.5f));
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(4f * u);
            p.setColor(shade(curHijabC, 0.86f));
            c.drawRoundRect(rf, rr, rr, p);
            p.setStrokeWidth(2.2f * u);
            p.setColor(shade(curHijabC, 1.05f));
            c.drawRoundRect(rf, rr, rr, p);
            p.setStyle(Paint.Style.FILL);
            p.setColor(skinCol);
            c.drawRoundRect(rf, rr, rr, p);
            // bayangan lembut di dahi tempat kain menaungi kulit
            c.save();
            Path cp = new Path();
            cp.addRoundRect(rf, rr, rr, Path.Direction.CW);
            c.clipPath(cp);
            p.setColor(shade(skinCol, 0.9f));
            c.drawRect(fx(36f), fy(16f), fx(64f), fy(20.5f), p);
            c.restore();
            p.setStyle(Paint.Style.FILL);
        }

        private void drawFaceAll(Canvas c) {
            if (it[1] == 11) {
                drawMask(c);
                dot(c, 43.6f, 32.7f, 3.4f, Color.WHITE);
                dot(c, 56.4f, 32.7f, 3.4f, Color.WHITE);
            }
            int eyeDark = col("#3B2418");
            int iris = col("#8A5A3C");
            float[] ecx = {43.6f, 56.4f};
            boolean blink = isBlinking();
            for (int k = 0; k < 2; k++) {
                float cx = ecx[k];
                if (blink) {
                    line(c, cx - 2.8f, 32.5f, cx + 2.8f, 32.5f, 1.7f, eyeDark);
                    line(c, cx - 1.8f, 31.6f, cx + 1.8f, 31.6f, 0.8f, col("#B77B60"));
                } else {
                    rf.set(fx(cx - 2.5f), fy(29.2f), fx(cx + 2.5f), fy(35.6f));
                    p.setStyle(Paint.Style.FILL);
                    p.setColor(eyeDark);
                    c.drawOval(rf, p);
                    dot(c, cx, 33.2f, 1.8f, iris);
                    dot(c, cx, 33.2f, 1.0f, col("#1E120B"));
                    dot(c, cx - 0.8f, 31.4f, 0.95f, Color.WHITE);
                    dot(c, cx + 1.2f, 34.4f, 0.45f, Color.WHITE);
                }
            }
            line(c, 39.8f, 26.6f, 46.2f, 25.4f, 1.7f, col("#3E2723"));
            line(c, 60.2f, 26.6f, 53.8f, 25.4f, 1.7f, col("#3E2723"));
            // Tahap C: hidung kecil (bayangan lembut + kilau) dan bayangan rahang agar wajah tidak datar.
            rf.set(fx(48.7f), fy(34.4f), fx(51.3f), fy(36.4f));
            p.setStyle(Paint.Style.FILL);
            p.setColor(0x55A5654A);
            c.drawOval(rf, p);
            dot(c, 49.7f, 34.9f, 0.45f, 0x66FFFFFF);
            rf.set(fx(46.5f), fy(35.5f), fx(53.5f), fy(40.5f));
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(1.2f * u);
            p.setStrokeCap(Paint.Cap.ROUND);
            p.setColor(col("#A0443A"));
            c.drawArc(rf, 20f, 140f, false, p);
            p.setStyle(Paint.Style.FILL);
            if (it[1] != 9) {
                dot(c, 39.2f, 38f, 2.6f, 0x77FF8A80);
                dot(c, 60.8f, 38f, 2.6f, 0x77FF8A80);
            }
            if (!curSkipFace) {
                drawFace(c, it[1]);
            }
        }

        @Override
        public boolean onTouchEvent(android.view.MotionEvent event) {
            if (mode != 0) return super.onTouchEvent(event);
            switch (event.getActionMasked()) {
                case android.view.MotionEvent.ACTION_DOWN:
                    touching = true;
                    lastTouchX = event.getX();
                    getParent().requestDisallowInterceptTouchEvent(true);
                    return true;
                case android.view.MotionEvent.ACTION_MOVE:
                    float dx = event.getX() - lastTouchX;
                    lastTouchX = event.getX();
                    avatarAngle = (avatarAngle + dx * 1.2f) % 360f;
                    invalidate();
                    return true;
                case android.view.MotionEvent.ACTION_UP:
                case android.view.MotionEvent.ACTION_CANCEL:
                    touching = false;
                    performClick();
                    return true;
            }
            return true;
        }

        @Override
        public boolean performClick() {
            super.performClick();
            return true;
        }

        @Override
        protected void onDraw(Canvas c) {
            float w = getWidth();
            float h = getHeight();
            float s = Math.min(w, h);
            float left = (w - s) / 2f;
            float top = (h - s) / 2f;
            u = s / 100f;
            ox = left;
            oy = top;
            p.setStyle(Paint.Style.FILL);
            if (tile) {
                if (it[4] >= 0) {
                    GradientDrawable g = bgDrawable(it[4], s * 0.24f);
                    g.setBounds((int) left, (int) top, (int) (left + s), (int) (top + s));
                    g.draw(c);
                    if (it[4] == ITEM_GALAKSI) {
                        p.setColor(Color.WHITE);
                        float[][] st = {{0.15f, 0.2f}, {0.82f, 0.15f}, {0.3f, 0.86f}, {0.9f, 0.62f},
                                {0.08f, 0.55f}, {0.6f, 0.1f}, {0.74f, 0.9f}};
                        for (int k = 0; k < st.length; k++) {
                            c.drawCircle(left + st[k][0] * s, top + st[k][1] * s,
                                    s * (k % 2 == 0 ? 0.016f : 0.011f), p);
                        }
                    }
                } else {
                    p.setColor(col("#E3F2FD"));
                    rf.set(left, top, left + s, top + s);
                    c.drawRoundRect(rf, s * 0.24f, s * 0.24f, p);
                }
            }
            c.save();
            if (mode == 1) {
                c.translate(w / 2f, h / 2f);
                c.scale(1.7f, 1.7f);
                c.translate(-fx(50f), -fy(33f));
            } else if (mode == 2) {
                c.translate(w / 2f, h / 2f);
                c.scale(1.5f, 1.5f);
                c.translate(-fx(60f), -fy(58f));
            }
            c.save();
            c.translate(fx(50f), fy(90f));
            c.scale(0.95f, 0.95f);
            c.translate(-fx(50f), -fy(90f));
            if (mode == 0) {
                // Animasi idle ringan: napas halus dan kedipan mata berkala.
                float t = (System.currentTimeMillis() - animEpoch) / 1000f;
                animT = t;
                // Napas: badan memuai sedikit (kaki tetap menapak), goyang pelan saat tidak disentuh.
                float br = (float) Math.sin(t * 2.0f);
                c.scale(1f - br * 0.004f, 1f + br * 0.010f, fx(50f), fy(90f));
                swayAmp += ((touching ? 0f : 1f) - swayAmp) * 0.08f;
                swayDeg = (float) Math.sin(t * 0.9f) * 5f * swayAmp;
                drawChar3D(c);
                if (isAttachedToWindow()) postInvalidateDelayed(40L);
            } else {
                drawChar(c);
            }
            p.setShader(null);
            c.restore();
            c.restore();
        }

        private void drawChar(Canvas c) {
            final int skin = col("#F2CFA5");
            final boolean hood = isHood(animal);
            final float ht = hood ? 8f : 13.5f;

            p.setStyle(Paint.Style.FILL);
            p.setColor(0x33000000);
            rf.set(fx(20f), fy(86.5f), fx(80f), fy(96f));
            c.drawOval(rf, p);

            if (it[3] >= 0) {
                emoji(c, itIcon(it[3]), 14f, 78f, 24f);
            }

            int pants = col("#C62828"); if (it[5] >= 0) { String[] cp = itIcon(it[5]).split(","); if (cp.length > 2) { pants = col(cp[2]); } }
            if (gender == 1) {
                rect(c, 35f, 64f, 65f, 86f, 3f, pants); // rok panjang sampai mata kaki
            } else {
                rect(c, 38f, 66f, 49.6f, 87f, 2f, pants);
                rect(c, 50.4f, 66f, 62f, 87f, 2f, pants);
            }
            rect(c, 36f, 83.5f, 49.6f, 89f, 2.5f, col("#212121"));
            rect(c, 50.4f, 83.5f, 64f, 89f, 2.5f, col("#212121"));

            int mainC = col("#F7F7F7");
            int slvC = mainC;
            boolean longSleeve = false;
            int cl = it[5];
            if (cl >= 0) {
                String[] cs = itIcon(cl).split(",");
                mainC = col(cs[0]);
                slvC = col(cs[1]);
                longSleeve = true;
            }
            float armEnd = (longSleeve || gender == 1) ? 61f : 53f;
            rect(c, 24f, 45f, 34f, 67f, 2.5f, skin);
            rect(c, 66f, 45f, 76f, 67f, 2.5f, skin);
            rect(c, 24f, 45f, 34f, armEnd, 2.5f, slvC);
            rect(c, 66f, 45f, 76f, armEnd, 2.5f, slvC);
            rect(c, 34f, 44f, 66f, 68f, 3f, mainC);
            if (cl >= 0) {
                drawClothes(c, cl);
            }

            int hid = it[6];
            int hairC = hid >= 0 ? col(itIcon(hid)) : col("#5D4037");
            rect(c, 33f, 14f, 67f, 46f, 6f, skin);
            boolean hijab2d = gender == 1 && !hood;
            if (hijab2d) {
                int wh = (hid >= 0) ? hairC : col("#F7F7F7");
                hoodRects(c, wh, 0f);
                rect(c, 35f, 44f, 65f, 58f, 3f, wh); // v6.14.0: kain menutup leher dan dada atas
            } else {
                drawHair(c, hid, hairC, hood);
            }
            if (hood) {
                drawHood(c, animal);
            }

            drawFaceAll(c);
            drawHat(c, it[0], ht);
            drawHeld(c, it[2]);
        }

        private void drawClothes(Canvas c, int id) {
            if (id == 34) {
                flat(c, 42f, 57f, 58f, 66f, 2f, col("#1565C0"));
                line(c, 47f, 45f, 47f, 52f, 1f, Color.WHITE);
                line(c, 53f, 45f, 53f, 52f, 1f, Color.WHITE);
            } else if (id == 35 || id == 36) {
                line(c, 50f, 45f, 50f, 67f, 1.2f, col("#FFF9C4"));
                flat(c, 37f, 58f, 44f, 62f, 1f, 0x22000000);
            } else if (id == 37) {
                ball(c, 45.5f, 50.8f, 2.4f, col("#212121"));
                ball(c, 54.5f, 50.8f, 2.4f, col("#212121"));
                ball(c, 50f, 57f, 6.6f, Color.WHITE);
                dot(c, 47.4f, 56.4f, 1.9f, col("#212121"));
                dot(c, 52.6f, 56.4f, 1.9f, col("#212121"));
                dot(c, 50f, 59.2f, 1f, col("#212121"));
            } else if (id == 38) {
                tri(c, 42f, 44f, 58f, 44f, 50f, 56f, col("#D32F2F"));
                tri(c, 46f, 44f, 54f, 44f, 50f, 50f, Color.WHITE);
                flat(c, 38f, 58f, 45f, 66f, 1.2f, col("#B58B58"));
            }
        }

        private void drawHair(Canvas c, int hid, int hairC, boolean hood) {
            if (hood) {
                rect(c, 36f, 20f, 64f, 26f, 2f, hairC);
                tri(c, 38f, 25f, 45f, 25f, 41.5f, 30f, hairC);
                tri(c, 47f, 25f, 53f, 25f, 50f, 29f, hairC);
                tri(c, 55f, 25f, 62f, 25f, 58.5f, 30f, hairC);
                return;
            }
            rect(c, 33f, 13f, 67f, 23f, 5f, hairC);
            rect(c, 33f, 13f, 37.5f, 30f, 2f, hairC);
            rect(c, 62.5f, 13f, 67f, 30f, 2f, hairC);
            if (hid == 41) {
                // belah samping: poni disisir dari garis belah (kanan) menyapu dahi ke kiri
                path.reset();
                path.moveTo(fx(33f), fy(9f));
                path.lineTo(fx(67f), fy(9f));
                path.lineTo(fx(67f), fy(22f));
                path.lineTo(fx(57f), fy(19f));
                path.lineTo(fx(46f), fy(24f));
                path.lineTo(fx(33f), fy(30f));
                path.close();
                p.setStyle(Paint.Style.FILL);
                p.setColor(hairC);
                c.drawPath(path, p);
            } else if (hid == 42) {
                // ikal: gumpalan bulat kecil di atas kepala
                for (int k = 0; k < 5; k++) {
                    ball(c, 37f + k * 6.5f, 12f, 4.2f, hairC);
                }
                ball(c, 34.5f, 22f, 3.5f, hairC);
                ball(c, 65.5f, 22f, 3.5f, hairC);
            } else if (hid == 43) {
                // pendek rapi: rapi, poni tipis rata
                rect(c, 33f, 11f, 67f, 25f, 5f, hairC);
            } else {
                rect(c, 36f, 22f, 64f, 26f, 1.5f, hairC);
            }
        }

        private void hoodRects(Canvas c, int color, float g) {
            flat(c, 30f - g, 8f - g, 70f + g, 23f + g, 6f + g, color);
            flat(c, 30f - g, 14f - g, 36.5f + g, 44f + g, 3f + g, color);
            flat(c, 63.5f - g, 14f - g, 70f + g, 44f + g, 3f + g, color);
        }

        // Tudung hewan 2D (kartu toko). Urutan: bagian di belakang tudung (telinga, tanduk, surai),
        // badan tudung, lalu detail di depan (belang, bercak, paruh, mata, dsb.).
        private void drawHood(Canvas c, int a) {
            int main = col(HOOD_MAIN[a]);
            int acc = col(HOOD_ACC[a]);
            int inner = col("#FFE0B2");
            int dk = shade(main, 0.62f);
            int blk = col("#212121");
            int wht = Color.WHITE;

            // ---------- 1. di belakang tudung ----------
            if (a == 0) {
                // singa: surai bergumpal dua lapis + telinga bulat
                ball(c, 33f, 10f, 5.8f, acc);
                ball(c, 67f, 10f, 5.8f, acc);
                dot(c, 33f, 10.3f, 2.8f, shade(acc, 1.35f));
                dot(c, 67f, 10.3f, 2.4f, shade(acc, 1.35f));
                float[] my = {16f, 24f, 32f, 40f};
                for (int i = 0; i < 4; i++) {
                    float mx = (i == 0 || i == 3) ? 29f : 27.5f;
                    ball(c, mx, my[i], 4.6f, acc);
                    ball(c, 100f - mx, my[i], 4.6f, acc);
                    dot(c, mx + 1.6f, my[i] + 0.5f, 2.3f, shade(acc, 1.22f));
                    dot(c, 100f - mx - 1.6f, my[i] + 0.5f, 2.3f, shade(acc, 1.22f));
                }
            } else if (a == 1) {
                // harimau: telinga bulat berisi krem, tepi gelap
                dot(c, 32f, 10f, 5.8f, blk);
                dot(c, 68f, 10f, 5.8f, blk);
                ball(c, 32f, 10f, 5.8f, main);
                ball(c, 68f, 10f, 5.8f, main);
                dot(c, 32f, 10.3f, 2.7f, col("#FFE9C7"));
                dot(c, 68f, 10.3f, 2.7f, col("#FFE9C7"));
            } else if (a == 2) {
                // panda: telinga hitam berisi abu
                ball(c, 32f, 9f, 6.3f, acc);
                ball(c, 68f, 9f, 6.3f, acc);
                dot(c, 32f, 9.4f, 2.4f, col("#5A5A5A"));
                dot(c, 68f, 9.4f, 2.4f, col("#5A5A5A"));
            } else if (a == 3) {
                // kapibara: telinga kecil bulat
                ball(c, 34f, 11f, 4.2f, main);
                ball(c, 66f, 11f, 4.2f, main);
                dot(c, 34f, 11.3f, 2f, acc);
                dot(c, 66f, 11.3f, 2f, acc);
            } else if (a == 4) {
                // katak: gundukan mata
                ball(c, 37f, 8f, 5.8f, main);
                ball(c, 63f, 8f, 5.8f, main);
            } else if (a == 5) {
                // rubah: telinga runcing, ujung cokelat tua, bagian dalam krem
                tri(c, 30.5f, 15.5f, 32.5f, -2f, 44f, 9f, dk);
                tri(c, 69.5f, 15.5f, 67.5f, -2f, 56f, 9f, dk);
                tri(c, 31f, 15f, 33f, -1f, 43f, 9f, main);
                tri(c, 69f, 15f, 67f, -1f, 57f, 9f, main);
                tri(c, 33f, 13f, 34f, 3f, 40.5f, 9f, inner);
                tri(c, 67f, 13f, 66f, 3f, 59.5f, 9f, inner);
                tri(c, 31.8f, 5f, 33f, -1f, 36.5f, 3.2f, col("#3E2723"));
                tri(c, 68.2f, 5f, 67f, -1f, 63.5f, 3.2f, col("#3E2723"));
            } else if (a == 8) {
                // kucing: telinga runcing berisi merah muda
                tri(c, 30.5f, 15.5f, 32.5f, -2f, 44f, 9f, dk);
                tri(c, 69.5f, 15.5f, 67.5f, -2f, 56f, 9f, dk);
                tri(c, 31f, 15f, 33f, -1f, 43f, 9f, main);
                tri(c, 69f, 15f, 67f, -1f, 57f, 9f, main);
                tri(c, 33.5f, 13f, 34.5f, 3f, 40.5f, 9f, col("#F4A6B0"));
                tri(c, 66.5f, 13f, 65.5f, 3f, 59.5f, 9f, col("#F4A6B0"));
            } else if (a == 6) {
                // kelinci: telinga panjang, bagian dalam merah muda
                rect(c, 35f, -4f, 43f, 12f, 4f, main);
                rect(c, 57f, -4f, 65f, 12f, 4f, main);
                flat(c, 37f, -1f, 41f, 10f, 2f, acc);
                flat(c, 59f, -1f, 63f, 10f, 2f, acc);
                flat(c, 37.6f, 0f, 39f, 5f, 0.7f, shade(acc, 1.08f));
                flat(c, 61f, 0f, 62.4f, 5f, 0.7f, shade(acc, 1.08f));
            } else if (a == 7) {
                // anjing: telinga lipat
                rect(c, 23f, 14f, 33f, 38f, 4f, acc);
                rect(c, 67f, 14f, 77f, 38f, 4f, acc);
                flat(c, 25f, 17f, 28f, 33f, 1.5f, shade(acc, 1.3f));
                flat(c, 72f, 17f, 75f, 33f, 1.5f, shade(acc, 1.3f));
            } else if (a == 9) {
                // unicorn: telinga + cula emas berulir
                tri(c, 33f, 12f, 35f, 3f, 41f, 9f, main);
                tri(c, 67f, 12f, 65f, 3f, 59f, 9f, main);
                tri(c, 35f, 11f, 36f, 5f, 39f, 9f, col("#F8BBD0"));
                tri(c, 65f, 11f, 64f, 5f, 61f, 9f, col("#F8BBD0"));
                tri(c, 46f, 9f, 50f, -5f, 54f, 9f, col("#FFD54F"));
                line(c, 47.2f, 6f, 52.6f, 4.6f, 0.9f, col("#FFA000"));
                line(c, 48.2f, 2.5f, 51.8f, 1.4f, 0.8f, col("#FFA000"));
                line(c, 49.2f, -0.8f, 50.8f, -1.2f, 0.7f, col("#FFA000"));
            } else if (a == 11) {
                // ayam: jengger merah di puncak
                ball(c, 43f, 6.5f, 3.6f, acc);
                ball(c, 50f, 3.8f, 4.6f, acc);
                ball(c, 57f, 6.5f, 3.6f, acc);
            } else if (a == 13) {
                // domba: gumpalan wol di atas dan sisi + telinga krem
                ball(c, 36f, 8.5f, 5.4f, main);
                ball(c, 45f, 5.5f, 5.8f, shade(main, 0.95f));
                ball(c, 55f, 5.5f, 5.8f, shade(main, 0.95f));
                ball(c, 64f, 8.5f, 5.4f, main);
                for (int i = 0; i < 4; i++) {
                    ball(c, 28.5f, 17f + i * 8f, 4.6f, (i & 1) == 0 ? main : shade(main, 0.95f));
                    ball(c, 71.5f, 17f + i * 8f, 4.6f, (i & 1) == 0 ? main : shade(main, 0.95f));
                }
                ball(c, 25f, 30f, 3.4f, acc);
                ball(c, 75f, 30f, 3.4f, acc);
            } else if (a == 14) {
                // buaya: gundukan mata
                ball(c, 37f, 8f, 5.6f, main);
                ball(c, 63f, 8f, 5.6f, main);
            } else if (a == 15) {
                // gajah: telinga lebar berisi merah muda
                ball(c, 25.5f, 27f, 8.8f, main);
                ball(c, 74.5f, 27f, 8.8f, main);
                dot(c, 27.2f, 27f, 5.6f, acc);
                dot(c, 72.8f, 27f, 5.6f, acc);
                line(c, 26f, 21f, 28f, 33f, 0.6f, shade(acc, 0.85f));
                line(c, 74f, 21f, 72f, 33f, 0.6f, shade(acc, 0.85f));
            } else if (a == 16) {
                // zebra: telinga tegak putih berisi hitam
                tri(c, 30.5f, 15.5f, 32.5f, -2f, 44f, 9f, dk);
                tri(c, 69.5f, 15.5f, 67.5f, -2f, 56f, 9f, dk);
                tri(c, 31f, 15f, 33f, -1f, 43f, 9f, main);
                tri(c, 69f, 15f, 67f, -1f, 57f, 9f, main);
                tri(c, 33f, 13f, 34f, 3f, 40.5f, 9f, acc);
                tri(c, 67f, 13f, 66f, 3f, 59.5f, 9f, acc);
            } else if (a == 21) {
                // kuda: surai di sisi dan puncak + telinga tegak berisi krem
                rect(c, 26.5f, 14f, 34f, 46f, 4f, acc);
                rect(c, 66f, 14f, 73.5f, 46f, 4f, acc);
                ball(c, 41f, 7f, 3.8f, acc);
                ball(c, 50f, 5f, 4.2f, acc);
                ball(c, 59f, 7f, 3.8f, acc);
                tri(c, 32f, 15f, 35f, -3f, 43f, 9f, main);
                tri(c, 68f, 15f, 65f, -3f, 57f, 9f, main);
                tri(c, 34f, 13f, 36f, 1f, 41f, 9f, col("#EBCBA6"));
                tri(c, 66f, 13f, 64f, 1f, 59f, 9f, col("#EBCBA6"));
            } else if (a == 22) {
                // sapi: tanduk kecil krem + telinga mendatar berisi merah muda
                tri(c, 34f, 11f, 30f, 1.5f, 40f, 9f, col("#F3E6C8"));
                tri(c, 66f, 11f, 70f, 1.5f, 60f, 9f, col("#F3E6C8"));
                rect(c, 21f, 20f, 33f, 27f, 3.5f, main);
                rect(c, 67f, 20f, 79f, 27f, 3.5f, main);
                flat(c, 23f, 21.5f, 31f, 25.5f, 2f, col("#F8BBD0"));
                flat(c, 69f, 21.5f, 77f, 25.5f, 2f, col("#F8BBD0"));
            } else if (a == 24) {
                // beruang: telinga bulat besar berisi krem
                ball(c, 32f, 9.5f, 6.2f, main);
                ball(c, 68f, 9.5f, 6.2f, main);
                dot(c, 32f, 9.8f, 3.3f, acc);
                dot(c, 68f, 9.8f, 3.3f, acc);
            } else if (a == 23) {
                // kura-kura: sirip depan di sisi
                rect(c, 21f, 32f, 32f, 44f, 5f, main);
                rect(c, 68f, 32f, 79f, 44f, 5f, main);
                flat(c, 23f, 35f, 29.5f, 41.5f, 3f, shade(main, 1.18f));
                flat(c, 70.5f, 35f, 77f, 41.5f, 3f, shade(main, 1.18f));
            }

            // ---------- 2. badan tudung ----------
            hoodRects(c, 0x55000000, 0.5f);
            hoodRects(c, main, 0f);

            // ---------- 3. detail di depan ----------
            if (a == 0) {
                // singa: jambul dahi
                tri(c, 39f, 9f, 42.5f, 2.5f, 46f, 9f, acc);
                tri(c, 45f, 9f, 50f, 0f, 55f, 9f, acc);
                tri(c, 54f, 9f, 57.5f, 2.5f, 61f, 9f, acc);
                line(c, 31.3f, 18f, 34.2f, 22f, 0.6f, shade(acc, 0.8f));
                line(c, 68.7f, 18f, 65.8f, 22f, 0.6f, shade(acc, 0.8f));
            } else if (a == 1) {
                // harimau (belang tebal di dahi dan pipi, pipi putih)
                int sc = acc;
                flat(c, 42f, 8.6f, 44.6f, 12.5f, 0.9f, sc);
                flat(c, 48.8f, 8.6f, 51.2f, 12.5f, 0.9f, sc);
                flat(c, 55.4f, 8.6f, 58f, 12.5f, 0.9f, sc);
                flat(c, 30.5f, 25f, 36.2f, 27.4f, 0.7f, sc);
                flat(c, 30.5f, 31f, 36.2f, 33.4f, 0.7f, sc);
                flat(c, 30.5f, 37f, 36.2f, 39.4f, 0.7f, sc);
                flat(c, 63.8f, 25f, 69.5f, 27.4f, 0.7f, sc);
                flat(c, 63.8f, 31f, 69.5f, 33.4f, 0.7f, sc);
                flat(c, 63.8f, 37f, 69.5f, 39.4f, 0.7f, sc);
                tri(c, 30f, 44f, 30f, 38f, 36.5f, 44f, wht);
                tri(c, 70f, 44f, 70f, 38f, 63.5f, 44f, wht);
            } else if (a == 2) {
                // panda: bercak mata miring + pipi
                flat(c, 30.8f, 24f, 36.2f, 36f, 2.6f, acc);
                flat(c, 63.8f, 24f, 69.2f, 36f, 2.6f, acc);
                dot(c, 33.5f, 40f, 1.5f, col("#F8C9D0"));
                dot(c, 66.5f, 40f, 1.5f, col("#F8C9D0"));
            } else if (a == 3) {
                // kapibara: bulu pipi cokelat muda
                tri(c, 30f, 44f, 30f, 36f, 36.5f, 44f, acc);
                tri(c, 70f, 44f, 70f, 36f, 63.5f, 44f, acc);
            } else if (a == 4) {
                // katak: mata menonjol, lubang hidung, bintik kulit
                dot(c, 37f, 8f, 3.9f, wht);
                dot(c, 63f, 8f, 3.9f, wht);
                dot(c, 37.5f, 8.3f, 1.9f, blk);
                dot(c, 62.5f, 8.3f, 1.9f, blk);
                dot(c, 38.1f, 7.4f, 0.6f, wht);
                dot(c, 62.9f, 7.4f, 0.6f, wht);
                dot(c, 48.6f, 13f, 0.8f, dk);
                dot(c, 51.4f, 13f, 0.8f, dk);
                dot(c, 42f, 17f, 1.5f, shade(main, 0.78f));
                dot(c, 58f, 18f, 1.5f, shade(main, 0.78f));
                dot(c, 45f, 20f, 1.1f, shade(main, 0.78f));
                dot(c, 55.5f, 13.5f, 1.2f, shade(main, 0.78f));
            } else if (a == 5) {
                // rubah: pipi putih + belang putih di dahi
                tri(c, 30f, 44f, 30f, 34f, 36.5f, 44f, wht);
                tri(c, 70f, 44f, 70f, 34f, 63.5f, 44f, wht);
            } else if (a == 6) {
                // kelinci: bulu halus di dahi + pipi pink
                dot(c, 46f, 11f, 2.2f, wht);
                dot(c, 50f, 9.5f, 2.4f, wht);
                dot(c, 54f, 11f, 2.2f, wht);
                dot(c, 33.3f, 39.5f, 1.6f, col("#F8BBD0"));
                dot(c, 66.7f, 39.5f, 1.6f, col("#F8BBD0"));
            } else if (a == 7) {
                // anjing: garis putih di dahi + bercak mata kiri
                flat(c, 47f, 8.6f, 53f, 12.5f, 2f, col("#F5E6D3"));
                flat(c, 30.6f, 14f, 36f, 31f, 2.6f, acc);
                dot(c, 40f, 17f, 0.9f, shade(main, 0.8f));
                dot(c, 60f, 15f, 0.9f, shade(main, 0.8f));
            } else if (a == 8) {
                // kucing: belang M di dahi + kumis di pipi
                int sc = acc;
                flat(c, 44f, 8.6f, 46f, 12.5f, 0.8f, sc);
                flat(c, 49f, 8.6f, 51f, 12.5f, 0.8f, sc);
                flat(c, 54f, 8.6f, 56f, 12.5f, 0.8f, sc);
                flat(c, 30.5f, 26f, 35f, 27.6f, 0.6f, sc);
                flat(c, 65f, 26f, 69.5f, 27.6f, 0.6f, sc);
                line(c, 30.8f, 36f, 36.3f, 35.2f, 0.5f, wht);
                line(c, 30.8f, 39f, 36.3f, 38.4f, 0.5f, wht);
                line(c, 69.2f, 36f, 63.7f, 35.2f, 0.5f, wht);
                line(c, 69.2f, 39f, 63.7f, 38.4f, 0.5f, wht);
            } else if (a == 9) {
                // unicorn: surai pelangi di sisi kanan + kilau bintang
                rect(c, 65.5f, 10f, 72f, 38f, 3f, acc);
                flat(c, 67f, 14f, 71f, 32f, 2f, col("#B39DDB"));
                flat(c, 66.6f, 26f, 70.6f, 42f, 2f, col("#80DEEA"));
                dot(c, 40f, 16f, 1.4f, col("#FFD54F"));
                dot(c, 36.5f, 12f, 0.9f, col("#FFD54F"));
                dot(c, 44f, 12.5f, 0.8f, col("#FFD54F"));
            } else if (a == 10) {
                // penguin: paruh, sayap putih, alis putih, jambul kuning
                tri(c, 46f, 10f, 54f, 10f, 50f, 16f, col("#FFB300"));
                flat(c, 31f, 27f, 36.2f, 41f, 2f, wht);
                flat(c, 63.8f, 27f, 69f, 41f, 2f, wht);
                flat(c, 37f, 17.5f, 45f, 21f, 1.8f, wht);
                flat(c, 55f, 17.5f, 63f, 21f, 1.8f, wht);
                tri(c, 36f, 17f, 28f, 7f, 34f, 10f, col("#FFCA28"));
                tri(c, 64f, 17f, 72f, 7f, 66f, 10f, col("#FFCA28"));
            } else if (a == 11) {
                // ayam: paruh kuning, pial merah, mata, pipi merah muda
                tri(c, 45.5f, 10.5f, 54.5f, 10.5f, 50f, 17f, col("#FFC107"));
                dot(c, 50f, 18.6f, 1.7f, acc);
                dot(c, 40f, 12f, 1.3f, col("#2B1B12"));
                dot(c, 60f, 12f, 1.3f, col("#2B1B12"));
                dot(c, 33.3f, 39.5f, 1.6f, col("#F8BBD0"));
                dot(c, 66.7f, 39.5f, 1.6f, col("#F8BBD0"));
            } else if (a == 13) {
                // domba: ikal wol di dahi dan bulu di sisi
                ball(c, 41f, 10f, 3.2f, main);
                ball(c, 46.5f, 8.6f, 3.6f, shade(main, 0.95f));
                ball(c, 53.5f, 8.6f, 3.6f, shade(main, 0.95f));
                ball(c, 59f, 10f, 3.2f, main);
                for (int i = 0; i < 3; i++) {
                    dot(c, 33.3f, 24f + i * 8f, 1.5f, shade(main, 0.9f));
                    dot(c, 66.7f, 24f + i * 8f, 1.5f, shade(main, 0.9f));
                }
            } else if (a == 14) {
                // buaya: mata menonjol, lubang hidung, gigi di depan, tulang punggung kuning
                dot(c, 37f, 8f, 3.9f, col("#F4E36B"));
                dot(c, 63f, 8f, 3.9f, col("#F4E36B"));
                dot(c, 37.5f, 8.3f, 1.9f, blk);
                dot(c, 62.5f, 8.3f, 1.9f, blk);
                dot(c, 38.1f, 7.4f, 0.6f, wht);
                dot(c, 62.9f, 7.4f, 0.6f, wht);
                dot(c, 47.6f, 13.5f, 0.8f, dk);
                dot(c, 52.4f, 13.5f, 0.8f, dk);
                for (int q = 1; q <= 3; q++) {
                    float sx = 38f + q * 6f;
                    tri(c, sx - 2f, 9.2f, sx, 4.5f, sx + 2f, 9.2f, acc);
                }
                for (int q = 0; q < 5; q++) {
                    float tx = 36f + q * 5.6f;
                    tri(c, tx, 23f, tx + 2.8f, 27.5f, tx + 5.6f, 23f, wht);
                }
            } else if (a == 15) {
                // gajah: tiga garis kerut horizontal di dahi (bukan belalai tegak)
                line(c, 40.5f, 8.2f, 59.5f, 8.2f, 0.6f, shade(main, 0.74f));
                line(c, 39.5f, 12.2f, 60.5f, 12.2f, 0.6f, shade(main, 0.74f));
                line(c, 40.5f, 16.2f, 59.5f, 16.2f, 0.6f, shade(main, 0.74f));
            } else if (a == 16) {
                // zebra: garis hitam di dahi dan sisi
                flat(c, 46f, 8.6f, 48f, 12f, 0.8f, acc);
                flat(c, 52f, 8.6f, 54f, 12f, 0.8f, acc);
                flat(c, 41f, 8.6f, 43f, 11f, 0.8f, acc);
                flat(c, 57f, 8.6f, 59f, 11f, 0.8f, acc);
                for (int i = 0; i < 4; i++) {
                    float yy = 17f + i * 7f;
                    flat(c, 30.5f, yy, 36.2f, yy + 2f, 0.7f, acc);
                    flat(c, 63.8f, yy, 69.5f, yy + 2f, 0.7f, acc);
                }
            } else if (a == 21) {
                // kuda: jambul dahi cokelat tua
                tri(c, 42f, 9f, 45f, 17.5f, 48f, 9f, acc);
                tri(c, 47f, 9f, 50f, 19.5f, 53f, 9f, acc);
                tri(c, 52f, 9f, 55f, 17.5f, 58f, 9f, acc);
            } else if (a == 22) {
                // sapi: bercak hitam
                flat(c, 32.5f, 8.6f, 38f, 13.5f, 2.2f, acc);
                flat(c, 62f, 8.6f, 67.5f, 12.5f, 2f, acc);
                flat(c, 30.4f, 24f, 36.2f, 33f, 2.6f, acc);
                flat(c, 63.8f, 34f, 69.6f, 43f, 2.6f, acc);
            } else if (a == 23) {
                // kura-kura: perisai sisik di atas, garis kuning di sisi, bintik
                flat(c, 42.5f, 10f, 57.5f, 21f, 3.6f, acc);
                flat(c, 35.2f, 11.5f, 41.5f, 20f, 2.6f, shade(acc, 0.88f));
                flat(c, 58.5f, 11.5f, 64.8f, 20f, 2.6f, shade(acc, 0.88f));
                line(c, 46f, 12f, 54f, 12f, 0.5f, shade(acc, 0.65f));
                line(c, 46f, 15.5f, 54f, 15.5f, 0.5f, shade(acc, 0.65f));
                line(c, 46f, 19f, 54f, 19f, 0.5f, shade(acc, 0.65f));
                line(c, 33.2f, 25f, 33.2f, 41f, 0.9f, col("#F5E6A8"));
                line(c, 66.8f, 25f, 66.8f, 41f, 0.9f, col("#F5E6A8"));
                dot(c, 26.5f, 38f, 0.7f, col("#F5E6A8"));
                dot(c, 73.5f, 38f, 0.7f, col("#F5E6A8"));
            }
            face2d(c, a);
        }

        // Wajah hewan di dahi tudung (kartu 2D): moncong, hidung, mata, kumis.
        private void face2d(Canvas c, int a) {
            int muz;
            int nose = col("#3A2418");
            boolean whisk = true;
            switch (a) {
                case 0: muz = col("#F7E6C4"); nose = col("#5D3A1E"); break;
                case 1: muz = col("#FFF3DF"); break;
                case 2: muz = Color.WHITE; whisk = false; break;
                case 5: muz = col("#FFF1E0"); break;
                case 6: muz = Color.WHITE; nose = col("#F48FB1"); break;
                case 7: muz = col("#F5E6D3"); whisk = false; break;
                case 8: muz = col("#FFF1E0"); nose = col("#F48FB1"); break;
                case 3: muz = col("#D8B58C"); whisk = false; break;
                case 13: muz = col("#F4E8DA"); nose = col("#C98B8B"); whisk = false; break;
                case 16: muz = col("#E6E6E6"); whisk = false; break;
                case 21: muz = col("#F3E1C8"); whisk = false; break;
                case 22: muz = col("#FFC9D4"); nose = col("#D6798F"); whisk = false; break;
                case 24: muz = col("#E2C6A0"); whisk = false; break;
                default: return;
            }
            int dk = col("#2B1B12");
            if (a == 2) {
                flat(c, 36.5f, 9.5f, 43.5f, 17f, 3f, dk);
                flat(c, 56.5f, 9.5f, 63.5f, 17f, 3f, dk);
                dot(c, 41.2f, 12.5f, 1.2f, Color.WHITE);
                dot(c, 58.8f, 12.5f, 1.2f, Color.WHITE);
            } else {
                dot(c, 40f, 12f, 1.3f, dk);
                dot(c, 60f, 12f, 1.3f, dk);
            }
            flat(c, 42.5f, 13.2f, 57.5f, 21.5f, 4.5f, muz);
            tri(c, 47.6f, 15f, 52.4f, 15f, 50f, 18f, nose);
            line(c, 50f, 18f, 50f, 19.6f, 0.5f, nose);
            if (whisk) {
                line(c, 43f, 17.5f, 35.5f, 16f, 0.5f, dk);
                line(c, 43f, 19.2f, 35.5f, 20.6f, 0.5f, dk);
                line(c, 57f, 17.5f, 64.5f, 16f, 0.5f, dk);
                line(c, 57f, 19.2f, 64.5f, 20.6f, 0.5f, dk);
            }
        }

        private void drawMask(Canvas c) {
            path.reset();
            path.moveTo(fx(35f), fy(28f));
            path.lineTo(fx(65f), fy(28f));
            path.lineTo(fx(63f), fy(36f));
            path.lineTo(fx(55f), fy(34f));
            path.lineTo(fx(50f), fy(36f));
            path.lineTo(fx(45f), fy(34f));
            path.lineTo(fx(37f), fy(36f));
            path.close();
            p.setStyle(Paint.Style.FILL);
            p.setColor(col("#7E57C2"));
            c.drawPath(path, p);
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(1f * u);
            p.setColor(col("#FFC107"));
            c.drawPath(path, p);
            p.setStyle(Paint.Style.FILL);
        }

        private void heart(Canvas c, float cx, float cy, float r, int color) {
            dot(c, cx - r * 0.5f, cy - r * 0.3f, r * 0.58f, color);
            dot(c, cx + r * 0.5f, cy - r * 0.3f, r * 0.58f, color);
            tri(c, cx - r * 1.04f, cy - r * 0.05f, cx + r * 1.04f, cy - r * 0.05f, cx, cy + r * 1.1f, color);
        }

        private void drawFace(Canvas c, int id) {
            if (id == 8) {
                int fr = col("#263238");
                for (int k = 0; k < 2; k++) {
                    float ex = k == 0 ? 43.6f : 56.4f;
                    dot(c, ex, 32.7f, 5.2f, 0x33BBDEFB);
                    p.setStyle(Paint.Style.STROKE);
                    p.setStrokeWidth(1f * u);
                    p.setColor(fr);
                    c.drawCircle(fx(ex), fy(32.7f), 5.2f * u, p);
                    p.setStyle(Paint.Style.FILL);
                }
                line(c, 48.8f, 32.2f, 51.2f, 32.2f, 1f, fr);
                line(c, 38.4f, 32f, 33.5f, 31.5f, 1f, fr);
                line(c, 61.6f, 32f, 66.5f, 31.5f, 1f, fr);
            } else if (id == 9) {
                heart(c, 38.5f, 38f, 3.2f, col("#F06292"));
                heart(c, 61.5f, 38f, 3.2f, col("#F06292"));
            } else if (id == 10) {
                rect(c, 37f, 29f, 48.6f, 36f, 2f, col("#111111"));
                rect(c, 51.4f, 29f, 63f, 36f, 2f, col("#111111"));
                flat(c, 48.6f, 31f, 51.4f, 32.6f, 0.5f, col("#111111"));
                flat(c, 38.5f, 30f, 41.5f, 31.4f, 0.6f, 0x88FFFFFF);
                flat(c, 52.9f, 30f, 55.9f, 31.4f, 0.6f, 0x88FFFFFF);
            }
        }

        private void drawHat(Canvas c, int id, float ht) {
            if (id == 0) {
                rect(c, 40f, ht - 9f, 60f, ht, 4f, col("#F6D77A"));
                flat(c, 40.5f, ht - 4f, 59.5f, ht, 0f, col("#E53935"));
                rect(c, 33f, ht - 1f, 67f, ht + 3f, 2f, col("#F6D77A"));
            } else if (id == 1) {
                tri(c, 60f, ht + 1f, 51f, ht - 5f, 51f, ht + 7f, col("#F06292"));
                tri(c, 60f, ht + 1f, 69f, ht - 5f, 69f, ht + 7f, col("#F06292"));
                ball(c, 60f, ht + 1f, 2.8f, col("#C2185B"));
            } else if (id == 2) {
                for (int k = 0; k < 5; k++) {
                    double a = -Math.PI / 2 + k * 2 * Math.PI / 5;
                    dot(c, 58f + (float) Math.cos(a) * 3.2f, ht + 1f + (float) Math.sin(a) * 3.2f, 2.8f, col("#F8BBD0"));
                }
                dot(c, 58f, ht + 1f, 1.7f, col("#FFC107"));
            } else if (id == 3) {
                rect(c, 40f, ht - 4f, 60f, ht + 2f, 2f, col("#212121"));
                path.reset();
                path.moveTo(fx(30f), fy(ht - 4f));
                path.lineTo(fx(50f), fy(ht - 10f));
                path.lineTo(fx(70f), fy(ht - 4f));
                path.lineTo(fx(50f), fy(ht + 0.5f));
                path.close();
                p.setStyle(Paint.Style.FILL);
                p.setColor(col("#263238"));
                c.drawPath(path, p);
                line(c, 50f, ht - 4.5f, 66f, ht - 3f, 1.1f, col("#FFC107"));
                line(c, 66f, ht - 3f, 66f, ht + 5f, 1.1f, col("#FFC107"));
                dot(c, 66f, ht + 5.5f, 1.6f, col("#FFC107"));
            } else if (id == 4) {
                rect(c, 36f, ht - 8f, 64f, ht + 2f, 7f, col("#E53935"));
                rect(c, 33.5f, ht, 66.5f, ht + 3.2f, 1.5f, col("#C62828"));
                flat(c, 48.4f, ht - 6f, 51.6f, ht - 0.5f, 0.4f, Color.WHITE);
                flat(c, 46f, ht - 4.3f, 54f, ht - 2.7f, 0.4f, Color.WHITE);
            } else if (id == 5) {
                rect(c, 40f, ht - 12f, 60f, ht, 2f, col("#212121"));
                flat(c, 40.5f, ht - 5f, 59.5f, ht - 1.5f, 0f, col("#C62828"));
                rect(c, 35f, ht - 1f, 65f, ht + 3f, 1.5f, col("#212121"));
            } else if (id == 6) {
                path.reset();
                for (int k = 0; k < 10; k++) {
                    double a = -Math.PI / 2 + k * Math.PI / 5;
                    float r = k % 2 == 0 ? 8.5f : 3.6f;
                    float px = fx(50f) + (float) Math.cos(a) * r * u;
                    float py = fy(ht - 5f) + (float) Math.sin(a) * r * u;
                    if (k == 0) {
                        path.moveTo(px, py);
                    } else {
                        path.lineTo(px, py);
                    }
                }
                path.close();
                p.setStyle(Paint.Style.FILL);
                p.setColor(col("#FFC107"));
                c.drawPath(path, p);
                outline(c);
                c.drawPath(path, p);
                p.setStyle(Paint.Style.FILL);
            } else if (id == 7) {
                path.reset();
                path.moveTo(fx(38f), fy(ht + 2.5f));
                path.lineTo(fx(38f), fy(ht - 8f));
                path.lineTo(fx(44f), fy(ht - 3f));
                path.lineTo(fx(50f), fy(ht - 10f));
                path.lineTo(fx(56f), fy(ht - 3f));
                path.lineTo(fx(62f), fy(ht - 8f));
                path.lineTo(fx(62f), fy(ht + 2.5f));
                path.close();
                p.setStyle(Paint.Style.FILL);
                p.setColor(col("#FFC107"));
                c.drawPath(path, p);
                outline(c);
                c.drawPath(path, p);
                p.setStyle(Paint.Style.FILL);
                dot(c, 50f, ht - 1f, 1.7f, col("#D32F2F"));
            }
        }

        private void drawHeld(Canvas c, int id) {
            if (id == 12) {
                ball(c, 80f, 60f, 6f, Color.WHITE);
                dot(c, 80f, 60f, 2.2f, Color.BLACK);
                dot(c, 76.6f, 56.8f, 1.1f, Color.BLACK);
                dot(c, 83.4f, 56.8f, 1.1f, Color.BLACK);
                dot(c, 77f, 63.8f, 1.1f, Color.BLACK);
                dot(c, 83f, 63.8f, 1.1f, Color.BLACK);
            } else if (id == 13) {
                ball(c, 80f, 60f, 6.2f, col("#FF8F00"));
                line(c, 73.8f, 60f, 86.2f, 60f, 0.8f, col("#5D2E00"));
                line(c, 80f, 53.8f, 80f, 66.2f, 0.8f, col("#5D2E00"));
            } else if (id == 14) {
                rf.set(fx(73f), fy(53f), fx(89f), fy(66f));
                p.setStyle(Paint.Style.FILL);
                p.setColor(col("#D7A86E"));
                c.drawOval(rf, p);
                outline(c);
                c.drawOval(rf, p);
                p.setStyle(Paint.Style.FILL);
                dot(c, 78f, 58f, 1.6f, col("#E53935"));
                dot(c, 82.5f, 56.5f, 1.6f, col("#1E88E5"));
                dot(c, 85.5f, 60.5f, 1.6f, col("#43A047"));
                dot(c, 80f, 62f, 1.6f, col("#FDD835"));
            } else if (id == 15) {
                line(c, 72f, 66f, 85f, 53f, 4.6f, col("#37474F"));
                line(c, 76f, 69f, 89f, 56f, 4.6f, col("#37474F"));
                dot(c, 85.8f, 52.4f, 2.5f, col("#90CAF9"));
                dot(c, 89.8f, 55.4f, 2.5f, col("#90CAF9"));
            } else if (id == 16) {
                rect(c, 77.6f, 43f, 80.4f, 61f, 0.8f, col("#5D4037"));
                rect(c, 76.8f, 40f, 81.2f, 45f, 1f, col("#3E2723"));
                ball(c, 79f, 66f, 6.2f, col("#A1572B"));
                dot(c, 79f, 66f, 1.9f, col("#3E2723"));
            } else if (id == 17) {
                rect(c, 74.8f, 34f, 78.4f, 61f, 0.8f, col("#CFD8DC"));
                rect(c, 71.6f, 60.6f, 81.6f, 63.4f, 1f, col("#FFC107"));
                rect(c, 75.3f, 63f, 77.9f, 70f, 1f, col("#6D4C41"));
            } else if (id == 18) {
                tri(c, 83.5f, 60f, 83.5f, 69f, 88.5f, 69f, col("#D32F2F"));
                tri(c, 75.5f, 60f, 75.5f, 69f, 70.5f, 69f, col("#D32F2F"));
                tri(c, 77f, 66f, 82f, 66f, 79.5f, 74f, col("#FFB300"));
                rect(c, 75.5f, 44f, 83.5f, 67f, 4f, col("#ECEFF1"));
                tri(c, 75.5f, 48f, 83.5f, 48f, 79.5f, 38f, col("#E53935"));
                dot(c, 79.5f, 55f, 2.4f, col("#4FC3F7"));
            }
        }
    }

    private AvatarView avatarView(int pl) {
        return avatarView(pl, 0);
    }

    private AvatarView avatarView(int pl, int mode) {
        int[] items = new int[7];
        for (int k = 0; k < 7; k++) {
            int e = eq(pl, k);
            items[k] = (e >= 0 && e < ITEMS.length && itSlot(e) == k && owns(e) && itFits(e, genderOf[pl])) ? e : -1;
        }
        int a = avatar[pl];
        AvatarView av3 = new AvatarView(this, isHood(a) ? a : -1, items, mode, true);
        av3.gender = genderOf[pl];
        av3.tasOff = tasOff(pl);
        return av3;
    }

    private AvatarView previewView(int animalIdx, int slot, int id, int mode) {
        int[] items = {-1, -1, -1, -1, -1, -1, -1};
        if (slot >= 0) {
            items[slot] = id;
        }
        return new AvatarView(this, animalIdx, items, mode, false);
    }

    // Modern cartoon-3D shop card. Existing item IDs, prices and ownership are unchanged.
    private View shopCell(final int id) {
        final boolean own = owns(id);
        final boolean on = eq(shopPl, itSlot(id)) == id;
        // This catalog displays only supported 3D equipment. Legacy 2D items
        // are kept in ITEMS for compatibility but filtered from the visible shop.
        final boolean pending3D = false;
        final int rar = itRar(id);
        final int rc = RAR_COLOR[rar];
        LinearLayout cell = new LinearLayout(this);
        cell.setOrientation(LinearLayout.VERTICAL);
        cell.setGravity(Gravity.CENTER_HORIZONTAL);
        cell.setPadding(dp(5), dp(6), dp(5), dp(6));

        GradientDrawable bg = new GradientDrawable(GradientDrawable.Orientation.TL_BR,
                on ? new int[]{Color.rgb(229, 250, 238), Color.WHITE}
                   : new int[]{Color.WHITE, Color.rgb(246, 246, 255)});
        bg.setCornerRadius(dp(20));
        bg.setStroke(dp(on ? 3 : 1), on ? GREEN : (rar > 0 ? rc : Color.rgb(222, 226, 240)));
        cell.setBackground(bg);
        cell.setElevation(dp(on ? 4 : 2));

        TextView rarity = text(RAR_NAME[rar].toUpperCase(Locale.ROOT), 9, rc, true);
        rarity.setGravity(Gravity.CENTER);
        rarity.setPadding(dp(7), dp(2), dp(7), dp(2));
        rarity.setBackground(box(Color.rgb(243, 245, 252), 10));
        // Rarity stays in accessibility description; omit its extra visual row.

        FrameLayout display = new FrameLayout(this);
        GradientDrawable stage = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{Color.rgb(237, 240, 255), Color.rgb(249, 251, 255)});
        stage.setCornerRadius(dp(16));
        display.setBackground(stage);
        if (itSlot(id) == 4) {
            View sw = new View(this);
            sw.setBackground(bgDrawable(id, dp(14)));
            FrameLayout.LayoutParams sp = new FrameLayout.LayoutParams(dp(58), dp(58), Gravity.CENTER);
            display.addView(sw, sp);
        } else if (itSlot(id) == 3) {
            TextView emoji = text(itIcon(id), 38, DARK, false);
            emoji.setGravity(Gravity.CENTER);
            display.addView(emoji, new FrameLayout.LayoutParams(MATCH, MATCH));
        } else {
            int sl = itSlot(id);
            AvatarView preview = previewView(-1, sl, id, (sl == 2 || sl == 5) ? 2 : 1);
            preview.gender = itGender(id) < 0 ? genderOf[shopPl] : itGender(id);
            display.addView(preview, new FrameLayout.LayoutParams(MATCH, MATCH));
        }
        cell.addView(display, new LinearLayout.LayoutParams(MATCH, dp(70)));

        TextView itemName = text(itName(id), 12, Color.rgb(48, 42, 72), true);
        itemName.setGravity(Gravity.CENTER);
        itemName.setMaxLines(2);
        itemName.setEllipsize(TextUtils.TruncateAt.END);
        cell.addView(itemName, lp(dp(30), 3));

        String label = pending3D ? "3D SEGERA" : on ? "✓ DIPAKAI" : own ? "PAKAI" : "🪙 " + itPrice(id);
        int actionColor = pending3D ? Color.rgb(135, 142, 160) : on ? GREEN : own ? BLUE : coins() >= itPrice(id) ? ORANGE : SILVER;
        TextView action = text(label, 11, Color.WHITE, true);
        action.setGravity(Gravity.CENTER);
        action.setMinHeight(dp(32));
        action.setBackground(box(actionColor, 12));
        cell.addView(action, new LinearLayout.LayoutParams(MATCH, dp(30)));
        cell.setContentDescription(itName(id) + ", " + RAR_NAME[rar] + ", " +
                (on ? "sedang dipakai" : own ? "sudah dimiliki, ketuk untuk memakai" :
                        "harga " + itPrice(id) + " koin"));
        cell.setOnClickListener(v -> {
            if (pending3D) {
                Toast.makeText(this, "Model 3D belum tersedia untuk " + itName(id), Toast.LENGTH_SHORT).show();
                return;
            }
            tapItem(id);
        });
        return cell;
    }

    private void tapItem(int id) {
        int slot = itSlot(id);
        if (owns(id)) {
            boolean on = eq(shopPl, slot) == id;
            prefs.edit().putInt("eq" + shopPl + "_" + slot, on ? -1 : id).apply();
            sfx(SFX_OK);
            showToko();
            return;
        }
        int price = itPrice(id);
        int have = coins();
        if (have < price) {
            sfx(SFX_WRONG);
            Toast.makeText(this, "Koin kurang " + (price - have) + " lagi. Ayo main dulu!",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        prefs.edit().putInt("coins", have - price).putBoolean("own" + id, true)
                .putInt("eq" + shopPl + "_" + slot, id).apply();
        sfx(SFX_WIN);
        Toast.makeText(this, itName(id) + " dibeli dan dipakai!", Toast.LENGTH_SHORT).show();
        showToko();
    }

    private void showToko() {
        handler.removeCallbacksAndMessages(null);
        setScreen(20);
        nameFields = null;
        loadPrefs();
        if (shopPl >= np) {
            shopPl = 0;
        }

        nagaHide = true;
        LinearLayout c = column();
        // Spanduk ringkas
        LinearLayout head = new LinearLayout(this);
        head.setOrientation(LinearLayout.HORIZONTAL);
        head.setGravity(Gravity.CENTER_VERTICAL);
        head.setPadding(dp(14), dp(7), dp(10), dp(7));
        GradientDrawable headBg = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{Color.rgb(126, 94, 232), Color.rgb(94, 62, 198)});
        headBg.setCornerRadius(dp(24));
        headBg.setStroke(dp(2), Color.rgb(196, 176, 255));
        head.setBackground(headBg);
        head.addView(text("🛍️ Toko Avatar 3D", 18, Color.WHITE, true), new LinearLayout.LayoutParams(0, WRAP, 1f));
        head.addView(pill("💰 " + coins(), ORANGE), new LinearLayout.LayoutParams(WRAP, WRAP));
        c.addView(head, lp(WRAP, 0));

        if (np > 1) {
            LinearLayout pr = new LinearLayout(this);
            pr.setOrientation(LinearLayout.HORIZONTAL);
            for (int i = 0; i < np; i++) {
                final int pl = i;
                boolean sel = i == shopPl;
                TextView t = text(av(i) + " " + names[i], 14, sel ? Color.WHITE : DARK, true);
                t.setBackground(box(sel ? PLAYER_COLORS[i] : GRAY, 16));
                t.setOnClickListener(v -> {
                    shopPl = pl;
                    showToko();
                });
                pr.addView(t, weight(i == 0 ? 0 : 4, 0));
            }
            c.addView(pr, lp(dp(44), 8));
        }

        // ---- Avatar besar di tengah; tombol ganti-gender / peci / tas melayang di sisinya ----
        LinearLayout pv = new LinearLayout(this);
        pv.setOrientation(LinearLayout.VERTICAL);
        pv.setGravity(Gravity.CENTER_HORIZONTAL);
        pv.setPadding(0, 0, 0, 0);
        final float pvRad = dp(24);
        pv.setOutlineProvider(new android.view.ViewOutlineProvider() {
            @Override
            public void getOutline(View v, Outline o) {
                o.setRoundRect(0, 0, v.getWidth(), v.getHeight(), pvRad);
            }
        });
        pv.setClipToOutline(true);
        GradientDrawable studioBg = new GradientDrawable(GradientDrawable.Orientation.TL_BR,
                new int[]{Color.rgb(237, 233, 255), Color.rgb(251, 243, 255)});
        studioBg.setCornerRadius(dp(24));
        studioBg.setStroke(dp(1), Color.rgb(219, 206, 255));
        pv.setBackground(studioBg);
        int usable = Math.min(getResources().getDisplayMetrics().widthPixels, dp(640)) - dp(44);
        int maxH = (int) (getResources().getDisplayMetrics().heightPixels * 0.36f);
        int avSize = Math.max(dp(150), Math.min(usable, maxH));
        FrameLayout stage = new FrameLayout(this);
        if (USE_PUTRI_GL) {
            putriGL = new PutriGLView(this, genderOf[shopPl]);
            applyLook3D(putriGL, shopPl);
            stage.addView(putriGL, new FrameLayout.LayoutParams(MATCH, MATCH));
        } else {
            putriGL = null;
            stage.addView(avatarView(shopPl), new FrameLayout.LayoutParams(MATCH, MATCH));
        }
        // kolom tombol kiri: Putra, Putri, Peci, Tas
        LinearLayout side = new LinearLayout(this);
        side.setOrientation(LinearLayout.VERTICAL);
        for (int g = 0; g < 2; g++) {
            final int gg = g;
            TextView gt = orb(g == 0 ? "👦" : "👧", genderOf[shopPl] == g, g == 0 ? BLUE : PINK);
            gt.setContentDescription(g == 0 ? "Putra" : "Putri");
            gt.setOnClickListener(v -> {
                genderOf[shopPl] = gg;
                prefs.edit().putInt("gender" + shopPl, gg).apply();
                showToko();
            });
            LinearLayout.LayoutParams gl = new LinearLayout.LayoutParams(dp(44), dp(44));
            gl.bottomMargin = dp(6);
            side.addView(gt, gl);
        }
        if (USE_PUTRI_GL) {
            final boolean ext = avatarExt();
            TextView xm = orb("3D", ext, PURPLE);
            xm.setContentDescription(ext ? "Model 3D baru aktif. Ketuk untuk memakai avatar lama"
                    : "Avatar lama aktif. Ketuk untuk mencoba model 3D baru");
            xm.setOnClickListener(v -> {
                prefs.edit().putBoolean("avatarExt", !ext).apply();
                sfx(SFX_OK);
                showToko();
            });
            LinearLayout.LayoutParams xml = new LinearLayout.LayoutParams(dp(44), dp(44));
            xml.bottomMargin = dp(6);
            side.addView(xm, xml);
            if (!ext && genderOf[shopPl] == 0) {
                final boolean peciLepas = peciOff(shopPl);
                TextView pc = orb("🧢", !peciLepas, GREEN);
                pc.setContentDescription(peciLepas ? "Peci dilepas. Ketuk untuk memakai peci" : "Peci dipakai. Ketuk untuk melepas peci");
                pc.setOnClickListener(v -> {
                    prefs.edit().putBoolean("peciOff" + shopPl, !peciLepas).apply();
                    sfx(SFX_OK);
                    showToko();
                });
                LinearLayout.LayoutParams pl2 = new LinearLayout.LayoutParams(dp(44), dp(44));
                pl2.bottomMargin = dp(6);
                side.addView(pc, pl2);
            }
            if (!ext) {
                final boolean tasLepas = tasOff(shopPl);
                TextView tb = orb("🎒", !tasLepas, GREEN);
                tb.setContentDescription(tasLepas ? "Tas dilepas. Ketuk untuk memakai tas" : "Tas dipakai. Ketuk untuk melepas tas");
                tb.setOnClickListener(v -> {
                    prefs.edit().putBoolean("tasOff" + shopPl, !tasLepas).apply();
                    sfx(SFX_OK);
                    showToko();
                });
                side.addView(tb, new LinearLayout.LayoutParams(dp(44), dp(44)));
            }
        }
        FrameLayout.LayoutParams sdl = new FrameLayout.LayoutParams(WRAP, WRAP, Gravity.TOP | Gravity.START);
        sdl.setMargins(dp(8), dp(8), 0, 0);
        stage.addView(side, sdl);
        // tombol tampak depan (kanan bawah)
        if (USE_PUTRI_GL) {
            TextView rs = orb("↺", false, PURPLE);
            rs.setContentDescription("Tampak depan");
            rs.setOnClickListener(v -> { if (putriGL != null) putriGL.resetView(); });
            FrameLayout.LayoutParams rl = new FrameLayout.LayoutParams(dp(44), dp(44), Gravity.BOTTOM | Gravity.END);
            rl.setMargins(0, 0, dp(8), dp(8));
            stage.addView(rs, rl);
        }
        // naga pendamping (kanan atas)
        if (prefs.getBoolean("nagaPakai", false)) {
            // naga digambar di dalam adegan 3D avatar (terhalang avatar saat di belakang)
            if (putriGL != null) putriGL.setNaga(true);
        }
        if (putriGL != null && prefs.getBoolean("gojiPakai", false)) putriGL.setGoji(true);
        String[] sp = {"✦", "✧"};
        int[] spg = {Gravity.BOTTOM | Gravity.START, Gravity.CENTER_VERTICAL | Gravity.END};
        for (int q = 0; q < 2; q++) {
            TextView star = text(sp[q], q == 0 ? 20 : 15, Color.WHITE, true);
            star.setShadowLayer(dp(3), 0, 0, Color.rgb(255, 235, 150));
            FrameLayout.LayoutParams sl = new FrameLayout.LayoutParams(WRAP, WRAP, spg[q]);
            sl.setMargins(dp(12), dp(8), dp(12), dp(12));
            stage.addView(star, sl);
        }
        pv.addView(stage, new LinearLayout.LayoutParams(MATCH, avSize));
        c.addView(pv, new LinearLayout.LayoutParams(MATCH, WRAP));
        ((LinearLayout.LayoutParams) pv.getLayoutParams()).topMargin = dp(8);

        // ---- Kategori: grid 2 baris x 3 kolom, tanpa geser ----
        LinearLayout tabs = new LinearLayout(this);
        tabs.setOrientation(LinearLayout.VERTICAL);
        for (int r = 0; r < 2; r++) {
            LinearLayout trow = new LinearLayout(this);
            trow.setOrientation(LinearLayout.HORIZONTAL);
            for (int q = 0; q < 3; q++) {
                final int tbi = r * 3 + q;
                boolean sel = tbi == shopTab;
                String nm = (tbi == 4 && genderOf[shopPl] == 1) ? "Jilbab" : SHOP_TAB_NAME[tbi];
                TextView chip = text(SHOP_TAB_ICON[tbi] + " " + nm, 12, sel ? Color.WHITE : DARK, true);
                chip.setGravity(Gravity.CENTER);
                chip.setMaxLines(1);
                chip.setPadding(dp(2), dp(2), dp(2), dp(2));
                chip.setBackground(box(sel ? Color.rgb(108, 76, 205) : Color.argb(235, 247, 244, 255), 16));
                chip.setContentDescription("Kategori " + nm + (sel ? ", dipilih" : ""));
                chip.setOnClickListener(v -> {
                    shopTab = tbi;
                    showToko();
                });
                trow.addView(chip, weight(q == 0 ? 0 : 3, q == 2 ? 0 : 3));
            }
            tabs.addView(trow, lp(dp(44), r == 0 ? 0 : 6));
        }
        c.addView(tabs, lp(WRAP, 10));

        if (shopTab == 5) {
            final boolean nagaOn = prefs.getBoolean("nagaPakai", false);
            LinearLayout nc = new LinearLayout(this);
            nc.setOrientation(LinearLayout.VERTICAL);
            nc.setPadding(dp(8), dp(8), dp(8), dp(8));
            nc.setBackground(box(Color.argb(235, 247, 244, 255), 22));
            nc.addView(new NagaPetView(this, true), new LinearLayout.LayoutParams(MATCH, dp(130)));
            LinearLayout nr = new LinearLayout(this);
            nr.setOrientation(LinearLayout.HORIZONTAL);
            nr.setGravity(Gravity.CENTER_VERTICAL);
            nr.addView(text("🐉 Naga Merah" + (nagaOn ? " · Dipakai" : ""), 14, DARK, true),
                    new LinearLayout.LayoutParams(0, WRAP, 1f));
            Button nb = button(nagaOn ? "Lepas naga" : "Pakai naga", nagaOn ? GRAY : GREEN, 13);
            if (nagaOn) nb.setTextColor(DARK);
            nb.setOnClickListener(v -> {
                prefs.edit().putBoolean("nagaPakai", !nagaOn).apply();
                sfx(SFX_OK);
                showToko();
            });
            nr.addView(nb, new LinearLayout.LayoutParams(dp(130), dp(42)));
            nc.addView(nr, lp(WRAP, 6));
            TextView cr = text("Red Dragon by GadgetHamster (CC-BY 4.0)", 10, Color.rgb(110, 90, 170), false);
            cr.setGravity(Gravity.CENTER);
            nc.addView(cr, lp(WRAP, 4));
            c.addView(nc, lp(WRAP, 10));

            final boolean gojiOn = prefs.getBoolean("gojiPakai", false);
            LinearLayout gc = new LinearLayout(this);
            gc.setOrientation(LinearLayout.VERTICAL);
            gc.setPadding(dp(8), dp(8), dp(8), dp(8));
            gc.setBackground(box(Color.argb(235, 247, 244, 255), 22));
            LinearLayout gr = new LinearLayout(this);
            gr.setOrientation(LinearLayout.HORIZONTAL);
            gr.setGravity(Gravity.CENTER_VERTICAL);
            gr.addView(text("\uD83E\uDD96 Godzilla Mini" + (gojiOn ? " · Dipakai" : ""), 14, DARK, true),
                    new LinearLayout.LayoutParams(0, WRAP, 1f));
            Button gb = button(gojiOn ? "Lepas Godzilla" : "Pakai Godzilla", gojiOn ? GRAY : GREEN, 13);
            if (gojiOn) gb.setTextColor(DARK);
            gb.setOnClickListener(v -> {
                prefs.edit().putBoolean("gojiPakai", !gojiOn).apply();
                sfx(SFX_OK);
                showToko();
            });
            gr.addView(gb, new LinearLayout.LayoutParams(dp(150), dp(42)));
            gc.addView(gr, lp(WRAP, 6));
            TextView gcr = text("Godzilla Minus Zero by Dola (CC-BY 4.0)", 10, Color.rgb(110, 90, 170), false);
            gcr.setGravity(Gravity.CENTER);
            gc.addView(gcr, lp(WRAP, 4));
            c.addView(gc, lp(WRAP, 10));
        }

        // ---- Barang: dua baris, digeser ke samping (tanpa gulir ke bawah) ----
        final ArrayList<View> cells = new ArrayList<>();
        if (shopTab == 0) {
            for (int idx = 0; idx < HOOD_ORDER.length; idx++) {
                    final int k = HOOD_ORDER[idx];
                    final boolean on = avatar[shopPl] == k;
                    LinearLayout tile = new LinearLayout(this);
                    tile.setOrientation(LinearLayout.VERTICAL);
                    tile.setGravity(Gravity.CENTER_HORIZONTAL);
                    tile.setPadding(dp(3), dp(4), dp(3), dp(4));
                    tile.setBackground(box(on ? Color.rgb(223, 246, 233) : Color.rgb(243, 241, 251), 14));
                    AvatarView preview = previewView(isHood(k) ? k : -1, -1, -1, 1);
                    preview.gender = genderOf[shopPl];
                    tile.addView(preview, new LinearLayout.LayoutParams(MATCH, dp(66)));
                    TextView label = text(HOOD_NAMES[k], 11, DARK, true);
                    label.setGravity(Gravity.CENTER);
                    label.setMaxLines(1);
                    tile.addView(label, new LinearLayout.LayoutParams(MATCH, WRAP));
                    if (on) {
                        TextView status = text("Dipakai", 10, GREEN, true);
                        status.setGravity(Gravity.CENTER);
                        tile.addView(status, new LinearLayout.LayoutParams(MATCH, WRAP));
                    }
                    tile.setContentDescription("Tudung " + HOOD_NAMES[k] + (on ? ", sedang dipakai" : ", ketuk untuk memakai"));
                    tile.setOnClickListener(v -> {
                        for (int j = 0; j < np; j++) {
                            if (isHood(k) && j != shopPl && avatar[j] == k) {
                                avatar[j] = avatar[shopPl];
                                prefs.edit().putInt("avatar" + j, avatar[j]).apply();
                            }
                        }
                        avatar[shopPl] = k;
                        prefs.edit().putInt("avatar" + shopPl, k).apply();
                        showToko();
                    });
                cells.add(tile);
            }
        }
        int[] slotsOfTab = shopTab == 0 ? new int[]{0} : shopTab == 1 ? new int[]{1, 2}
                : shopTab == 2 ? new int[]{4} : shopTab == 3 ? new int[]{5}
                : shopTab == 4 ? new int[]{6} : new int[]{3};
        for (int slot : slotsOfTab) {
            for (int i = 0; i < ITEMS.length; i++) {
                // 2D-only head/face/handheld/companion items cannot be rendered on the 3D avatar
                if (itSlot(i) == slot && itFits(i, genderOf[shopPl]) &&
                        slot != 0 && slot != 1 && slot != 2 && slot != 3) {
                    cells.add(shopCell(i));
                }
            }
        }
        if (!cells.isEmpty()) {
            android.widget.HorizontalScrollView sh = new android.widget.HorizontalScrollView(this);
            sh.setHorizontalScrollBarEnabled(false);
            LinearLayout strip = new LinearLayout(this);
            strip.setOrientation(LinearLayout.HORIZONTAL);
            for (int i = 0; i < cells.size(); i += 2) {
                LinearLayout col = new LinearLayout(this);
                col.setOrientation(LinearLayout.VERTICAL);
                for (int q = 0; q < 2 && i + q < cells.size(); q++) {
                    LinearLayout.LayoutParams cellLp = new LinearLayout.LayoutParams(dp(104), WRAP);
                    cellLp.bottomMargin = dp(6);
                    col.addView(cells.get(i + q), cellLp);
                }
                LinearLayout.LayoutParams colLp = new LinearLayout.LayoutParams(WRAP, WRAP);
                colLp.rightMargin = dp(6);
                strip.addView(col, colLp);
            }
            sh.addView(strip);
            c.addView(sh, lp(WRAP, 8));
        }
        int have = 0, available = 0;
        for (int i = 0; i < ITEMS.length; i++) {
            if (itSlot(i) <= 3 || !itFits(i, genderOf[shopPl])) continue;
            available++;
            if (owns(i)) have++;
        }
        LinearLayout foot = new LinearLayout(this);
        foot.setOrientation(LinearLayout.HORIZONTAL);
        foot.setGravity(Gravity.CENTER_VERTICAL);
        foot.addView(text("\uD83D\uDDC2\uFE0F Koleksi 3D: " + have + "/" + available, 13, DARK, true),
                new LinearLayout.LayoutParams(0, WRAP, 1f));
        Button back = button("Kembali", Color.rgb(108, 76, 205), 14);
        back.setOnClickListener(v -> showMenu());
        foot.addView(back, new LinearLayout.LayoutParams(dp(130), dp(46)));
        c.addView(foot, lp(WRAP, 4));

        wrapBg(c, tokoBg());
    }

    private TextView orb(String label, boolean on, int color) {
        TextView t = text(label, 18, on ? Color.WHITE : DARK, true);
        t.setGravity(Gravity.CENTER);
        t.setBackground(box(on ? color : Color.argb(215, 255, 255, 255), 22));
        t.setElevation(dp(3));
        return t;
    }

    private Drawable tokoBg() {
        return new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{Color.rgb(160, 128, 242), Color.rgb(205, 188, 250), Color.rgb(241, 233, 255)});
    }

    private void dialogAvatar() {
        LinearLayout b = dialogBody();
        for (int i = 0; i < np; i++) {
            final int pl = i;
            String who = np == 1 ? "Avatar kamu" : "Avatar " + names[i];
            b.addView(text(who, 13, DARK, true), lp(WRAP, i == 0 ? 0 : 14));
            for (int r = 0; r < 2; r++) {
                LinearLayout row = new LinearLayout(this);
                row.setOrientation(LinearLayout.HORIZONTAL);
                for (int q = 0; q < 6; q++) {
                    final int k = r * 6 + q;
                    boolean on = avatar[i] == k;
                    TextView t = text(AVATARS[k], 24, Color.WHITE, false);
                    t.setBackground(on ? box(PLAYER_COLORS[i], 14) : box(GRAY, 14));
                    t.setOnClickListener(v -> {
                        for (int j = 0; j < np; j++) {
                            if (j != pl && avatar[j] == k) {
                                avatar[j] = avatar[pl];
                                prefs.edit().putInt("avatar" + j, avatar[j]).apply();
                            }
                        }
                        avatar[pl] = k;
                        prefs.edit().putInt("avatar" + pl, k).apply();
                        dialogAvatar();
                    });
                    row.addView(t, weight(q == 0 ? 0 : 3, q == 5 ? 0 : 3));
                }
                b.addView(row, lp(dp(48), 8));
            }
        }
        showDialogView("Pilih avatar", b);
    }

    // ---------- Game Cocokkan Pasangan ----------

    private String[][] builtinPairs(int sj, int k) {
        if (sj == 1) {
            return k == 0 ? M_ID_1 : k == 1 ? M_ID_2 : k == 2 ? M_ID_3 : null;
        }
        if (sj == 2) {
            return k == 0 ? M_EN_1 : k == 1 ? M_EN_2 : k == 2 ? M_EN_3 : null;
        }
        return null;
    }

    // Set pasangan bergambar untuk Indonesia Kelas 1 dan Inggris Kelas 2-3 (selain set kata biasa)
    private String[][] picturePairs(int sj, int k) {
        if (sj == 1) {
            return k == 0 ? M_ID_1G : null;
        }
        if (sj == 2) {
            return k == 1 ? M_EN_2G : k == 2 ? M_EN_3G : null;
        }
        return null;
    }

    private String pictureHint(int sj, int k) {
        return sj == 1 ? "Cocokkan gambar dengan namanya" : "Cocokkan gambar dengan kata Inggrisnya";
    }

    private String builtinHint(int sj, int k) {
        if (sj == 1) {
            return k == 2 ? "Cocokkan persamaan kata" : "Cocokkan lawan kata";
        }
        return k == 0 ? "Cocokkan gambar dengan katanya" : "Cocokkan kata dengan artinya";
    }

    private ArrayList<String[]> distinctPairs(ArrayList<String[]> in) {
        ArrayList<String[]> out = new ArrayList<>();
        for (String[] p : in) {
            boolean dup = false;
            for (String[] q : out) {
                if (q[0].equalsIgnoreCase(p[0]) || q[1].equalsIgnoreCase(p[1])) {
                    dup = true;
                    break;
                }
            }
            if (!dup) {
                out.add(p);
            }
        }
        return out;
    }

    // Soal guru dipakai kalau ada cukup (minimal 4); kalau tidak, pasangan bawaan (kelas 1-3)
    private ArrayList<String[]> matchPool(int sj) {
        ArrayList<String[]> teacher = new ArrayList<>();
        for (B b : bank) {
            if (b.subj != sj || b.kelas != kelas + 1) {
                continue;
            }
            String a = b.opt[b.key];
            if (b.text.length() > 36 || a.length() > 22) {
                continue;
            }
            teacher.add(new String[]{b.text, a});
        }
        ArrayList<String[]> t = distinctPairs(teacher);
        if (t.size() >= 4) {
            matchHint = "Cocokkan soal dengan jawabannya";
            return t;
        }
        String[][] bi = builtinPairs(sj, kelas);
        String[][] pic = picturePairs(sj, kelas);
        ArrayList<String[]> out = new ArrayList<>();
        // Di Cocokkan Pasangan biasa, set bergambar dan set kata dipilih bergantian; di Peta tetap set kata
        if (pic != null && (bi == null || (advLevel < 0 && rnd.nextBoolean()))) {
            out.addAll(Arrays.asList(pic));
            matchHint = pictureHint(sj, kelas);
        } else if (bi != null) {
            out.addAll(Arrays.asList(bi));
            matchHint = builtinHint(sj, kelas);
        }
        return out;
    }

    private boolean matchAvailable(int sj) {
        return matchPool(sj).size() >= 4;
    }

    private void addModeButton(LinearLayout c, String label, int color, final AlertDialog[] dh,
                               final Runnable go) {
        Button b = button(label, color, 18);
        flat(b);
        c.addView(b, lp(dp(60), 12));
        b.setOnClickListener(v -> {
            if (dh[0] != null) {
                dh[0].dismiss();
            }
            go.run();
        });
    }

    private void dialogMode(final int sj) {
        LinearLayout c = new LinearLayout(this);
        c.setOrientation(LinearLayout.VERTICAL);
        c.setPadding(dp(18), dp(16), dp(18), dp(16));
        c.addView(text("Main apa?", 22, ORANGE_DARK, true), lp(WRAP, 0));
        final AlertDialog[] dh = new AlertDialog[1];
        if (quizAvailable(sj)) {
            addModeButton(c, "\uD83D\uDCDD  Kuis soal", BLUE, dh, () -> startBank(sj));
        }
        if (matchAvailable(sj)) {
            addModeButton(c, "\uD83E\uDDE9  Cocokkan Pasangan", PURPLE, dh, () -> startMatch(sj));
        }
        if (sj == 1 && susunAvailable(0)) {
            addModeButton(c, "\uD83D\uDD24  Susun Kata", TEAL, dh, () -> startSusun(0));
        }
        if (sj == 1 && susunAvailable(1)) {
            addModeButton(c, "\uD83D\uDCCB  Susun Kalimat", ORANGE, dh, () -> startSusun(1));
        }
        if (tebakAvailable(sj)) {
            addModeButton(c, "\uD83D\uDDBC\uFE0F  Tebak Gambar", ORANGE, dh, () -> startTebak(sj));
        }
        addModeButton(c, "\uD83C\uDF3F  Kuis Alam", GREEN, dh, () -> startAlam(sj));
        ScrollView dsv = new ScrollView(this);
        dsv.addView(c);
        final AlertDialog d = new AlertDialog.Builder(this).setView(dsv).create();
        dh[0] = d;
        pickDialog = d;
        d.show();
        if (d.getWindow() != null) {
            d.getWindow().setBackgroundDrawable(box(Color.WHITE, 24));
            int maxW = getResources().getDisplayMetrics().widthPixels - dp(24);
            d.getWindow().setLayout(Math.min(dp(340), maxW), ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

    private void startMatch(int sj) {
        saveNames();
        nameFields = null;
        ArrayList<String[]> pool = matchPool(sj);
        if (pool.size() < 4) {
            Toast.makeText(this, "Belum cukup pasangan untuk dimainkan.", Toast.LENGTH_LONG).show();
            showMenu();
            return;
        }
        Collections.shuffle(pool, rnd);
        matchSubj = sj;
        matchN = Math.min(5, pool.size());
        matchPairs = new String[matchN][];
        for (int i = 0; i < matchN; i++) {
            matchPairs[i] = pool.get(i);
        }
        matchWrong = 0;
        showMatch();
    }

    private int[] shuffledOrder(int n) {
        int[] o = new int[n];
        for (int i = 0; i < n; i++) {
            o[i] = i;
        }
        for (int i = n - 1; i > 0; i--) {
            int j = rnd.nextInt(i + 1);
            int t = o[i];
            o[i] = o[j];
            o[j] = t;
        }
        return o;
    }

    private TextView matchCard(String label, int color) {
        boolean emoji = label.length() <= 4 && !Character.isLetterOrDigit(label.codePointAt(0));
        int sp = emoji ? 30 : label.length() > 22 ? 12 : label.length() > 12 ? 15 : 18;
        TextView t = text(label, sp, Color.WHITE, true);
        t.setBackground(box(color, 18));
        t.setPadding(dp(6), dp(4), dp(6), dp(4));
        t.setMaxLines(3);
        t.setElevation(dp(3));
        return t;
    }

    private void showMatch() {
        handler.removeCallbacksAndMessages(null);
        setScreen(9);
        matchBusy = false;
        selL = -1;
        selR = -1;
        matchDone = 0;
        matchedFlag = new boolean[matchN];
        cardL = new TextView[matchN];
        cardR = new TextView[matchN];

        LinearLayout c = column();
        LinearLayout top = new LinearLayout(this);
        top.setOrientation(LinearLayout.HORIZONTAL);
        top.setGravity(Gravity.CENTER_VERTICAL);
        top.addView(pill(av(0) + " " + SUBJ_NAMES[matchSubj], SUBJ_COLOR[matchSubj]),
                new LinearLayout.LayoutParams(WRAP, WRAP));
        top.addView(new View(this), new LinearLayout.LayoutParams(0, 1, 1f));
        tvMatchInfo = text("0/" + matchN, 20, DARK, true);
        top.addView(tvMatchInfo, new LinearLayout.LayoutParams(WRAP, WRAP));
        c.addView(top, lp(WRAP, 0));
        c.addView(text(matchHint, 16, DARK, true), lp(WRAP, 14));

        LinearLayout cols = new LinearLayout(this);
        cols.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout colL = new LinearLayout(this);
        colL.setOrientation(LinearLayout.VERTICAL);
        LinearLayout colR = new LinearLayout(this);
        colR.setOrientation(LinearLayout.VERTICAL);
        int[] oL = shuffledOrder(matchN);
        int[] oR = shuffledOrder(matchN);
        for (int pos = 0; pos < matchN; pos++) {
            final int li = oL[pos];
            final int ri = oR[pos];
            TextView l = matchCard(matchPairs[li][0], BLUE);
            l.setOnClickListener(v -> matchTap(true, li));
            cardL[li] = l;
            colL.addView(l, lp(dp(70), pos == 0 ? 0 : 10));
            TextView r = matchCard(matchPairs[ri][1], PURPLE);
            r.setOnClickListener(v -> matchTap(false, ri));
            cardR[ri] = r;
            colR.addView(r, lp(dp(70), pos == 0 ? 0 : 10));
        }
        cols.addView(colL, weight(0, 6));
        cols.addView(colR, weight(6, 0));
        c.addView(cols, lp(WRAP, 16));

        Button cancel = button("Kembali", GRAY, 14);
        cancel.setTextColor(DARK);
        c.addView(cancel, lp(dp(46), 22));
        cancel.setOnClickListener(v -> {
            if (advLevel >= 0) {
                showPeta();
            } else {
                showMenu();
            }
        });
        wrap(c);
    }

    private void matchTap(boolean left, int pi) {
        if (matchBusy || matchedFlag[pi]) {
            return;
        }
        sfx(SFX_TAP);
        if (left) {
            if (selL >= 0) {
                cardL[selL].setBackground(box(BLUE, 18));
            }
            selL = pi;
            cardL[pi].setBackground(box(ORANGE, 18));
            pop(cardL[pi], 1.08f);
        } else {
            if (selR >= 0) {
                cardR[selR].setBackground(box(PURPLE, 18));
            }
            selR = pi;
            cardR[pi].setBackground(box(ORANGE, 18));
            pop(cardR[pi], 1.08f);
        }
        if (selL < 0 || selR < 0) {
            return;
        }
        final int a = selL;
        final int b = selR;
        if (a == b) {
            recordMatch(a, true);
            matchedFlag[a] = true;
            cardL[a].setBackground(box(GREEN, 18));
            cardR[a].setBackground(box(GREEN, 18));
            cardL[a].setAlpha(0.6f);
            cardR[a].setAlpha(0.6f);
            pop(cardL[a], 1.15f);
            pop(cardR[a], 1.15f);
            sfx(SFX_CORRECT);
            selL = -1;
            selR = -1;
            matchDone++;
            tvMatchInfo.setText(matchDone + "/" + matchN);
            if (matchDone >= matchN) {
                matchBusy = true;
                handler.postDelayed(this::showMatchResult, 800);
            }
        } else {
            recordMatch(a, false);
            matchWrong++;
            matchBusy = true;
            sfx(SFX_WRONG);
            buzz(80);
            shake(cardL[a]);
            shake(cardR[b]);
            handler.postDelayed(() -> {
                cardL[a].setBackground(box(BLUE, 18));
                cardR[b].setBackground(box(PURPLE, 18));
                selL = -1;
                selR = -1;
                matchBusy = false;
            }, 500);
        }
    }

    private void showMatchResult() {
        handler.removeCallbacksAndMessages(null);
        setScreen(9);
        saveStats();
        int stars = matchWrong == 0 ? 3 : matchWrong <= 2 ? 2 : 1;
        freshBadges = awardBadges(2, stars, matchWrong == 0, false);
        boolean advMatch = advLevel >= 0;
        if (advMatch) {
            advSave(stars);
            awardMapBadges(freshBadges);
        }
        LinearLayout c = column();
        c.setGravity(Gravity.CENTER);
        c.addView(text(av(0), 48, Color.WHITE, false), lp(WRAP, 0));
        c.addView(text(stars == 3 ? "Hebat sekali!" : "Selesai!", 32, ORANGE_DARK, true), lp(WRAP, 4));
        c.addView(pill(KELAS_NAMES[kelas] + " \u2022 " + SUBJ_NAMES[matchSubj],
                SUBJ_COLOR[matchSubj]), lpw(10));

        LinearLayout card = card();
        card.setGravity(Gravity.CENTER_HORIZONTAL);
        LinearLayout starRow = new LinearLayout(this);
        starRow.setOrientation(LinearLayout.HORIZONTAL);
        starRow.setGravity(Gravity.CENTER);
        for (int i = 0; i < 3; i++) {
            starRow.addView(text(i < stars ? "\u2B50" : "\u2606", 44, ORANGE, false),
                    new LinearLayout.LayoutParams(WRAP, WRAP));
        }
        card.addView(starRow, lp(WRAP, 0));
        card.addView(text(matchWrong == 0 ? "Semua benar, tanpa salah!"
                : "Salah memilih " + matchWrong + " kali", 16, DARK, false), lp(WRAP, 10));
        card.addView(text("\uD83C\uDF1F Total bintang: " + totalStars(), 14, DARK, false), lp(WRAP, 8));
        c.addView(card, lp(WRAP, 18));
        addFreshBadges(c);

        if (advMatch) {
            addAdvButtons(c, true);
        } else {
            Button again = button("Main Lagi", GREEN, 22);
            again.setElevation(dp(4));
            c.addView(again, lp(dp(64), 26));
            again.setOnClickListener(v -> startMatch(matchSubj));

            Button menu = button("Menu Utama", ORANGE, 20);
            menu.setElevation(dp(4));
            c.addView(menu, lp(dp(60), 12));
            menu.setOnClickListener(v -> showMenu());
        }
        wrap(c);
        sfx(stars >= 2 ? SFX_WIN : SFX_OK);
        if (stars >= 2) {
            launchConfetti(stars == 3 ? 130 : 80);
        }
    }

    // =====================================================================
    // Susun Kata dan Susun Kalimat (versi 6.0.4), Bahasa Indonesia
    // Soal buatan Claude, perlu dicek guru kelas.
    // =====================================================================

    private static final String[][] SUSUN_W = {
            {"kucing", "buku", "meja", "sapi", "ikan", "bola", "rumah", "kursi", "pintu", "baju"},
            {"sekolah", "sepeda", "jendela", "kertas", "pensil", "burung", "guru", "tangan"},
            {"tetangga", "gembira", "pelajaran", "halaman", "lapangan", "makanan", "minuman", "kebun"},
            {}, {}, {}
    };
    private static final String[][] SUSUN_S = {
            {"Ani makan nasi.", "Ibu memasak sayur.", "Adik bermain bola.", "Ayah membaca koran.",
                    "Kakak menulis surat.", "Aku suka buku.", "Kucing minum susu.", "Burung terbang tinggi."},
            {"Ibu pergi ke pasar.", "Adik membaca buku cerita.", "Kami belajar di sekolah.",
                    "Budi menyiram bunga di taman.", "Kucing tidur di bawah meja.", "Ayah membeli ikan segar.",
                    "Ani menolong ibunya memasak.", "Murid-murid berbaris di lapangan."},
            {"Kami bermain bola di lapangan sekolah.", "Ibu membeli sayur dan buah di pasar.",
                    "Pak guru menjelaskan pelajaran dengan sabar.", "Setiap pagi ayah berangkat bekerja.",
                    "Adik dan kakak membersihkan halaman rumah.", "Hujan turun deras sejak tadi malam.",
                    "Kita harus menjaga kebersihan lingkungan.", "Anak yang rajin pasti disayang guru."},
            {"Petani menanam padi di sawah yang luas.", "Kita wajib menghormati orang yang lebih tua.",
                    "Ibu menyiapkan sarapan sebelum kami berangkat sekolah.",
                    "Perpustakaan sekolah buka setiap hari Senin sampai Jumat.",
                    "Para siswa mengerjakan tugas kelompok dengan kompak.",
                    "Sungai itu mengalir jernih menuju laut.",
                    "Dengan rajin membaca kita akan memperoleh pengetahuan.",
                    "Ayah menanam pohon mangga di depan rumah."},
            {"Menjaga kelestarian alam adalah tanggung jawab kita semua.",
                    "Setelah makan malam, keluarga kami berkumpul di ruang tengah.",
                    "Para nelayan berangkat melaut sebelum matahari terbit.",
                    "Pemerintah membangun jembatan baru untuk memudahkan warga.",
                    "Membuang sampah pada tempatnya menjadikan lingkungan bersih.",
                    "Kerja sama yang baik membuat pekerjaan cepat selesai.",
                    "Siswa teladan selalu datang tepat waktu.",
                    "Hasil panen petani meningkat karena cuaca bersahabat."},
            {"Persatuan dan kesatuan bangsa harus kita jaga bersama.",
                    "Membaca buku setiap hari dapat menambah wawasan kita.",
                    "Keberhasilan diperoleh melalui kerja keras dan doa.",
                    "Para pahlawan telah berjuang demi kemerdekaan bangsa Indonesia.",
                    "Kita perlu menghemat air agar tidak kekurangan di masa depan.",
                    "Kejujuran adalah kunci kepercayaan antarteman.",
                    "Peserta lomba menyiapkan diri dengan latihan yang teratur.",
                    "Teknologi membantu manusia menyelesaikan pekerjaan lebih cepat."}
    };

    private int susunKind;
    private String[] susunPool;
    private int susunN;
    private int susunIdx;
    private int susunWrong;
    private String susunTarget = "";
    private String[] susunTok;
    private boolean[] susunUsed;
    private final ArrayList<Integer> susunSeq = new ArrayList<>();
    private boolean susunBusy;
    private Button[] susunBtn;
    private TextView tvSusunAns;
    private LinearLayout susunAnsBox;

    private boolean susunAvailable(int kind) {
        String[] src = kind == 0 ? SUSUN_W[kelas] : SUSUN_S[kelas];
        return src.length >= 4;
    }

    private void startSusun(int kind) {
        saveNames();
        nameFields = null;
        susunKind = kind;
        String[] src = kind == 0 ? SUSUN_W[kelas] : SUSUN_S[kelas];
        if (src.length < 4) {
            Toast.makeText(this, "Belum ada soal susun untuk kelas ini.", Toast.LENGTH_LONG).show();
            showMenu();
            return;
        }
        ArrayList<String> pool = new ArrayList<>(Arrays.asList(src));
        Collections.shuffle(pool, rnd);
        susunN = Math.min(5, pool.size());
        susunPool = new String[susunN];
        for (int i = 0; i < susunN; i++) {
            susunPool[i] = pool.get(i);
        }
        susunIdx = 0;
        susunWrong = 0;
        showSusun();
    }

    private String[] susunTokens(String target, int kind) {
        String[] t;
        if (kind == 0) {
            t = new String[target.length()];
            for (int i = 0; i < t.length; i++) {
                t[i] = String.valueOf(target.charAt(i));
            }
        } else {
            t = target.split(" ");
        }
        String[] o = t.clone();
        for (int tries = 0; tries < 12; tries++) {
            for (int i = o.length - 1; i > 0; i--) {
                int j = rnd.nextInt(i + 1);
                String x = o[i];
                o[i] = o[j];
                o[j] = x;
            }
            if (!Arrays.equals(o, t)) {
                break;
            }
        }
        return o;
    }

    private String susunBuilt() {
        StringBuilder sb = new StringBuilder();
        for (int n = 0; n < susunSeq.size(); n++) {
            if (n > 0 && susunKind == 1) {
                sb.append(' ');
            }
            sb.append(susunTok[susunSeq.get(n)]);
        }
        return sb.toString();
    }

    private void susunRefresh() {
        String built = susunBuilt();
        tvSusunAns.setText(built.isEmpty() ? "…" : built);
        for (int i = 0; i < susunBtn.length; i++) {
            boolean u = susunUsed[i];
            susunBtn[i].setAlpha(u ? 0.3f : 1f);
            susunBtn[i].setEnabled(!u);
        }
    }

    private void showSusun() {
        handler.removeCallbacksAndMessages(null);
        setScreen(12);
        susunBusy = false;
        susunSeq.clear();
        susunTarget = susunPool[susunIdx];
        susunTok = susunTokens(susunTarget, susunKind);
        susunUsed = new boolean[susunTok.length];
        susunBtn = new Button[susunTok.length];

        LinearLayout c = column();
        LinearLayout top = new LinearLayout(this);
        top.setOrientation(LinearLayout.HORIZONTAL);
        top.setGravity(Gravity.CENTER_VERTICAL);
        top.addView(pill(av(0) + " " + (susunKind == 0 ? "Susun Kata" : "Susun Kalimat"), SUBJ_COLOR[1]),
                new LinearLayout.LayoutParams(WRAP, WRAP));
        top.addView(new View(this), new LinearLayout.LayoutParams(0, 1, 1f));
        top.addView(text((susunIdx + 1) + "/" + susunN, 20, DARK, true),
                new LinearLayout.LayoutParams(WRAP, WRAP));
        c.addView(top, lp(WRAP, 0));
        c.addView(text(susunKind == 0 ? "Susun huruf-huruf ini menjadi kata"
                : "Susun kata-kata ini menjadi kalimat", 16, DARK, true), lp(WRAP, 14));

        susunAnsBox = new LinearLayout(this);
        susunAnsBox.setOrientation(LinearLayout.VERTICAL);
        susunAnsBox.setGravity(Gravity.CENTER);
        susunAnsBox.setBackground(outline(PURPLE, 18));
        susunAnsBox.setPadding(dp(12), dp(16), dp(12), dp(16));
        tvSusunAns = text("", susunKind == 0 ? 34 : 20, PURPLE, true);
        tvSusunAns.setMinHeight(dp(44));
        susunAnsBox.addView(tvSusunAns, new LinearLayout.LayoutParams(MATCH, WRAP));
        c.addView(susunAnsBox, lp(WRAP, 14));

        int per = susunKind == 0 ? 5 : 2;
        LinearLayout row = null;
        for (int i = 0; i < susunTok.length; i++) {
            if (i % per == 0) {
                row = new LinearLayout(this);
                row.setOrientation(LinearLayout.HORIZONTAL);
                c.addView(row, lp(dp(susunKind == 0 ? 62 : 58), i == 0 ? 16 : 8));
            }
            final int idx = i;
            String tk = susunTok[i];
            int sp = susunKind == 0 ? 26 : (tk.length() > 12 ? 13 : 16);
            Button b = button(tk, OPT_COLORS[i % 4], sp);
            flat(b);
            b.setOnClickListener(v -> susunTap(idx));
            row.addView(b, weight(3, 3));
            susunBtn[i] = b;
        }
        int rem = susunTok.length % per;
        if (rem != 0 && row != null) {
            for (int k = rem; k < per; k++) {
                row.addView(new View(this), weight(3, 3));
            }
        }

        LinearLayout ctl = new LinearLayout(this);
        ctl.setOrientation(LinearLayout.HORIZONTAL);
        Button del = button("Hapus", GRAY, 15);
        del.setTextColor(DARK);
        flat(del);
        del.setOnClickListener(v -> susunUndo());
        ctl.addView(del, weight(0, 4));
        Button rs = button("Ulang", GRAY, 15);
        rs.setTextColor(DARK);
        flat(rs);
        rs.setOnClickListener(v -> susunReset());
        ctl.addView(rs, weight(4, 0));
        c.addView(ctl, lp(dp(48), 18));

        Button cancel = button("Kembali", GRAY, 14);
        cancel.setTextColor(DARK);
        c.addView(cancel, lp(dp(46), 14));
        cancel.setOnClickListener(v -> showMenu());

        susunRefresh();
        wrap(c);
    }

    private void susunTap(int idx) {
        if (susunBusy || susunUsed[idx]) {
            return;
        }
        susunUsed[idx] = true;
        susunSeq.add(idx);
        susunRefresh();
        if (susunSeq.size() == susunTok.length) {
            susunCheck();
        }
    }

    private void susunUndo() {
        if (susunBusy || susunSeq.isEmpty()) {
            return;
        }
        int last = susunSeq.remove(susunSeq.size() - 1);
        susunUsed[last] = false;
        susunRefresh();
    }

    private void susunReset() {
        if (susunBusy) {
            return;
        }
        susunSeq.clear();
        Arrays.fill(susunUsed, false);
        susunRefresh();
    }

    private void susunCheck() {
        boolean ok = susunBuilt().equals(susunTarget);
        susunBusy = true;
        recordSusun(ok);
        if (ok) {
            tvSusunAns.setTextColor(GREEN);
            susunAnsBox.setBackground(outline(GREEN, 18));
            sfx(SFX_CORRECT);
            buzz(25);
            pop(susunAnsBox, 1.06f);
            handler.postDelayed(() -> {
                susunIdx++;
                if (susunIdx >= susunN) {
                    showSusunResult();
                } else {
                    showSusun();
                }
            }, 900);
        } else {
            susunWrong++;
            tvSusunAns.setTextColor(RED);
            susunAnsBox.setBackground(outline(RED, 18));
            sfx(SFX_WRONG);
            buzz(80);
            shake(susunAnsBox);
            handler.postDelayed(() -> {
                tvSusunAns.setTextColor(PURPLE);
                susunAnsBox.setBackground(outline(PURPLE, 18));
                susunSeq.clear();
                Arrays.fill(susunUsed, false);
                susunBusy = false;
                susunRefresh();
            }, 700);
        }
    }

    private void recordSusun(boolean ok) {
        try {
            String topic = susunKind == 0 ? "Susun Kata" : "Susun Kalimat";
            bumpStat(stTopic, "1|" + kelas + "|" + topic, ok);
            String gk = "1|" + kelas + "|s:" + cutText(susunTarget, 60);
            bumpStat(stItem, gk, ok);
            if (!stText.containsKey(gk)) {
                stText.put(gk, cutText(susunTarget, 80));
            }
            touchStats();
        } catch (Exception e) {
            // Pencatatan laporan tidak boleh mengganggu permainan
        }
    }

    private void showSusunResult() {
        handler.removeCallbacksAndMessages(null);
        setScreen(9);
        saveStats();
        int stars = susunWrong == 0 ? 3 : susunWrong <= 2 ? 2 : 1;
        freshBadges = awardBadges(3, stars, false, false);
        LinearLayout c = column();
        c.setGravity(Gravity.CENTER);
        c.addView(text(av(0), 48, Color.WHITE, false), lp(WRAP, 0));
        c.addView(text(stars == 3 ? "Hebat sekali!" : "Selesai!", 32, ORANGE_DARK, true), lp(WRAP, 4));
        c.addView(pill(KELAS_NAMES[kelas] + " • " + (susunKind == 0 ? "Susun Kata" : "Susun Kalimat"),
                SUBJ_COLOR[1]), lpw(10));

        LinearLayout card = card();
        card.setGravity(Gravity.CENTER_HORIZONTAL);
        LinearLayout starRow = new LinearLayout(this);
        starRow.setOrientation(LinearLayout.HORIZONTAL);
        starRow.setGravity(Gravity.CENTER);
        for (int i = 0; i < 3; i++) {
            starRow.addView(text(i < stars ? "⭐" : "☆", 44, ORANGE, false),
                    new LinearLayout.LayoutParams(WRAP, WRAP));
        }
        card.addView(starRow, lp(WRAP, 0));
        card.addView(text(susunWrong == 0 ? "Semua benar, tanpa salah!"
                : "Salah menyusun " + susunWrong + " kali", 16, DARK, false), lp(WRAP, 10));
        card.addView(text("🌟 Total bintang: " + totalStars(), 14, DARK, false), lp(WRAP, 8));
        c.addView(card, lp(WRAP, 18));
        addFreshBadges(c);

        Button again = button("Main Lagi", GREEN, 22);
        again.setElevation(dp(4));
        c.addView(again, lp(dp(64), 26));
        again.setOnClickListener(v -> startSusun(susunKind));

        Button menu = button("Menu Utama", ORANGE, 20);
        menu.setElevation(dp(4));
        c.addView(menu, lp(dp(60), 12));
        menu.setOnClickListener(v -> showMenu());
        wrap(c);
        sfx(stars >= 2 ? SFX_WIN : SFX_OK);
        if (stars >= 2) {
            launchConfetti(stars == 3 ? 130 : 80);
        }
    }

    private void startGame(int m) {
        saveNames();
        nameFields = null;
        mode = m;
        plainUi = sambungOn();
        if (m != 7) {
            subj = 0;
        }
        buildQuestions();
        Arrays.fill(pScore, 0);
        Arrays.fill(pCorrect, 0);
        for (int p = 0; p < MAX_PLAYERS; p++) {
            Arrays.fill(pRes[p], 0);
            Arrays.fill(pPick[p], 0);
        }
        cur = 0;
        if (np > 1) {
            showPass();
        } else {
            startTurn();
        }
    }

    // Jumlah soal permainan ini: Peta selalu 10, selain itu menurut kelas
    private int qCount() {
        return advLevel >= 0 ? TOTAL : KELAS_Q[kelas];
    }

    private int range(int lo, int hi) {
        return lo + rnd.nextInt(hi - lo + 1);
    }

    // Nilai yang naik pelan-pelan dari s (soal 1) ke e (soal 10)
    private int grow(int s, int e, int n) {
        return s + (e - s) * (n - 1) / (Math.max(2, qCount()) - 1);
    }

    // Titik pemisah ribuan gaya Indonesia: 1250000 -> 1.250.000
    private String fmt(int v) {
        String s = String.valueOf(v);
        StringBuilder out = new StringBuilder();
        int c = 0;
        for (int i = s.length() - 1; i >= 0; i--) {
            out.append(s.charAt(i));
            c++;
            if (c % 3 == 0 && i > 0) {
                out.append('.');
            }
        }
        return out.reverse().toString();
    }

    private int questionSize(int len) {
        return len <= 9 ? 46 : len <= 13 ? 38 : len <= 16 ? 30 : len <= 19 ? 24 : len <= 23 ? 20 : 17;
    }

    private int optionSize(int len) {
        return len <= 6 ? 28 : len <= 8 ? 24 : 20;
    }

    private void buildQuestions() {
        if (mode == 8) {
            buildTable();
            return;
        }
        if (mode == 7) {
            buildBank();
            return;
        }
        total = qCount();
        ArrayList<String> seen = new ArrayList<>();
        for (int i = 0; i < total; i++) {
            Q q = null;
            // Ulangi sampai dapat soal yang belum muncul di permainan ini.
            // Percobaan 0-29 juga menghindari soal dari permainan sebelumnya.
            // Percobaan 60 ke atas menaikkan tingkat soal sedikit supaya pilihan angkanya lebih luas.
            for (int t = 0; t < 120; t++) {
                q = new Q();
                int lvl = (t < 60) ? i + 1 : Math.min(total, i + 2 + (t - 60) / 10);
                if (mode == 5) {
                    buildThree(q, lvl);
                } else if (kelas <= 2 && (mode == 0 || mode == 1 || mode == 4) && rnd.nextInt(100) < 30) {
                    buildVariety(q, lvl);
                } else {
                    buildTwo(q, lvl);
                }
                boolean dup = seen.contains(q.key) || (t < 30 && lastKeys.contains(q.key));
                if (!dup) {
                    break;
                }
            }
            seen.add(q.key);
            if (q.sopts == null) {
                fillOptions(q);
            }
            qs[i] = q;
        }
        // Ingat soal dari beberapa permainan terakhir supaya tidak muncul berturut-turut
        lastKeys.addAll(seen);
        while (lastKeys.size() > 40) {
            lastKeys.remove(0);
        }
    }

    // Bentuk soal lain (Kelas 1-3, Tambah/Kurang/Campur): bilangan terbesar/terkecil,
    // bilangan yang hilang di deret, dan membandingkan hasil
    private void buildVariety(Q q, int n) {
        int form = rnd.nextInt(3);
        int top = Math.max(10, grow(ADD_LO[kelas], ADD_HI[kelas], n));
        boolean big = rnd.nextBoolean();
        if (form == 1) {
            int step;
            int start;
            if (kelas == 0) {
                step = rnd.nextInt(3) == 0 ? 2 : 1;
                start = range(1, 10 - 4 * step);
            } else if (kelas == 1) {
                step = range(1, 2);
                start = range(1, 10 - 4 * step);
            } else {
                int[] st = {2, 5, 10, 20, 25};
                step = st[rnd.nextInt(st.length)];
                start = range(1, 300);
            }
            int[] v = new int[5];
            for (int i = 0; i < 5; i++) {
                v[i] = start + i * step;
            }
            boolean down = kelas >= 1 && rnd.nextBoolean();
            if (down) {
                for (int i = 0; i < 2; i++) {
                    int t = v[i];
                    v[i] = v[4 - i];
                    v[4 - i] = t;
                }
            }
            int hide = range(1, 3);
            StringBuilder sb = new StringBuilder();
            StringBuilder fl = new StringBuilder();
            for (int i = 0; i < 5; i++) {
                sb.append(i == hide ? "\u2026" : fmt(v[i])).append(i < 4 ? ", " : "");
                fl.append(fmt(v[i])).append(i < 4 ? ", " : "");
            }
            q.text = sb.toString();
            q.full = fl.toString();
            q.answer = v[hide];
            q.key = "deret" + fl;
            q.expl = "Bilangan di deret ini " + (down ? "turun " : "naik ") + step + " setiap langkah. "
                    + "Jadi yang hilang adalah " + fmt(v[hide]) + ".";
            return;
        }
        // Empat bilangan berjauhan: dipakai untuk "paling besar/kecil" dan untuk membandingkan hasil
        int lo = (form == 2 && mode == 0) ? 2 : 1;
        int hi = (form == 2 && mode == 1) ? top - 1 : top;
        int gap = kelas == 0 ? 2 : Math.max(2, top / 10);
        ArrayList<Integer> nums = new ArrayList<>();
        for (int g = 0; g < 400 && nums.size() < 4; g++) {
            int x = range(lo, hi);
            boolean ok = true;
            for (int y : nums) {
                if (Math.abs(x - y) < (g < 300 ? gap : 1)) {
                    ok = false;
                }
            }
            if (ok) {
                nums.add(x);
            }
        }
        if (nums.size() < 4) {
            nums.clear();
            for (int i = 0; i < 4; i++) {
                nums.add(lo + i);
            }
        }
        ArrayList<Integer> sorted = new ArrayList<>(nums);
        Collections.sort(sorted);
        int target = big ? sorted.get(3) : sorted.get(0);
        Collections.shuffle(nums, rnd);
        q.sopts = new String[4];
        String word = big ? "besar" : "kecil";
        String[] ex = new String[4];
        for (int i = 0; i < 4; i++) {
            int r = nums.get(i);
            if (form == 0) {
                q.sopts[i] = fmt(r);
            } else {
                boolean add = (mode == 0) || (mode == 4 && r >= 2 && (r == top || rnd.nextBoolean()));
                if (mode == 1 || r < 2) {
                    add = false;
                }
                if (add) {
                    int a = range(1, r - 1);
                    ex[i] = fmt(a) + " + " + fmt(r - a);
                } else {
                    int b = range(1, Math.max(1, top - r));
                    ex[i] = fmt(r + b) + " - " + fmt(b);
                }
                q.sopts[i] = ex[i];
            }
            if (r == target) {
                q.correctIdx = i;
            }
        }
        if (form == 0) {
            q.text = "Mana bilangan yang paling " + word + "?";
            q.expl = fmt(target) + " adalah bilangan yang paling " + word + ".";
        } else {
            q.text = "Hasil mana yang paling " + word + "?";
            q.expl = q.sopts[q.correctIdx] + " = " + fmt(target) + ". Itu hasil yang paling " + word + ".";
        }
        q.full = q.text;
        q.key = "bdg" + form + word + sorted;
    }

    private void buildTwo(Q q, int n) {
        int op;
        if (mode == 6) {
            // Isi titik: Kelas 1-2 hanya tambah dan kurang, Kelas 3-6 semua operasi
            op = rnd.nextInt(kelas <= 1 ? 2 : 4);
        } else if (mode == 4) {
            op = rnd.nextInt(kelas <= 1 ? 2 : 4);
        } else {
            op = mode;
        }
        int a;
        int b;
        int ans;
        String sym;
        if (op >= 2) {
            boolean div = (op == 3);
            int fa = range(div ? Math.max(2, MUL_A_LO[kelas]) : MUL_A_LO[kelas],
                    grow(MUL_A_S[kelas], MUL_A_E[kelas], n));
            int fb = range(div ? Math.max(2, MUL_B_LO[kelas]) : MUL_B_LO[kelas],
                    grow(MUL_B_S[kelas], MUL_B_E[kelas], n));
            if (div) {
                a = fa * fb;
                b = Math.min(fa, fb);
                ans = Math.max(fa, fb);
                sym = "\u00F7";
            } else {
                a = fa;
                b = fb;
                ans = fa * fb;
                sym = "\u00D7";
            }
        } else {
            int max = (mode == 6) ? grow(EASY_LO[kelas], EASY_HI[kelas], n)
                    : grow(ADD_LO[kelas], ADD_HI[kelas], n);
            int low = Math.max(1, max / 10);
            if (op == 0) {
                a = range(low, max - low);
                b = range(low, max - a);
                ans = a + b;
                sym = "+";
            } else {
                a = range(2 * low, max);
                b = range(low, a - low);
                ans = a - b;
                sym = "-";
            }
        }
        int lo = Math.min(a, b);
        int hi = Math.max(a, b);
        q.key = (op == 0 || op == 2) ? (lo + sym + hi) : (a + sym + b);
        q.full = fmt(a) + " " + sym + " " + fmt(b) + " = " + fmt(ans);
        if (mode == 6) {
            boolean hideA = rnd.nextBoolean();
            String sa = hideA ? "\u2026" : fmt(a);
            String sb = hideA ? fmt(b) : "\u2026";
            q.text = sa + " " + sym + " " + sb + " = " + fmt(ans);
            q.answer = hideA ? a : b;
        } else {
            q.text = fmt(a) + " " + sym + " " + fmt(b) + " = ?";
            q.answer = ans;
        }
    }

    // Tiga bilangan. Kelas 1-4: tambah dan kurang. Kelas 5-6: ada perkalian (urutan operasi).
    private void buildThree(Q q, int n) {
        String expr;
        int ans;
        if (kelas <= 3) {
            int max = grow(EASY_LO[kelas], EASY_HI[kelas], n);
            int low = Math.max(1, max / 10);
            int form = rnd.nextInt(4);
            int a;
            int b;
            int c;
            if (form == 0) {
                int top = Math.max(low, max / 3);
                a = range(low, top);
                b = range(low, top);
                c = range(low, top);
                ans = a + b + c;
            } else if (form == 1) {
                int top = Math.max(low, max / 2);
                a = range(low, top);
                b = range(low, top);
                c = range(low, a + b - low);
                ans = a + b - c;
            } else if (form == 2) {
                a = range(2 * low, max);
                b = range(low, a - low);
                c = range(low, max - (a - b));
                ans = a - b + c;
            } else {
                a = range(3 * low, max);
                b = range(low, (a - low) / 2);
                c = range(low, a - b - low);
                ans = a - b - c;
            }
            String s1 = (form < 2) ? "+" : "-";
            String s2 = (form % 2 == 0) ? "+" : "-";
            expr = fmt(a) + " " + s1 + " " + fmt(b) + " " + s2 + " " + fmt(c);
        } else {
            int x = range(2, grow(X_S[kelas], X_E[kelas], n));
            int y = range(2, grow(Y_S[kelas], Y_E[kelas], n));
            int zmax = grow(Z_S[kelas], Z_E[kelas], n);
            int p = x * y;
            int z;
            int form = rnd.nextInt(kelas == 4 ? 4 : 6);
            if (form == 0) {
                z = range(2, zmax);
                ans = z + p;
                expr = fmt(z) + " + " + x + " \u00D7 " + y;
                q.trap = (z + x) * y;
            } else if (form == 1) {
                z = range(2, zmax);
                ans = p + z;
                expr = x + " \u00D7 " + y + " + " + fmt(z);
                q.trap = x * (y + z);
            } else if (form == 2) {
                z = range(2, Math.min(zmax, p - 1));
                ans = p - z;
                expr = x + " \u00D7 " + y + " - " + fmt(z);
                q.trap = x * (y - z);
            } else if (form == 3) {
                z = range(p + 1, p + zmax);
                ans = z - p;
                expr = fmt(z) + " - " + x + " \u00D7 " + y;
                q.trap = (z - x) * y;
            } else if (form == 4) {
                z = range(2, zmax);
                ans = (z + x) * y;
                expr = "(" + fmt(z) + " + " + x + ") \u00D7 " + y;
                q.trap = z + x * y;
            } else {
                z = range(x + 1, x + zmax);
                ans = (z - x) * y;
                expr = "(" + fmt(z) + " - " + x + ") \u00D7 " + y;
                q.trap = z - x * y;
            }
        }
        q.key = expr;
        q.text = expr + "\n= ?";
        q.full = expr + " = " + fmt(ans);
        q.answer = ans;
    }

    // Empat pilihan jawaban. Untuk soal ber-urutan-operasi, salah satu pilihan
    // adalah jawaban kalau dihitung dari kiri ke kanan (jebakan).
    private void fillOptions(Q q) {
        int ans = q.answer;
        int spread = kelas <= 1 ? Math.max(8, ans) : Math.max(5, ans / 10);
        int pos = rnd.nextInt(4);
        Arrays.fill(q.opts, -1);
        q.opts[pos] = ans;
        q.correctIdx = pos;
        boolean needTrap = q.trap >= 1 && q.trap != ans;
        for (int k = 0; k < 4; k++) {
            if (k == pos) {
                continue;
            }
            if (needTrap) {
                q.opts[k] = q.trap;
                needTrap = false;
                continue;
            }
            int v;
            boolean bad;
            int tries = 0;
            // Kelas 1-2: pilihan berjauhan (selisih minimal 2) dan tidak melewati batas bilangan kelas
            int cap = (kelas <= 1 && ans <= 10) ? 10 : Integer.MAX_VALUE;
            do {
                tries++;
                if (tries < 200) {
                    v = range(Math.max(1, ans - spread), Math.min(cap, ans + spread));
                } else {
                    v = ans + rnd.nextInt(2 * spread + 1) - spread;
                }
                bad = (v < 1 || v == ans);
                int gap = (kelas <= 1 && tries < 40) ? 2 : 1;
                for (int j = 0; j < 4 && !bad; j++) {
                    if (q.opts[j] != -1 && Math.abs(q.opts[j] - v) < gap) {
                        bad = true;
                    }
                }
            } while (bad);
            q.opts[k] = v;
        }
    }
    // ---------- Layar giliran (multi pemain) ----------

    private void showPass() {
        handler.removeCallbacksAndMessages(null);
        setScreen(3);
        LinearLayout c = column();
        c.setGravity(Gravity.CENTER);

        TextView av = text(av(cur), 60, Color.WHITE, false);
        av.setBackground(box(PLAYER_COLORS[cur], 60));
        av.setElevation(dp(6));
        c.addView(av, new LinearLayout.LayoutParams(dp(120), dp(120)));

        c.addView(text(cur == 0 ? "Giliran pertama" : "Giliran berikutnya", 16, DARK, false),
                lp(WRAP, 18));
        c.addView(text(names[cur], 34, PLAYER_COLORS[cur], true), lp(WRAP, 4));
        c.addView(text("Pemain " + (cur + 1) + " dari " + np, 14, DARK, false), lp(WRAP, 4));
        c.addView(pill(KELAS_NAMES[kelas] + " \u2022 " + modeLabel(), KELAS_COLOR[kelas]), lpw(14));

        if (cur > 0) {
            LinearLayout board = card();
            board.addView(heading("Skor sementara"), lp(WRAP, 0));
            for (int i = 0; i < cur; i++) {
                board.addView(text(av(i) + " " + names[i] + ":  " + pScore[i] + " poin",
                        15, DARK, false), lp(WRAP, 6));
            }
            c.addView(board, lp(WRAP, 18));
        }

        Button go = button("Mulai!", PLAYER_COLORS[cur], 24);
        go.setElevation(dp(4));
        c.addView(go, lp(dp(66), 24));
        go.setOnClickListener(v -> startTurn());

        Button cancel = button("Batal", GRAY, 14);
        cancel.setTextColor(DARK);
        c.addView(cancel, lp(dp(46), 14));
        cancel.setOnClickListener(v -> showMenu());

        wrap(c);
    }

    // ---------- Permainan ----------

    // Jeda saat app ke background atau dialog tampil, lanjut saat kembali
    private void pauseGame() {
        if (screen != 1 || gamePaused) {
            return;
        }
        gamePaused = true;
        resumeNext = locked;
        handler.removeCallbacksAndMessages(null);
        if (timed && timerFill != null) {
            timerFill.animate().cancel();
        }
    }

    private void resumeGame() {
        if (screen != 1 || !gamePaused || dialogOpen) {
            return;
        }
        gamePaused = false;
        if (resumeNext) {
            resumeNext = false;
            handler.postDelayed(this::showQuestion, 600);
        } else if (timed && timeLeft > 0) {
            timerFill.setScaleX(timeLeft / (float) secsNow());
            timerFill.animate().scaleX(0f).setDuration(timeLeft * 1000L)
                    .setInterpolator(new LinearInterpolator()).start();
            handler.postDelayed(tick, 1000);
        }
    }

    private void confirmExit() {
        pauseGame();
        dialogOpen = true;
        exitDialog = new AlertDialog.Builder(this)
                .setTitle("Keluar dari permainan?")
                .setMessage("Skor permainan ini tidak akan tersimpan.")
                .setPositiveButton("Keluar", (dl, w) -> {
                    if (advLevel >= 0) {
                        showPeta();
                    } else if (sambungOn()) {
                        kebunNoGrow = true;
                        showSambung();
                    } else {
                        showMenu();
                    }
                })
                .setNegativeButton("Lanjut main", null)
                .setOnDismissListener(dl -> {
                    dialogOpen = false;
                    resumeGame();
                })
                .show();
    }

    private void startTurn() {
        qi = 0;
        score = 0;
        correct = 0;
        lives = noLives() ? 99 : KELAS_LIVES[kelas];
        showGame();
        showQuestion();
    }

    private void showGame() {
        setScreen(1);
        gamePaused = false;
        resumeNext = false;
        LinearLayout c = column();
        final boolean wide = isWide();
        LinearLayout cr = wide ? column() : null;
        LinearLayout rt = wide ? cr : c;

        // Baris atas: nama pemain dan nyawa
        LinearLayout top = new LinearLayout(this);
        top.setOrientation(LinearLayout.HORIZONTAL);
        top.setGravity(Gravity.CENTER_VERTICAL);
        c.addView(top, lp(WRAP, 0));
        String who = (np > 1) ? av(cur) + " " + names[cur]
                : (advLevel >= 0 ? av(0) + " Level " + (advLevel + 1) : av(0) + " Petualang");
        if (plainUi) {
            TextView bk = text("\u2190", 20, Color.parseColor("#285A35"), true);
            bk.setBackground(rr(Color.parseColor("#FFF8E6"), Color.parseColor("#EADFC0"), 1, 22));
            bk.setElevation(dp(3));
            pressable(bk);
            bk.setOnClickListener(v -> confirmExit());
            LinearLayout.LayoutParams bkp = new LinearLayout.LayoutParams(dp(44), dp(44));
            bkp.rightMargin = dp(10);
            top.addView(bk, bkp);
        }
        top.addView(pill(who, np > 1 ? PLAYER_COLORS[cur] : (plainUi ? Color.parseColor("#3E8E41") : ORANGE)),
                new LinearLayout.LayoutParams(WRAP, WRAP));
        top.addView(new View(this), new LinearLayout.LayoutParams(0, 1, 1f));
        tvHearts = text("", 18, DARK, false);
        tvHearts.setBackground(premiumSurface(18));
        tvHearts.setPadding(dp(10), dp(5), dp(10), dp(5));
        tvHearts.setElevation(dp(3));
        top.addView(tvHearts, new LinearLayout.LayoutParams(WRAP, WRAP));

        // Penanda kemajuan soal di dalam lintasan lembut
        LinearLayout dotsRow = new LinearLayout(this);
        dotsRow.setOrientation(LinearLayout.HORIZONTAL);
        dotsRow.setGravity(Gravity.CENTER_VERTICAL);
        dotsRow.setPadding(dp(6), dp(4), dp(6), dp(4));
        dotsRow.setBackground(premiumSurface(10));
        c.addView(dotsRow, lp(dp(16), 14));
        for (int i = 0; i < total; i++) {
            View d = new View(this);
            d.setBackground(box(GRAY_D, 5));
            dotsRow.addView(d, weight(i == 0 ? 0 : 2, i == total - 1 ? 0 : 2));
            dots[i] = d;
        }

        // Nomor soal dan skor
        LinearLayout info = new LinearLayout(this);
        info.setOrientation(LinearLayout.HORIZONTAL);
        info.setGravity(Gravity.CENTER_VERTICAL);
        info.setBackground(premiumSurface(18));
        info.setPadding(dp(14), dp(9), dp(14), dp(9));
        info.setElevation(dp(3));
        c.addView(info, lp(WRAP, 10));
        tvStep = text("", 15, INK, true);
        tvStep.setGravity(Gravity.START);
        info.addView(tvStep, new LinearLayout.LayoutParams(0, WRAP, 1f));
        tvScore = text("", 15, GOLD_DEEP, true);
        tvScore.setGravity(Gravity.END);
        info.addView(tvScore, new LinearLayout.LayoutParams(0, WRAP, 1f));

        // Penghitung waktu
        tvTimer = text(" ", 15, INK, true);
        c.addView(tvTimer, lp(WRAP, 10));
        timerBox = new FrameLayout(this);
        GradientDrawable track = box(GRAY, 8);
        track.setStroke(dp(1), HAIRLINE);
        timerBox.setBackground(track);
        timerFill = new View(this);
        timerFill.setBackground(timerBg(GREEN));
        timerFill.setPivotX(0f);
        timerBox.addView(timerFill, new FrameLayout.LayoutParams(MATCH, MATCH));
        c.addView(timerBox, lp(dp(12), 6));
        if (!timed) {
            tvTimer.setVisibility(View.GONE);
            timerBox.setVisibility(View.GONE);
        }

        // Kartu soal
        cardQ = new LinearLayout(this);
        cardQ.setOrientation(LinearLayout.VERTICAL);
        cardQ.setGravity(Gravity.CENTER);
        cardQ.setBackground(plainUi ? sambungCardBg() : premiumSurface(28));
        cardQ.setElevation(dp(8));
        cardQ.setPadding(dp(18), dp(plainUi ? 22 : 30), dp(18), dp(30));
        if (plainUi) {
            TextView tag = text(urut ? "\u2726  URUTKAN AYAT  \u2726" : "\u2726  SAMBUNG AYAT  \u2726", 11, GOLD_DEEP, true);
            tag.setLetterSpacing(0.12f);
            LinearLayout.LayoutParams tagp = new LinearLayout.LayoutParams(WRAP, WRAP);
            tagp.bottomMargin = dp(14);
            cardQ.addView(tag, tagp);
        }
        tvQuestion = text("", 46, plainUi ? Color.parseColor("#1F5A2E") : ORANGE_DARK, true);
        cardQ.addView(tvQuestion, new LinearLayout.LayoutParams(MATCH, WRAP));
        c.addView(cardQ, lp(WRAP, 18));

        tvFeedback = text(" ", 18, DARK, true);
        c.addView(tvFeedback, lp(WRAP, 12));

        // Pilihan jawaban
        for (int r = 0; r < 2; r++) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            rt.addView(row, lp(dp(wide ? 96 : 84), 12));
            for (int k = 0; k < 2; k++) {
                Button b = button("", OPT_COLORS[r * 2 + k], 28);
                b.setTag("opt");
                b.setElevation(dp(4));
                row.addView(b, weight(k == 0 ? 0 : 6, k == 0 ? 6 : 0));
                opts[r * 2 + k] = b;
            }
        }

        Button back = button(advLevel >= 0 ? "Kembali ke peta" : (sambungOn() ? "Kembali ke Kebun" : "Kembali ke menu"), GRAY, 14);
        back.setTextColor(GOLD_DEEP);
        back.setBackground(ghostButtonBg());
        rt.addView(back, lp(dp(46), 22));
        back.setOnClickListener(v -> confirmExit());

        if (wide) {
            wrapTwo(c, cr);
        } else if (plainUi) {
            wrapBg(c, sambungScreenBg());
        } else {
            wrap(c);
        }
    }

    private GradientDrawable sambungScreenBg() {
        return new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{Color.parseColor("#FFFDF7"), Color.parseColor("#F1F7E6"), Color.parseColor("#E4F1D3")});
    }

    private GradientDrawable sambungCardBg() {
        GradientDrawable g = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{Color.WHITE, Color.parseColor("#FFF4D6")});
        g.setCornerRadius(dp(28));
        g.setStroke(dp(2), Color.parseColor("#E8C766"));
        return g;
    }

    private int sambungOptSize(int len) {
        return len <= 10 ? 24 : len <= 24 ? 19 : len <= 40 ? 16 : 14;
    }

    private void showQuestion() {
        if (qi >= total || lives <= 0) {
            endTurn();
            return;
        }
        Q q = qs[qi];
        qi++;
        locked = false;

        tvQuestion.setText(q.text);
        int nl = q.text.indexOf('\n');
        int qsz = questionSize(nl >= 0 ? nl : q.text.length());
        tvQuestion.setTextSize((q.sopts != null ? Math.min(qsz, 24) : qsz) * ui);
        tvFeedback.setText(mode == 6 ? "Isi titik-titiknya!" : " ");
        tvFeedback.setTextColor(DARK);
        for (int i = 0; i < 4; i++) {
            final int idx = i;
            String ot = optText(q, i);
            opts[i].setText(ot);
            opts[i].setTextSize((q.sopts != null ? (plainUi ? sambungOptSize(ot.length()) : bankOptionSize(ot.length()))
                    : optionSize(ot.length())) * ui);
            opts[i].setBackground(optionBg(OPT_COLORS[i]));
            if (plainUi) {
                opts[i].setTextColor(Color.parseColor("#1F2A24"));
            }
            opts[i].setAlpha(1f);
            opts[i].setOnClickListener(v -> choose(idx));
        }
        correctBtn = opts[q.correctIdx];
        dots[qi - 1].setBackground(box(ORANGE, 5));

        cardQ.animate().cancel();
        cardQ.setTranslationX(dp(70));
        cardQ.setScaleX(0.92f);
        cardQ.setScaleY(0.92f);
        cardQ.setRotation(3f);
        cardQ.setAlpha(0f);
        cardQ.animate().setStartDelay(0).translationX(0f).scaleX(1f).scaleY(1f).rotation(0f).alpha(1f)
                .setDuration(340).setInterpolator(new OvershootInterpolator(0.9f)).start();
        for (int i = 0; i < 4; i++) {
            opts[i].animate().cancel();
            opts[i].setScaleX(0.6f);
            opts[i].setScaleY(0.6f);
            opts[i].setAlpha(0f);
            opts[i].animate().scaleX(1f).scaleY(1f).alpha(1f).setStartDelay(120 + 60L * i)
                    .setDuration(260).setInterpolator(new OvershootInterpolator(1.6f)).start();
        }

        handler.removeCallbacks(tick);
        if (timed) {
            timeLeft = secsNow();
            showTime();
            timerFill.animate().cancel();
            timerFill.setScaleX(1f);
            timerFill.animate().scaleX(0f).setDuration(secsNow() * 1000L)
                    .setInterpolator(new LinearInterpolator()).start();
            handler.postDelayed(tick, 1000);
        }
        updateHud();
    }

    private void showTime() {
        tvTimer.setText("Sisa waktu: " + timeLeft + " detik");
        tvTimer.setTextColor(timeLeft <= 3 ? RED : INK);
        timerFill.setBackground(timerBg(timeLeft <= 3 ? RED : GREEN));
    }

    private void dimOthers(int keep1, int keep2) {
        for (int i = 0; i < 4; i++) {
            opts[i].animate().cancel();
            opts[i].setScaleX(1f);
            opts[i].setScaleY(1f);
            opts[i].setAlpha(i == keep1 || i == keep2 ? 1f : 0.45f);
        }
    }

    private void choose(int idx) {
        if (locked) {
            return;
        }
        locked = true;
        handler.removeCallbacks(tick);
        if (timed) {
            timerFill.animate().cancel();
        }
        Q q = qs[qi - 1];
        pPick[cur][qi - 1] = idx;
        pRes[cur][qi - 1] = (idx == q.correctIdx) ? 1 : 2;
        recordAnswer(q, idx == q.correctIdx);
        dimOthers(idx, q.correctIdx);
        if (idx == q.correctIdx) {
            score += 10;
            correct++;
            opts[idx].setBackground(optionBg(GREEN));
            dots[qi - 1].setBackground(box(GREEN, 5));
            tvFeedback.setText("Benar! \uD83C\uDF89");
            tvFeedback.setTextColor(GREEN);
            sfx(SFX_CORRECT);
            NagaPetView.reactAll(true);
            buzz(25);
            pop(opts[idx], 1.12f);
            pop(tvFeedback, 1.25f);
            pop(tvScore, 1.45f);
        } else {
            lives--;
            opts[idx].setBackground(optionBg(RED));
            correctBtn.setBackground(optionBg(GREEN));
            dots[qi - 1].setBackground(box(RED, 5));
            tvFeedback.setText("Oops! Jawabannya " + optText(q, q.correctIdx));
            tvFeedback.setTextColor(RED);
            sfx(SFX_WRONG);
            NagaPetView.reactAll(false);
            buzz(120);
            shake(opts[idx]);
            shake(cardQ);
            shake(tvHearts);
            pop(correctBtn, 1.1f);
        }
        updateHud();
        exitCard();
        handler.postDelayed(this::showQuestion, 1000);
    }

    private void timeout() {
        locked = true;
        lives--;
        Q q = qs[qi - 1];
        timerFill.animate().cancel();
        timerFill.setScaleX(0f);
        pRes[cur][qi - 1] = 3;
        recordAnswer(q, false);
        tvTimer.setText("Waktu habis!");
        tvTimer.setTextColor(RED);
        dimOthers(q.correctIdx, q.correctIdx);
        correctBtn.setBackground(optionBg(GREEN));
        dots[qi - 1].setBackground(box(RED, 5));
        tvFeedback.setText("Jawabannya " + optText(q, q.correctIdx));
        tvFeedback.setTextColor(RED);
        sfx(SFX_TIMEOUT);
        NagaPetView.reactAll(false);
        buzz(150);
        shake(cardQ);
        shake(tvHearts);
        pop(correctBtn, 1.1f);
        updateHud();
        exitCard();
        handler.postDelayed(this::showQuestion, 1000);
    }

    private void updateHud() {
        StringBuilder h = new StringBuilder();
        for (int i = 0; !noLives() && i < lives; i++) {
            h.append("\u2764\uFE0F");
        }
        if (relaxed && mode != 7 && advLevel < 0) {
            h.append("\uD83D\uDC9A Santai");
        }
        tvHearts.setText(h.toString());
        tvHearts.setVisibility(h.length() == 0 ? View.GONE : View.VISIBLE);
        tvStep.setText("Soal " + qi + " dari " + total);
        tvScore.setText("\u2B50 " + score);
    }

    private void endTurn() {
        saveStats();
        pScore[cur] = score;
        pCorrect[cur] = correct;
        cur++;
        if (cur < np) {
            showPass();
        } else if (np == 1) {
            showSoloResult();
        } else {
            freshBadges = awardBadges(1, 0, false, false);
            showRanking(true);
        }
    }

    // ---------- Hasil ----------

    private void showSoloResult() {
        if (advLevel >= 0) {
            showAdvResult();
            return;
        }
        setScreen(2);
        String key = bestKey();
        int best = prefs.getInt(key, 0);
        rekorNow = countsForRecord() && pScore[0] > best;
        if (rekorNow) {
            prefs.edit().putInt(key, pScore[0]).apply();
        }
        boolean perfect = total >= 5 && pCorrect[0] == total;
        freshBadges = awardBadges(0, soloStars(), perfect, rekorNow && best > 0);
        renderSoloResult(true);
    }

    // Bintang permainan solo: 3 bintang kalau benar 90% ke atas
    private int soloStars() {
        int pct = total > 0 ? pCorrect[0] * 10 / total : 0;
        return pct >= 9 ? 3 : pct >= 6 ? 2 : pct >= 3 ? 1 : 0;
    }

    private void renderSoloResult() {
        if (advLevel >= 0) {
            renderAdvResult(false);
            return;
        }
        renderSoloResult(false);
    }

    private void renderSoloResult(boolean fx) {
        setScreen(2);
        boolean rekor = rekorNow;
        int stars = soloStars();

        LinearLayout c = column();
        c.setGravity(Gravity.CENTER);
        TextView avt = text(av(0), 40, Color.WHITE, false);
        avt.setBackground(medallionBg(KELAS_COLOR[kelas], 3));
        avt.setElevation(dp(6));
        c.addView(avt, new LinearLayout.LayoutParams(dp(86), dp(86)));
        TextView title = text(lives <= 0 ? "Nyawa habis!" : "Selesai!", 32, INK, true);
        c.addView(title, lp(WRAP, 10));
        c.addView(pill(KELAS_NAMES[kelas] + " \u2022 " + modeLabel(), KELAS_COLOR[kelas]), lpw(10));

        LinearLayout card = card();
        card.setGravity(Gravity.CENTER_HORIZONTAL);
        LinearLayout starRow = new LinearLayout(this);
        starRow.setOrientation(LinearLayout.HORIZONTAL);
        starRow.setGravity(Gravity.CENTER);
        TextView[] st = new TextView[3];
        for (int i = 0; i < 3; i++) {
            st[i] = text(i < stars ? "\u2B50" : "\u2606", 46, i < stars ? ORANGE : Color.parseColor("#D9C7A0"), false);
            starRow.addView(st[i], new LinearLayout.LayoutParams(WRAP, WRAP));
        }
        card.addView(starRow, lp(WRAP, 0));
        TextView sc = text("Skor: " + pScore[0], 32, INK, true);
        card.addView(sc, lp(WRAP, 10));
        TextView ben = text("Benar " + pCorrect[0] + " dari " + total, 16, INK_SOFT, false);
        card.addView(ben, lp(WRAP, 6));
        card.addView(text("\uD83C\uDF1F Total bintang: " + totalStars(), 14, GOLD_DEEP, true), lp(WRAP, 6));
        if (!countsForRecord()) {
            card.addView(text("Mode santai: skor tidak masuk rekor", 12, INK_SOFT, false), lp(WRAP, 4));
        }
        TextView rekorPill = null;
        if (rekor) {
            rekorPill = pill("Rekor baru! \uD83C\uDFC6", PURPLE);
            card.addView(rekorPill, lpw(12));
        }
        c.addView(card, lp(WRAP, 18));

        addResultButtons(c);
        wrap(c);
        if (fx) {
            playResultFx(title, st, stars, sc, ben, rekorPill, rekor);
        }
    }

    private void showRanking() {
        showRanking(false);
    }

    private void showRanking(boolean fx) {
        setScreen(2);
        Integer[] order = new Integer[np];
        for (int i = 0; i < np; i++) {
            order[i] = i;
        }
        Arrays.sort(order, (x, y) -> {
            int a = x;
            int b = y;
            if (pScore[a] != pScore[b]) {
                return pScore[b] - pScore[a];
            }
            return pCorrect[b] - pCorrect[a];
        });

        int top = pScore[order[0]];
        int winners = 0;
        for (int i = 0; i < np; i++) {
            if (pScore[i] == top) {
                winners++;
            }
        }

        LinearLayout c = column();
        c.setGravity(Gravity.CENTER);
        c.addView(text("Hasil Akhir", 32, INK, true), lp(WRAP, 0));
        c.addView(pill(KELAS_NAMES[kelas] + " \u2022 " + modeLabel(), KELAS_COLOR[kelas]), lpw(10));

        TextView banner = text(winners > 1 ? "Seri! Skor sama \uD83E\uDD1D"
                : "\uD83C\uDFC6 " + names[order[0]] + " menang!", 22,
                winners > 1 ? Color.WHITE : INK, true);
        banner.setBackground(premiumButtonBg(winners > 1 ? PURPLE : GOLD, 20));
        banner.setPadding(dp(16), dp(16), dp(16), dp(16));
        banner.setElevation(dp(6));
        c.addView(banner, lp(WRAP, 18));
        if (fx) {
            banner.setScaleX(0f);
            banner.setScaleY(0f);
            banner.animate().scaleX(1f).scaleY(1f).setStartDelay(150).setDuration(550)
                    .setInterpolator(new OvershootInterpolator(2.5f)).start();
        }

        int[] rankColor = {GOLD, SILVER, BRONZE, GRAY_D};
        for (int r = 0; r < np; r++) {
            int p = order[r];
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setBackground(premiumSurface(18));
            row.setElevation(dp(4));
            row.setPadding(dp(12), dp(10), dp(14), dp(10));

            TextView rank = text(String.valueOf(r + 1), 18, r == 0 ? INK : Color.WHITE, true);
            rank.setBackground(medallionBg(rankColor[r], 2));
            row.addView(rank, new LinearLayout.LayoutParams(dp(38), dp(38)));

            TextView nm = text(av(p) + " " + names[p], 18, INK, true);
            nm.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
            LinearLayout.LayoutParams nmp = new LinearLayout.LayoutParams(0, WRAP, 1f);
            nmp.leftMargin = dp(12);
            row.addView(nm, nmp);

            TextView sc = text(pScore[p] + " poin\nbenar " + pCorrect[p] + "/" + total,
                    14, GOLD_DEEP, true);
            sc.setGravity(Gravity.END);
            row.addView(sc, new LinearLayout.LayoutParams(WRAP, WRAP));

            if (fx) {
                row.setAlpha(0f);
                row.setTranslationX(dp(90));
                row.animate().alpha(1f).translationX(0f).setStartDelay(500 + 180L * r).setDuration(380)
                        .setInterpolator(new DecelerateInterpolator(1.6f)).start();
            }
            c.addView(row, lp(WRAP, 10));
        }

        addResultButtons(c);
        wrap(c);
        if (fx) {
            handler.postDelayed(() -> {
                sfx(SFX_WIN);
                buzz(60);
                launchConfetti(130);
            }, 400);
        }
    }

    private boolean hasMistakes() {
        for (int p = 0; p < np; p++) {
            for (int i = 0; i < total; i++) {
                if (pRes[p][i] != 1) {
                    return true;
                }
            }
        }
        return false;
    }

    // ---------- Pembahasan ----------

    private void showReview() {
        handler.removeCallbacksAndMessages(null);
        setScreen(5);
        int p = reviewPlayer;

        LinearLayout c = column();
        c.setGravity(Gravity.CENTER_HORIZONTAL);
        c.addView(text("Pembahasan", 30, INK, true), lp(WRAP, 0));
        c.addView(pill(KELAS_NAMES[kelas] + " \u2022 " + modeLabel(), KELAS_COLOR[kelas]), lpw(10));

        if (np > 1) {
            String[] labels = new String[np];
            int[] cols = new int[np];
            for (int i = 0; i < np; i++) {
                labels[i] = names[i];
                cols[i] = PLAYER_COLORS[i];
            }
            c.addView(chips(labels, p, cols, k -> {
                reviewPlayer = k;
                showReview();
            }), lp(dp(46), 16));
        }

        int wrong = 0;
        int skipped = 0;
        for (int i = 0; i < total; i++) {
            if (pRes[p][i] == 2 || pRes[p][i] == 3) {
                wrong++;
            } else if (pRes[p][i] == 0) {
                skipped++;
            }
        }

        if (wrong == 0) {
            LinearLayout ok = card();
            ok.setGravity(Gravity.CENTER_HORIZONTAL);
            ok.addView(text("\uD83C\uDF89", 44, GREEN, false), lp(WRAP, 0));
            ok.addView(text(skipped == 0 ? "Semua jawabanmu benar!"
                    : "Semua jawaban yang sempat dikerjakan benar!", 18, GREEN, true), lp(WRAP, 8));
            c.addView(ok, lp(WRAP, 16));
        } else {
            c.addView(text("Ini soal yang masih salah. Yuk, lihat jawaban yang benar.", 14, INK_SOFT, false),
                    lp(WRAP, 14));
        }

        for (int i = 0; i < total; i++) {
            int r = pRes[p][i];
            if (r != 2 && r != 3) {
                continue;
            }
            Q q = qs[i];
            LinearLayout cd = card();
            TextView no = text("Soal " + (i + 1), 13, DARK, false);
            no.setGravity(Gravity.START);
            cd.addView(no, lp(WRAP, 0));
            cd.addView(text(q.text.replace("\n", " "), q.sopts != null ? 18 : 22, ORANGE_DARK, true),
          lp(WRAP, 6));
            if (r == 3) {
                cd.addView(text("Waktu habis \u23F0", 15, RED, true), lp(WRAP, 10));
            } else {
                cd.addView(text("Jawabanmu: " + optText(q, pPick[p][i]), 15, RED, true), lp(WRAP, 10));
            }
            cd.addView(text("Jawaban yang benar: " + optText(q, q.correctIdx), 16, GREEN, true), lp(WRAP, 4));
            if (mode == 6) {
                cd.addView(text("Jadi: " + q.full, 15, DARK, false), lp(WRAP, 4));
            }
            if (q.expl != null && !q.expl.isEmpty()) {
                cd.addView(text("Pembahasan: " + q.expl, 14, DARK, false), lp(WRAP, 8));
            }
            if (r == 2 && q.trap >= 1 && q.opts[pPick[p][i]] == q.trap) {
                cd.addView(text("Ingat: kerjakan tanda kurung atau perkalian dulu.", 13, PURPLE, true),
                        lp(WRAP, 8));
            }
            c.addView(cd, lp(WRAP, 12));
        }

        if (skipped > 0) {
            c.addView(text(skipped + " soal belum sempat dikerjakan karena nyawa habis.", 13, DARK, false),
                    lp(WRAP, 14));
        }

        Button back = button("Kembali ke hasil", ORANGE, 18);
        back.setElevation(dp(4));
        c.addView(back, lp(dp(56), 22));
        back.setOnClickListener(v -> {
            if (np == 1) {
                renderSoloResult();
            } else {
                showRanking();
            }
        });

        wrap(c);
    }

    private void addResultButtons(LinearLayout c) {
        addFreshBadges(c);
        boolean mistakes = hasMistakes();
        if (mistakes) {
            Button rev = button("Lihat Pembahasan", BLUE, 20);
            rev.setBackground(ghostButtonBg());
            rev.setTextColor(BLUE);
            rev.setElevation(dp(2));
            c.addView(rev, lp(dp(60), 22));
            rev.setOnClickListener(v -> {
                reviewPlayer = 0;
                showReview();
            });
        }
        Button again = button("Main Lagi", GREEN, 22);
        again.setElevation(dp(6));
        c.addView(again, lp(dp(64), mistakes ? 12 : 26));
        again.setOnClickListener(v -> startGame(mode));

        final boolean toKebun = sambungOn();
        Button menu = button(toKebun ? "Kembali ke Kebun" : "Menu Utama", ORANGE, 20);
        menu.setBackground(ghostButtonBg());
        menu.setTextColor(ORANGE_DARK);
        menu.setElevation(dp(2));
        c.addView(menu, lp(dp(60), 12));
        menu.setOnClickListener(v -> {
            if (toKebun) {
                kebunNoGrow = true;
                showSambung();
            } else {
                showMenu();
            }
        });
    }

    // =====================================================================
    // Peta Petualangan (versi 6.0): level terbuka bertahap, latihan per topik, bank soal bawaan
    // =====================================================================

    private static final String[] WORLD_NAME = {
            "Hutan Ceria", "Pantai Pelangi", "Gunung Berani", "Samudra Biru", "Gurun Misteri", "Angkasa Raya"
    };
    private static final String[] WORLD_ICON = {"\uD83C\uDF33", "\uD83C\uDFD6\uFE0F", "\u26F0\uFE0F", "\uD83C\uDF0A", "\uD83C\uDFDC\uFE0F", "\uD83D\uDE80"};
    private static final String[] WORLD_PAL = {"#2E7D32", "#00838F", "#6D4C41", "#1565C0", "#E65100", "#4527A0"};
    private static final String[] WORLD_PAL2 = {"#81C784", "#4DD0E1", "#BCAAA4", "#64B5F6", "#FFB74D", "#9575CD"};
    private static final String[] BUDDY_ICON = {"\uD83D\uDC3F\uFE0F", "\uD83E\uDD80", "\uD83E\uDD85", "\uD83D\uDC2C", "\uD83D\uDC2A", "\uD83E\uDD16"};
    private static final String[] BUDDY_NAME = {
            "Tupai Tito", "Kepiting Kiki", "Elang Elo", "Lumba Lulu", "Unta Uni", "Robo Rama"
    };
    private static final String[] MODE_DESC = {
            "Latihan penjumlahan", "Latihan pengurangan", "Latihan perkalian", "Latihan pembagian",
            "Campuran tambah, kurang, kali, dan bagi", "Hitung tiga bilangan sekaligus", "Isi titik yang hilang"
    };

    // Daftar level per kelas: ikon|judul|jenis|parameter
    // jenis O = latihan hitung (parameter = nomor permainan), T = latihan tabel perkalian,
    // B = kuis bawaan (pelajaran,bagian), M = Cocokkan Pasangan (pelajaran)
    private static final String[][] LEVELS = {
            {
                    "\u2795|Tambah|O|0", "\u2796|Kurang|O|1", "\uD83D\uDCD6|Kuis Indonesia|B|1,0", "\u2753|Isi Titik|O|6",
                    "\uD83C\uDF0D|Kuis English|B|2,0", "\uD83E\uDDE9|Lawan Kata|M|1", "\uD83D\uDD22|Tiga Bilangan|O|5",
                    "\uD83D\uDCDA|Kuis Indonesia 2|B|1,1", "\uD83C\uDF08|Kuis English 2|B|2,1", "\uD83D\uDC51|Raja Hutan|O|4"
            },
            {
                    "\u2795|Tambah|O|0", "\u2796|Kurang|O|1", "\u2753|Isi Titik|O|6", "\uD83D\uDCD6|Kuis Indonesia|B|1,0",
                    "\uD83D\uDD22|Tiga Bilangan|O|5", "\uD83C\uDF0D|Kuis English|B|2,0", "\uD83C\uDF08|Kuis English 2|B|2,1", "\uD83E\uDDE9|Lawan Kata|M|1",
                    "\uD83D\uDCDA|Kuis Indonesia 2|B|1,1", "\uD83D\uDC51|Ratu Pantai|O|4"
            },
            {
                    "\u2795|Tambah|O|0", "\u2796|Kurang|O|1", "\u2716\uFE0F|Tabel 3|T|3", "\uD83D\uDCD6|Kuis Indonesia|B|1,0",
                    "\u2716\uFE0F|Tabel 4|T|4", "\uD83C\uDF0D|Kuis English|B|2,0", "\u2716\uFE0F|Tabel 6|T|6", "\uD83E\uDDE9|Pasangan Kata|M|2",
                    "\u2797|Bagi|O|3", "\uD83D\uDCDA|Kuis Indonesia 2|B|1,1", "\uD83C\uDF08|Kuis English 2|B|2,1", "\uD83D\uDC51|Penguasa Gunung|O|4"
            },
            {
                    "\u2795|Tambah|O|0", "\u2796|Kurang|O|1", "\u2716\uFE0F|Tabel 7|T|7", "\uD83D\uDCD6|Kuis Indonesia|B|1,0",
                    "\u2716\uFE0F|Tabel 8|T|8", "\uD83C\uDF0D|Kuis English|B|2,0", "\u2716\uFE0F|Tabel 9|T|9", "\u2797|Bagi|O|3",
                    "\uD83D\uDD22|Tiga Bilangan|O|5", "\uD83D\uDCDA|Kuis Indonesia 2|B|1,1", "\uD83C\uDF08|Kuis English 2|B|2,1", "\uD83D\uDC51|Raja Samudra|O|4"
            },
            {
                    "\u2716\uFE0F|Kali|O|2", "\u2797|Bagi|O|3", "\u2716\uFE0F|Tabel 11|T|11", "\uD83D\uDCD6|Kuis Indonesia|B|1,0",
                    "\u2716\uFE0F|Tabel 15|T|15", "\uD83C\uDF0D|Kuis English|B|2,0", "\u2795|Tambah|O|0", "\u2796|Kurang|O|1",
                    "\uD83D\uDD22|Tiga Bilangan|O|5", "\uD83D\uDCDA|Kuis Indonesia 2|B|1,1", "\uD83C\uDF08|Kuis English 2|B|2,1", "\uD83D\uDC51|Penjaga Gurun|O|4"
            },
            {
                    "\u2716\uFE0F|Kali|O|2", "\u2797|Bagi|O|3", "\u2716\uFE0F|Tabel 12|T|12", "\uD83D\uDCD6|Kuis Indonesia|B|1,0",
                    "\u2716\uFE0F|Tabel 25|T|25", "\uD83C\uDF0D|Kuis English|B|2,0", "\u2795|Tambah|O|0", "\u2796|Kurang|O|1",
                    "\uD83D\uDD22|Tiga Bilangan|O|5", "\uD83D\uDCDA|Kuis Indonesia 2|B|1,1", "\uD83C\uDF08|Kuis English 2|B|2,1", "\uD83D\uDC51|Komandan Angkasa|O|4"
            }
    };

    // Bank soal bawaan (buatan Claude, perlu dicek guru kelas).
    // Format: pertanyaan|jawaban benar|salah 1|salah 2|salah 3|pembahasan (boleh kosong)
    // 16 soal per kelas; 8 soal pertama = bagian 0, 8 soal berikutnya = bagian 1.
    private static final String[][] BI_ID = {
        {
            "Lawan kata 'besar' adalah ...|kecil|tinggi|jauh|baru",
            "Kata yang diawali huruf m adalah ...|meja|buku|kursi|pintu|Kata meja diawali huruf m.",
            "Hewan yang bisa terbang adalah ...|burung|ikan|kucing|sapi|Burung punya sayap sehingga bisa terbang.",
            "Kita mencuci tangan dengan ...|sabun|nasi|buku|sepatu|Sabun membantu membersihkan tangan dari kuman.",
            "Ibu memasak di ...|dapur|kamar|kebun|kelas|Dapur adalah tempat memasak.",
            "Pagi hari kita berangkat ke ...|sekolah|gunung|laut|bulan|Anak-anak berangkat ke sekolah di pagi hari.",
            "Penulisan yang benar adalah ...|sekolah|sekolh|skolah|sekola|Tulisan yang benar: sekolah.",
            "Lawan kata 'panas' adalah ...|dingin|hangat|api|masak",
            "Kata 'apel' diawali huruf ...|a|p|e|l|Apel diawali huruf a.",
            "Daun yang segar biasanya berwarna ...|hijau|merah|biru|ungu|Daun segar berwarna hijau.",
            "Kakak itu saudara yang usianya ... daripada kita.|lebih tua|lebih muda|sama persis|belum lahir|Kakak lebih tua, adik lebih muda.",
            "Kita melihat dengan ...|mata|telinga|hidung|kaki|Mata adalah alat untuk melihat.",
            "Lawan kata 'tinggi' adalah ...|rendah|merah|makan|bola|Tinggi lawannya rendah.",
            "\uD83D\uDC31 Gambar ini adalah ...|kucing|ikan|rumah|buku|Gambar itu adalah kucing.",
            "Kata 'ayam' diawali huruf ...|a|b|k|s|Ayam diawali huruf a.",
            "Kata 'ibu' terdiri dari ... suku kata.|dua|lima|enam|sepuluh|I-bu terdiri dari dua suku kata.",
            "Kata 'sepatu' dibaca se-pa-tu. Jumlah suku katanya ...|tiga|satu|enam|sembilan|Se-pa-tu ada tiga suku kata.",
            "Kita makan nasi dengan ...|sendok|sapu|bantal|sepatu|Sendok dipakai untuk makan.",
            "Untuk menulis kita memakai ...|pensil|sendok|sabun|sisir|Pensil dipakai untuk menulis.",
            "Nama yang ditulis dengan benar adalah ...|Budi|budi|bUdi|bUDI|Nama orang diawali huruf kapital.",
            "Kita mendengar dengan ...|telinga|mata|tangan|gigi|Telinga adalah alat untuk mendengar.",
            "Kalimat yang benar adalah ...|Ani membaca buku.|Buku membaca Ani makan.|Ani buku membaca ke.|Membaca Ani di.|Kalimat yang baik punya susunan kata yang jelas.",
            "Pagi hari kita mengucapkan ...|selamat pagi|selamat malam|selamat tidur|sampai jumpa|Salam yang tepat untuk pagi hari adalah selamat pagi.",
            "Hewan yang hidup di air adalah ...|ikan|ayam|kucing|kambing|Ikan hidup di dalam air.",
            "Lengkapi kata: ku_ing (hewan peliharaan).|c|k|s|t|Kata yang benar: kucing.",
            "Lengkapi kata: bu_u (untuk dibaca).|k|t|s|m|Kata yang benar: buku.",
            "Lengkapi kata: me_a (tempat menaruh buku).|j|k|n|s|Kata yang benar: meja.",
            "Lengkapi kata: i_an (hidup di air).|k|b|m|t|Kata yang benar: ikan.",
            "Lengkapi kata: sa_un (untuk mencuci tangan).|b|d|g|r|Kata yang benar: sabun.",
            "Tulisan yang benar adalah ...|rumah|rmah|ruamh|rumha|Tulisan yang benar: rumah.",
            "Tulisan yang benar adalah ...|pensil|pesil|pensl|pnsil|Tulisan yang benar: pensil.",
            "Kalimat yang benar adalah ...|Aku makan nasi.|Nasi aku makan di.|Makan nasi ke aku.|Aku nasi makan ke.|Kalimat yang baik punya susunan kata yang jelas.",
            "Lawan kata 'siang' adalah ...|malam|kursi|topi|jalan|Siang lawannya malam.",
            "Kata yang diawali huruf b adalah ...|bola|kursi|topi|meja|Bola diawali huruf b.",
            "Lengkapi kata: ba_u (dipakai di badan).|j|m|r|n|Kata yang benar: baju.",
            "Lengkapi kata: to_i (dipakai di kepala).|p|k|m|s|Kata yang benar: topi.",
            "Lengkapi kata: ka_i (untuk berjalan).|k|d|p|t|Kata yang benar: kaki.",
            "Kalimat yang benar adalah ...|Adik minum susu.|Susu adik di minum ke.|Minum adik ke susu.|Susu minum adik di.|Kalimat harus punya susunan kata yang jelas.",
            "Tulisan yang benar adalah ...|kelas|kels|kalse|kleas|Tulisan yang benar: kelas.",
            "Kata 'bola' dibaca bo-la. Suku kata pertamanya ...|bo|la|ol|ba|Bo-la: suku kata pertama adalah bo."
        },
        {
            "Persamaan kata (sinonim) 'pandai' adalah ...|pintar|malas|nakal|lambat|Pandai dan pintar artinya sama.",
            "Lawan kata 'rajin' adalah ...|malas|giat|tekun|cepat",
            "Kalimat tanya diakhiri dengan tanda ...|tanya (?)|titik (.)|koma (,)|seru (!)|Kalimat tanya ditutup tanda tanya.",
            "Hari setelah hari Senin adalah ...|Selasa|Rabu|Minggu|Kamis",
            "Air yang mengalir dari gunung ke laut disebut ...|sungai|pasir|batu|awan",
            "Kalimat dengan huruf kapital yang benar adalah ...|Adik makan nasi.|adik makan nasi.|adik Makan nasi.|adik makan Nasi.|Huruf pertama kalimat ditulis dengan huruf kapital.",
            "Lawan kata 'jauh' adalah ...|dekat|panjang|tinggi|berat",
            "Kata 'sepatu' dipisah menjadi suku kata ...|se-pa-tu|sep-a-tu|s-epa-tu|sepa-t-u|Sepatu memiliki tiga suku kata: se-pa-tu.",
            "Kata dasar dari 'membaca' adalah ...|baca|mem|membac|bacaan|Awalan mem- ditambahkan pada kata dasar baca.",
            "Perintah yang sopan adalah ...|Tolong ambilkan bukuku.|Ambil bukuku!|Cepat, bukumu!|Buku, ambil.|Kata tolong membuat perintah terdengar sopan.",
            "Padi menghasilkan ...|beras|gula|garam|susu|Padi diolah menjadi beras.",
            "Saat menerima hadiah, kita mengucapkan ...|terima kasih|maaf|permisi|selamat tidur",
            "Persamaan kata 'senang' adalah ...|gembira|marah|takut|lapar|Senang dan gembira artinya sama.",
            "Lawan kata 'terang' adalah ...|gelap|ramai|sepi|cepat|Terang lawannya gelap.",
            "Kalimat seru diakhiri dengan tanda ...|seru (!)|tanya (?)|titik (.)|koma (,)|Kalimat seru ditutup tanda seru.",
            "Kata 'meja' termasuk kata ...|benda|kerja|sifat|tanya|Meja adalah nama benda.",
            "Kata 'berlari' termasuk kata ...|kerja|benda|sifat|tanya|Berlari adalah kegiatan, jadi kata kerja.",
            "Kata 'rajin' termasuk kata ...|sifat|benda|kerja|tanya|Rajin menyatakan sifat.",
            "Kata 'kelinci' terdiri dari ... suku kata.|tiga|dua|lima|enam|Ke-lin-ci terdiri dari tiga suku kata.",
            "Saat lewat di depan orang, kita mengucapkan ...|permisi|terima kasih|selamat ulang tahun|sampai jumpa",
            "Pasangan kata yang berlawanan adalah ...|tinggi - rendah|besar - luas|cepat - kilat|senang - gembira|Tinggi lawan katanya rendah.",
            "Ibu membeli sayur di ...|pasar|bengkel|bandara|stasiun|Sayur dijual di pasar.",
            "Lawan kata 'bersih' adalah ...|kotor|rapi|indah|wangi",
            "Lengkapi kata: ker_as (untuk menulis).|t|s|n|m|Kata yang benar: kertas.",
            "Lengkapi kata: gu_u (mengajar di sekolah).|r|n|l|t|Kata yang benar: guru.",
            "Kalimat tanya diakhiri tanda tanya. Kalimat yang benar adalah ...|Siapa namamu?|Siapa namamu.|Siapa namamu!|Siapa namamu,|Kalimat tanya diakhiri tanda tanya (?).",
            "Kalimat berita diakhiri tanda titik. Kalimat yang benar adalah ...|Ibu pergi ke pasar.|Ibu pergi ke pasar?|Ibu pergi ke pasar,|Ibu pergi ke pasar:|Kalimat berita diakhiri tanda titik (.).",
            "Kalimat seru memakai tanda seru. Kalimat yang benar adalah ...|Wah, bunganya cantik!|Wah, bunganya cantik.|Wah, bunganya cantik?|Wah, bunganya cantik,|Tanda seru (!) dipakai untuk menyatakan kekaguman.",
            "Tulisan yang benar adalah ...|sepeda|sepdea|seeda|sepeta|Tulisan yang benar: sepeda.",
            "Tulisan yang benar adalah ...|jendela|jedela|jandela|jendala|Tulisan yang benar: jendela.",
            "Susunan kata yang benar adalah ...|Adik bermain bola.|Bola adik bermain di.|Bermain bola adik ke.|Bola bermain adik.|Kalimat harus punya susunan kata yang jelas.",
            "Susunan kata yang benar adalah ...|Ibu memasak nasi.|Nasi ibu di memasak.|Memasak nasi ibu ke.|Ibu nasi ke memasak.|Kalimat harus punya susunan kata yang jelas.",
            "Tulisan yang benar adalah ...|lemari|lemri|lamari|lemary|Tulisan yang benar: lemari.",
            "Tulisan yang benar adalah ...|kelinci|kelinsi|klinci|kelinki|Tulisan yang benar: kelinci.",
            "Lengkapi: Ani pergi ... sekolah.|ke|di|dari|pada|Ke dipakai untuk menyatakan tujuan.",
            "Lengkapi: Buku ada ... atas meja.|di|ke|dari|dengan|Di dipakai untuk menyatakan tempat.",
            "Kata 'pagi' dan 'siang' menunjukkan ...|waktu|tempat|warna|rasa|Pagi dan siang adalah waktu.",
            "Awal kalimat ditulis dengan huruf ...|kapital|miring|kecil semua|angka|Awal kalimat selalu diawali huruf kapital."
        },
        {
            "Kata baku yang benar adalah ...|apotek|apotik|appotek|apoteek",
            "Kata baku yang benar adalah ...|aktif|aktip|aktiv|akthif",
            "Kata baku yang benar adalah ...|risiko|resiko|risikho|risiqo",
            "Persamaan kata (sinonim) 'gembira' adalah ...|senang|marah|sedih|takut|Gembira dan senang artinya sama.",
            "Lawan kata 'dermawan' adalah ...|kikir|murah hati|ramah|rajin|Dermawan suka memberi, kikir sulit memberi.",
            "Kata tanya untuk menanyakan orang adalah ...|siapa|dan|di|ke|Siapa dipakai untuk menanyakan orang.",
            "Huruf pertama nama orang ditulis dengan huruf ...|kapital|kecil|miring|tebal|Nama orang diawali huruf kapital.",
            "Kalimat berita diakhiri tanda ...|titik (.)|koma (,)|tanda tanya (?)|titik dua (:)|Kalimat berita ditutup dengan tanda titik.",
            "Ide pokok paragraf adalah ...|gagasan utama paragraf|kalimat terakhir saja|judul buku|nama tokoh|Ide pokok adalah hal terpenting yang dibahas dalam paragraf.",
            "Kata ganti orang pertama tunggal adalah ...|aku|kamu|dia|mereka|Aku dipakai untuk menyebut diri sendiri.",
            "Kata ganti 'kita' mencakup ...|pembicara dan lawan bicara|hanya pembicara|hanya orang lain|hanya hewan|Kita berarti kamu dan aku (kami berdua).",
            "Kata 'berlari' memakai awalan ...|ber-|me-|di-|ter-|Berlari = ber- + lari.",
            "Kata baku yang benar adalah ...|kualitas|kwalitas|kuwalitas|qualitas|Kata baku: kualitas.",
            "Kata baku yang benar adalah ...|izin|ijin|ijzin|idzin|Kata baku: izin.",
            "Kata baku yang benar adalah ...|silakan|silahkan|silakhan|sylakan|Kata baku: silakan.",
            "Kata baku yang benar adalah ...|teknik|tehnik|teknick|tekhnik|Kata baku: teknik.",
            "Kata 'tulis' diberi awalan me- menjadi ...|menulis|mentulis|metulis|mengtulis|Huruf t pada awal kata luluh: menulis.",
            "Kata 'petani' berasal dari kata dasar ...|tani|pe|tan|petan|Petani terdiri dari pe- dan tani.",
            "Kalimat tanya yang benar adalah ...|Kapan kamu pulang?|Kapan kamu pulang.|Kapan kamu pulang!|Kapan kamu pulang,|Kalimat tanya diakhiri tanda tanya.",
            "Kata 'dimakan' memakai awalan ...|di-|ber-|me-|se-|Dimakan = di- + makan.",
            "Cerita tentang binatang yang bisa berbicara disebut ...|fabel|legenda|biografi|puisi|Fabel adalah cerita dengan tokoh binatang.",
            "Karangan tentang kehidupan seseorang disebut ...|biografi|fabel|pantun|dongeng|Biografi menceritakan riwayat hidup seseorang.",
            "Satu bait pantun umumnya terdiri dari ... baris.|4|2|3|6|Pantun biasanya terdiri dari empat baris.",
            "Kata baku yang benar adalah ...|ijazah|ijasah|ijaza|izajah|Kata baku: ijazah.",
            "Kata baku yang benar adalah ...|khusus|husus|khusuz|kusus|Kata baku: khusus.",
            "Kata baku yang benar adalah ...|rapi|rappi|rafi|rapih|Kata baku: rapi.",
            "Kata 'ubah' diberi awalan meng- menjadi ...|mengubah|merubah|menguba|mengubak|Awalan meng- ditambahkan pada kata dasar ubah.",
            "Kalimat yang memakai tanda koma dengan benar adalah ...|Ibu membeli apel, jeruk, dan mangga.|Ibu membeli apel jeruk dan, mangga.|Ibu, membeli apel jeruk dan mangga.|Ibu membeli, apel jeruk dan mangga.|Koma memisahkan unsur-unsur yang disebutkan satu per satu.",
            "Kata 'baca' diberi awalan me- menjadi ...|membaca|mebaca|menbaca|mengbaca|Sebelum huruf b, awalan me- berubah menjadi mem-.",
            "Kata 'sapu' diberi awalan me- menjadi ...|menyapu|mesapu|mensapu|mengsapu|Huruf s pada awal kata luluh menjadi ny: menyapu.",
            "Kalimat yang memakai huruf kapital dengan benar adalah ...|Aku tinggal di Kandangan.|aku tinggal di kandangan.|Aku tinggal di kandangan.|aku tinggal di Kandangan.|Awal kalimat dan nama tempat memakai huruf kapital.",
            "Kalimat yang memakai huruf kapital dengan benar adalah ...|Kami pergi ke Bandung.|kami pergi ke bandung.|Kami pergi ke bandung.|kami Pergi ke Bandung.|Awal kalimat dan nama kota diawali huruf kapital.",
            "Persamaan kata 'gembira' adalah ...|senang|sedih|marah|takut|Gembira dan senang artinya sama.",
            "Lawan kata 'kuat' adalah ...|lemah|besar|berat|keras|Lawan kuat adalah lemah.",
            "Ibu pergi ... pasar.|ke|di|oleh|dan|Ke menunjukkan tujuan.",
            "Tanda koma dipakai untuk memisahkan ...|rincian|akhir kalimat tanya|kata seru|judul|Koma memisahkan rincian, misalnya apel, jeruk, dan mangga.",
            "Kata 'rumah' diberi akhiran -ku menjadi ...|rumahku|rumah ku|ku rumah|rumah-ku|Akhiran -ku ditulis serangkai: rumahku.",
            "Paragraf adalah kumpulan ... yang membahas satu hal.|kalimat|huruf|angka|gambar|Paragraf terdiri atas beberapa kalimat."
        },
        {
            "Kalimat yang penulisannya benar adalah ...|Dia pergi ke Bandung kemarin.|dia pergi ke bandung kemarin.|Dia pergi ke bandung Kemarin.|Dia Pergi ke Bandung kemarin.|Awal kalimat dan nama kota memakai huruf kapital.",
            "Kata baku yang benar adalah ...|analisis|analisa|analisys|analises",
            "Kata baku yang benar adalah ...|teknik|tehnik|tekhnik|tekknik",
            "Kata baku yang benar adalah ...|silakan|silahkan|silakhan|sielakan",
            "Kata baku yang benar adalah ...|manajemen|menejemen|managemen|manajmen",
            "Persamaan kata (sinonim) 'cermat' adalah ...|teliti|ceroboh|lengah|santai|Cermat dan teliti artinya sama.",
            "Lawan kata 'ramah' adalah ...|ketus|sopan|baik|lembut|Orang yang ketus tidak ramah.",
            "Kata dasar dari 'menulis' adalah ...|tulis|menu|lis|menulis|Menulis = me- + tulis.",
            "Kalimat seru adalah ...|Wah, indah sekali pemandangannya!|Kamu pergi ke mana?|Tolong buka jendela.|Ayah membaca koran.|Kalimat seru menyatakan kekaguman atau rasa kuat.",
            "Kata ganti 'mereka' menunjuk pada ...|orang ketiga banyak|diri sendiri|orang yang diajak bicara|benda mati saja|Mereka adalah kata ganti orang ketiga jamak.",
            "Gagasan utama dalam paragraf disebut ...|ide pokok|judul|tokoh|alur",
            "Bagian cerita saat tokoh menghadapi masalah disebut ...|konflik|pembukaan|penutup|judul",
            "Tempat dan waktu terjadinya cerita disebut ...|latar|tema|amanat|tokoh|Latar adalah tempat, waktu, dan suasana cerita.",
            "Pesan yang ingin disampaikan penulis dalam cerita disebut ...|amanat|latar|judul|tokoh|Amanat adalah pesan moral cerita.",
            "Dua baris pertama pantun disebut ...|sampiran|isi|judul|amanat|Sampiran ada di dua baris awal, isi di dua baris akhir.",
            "Tanda koma (,) dipakai untuk ...|memisahkan unsur pemerincian|mengakhiri kalimat berita|menulis judul buku|membuka kalimat|Contoh: Ibu membeli apel, jeruk, dan pisang.",
            "Kata baku yang benar adalah ...|atlet|atlit|atlett|athlet|Kata baku: atlet.",
            "Kata baku yang benar adalah ...|teknologi|tehnologi|tekhnologi|teknolozi|Kata baku: teknologi.",
            "Kata baku yang benar adalah ...|Februari|Pebruari|Februarie|Febuari|Kata baku: Februari.",
            "Kata baku yang benar adalah ...|andal|handal|andall|anndal|Kata baku: andal.",
            "Tanda titik dua (:) dipakai sebelum ...|daftar pemerincian|akhir kalimat tanya|judul buku|nama kota|Contoh: Bahan yang diperlukan: telur, tepung, dan gula.",
            "Tanda petik dua dipakai untuk mengapit ...|kalimat langsung (ucapan)|nama hari|angka|tanda tanya|Tanda petik mengapit ucapan yang dikutip langsung.",
            "Kata 'tanam' diberi awalan me- menjadi ...|menanam|menganam|mentanam|metanam|Huruf t pada awal kata luluh menjadi n: menanam.",
            "Kata 'pukul' diberi awalan me- menjadi ...|memukul|mempukul|mengpukul|mepukul|Huruf p pada awal kata luluh menjadi m: memukul."
        },
        {
            "Kata baku yang benar adalah ...|nasihat|nasehat|nasihaat|nasiahat",
            "Kata baku yang benar adalah ...|izin|ijin|izzin|ihzin",
            "Kata baku yang benar adalah ...|objek|obyek|ogjek|obiek",
            "Kata baku yang benar adalah ...|kualitas|kwalitas|kualites|qualitas",
            "Kata baku yang benar adalah ...|standar|standard|setandar|stanndar",
            "Kata hubung yang dipakai untuk menghubungkan dua hal yang setara adalah ...|dan|karena|jika|sehingga|Dan menghubungkan dua hal yang kedudukannya sama.",
            "Kata hubung 'karena' menyatakan ...|sebab|akibat|waktu|tujuan|Contoh: Aku terlambat karena hujan.",
            "Kata hubung 'tetapi' menyatakan ...|pertentangan|sebab|syarat|waktu|Contoh: Dia pintar tetapi pemalu.",
            "Pembaca adalah ...|orang yang membaca|alat untuk membaca|tempat membaca|hasil membaca|Awalan pe- menyatakan pelaku.",
            "Kata ulang 'buku-buku' menyatakan ...|jamak (banyak)|tunggal|pekerjaan|waktu|Kata ulang sering menyatakan jumlah banyak.",
            "Pidato yang baik disampaikan dengan ...|suara jelas dan sopan|suara pelan sekali|membelakangi pendengar|tanpa persiapan",
            "Ungkapan 'panjang tangan' berarti ...|suka mencuri|tangannya panjang|rajin bekerja|tinggi badan|Panjang tangan adalah ungkapan untuk orang yang suka mencuri.",
            "Ungkapan 'besar kepala' berarti ...|sombong|kepalanya besar|pandai|marah|Besar kepala adalah ungkapan untuk orang sombong.",
            "Kalimat efektif adalah kalimat yang ...|jelas dan tidak berbelit-belit|panjang dan rumit|tanpa subjek|tanpa tanda baca",
            "Surat untuk teman atau keluarga disebut surat ...|pribadi|dinas|kuasa|undangan|Surat pribadi ditulis untuk keperluan pribadi.",
            "Penutup surat pribadi yang cocok adalah ...|Salam hangat,|Dengan hormat,|Perihal:|Lampiran:|Salam hangat cocok untuk menutup surat pribadi.",
            "Kata baku yang benar adalah ...|zaman|jaman|zamman|saman|Kata baku: zaman.",
            "Kata baku yang benar adalah ...|napas|nafas|napaz|nappas|Kata baku: napas.",
            "Kata baku yang benar adalah ...|sekadar|sekedar|sekadarr|sekeddar|Kata baku: sekadar.",
            "Kata baku yang benar adalah ...|hakikat|hakekat|hakikad|hakiqat|Kata baku: hakikat.",
            "Kata hubung 'meskipun' menyatakan ...|pertentangan|sebab|syarat|waktu|Contoh: Meskipun hujan, dia tetap berangkat.",
            "Kata hubung 'jika' menyatakan ...|syarat|pertentangan|sebab|tempat|Contoh: Jika hujan, kami tinggal di rumah.",
            "Kalimat tanya yang benar adalah ...|Kapan kamu pulang?|Kapan kamu pulang.|Kapan kamu pulang!|Kapan, kamu pulang|Kalimat tanya diakhiri tanda tanya (?).",
            "Awalan pe- pada kata 'tulis' menghasilkan ...|penulis|pentulis|pemulis|pengtulis|Pe- + tulis = penulis (orang yang menulis)."
        },
        {
            "Kata baku yang benar adalah ...|aktivitas|aktifitas|aktivitaz|aktipitas",
            "Kata baku yang benar adalah ...|metode|metoda|metodhe|metoode",
            "Kata baku yang benar adalah ...|sistem|sistim|sisthem|sistemm",
            "Kata baku yang benar adalah ...|praktik|praktek|praktikk|praktiq",
            "Kata baku yang benar adalah ...|konsekuen|konsekwen|konsekuenn|qonsekuen",
            "'Dia bagaikan singa di medan perang' adalah majas ...|simile (perumpamaan)|personifikasi|hiperbola|ironi|Simile membandingkan dengan kata seperti, bagaikan, bak.",
            "Majas yang memberi sifat manusia pada benda adalah ...|personifikasi|hiperbola|ironi|simile|Contoh: Angin berbisik di telingaku.",
            "'Tangisannya membanjiri seisi rumah' adalah majas ...|hiperbola|personifikasi|metafora|litotes|Hiperbola adalah ungkapan yang berlebihan.",
            "Fakta adalah informasi yang ...|dapat dibuktikan kebenarannya|hanya pendapat|belum tentu ada|berdasarkan perasaan|Fakta bisa dibuktikan dengan data atau bukti.",
            "Kalimat opini biasanya mengandung ...|pendapat atau penilaian|angka pasti|data resmi|tanggal kejadian|Opini adalah pendapat seseorang.",
            "Kalimat yang berisi fakta adalah ...|Indonesia merdeka pada tahun 1945.|Menurutku Indonesia paling indah.|Mungkin besok hujan deras.|Sepertinya bulan lebih bagus.|Tahun kemerdekaan dapat dibuktikan.",
            "Tujuan teks eksplanasi adalah ...|menjelaskan proses atau sebab akibat|menghibur dengan lelucon|memberi perintah|membuat puisi",
            "Teks yang mengajak orang membeli suatu produk disebut ...|iklan|biografi|berita|puisi|Iklan bertujuan menawarkan barang atau jasa.",
            "Dalam berita 5W+1H, 'what' berarti ...|apa|siapa|kapan|di mana|What = apa.",
            "Unsur 'siapa' dalam 5W+1H adalah ...|who|when|why|how|Who = siapa.",
            "Bentuk serapan dari kata 'computer' adalah ...|komputer|kompoter|kumputer|komputar|Serapan baku: komputer.",
            "Kata baku yang benar adalah ...|lembap|lembab|lembapp|lemmbap|Kata baku: lembap.",
            "Kata baku yang benar adalah ...|kuantitas|kwantitas|kuantitaz|quantitas|Kata baku: kuantitas.",
            "Kata baku yang benar adalah ...|kuitansi|kwitansi|kuitanci|kwitansy|Kata baku: kuitansi.",
            "Kata baku yang benar adalah ...|azan|adzan|azzan|asan|Kata baku: azan.",
            "'Dialah bintang kelas kami' adalah majas ...|metafora|simile|hiperbola|litotes|Metafora membandingkan langsung tanpa kata seperti atau bagai.",
            "'Hatinya bagai disayat sembilu' adalah majas ...|simile|metafora|ironi|litotes|Kata bagai menandakan perumpamaan (simile).",
            "Kalimat majemuk adalah kalimat yang ...|punya dua pola kalimat atau lebih|hanya satu subjek|tanpa predikat|terdiri atas satu kata|Contoh: Ibu memasak dan ayah membaca koran.",
            "Kalimat yang menjelaskan ide pokok dalam paragraf disebut ...|kalimat penjelas|judul|amanat|tokoh|Kalimat penjelas menguatkan ide pokok."
        }
    };

    private static final String[][] BI_EN = {
        {
            "Apa bahasa Inggris dari 'kucing'?|cat|dog|cow|hen",
            "Apa bahasa Inggris dari 'merah'?|red|blue|green|black",
            "Apa bahasa Inggris dari 'satu'?|one|two|three|four",
            "Apa bahasa Inggris dari 'dua'?|two|one|ten|six",
            "Apa bahasa Inggris dari 'anjing'?|dog|cat|bird|fish",
            "Apa bahasa Inggris dari 'apel'?|apple|banana|orange|grape",
            "Apa bahasa Inggris dari 'buku'?|book|pen|bag|desk",
            "Apa bahasa Inggris dari 'biru'?|blue|red|pink|white",
            "\uD83D\uDC36 Apa bahasa Inggris gambar ini?|dog|cat|cow|hen|Gambar itu adalah dog (anjing).",
            "\uD83C\uDF4E Apa bahasa Inggris gambar ini?|apple|banana|grape|mango|Gambar itu adalah apple (apel).",
            "\uD83D\uDC26 Apa bahasa Inggris gambar ini?|bird|fish|frog|duck|Gambar itu adalah bird (burung).",
            "\uD83C\uDF19 Apa bahasa Inggris gambar ini?|moon|sun|tree|book|Gambar itu adalah moon (bulan).",
            "\uD83D\uDCD6 Apa bahasa Inggris gambar ini?|book|bag|pen|box|Gambar itu adalah book (buku).",
            "\uD83C\uDFE0 Apa bahasa Inggris gambar ini?|house|car|tree|ball|Gambar itu adalah house (rumah).",
            "\u26BD Apa bahasa Inggris gambar ini?|ball|book|bag|bed|Gambar itu adalah ball (bola).",
            "\uD83D\uDE97 Apa bahasa Inggris gambar ini?|car|cup|bed|hat|Gambar itu adalah car (mobil).",
            "Apa bahasa Inggris dari 'lima'?|five|two|eight|ten|Lima = five.",
            "Apa bahasa Inggris dari 'ibu'?|mother|father|sister|brother",
            "Apa bahasa Inggris dari 'ayah'?|father|mother|baby|friend",
            "Apa bahasa Inggris dari 'mata'?|eye|ear|nose|hand",
            "Apa bahasa Inggris dari 'tiga'?|three|five|seven|eight",
            "Bertemu di pagi hari, kita bilang ...|Good morning|Good night|Goodbye|Sorry|Good morning artinya selamat pagi.",
            "Apa bahasa Inggris dari 'terima kasih'?|thank you|sorry|please|hello",
            "Apa bahasa Inggris dari 'hijau'?|green|yellow|brown|gray",
            "Apa bahasa Inggris dari 'ikan'?|fish|frog|duck|goat",
            "Apa bahasa Inggris dari 'sepuluh'?|ten|one|three|five|Sepuluh = ten.",
            "Apa bahasa Inggris dari 'putih'?|white|black|red|blue|Putih = white.",
            "Apa bahasa Inggris dari 'hitam'?|black|white|red|green|Hitam = black.",
            "Apa bahasa Inggris dari 'telinga'?|ear|eye|foot|hair|Telinga = ear.",
            "Apa bahasa Inggris dari 'tangan'?|hand|nose|leg|hair|Tangan = hand.",
            "Apa bahasa Inggris dari 'kepala'?|head|hand|foot|hair|Kepala = head.",
            "Apa bahasa Inggris dari 'susu'?|milk|rice|egg|bread|Susu = milk.",
            "Terima kasih dalam bahasa Inggris adalah ...|thank you|sorry|hello|goodbye|Thank you artinya terima kasih.",
            "Halo dalam bahasa Inggris adalah ...|hello|goodbye|sorry|please|Hello artinya halo."
        },
        {
            "Apa bahasa Inggris dari 'kuning'?|yellow|orange|purple|pink",
            "Hewan berhidung panjang dalam bahasa Inggris adalah ...|elephant|monkey|rabbit|tiger|Gajah = elephant.",
            "Apa bahasa Inggris dari 'kursi'?|chair|table|door|window",
            "Apa bahasa Inggris dari 'meja'?|table|chair|bed|lamp",
            "Apa bahasa Inggris dari 'sepuluh'?|ten|six|nine|twelve",
            "Jawaban yang tepat untuk 'How are you?' adalah ...|I am fine, thank you.|Good night.|See you tomorrow.|It is red.|How are you? artinya apa kabar?",
            "Apa bahasa Inggris dari 'sekolah'?|school|home|market|garden",
            "Apa bahasa Inggris dari 'guru'?|teacher|doctor|farmer|driver",
            "Apa bahasa Inggris dari 'oranye'?|orange|blue|black|white|Oranye = orange.",
            "Apa bahasa Inggris dari 'merah muda'?|pink|red|green|black|Merah muda = pink.",
            "Apa bahasa Inggris dari 'ungu'?|purple|blue|brown|white|Ungu = purple.",
            "Apa bahasa Inggris dari 'coklat'?|brown|pink|white|blue|Coklat = brown.",
            "Apa bahasa Inggris dari 'tas'?|bag|desk|ruler|eraser|Tas = bag.",
            "Apa bahasa Inggris dari 'penghapus'?|eraser|ruler|pencil|book|Penghapus = eraser.",
            "Apa bahasa Inggris dari 'penggaris'?|ruler|eraser|pencil|bag|Penggaris = ruler.",
            "Apa bahasa Inggris dari 'pensil'?|pencil|paper|desk|bag|Pensil = pencil.",
            "Apa bahasa Inggris dari 'pintu'?|door|window|floor|wall|Pintu = door.",
            "Apa bahasa Inggris dari 'jendela'?|window|door|roof|floor|Jendela = window.",
            "Apa bahasa Inggris dari 'murid'?|student|teacher|farmer|driver|Murid = student.",
            "Apa bahasa Inggris dari 'tangan'?|hand|foot|head|leg",
            "Apa bahasa Inggris dari 'kepala'?|head|hand|nose|mouth",
            "Apa bahasa Inggris dari 'makan'?|eat|sleep|run|read",
            "Apa bahasa Inggris dari 'tidur'?|sleep|eat|jump|sing",
            "Lengkapi: I ... a student.|am|is|are|be|Setelah I kita pakai am.",
            "Lengkapi: She ... my sister.|is|am|are|be|Setelah she kita pakai is.",
            "Apa bahasa Inggris dari 'pisang'?|banana|apple|mango|grape",
            "Apa bahasa Inggris dari 'nol'?|zero|one|ten|nine",
            "Apa bahasa Inggris dari 'nenek'?|grandmother|grandfather|mother|sister|Nenek = grandmother.",
            "Apa bahasa Inggris dari 'kakek'?|grandfather|grandmother|father|brother|Kakek = grandfather.",
            "Apa bahasa Inggris dari 'saudara laki-laki'?|brother|sister|uncle|aunt|Saudara laki-laki = brother.",
            "Apa bahasa Inggris dari 'hidung'?|nose|mouth|ear|eye|Hidung = nose.",
            "Apa bahasa Inggris dari 'mulut'?|mouth|nose|eye|hair|Mulut = mouth.",
            "Apa bahasa Inggris dari 'rambut'?|hair|hand|foot|leg|Rambut = hair.",
            "Apa bahasa Inggris dari 'kaki'?|foot|hand|head|nose|Kaki = foot.",
            "Sore hari kita berkata '...'|Good afternoon|Good morning|Good night|Goodbye|Good afternoon artinya selamat sore.",
            "Sebelum tidur kita berkata '...'|Good night|Good morning|Good afternoon|Hello|Good night artinya selamat malam.",
            "Apa arti 'Thank you'?|terima kasih|maaf|selamat tinggal|tolong|Thank you artinya terima kasih.",
            "Apa arti 'sorry'?|maaf|terima kasih|tolong|silakan|Sorry artinya maaf."
        },
        {
            "Lengkapi: They ... playing football.|are|is|am|be|Setelah they kita pakai are.",
            "Lengkapi: He ... a book.|has|have|having|haves|Setelah he kita pakai has.",
            "Lengkapi: I ... two cats.|have|has|is|are|Setelah I kita pakai have.",
            "Apa arti 'window'?|jendela|pintu|atap|lantai",
            "Apa arti 'door'?|pintu|jendela|dinding|meja",
            "Hari setelah Monday adalah ...|Tuesday|Wednesday|Sunday|Friday|Monday = Senin, Tuesday = Selasa.",
            "Apa bahasa Inggris dari 'air'?|water|milk|tea|rice",
            "Apa bahasa Inggris dari 'nasi'?|rice|bread|egg|meat",
            "Lengkapi: She ... a student.|is|are|am|be|Setelah she kita pakai is.",
            "Lengkapi: We ... happy.|are|is|am|be|Setelah we kita pakai are.",
            "Lengkapi: I ... a boy.|am|is|are|be|Setelah I kita pakai am.",
            "Lengkapi: It ... a cat.|is|are|am|do|Setelah it kita pakai is.",
            "Lengkapi: She ... a cat.|has|have|having|haves|Setelah she kita pakai has.",
            "Lengkapi: This is ... apple.|an|a|two|any|Kata apple diawali bunyi vokal, jadi memakai an.",
            "Lengkapi: This is ... book.|a|an|are|is|Kata book diawali bunyi konsonan, jadi memakai a.",
            "Apa arti 'beautiful'?|cantik|besar|cepat|dingin|Beautiful artinya cantik.",
            "Apa arti 'tall'?|tinggi|pendek|gemuk|kecil|Tall artinya tinggi.",
            "Apa arti 'hungry'?|lapar|haus|lelah|senang|Hungry artinya lapar.",
            "Apa bahasa Inggris dari 'dokter'?|doctor|nurse|farmer|police",
            "Lawan kata 'big' adalah ...|small|tall|long|fast|Big = besar, small = kecil.",
            "Lawan kata 'hot' adalah ...|cold|warm|dry|new|Hot = panas, cold = dingin.",
            "Lengkapi: What ... your name?|is|are|am|do|Setelah what kita pakai is untuk satu benda.",
            "Jawaban untuk 'What is your name?' adalah ...|My name is Budi.|I am fine.|It is red.|Yes, I do.|Pertanyaan tentang nama dijawab dengan My name is ...",
            "Five + two = ...|seven|six|eight|nine|Five = 5, two = 2, jadi 7 = seven.",
            "Apa bahasa Inggris dari 'rumah'?|house|school|room|road",
            "Apa arti 'brother'?|saudara laki-laki|saudara perempuan|ayah|paman",
            "Apa arti 'thirsty'?|haus|lapar|sedih|marah|Thirsty artinya haus.",
            "Apa bahasa Inggris dari 'hari Senin'?|Monday|Sunday|Friday|Tuesday|Hari Senin = Monday.",
            "Apa bahasa Inggris dari 'hari Minggu'?|Sunday|Monday|Saturday|Friday|Hari Minggu = Sunday.",
            "Apa arti 'January'?|Januari|Juli|Juni|Maret|January = Januari.",
            "Apa bahasa Inggris dari 'dapur'?|kitchen|bedroom|garden|bathroom|Dapur = kitchen.",
            "Apa bahasa Inggris dari 'kamar tidur'?|bedroom|kitchen|garage|classroom|Kamar tidur = bedroom.",
            "Apa bahasa Inggris dari 'petani'?|farmer|doctor|teacher|driver|Petani = farmer.",
            "Lengkapi: I ... milk every morning.|drink|drinks|drinking|drank|Setelah I kita pakai bentuk dasar drink.",
            "Lengkapi: He ... to school.|goes|go|going|gone|Setelah he kita pakai goes.",
            "Lengkapi: They ... in the garden.|play|plays|playing|played|Setelah they kita pakai play."
        },
        {
            "Lengkapi: She ... to school every day.|goes|go|going|gone|Setelah she, kata kerja diberi -es/-s: goes.",
            "Lengkapi: We ... English now.|study|studies|studied|studying|Setelah we kita pakai study.",
            "Lengkapi: Kucing di bawah meja. The cat is ... the table.|under|on|above|beside|Under artinya di bawah.",
            "Apa bahasa Inggris dari 'sungai'?|river|mountain|lake|sea",
            "Apa arti 'kitchen'?|dapur|kamar tidur|kamar mandi|ruang tamu",
            "Apa arti 'bathroom'?|kamar mandi|dapur|garasi|halaman",
            "Jawaban untuk 'What time is it?' (pukul 3) adalah ...|It is three o'clock.|It is Monday.|I am three.|It is red.|Pukul tiga = three o'clock.",
            "Lengkapi: There ... three books on the desk.|are|is|am|be|Untuk benda banyak kita pakai there are.",
            "Lengkapi: There ... a pen on the desk.|is|are|am|be|Untuk satu benda kita pakai there is.",
            "Apa bahasa Inggris dari 'mereka'?|they|we|she|you",
            "Apa bahasa Inggris dari 'kami'?|we|they|he|it",
            "Lengkapi: My father ... a farmer.|is|are|am|do|Setelah my father kita pakai is.",
            "Apa arti 'beautiful'?|cantik atau indah|jelek|besar|cepat",
            "Apa arti 'fast'?|cepat|lambat|tinggi|berat",
            "Hari setelah Friday adalah ...|Saturday|Thursday|Sunday|Monday|Friday = Jumat, Saturday = Sabtu.",
            "Bulan pertama dalam setahun adalah ...|January|March|June|December|January = Januari."
        },
        {
            "Lengkapi: I ... reading a book now.|am|is|are|was|Setelah I kita pakai am.",
            "Lengkapi: She ... washing the dishes.|is|are|am|do|Setelah she kita pakai is.",
            "Lengkapi: They ... football yesterday.|played|play|plays|playing|Kata 'yesterday' menunjukkan masa lampau.",
            "Lengkapi: I ... to the market yesterday.|went|go|goes|gone|Bentuk lampau dari go adalah went.",
            "Bentuk lampau dari 'eat' adalah ...|ate|eated|eaten|eats",
            "Bentuk lampau dari 'see' adalah ...|saw|seen|seed|sees",
            "Apa arti 'beautiful garden'?|taman yang indah|rumah yang besar|sungai yang dalam|kota yang ramai",
            "Apa arti 'always'?|selalu|kadang-kadang|tidak pernah|kemarin",
            "Apa arti 'never'?|tidak pernah|selalu|sering|besok",
            "Lengkapi: Ani is ... than Budi.|taller|tallest|more tall|tall|Untuk membandingkan dua orang kita pakai taller.",
            "Lengkapi: Everest is the ... mountain in the world.|highest|higher|high|more high|Untuk yang paling kita pakai highest.",
            "Lengkapi: How ... apples do you have?|many|much|any|a|Apples bisa dihitung, jadi pakai many.",
            "Lengkapi: How ... water do you need?|much|many|few|a|Water tidak bisa dihitung, jadi pakai much.",
            "Lengkapi: The bank is ... the post office and the school.|between|on|under|at|Between artinya di antara.",
            "Apa arti 'library'?|perpustakaan|toko buku|kantin|lapangan",
            "Apa arti 'hospital'?|rumah sakit|apotek|sekolah|pasar"
        },
        {
            "Lengkapi: If it rains, I ... at home.|will stay|stayed|staying|have stay|Kalimat pengandaian (if) memakai will.",
            "Lengkapi: She has ... in Jakarta since 2020.|lived|live|living|lives|Has/have diikuti bentuk ketiga: lived.",
            "Lengkapi: I am going to ... a letter.|write|wrote|written|writing|Setelah to kita pakai bentuk dasar: write.",
            "Bentuk lampau dari 'buy' adalah ...|bought|buyed|buys|brought",
            "Bentuk lampau dari 'go' adalah ...|went|goed|gone|going",
            "Bentuk lampau dari 'write' adalah ...|wrote|writed|written|writes",
            "Lengkapi: The letter was ... by Budi.|written|write|wrote|writing|Kalimat pasif memakai was + bentuk ketiga.",
            "'My mother is cooking in the kitchen.' artinya ...|Ibuku sedang memasak di dapur.|Ibuku akan memasak besok.|Ibuku sudah memasak kemarin.|Ibuku tidak suka memasak.|Is cooking menunjukkan sedang berlangsung.",
            "Apa arti 'environment'?|lingkungan|pemerintah|pengetahuan|perjalanan",
            "Apa arti 'knowledge'?|pengetahuan|lingkungan|kebahagiaan|pekerjaan",
            "Apa arti 'dangerous'?|berbahaya|aman|penting|mudah",
            "Lawan kata 'difficult' adalah ...|easy|hard|heavy|serious|Difficult = sulit, easy = mudah.",
            "Lengkapi: He can ... English very well.|speak|speaks|spoke|speaking|Setelah can kita pakai bentuk dasar.",
            "Lengkapi: You must ... your homework.|do|does|did|doing|Setelah must kita pakai bentuk dasar.",
            "Lengkapi: She ... TV when I called her.|was watching|watches|watched|will watch|Kejadian sedang berlangsung di masa lampau memakai was watching.",
            "Apa arti 'Good luck!'?|Semoga berhasil!|Selamat tidur!|Sampai jumpa!|Terima kasih!"
        }
    };

    // Level yang sedang dimainkan di peta (-1 = bukan permainan peta)
    private int advLevel = -1;
    private int advK = 0;
    private int advPart = 0;
    private int tableN = 2;
    private boolean advWorldNew = false;

    private String[] lv(int k, int l) {
        return LEVELS[k][l].split("\\|");
    }

    private int advStarsOf(int k, int l) {
        return prefs.getInt("adv_" + k + "_" + l, 0);
    }

    private boolean advOpen(int k, int l) {
        return l == 0 || advStarsOf(k, l - 1) >= 1;
    }

    private int advDone(int k) {
        int d = 0;
        for (int l = 0; l < LEVELS[k].length; l++) {
            if (advStarsOf(k, l) >= 1) {
                d++;
            }
        }
        return d;
    }

    private int advStarSum(int k) {
        int s = 0;
        for (int l = 0; l < LEVELS[k].length; l++) {
            s += advStarsOf(k, l);
        }
        return s;
    }

    private String advSummary() {
        return WORLD_NAME[kelas] + " \u2022 " + advDone(kelas) + " dari " + LEVELS[kelas].length + " level";
    }

    // Bintang level peta: 3 bintang bila benar 90% ke atas, 2 bila 70%, 1 bila 50% (lulus)
    private int advStarsFor(int ok, int tot) {
        int pct = tot > 0 ? ok * 100 / tot : 0;
        return pct >= 90 ? 3 : pct >= 70 ? 2 : pct >= 50 ? 1 : 0;
    }

    private String starText(int n) {
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < 3; i++) {
            s.append(i < n ? "\u2B50" : "\u2606");
        }
        return s.toString();
    }

    private void advSave(int stars) {
        String key = "adv_" + advK + "_" + advLevel;
        if (stars > prefs.getInt(key, 0)) {
            prefs.edit().putInt(key, stars).apply();
        }
    }

    // Lencana peta: 9 = 5 level selesai, 10 = satu dunia tamat, 11 = tiga bintang di semua level satu dunia
    private void awardMapBadges(ArrayList<Integer> fresh) {
        int clear = 0;
        boolean worldDone = false;
        boolean allStars = false;
        for (int k = 0; k < LEVELS.length; k++) {
            int n = LEVELS[k].length;
            int d = 0;
            int s3 = 0;
            for (int l = 0; l < n; l++) {
                int st = advStarsOf(k, l);
                if (st >= 1) {
                    d++;
                }
                if (st >= 3) {
                    s3++;
                }
            }
            clear += d;
            if (d == n) {
                worldDone = true;
            }
            if (s3 == n) {
                allStars = true;
            }
        }
        if (clear >= 5) {
            giveBadge(9, fresh);
        }
        if (worldDone) {
            giveBadge(10, fresh);
        }
        if (allStars) {
            giveBadge(11, fresh);
        }
    }

    private String levelDesc(String[] p) {
        char t = p[2].charAt(0);
        int v = Integer.parseInt(p[3].split(",")[0]);
        if (t == 'O') {
            return MODE_DESC[v];
        }
        if (t == 'T') {
            return "Latihan tabel perkalian " + v + ": 7 soal kali dan 3 soal bagi";
        }
        if (t == 'B') {
            return "Kuis " + SUBJ_NAMES[v] + " untuk " + KELAS_NAMES[kelas];
        }
        return "Cocokkan Pasangan: pasangkan kartu kiri dan kanan";
    }

    private void startLevel(int l) {
        String[] p = lv(kelas, l);
        np = 1;
        advLevel = l;
        advK = kelas;
        advWorldNew = false;
        char t = p[2].charAt(0);
        if (t == 'O') {
            startGame(Integer.parseInt(p[3]));
        } else if (t == 'T') {
            tableN = Integer.parseInt(p[3]);
            startGame(8);
        } else if (t == 'B') {
            String[] sp = p[3].split(",");
            subj = Integer.parseInt(sp[0]);
            advPart = Integer.parseInt(sp[1]);
            startGame(7);
        } else {
            startMatch(Integer.parseInt(p[3]));
        }
    }

    // ---------- Layar peta ----------

    private void showPeta() {
        handler.removeCallbacksAndMessages(null);
        setScreen(10);
        advLevel = -1;
        nameFields = null;
        final int k = kelas;
        final int n = LEVELS[k].length;
        final int done = advDone(k);

        LinearLayout c = column();
        c.addView(text("\uD83D\uDDFA\uFE0F Peta Petualangan", 24, Color.parseColor("#1A3C6E"), true), lp(WRAP, 0));
        c.addView(text("Pilih kelas", 13, DARK, false), lp(WRAP, 8));
        c.addView(chips(KELAS_LABEL, k, KELAS_COLOR, i -> {
            prefs.edit().putInt("kelas", i).apply();
            kelas = i;
            showPeta();
        }), lp(dp(44), 6));

        // Spanduk dunia
        LinearLayout hero = new LinearLayout(this);
        hero.setOrientation(LinearLayout.VERTICAL);
        hero.setGravity(Gravity.CENTER_HORIZONTAL);
        hero.setPadding(dp(16), dp(16), dp(16), dp(16));
        premium(hero, Color.parseColor(WORLD_PAL[k]), 28, 600L);
        hero.addView(text(WORLD_ICON[k] + " " + WORLD_NAME[k], 22, Color.WHITE, true), lp(WRAP, 0));
        hero.addView(text(KELAS_NAMES[k] + " \u2022 " + done + " dari " + n + " level \u2022 \u2B50 "
                + advStarSum(k) + "/" + (3 * n), 13, Color.WHITE, false), lp(WRAP, 4));
        LinearLayout bar = new LinearLayout(this);
        bar.setOrientation(LinearLayout.HORIZONTAL);
        bar.setBackground(box(Color.argb(90, 255, 255, 255), 6));
        if (done > 0) {
            View fill = new View(this);
            GradientDrawable fg = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,
                    new int[]{Color.parseColor("#FFE082"), Color.parseColor("#FFB300")});
            fg.setCornerRadius(dp(6));
            fill.setBackground(fg);
            bar.addView(fill, new LinearLayout.LayoutParams(0, MATCH, done));
        }
        if (n - done > 0) {
            bar.addView(new View(this), new LinearLayout.LayoutParams(0, MATCH, n - done));
        }
        hero.addView(bar, lp(dp(12), 10));
        c.addView(hero, lp(WRAP, 12));

        // Teman pendamping
        LinearLayout buddy = new LinearLayout(this);
        buddy.setOrientation(LinearLayout.HORIZONTAL);
        buddy.setGravity(Gravity.CENTER_VERTICAL);
        buddy.addView(text(BUDDY_ICON[k], 38, DARK, false), new LinearLayout.LayoutParams(dp(56), dp(56)));
        String msg;
        if (done == 0) {
            msg = "Halo! Aku " + BUDDY_NAME[k] + ". Ayo mulai dari level 1!";
        } else if (done < n) {
            msg = "Hebat! Tinggal " + (n - done) + " level lagi di " + WORLD_NAME[k] + ".";
        } else {
            msg = "Dunia ini sudah tamat! Coba raih 3 bintang di semua level.";
        }
        TextView bt = text(msg, 14, DARK, false);
        bt.setGravity(Gravity.START);
        bt.setBackground(box(Color.WHITE, 18));
        bt.setPadding(dp(14), dp(10), dp(14), dp(10));
        bt.setElevation(dp(2));
        LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(0, WRAP, 1f);
        bp.leftMargin = dp(6);
        buddy.addView(bt, bp);
        c.addView(buddy, lp(WRAP, 12));

        // Tombol lanjut ke level pertama yang belum lulus
        int next = -1;
        for (int l = 0; l < n; l++) {
            if (advStarsOf(k, l) < 1) {
                next = l;
                break;
            }
        }
        if (next >= 0) {
            final int nl = next;
            Button go = button("\u25B6 Lanjut: Level " + (nl + 1) + " \u2022 " + lv(k, nl)[1], GREEN, 15);
            premium(go, GREEN, 28, 2400L);
            c.addView(go, lp(dp(56), 14));
            go.setOnClickListener(v -> startLevel(nl));
        }

        // Peta jalur
        final String[] icons = new String[n];
        final String[] titles = new String[n];
        final int[] stars = new int[n];
        final boolean[] open = new boolean[n];
        for (int l = 0; l < n; l++) {
            String[] p = lv(k, l);
            icons[l] = p[0];
            titles[l] = p[1];
            stars[l] = advStarsOf(k, l);
            open[l] = advOpen(k, l);
        }
        MapView mv = new MapView(this, icons, titles, stars, open, Color.parseColor(WORLD_PAL[k]), li -> {
            if (open[li]) {
                dialogLevel(li);
            } else {
                Toast.makeText(this, "Selesaikan level sebelumnya dulu ya!", Toast.LENGTH_SHORT).show();
            }
        });
        mv.setBackground(box(Color.argb(170, 255, 255, 255), 26));
        c.addView(mv, lp(dp(100) * n + dp(30), 14));

        Button back = button("Kembali ke Menu", GRAY, 14);
        back.setTextColor(DARK);
        c.addView(back, lp(dp(46), 18));
        back.setOnClickListener(v -> showMenu());

        menuBgOn = true;
        wrap(c);
        menuBgOn = false;
    }

    private void dialogLevel(final int l) {
        String[] p = lv(kelas, l);
        int st = advStarsOf(kelas, l);
        LinearLayout c = new LinearLayout(this);
        c.setOrientation(LinearLayout.VERTICAL);
        c.setGravity(Gravity.CENTER_HORIZONTAL);
        c.setPadding(dp(18), dp(16), dp(18), dp(16));
        c.addView(text(p[0], 44, DARK, false), lp(WRAP, 0));
        c.addView(text("Level " + (l + 1) + ": " + p[1], 22, ORANGE_DARK, true), lp(WRAP, 6));
        c.addView(text(levelDesc(p), 14, DARK, false), lp(WRAP, 6));
        c.addView(text(st >= 1 ? starText(st) : "Belum diselesaikan", st >= 1 ? 24 : 14, ORANGE, st < 1), lp(WRAP, 8));
        c.addView(text(timeLabel() + " (bisa diubah di menu)", 12, DARK, false), lp(WRAP, 6));
        c.addView(text("Lulus bila benar separuh atau lebih. Benar 90% ke atas dapat 3 bintang.", 12, DARK, false),
                lp(WRAP, 4));
        Button go = button("\u25B6 Mulai", GREEN, 20);
        flat(go);
        c.addView(go, lp(dp(56), 14));
        Button no = button("Nanti dulu", GRAY, 14);
        no.setTextColor(DARK);
        flat(no);
        c.addView(no, lp(dp(44), 8));
        final AlertDialog d = new AlertDialog.Builder(this).setView(c).create();
        pickDialog = d;
        go.setOnClickListener(v -> {
            d.dismiss();
            startLevel(l);
        });
        no.setOnClickListener(v -> d.dismiss());
        d.show();
        if (d.getWindow() != null) {
            d.getWindow().setBackgroundDrawable(box(Color.WHITE, 24));
            int maxW = getResources().getDisplayMetrics().widthPixels - dp(24);
            d.getWindow().setLayout(Math.min(dp(340), maxW), ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

    // Gambar peta: jalur melengkung, lingkaran level, bintang, dan gembok (semua lewat kode)
    private class MapView extends View {
        private final String[] icons;
        private final String[] titles;
        private final int[] stars;
        private final boolean[] open;
        private final int color;
        private final Pick onTap;
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final TextPaint tp = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        private final Path path = new Path();
        private final DashPathEffect dash = new DashPathEffect(new float[]{dp(10), dp(10)}, 0f);

        MapView(Context ctx, String[] icons, String[] titles, int[] stars, boolean[] open, int color, Pick onTap) {
            super(ctx);
            this.icons = icons;
            this.titles = titles;
            this.stars = stars;
            this.open = open;
            this.color = color;
            this.onTap = onTap;
        }

        private float nodeX(int i, int w) {
            return w * (0.5f + 0.27f * (float) Math.sin(i * Math.PI / 2.0));
        }

        private float nodeY(int i) {
            return dp(56) + i * dp(100);
        }

        private float baseY(Paint p, float cy) {
            return cy - (p.ascent() + p.descent()) / 2f;
        }

        @Override
        protected void onDraw(Canvas c) {
            int w = getWidth();
            int n = icons.length;
            float r = dp(30);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeCap(Paint.Cap.ROUND);
            for (int i = 0; i + 1 < n; i++) {
                float x1 = nodeX(i, w);
                float y1 = nodeY(i);
                float x2 = nodeX(i + 1, w);
                float y2 = nodeY(i + 1);
                float my = (y1 + y2) / 2f;
                path.reset();
                path.moveTo(x1, y1);
                path.cubicTo(x1, my, x2, my, x2, y2);
                boolean lit = stars[i] >= 1;
                paint.setColor(lit ? color : GRAY_D);
                paint.setStrokeWidth(lit ? dp(8) : dp(6));
                paint.setPathEffect(lit ? null : dash);
                c.drawPath(path, paint);
            }
            paint.setPathEffect(null);
            for (int i = 0; i < n; i++) {
                float x = nodeX(i, w);
                float y = nodeY(i);
                boolean isOpen = open[i];
                boolean done = stars[i] >= 1;
                paint.setStyle(Paint.Style.FILL);
                paint.setColor(Color.WHITE);
                c.drawCircle(x, y, r + dp(4), paint);
                paint.setColor(!isOpen ? GRAY_D : done ? GREEN : color);
                c.drawCircle(x, y, r, paint);
                if (isOpen && !done) {
                    paint.setStyle(Paint.Style.STROKE);
                    paint.setStrokeWidth(dp(3));
                    paint.setColor(GOLD);
                    c.drawCircle(x, y, r + dp(9), paint);
                }
                tp.setTextAlign(Paint.Align.CENTER);
                tp.setFakeBoldText(false);
                tp.setColor(Color.WHITE);
                tp.setTextSize(dp(isOpen ? 26 : 22));
                c.drawText(isOpen ? icons[i] : "\uD83D\uDD12", x, baseY(tp, y), tp);
                if (done) {
                    tp.setTextSize(dp(15));
                    for (int j = 0; j < 3; j++) {
                        tp.setColor(j < stars[i] ? GOLD : GRAY_D);
                        c.drawText("\u2605", x + (j - 1) * dp(17), baseY(tp, y + r + dp(18)), tp);
                    }
                }
                tp.setFakeBoldText(true);
                tp.setTextSize(dp(13));
                tp.setColor(isOpen ? DARK : SILVER);
                boolean right = x <= w / 2f + 1f;
                float gap = r + dp(16);
                float avail = right ? (w - x - gap - dp(6)) : (x - gap - dp(6));
                String lab = (i + 1) + ". " + titles[i];
                CharSequence ell = TextUtils.ellipsize(lab, tp, Math.max(dp(40), avail), TextUtils.TruncateAt.END);
                tp.setTextAlign(right ? Paint.Align.LEFT : Paint.Align.RIGHT);
                c.drawText(ell, 0, ell.length(), right ? x + gap : x - gap, baseY(tp, y), tp);
            }
        }

        @Override
        public boolean onTouchEvent(MotionEvent e) {
            int act = e.getActionMasked();
            if (act == MotionEvent.ACTION_DOWN) {
                return true;
            }
            if (act == MotionEvent.ACTION_UP) {
                int w = getWidth();
                float lim = dp(44);
                for (int i = 0; i < icons.length; i++) {
                    float dx = e.getX() - nodeX(i, w);
                    float dy = e.getY() - nodeY(i);
                    if (dx * dx + dy * dy <= lim * lim) {
                        performClick();
                        onTap.on(i);
                        return true;
                    }
                }
            }
            return true;
        }

        @Override
        public boolean performClick() {
            return super.performClick();
        }
    }

    // =====================================================================
    // Kuis Alam (versi 6.0.6): soal bertema alam untuk Matematika, Indonesia, dan Inggris
    // Soal buatan Claude, perlu dicek guru kelas.
    // =====================================================================

    private boolean alam;

    private boolean alamOn() {
        return alam && mode == 7 && advLevel < 0;
    }

    private void startAlam(int s) {
        subj = s;
        advPart = -1;
        alam = true;
        tebak = false;
        sambung = false;
        startGame(7);
    }

    // ---------- Sambung Ayat + Kebun Hafalan (versi 6.29.0), Juz 30 ----------
    private boolean sambung;
    private boolean urut;
    private int sambungSurah = -1;

    private boolean sambungOn() {
        return sambung && mode == 7 && advLevel < 0;
    }

    private void startUrut(int si) {
        startSambung(si, true);
    }

    private void startSambung(int si) {
        startSambung(si, false);
    }

    private void startSambung(int si, boolean sus) {
        urut = sus;
        if (!SambungAyat.load(this)) {
            Toast.makeText(this, "Data ayat tidak bisa dibuka", Toast.LENGTH_LONG).show();
            return;
        }
        sambungSurah = si;
        subj = 1;
        advPart = -1;
        alam = false;
        tebak = false;
        sambung = true;
        startGame(7);
    }

    // Soal: ayat sebelumnya -> pilih ayat berikutnya. Ayat ke-1 ditanya lewat nama surahnya.
    private void buildSambung() {
        if (!SambungAyat.load(this)) {
            sambung = false;
            buildBuiltin();
            return;
        }
        if (urut) {
            buildUrut();
            return;
        }
        final int ns = SambungAyat.JUMLAH_SURAH;
        ArrayList<int[]> items = new ArrayList<>();
        for (int si = 0; si < ns; si++) {
            if (sambungSurah >= 0 && si != sambungSurah) {
                continue;
            }
            for (int ai = 0; ai < SambungAyat.jumlahAyat(si); ai++) {
                items.add(new int[]{si, ai});
            }
        }
        Collections.shuffle(items, rnd);
        total = Math.min(qCount(), items.size());
        for (int i = 0; i < total; i++) {
            int si = items.get(i)[0];
            int ai = items.get(i)[1];
            String right = SambungAyat.ayat(si, ai);
            String stem = ai > 0 ? SambungAyat.ayat(si, ai - 1) : "";
            ArrayList<String> cand = new ArrayList<>();
            if (ai == 0) {
                for (int s2 = 0; s2 < ns; s2++) {
                    if (s2 != si) {
                        cand.add(SambungAyat.ayat(s2, 0));
                    }
                }
            } else {
                for (int a2 = 0; a2 < SambungAyat.jumlahAyat(si); a2++) {
                    if (a2 != ai && a2 != ai - 1) {
                        cand.add(SambungAyat.ayat(si, a2));
                    }
                }
            }
            Collections.shuffle(cand, rnd);
            ArrayList<String> wrong = new ArrayList<>();
            for (String s : cand) {
                if (wrong.size() == 3) {
                    break;
                }
                if (!s.equals(right) && !s.equals(stem) && !wrong.contains(s)) {
                    wrong.add(s);
                }
            }
            // Surah sangat pendek: lengkapi pengecoh dari surah lain
            int guard = 0;
            while (wrong.size() < 3 && guard++ < 500) {
                int s2 = rnd.nextInt(ns);
                String s = SambungAyat.ayat(s2, rnd.nextInt(SambungAyat.jumlahAyat(s2)));
                if (!s.equals(right) && !s.equals(stem) && !wrong.contains(s)) {
                    wrong.add(s);
                }
            }
            ArrayList<String> opts = new ArrayList<>();
            opts.add(right);
            opts.addAll(wrong);
            Collections.shuffle(opts, rnd);
            Q q = new Q();
            q.text = ai == 0
                    ? "Surah " + SambungAyat.NAMA[si] + "\nDimulai dengan ayat…?"
                    : stem + "\n" + SambungAyat.NAMA[si] + " ayat " + ai + " → ayat berikutnya?";
            q.full = q.text;
            q.sopts = new String[4];
            for (int j = 0; j < 4; j++) {
                q.sopts[j] = opts.get(j);
                if (q.sopts[j].equals(right)) {
                    q.correctIdx = j;
                }
            }
            q.expl = "QS. " + SambungAyat.NAMA[si] + ": " + (ai + 1) + " — " + SambungAyat.terjemah(si, ai);
            q.key = si + ":" + ai;
            qs[i] = q;
        }
    }

    // Urut Ayat: 3 ayat berurutan ditampilkan acak (A, B, C); pilih urutan yang benar. Tidak mengubah Kebun.
    private void buildUrut() {
        ArrayList<int[]> items = new ArrayList<>();
        for (int si = 0; si < SambungAyat.JUMLAH_SURAH; si++) {
            if (sambungSurah >= 0 && si != sambungSurah) {
                continue;
            }
            for (int ai = 0; ai + 2 < SambungAyat.jumlahAyat(si); ai++) {
                int len = SambungAyat.ayat(si, ai).length() + SambungAyat.ayat(si, ai + 1).length()
                        + SambungAyat.ayat(si, ai + 2).length();
                if (len <= 300) {
                    items.add(new int[]{si, ai});
                }
            }
        }
        if (items.isEmpty()) {
            sambung = false;
            urut = false;
            buildBuiltin();
            return;
        }
        Collections.shuffle(items, rnd);
        total = Math.min(qCount(), items.size());
        final int[][] P = {{0, 1, 2}, {0, 2, 1}, {1, 0, 2}, {1, 2, 0}, {2, 0, 1}, {2, 1, 0}};
        final String L = "ABC";
        for (int i = 0; i < total; i++) {
            int si = items.get(i)[0];
            int ai = items.get(i)[1];
            int[] p = P[1 + rnd.nextInt(5)]; // tampilan A,B,C = ayat ke-(ai+p[k]); tidak pernah sudah urut
            StringBuilder sb = new StringBuilder("Urutkan ayat QS. " + SambungAyat.NAMA[si] + ":");
            for (int k = 0; k < 3; k++) {
                sb.append("\n\n(").append(L.charAt(k)).append(") ").append(SambungAyat.ayat(si, ai + p[k]));
            }
            // urutan benar: label dari ayat ke-0, 1, 2
            String right = "";
            for (int t = 0; t < 3; t++) {
                for (int k = 0; k < 3; k++) {
                    if (p[k] == t) {
                        right += (t > 0 ? " \u2192 " : "") + L.charAt(k);
                    }
                }
            }
            ArrayList<String> opts = new ArrayList<>();
            opts.add(right);
            ArrayList<int[]> perms = new ArrayList<>(Arrays.asList(P));
            Collections.shuffle(perms, rnd);
            for (int[] q2 : perms) {
                if (opts.size() == 4) {
                    break;
                }
                String o = L.charAt(q2[0]) + " \u2192 " + L.charAt(q2[1]) + " \u2192 " + L.charAt(q2[2]);
                if (!opts.contains(o)) {
                    opts.add(o);
                }
            }
            Collections.shuffle(opts, rnd);
            Q q = new Q();
            q.text = sb.toString();
            q.full = q.text;
            q.sopts = new String[4];
            for (int j = 0; j < 4; j++) {
                q.sopts[j] = opts.get(j);
                if (q.sopts[j].equals(right)) {
                    q.correctIdx = j;
                }
            }
            q.expl = "Urutan benar QS. " + SambungAyat.NAMA[si] + " ayat " + (ai + 1) + "-" + (ai + 3) + ": " + right;
            qs[i] = q;
        }
    }

    // Murid menjawab benar: ayat itu naik satu hari latihan (hanya sekali per hari) + bonus Embun
    private void sambungCatat(Q q) {
        try {
            int c = q.key == null ? -1 : q.key.indexOf(':');
            if (c > 0) {
                SambungAyat.benar(prefs, Integer.parseInt(q.key.substring(0, c)),
                        Integer.parseInt(q.key.substring(c + 1)));
                SambungAyat.tambahEmbun(prefs, 1);
            }
        } catch (Exception e) {
            // pencatatan tidak boleh mengganggu permainan
        }
    }

    private GradientDrawable plainBox(int fill, int line) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(fill);
        g.setCornerRadius(dp(16));
        g.setStroke(dp(2), line);
        return g;
    }

    private int kebunSelectedSi = -1;

    private GradientDrawable gardenPlotBg(boolean selected, boolean mutqin) {
        GradientDrawable g = new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                mutqin
                        ? new int[]{Color.parseColor("#FEF9C3"), Color.parseColor("#DCFCE7"), Color.parseColor("#8D5524")}
                        : new int[]{Color.parseColor("#E8F5E9"), Color.parseColor("#D7ECD9"), Color.parseColor("#8D5524")}
        );
        g.setCornerRadius(dp(18));
        g.setStroke(dp(selected ? 3 : 2), selected ? Color.parseColor("#F59E0B") : Color.parseColor("#C8E6C9"));
        return g;
    }

    // ---------- Kebun Hafalan (desain ulang: kebun 2D, benih, tanaman tumbuh) ----------
    private interface IntPick {
        void pick(int si);
    }

    private int kebunScrollX = 0;
    private boolean kebunNoGrow = false;

    private GradientDrawable rr(int color, int strokeColor, int strokeDp, int radiusDp) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(color);
        g.setCornerRadius(dp(radiusDp));
        if (strokeDp > 0) {
            g.setStroke(dp(strokeDp), strokeColor);
        }
        return g;
    }

    /** Gambar satu tanaman. stage: 0 benih, 1 tunas, 2 pohon, 3 pohon emas. by = garis tanah. */
    private void drawPlant(Canvas c, Paint p, float cx, float by, float s, int stage, float ang, float tw, boolean shadow) {
        p.setShader(null);
        p.setStyle(Paint.Style.FILL);
        if (shadow && stage > 0) {
            p.setColor(Color.argb(45, 40, 20, 0));
            c.drawOval(new RectF(cx - s * 0.26f, by - s * 0.045f, cx + s * 0.26f, by + s * 0.045f), p);
        }
        if (stage == 0) {
            p.setColor(Color.parseColor("#8A5428"));
            c.drawOval(new RectF(cx - s * 0.2f, by - s * 0.06f, cx + s * 0.2f, by + s * 0.05f), p);
            c.save();
            c.rotate(-25f, cx, by - s * 0.12f);
            p.setColor(Color.parseColor("#7B4A24"));
            c.drawOval(new RectF(cx - s * 0.1f, by - s * 0.24f, cx + s * 0.1f, by - s * 0.03f), p);
            p.setColor(Color.argb(110, 255, 220, 170));
            c.drawOval(new RectF(cx - s * 0.05f, by - s * 0.21f, cx - s * 0.01f, by - s * 0.12f), p);
            c.restore();
            return;
        }
        c.save();
        c.rotate(ang, cx, by);
        if (stage == 1) {
            float top = by - s * 0.46f;
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeCap(Paint.Cap.ROUND);
            p.setStrokeWidth(s * 0.07f);
            p.setColor(Color.parseColor("#4E9A3A"));
            c.drawLine(cx, by, cx, top, p);
            p.setStyle(Paint.Style.FILL);
            c.save();
            c.translate(cx, top);
            c.rotate(-32f);
            p.setColor(Color.parseColor("#68B84B"));
            c.drawOval(new RectF(-s * 0.3f, -s * 0.1f, 0f, s * 0.1f), p);
            p.setColor(Color.argb(120, 185, 231, 124));
            c.drawOval(new RectF(-s * 0.26f, -s * 0.07f, -s * 0.06f, 0f), p);
            c.restore();
            c.save();
            c.translate(cx, top);
            c.rotate(32f);
            p.setColor(Color.parseColor("#7FCB5A"));
            c.drawOval(new RectF(0f, -s * 0.1f, s * 0.3f, s * 0.1f), p);
            p.setColor(Color.argb(120, 185, 231, 124));
            c.drawOval(new RectF(s * 0.06f, -s * 0.07f, s * 0.26f, 0f), p);
            c.restore();
        } else {
            float k = stage == 2 ? 0.86f : 1f;
            float z = s * k;
            float cy = by - z * 0.6f;
            if (stage == 3) {
                float pulse = (float) (0.5 + 0.5 * Math.sin(tw));
                float gr = z * (0.7f + 0.08f * pulse);
                p.setShader(new android.graphics.RadialGradient(cx, cy, gr,
                        Color.argb((int) (120 + 60 * pulse), 255, 214, 90), Color.argb(0, 255, 214, 90), Shader.TileMode.CLAMP));
                c.drawCircle(cx, cy, gr, p);
                p.setShader(null);
            }
            p.setColor(Color.parseColor("#A96C3D"));
            c.drawRoundRect(new RectF(cx - z * 0.065f, cy, cx + z * 0.065f, by), z * 0.03f, z * 0.03f, p);
            p.setColor(Color.parseColor("#4E9A3A"));
            c.drawCircle(cx, cy, z * 0.3f, p);
            p.setColor(Color.parseColor("#5DAA45"));
            c.drawCircle(cx - z * 0.2f, cy + z * 0.08f, z * 0.22f, p);
            c.drawCircle(cx + z * 0.2f, cy + z * 0.08f, z * 0.22f, p);
            p.setColor(Color.parseColor("#68B84B"));
            c.drawCircle(cx, cy - z * 0.08f, z * 0.24f, p);
            p.setColor(Color.argb(150, 185, 231, 124));
            c.drawCircle(cx - z * 0.09f, cy - z * 0.17f, z * 0.09f, p);
            if (stage == 3) {
                float[][] ap = {{-0.15f, -0.05f}, {0.18f, 0.02f}, {0f, 0.12f}, {-0.2f, 0.14f}, {0.1f, -0.17f}};
                for (float[] a : ap) {
                    float ax = cx + a[0] * z;
                    float ay = cy + a[1] * z;
                    p.setColor(Color.parseColor("#E0A21A"));
                    c.drawCircle(ax, ay, z * 0.062f, p);
                    p.setColor(Color.parseColor("#F6C84B"));
                    c.drawCircle(ax - z * 0.004f, ay - z * 0.006f, z * 0.052f, p);
                    p.setColor(Color.argb(200, 255, 255, 255));
                    c.drawCircle(ax - z * 0.02f, ay - z * 0.022f, z * 0.014f, p);
                }
            }
        }
        c.restore();
        if (stage == 3) {
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeCap(Paint.Cap.ROUND);
            p.setStrokeWidth(s * 0.025f);
            float[][] sp = {{-0.34f, -0.78f}, {0.36f, -0.62f}, {0.02f, -1.0f}};
            for (int i = 0; i < sp.length; i++) {
                float al = (float) (0.5 + 0.5 * Math.sin(tw * 1.0 + i * 2.1));
                p.setColor(Color.argb((int) (230 * al), 255, 243, 176));
                float sx = cx + sp[i][0] * s;
                float sy = by + sp[i][1] * s;
                float r = s * (0.05f + 0.05f * al);
                c.drawLine(sx - r, sy, sx + r, sy, p);
                c.drawLine(sx, sy - r, sx, sy + r, p);
            }
            p.setStyle(Paint.Style.FILL);
        }
    }

    /** Ikon tanaman kecil (kartu benih dan header). */
    private class SeedIcon extends View {
        private final int stage;
        private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);

        SeedIcon(Context ctx, int stage) {
            super(ctx);
            this.stage = stage;
        }

        @Override
        protected void onDraw(Canvas c) {
            float w = getWidth();
            float h = getHeight();
            float sz = Math.min(w, h) * (stage == 0 ? 1.7f : 0.82f);
            drawPlant(c, p, w / 2f, h * (stage == 0 ? 0.78f : 0.94f), sz, stage, 0f, 0f, false);
        }
    }

    /** Kantong benih. */
    private class SeedBagView extends View {
        private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Path path = new Path();

        SeedBagView(Context ctx) {
            super(ctx);
        }

        @Override
        protected void onDraw(Canvas c) {
            float w = getWidth();
            float h = getHeight();
            float cx = w / 2f;
            p.setStyle(Paint.Style.FILL);
            p.setShader(new android.graphics.RadialGradient(cx, h * 0.52f, w * 0.5f,
                    Color.argb(190, 255, 236, 160), Color.argb(0, 255, 236, 160), Shader.TileMode.CLAMP));
            c.drawCircle(cx, h * 0.52f, w * 0.5f, p);
            p.setShader(null);
            float bw = w * 0.5f;
            float top = h * 0.24f;
            float bot = h * 0.86f;
            path.reset();
            path.moveTo(cx - bw / 2f, top);
            path.lineTo(cx + bw / 2f, top);
            path.lineTo(cx + bw / 2f + w * 0.03f, bot);
            path.lineTo(cx - bw / 2f - w * 0.03f, bot);
            path.close();
            p.setColor(Color.parseColor("#E6BC7E"));
            c.drawPath(path, p);
            p.setColor(Color.parseColor("#F3D7A2"));
            c.drawRect(cx - bw / 2f, top - h * 0.1f, cx + bw / 2f, top + h * 0.03f, p);
            p.setColor(Color.argb(60, 120, 70, 20));
            c.drawRect(cx + bw * 0.2f, top, cx + bw / 2f + w * 0.03f, bot, p);
            p.setColor(Color.parseColor("#FFF8E6"));
            c.drawRoundRect(new RectF(cx - bw * 0.34f, top + h * 0.17f, cx + bw * 0.34f, bot - h * 0.08f), w * 0.06f, w * 0.06f, p);
            drawPlant(c, p, cx, bot - h * 0.1f, w * 0.42f, 1, 0f, 0f, false);
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeCap(Paint.Cap.ROUND);
            p.setStrokeWidth(w * 0.025f);
            p.setColor(Color.parseColor("#F6C84B"));
            float sx = cx + bw * 0.42f;
            float sy = top + h * 0.02f;
            float r = w * 0.08f;
            c.drawLine(sx - r, sy, sx + r, sy, p);
            c.drawLine(sx, sy - r, sx, sy + r, p);
            p.setStyle(Paint.Style.FILL);
        }
    }

    /** Area kebun utama: langit, pagar, papan, tanah, dan tanaman yang tumbuh. */
    private class GardenView extends View {
        private final int[] si;
        private final int[] stg;
        private final boolean[] haus;
        private final int sel;
        private final int done;
        private final int tot;
        private final IntPick cb;
        private final boolean animate;
        private final float[] hx;
        private final float[] hy;
        private float hr = 0f;
        private float rx = -1f;
        private float ry = -1f;
        private int hausN = 0;
        private long tapMs = 0L;
        private float grow = 0f;
        private float tm = 0f;
        private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Path path = new Path();
        private final RectF rf = new RectF();
        private ValueAnimator gA;
        private ValueAnimator tA;
        private final float d = getResources().getDisplayMetrics().density;
        private final Paint ghostP = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
        private final android.view.animation.Interpolator ov = new OvershootInterpolator(2.2f);
        private String pctTxt = "0%";
        private String doneTxt = "";

        GardenView(Context ctx, int[] si, int[] stg, boolean[] haus, int sel, int done, int tot,
                   boolean animate, IntPick cb) {
            super(ctx);
            this.si = si;
            this.stg = stg;
            this.haus = haus;
            this.sel = sel;
            this.done = done;
            this.tot = tot;
            this.cb = cb;
            this.animate = animate;
            ghostP.setAlpha(128);
            this.pctTxt = Math.round((tot > 0 ? Math.min(1f, done / (float) tot) : 0f) * 100) + "%";
            this.doneTxt = done + " / " + tot + " Ayat";
            this.hx = new float[si.length];
            this.hy = new float[si.length];
            this.grow = animate ? 0f : 1f;
            for (boolean hb : haus) {
                if (hb) {
                    hausN++;
                }
            }
        }

        @Override
        protected void onAttachedToWindow() {
            super.onAttachedToWindow();
            if (animate) {
                gA = ValueAnimator.ofFloat(0f, 1f);
                gA.setDuration(1300);
                gA.addUpdateListener(a -> {
                    grow = (float) a.getAnimatedValue();
                    invalidate();
                });
                gA.start();
            }
            tA = ValueAnimator.ofFloat(0f, (float) (Math.PI * 2));
            tA.setDuration(5200);
            tA.setRepeatCount(ValueAnimator.INFINITE);
            tA.setInterpolator(new android.view.animation.LinearInterpolator());
            tA.addUpdateListener(a -> {
                tm = (float) a.getAnimatedValue();
                invalidate();
            });
            tA.start();
        }

        @Override
        protected void onDetachedFromWindow() {
            super.onDetachedFromWindow();
            if (gA != null) {
                gA.cancel();
            }
            if (tA != null) {
                tA.cancel();
            }
        }

        private void cloud(Canvas c, float cx, float cy, float r) {
            p.setColor(Color.argb(235, 255, 255, 255));
            c.drawCircle(cx, cy, r, p);
            c.drawCircle(cx + r * 0.95f, cy + r * 0.2f, r * 0.75f, p);
            c.drawCircle(cx - r * 0.95f, cy + r * 0.25f, r * 0.65f, p);
            rf.set(cx - r * 1.5f, cy + r * 0.25f, cx + r * 1.7f, cy + r * 0.95f);
            c.drawRoundRect(rf, r * 0.4f, r * 0.4f, p);
        }

        private void bush(Canvas c, float cx, float cy, float r) {
            p.setColor(Color.parseColor("#4E9A3A"));
            c.drawCircle(cx, cy, r, p);
            p.setColor(Color.parseColor("#68B84B"));
            c.drawCircle(cx - r * 0.3f, cy - r * 0.15f, r * 0.8f, p);
            c.drawCircle(cx + r * 0.55f, cy + r * 0.1f, r * 0.6f, p);
            p.setColor(Color.argb(120, 185, 231, 124));
            c.drawCircle(cx - r * 0.5f, cy - r * 0.45f, r * 0.28f, p);
        }

        private void flower(Canvas c, float x, float y, float r) {
            p.setColor(Color.WHITE);
            for (int i = 0; i < 5; i++) {
                double a = i * Math.PI * 2 / 5;
                c.drawCircle(x + (float) Math.cos(a) * r, y + (float) Math.sin(a) * r, r * 0.8f, p);
            }
            p.setColor(Color.parseColor("#F6C84B"));
            c.drawCircle(x, y, r * 0.65f, p);
        }

        /** Teks papan: dikecilkan bila lebih lebar dari area kayu (maxW). Tanpa alokasi. */
        private void fitText(Canvas c, String s, float cx, float y, float size, float maxW) {
            p.setStyle(Paint.Style.FILL);
            p.setTypeface(ROUND_BOLD);
            p.setTextAlign(Paint.Align.CENTER);
            p.setColor(Color.parseColor("#5A3416"));
            p.setTextSize(size);
            float tw = p.measureText(s);
            if (tw > maxW && tw > 0f) {
                p.setTextSize(size * maxW / tw);
            }
            c.drawText(s, cx, y, p);
        }

        // ================= LATAR & PAPAN KEBUN (digambar kode) =================

        private void tree(Canvas c, float cx, float baseY, float hgt) {
            p.setStyle(Paint.Style.FILL);
            p.setShader(null);
            p.setColor(Color.parseColor("#9A6A3A"));
            rf.set(cx - hgt * 0.06f, baseY - hgt * 0.42f, cx + hgt * 0.06f, baseY);
            c.drawRect(rf, p);
            float r = hgt * 0.30f;
            float cy = baseY - hgt * 0.62f;
            p.setColor(Color.parseColor("#4E9A3A"));
            c.drawCircle(cx, cy, r, p);
            c.drawCircle(cx - r * 0.75f, cy + r * 0.35f, r * 0.72f, p);
            c.drawCircle(cx + r * 0.75f, cy + r * 0.35f, r * 0.72f, p);
            p.setColor(Color.parseColor("#68B84B"));
            c.drawCircle(cx - r * 0.2f, cy - r * 0.15f, r * 0.7f, p);
            c.drawCircle(cx + r * 0.6f, cy + r * 0.2f, r * 0.5f, p);
            p.setColor(Color.argb(110, 200, 240, 140));
            c.drawCircle(cx - r * 0.45f, cy - r * 0.45f, r * 0.25f, p);
        }

        private void leaf(Canvas c, float x, float y, float len, float angle, String color) {
            c.save();
            c.rotate(angle, x, y);
            p.setStyle(Paint.Style.FILL);
            p.setShader(null);
            p.setColor(Color.parseColor(color));
            rf.set(x, y - len * 0.3f, x + len, y + len * 0.3f);
            c.drawOval(rf, p);
            c.restore();
        }

        /** Latar kebun: langit, matahari, awan, bukit, pohon, dan hamparan rumput. */
        private void drawScene(Canvas c, float w, float h, float fenceTop) {
            float hor = fenceTop - d * 14;
            p.setStyle(Paint.Style.FILL);
            p.setShader(new LinearGradient(0, 0, 0, hor + d * 24, Color.parseColor("#7CC8F0"),
                    Color.parseColor("#E2F5E6"), Shader.TileMode.CLAMP));
            c.drawRect(0, 0, w, hor + d * 24, p);
            p.setShader(null);
            // matahari
            float sx = w * 0.52f;
            float sy = Math.max(d * 20, hor * 0.32f);
            p.setShader(new android.graphics.RadialGradient(sx, sy, d * 44, Color.argb(200, 255, 244, 190),
                    Color.argb(0, 255, 244, 190), Shader.TileMode.CLAMP));
            c.drawCircle(sx, sy, d * 44, p);
            p.setShader(null);
            p.setColor(Color.parseColor("#FFE79A"));
            c.drawCircle(sx, sy, d * 11, p);
            // awan (bergerak pelan)
            cloud(c, w * 0.30f + (float) Math.sin(tm) * d * 5, hor * 0.42f, d * 11);
            cloud(c, w * 0.80f + (float) Math.sin(tm + 2f) * d * 5, hor * 0.30f, d * 9);
            // bukit
            p.setColor(Color.parseColor("#A7DC7E"));
            rf.set(-w * 0.25f, hor - d * 34, w * 0.60f, hor + d * 40);
            c.drawOval(rf, p);
            p.setColor(Color.parseColor("#93D06C"));
            rf.set(w * 0.35f, hor - d * 28, w * 1.25f, hor + d * 40);
            c.drawOval(rf, p);
            // pohon di kejauhan
            tree(c, w * 0.07f, hor + d * 4, d * 46);
            tree(c, w * 0.21f, hor + d * 5, d * 32);
            tree(c, w * 0.47f, hor + d * 3, d * 24);
            tree(c, w * 0.60f, hor + d * 4, d * 30);
            tree(c, w * 0.93f, hor + d * 4, d * 48);
            // hamparan rumput
            p.setShader(new LinearGradient(0, hor, 0, h, Color.parseColor("#B3E07D"),
                    Color.parseColor("#CDEB9A"), Shader.TileMode.CLAMP));
            c.drawRect(0, hor, w, h, p);
            p.setShader(null);
            // semak dan bunga di tepi
            bush(c, -d * 4, fenceTop + d * 8, d * 24);
            bush(c, w + d * 4, fenceTop + d * 10, d * 26);
            flower(c, w * 0.04f, fenceTop + d * 4, d * 3.5f);
            flower(c, w * 0.97f, fenceTop + d * 6, d * 3.8f);
            flower(c, w * 0.52f, fenceTop + d * 4, d * 3f);
        }

        /** Pagar kayu rendah sepanjang bagian atas petak. */
        private void drawFence(Canvas c, float w, float fenceTop) {
            float fh = d * 26;
            float top = fenceTop - fh;
            float pw = d * 10;
            float gap = d * 7;
            p.setStyle(Paint.Style.FILL);
            p.setShader(null);
            p.setColor(Color.argb(40, 60, 100, 20));
            rf.set(0, fenceTop - d * 2, w, fenceTop + d * 8);
            c.drawRect(rf, p);
            for (float x = d * 2; x < w; x += pw + gap) {
                rf.set(x, top, x + pw, fenceTop + d * 6);
                p.setColor(Color.parseColor("#EBBB76"));
                c.drawRoundRect(rf, d * 4, d * 4, p);
                rf.set(x + pw * 0.62f, top + d * 3, x + pw, fenceTop + d * 6);
                p.setColor(Color.argb(50, 120, 70, 20));
                c.drawRect(rf, p);
            }
            p.setColor(Color.parseColor("#D9A055"));
            float y1 = top + fh * 0.28f;
            float y2 = top + fh * 0.68f;
            rf.set(0, y1, w, y1 + d * 4);
            c.drawRect(rf, p);
            rf.set(0, y2, w, y2 + d * 4);
            c.drawRect(rf, p);
        }

        private void signPosts(Canvas c, float l, float r, float bb, float fenceTop) {
            p.setStyle(Paint.Style.FILL);
            p.setShader(null);
            float[] px = {l + (r - l) * 0.2f, l + (r - l) * 0.8f};
            for (float x : px) {
                rf.set(x - d * 3.5f, bb - d * 4, x + d * 3.5f, fenceTop + d * 14);
                p.setColor(Color.parseColor("#B9783E"));
                c.drawRoundRect(rf, d * 2, d * 2, p);
                rf.set(x + d * 0.5f, bb - d * 4, x + d * 3.5f, fenceTop + d * 14);
                p.setColor(Color.argb(60, 70, 30, 0));
                c.drawRect(rf, p);
            }
        }

        private void signBase(Canvas c, float l, float r, float fenceTop) {
            float[] px = {l + (r - l) * 0.2f, l + (r - l) * 0.8f};
            for (float x : px) {
                bush(c, x, fenceTop + d * 1, d * 11);
            }
            flower(c, l + (r - l) * 0.5f, fenceTop - d * 1, d * 3f);
        }

        /** Papan kayu: bayangan, gradasi, serat, bingkai, dan paku. */
        private void signBoard(Canvas c, float l, float t, float r, float b) {
            p.setStyle(Paint.Style.FILL);
            p.setShader(null);
            p.setColor(Color.argb(55, 50, 25, 0));
            rf.set(l + d * 1.5f, t + d * 3, r + d * 1.5f, b + d * 3);
            c.drawRoundRect(rf, d * 9, d * 9, p);
            rf.set(l, t, r, b);
            p.setShader(new LinearGradient(0, t, 0, b, Color.parseColor("#F6D595"),
                    Color.parseColor("#E2A95F"), Shader.TileMode.CLAMP));
            c.drawRoundRect(rf, d * 9, d * 9, p);
            p.setShader(null);
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(d * 1f);
            p.setColor(Color.argb(35, 120, 70, 20));
            float my = t + (b - t) * 0.55f;
            c.drawLine(l + d * 10, my, r - d * 10, my, p);
            p.setStrokeWidth(d * 2.5f);
            p.setColor(Color.parseColor("#A5683A"));
            rf.set(l, t, r, b);
            c.drawRoundRect(rf, d * 9, d * 9, p);
            p.setStyle(Paint.Style.FILL);
            p.setColor(Color.parseColor("#8A5428"));
            c.drawCircle(l + d * 6, b - d * 6, d * 1.6f, p);
            c.drawCircle(r - d * 6, b - d * 6, d * 1.6f, p);
        }

        private void signText(Canvas c, String s, float x, float y, float size, float maxW, Paint.Align al) {
            p.setStyle(Paint.Style.FILL);
            p.setShader(null);
            p.setTypeface(ROUND_BOLD);
            p.setTextAlign(al);
            p.setColor(Color.parseColor("#5A3416"));
            p.setTextSize(size);
            float tw = p.measureText(s);
            if (tw > maxW && tw > 0f) {
                p.setTextSize(size * maxW / tw);
            }
            c.drawText(s, x, y, p);
        }

        private void drawProgressSign(Canvas c, float l, float t, float r, float b) {
            float h = b - t;
            float pad = d * 9;
            float frac = tot > 0 ? Math.min(1f, done / (float) tot) : 0f;
            float y1 = t + h * 0.36f;
            float icon = Math.min(d * 22, h * 0.4f);
            float x = l + pad;
            android.graphics.Bitmap tn = kBmp("ic_tunas");
            if (tn != null) {
                float iw = icon * tn.getWidth() / tn.getHeight();
                kRf.set(x, y1 - icon / 2f, x + iw, y1 + icon / 2f);
                c.drawBitmap(tn, null, kRf, kPaint);
                x += iw + d * 5;
            } else {
                drawPlant(c, p, x + d * 10, y1 + d * 9, d * 20, 1, 0f, 0f, false);
                x += d * 24;
            }
            p.setStyle(Paint.Style.FILL);
            p.setShader(null);
            p.setTypeface(ROUND_BOLD);
            p.setTextSize(d * 11.5f);
            float pctW = p.measureText("100%");
            float barR = r - pad - pctW - d * 5;
            float barH = d * 9;
            rf.set(x, y1 - barH / 2f, barR, y1 + barH / 2f);
            p.setColor(Color.parseColor("#8A5A2B"));
            c.drawRoundRect(rf, barH / 2f, barH / 2f, p);
            if (frac > 0f) {
                float fx0 = x + d * 1.5f;
                float fx1 = Math.max(fx0 + d * 7, fx0 + (barR - x - d * 3) * frac);
                rf.set(fx0, y1 - barH / 2f + d * 1.5f, fx1, y1 + barH / 2f - d * 1.5f);
                p.setColor(Color.parseColor("#7BC95A"));
                c.drawRoundRect(rf, barH / 2f, barH / 2f, p);
            }
            signText(c, pctTxt, r - pad, y1 + d * 4.2f, d * 11.5f, pctW, Paint.Align.RIGHT);
            // baris kedua: ikon buku + jumlah ayat
            float y2 = t + h * 0.76f;
            float px0 = l + pad;
            android.graphics.Bitmap bk = kBmp("ic_buku");
            if (bk != null) {
                float bi = Math.min(d * 16, h * 0.3f);
                float bw = bi * bk.getWidth() / bk.getHeight();
                kRf.set(px0, y2 - bi / 2f, px0 + bw, y2 + bi / 2f);
                c.drawBitmap(bk, null, kRf, kPaint);
                px0 += bw + d * 4;
            }
            float pr0 = r - pad;
            float pillH = d * 16;
            rf.set(px0, y2 - pillH / 2f, pr0, y2 + pillH / 2f);
            p.setColor(Color.argb(165, 255, 246, 222));
            c.drawRoundRect(rf, pillH / 2f, pillH / 2f, p);
            signText(c, doneTxt, (px0 + pr0) / 2f, y2 + d * 4f, d * 11f, (pr0 - px0) - d * 8, Paint.Align.CENTER);
        }

        private void drawNameSign(Canvas c, float l, float t, float r, float b) {
            float h = b - t;
            float cy = t + h * 0.5f;
            float maxW = (r - l) - d * 20;
            float icon = Math.min(d * 24, h * 0.46f);
            android.graphics.Bitmap tn = kBmp("ic_tunas");
            float iw = tn != null ? icon * tn.getWidth() / tn.getHeight() : 0f;
            float gap = tn != null ? d * 4 : 0f;
            p.setTypeface(ROUND_BOLD);
            float size = d * 15;
            p.setTextSize(size);
            float tw = p.measureText("Kebunmu");
            if (tw + iw + gap > maxW) {
                size = size * maxW / (tw + iw + gap);
                p.setTextSize(size);
                tw = p.measureText("Kebunmu");
            }
            float x0 = (l + r) / 2f - (iw + gap + tw) / 2f;
            if (tn != null) {
                kRf.set(x0, cy - icon / 2f, x0 + iw, cy + icon / 2f);
                c.drawBitmap(tn, null, kRf, kPaint);
            }
            signText(c, "Kebunmu", x0 + iw + gap, cy + size * 0.35f, size, tw + d, Paint.Align.LEFT);
        }


        private void board(Canvas c, float l, float t, float r, float b, float rot) {
            c.save();
            c.rotate(rot, (l + r) / 2f, (t + b) / 2f);
            rf.set(l, t, r, b);
            p.setStyle(Paint.Style.FILL);
            p.setColor(Color.parseColor("#EFC27F"));
            c.drawRoundRect(rf, d * 8, d * 8, p);
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(d * 2.5f);
            p.setColor(Color.parseColor("#A96C3D"));
            c.drawRoundRect(rf, d * 8, d * 8, p);
            p.setStyle(Paint.Style.FILL);
            p.setColor(Color.parseColor("#8A5428"));
            c.drawCircle(l + d * 7, t + d * 7, d * 1.8f, p);
            c.drawCircle(r - d * 7, t + d * 7, d * 1.8f, p);
            c.restore();
        }

        @Override
        protected void onDraw(Canvas c) {
            float w = getWidth();
            float h = getHeight();
            if (w <= 0 || h <= 0) {
                return;
            }
            p.setShader(null);
            p.setStyle(Paint.Style.FILL);
            p.setTypeface(ROUND_BOLD);

            // geometri petak dihitung lebih dulu agar pagar, papan, dan latar menyatu dengan bingkai tanah
            float fl = d * 4;
            float fr = w - d * 4;
            float fb = h - d * 8;
            android.graphics.Bitmap ptB = kBmp("kebun_petak");
            float qs = (fr - fl) / 900f;
            float st = h * 0.45f;
            float sl;
            float sr;
            float sb;
            if (ptB != null) {
                sl = fl + 80 * qs;
                sr = fr - 80 * qs;
                sb = fb - 51 * qs;
            } else {
                sl = fl + d * 11;
                sr = fr - d * 11;
                sb = fb - d * 8;
            }
            if (st > sb - d * 70) {
                st = sb - d * 70;
            }
            float fenceTop = ptB != null ? st - 100 * qs : st - d * 10;

            // latar bertema kebun (digambar kode): langit, bukit, pohon, rumput, pagar
            drawScene(c, w, h, fenceTop);
            drawFence(c, w, fenceTop);

            // dua papan sejajar, berdiri di tanah di belakang bingkai petak
            float sgM = Math.max(d * 10, w * 0.025f);
            float sgB = fenceTop - d * 10;
            float sgH = Math.max(d * 34, Math.min(d * 56, sgB - d * 6));
            float sgT = sgB - sgH;
            float sgL1 = sgM;
            float sgR1 = sgM + w * 0.40f;
            float sgR2 = w - sgM;
            float sgL2 = sgR2 - w * 0.30f;
            signPosts(c, sgL1, sgR1, sgB, fenceTop);
            signPosts(c, sgL2, sgR2, sgB, fenceTop);
            signBoard(c, sgL1, sgT, sgR1, sgB);
            signBoard(c, sgL2, sgT, sgR2, sgB);
            drawProgressSign(c, sgL1, sgT, sgR1, sgB);
            drawNameSign(c, sgL2, sgT, sgR2, sgB);
            // semak + bunga di dasar tiang supaya papan terlihat tumbuh dari kebun
            signBase(c, sgL1, sgR1, fenceTop);
            signBase(c, sgL2, sgR2, fenceTop);
            // hiasan daun dan bunga di sudut papan
            flower(c, sgL1 + d * 5, sgT + d * 4, d * 3.2f);
            flower(c, sgR2 - d * 5, sgT + d * 4, d * 3.2f);
            leaf(c, sgL1 + d * 8, sgT + d * 3, d * 11, -25f, "#4E9A3A");
            leaf(c, sgR1 - d * 8, sgT + d * 3, d * 11, 205f, "#68B84B");
            leaf(c, sgL2 + d * 8, sgT + d * 3, d * 11, -25f, "#68B84B");
            leaf(c, sgR2 - d * 9, sgT + d * 3, d * 11, 205f, "#4E9A3A");

            // tanah: bingkai petak seluruhnya di dalam view (inset 4dp kiri/kanan, 8dp bawah), geometri sudah dihitung di atas
            if (ptB != null) {
                kPiece(c, ptB, 0, 100, fl, st - 100 * qs, fr, st);
                kPiece(c, ptB, 100, 268, fl, st, fr, sb);
                kPiece(c, ptB, 268, 319, fl, sb, fr, fb);
            } else {
            rf.set(sl - d * 7, st - d * 7, sr + d * 7, sb + d * 7);
            p.setStyle(Paint.Style.FILL);
            p.setColor(Color.parseColor("#E7B26A"));
            c.drawRoundRect(rf, d * 22, d * 22, p);
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(d * 2);
            p.setColor(Color.parseColor("#B9783E"));
            c.drawRoundRect(rf, d * 22, d * 22, p);
            p.setStyle(Paint.Style.FILL);
            rf.set(sl, st, sr, sb);
            p.setShader(new LinearGradient(0, st, 0, sb, Color.parseColor("#B47A4A"), Color.parseColor("#96592F"), Shader.TileMode.CLAMP));
            c.drawRoundRect(rf, d * 16, d * 16, p);
            p.setShader(null);
            p.setColor(Color.argb(40, 70, 30, 0));
            for (int i = 0; i < 22; i++) {
                float ox = sl + (sr - sl) * (((i * 37) % 100) / 100f);
                float oy = st + (sb - st) * (((i * 53 + 11) % 100) / 100f);
                c.drawOval(new RectF(ox - d * 7, oy - d * 3.5f, ox + d * 7, oy + d * 3.5f), p);
            }
            p.setColor(Color.argb(45, 0, 0, 0));
            rf.set(sl, st, sr, st + d * 8);
            c.drawRoundRect(rf, d * 8, d * 8, p);

            // rumput di tepi atas tanah
            p.setColor(Color.parseColor("#68B84B"));
            for (int i = 0; i < 14; i++) {
                float gx = sl + (sr - sl) * ((i * 29 % 100) / 100f);
                float gh = d * (6 + (i % 3) * 2);
                path.reset();
                path.moveTo(gx - d * 3, st + d * 2);
                path.lineTo(gx, st - gh + d * 4);
                path.lineTo(gx + d * 3, st + d * 2);
                path.close();
                c.drawPath(path, p);
            }
            // batu kecil
            p.setColor(Color.parseColor("#B8B2A6"));
            c.drawOval(new RectF(sr - d * 34, sb - d * 20, sr - d * 16, sb - d * 10), p);
            p.setColor(Color.parseColor("#9C968A"));
            c.drawOval(new RectF(sr - d * 26, sb - d * 24, sr - d * 12, sb - d * 14), p);
            c.drawOval(new RectF(sl + d * 14, sb - d * 18, sl + d * 28, sb - d * 10), p);
            }
            // kupu-kupu terbang pelan (saat kosong: hanya di sisi atas, di luar kotak teks)
            boolean empty = si.length == 0;
            for (int k = 0; k < 2; k++) {
                float bx;
                float by2;
                if (empty) {
                    bx = w * (0.24f + 0.52f * k) + (float) Math.sin(tm * 0.5f + k * 2.4f) * d * 20;
                    by2 = st + (sb - st) * 0.16f + (float) Math.cos(tm * 0.9f + k) * d * 8;
                } else {
                    bx = w * (0.3f + 0.4f * k) + (float) Math.sin(tm * 0.5f + k * 2.4f) * d * 40;
                    by2 = st + (sb - st) * (0.25f + 0.25f * k) + (float) Math.cos(tm * 0.9f + k) * d * 12;
                }
                float flap = (float) (0.35 + 0.65 * Math.abs(Math.sin(tm * 5 + k)));
                p.setColor(k == 0 ? Color.parseColor("#FFB74D") : Color.parseColor("#F48FB1"));
                rf.set(bx - d * 7 * flap, by2 - d * 5, bx, by2 + d * 2);
                c.drawOval(rf, p);
                rf.set(bx, by2 - d * 5, bx + d * 7 * flap, by2 + d * 2);
                c.drawOval(rf, p);
                p.setColor(Color.parseColor("#5A3416"));
                c.drawRect(bx - d * 0.8f, by2 - d * 4, bx + d * 0.8f, by2 + d * 3, p);
            }

            // tanaman
            int n = si.length;
            float ix = sl + d * 6;
            float iy = st + d * 18;
            float iw = (sr - sl) - d * 12;
            float ih = (sb - st) - d * 24;
            if (n == 0) {
                float sh = sb - st;
                float gcx = w / 2f;
                float gcy = st + sh * 0.34f + (float) Math.sin(tm) * d * 4f;
                android.graphics.Bitmap tnB = kBmp("ic_tunas");
                if (tnB != null) {
                    float gs = d * 56;
                    float gw = gs * tnB.getWidth() / tnB.getHeight();
                    kRf.set(gcx - gw / 2f, gcy - gs / 2f, gcx + gw / 2f, gcy + gs / 2f);
                    c.drawBitmap(tnB, null, kRf, ghostP);
                } else {
                    c.saveLayerAlpha(gcx - d * 50, gcy - d * 40, gcx + d * 50, gcy + d * 40, 130);
                    drawPlant(c, p, gcx, gcy + d * 30, d * 62, 1, 0f, 0f, false);
                    c.restore();
                }
                p.setStyle(Paint.Style.FILL);
                p.setTypeface(ROUND_BOLD);
                p.setTextAlign(Paint.Align.CENTER);
                p.setTextSize(d * 14);
                float tw = Math.max(p.measureText("Pilih benih ayat"), p.measureText("untuk ditanam"));
                float pw = Math.min(tw + d * 24, (sr - sl) * 0.6f);
                float ph = d * 48;
                float ptop = st + sh * 0.66f - ph / 2f;
                p.setColor(Color.argb(130, 78, 52, 46));
                rf.set(gcx - pw / 2f, ptop, gcx + pw / 2f, ptop + ph);
                c.drawRoundRect(rf, d * 14, d * 14, p);
                p.setColor(Color.parseColor("#FFF8EC"));
                p.setShadowLayer(d * 2, 0f, d, Color.argb(170, 0, 0, 0));
                c.drawText("Pilih benih ayat", gcx, ptop + d * 20, p);
                c.drawText("untuk ditanam", gcx, ptop + d * 38, p);
                p.clearShadowLayer();
                hr = 0f;
                pet(c, sl, sr, sb);
                return;
            }
            int cols = (int) Math.max(1, Math.min(n, Math.ceil(Math.sqrt(n * iw / ih))));
            int rows = (int) Math.ceil(n / (double) cols);
            float cap = n <= 3 ? d * 150 : (n <= 8 ? d * 110 : d * 84);
            float cell = Math.min(Math.min(iw / cols, ih / rows), cap);
            float gx0 = ix + (iw - cell * cols) / 2f;
            float gy0 = iy + (ih - cell * rows) / 2f;
            hr = cell / 2f;
            for (int i = 0; i < n; i++) {
                int col = i % cols;
                int row = i / cols;
                float cx = gx0 + cell * (col + 0.5f);
                float by = gy0 + cell * (row + 0.8f);
                float s = cell * 0.95f;
                hx[i] = cx;
                hy[i] = gy0 + cell * (row + 0.5f);
                float pr = Math.max(0f, Math.min(1f, grow * 1.5f - i * 0.04f));
                float g = ov.getInterpolation(pr);
                boolean isSel = si[i] == sel;
                if (isSel) {
                    p.setStyle(Paint.Style.FILL);
                    p.setColor(Color.argb((int) (70 + 40 * Math.sin(tm * 2)), 255, 248, 214));
                    rf.set(cx - s * 0.42f, by - s * 0.09f, cx + s * 0.42f, by + s * 0.09f);
                    c.drawOval(rf, p);
                }
                c.save();
                c.scale(g, g, cx, by);
                drawPlant(c, p, cx, by, s, stg[i], (float) Math.sin(tm + i * 0.9) * 2.4f, tm * 1.5f, true);
                c.restore();
                if (haus[i]) {
                    p.setStyle(Paint.Style.FILL);
                    p.setColor(Color.parseColor("#4FC3F7"));
                    float dx = cx + s * 0.3f;
                    float dy = by - s * 0.78f;
                    c.drawCircle(dx, dy + d * 3, d * 4.5f, p);
                    path.reset();
                    path.moveTo(dx, dy - d * 6);
                    path.lineTo(dx - d * 4, dy + d * 1.5f);
                    path.lineTo(dx + d * 4, dy + d * 1.5f);
                    path.close();
                    c.drawPath(path, p);
                }
                if (cell >= d * 44) {
                    p.setStyle(Paint.Style.FILL);
                    p.setTextAlign(Paint.Align.CENTER);
                    p.setTextSize(Math.min(d * 10.5f, cell * 0.17f));
                    p.setColor(isSel ? Color.parseColor("#FFF3D6") : Color.argb(220, 255, 243, 214));
                    String nm = SambungAyat.NAMA[si[i]];
                    c.drawText(nm, cx, gy0 + cell * (row + 0.97f), p);
                }
            }
            p.setStyle(Paint.Style.FILL);
            pet(c, sl, sr, sb);
        }

        /** Kelinci penjaga kebun: melompat riang; lesu dan minta disiram bila ada tanaman haus. */
        private void pet(Canvas c, float sl, float sr, float sb) {
            float sz = d * 40;
            long now = android.os.SystemClock.uptimeMillis();
            boolean sleepy = hausN > 0;
            boolean emptyG = si.length == 0;
            float cx0 = emptyG ? sr - d * 34 : (sl + sr) / 2f;
            float x;
            float lift = 0f;
            int dir;
            if (sleepy) {
                x = sr - d * 52;
                dir = -1;
            } else {
                // lompat satu kali (0,5 dtk), lalu diam sebentar; tiap siklus 1,8 dtk
                float t = now / 1000f;
                float per = 1.8f;
                int kk = (int) (t / per);
                float f = t / per - kk;
                float amp = emptyG ? d * 10 : (sr - sl) * 0.3f;
                float x0 = cx0 + amp * (float) Math.sin(kk * 0.45);
                float x1 = cx0 + amp * (float) Math.sin((kk + 1) * 0.45);
                float u = Math.min(1f, f / 0.3f);
                x = x0 + (x1 - x0) * (u * u * (3f - 2f * u));
                dir = x1 >= x0 ? 1 : -1;
                lift = (float) Math.sin(u * Math.PI) * d * 9;
            }
            float tp = Math.max(0f, 1f - (now - tapMs) / 650f);
            if (tp > 0f) {
                lift += (float) Math.sin(tp * Math.PI) * d * 24;
            }
            float by = sb - d * 12;
            rx = x;
            ry = by - sz * 0.5f - lift;
            p.setStyle(Paint.Style.FILL);
            p.setColor(Color.argb(55, 40, 20, 0));
            c.drawOval(new RectF(x - sz * 0.5f, by - sz * 0.06f, x + sz * 0.5f, by + sz * 0.07f), p);
            float breathe = sleepy ? (float) Math.sin(tm * 2) * sz * 0.015f : 0f;
            c.save();
            c.translate(0f, -lift + breathe);
            c.scale(dir, 1f, x, by);
            int fur = Color.parseColor("#FFF6EC");
            p.setColor(fur);
            c.drawCircle(x - sz * 0.46f, by - sz * 0.3f, sz * 0.14f, p);
            c.drawOval(new RectF(x - sz * 0.5f, by - sz * 0.62f, x + sz * 0.42f, by - sz * 0.04f), p);
            p.setColor(Color.parseColor("#F0E0CC"));
            c.drawOval(new RectF(x - sz * 0.4f, by - sz * 0.22f, x + sz * 0.34f, by - sz * 0.04f), p);
            p.setColor(fur);
            c.drawOval(new RectF(x + sz * 0.14f, by - sz * 0.12f, x + sz * 0.38f, by - sz * 0.02f), p);
            float hx0 = x + sz * 0.32f;
            float hy0 = by - sz * 0.64f;
            for (int e2 = 0; e2 < 2; e2++) {
                float ang = sleepy ? 58f : (e2 == 0 ? -10f : 8f);
                c.save();
                c.translate(hx0 - sz * 0.08f + e2 * sz * 0.18f, hy0 - sz * 0.2f);
                c.rotate(ang);
                p.setColor(fur);
                c.drawOval(new RectF(-sz * 0.085f, -sz * 0.36f, sz * 0.085f, sz * 0.02f), p);
                p.setColor(Color.parseColor("#F8B6C8"));
                c.drawOval(new RectF(-sz * 0.045f, -sz * 0.31f, sz * 0.045f, -sz * 0.03f), p);
                c.restore();
            }
            p.setColor(fur);
            c.drawCircle(hx0, hy0, sz * 0.3f, p);
            p.setColor(Color.argb(110, 248, 150, 170));
            c.drawCircle(hx0 + sz * 0.04f, hy0 + sz * 0.1f, sz * 0.07f, p);
            p.setColor(Color.parseColor("#3B2A20"));
            if (sleepy) {
                p.setStyle(Paint.Style.STROKE);
                p.setStrokeCap(Paint.Cap.ROUND);
                p.setStrokeWidth(sz * 0.035f);
                c.drawLine(hx0 + sz * 0.06f, hy0 - sz * 0.02f, hx0 + sz * 0.17f, hy0 - sz * 0.02f, p);
                p.setStyle(Paint.Style.FILL);
            } else {
                c.drawCircle(hx0 + sz * 0.12f, hy0 - sz * 0.03f, sz * 0.045f, p);
                p.setColor(Color.WHITE);
                c.drawCircle(hx0 + sz * 0.13f, hy0 - sz * 0.045f, sz * 0.014f, p);
            }
            p.setColor(Color.parseColor("#F48FB1"));
            c.drawCircle(hx0 + sz * 0.28f, hy0 + sz * 0.05f, sz * 0.04f, p);
            c.restore();
            if (sleepy) {
                float al = (float) (0.5 + 0.5 * Math.sin(tm * 3));
                p.setTextAlign(Paint.Align.CENTER);
                p.setTypeface(ROUND_BOLD);
                p.setColor(Color.argb((int) (120 + 135 * al), 255, 255, 255));
                p.setTextSize(d * 13);
                c.drawText("z", x - dir * sz * 0.2f, by - sz * 1.2f, p);
                p.setTextSize(d * 10);
                c.drawText("z", x - dir * sz * 0.02f, by - sz * 1.0f, p);
                p.setColor(Color.parseColor("#4FC3F7"));
                float dx = x - dir * sz * 0.56f;
                float dy = by - sz * 0.95f;
                c.drawCircle(dx, dy + d * 3, d * 4.5f, p);
                path.reset();
                path.moveTo(dx, dy - d * 6);
                path.lineTo(dx - d * 4, dy + d * 1.5f);
                path.lineTo(dx + d * 4, dy + d * 1.5f);
                path.close();
                c.drawPath(path, p);
            }
        }

        @Override
        public boolean onTouchEvent(MotionEvent e) {
            int a = e.getActionMasked();
            boolean nearPet = rx >= 0f && Math.abs(e.getX() - rx) < d * 30 && Math.abs(e.getY() - ry) < d * 34;
            if (a == MotionEvent.ACTION_DOWN) {
                return hr > 0f || nearPet;
            }
            if (a == MotionEvent.ACTION_UP && nearPet) {
                tapMs = android.os.SystemClock.uptimeMillis();
                return true;
            }
            if (a == MotionEvent.ACTION_UP && hr > 0f) {
                int best = -1;
                float bd = hr * hr;
                for (int i = 0; i < si.length; i++) {
                    float dx = e.getX() - hx[i];
                    float dy = e.getY() - hy[i];
                    float dd = dx * dx + dy * dy;
                    if (dd <= bd) {
                        bd = dd;
                        best = i;
                    }
                }
                if (best >= 0 && cb != null) {
                    cb.pick(si[best]);
                }
                return true;
            }
            return true;
        }
    }

    // ---------- Mushaf Hafalan: baca, tutup kata, mikrofon ----------
    private int mushafSi = -1;
    private int mushafMode = 0;
    private int mushafAwal = 1;
    private boolean mushafOn;
    private String mushafPeek;
    private int mushafTouch = -1;
    private boolean mushafLong;
    private final java.util.ArrayList<int[]> mushafRanges = new java.util.ArrayList<>();
    private final Runnable mushafLongRun = () -> {
        if (mushafTouch >= 0 && mushafTouch < mushafRanges.size()) {
            int[] rg = mushafRanges.get(mushafTouch);
            mushafLong = true;
            mushafPeek = rg[2] + ":" + rg[3];
            mushafRender();
        }
    };
    private final java.util.HashSet<String> mushafOpen = new java.util.HashSet<>();
    private final java.util.HashSet<String> mushafGood = new java.util.HashSet<>();
    private android.speech.SpeechRecognizer mushafSr;
    private boolean mushafListening;
    private TextView mushafTv;
    private TextView mushafStatus;
    private TextView mushafMicBtn;
    private static final int REQ_MIC = 71;

    private static String normAr(String s) {
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            if ((ch >= '\u064B' && ch <= '\u065F') || ch == '\u0670' || ch == '\u0640'
                    || (ch >= '\u06D6' && ch <= '\u06ED')) {
                continue;
            }
            if (ch == '\u0623' || ch == '\u0625' || ch == '\u0622' || ch == '\u0671') {
                ch = '\u0627';
            } else if (ch == '\u0649' || ch == '\u06CC') {
                ch = '\u064A';
            } else if (ch == '\u0629') {
                ch = '\u0647';
            } else if (ch == '\u06A9') {
                ch = '\u0643';
            }
            b.append(ch);
        }
        return b.toString();
    }

    private static boolean sameAr(String a, String b) {
        if (a.equals(b)) return true;
        if (a.length() < 4 || b.length() < 4 || Math.abs(a.length() - b.length()) > 1) return false;
        int[] prev = new int[b.length() + 1];
        for (int j = 0; j <= b.length(); j++) prev[j] = j;
        for (int i = 1; i <= a.length(); i++) {
            int[] cur = new int[b.length() + 1];
            cur[0] = i;
            for (int j = 1; j <= b.length(); j++) {
                int c = a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1;
                cur[j] = Math.min(Math.min(cur[j - 1] + 1, prev[j] + 1), prev[j - 1] + c);
            }
            prev = cur;
        }
        return prev[b.length()] <= 1;
    }

    private boolean mushafHidden(int si, int ai, int wi) {
        if (mushafMode == 0 || mushafOpen.contains(ai + ":" + wi)) return false;
        if (mushafMode == 1 && wi < mushafAwal) return false;
        return true;
    }

    private int mushafSisa() {
        int sisa = 0;
        for (int ai = 0; ai < SambungAyat.jumlahAyat(mushafSi); ai++) {
            String[] w = SambungAyat.ayat(mushafSi, ai).trim().split("\\s+");
            for (int wi = 0; wi < w.length; wi++) {
                if (mushafHidden(mushafSi, ai, wi)) sisa++;
            }
        }
        return sisa;
    }

    private void mushafRender() {
        if (mushafTv == null) return;
        mushafRanges.clear();
        android.text.SpannableStringBuilder sb = new android.text.SpannableStringBuilder();
        for (int ai = 0; ai < SambungAyat.jumlahAyat(mushafSi); ai++) {
            String[] w = SambungAyat.ayat(mushafSi, ai).trim().split("\\s+");
            for (int wi = 0; wi < w.length; wi++) {
                String key = ai + ":" + wi;
                int st = sb.length();
                if (mushafHidden(mushafSi, ai, wi)) {
                    if (key.equals(mushafPeek)) {
                        sb.append(w[wi]);
                        sb.setSpan(new android.text.style.ForegroundColorSpan(Color.parseColor("#E08A00")),
                                st, sb.length(), android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    } else {
                        int n = Math.max(2, Math.min(7, normAr(w[wi]).length()));
                        StringBuilder ph = new StringBuilder();
                        for (int k = 0; k < n; k++) ph.append('_');
                        sb.append(ph);
                        sb.setSpan(new android.text.style.ForegroundColorSpan(Color.parseColor("#8FA08F")),
                                st, sb.length(), android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                    mushafRanges.add(new int[]{st, sb.length(), ai, wi});
                } else {
                    sb.append(w[wi]);
                    if (mushafGood.contains(key)) {
                        sb.setSpan(new android.text.style.ForegroundColorSpan(Color.parseColor("#2E8B3D")),
                                st, sb.length(), android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                }
                sb.append(' ');
            }
            StringBuilder num = new StringBuilder();
            String ns = String.valueOf(ai + 1);
            for (int k = 0; k < ns.length(); k++) num.append((char) ('\u0660' + (ns.charAt(k) - '0')));
            int ms = sb.length();
            sb.append("\uFD3F").append(num).append("\uFD3E ");
            sb.setSpan(new android.text.style.ForegroundColorSpan(Color.parseColor("#3F5FB5")),
                    ms, sb.length(), android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        mushafTv.setText(sb);
    }

    private void mushafSetStatus(String s) {
        if (mushafStatus != null) mushafStatus.setText(s);
    }

    private void stopMic() {
        mushafListening = false;
        if (mushafSr != null) {
            try {
                mushafSr.cancel();
            } catch (Exception e) {
                // abaikan
            }
            try {
                mushafSr.destroy();
            } catch (Exception e) {
                // abaikan
            }
            mushafSr = null;
        }
        if (mushafMicBtn != null) mushafMicBtn.setText("🎙 Baca");
    }

    private void mushafMic() {
        if (mushafListening) {
            stopMic();
            mushafSetStatus("Mikrofon dihentikan");
            return;
        }
        if (android.os.Build.VERSION.SDK_INT >= 23 && checkSelfPermission(android.Manifest.permission.RECORD_AUDIO)
                != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{android.Manifest.permission.RECORD_AUDIO}, REQ_MIC);
            return;
        }
        mushafIdle = 0;
        startMic();
    }

    @Override
    public void onRequestPermissionsResult(int rc, String[] perms, int[] res) {
        super.onRequestPermissionsResult(rc, perms, res);
        if (rc == REQ_MIC) {
            if (res.length > 0 && res[0] == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                mushafIdle = 0;
                startMic();
            } else {
                mushafSetStatus("Izin mikrofon ditolak");
            }
        }
    }

    private int mushafIdle = 0;

    /** Lanjut mendengarkan otomatis selama masih ada kata tersembunyi; berhenti bila selesai atau lama hening. */
    private void mushafNext(boolean got) {
        if (!got) {
            mushafIdle++;
        }
        if (!mushafListening || mushafSisa() == 0 || mushafIdle >= 3) {
            boolean idle = mushafListening && mushafSisa() > 0 && mushafIdle >= 3;
            handler.post(() -> stopMic());
            if (idle) {
                mushafSetStatus("Suara belum terdengar. Tekan 🎙 Baca untuk lanjut.");
            }
            return;
        }
        handler.postDelayed(() -> {
            if (mushafListening) {
                startMic();
            }
        }, 250);
    }

    private void startMic() {
        stopSurahAudio();
        if (mushafSr != null) {
            try {
                mushafSr.cancel();
            } catch (Exception e) {
                // abaikan
            }
            try {
                mushafSr.destroy();
            } catch (Exception e) {
                // abaikan
            }
            mushafSr = null;
        }
        try {
            mushafSr = android.speech.SpeechRecognizer.createSpeechRecognizer(this);
            mushafSr.setRecognitionListener(new android.speech.RecognitionListener() {
                @Override public void onReadyForSpeech(Bundle p) { mushafSetStatus("🎙 Silakan baca ayatnya..."); }
                @Override public void onBeginningOfSpeech() { }
                @Override public void onRmsChanged(float v) { }
                @Override public void onBufferReceived(byte[] b) { }
                @Override public void onEndOfSpeech() { mushafSetStatus("Memeriksa bacaan..."); }
                @Override
                public void onPartialResults(Bundle pb) {
                    if (!mushafListening) return;
                    java.util.ArrayList<String> l = pb.getStringArrayList(android.speech.SpeechRecognizer.RESULTS_RECOGNITION);
                    if (l != null && !l.isEmpty()) {
                        mushafCocok(l.get(0), true);
                    }
                }

                @Override public void onEvent(int t, Bundle p) { }

                @Override
                public void onResults(Bundle b) {
                    java.util.ArrayList<String> l = b.getStringArrayList(android.speech.SpeechRecognizer.RESULTS_RECOGNITION);
                    boolean got = l != null && !l.isEmpty();
                    if (got) {
                        mushafIdle = 0;
                        mushafCocok(l.get(0), false);
                    }
                    mushafNext(got);
                }

                @Override
                public void onError(int e) {
                    boolean silent = e == android.speech.SpeechRecognizer.ERROR_NO_MATCH
                            || e == android.speech.SpeechRecognizer.ERROR_SPEECH_TIMEOUT;
                    if (silent && mushafListening) {
                        mushafNext(false);
                        return;
                    }
                    handler.post(() -> stopMic());
                    mushafSetStatus("Pengenalan suara gagal (kode " + e + "). Pastikan layanan suara Google aktif.");
                }
            });
            Intent in = new Intent(android.speech.RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            in.putExtra(android.speech.RecognizerIntent.EXTRA_LANGUAGE_MODEL, android.speech.RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            in.putExtra(android.speech.RecognizerIntent.EXTRA_LANGUAGE, "ar-SA");
            in.putExtra(android.speech.RecognizerIntent.EXTRA_MAX_RESULTS, 3);
            in.putExtra(android.speech.RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
            mushafSr.startListening(in);
            mushafListening = true;
            if (mushafMicBtn != null) mushafMicBtn.setText("⏹ Berhenti");
        } catch (Exception e) {
            stopMic();
            mushafSetStatus("Mikrofon tidak bisa dipakai di HP ini");
        }
    }

    private void mushafCocok(String said, final boolean partial) {
        java.util.ArrayList<int[]> pos = new java.util.ArrayList<>();
        java.util.ArrayList<String> nrm = new java.util.ArrayList<>();
        for (int ai = 0; ai < SambungAyat.jumlahAyat(mushafSi); ai++) {
            String[] w = SambungAyat.ayat(mushafSi, ai).trim().split("\\s+");
            for (int wi = 0; wi < w.length; wi++) {
                pos.add(new int[]{ai, wi});
                nrm.add(normAr(w[wi]));
            }
        }
        String[] sp = normAr(said).trim().split("\\s+");
        int p = -1;
        int hit = 0;
        int n = nrm.size();
        for (String w : sp) {
            if (w.isEmpty()) continue;
            int found = -1;
            if (p < 0) {
                for (int k = 0; k < n && found < 0; k++) {
                    if (sameAr(nrm.get(k), w) && mushafHidden(mushafSi, pos.get(k)[0], pos.get(k)[1])) found = k;
                }
                for (int k = 0; k < n && found < 0; k++) {
                    if (sameAr(nrm.get(k), w)) found = k;
                }
            } else {
                for (int k = p; k < Math.min(n, p + 4); k++) {
                    if (sameAr(nrm.get(k), w)) {
                        found = k;
                        break;
                    }
                }
            }
            if (found >= 0) {
                int ai = pos.get(found)[0];
                int wi = pos.get(found)[1];
                if (mushafHidden(mushafSi, ai, wi)) {
                    mushafOpen.add(ai + ":" + wi);
                    mushafGood.add(ai + ":" + wi);
                    hit++;
                }
                p = found + 1;
            }
        }
        if (!partial || hit > 0) {
            mushafRender();
        }
        if (hit > 0 && mushafSisa() == 0) {
            sfx(SFX_CORRECT);
            mushafSetStatus("🎉 MasyaAllah! Semua kata sudah terbuka.");
            if (partial) {
                handler.post(() -> stopMic());
            }
        } else if (hit > 0) {
            if (!partial) {
                sfx(SFX_CORRECT);
            }
            mushafSetStatus("✅ " + hit + " kata benar! Sisa " + mushafSisa() + " kata.");
        } else if (!partial) {
            mushafSetStatus("Belum cocok, coba baca lebih pelan dan jelas.");
        }
    }

    private TextView mushafChip(String label, boolean on) {
        TextView t = text(label, 12, on ? Color.WHITE : Color.parseColor("#285A35"), true);
        t.setBackground(rr(on ? Color.parseColor("#68B84B") : Color.parseColor("#FFF8E6"), Color.parseColor("#EADFC0"), 1, 16));
        t.setPadding(dp(12), dp(8), dp(12), dp(8));
        t.setSingleLine(true);
        pressable(t);
        return t;
    }

    private static final String[] MUSHAF_AR = {"النبأ", "النازعات", "عبس", "التكوير", "الانفطار", "المطففين",
            "الانشقاق", "البروج", "الطارق", "الأعلى", "الغاشية", "الفجر", "البلد", "الشمس", "الليل", "الضحى",
            "الشرح", "التين", "العلق", "القدر", "البينة", "الزلزلة", "العاديات", "القارعة", "التكاثر", "العصر",
            "الهمزة", "الفيل", "قريش", "الماعون", "الكوثر", "الكافرون", "النصر", "المسد", "الإخلاص", "الفلق", "الناس"};

    private TextView mushafCircle(String ic) {
        TextView t = text(ic, 16, Color.parseColor("#285A35"), false);
        t.setBackground(rr(Color.WHITE, Color.parseColor("#EADFC0"), 1, 22));
        t.setElevation(dp(2));
        pressable(t);
        return t;
    }

    private void mushafGo(int si) {
        if (si < 0 || si >= SambungAyat.JUMLAH_SURAH) return;
        mushafSi = si;
        mushafOpen.clear();
        mushafGood.clear();
        mushafPeek = null;
        showMushaf();
    }

    private void mushafSheet() {
        final int cDark = Color.parseColor("#285A35");
        final android.app.Dialog dlg = new android.app.Dialog(this);
        dlg.requestWindowFeature(android.view.Window.FEATURE_NO_TITLE);
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(18), dp(16), dp(18), dp(20));
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.WHITE);
        float r = dp(24);
        bg.setCornerRadii(new float[]{r, r, r, r, 0, 0, 0, 0});
        box.setBackground(bg);

        TextView t1 = text("🎙 Murajaah", 18, cDark, true);
        t1.setGravity(Gravity.START);
        box.addView(t1, new LinearLayout.LayoutParams(MATCH, WRAP));
        TextView t2 = text("Teman murajaah untuk latihan mandiri. Ikuti bacaan kata demi kata dan kenali kata yang terlewat. Fitur ini belum menilai tajwid dan tidak menggantikan peran guru ngaji.", 12, Color.parseColor("#6B6B6B"), false);
        t2.setGravity(Gravity.START);
        LinearLayout.LayoutParams p2 = new LinearLayout.LayoutParams(MATCH, WRAP);
        p2.topMargin = dp(8);
        box.addView(t2, p2);
        TextView t3 = text("Sembunyikan kata", 14, Color.parseColor("#222222"), true);
        t3.setGravity(Gravity.START);
        LinearLayout.LayoutParams p3 = new LinearLayout.LayoutParams(MATCH, WRAP);
        p3.topMargin = dp(14);
        box.addView(t3, p3);

        final String[] ol = {"👁  Tampilkan Semua", "➖  Sisakan Kata Awal", "🙈  Sembunyikan Penuh"};
        final TextView[] opt = new TextView[3];
        final TextView[] ab = new TextView[3];
        final LinearLayout awalRow = new LinearLayout(this);
        final Runnable[] upd = new Runnable[1];
        for (int i = 0; i < 3; i++) {
            final int fi = i;
            opt[i] = text(ol[i], 14, Color.parseColor("#333333"), true);
            opt[i].setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
            opt[i].setPadding(dp(14), dp(13), dp(14), dp(13));
            opt[i].setOnClickListener(v -> {
                mushafMode = fi;
                upd[0].run();
            });
            LinearLayout.LayoutParams lp0 = new LinearLayout.LayoutParams(MATCH, WRAP);
            lp0.topMargin = dp(6);
            box.addView(opt[i], lp0);
        }
        awalRow.setOrientation(LinearLayout.HORIZONTAL);
        awalRow.setGravity(Gravity.CENTER_VERTICAL);
        awalRow.setPadding(dp(14), dp(8), dp(10), dp(8));
        awalRow.setBackground(rr(Color.parseColor("#E8F0EA"), 0, 0, 16));
        TextView al = text("Kata awal", 14, cDark, true);
        al.setGravity(Gravity.START);
        awalRow.addView(al, new LinearLayout.LayoutParams(0, WRAP, 1f));
        for (int j = 0; j < 3; j++) {
            final int fj = j;
            ab[j] = text(String.valueOf(j + 1), 15, cDark, true);
            ab[j].setPadding(dp(18), dp(8), dp(18), dp(8));
            ab[j].setOnClickListener(v -> {
                mushafAwal = fj + 1;
                upd[0].run();
            });
            LinearLayout.LayoutParams ap = new LinearLayout.LayoutParams(WRAP, WRAP);
            ap.leftMargin = dp(4);
            awalRow.addView(ab[j], ap);
        }
        LinearLayout.LayoutParams arp = new LinearLayout.LayoutParams(MATCH, WRAP);
        arp.topMargin = dp(8);
        box.addView(awalRow, arp);
        upd[0] = () -> {
            for (int i = 0; i < 3; i++) {
                boolean on = mushafMode == i;
                opt[i].setText(ol[i] + (on ? "    ✓" : ""));
                opt[i].setTextColor(on ? cDark : Color.parseColor("#333333"));
                opt[i].setBackground(rr(on ? Color.parseColor("#E9F5D3") : Color.WHITE, Color.parseColor("#E0E0E0"), 1, 14));
            }
            awalRow.setVisibility(mushafMode == 1 ? View.VISIBLE : View.GONE);
            for (int j = 0; j < 3; j++) {
                boolean on = mushafAwal == j + 1;
                ab[j].setTextColor(on ? Color.WHITE : cDark);
                ab[j].setBackground(rr(on ? cDark : Color.WHITE, cDark, 1, 12));
            }
        };
        upd[0].run();

        TextView hint = text("👆 Saat latihan, tahan kata tersembunyi untuk mengintip.", 12, cDark, false);
        hint.setGravity(Gravity.START);
        LinearLayout.LayoutParams hp = new LinearLayout.LayoutParams(MATCH, WRAP);
        hp.topMargin = dp(12);
        box.addView(hint, hp);

        final boolean[] go = {false};
        TextView start = text("Mulai Murajaah", 16, Color.WHITE, true);
        start.setBackground(rr(cDark, 0, 0, 26));
        start.setPadding(dp(10), dp(14), dp(10), dp(14));
        pressable(start);
        start.setOnClickListener(v -> {
            go[0] = true;
            dlg.dismiss();
        });
        LinearLayout.LayoutParams sp = new LinearLayout.LayoutParams(MATCH, WRAP);
        sp.topMargin = dp(14);
        box.addView(start, sp);

        dlg.setOnDismissListener(d -> {
            mushafOpen.clear();
            mushafGood.clear();
            mushafPeek = null;
            showMushaf();
            if (go[0] && mushafMode != 0) mushafMic();
        });
        dlg.setContentView(box);
        android.view.Window w = dlg.getWindow();
        if (w != null) {
            w.setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(Color.TRANSPARENT));
            w.setLayout(MATCH, WRAP);
            w.setGravity(Gravity.BOTTOM);
        }
        dlg.show();
    }

    private void showMushaf() {
        handler.removeCallbacksAndMessages(null);
        plainUi = true;
        setScreen(8);
        mushafOn = true;
        nameFields = null;
        if (!SambungAyat.load(this)) {
            Toast.makeText(this, "Data ayat tidak bisa dibuka", Toast.LENGTH_LONG).show();
            showMenu();
            return;
        }
        final int ns = SambungAyat.JUMLAH_SURAH;
        if (mushafSi < 0 || mushafSi >= ns) mushafSi = ns - 1;
        final int cDark = Color.parseColor("#285A35");
        final int cGold = Color.parseColor("#D9B766");
        final int cCream = Color.parseColor("#FFF8E6");

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackground(new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{Color.parseColor("#FFFBEF"), Color.parseColor("#F7F3D6")}));
        root.setMinimumHeight(getResources().getDisplayMetrics().heightPixels);

        // HEADER
        LinearLayout head = new LinearLayout(this);
        head.setOrientation(LinearLayout.VERTICAL);
        head.setBackground(new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{Color.parseColor("#9ED8F5"), Color.parseColor("#D6EFD0"), Color.parseColor("#FFFBEF")}));
        head.setPadding(dp(12), dp(12), dp(12), dp(8));
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        TextView back = mushafCircle("←");
        back.setOnClickListener(v -> {
            kebunNoGrow = true;
            showSambung();
        });
        row.addView(back, new LinearLayout.LayoutParams(dp(44), dp(44)));
        LinearLayout tb = new LinearLayout(this);
        tb.setOrientation(LinearLayout.VERTICAL);
        tb.setPadding(dp(10), 0, dp(6), 0);
        TextView tt = text("📖 Mushaf Hafalan", 17, cDark, true);
        tt.setGravity(Gravity.START);
        tb.addView(tt, new LinearLayout.LayoutParams(WRAP, WRAP));
        TextView ts = text("Baca • Hafal • Amalkan", 11, Color.parseColor("#4F6B52"), false);
        ts.setGravity(Gravity.START);
        tb.addView(ts, new LinearLayout.LayoutParams(WRAP, WRAP));
        row.addView(tb, new LinearLayout.LayoutParams(0, WRAP, 1f));
        final String audioPath = surahAudioPath(mushafSi);
        final TextView dg = mushafCircle("🔊");
        dg.setTag(new String[]{"🔊", "⏹"});
        dg.setOnClickListener(v -> {
            if (audioPath != null) {
                toggleSurahAudio(audioPath, dg);
            } else {
                Toast.makeText(this, "Audio surah ini belum ada", Toast.LENGTH_SHORT).show();
            }
        });
        dg.setAlpha(audioPath != null ? 1f : 0.45f);
        LinearLayout.LayoutParams dgp = new LinearLayout.LayoutParams(dp(44), dp(44));
        dgp.rightMargin = dp(8);
        row.addView(dg, dgp);
        TextView mc = mushafCircle("🎙");
        mc.setOnClickListener(v -> mushafSheet());
        row.addView(mc, new LinearLayout.LayoutParams(dp(44), dp(44)));
        head.addView(row, new LinearLayout.LayoutParams(MATCH, WRAP));

        // pilih surah
        int nAyat = SambungAyat.jumlahAyat(mushafSi);
        android.text.SpannableStringBuilder pl = new android.text.SpannableStringBuilder(SambungAyat.NAMA[mushafSi] + " ▾\n");
        int ps = pl.length();
        pl.append("Juz " + SambungAyat.juz + " • " + nAyat + " ayat");
        pl.setSpan(new android.text.style.RelativeSizeSpan(0.62f), ps, pl.length(), android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        TextView pill = text("", 18, cDark, true);
        pill.setText(pl);
        pill.setBackground(rr(cCream, cGold, 2, 24));
        pill.setPadding(dp(26), dp(8), dp(26), dp(8));
        pressable(pill);
        pill.setOnClickListener(v -> {
            String[] names = new String[ns];
            for (int i = 0; i < ns; i++) names[i] = (i + 1) + ". " + SambungAyat.NAMA[i];
            new android.app.AlertDialog.Builder(this).setTitle("Pilih surah")
                    .setItems(names, (d, i) -> mushafGo(i)).show();
        });
        LinearLayout.LayoutParams pp = new LinearLayout.LayoutParams(WRAP, WRAP);
        pp.gravity = Gravity.CENTER_HORIZONTAL;
        pp.topMargin = dp(10);
        head.addView(pill, pp);
        root.addView(head, new LinearLayout.LayoutParams(MATCH, WRAP));

        LinearLayout body = new LinearLayout(this);
        body.setOrientation(LinearLayout.VERTICAL);
        body.setPadding(dp(14), dp(6), dp(14), dp(18));

        // spanduk surah
        String arName = (SambungAyat.juz == 30 && ns == MUSHAF_AR.length) ? "سُورَةُ " + MUSHAF_AR[mushafSi]
                : "Surah " + SambungAyat.NAMA[mushafSi];
        TextView ban = text(arName, 28, Color.parseColor("#1F3D2A"), true);
        ban.setBackground(rr(Color.parseColor("#DCEBC8"), cGold, 2, 26));
        ban.setPadding(dp(10), dp(8), dp(10), dp(10));
        body.addView(ban, new LinearLayout.LayoutParams(MATCH, WRAP));

        // status & tombol sesi
        mushafMicBtn = null;
        mushafStatus = text("", 12, Color.parseColor("#7A6A4F"), false);
        mushafStatus.setGravity(Gravity.START);
        if (mushafMode != 0) {
            LinearLayout ac = new LinearLayout(this);
            ac.setOrientation(LinearLayout.HORIZONTAL);
            ac.setGravity(Gravity.CENTER_VERTICAL);
            mushafMicBtn = mushafChip("🎙 Baca", false);
            mushafMicBtn.setBackground(rr(Color.parseColor("#FFE3E3"), Color.parseColor("#EADFC0"), 1, 16));
            mushafMicBtn.setOnClickListener(v -> mushafMic());
            LinearLayout.LayoutParams mp = new LinearLayout.LayoutParams(WRAP, WRAP);
            mp.rightMargin = dp(6);
            ac.addView(mushafMicBtn, mp);
            TextView ul = mushafChip("🔄 Ulangi", false);
            ul.setOnClickListener(v -> {
                mushafOpen.clear();
                mushafGood.clear();
                mushafRender();
                mushafSetStatus("Ketuk garis untuk membuka kata, tahan untuk mengintip.");
            });
            ac.addView(ul, new LinearLayout.LayoutParams(WRAP, WRAP));
            TextView md = text(mushafMode == 1 ? "Sisakan " + mushafAwal + " kata awal" : "Sembunyikan penuh", 11, Color.parseColor("#7A6A4F"), false);
            md.setGravity(Gravity.END);
            ac.addView(md, new LinearLayout.LayoutParams(0, WRAP, 1f));
            LinearLayout.LayoutParams acp = new LinearLayout.LayoutParams(MATCH, WRAP);
            acp.topMargin = dp(8);
            body.addView(ac, acp);
            mushafStatus.setText("Ketuk garis untuk membuka kata, tahan untuk mengintip, atau tekan 🎙 lalu baca.");
        }
        LinearLayout.LayoutParams stp = new LinearLayout.LayoutParams(MATCH, WRAP);
        stp.topMargin = dp(6);
        body.addView(mushafStatus, stp);

        // teks ayat
        final TextView tv = new TextView(this);
        tv.setTextSize(26 * ui);
        tv.setTextColor(Color.parseColor("#1B1B1B"));
        tv.setTextDirection(View.TEXT_DIRECTION_RTL);
        tv.setGravity(Gravity.RIGHT);
        tv.setLineSpacing(0, 1.7f);
        tv.setPadding(dp(14), dp(14), dp(14), dp(14));
        tv.setBackground(rr(cCream, cGold, 2, 22));
        tv.setOnTouchListener((v, ev) -> {
            int a = ev.getActionMasked();
            if (a == MotionEvent.ACTION_DOWN) {
                int off = tv.getOffsetForPosition(ev.getX(), ev.getY());
                mushafTouch = -1;
                for (int i = 0; i < mushafRanges.size(); i++) {
                    int[] rg = mushafRanges.get(i);
                    if (off >= rg[0] && off <= rg[1]) {
                        mushafTouch = i;
                        break;
                    }
                }
                if (mushafTouch < 0) return false;
                mushafLong = false;
                handler.postDelayed(mushafLongRun, android.view.ViewConfiguration.getLongPressTimeout());
                return true;
            }
            if (a == MotionEvent.ACTION_UP || a == MotionEvent.ACTION_CANCEL) {
                handler.removeCallbacks(mushafLongRun);
                if (mushafTouch >= 0 && mushafTouch < mushafRanges.size()) {
                    int[] rg = mushafRanges.get(mushafTouch);
                    if (a == MotionEvent.ACTION_UP && !mushafLong) {
                        mushafOpen.add(rg[2] + ":" + rg[3]);
                    }
                    mushafPeek = null;
                    mushafRender();
                }
                mushafTouch = -1;
                mushafLong = false;
                return true;
            }
            return mushafTouch >= 0;
        });
        mushafTv = tv;
        LinearLayout.LayoutParams tvp = new LinearLayout.LayoutParams(MATCH, WRAP);
        tvp.topMargin = dp(8);
        body.addView(tv, tvp);
        mushafRender();

        // navigasi surah
        LinearLayout nav = new LinearLayout(this);
        nav.setOrientation(LinearLayout.HORIZONTAL);
        nav.setGravity(Gravity.CENTER);
        TextView pv = mushafCircle("◀");
        pv.setOnClickListener(v -> mushafGo(mushafSi - 1));
        nav.addView(pv, new LinearLayout.LayoutParams(dp(52), dp(52)));
        TextView cnt = text("📖  " + (mushafSi + 1) + " / " + ns, 16, cDark, true);
        cnt.setBackground(rr(cCream, cGold, 2, 26));
        cnt.setPadding(dp(22), dp(12), dp(22), dp(12));
        LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(WRAP, WRAP);
        cp.leftMargin = dp(14);
        cp.rightMargin = dp(14);
        nav.addView(cnt, cp);
        TextView nx = mushafCircle("▶");
        nx.setOnClickListener(v -> mushafGo(mushafSi + 1));
        nav.addView(nx, new LinearLayout.LayoutParams(dp(52), dp(52)));
        pv.setAlpha(mushafSi > 0 ? 1f : 0.4f);
        nx.setAlpha(mushafSi < ns - 1 ? 1f : 0.4f);
        LinearLayout.LayoutParams navp = new LinearLayout.LayoutParams(MATCH, WRAP);
        navp.topMargin = dp(16);
        body.addView(nav, navp);
        TextView leaf = text("🌿 🌼 🌿", 18, cDark, false);
        body.addView(leaf, lp(WRAP, 6));

        root.addView(body, new LinearLayout.LayoutParams(MATCH, WRAP));
        wrap(root);
    }

    private android.media.MediaPlayer surahPlayer;

    private int surahNoOf(int si) {
        return SambungAyat.juz == 30 ? 78 + si : -1;
    }

    private String surahAudioPath(int si) {
        int no = surahNoOf(si);
        if (no < 0) return null;
        String path = "quran/au_" + (no < 10 ? "00" : (no < 100 ? "0" : "")) + no + ".m4a";
        try {
            getAssets().openFd(path).close();
            return path;
        } catch (Exception e) {
            return null;
        }
    }

    private void stopSurahAudio() {
        if (surahPlayer != null) {
            try {
                surahPlayer.stop();
            } catch (Exception e) {
                // abaikan
            }
            try {
                surahPlayer.release();
            } catch (Exception e) {
                // abaikan
            }
            surahPlayer = null;
        }
    }

    private void toggleSurahAudio(String path, final TextView btn) {
        final String[] lb = btn.getTag() instanceof String[] ? (String[]) btn.getTag()
                : new String[]{"Dengar surah", "⏹ Berhenti"};
        if (surahPlayer != null) {
            stopSurahAudio();
            btn.setText(lb[0]);
            return;
        }
        try {
            android.content.res.AssetFileDescriptor afd = getAssets().openFd(path);
            android.media.MediaPlayer mp = new android.media.MediaPlayer();
            mp.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            afd.close();
            mp.setOnCompletionListener(m -> {
                stopSurahAudio();
                btn.setText(lb[0]);
            });
            mp.prepare();
            mp.start();
            surahPlayer = mp;
            btn.setText(lb[1]);
        } catch (Exception e) {
            stopSurahAudio();
            Toast.makeText(this, "Audio tidak bisa diputar", Toast.LENGTH_SHORT).show();
        }
    }

    private void kebunPilih(int si) {
        kebunSelectedSi = si;
        kebunNoGrow = true;
        showSambung();
    }

    private void showSambung() {
        handler.removeCallbacksAndMessages(null);
        plainUi = true;
        setScreen(8);
        mushafOn = false;
        nameFields = null;
        if (!SambungAyat.load(this)) {
            Toast.makeText(this, "Data ayat tidak bisa dibuka", Toast.LENGTH_LONG).show();
            showMenu();
            return;
        }
        final int ns = SambungAyat.JUMLAH_SURAH;
        if (kebunSelectedSi < 0 || kebunSelectedSi >= ns) {
            kebunSelectedSi = ns - 1;
        }
        final boolean animate = !kebunNoGrow;
        kebunNoGrow = false;
        final int hari = SambungAyat.hariIni();

        int totAyat = 0;
        int doneAyat = 0;
        int[] tahapAll = new int[ns];
        boolean[] hausAll = new boolean[ns];
        int plantedN = 0;
        for (int s0 = 0; s0 < ns; s0++) {
            int n0 = SambungAyat.jumlahAyat(s0);
            int m0 = SambungAyat.jumlahDikuasai(prefs, s0);
            totAyat += n0;
            doneAyat += m0;
            tahapAll[s0] = SambungAyat.tahapTanaman(prefs, s0);
            hausAll[s0] = m0 < n0 && SambungAyat.pernahLatih(prefs, s0)
                    && hari - SambungAyat.hariTerakhir(prefs, s0) >= SambungAyat.HARI_HAUS;
            if (tahapAll[s0] >= 1) {
                plantedN++;
            }
        }
        int[] pSi = new int[plantedN];
        int[] pSt = new int[plantedN];
        boolean[] pHaus = new boolean[plantedN];
        int pi = 0;
        for (int s1 = ns - 1; s1 >= 0; s1--) {
            if (tahapAll[s1] >= 1) {
                pSi[pi] = s1;
                pSt[pi] = tahapAll[s1];
                pHaus[pi] = hausAll[s1];
                pi++;
            }
        }

        final int selSi = kebunSelectedSi;
        final int selN = SambungAyat.jumlahAyat(selSi);
        final int selM = SambungAyat.jumlahDikuasai(prefs, selSi);
        final int selTahap = tahapAll[selSi];
        final int selSegar = SambungAyat.kesegaran(prefs, selSi);
        final boolean selHaus = hausAll[selSi];

        final int cGreen = Color.parseColor("#68B84B");
        final int cDark = Color.parseColor("#285A35");
        final int cCream = Color.parseColor("#FFF8E6");
        final int cBrown = Color.parseColor("#5A3416");

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        GradientDrawable skyBg = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{Color.parseColor("#78CCF5"), Color.parseColor("#A9DCF3"),
                        Color.parseColor("#CFEA9A"), Color.parseColor("#F4F2C0")});
        root.setBackground(skyBg);

        // HEADER
        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setPadding(dp(12), dp(12), dp(12), dp(4));

        TextView back = text("←", 20, cDark, true);
        back.setBackground(rr(cCream, Color.parseColor("#EADFC0"), 1, 22));
        back.setElevation(dp(3));
        back.setOnClickListener(v -> showMenu());
        pressable(back);
        header.addView(back, new LinearLayout.LayoutParams(dp(44), dp(44)));

        LinearLayout titlePill = new LinearLayout(this);
        titlePill.setOrientation(LinearLayout.HORIZONTAL);
        titlePill.setGravity(Gravity.CENTER_VERTICAL);
        titlePill.setBackground(rr(cCream, Color.parseColor("#EADFC0"), 1, 24));
        titlePill.setElevation(dp(3));
        titlePill.setPadding(dp(10), dp(4), dp(14), dp(4));
        titlePill.addView(kIconView("ic_tunas", seedIcon(1)), new LinearLayout.LayoutParams(dp(32), dp(32)));
        LinearLayout tcol = new LinearLayout(this);
        tcol.setOrientation(LinearLayout.VERTICAL);
        tcol.setPadding(dp(8), 0, 0, 0);
        TextView tJudul = text("Kebun Hafalan", 17, cDark, true);
        tJudul.setGravity(Gravity.START);
        tcol.addView(tJudul, new LinearLayout.LayoutParams(WRAP, WRAP));
        TextView tSub = text("Tanam ayat, panen pahala", 10, Color.parseColor("#7A6A4F"), false);
        tSub.setGravity(Gravity.START);
        tcol.addView(tSub, new LinearLayout.LayoutParams(WRAP, WRAP));
        titlePill.addView(tcol, new LinearLayout.LayoutParams(0, WRAP, 1f));
        LinearLayout.LayoutParams tpl = new LinearLayout.LayoutParams(0, dp(44), 1f);
        tpl.leftMargin = dp(8);
        tpl.rightMargin = dp(8);
        titlePill.setMinimumWidth(dp(0));
        header.addView(titlePill, tpl);

        TextView juzBtn = text("Juz " + SambungAyat.juz + " ▾", 13, cDark, true);
        iconLeft(juzBtn, "ic_tunas", 18, "Juz " + SambungAyat.juz + " ▾");
        juzBtn.setBackground(rr(cCream, Color.parseColor("#EADFC0"), 1, 22));
        juzBtn.setElevation(dp(3));
        juzBtn.setPadding(dp(12), dp(10), dp(12), dp(10));
        juzBtn.setSingleLine(true);
        pressable(juzBtn);
        juzBtn.setOnClickListener(v -> {
            android.widget.PopupMenu pm = new android.widget.PopupMenu(this, v);
            for (int jj = 30; jj >= 28; jj--) {
                android.view.MenuItem mi = pm.getMenu().add(0, jj, 0, "Juz " + jj);
                mi.setEnabled(SambungAyat.ada(this, jj));
            }
            pm.setOnMenuItemClickListener(it -> {
                SambungAyat.pilihJuz(it.getItemId());
                kebunSelectedSi = -1;
                kebunScrollX = 0;
                showSambung();
                return true;
            });
            pm.show();
        });
        LinearLayout.LayoutParams jbp = new LinearLayout.LayoutParams(WRAP, dp(44));
        header.addView(juzBtn, jbp);
        header.setGravity(Gravity.CENTER_VERTICAL);
        back.setLayoutParams(new LinearLayout.LayoutParams(dp(44), dp(44)));
        root.addView(header, new LinearLayout.LayoutParams(MATCH, WRAP));

        // KEBUN
        GardenView garden = new GardenView(this, pSi, pSt, pHaus, selSi, doneAyat, totAyat, animate, this::kebunPilih);
        root.addView(garden, new LinearLayout.LayoutParams(MATCH, 0, 1f));
        garden.setMinimumHeight(dp(250));

        LinearLayout body = new LinearLayout(this);
        body.setOrientation(LinearLayout.VERTICAL);
        body.setPadding(dp(14), dp(4), dp(14), dp(14));

        // KARTU BENIH TERPILIH
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setGravity(Gravity.CENTER_VERTICAL);
        card.setBackground(rr(cCream, Color.parseColor("#EADFC0"), 1, 22));
        card.setElevation(dp(4));
        card.setPadding(dp(14), dp(14), dp(14), dp(14));

        LinearLayout left = new LinearLayout(this);
        left.setOrientation(LinearLayout.VERTICAL);
        TextView cName = text("Benih Surah " + SambungAyat.NAMA[selSi], 16, cDark, true);
        cName.setGravity(Gravity.START);
        left.addView(cName, new LinearLayout.LayoutParams(WRAP, WRAP));
        TextView cInfo = text("📖 QS. " + SambungAyat.NAMA[selSi] + " • " + selN + " Ayat", 12, Color.parseColor("#7A6A4F"), false);
        iconLeft(cInfo, "ic_buku", 14, "QS. " + SambungAyat.NAMA[selSi] + " \u2022 " + selN + " Ayat");
        cInfo.setGravity(Gravity.START);
        left.addView(cInfo, lp(WRAP, 2));
        String hint = selHaus ? "💧 Tanaman haus! Siram atau muraja'ah sekarang."
                : (selTahap == 3 ? "MasyaAllah! Pohon emas Mutqin."
                : (selTahap == 0 ? "Menanam ayat ini akan menambah kesegaran hafalanmu."
                : "Kesegaran " + selSegar + "% • " + selM + "/" + selN + " ayat mutqin"));
        TextView cHint = text(hint, 11, cDark, false);
        cHint.setGravity(Gravity.START);
        cHint.setMaxLines(2);
        cHint.setEllipsize(TextUtils.TruncateAt.END);
        cHint.setBackground(rr(Color.parseColor("#E9F5D3"), 0, 0, 12));
        cHint.setPadding(dp(8), dp(5), dp(8), dp(5));
        left.addView(cHint, lp(WRAP, 8));
        final String audioPath = surahAudioPath(selSi);
        if (audioPath != null) {
            final TextView dengar = text("🔊 Dengar surah", 12, Color.parseColor("#0B5E86"), true);
            iconLeft(dengar, "ic_speaker", 16, "Dengar surah");
            dengar.setBackground(rr(Color.parseColor("#DDF2FC"), Color.parseColor("#EADFC0"), 1, 16));
            dengar.setPadding(dp(10), dp(6), dp(10), dp(6));
            dengar.setGravity(Gravity.CENTER);
            dengar.setSingleLine(true);
            dengar.setOnClickListener(v -> toggleSurahAudio(audioPath, dengar));
            pressable(dengar);
            left.addView(dengar, new LinearLayout.LayoutParams(MATCH, WRAP, 0) {{ topMargin = dp(8); }});
        }
        LinearLayout.LayoutParams leftLp = new LinearLayout.LayoutParams(0, WRAP, 1f);
        leftLp.leftMargin = dp(12);
        leftLp.rightMargin = dp(12);
        card.addView(left, leftLp);

        card.addView(kIconView("ic_karung_benih", new SeedBagView(this)), new LinearLayout.LayoutParams(dp(84), dp(84)));

        LinearLayout tanam = new LinearLayout(this);
        tanam.setOrientation(LinearLayout.VERTICAL);
        tanam.setGravity(Gravity.CENTER);
        android.graphics.drawable.LayerDrawable ld = new android.graphics.drawable.LayerDrawable(new Drawable[]{
                rr(Color.parseColor("#3F8A3A"), 0, 0, 20),
                rr(cGreen, 0, 0, 20)});
        ld.setLayerInset(1, 0, 0, 0, dp(4));
        tanam.setBackground(ld);
        tanam.setPadding(dp(6), dp(8), dp(6), dp(12));
        View tanamIcon = kIconView("ic_tunas", seedIcon(1));
        tanam.addView(tanamIcon, new LinearLayout.LayoutParams(dp(26), dp(26)));
        TextView tanamTxt = text(selTahap == 0 ? "Tanam Benih" : "Rawat Tanaman", 15, Color.WHITE, true);
        tanamTxt.setGravity(Gravity.CENTER);
        tanamTxt.setSingleLine(true);
        tanamTxt.setAutoSizeTextTypeUniformWithConfiguration(9, 15, 1, android.util.TypedValue.COMPLEX_UNIT_SP);
        LinearLayout.LayoutParams tanamTxtLp = new LinearLayout.LayoutParams(MATCH, WRAP);
        tanamTxtLp.topMargin = dp(4);
        tanam.addView(tanamTxt, tanamTxtLp);
        tanam.setOnClickListener(v -> startSambung(selSi));
        pressable(tanam);
        LinearLayout.LayoutParams tanamLp = new LinearLayout.LayoutParams(dp(104), dp(64));
        tanamLp.gravity = Gravity.CENTER_VERTICAL;
        card.addView(tanam, tanamLp);
        body.addView(card, new LinearLayout.LayoutParams(MATCH, WRAP));

        // AKSI CEPAT
        LinearLayout chips = new LinearLayout(this);
        chips.setOrientation(LinearLayout.HORIZONTAL);
        chips.setGravity(Gravity.CENTER_VERTICAL);
        TextView siram = text("💧 Siram • " + SambungAyat.embun(prefs), 12, Color.parseColor("#0B5E86"), true);
        TextView acak = text("🎲 Acak", 12, cDark, true);
        TextView urutBtn = text("🔢 Urut Ayat", 12, cDark, true);
        TextView mushafBtn = text("📖 Mushaf", 12, cDark, true);
        iconLeft(mushafBtn, "ic_buku", 18, "Mushaf");
        mushafBtn.setPadding(0, 0, 0, 0);
        TextView[] cv = {siram, acak, urutBtn, mushafBtn};
        for (int i = 0; i < cv.length; i++) {
            cv[i].setBackground(rr(i == 0 ? Color.parseColor("#DDF2FC") : cCream, Color.parseColor("#EADFC0"), 1, 18));
            cv[i].setPadding(dp(2), 0, dp(2), 0);
            cv[i].setGravity(Gravity.CENTER);
            cv[i].setSingleLine(true);
            cv[i].setAutoSizeTextTypeUniformWithConfiguration(8, 12, 1, android.util.TypedValue.COMPLEX_UNIT_SP);
            pressable(cv[i]);
            LinearLayout.LayoutParams clp = new LinearLayout.LayoutParams(0, dp(44), 1f);
            clp.leftMargin = i == 0 ? 0 : dp(4);
            clp.rightMargin = i == cv.length - 1 ? 0 : dp(4);
            chips.addView(cv[i], clp);
        }
        siram.setOnClickListener(v -> {
            if (SambungAyat.siramSurah(prefs, selSi)) {
                sfx(SFX_CORRECT);
                Toast.makeText(this, "Surah " + SambungAyat.NAMA[selSi] + " disiram! Kesegaran naik 🌱", Toast.LENGTH_SHORT).show();
                showSambung();
            } else {
                Toast.makeText(this, "Embun habis! Mainkan Sambung Ayat untuk dapat Embun baru 💧", Toast.LENGTH_SHORT).show();
            }
        });
        acak.setOnClickListener(v -> startSambung(-1));
        urutBtn.setOnClickListener(v -> startUrut(selSi));
        mushafBtn.setOnClickListener(v -> {
            mushafSi = selSi;
            mushafOpen.clear();
            mushafGood.clear();
            mushafPeek = null;
            showMushaf();
        });
        body.addView(chips, lp(WRAP, 12));

        // PILIHAN BENIH LAINNYA
        LinearLayout sh = new LinearLayout(this);
        sh.setOrientation(LinearLayout.HORIZONTAL);
        sh.setGravity(Gravity.CENTER_VERTICAL);
        sh.addView(kIconView("ic_tunas", seedIcon(1)), new LinearLayout.LayoutParams(dp(26), dp(26)));
        TextView shT = text("Pilihan Benih Lainnya", 14, cDark, true);
        shT.setPadding(dp(6), 0, dp(8), 0);
        sh.addView(shT, new LinearLayout.LayoutParams(WRAP, WRAP));
        View shL = new View(this);
        shL.setBackgroundColor(Color.argb(90, 104, 184, 75));
        sh.addView(shL, new LinearLayout.LayoutParams(0, dp(2), 1f));
        body.addView(sh, lp(WRAP, 16));

        final HorizontalScrollView hsv = new HorizontalScrollView(this);
        hsv.setHorizontalScrollBarEnabled(false);
        LinearLayout seedRow = new LinearLayout(this);
        seedRow.setOrientation(LinearLayout.HORIZONTAL);
        seedRow.setClipChildren(false);
        seedRow.setClipToPadding(false);
        seedRow.setPadding(dp(2), dp(8), dp(2), dp(12));
        View selCard = null;
        for (int s2 = ns - 1; s2 >= 0; s2--) {
            final int fs = s2;
            boolean isSel = s2 == selSi;
            FrameLayout fc = new FrameLayout(this);
            fc.setClipChildren(false);
            fc.setClipToPadding(false);
            fc.setBackground(rr(cCream, isSel ? cGreen : Color.parseColor("#EADFC0"), isSel ? 3 : 1, 16));
            fc.setElevation(dp(isSel ? 4 : 2));
            LinearLayout in = new LinearLayout(this);
            in.setOrientation(LinearLayout.VERTICAL);
            in.setGravity(Gravity.CENTER_HORIZONTAL);
            in.setPadding(dp(6), dp(8), dp(6), dp(8));
            in.addView(seedIcon(tahapAll[s2]), new LinearLayout.LayoutParams(dp(44), dp(44)));
            TextView nm = text(SambungAyat.NAMA[s2], 12, cDark, true);
            nm.setSingleLine(true);
            nm.setEllipsize(TextUtils.TruncateAt.END);
            in.addView(nm, lp(WRAP, 4));
            TextView ay = text(SambungAyat.jumlahAyat(s2) + " Ayat", 11, Color.parseColor("#8A7F68"), false);
            in.addView(ay, new LinearLayout.LayoutParams(WRAP, WRAP));
            fc.addView(in, new FrameLayout.LayoutParams(MATCH, MATCH));
            if (isSel) {
                TextView ck = text("✓", 12, Color.WHITE, true);
                ck.setBackground(rr(cGreen, 0, 0, 12));
                FrameLayout.LayoutParams ckp = new FrameLayout.LayoutParams(dp(22), dp(22), Gravity.TOP | Gravity.END);
                ckp.topMargin = dp(-6);
                ckp.rightMargin = dp(-4);
                fc.addView(ck, ckp);
                selCard = fc;
            }
            fc.setClickable(true);
            pressable(fc);
            fc.setOnClickListener(v -> {
                kebunScrollX = hsv.getScrollX();
                kebunPilih(fs);
            });
            LinearLayout.LayoutParams fcp = new LinearLayout.LayoutParams(dp(88), dp(108));
            fcp.rightMargin = dp(8);
            fcp.topMargin = dp(8);
            seedRow.addView(fc, fcp);
        }
        hsv.addView(seedRow);
        hsv.setClipToPadding(false);
        hsv.setClipChildren(false);
        body.addView(hsv, new LinearLayout.LayoutParams(MATCH, WRAP));
        final View fSel = selCard;
        hsv.post(() -> {
            int sx = kebunScrollX;
            if (fSel != null && (fSel.getLeft() < sx || fSel.getRight() > sx + hsv.getWidth())) {
                sx = Math.max(0, fSel.getLeft() - (hsv.getWidth() - fSel.getWidth()) / 2);
            }
            kebunScrollX = sx;
            hsv.scrollTo(sx, 0);
        });

        android.widget.ScrollView bodyScroll = new android.widget.ScrollView(this);
        bodyScroll.setFillViewport(false);
        bodyScroll.addView(body, new LinearLayout.LayoutParams(MATCH, WRAP));
        root.addView(bodyScroll, new LinearLayout.LayoutParams(MATCH, WRAP));
        wrap(root);
    }

    // ---------- Tebak Gambar (versi 6.10.0): "Ini gambar apa?", memakai set pasangan bergambar ----------
    private boolean tebak;

    private boolean tebakOn() {
        return tebak && mode == 7 && advLevel < 0;
    }

    private boolean tebakAvailable(int s) {
        String[][] pp = picturePairs(s, kelas);
        return pp != null && pp.length >= 4;
    }

    private void startTebak(int s) {
        subj = s;
        advPart = -1;
        alam = false;
        tebak = true;
        sambung = false;
        startGame(7);
    }

    private void buildTebak() {
        String[][] pp = picturePairs(subj, kelas);
        ArrayList<String[]> pool = new ArrayList<>();
        if (pp != null) {
            pool.addAll(distinctPairs(new ArrayList<>(Arrays.asList(pp))));
        }
        Collections.shuffle(pool, rnd);
        if (pool.size() < 4) {
            tebak = false;
            buildBuiltin();
            return;
        }
        total = Math.min(qCount(), pool.size());
        for (int i = 0; i < total; i++) {
            String[] it0 = pool.get(i);
            ArrayList<String> wrong = new ArrayList<>();
            for (String[] o : pool) {
                if (o != it0) {
                    wrong.add(o[1]);
                }
            }
            Collections.shuffle(wrong, rnd);
            ArrayList<String> opts = new ArrayList<>();
            opts.add(it0[1]);
            for (int j = 0; j < 3; j++) {
                opts.add(wrong.get(j));
            }
            Collections.shuffle(opts, rnd);
            Q q = new Q();
            q.text = it0[0] + "\n" + (subj == 2 ? "What is this?" : "Ini gambar apa?");
            q.full = q.text;
            q.sopts = new String[4];
            for (int j = 0; j < 4; j++) {
                q.sopts[j] = opts.get(j);
                if (q.sopts[j].equals(it0[1])) {
                    q.correctIdx = j;
                }
            }
            q.expl = it0[0] + " = " + it0[1];
            qs[i] = q;
        }
    }

    private static final String[] ALAM_NAMA = {"Ani", "Budi", "Siti", "Dimas", "Rina", "Udin", "Dewi"};
    private static final String[] ALAM_SAYUR = {"wortel", "tomat", "terong", "cabai", "bayam"};
    private static final String[] ALAM_BUAH = {"mangga", "jambu", "pisang", "jeruk", "rambutan"};

    private String alamPick(String[] a) {
        return a[rnd.nextInt(a.length)];
    }

    // Bentuk soal: pertanyaan|jawaban benar|salah 1|salah 2|salah 3|pembahasan
    private String alamQ(String text, int ans, String expl) {
        // Kelas 1-2: pengecoh berjauhan dan tidak melewati batas bilangan kelas
        boolean low = kelas <= 1;
        int cap = kelas <= 1 ? 10 : Integer.MAX_VALUE;
        ArrayList<Integer> ds = new ArrayList<>(low
                ? Arrays.asList(3, -3, 5, -5, 7, -7, 10, -10)
                : Arrays.asList(1, -1, 2, -2, 3, -3, 10, -10));
        Collections.shuffle(ds, rnd);
        StringBuilder sb = new StringBuilder();
        sb.append(text).append('|').append(fmt(ans));
        int added = 0;
        for (int d : ds) {
            int v = ans + d;
            if (v < (low ? 1 : 0) || v == ans || (low && v > cap)) {
                continue;
            }
            sb.append('|').append(fmt(v));
            added++;
            if (added == 3) {
                break;
            }
        }
        sb.append('|').append(expl);
        return sb.toString();
    }

    // Templat cerita Kelas 1-3. Awalan: A tambah, S kurang, M kali, D bagi.
    // Penanda: {a} {b} {n} {m} {t} {nm} {sy} {bh}. Buatan Claude, perlu dicek guru kelas.
    private static final String[] ALAM_STORY_S = {
        "A|{nm} memetik {a} {sy} pagi hari dan {b} {sy} sore hari. Berapa semuanya?",
        "A|Di sawah ada {a} bebek. Lalu datang {b} bebek lagi. Berapa semua bebek?",
        "A|Di sungai ada {a} ikan kecil dan {b} ikan besar. Berapa semua ikan?",
        "A|Di hutan ada {a} burung di pohon mangga dan {b} burung di pohon jambu. Berapa semua burung?",
        "A|{nm} mengumpulkan {a} kerang di pantai. Temannya mengumpulkan {b} kerang. Berapa semua kerang?",
        "A|Kebun {nm} punya {a} bunga merah dan {b} bunga kuning. Berapa semua bunga?",
        "A|Pak Tani menanam {a} pohon {bh} di kebun dan {b} pohon di halaman. Berapa semua pohon?",
        "A|Ada {a} kupu-kupu di bunga dan {b} kupu-kupu di daun. Berapa semua kupu-kupu?",
        "S|Ada {a} burung di pohon. {b} burung terbang pergi. Berapa burung yang masih di pohon?",
        "S|Di kolam ada {a} ikan. {b} ikan dimakan bebek. Berapa ikan yang tersisa?",
        "S|Kebun {nm} punya {a} bunga. {b} bunga dipetik untuk guru. Berapa bunga yang tersisa?",
        "S|Di sawah ada {a} bebek. {b} bebek pulang ke kandang. Berapa bebek yang masih di sawah?",
        "S|{nm} punya {a} kerang. {b} kerang diberikan kepada adik. Berapa kerang yang tersisa?",
        "S|Di hutan ada {a} kelinci. {b} kelinci masuk ke lubang. Berapa kelinci di luar lubang?",
        "S|Pohon {bh} punya {a} buah. {b} buah jatuh ke tanah. Berapa buah yang masih di pohon?",
        "S|Ada {a} perahu di sungai. {b} perahu pergi ke laut. Berapa perahu yang tersisa di sungai?"
    };
    private static final String[] ALAM_STORY_L = {
        "A|Hasil panen padi pagi hari {a} kg dan siang hari {b} kg. Berapa kg hasil panen seluruhnya?",
        "A|Pak Tani menjual {a} butir telur hari Senin dan {b} butir telur hari Selasa. Berapa butir telur yang terjual?",
        "A|Nelayan mendapat {a} ikan di sungai dan {b} ikan di pantai. Berapa ikan yang didapat seluruhnya?",
        "A|Di hutan lindung ada {a} pohon jati dan {b} pohon mahoni. Berapa semua pohon?",
        "A|{nm} memetik {a} {bh} di kebun pagi hari dan {b} {bh} sore hari. Berapa {bh} yang dipetik?",
        "A|Sawah Pak Dimas menghasilkan {a} kg gabah dan sawah Pak Udin {b} kg. Berapa kg gabah seluruhnya?",
        "S|Gudang punya {a} kg beras. Sebanyak {b} kg dijual. Berapa kg beras yang tersisa?",
        "S|Peternak punya {a} telur. Sebanyak {b} telur dikirim ke pasar. Berapa telur yang tersisa?",
        "S|Di kolam ada {a} ikan. Sebanyak {b} ikan diambil untuk dijual. Berapa ikan yang masih di kolam?",
        "S|Kebun punya {a} batang pohon. Sebanyak {b} batang ditebang karena sakit. Berapa batang yang tersisa?",
        "S|{nm} membawa {a} kg {sy} ke pasar. Pembeli membeli {b} kg. Berapa kg {sy} yang tersisa?",
        "S|Penjaga pantai punya {a} botol air. Sebanyak {b} botol dibagikan. Berapa botol yang tersisa?",
        "M|Ada {n} keranjang. Setiap keranjang berisi {m} {bh}. Berapa semua {bh}?",
        "M|Satu baris sawah ditanami {m} bibit padi. Ada {n} baris. Berapa semua bibit?",
        "M|Ada {n} ember. Setiap ember berisi {m} ikan. Berapa semua ikan?",
        "M|Setiap pohon kelapa berbuah {m} kelapa. Ada {n} pohon. Berapa semua kelapa?",
        "M|Pantai punya {n} payung. Setiap payung dipakai {m} orang. Berapa semua orang?",
        "D|Ada {t} {bh} dibagi sama banyak ke {n} keranjang. Berapa {bh} di tiap keranjang?",
        "D|{t} bibit ditanam sama banyak di {n} baris. Berapa bibit tiap baris?",
        "D|{t} ikan dimasukkan sama banyak ke {n} kolam. Berapa ikan tiap kolam?",
        "D|Nelayan membagi {t} kerang sama banyak untuk {n} anak. Berapa kerang tiap anak?"
    };

    // Cerita Kelas 1 (sampai 10), Kelas 2 (sampai 10), Kelas 3 (ratusan, kali-bagi kecil)
    private String alamStoryOne() {
        String[] pool = kelas <= 1 ? ALAM_STORY_S : ALAM_STORY_L;
        String tpl = pool[rnd.nextInt(pool.length)];
        char ty = tpl.charAt(0);
        String text = tpl.substring(2);
        text = text.replace("{nm}", alamPick(ALAM_NAMA)).replace("{sy}", alamPick(ALAM_SAYUR))
                .replace("{bh}", alamPick(ALAM_BUAH));
        int a;
        int b;
        if (ty == 'A') {
            if (kelas == 0) {
                a = range(2, 9);
                b = range(1, 10 - a);
            } else if (kelas == 1) {
                a = range(3, 7);
                b = range(2, 10 - a);
            } else {
                a = range(120, 480);
                b = range(100, 350);
            }
            return alamQ(text.replace("{a}", fmt(a)).replace("{b}", fmt(b)), a + b,
                    fmt(a) + " + " + fmt(b) + " = " + fmt(a + b) + ".");
        } else if (ty == 'S') {
            if (kelas == 0) {
                a = range(4, 10);
                b = range(1, a - 1);
            } else if (kelas == 1) {
                a = range(6, 10);
                b = range(2, a - 2);
            } else {
                a = range(500, 900);
                b = range(100, 400);
            }
            return alamQ(text.replace("{a}", fmt(a)).replace("{b}", fmt(b)), a - b,
                    fmt(a) + " \u2212 " + fmt(b) + " = " + fmt(a - b) + ".");
        }
        int n = range(3, 9);
        int m = range(3, 9);
        if (ty == 'M') {
            return alamQ(text.replace("{n}", "" + n).replace("{m}", "" + m), n * m,
                    n + " \u00D7 " + m + " = " + (n * m) + ".");
        }
        return alamQ(text.replace("{t}", "" + (n * m)).replace("{n}", "" + n), m,
                (n * m) + " \u00F7 " + n + " = " + m + ".");
    }

    private String alamMathOne() {
        int t = rnd.nextInt(5);
        String nm = alamPick(ALAM_NAMA);
        String sy = alamPick(ALAM_SAYUR);
        String bh = alamPick(ALAM_BUAH);
        int a;
        int b;
        int n;
        int m;
        switch (kelas) {
            case 0:
            case 1:
            case 2:
                return alamStoryOne();
            case 3:
                if (t == 0) {
                    a = range(12, 47) * 100;
                    b = range(5, 24) * 100;
                    return alamQ("Petani memanen " + fmt(a) + " kg padi. Minggu lalu ia memanen " + fmt(b)
                            + " kg lagi. Berapa kg padi seluruhnya?", a + b,
                            fmt(a) + " + " + fmt(b) + " = " + fmt(a + b) + ".");
                } else if (t == 1) {
                    a = range(30, 89) * 100;
                    b = range(5, 24) * 50;
                    return alamQ("Gudang menyimpan " + fmt(a) + " kg gabah. Sebanyak " + fmt(b)
                            + " kg dikirim ke kota. Berapa kg gabah yang tersisa?", a - b,
                            fmt(a) + " \u2212 " + fmt(b) + " = " + fmt(a - b) + ".");
                } else if (t == 2) {
                    n = range(12, 39);
                    m = range(6, 14);
                    return alamQ("Ada " + n + " kotak telur. Setiap kotak berisi " + m + " telur. Berapa semua telur?",
                            n * m, n + " \u00D7 " + m + " = " + (n * m) + ".");
                } else if (t == 3) {
                    n = range(12, 39);
                    m = range(6, 14);
                    return alamQ("Sebanyak " + fmt(n * m) + " bibit ditanam sama banyak di " + n
                            + " petak. Berapa bibit di tiap petak?", m, fmt(n * m) + " \u00F7 " + n + " = " + m + ".");
                }
                a = range(2, 8) * 1000;
                n = range(2, 5);
                return alamQ("Sekilo " + sy + " harganya Rp" + fmt(a) + ". " + nm + " membeli " + n
                        + " kg. Berapa rupiah yang harus dibayar?", a * n,
                        fmt(a) + " \u00D7 " + n + " = " + fmt(a * n) + ".");
            case 4:
                if (t == 0) {
                    n = range(25, 59);
                    m = range(12, 29);
                    return alamQ("Satu petak ditanami " + m + " bibit. Ada " + n + " petak. Berapa semua bibit?",
                            n * m, n + " \u00D7 " + m + " = " + fmt(n * m) + ".");
                } else if (t == 1) {
                    n = range(12, 24);
                    m = range(12, 39);
                    return alamQ("Sebanyak " + fmt(n * m) + " kg jagung dimasukkan ke " + n
                            + " karung sama berat. Berapa kg isi tiap karung?", m,
                            fmt(n * m) + " \u00F7 " + n + " = " + m + ".");
                } else if (t == 2) {
                    int avg = range(30, 59);
                    int d1 = range(1, 8);
                    int d2 = range(1, 8);
                    int x = avg - d1;
                    int y = avg + d2;
                    int z = avg + d1 - d2;
                    return alamQ("Panen tiga hari berturut-turut: " + x + " kg, " + y + " kg, dan " + z
                            + " kg. Berapa kg rata-rata panen per hari?", avg,
                            "(" + x + " + " + y + " + " + z + ") \u00F7 3 = " + avg + ".");
                } else if (t == 3) {
                    a = range(40, 89) * 100;
                    b = range(10, 39) * 100;
                    return alamQ("Kebun A menghasilkan " + fmt(a) + " kg sayur, kebun B menghasilkan " + fmt(b)
                            + " kg. Berapa selisih hasilnya?", a - b,
                            fmt(a) + " \u2212 " + fmt(b) + " = " + fmt(a - b) + ".");
                }
                a = range(10, 29);
                b = range(5, 9);
                return alamQ("Kebun sekolah berbentuk persegi panjang, panjang " + a + " m dan lebar " + b
                        + " m. Berapa luas kebun (dalam m\u00B2)?", a * b,
                        "Luas = panjang \u00D7 lebar = " + a + " \u00D7 " + b + " = " + (a * b) + ".");
            default:
                if (t == 0) {
                    int[] tots = {100, 200, 300, 400, 500, 800};
                    int[] ps = {10, 20, 25, 50};
                    a = tots[rnd.nextInt(tots.length)];
                    b = ps[rnd.nextInt(ps.length)];
                    return alamQ("Dari " + a + " bibit, " + b + "% tumbuh subur. Berapa bibit yang tumbuh subur?",
                            a * b / 100, b + "% \u00D7 " + a + " = " + (a * b / 100) + ".");
                } else if (t == 1) {
                    int avg = range(40, 89);
                    int d1 = range(1, 8);
                    int d2 = range(1, 8);
                    int p1 = avg - d1;
                    int p2 = avg + d1;
                    int p3 = avg - d2;
                    int p4 = avg + d2;
                    return alamQ("Panen empat hari: " + p1 + " kg, " + p2 + " kg, " + p3 + " kg, dan " + p4
                            + " kg. Berapa kg rata-rata panen per hari?", avg,
                            "(" + p1 + " + " + p2 + " + " + p3 + " + " + p4 + ") \u00F7 4 = " + avg + ".");
                } else if (t == 2) {
                    a = range(15, 40);
                    b = range(5, 14);
                    return alamQ("Kebun berbentuk persegi panjang, panjang " + a + " m dan lebar " + b
                            + " m. Berapa meter keliling kebun?", 2 * (a + b),
                            "Keliling = 2 \u00D7 (" + a + " + " + b + ") = " + (2 * (a + b)) + ".");
                } else if (t == 3) {
                    int k = range(2, 9);
                    return alamQ("Perbandingan ayam dan bebek di peternakan adalah 3 : 2. Jika ada " + (3 * k)
                            + " ayam, berapa bebek?", 2 * k,
                            (3 * k) + " \u00F7 3 = " + k + ", lalu " + k + " \u00D7 2 = " + (2 * k) + ".");
                }
                a = range(12, 24);
                b = range(5, 19);
                return alamQ("Selang mengalirkan " + a + " liter air per menit ke kebun. Berapa liter air dalam " + b
                        + " menit?", a * b, a + " \u00D7 " + b + " = " + (a * b) + ".");
        }
    }

    private ArrayList<String> alamMathPool() {
        ArrayList<String> out = new ArrayList<>();
        ArrayList<String> seen = new ArrayList<>();
        int guard = 0;
        while (out.size() < qCount() && guard < 300) {
            guard++;
            String s = alamMathOne();
            String text = s.substring(0, s.indexOf('|'));
            if (seen.contains(text)) {
                continue;
            }
            seen.add(text);
            out.add(s);
        }
        return out;
    }

    private void buildAlam() {
        ArrayList<String> pool = new ArrayList<>();
        if (subj == 0) {
            pool = alamMathPool();
        } else {
            pool.addAll(Arrays.asList((subj == 1 ? ALAM_ID : ALAM_EN)[kelas]));
            Collections.shuffle(pool, rnd);
        }
        total = Math.min(qCount(), pool.size());
        for (int i = 0; i < total; i++) {
            String[] f = pool.get(i).split("\\|");
            ArrayList<Integer> ord = new ArrayList<>();
            for (int j = 0; j < 4; j++) {
                ord.add(j);
            }
            Collections.shuffle(ord, rnd);
            Q q = new Q();
            q.text = f[0];
            q.full = f[0];
            q.sopts = new String[4];
            for (int j = 0; j < 4; j++) {
                q.sopts[j] = f[1 + ord.get(j)];
                if (ord.get(j) == 0) {
                    q.correctIdx = j;
                }
            }
            q.expl = (f.length > 5 && !f[5].isEmpty()) ? f[5] : autoExpl(f[0], f[1]);
            qs[i] = q;
        }
    }

    // Soal bertema alam. Format: pertanyaan|jawaban benar|salah 1|salah 2|salah 3|pembahasan (boleh kosong)
    // 10 soal per kelas, Kelas 1 sampai 6.
    private static final String[][] ALAM_ID = {
        {
            "Hewan yang berkokok di pagi hari adalah ...|ayam|sapi|ikan|kucing|Ayam jantan berkokok di pagi hari.",
            "Sapi menghasilkan ...|susu|telur|madu|beras|Sapi diperah untuk diambil susunya.",
            "Tumbuhan butuh air dan ... supaya tumbuh.|cahaya matahari|permen|bola|sepatu|Tumbuhan butuh air dan cahaya matahari.",
            "Kita menyiram tanaman dengan ...|air|pasir|batu|sabun|Air membuat tanaman tidak layu.",
            "Kita mencium bau bunga dengan ...|hidung|mata|kaki|tangan|Hidung adalah alat untuk mencium bau.",
            "Hewan yang hidup di sungai adalah ...|ikan|sapi|ayam|kambing|Ikan hidup di dalam air sungai.",
            "Lengkapi kata: po_on (tempat daun tumbuh).|h|k|m|t|Kata yang benar: pohon.",
            "Lengkapi kata: ke_un (tempat menanam sayur).|b|d|m|s|Kata yang benar: kebun.",
            "Kita membuang sampah di ...|tempat sampah|sungai|jalan|kelas|Sampah dibuang di tempat sampah agar lingkungan bersih.",
            "Kupu-kupu terbang dengan ...|sayap|roda|sirip|tanduk|Kupu-kupu punya sayap yang indah.",
            "\uD83C\uDF33 Pohon tumbuh di ...|tanah|air|awan|kamar|Pohon tumbuh di tanah.",
            "\uD83D\uDC04 Sapi makan ...|rumput|batu|kertas|sabun|Sapi makan rumput.",
            "Ikan bernapas dengan ...|insang|tangan|kaki|rambut|Ikan bernapas dengan insang.",
            "Telur ayam dihasilkan oleh ...|ayam|sapi|kucing|ikan|Telur ayam dihasilkan oleh ayam.",
            "Ulat akan berubah menjadi ...|kupu-kupu|ikan|burung|katak|Ulat menjadi kupu-kupu setelah menjadi kepompong.",
            "Awan gelap biasanya membawa ...|hujan|pasir|api|batu|Awan gelap menandakan akan turun hujan.",
            "Padi tumbuh di ...|sawah|laut|gurun|langit|Padi ditanam di sawah.",
            "Nasi berasal dari ...|padi|ikan|sapi|telur|Nasi dibuat dari beras yang berasal dari padi.",
            "Hewan yang hidup di hutan adalah ...|harimau|ayam|bebek|kambing|Harimau hidup di hutan.",
            "Kita minum air supaya tidak ...|haus|marah|gelap|takut|Minum air menghilangkan rasa haus.",
            "Kita merawat tanaman dengan cara ...|menyiram|membakar|menginjak|membuang|Tanaman dirawat dengan disiram.",
            "\uD83D\uDC1D Lebah menghasilkan ...|madu|susu|telur|beras|Lebah menghasilkan madu.",
            "Gunung itu ...|tinggi|kecil|pendek|rendah|Gunung adalah bagian bumi yang menjulang tinggi.",
            "Lengkapi kata: ik_n (hidup di air).|a|u|o|e|Kata yang benar: ikan.",
            "Lengkapi kata: bu_ga (ada di taman).|n|m|k|t|Kata yang benar: bunga."
        },
        {
            "Kambing suka makan ...|rumput|daging|batu|plastik|Kambing memakan rumput dan daun.",
            "Biji yang ditanam di tanah akan tumbuh menjadi ...|tanaman|batu|pasir|air|Biji yang disiram akan tumbuh menjadi tanaman.",
            "Bagian tumbuhan yang menyerap air dari tanah adalah ...|akar|daun|bunga|buah|Akar menyerap air dari dalam tanah.",
            "Pak Tani menanam padi di ...|sawah|bengkel|bandara|toko|Padi ditanam di sawah.",
            "Persamaan kata (sinonim) 'indah' adalah ...|cantik|buruk|kotor|bau",
            "Lawan kata 'kering' adalah ...|basah|panas|jauh|tinggi",
            "Kulit pisang termasuk sampah ...|organik|plastik|kaca|logam|Sampah organik berasal dari makhluk hidup dan bisa membusuk.",
            "Kalimat yang benar adalah ...|Petani menanam padi di sawah.|Padi sawah petani di menanam.|Menanam di padi sawah.|Sawah petani padi di.|Kalimat yang baik punya susunan kata yang jelas.",
            "Kata 'bunga' dipisah menjadi suku kata ...|bu-nga|bun-ga|b-unga|bung-a|Kata bunga dipisah menjadi bu-nga.",
            "Lengkapi kata: ker_au (hewan pembajak sawah).|b|d|m|p|Kata yang benar: kerbau.",
            "Bagian tumbuhan yang berwarna-warni dan harum adalah ...|bunga|akar|batang|tanah|Bunga berwarna-warni dan wangi.",
            "Bagian tumbuhan yang menopang pohon agar tegak adalah ...|batang|bunga|buah|biji|Batang menopang tumbuhan.",
            "Ayam berkembang biak dengan cara ...|bertelur|melahirkan|membelah diri|bertunas|Ayam bertelur.",
            "Kucing berkembang biak dengan cara ...|melahirkan|bertelur|bertunas|berbunga|Kucing melahirkan anak.",
            "Air hujan jatuh dari ...|awan|tanah|pohon|batu|Hujan turun dari awan.",
            "Air sungai mengalir menuju ...|laut|gunung|awan|atap|Air sungai mengalir ke laut.",
            "Hewan yang hidup di air dan di darat adalah ...|katak|ikan|burung|sapi|Katak hidup di air dan di darat.",
            "Tumbuhan hijau membuat makanan dengan bantuan cahaya ...|matahari|bulan|kilat|api|Tumbuhan membuat makanan dengan cahaya matahari.",
            "Pohon kelapa banyak tumbuh di daerah ...|pantai|kutub|gurun|laut dalam|Kelapa banyak tumbuh di pantai.",
            "Kata 'sungai' terdiri dari ... suku kata.|dua|satu|tiga|empat|Su-ngai terdiri dari dua suku kata.",
            "Lengkapi kata: sa_ah (tempat menanam padi).|w|b|m|k|Kata yang benar: sawah.",
            "Lengkapi kata: ke_inci (hewan berbulu lembut).|l|m|p|t|Kata yang benar: kelinci.",
            "Lawan kata 'basah' adalah ...|kering|dingin|besar|jauh|Basah lawannya kering.",
            "Tumbuhan di sawah yang menghasilkan beras adalah ...|padi|kelapa|pisang|kopi|Padi menghasilkan beras.",
            "Cacing menggemburkan tanah sehingga tanah menjadi ...|subur|keras|kering|sempit|Cacing membuat tanah gembur dan subur."
        },
        {
            "Tumbuhan membuat makanan dengan bantuan cahaya matahari. Prosesnya disebut ...|fotosintesis|pernapasan|penguapan|pencernaan|Fotosintesis terjadi di daun yang hijau.",
            "Hewan pemakan tumbuhan disebut ...|herbivora|karnivora|omnivora|insektivora|Herbivora memakan tumbuhan, misalnya sapi dan kambing.",
            "Kata dasar dari 'menanam' adalah ...|tanam|nam|menan|tanaman|Awalan me- ditambahkan pada kata dasar tanam.",
            "Sampah plastik sebaiknya ...|didaur ulang|dibakar|dibuang ke sungai|ditimbun di kebun|Daur ulang mengurangi sampah dan menjaga bumi.",
            "Persamaan kata (sinonim) 'elok' adalah ...|indah|jelek|kecil|cepat",
            "Kalimat yang memakai tanda baca benar adalah ...|Kami menanam cabai, tomat, dan terong.|Kami menanam cabai tomat dan terong|Kami, menanam cabai tomat. dan terong|Kami menanam, cabai tomat dan, terong|Tanda koma memisahkan benda dalam sebuah rincian.",
            "Air hujan yang meresap ke dalam tanah menjadi ...|air tanah|awan|garam|lumpur|Air hujan yang meresap tersimpan sebagai air tanah.",
            "Jika tanaman disiram setiap hari, tanaman akan ...|tumbuh subur|layu|mati|berlari|Air yang cukup membuat tanaman tumbuh subur.",
            "Beternak artinya memelihara ...|hewan|batu|awan|mobil|Beternak berarti memelihara hewan seperti ayam atau sapi.",
            "Penulisan yang benar adalah ...|sayuran|sayurran|sayoran|sayuren|Tulisan yang benar: sayuran.",
            "Hewan pemakan daging disebut ...|karnivora|herbivora|omnivora|insektivora|Karnivora memakan daging, misalnya harimau.",
            "Hewan pemakan tumbuhan dan daging disebut ...|omnivora|herbivora|karnivora|insektivora|Omnivora memakan tumbuhan dan daging, misalnya ayam.",
            "Daur hidup kupu-kupu: telur, ulat, kepompong, lalu ...|kupu-kupu|belalang|lebah|capung|Setelah kepompong menjadi kupu-kupu dewasa.",
            "Air berubah menjadi uap karena panas. Peristiwa ini disebut ...|penguapan|pembekuan|pengembunan|pencairan|Penguapan: air menjadi uap.",
            "Uap air berubah menjadi titik air di awan. Peristiwa ini disebut ...|pengembunan|penguapan|pembekuan|pencairan|Pengembunan: uap menjadi air.",
            "Bagian bunga yang berkembang menjadi buah adalah ...|bakal buah|mahkota|kelopak|tangkai|Bakal buah berkembang menjadi buah.",
            "Menanam pohon bermanfaat untuk ...|mencegah banjir dan longsor|menambah sampah|mengotori air|menghabiskan udara|Akar pohon menahan tanah dan air.",
            "Sampah plastik sulit terurai. Sebaiknya kita ... sampah plastik.|mengurangi|membakar di kebun|membuang ke sungai|mengubur di sawah|Mengurangi sampah plastik menjaga lingkungan.",
            "Sungai yang kotor dapat menyebabkan ...|penyakit|panen melimpah|udara segar|ikan bertambah|Sungai kotor menimbulkan penyakit.",
            "Kata 'pertanian' berasal dari kata dasar ...|tani|per|tanian|an|Pertanian terdiri dari per-, tani, dan -an.",
            "Penanam adalah orang yang ...|menanam|memetik|menjual|memasak|Penanam adalah orang yang menanam.",
            "Penulisan yang benar adalah ...|ladang|ladan|ladnag|laddang|Tulisan yang benar: ladang.",
            "Penulisan yang benar adalah ...|nelayan|nelyan|nealayan|nelayaan|Tulisan yang benar: nelayan.",
            "Kalimat yang benar adalah ...|Petani menanam padi di sawah.|Menanam petani padi di.|Padi di petani sawah menanam.|Sawah padi menanam petani di.|Kalimat harus punya susunan kata yang jelas.",
            "Hewan yang bernapas dengan insang dan hidup di air adalah ...|ikan|katak dewasa|burung|sapi|Ikan bernapas dengan insang."
        },
        {
            "Makhluk hidup dan lingkungannya yang saling berhubungan disebut ...|ekosistem|galaksi|kota|musim|Ekosistem terdiri dari makhluk hidup dan lingkungannya.",
            "Rantai makanan dimulai dari ...|tumbuhan|singa|elang|ular|Tumbuhan membuat makanan sendiri sehingga menjadi awal rantai makanan.",
            "Hewan pemakan tumbuhan dan daging disebut ...|omnivora|herbivora|karnivora|nokturnal|Omnivora memakan tumbuhan sekaligus daging.",
            "Kata baku yang benar adalah ...|kedelai|kedele|kedelei|kadelai|Penulisan baku yang benar adalah kedelai.",
            "Kata penanam berasal dari kata dasar ...|tanam|penan|nam|tanaman|Penanam = pe- + tanam.",
            "Kalimat yang tersusun baik adalah ...|Petani memanen jagung di kebun.|Jagung memanen petani di.|Di kebun jagung petani memanen mereka.|Petani itu mereka memanen jagung kebun.|Kalimat yang baik punya susunan kata yang jelas.",
            "Daun kering dan sisa sayur dapat diolah menjadi ...|kompos|plastik|kaca|baterai|Kompos adalah pupuk dari sisa tumbuhan dan makanan.",
            "Kata 'rimbun' berarti ...|lebat dan banyak daunnya|gundul|kering|kecil|Pohon yang rimbun daunnya banyak dan lebat.",
            "Tanda baca yang tepat: Ayo kita jaga bumi ...|!|,|;|-|Kalimat ajakan atau seruan memakai tanda seru.",
            "Kata 'lestari' dalam 'menjaga alam tetap lestari' artinya ...|terjaga dan tidak rusak|dibakar|ditebang|dijual|Lestari berarti tetap terjaga dan tidak rusak."
        },
        {
            "Hutan yang ditebang sembarangan dapat menyebabkan ...|banjir dan longsor|hujan permen|udara bersih|tanah subur|Tanpa pohon, tanah tidak tertahan dan air mudah meluap.",
            "Majas pada 'Angin berbisik di sela-sela daun' adalah ...|personifikasi|hiperbola|simile|ironi|Angin (benda mati) digambarkan seperti makhluk hidup yang berbisik.",
            "Kalimat yang berisi fakta adalah ...|Padi tumbuh di sawah yang berair.|Padi adalah tanaman paling indah.|Semua orang pasti suka padi.|Padi pasti enak sekali.|Fakta dapat dibuktikan kebenarannya.",
            "Kata pertanian berasal dari kata dasar ...|tani|pertan|tanian|pe|Pertanian = per- + tani + -an.",
            "Ungkapan 'ringan tangan' berarti ...|suka menolong|tangannya kecil|tidak mau bekerja|suka marah|Ringan tangan berarti suka membantu orang lain.",
            "Persamaan kata (sinonim) 'melestarikan' adalah ...|menjaga|merusak|menebang|membuang|Melestarikan berarti menjaga agar tidak rusak.",
            "Kalimat majemuk setara adalah ...|Kami menanam pohon, dan teman-teman menyiramnya.|Kami menanam pohon.|Pohon itu tinggi.|Teman menyiram.|Kalimat majemuk punya dua pokok kalimat atau lebih.",
            "Daur ulang artinya ...|mengolah barang bekas agar dapat dipakai lagi|membakar sampah|membuang sampah|menumpuk sampah|Daur ulang mengubah barang bekas menjadi barang yang berguna.",
            "Penulisan kata baku yang benar adalah ...|ekosistem|ekosestim|ekosistim|ecosistem|Penulisan baku yang benar adalah ekosistem.",
            "Peribahasa 'Sedikit demi sedikit, lama-lama menjadi bukit' mengajarkan kita untuk ...|rajin menabung dan bersabar|boros|malas bekerja|sombong|Hal kecil yang dilakukan terus-menerus akan menjadi banyak."
        },
        {
            "Pemanasan global terutama disebabkan oleh meningkatnya gas ... di udara.|karbon dioksida|oksigen|nitrogen|helium|Gas karbon dioksida menahan panas di atmosfer.",
            "Kalimat yang memakai majas hiperbola adalah ...|Tanaman itu tinggi sekali, hampir menyentuh langit.|Tanaman itu tinggi.|Tanaman itu hijau.|Tanaman itu disiram.|Hiperbola adalah gaya bahasa yang melebih-lebihkan.",
            "Hutan bakau (mangrove) bermanfaat bagi pantai karena ...|mencegah abrasi|menyebabkan banjir|mengeringkan laut|menambah sampah|Akar bakau menahan ombak sehingga tanah pantai tidak terkikis.",
            "Penulisan kata baku yang benar adalah ...|aktivitas|aktifitas|aktivitaz|aktifittas|Penulisan baku yang benar adalah aktivitas.",
            "Perumpamaan 'bagai air di daun talas' berarti ...|tidak tetap pendiriannya|sangat kuat|sangat basah|sangat ringan|Air di daun talas mudah bergulir, seperti orang yang tidak tetap pendirian.",
            "Kalimat yang berisi opini adalah ...|Kebun sekolah kita paling indah di dunia.|Kebun sekolah ditanami 20 pohon.|Kebun sekolah berada di belakang kelas.|Kebun sekolah disiram setiap pagi.|Opini adalah pendapat seseorang, bukan fakta yang bisa dibuktikan.",
            "Kalimat majemuk bertingkat adalah ...|Kami bersyukur karena panen berhasil.|Kami panen jagung.|Panen berhasil.|Kami bersyukur.|Ada induk kalimat dan anak kalimat yang dihubungkan kata karena.",
            "Persamaan kata (sinonim) 'konservasi' adalah ...|pelestarian|perusakan|penjualan|penebangan|Konservasi berarti pelestarian alam.",
            "Lawan kata 'subur' adalah ...|tandus|hijau|basah|lebat|Tanah yang tandus tidak subur.",
            "Peribahasa 'Di mana bumi dipijak, di situ langit dijunjung' artinya ...|kita menghormati adat tempat kita tinggal|kita memanjat langit|kita menanam padi|kita berjalan jauh|Kita harus menghormati aturan dan adat di tempat kita berada."
        }
    };

    private static final String[][] ALAM_EN = {
        {
            "Apa bahasa Inggris dari 'pohon'?|tree|bee|sea|key",
            "Apa bahasa Inggris dari 'bunga'?|flower|water|finger|summer",
            "Apa bahasa Inggris dari 'daun'?|leaf|leg|lip|lamp",
            "Apa bahasa Inggris dari 'matahari'?|sun|moon|star|cloud",
            "Apa bahasa Inggris dari 'hujan'?|rain|wind|snow|sun",
            "Apa bahasa Inggris dari 'sapi'?|cow|goat|duck|hen",
            "Apa bahasa Inggris dari 'ayam'?|chicken|duck|cow|goat",
            "Apa bahasa Inggris dari 'bebek'?|duck|cow|cat|bird",
            "Apa bahasa Inggris dari 'ikan'?|fish|bird|frog|bee",
            "Apa bahasa Inggris dari 'kupu-kupu'?|butterfly|bee|bird|bat",
            "\uD83D\uDC04 Apa bahasa Inggris dari 'sapi'?|cow|cat|dog|hen|Sapi = cow.",
            "\uD83D\uDC14 Apa bahasa Inggris dari 'ayam betina'?|hen|pig|cow|bee|Ayam betina = hen.",
            "\uD83D\uDC1D Apa bahasa Inggris dari 'lebah'?|bee|cat|rain|tree|Lebah = bee.",
            "\u2600\uFE0F Apa bahasa Inggris dari 'matahari'?|sun|moon|star|rain|Matahari = sun.",
            "\uD83C\uDF27\uFE0F Apa bahasa Inggris dari 'hujan'?|rain|sun|wind|cloud|Hujan = rain.",
            "\u2601\uFE0F Apa bahasa Inggris dari 'awan'?|cloud|sun|moon|star|Awan = cloud.",
            "Apa bahasa Inggris dari 'rumput'?|grass|glass|green|gray|Rumput = grass.",
            "\uD83D\uDC38 Apa bahasa Inggris dari 'katak'?|frog|fish|bird|duck|Katak = frog.",
            "\uD83E\uDD86 Apa bahasa Inggris dari 'bebek'?|duck|pig|bee|cow|Bebek = duck.",
            "\uD83D\uDC10 Apa bahasa Inggris dari 'kambing'?|goat|cow|hen|duck|Kambing = goat.",
            "\uD83C\uDF0A Apa bahasa Inggris dari 'laut'?|sea|tree|sun|cow|Laut = sea.",
            "\uD83E\uDD55 Apa bahasa Inggris dari 'wortel'?|carrot|apple|rice|milk|Wortel = carrot.",
            "\uD83C\uDF45 Apa bahasa Inggris dari 'tomat'?|tomato|potato|banana|corn|Tomat = tomato.",
            "\uD83C\uDF3D Apa bahasa Inggris dari 'jagung'?|corn|rice|egg|milk|Jagung = corn.",
            "Apa bahasa Inggris dari 'tanah'?|soil|sky|sun|sea|Tanah = soil."
        },
        {
            "Apa bahasa Inggris dari 'kebun'?|garden|gate|game|goat",
            "Apa bahasa Inggris dari 'sungai'?|river|rice|road|roof",
            "Apa bahasa Inggris dari 'gunung'?|mountain|market|moon|monkey",
            "Apa bahasa Inggris dari 'petani'?|farmer|teacher|doctor|driver",
            "Apa bahasa Inggris dari 'kambing'?|goat|cat|boat|coat",
            "Apa bahasa Inggris dari 'kelinci'?|rabbit|rubber|robot|ribbon",
            "Apa bahasa Inggris dari 'rumput'?|grass|glass|gas|grape",
            "Apa bahasa Inggris dari 'biji'?|seed|seat|seven|sand",
            "Lengkapi: The cow gives us ...|milk|eggs|honey|wool|Sapi menghasilkan susu (milk).",
            "Lengkapi: A bird has two ...|wings|wheels|fins|horns|Burung punya dua sayap (wings).",
            "Apa bahasa Inggris dari 'sawah'?|rice field|river|road|roof|Sawah = rice field.",
            "Apa bahasa Inggris dari 'kerbau'?|buffalo|bear|bee|bird|Kerbau = buffalo.",
            "Apa bahasa Inggris dari 'tanaman'?|plant|plate|plane|play|Tanaman = plant.",
            "Apa bahasa Inggris dari 'akar'?|root|leaf|seed|stem|Akar = root.",
            "Apa bahasa Inggris dari 'danau'?|lake|lamp|leaf|lion|Danau = lake.",
            "Apa bahasa Inggris dari 'pulau'?|island|river|road|rock|Pulau = island.",
            "Lengkapi: Fish swim in the ...|water|sky|tree|house|Ikan berenang di air (water).",
            "Lengkapi: Birds can ...|fly|dig|drive|read|Burung bisa terbang (fly).",
            "Lengkapi: A cat says ...|meow|moo|woof|quack|Kucing mengeong (meow).",
            "Lengkapi: A dog says ...|woof|meow|moo|quack|Anjing menggonggong (woof).",
            "Lengkapi: A cow says ...|moo|woof|meow|quack|Sapi bersuara moo.",
            "Apa bahasa Inggris dari 'angin'?|wind|rain|sun|cloud|Angin = wind.",
            "Apa bahasa Inggris dari 'pelangi'?|rainbow|rain|cloud|wind|Pelangi = rainbow.",
            "Apa bahasa Inggris dari 'bintang'?|star|moon|sun|sky|Bintang = star."
        },
        {
            "Lengkapi: Plants need water and ... to grow.|sunlight|candy|toys|shoes|Tumbuhan butuh air dan cahaya matahari (sunlight).",
            "Apa bahasa Inggris dari 'hutan'?|forest|farm|flower|fish",
            "Apa bahasa Inggris dari 'bunga matahari'?|sunflower|sunshine|sunset|sunrise",
            "Apa bahasa Inggris dari 'pelangi'?|rainbow|rain|rainy|raincoat",
            "Apa bahasa Inggris dari 'awan'?|cloud|clock|close|clown",
            "Apa bahasa Inggris dari 'sampah'?|trash|train|tree|truck",
            "Lengkapi: We must ... the trees and flowers.|water|break|burn|throw|Kita menyiram (water) pohon dan bunga.",
            "Apa bahasa Inggris dari 'ular'?|snake|snail|spider|sheep",
            "Lengkapi: Fish live in the ...|water|tree|sky|sand|Ikan hidup di air (water).",
            "Lengkapi: A farmer grows rice in the ...|field|kitchen|bank|garage|Petani menanam padi di sawah (field).",
            "Lengkapi: The sun gives us ...|light|milk|paper|shoes|Matahari memberi kita cahaya (light).",
            "Lengkapi: Farmers plant seeds in the ...|soil|sky|sea|kitchen|Petani menanam biji di tanah (soil).",
            "Lengkapi: A tadpole becomes a ...|frog|fish|bird|cat|Berudu menjadi katak (frog).",
            "Lengkapi: A caterpillar becomes a ...|butterfly|bee|bat|bird|Ulat menjadi kupu-kupu (butterfly).",
            "Apa arti 'rainforest'?|hutan hujan|ladang padi|pantai|gunung|Rainforest = hutan hujan.",
            "Apa arti 'waterfall'?|air terjun|sungai kecil|kolam|laut|Waterfall = air terjun.",
            "Apa arti 'beach'?|pantai|gunung|sawah|hutan|Beach = pantai.",
            "Apa arti 'owl'?|burung hantu|elang|bebek|angsa|Owl = burung hantu.",
            "Lengkapi: We must not throw ... into the river.|trash|water|flowers|fish|Kita tidak boleh membuang sampah (trash) ke sungai.",
            "Lengkapi: Trees give us fresh ...|air|trash|plastic|smoke|Pohon memberi kita udara (air) yang segar.",
            "Apa arti 'insect'?|serangga|ikan|burung|kuda|Insect = serangga.",
            "Apa arti 'harvest'?|panen|menanam|menyiram|menjual|Harvest = panen.",
            "Lengkapi: Cows eat ...|grass|meat|plastic|stones|Sapi makan rumput (grass).",
            "Lengkapi: A bee makes ...|honey|milk|rice|bread|Lebah membuat madu (honey)."
        },
        {
            "Lengkapi: Butterflies have colorful ...|wings|wheels|keys|boots|Kupu-kupu punya sayap (wings) yang berwarna-warni.",
            "Apa bahasa Inggris dari 'akar'?|root|roof|rope|road",
            "Apa bahasa Inggris dari 'tanah'?|soil|soap|sail|seal",
            "Apa bahasa Inggris dari 'pupuk'?|fertilizer|water|seed|soil",
            "Apa bahasa Inggris dari 'musim hujan'?|rainy season|dry season|cold season|windy day",
            "Lengkapi: The sun rises in the ...|east|west|north|south|Matahari terbit di timur (east).",
            "Lengkapi: Cows eat ... in the field.|grass|rocks|plastic|meat|Sapi makan rumput (grass).",
            "Apa bahasa Inggris dari 'menanam'?|plant|pick|cook|clean",
            "Lengkapi: Please do not ... trash into the river.|throw|plant|drink|water|Jangan membuang (throw) sampah ke sungai.",
            "Bentuk lampau dari 'grow' adalah ...|grew|growed|grown|growing|Bentuk lampau dari grow adalah grew."
        },
        {
            "Lengkapi: Farmers ... the rice in the field yesterday.|planted|plant|plants|planting|Yesterday (kemarin) memakai bentuk lampau: planted.",
            "Lengkapi: Look! The bird is ... in the tree.|singing|sing|sang|sings|Look! menunjukkan kegiatan yang sedang berlangsung: is singing.",
            "Lengkapi: She ... flowers every morning.|waters|water|watering|watered|Untuk kebiasaan dengan She, kata kerja ditambah s: waters.",
            "Apa bahasa Inggris dari 'sampah organik'?|organic waste|plastic waste|glass waste|metal waste",
            "Lengkapi: We should save water to protect the ...|environment|kitchen|market|pencil|Kita menjaga lingkungan (environment).",
            "Lawan kata 'clean' adalah ...|dirty|clear|cold|close",
            "Apa arti 'harvest'?|panen|tanam|siram|jual",
            "Apa arti 'recycle'?|daur ulang|buang|bakar|beli",
            "Lengkapi: There ... many fish in the river.|are|is|am|be|Many fish (banyak ikan) memakai are.",
            "Apa arti 'compost'?|kompos|kompor|komputer|kontes"
        },
        {
            "Lengkapi: If we plant more trees, the air ... cleaner.|will be|was|is being|were|Kalimat pengandaian masa depan memakai will be.",
            "Lengkapi: The river is ... than the pond.|longer|long|longest|more long|Perbandingan dua benda: longer than.",
            "Lengkapi: Mount Bromo is one of the ... mountains in Indonesia.|most beautiful|more beautiful|beautifulest|beautiful|Perbandingan paling: most beautiful.",
            "Lengkapi: Last week, we ... a tree in the school garden.|planted|plant|plants|will plant|Last week (minggu lalu) memakai bentuk lampau: planted.",
            "Lengkapi: The trash ... collected by the students every Friday.|is|are|was|were|Kalimat pasif kebiasaan dengan trash: is collected.",
            "Apa arti 'endangered animals'?|hewan yang terancam punah|hewan peliharaan|hewan buas|hewan ternak",
            "Apa arti 'pollution'?|pencemaran|perayaan|pelajaran|perjalanan",
            "Apa arti 'rainforest'?|hutan hujan tropis|hutan jati|kebun teh|padang pasir",
            "Bentuk lampau dari 'bring' adalah ...|brought|bringed|bringing|brang|Bentuk lampau dari bring adalah brought.",
            "Lengkapi: We must not cut down trees ... they give us oxygen.|because|but|or|so|Because menyatakan alasan: karena pohon memberi kita oksigen."
        }
    };

    // ---------- Soal peta: kuis bawaan dan latihan tabel ----------

    private String autoExpl(String q, String ans) {
        int a = q.indexOf('\'');
        int b = a >= 0 ? q.indexOf('\'', a + 1) : -1;
        String w = (a >= 0 && b > a) ? q.substring(a + 1, b) : "";
        boolean hasW = !w.isEmpty();
        if (q.startsWith("Apa bahasa Inggris dari") && hasW) {
            return "'" + w + "' dalam bahasa Inggris adalah " + ans + ".";
        }
        if (q.startsWith("Apa arti") && hasW) {
            return "'" + w + "' artinya " + ans + ".";
        }
        if (q.startsWith("Lawan kata") && hasW) {
            return "Lawan kata '" + w + "' adalah " + ans + ".";
        }
        if (q.startsWith("Bentuk lampau") && hasW) {
            return "Bentuk lampau dari '" + w + "' adalah " + ans + ".";
        }
        if (q.startsWith("Kata baku")) {
            return "Penulisan baku yang benar adalah " + ans + ".";
        }
        return "Jawaban yang benar adalah " + ans + ".";
    }

    private void buildBuiltin() {
        String[] src = (subj == 1 ? BI_ID : BI_EN)[kelas];
        int half = src.length / 2;
        int from = advPart <= 0 ? 0 : half;
        int to = advPart < 0 ? src.length : (advPart == 0 ? half : src.length);
        ArrayList<String> pool = new ArrayList<>();
        for (int i = from; i < to; i++) {
            pool.add(src[i]);
        }
        Collections.shuffle(pool, rnd);
        total = Math.min(qCount(), pool.size());
        for (int i = 0; i < total; i++) {
            String[] f = pool.get(i).split("\\|");
            ArrayList<Integer> ord = new ArrayList<>();
            for (int j = 0; j < 4; j++) {
                ord.add(j);
            }
            Collections.shuffle(ord, rnd);
            Q q = new Q();
            q.text = f[0];
            q.full = f[0];
            q.sopts = new String[4];
            for (int j = 0; j < 4; j++) {
                q.sopts[j] = f[1 + ord.get(j)];
                if (ord.get(j) == 0) {
                    q.correctIdx = j;
                }
            }
            q.expl = (f.length > 5 && !f[5].isEmpty()) ? f[5] : autoExpl(f[0], f[1]);
            qs[i] = q;
        }
    }

    // Latihan tabel perkalian: 7 soal kali, lalu 3 soal bagi dengan angka yang sama
    private void buildTable() {
        total = TOTAL;
        int n = tableN;
        ArrayList<Integer> ms = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            ms.add(i);
        }
        Collections.shuffle(ms, rnd);
        for (int i = 0; i < TOTAL; i++) {
            int m = ms.get(i);
            Q q = new Q();
            if (i < 7) {
                boolean swap = rnd.nextBoolean();
                q.text = (swap ? m + " \u00D7 " + n : n + " \u00D7 " + m) + " = ?";
                q.answer = n * m;
                q.full = n + " \u00D7 " + m + " = " + fmt(n * m);
                q.key = "t" + n + "x" + m;
                fillTableOptions(q, n);
            } else {
                q.text = fmt(n * m) + " \u00F7 " + n + " = ?";
                q.answer = m;
                q.full = fmt(n * m) + " \u00F7 " + n + " = " + m;
                q.key = "t" + n + "d" + m;
                fillTableOptions(q, 1);
            }
            qs[i] = q;
        }
    }

    // Pilihan jawaban yang mirip dengan jawaban benar (selisih satu langkah tabel, atau 1, 2, 10)
    private void fillTableOptions(Q q, int step) {
        int ans = q.answer;
        ArrayList<Integer> cand = new ArrayList<>();
        int[] ds = {step, -step, 1, -1, 2 * step, -2 * step, 10, -10, 2, -2};
        for (int d : ds) {
            int v = ans + d;
            if (v >= 1 && v != ans && !cand.contains(v)) {
                cand.add(v);
            }
        }
        Collections.shuffle(cand, rnd);
        int pos = rnd.nextInt(4);
        Arrays.fill(q.opts, -1);
        q.opts[pos] = ans;
        q.correctIdx = pos;
        int ci = 0;
        for (int k = 0; k < 4; k++) {
            if (k == pos) {
                continue;
            }
            q.opts[k] = cand.get(ci++);
        }
    }

    // ---------- Hasil level peta ----------

    private void showAdvResult() {
        setScreen(2);
        int stars = advStarsFor(pCorrect[0], total);
        boolean perfect = total >= 5 && pCorrect[0] == total;
        int old = advStarsOf(advK, advLevel);
        advSave(stars);
        advWorldNew = old < 1 && stars >= 1 && advDone(advK) == LEVELS[advK].length;
        freshBadges = awardBadges(0, stars, perfect, false);
        awardMapBadges(freshBadges);
        renderAdvResult(true);
    }

    private void renderAdvResult(boolean fx) {
        setScreen(2);
        final int stars = advStarsFor(pCorrect[0], total);
        boolean passed = stars >= 1;
        String[] p = lv(advK, advLevel);

        LinearLayout c = column();
        c.setGravity(Gravity.CENTER);
        TextView avt = text(BUDDY_ICON[advK], 40, Color.WHITE, false);
        avt.setBackground(medallionBg(KELAS_COLOR[advK], 3));
        avt.setElevation(dp(6));
        c.addView(avt, new LinearLayout.LayoutParams(dp(86), dp(86)));
        TextView title = text(passed ? "Level selesai!" : "Ayo coba lagi!", 30, INK, true);
        c.addView(title, lp(WRAP, 10));
        c.addView(pill("Level " + (advLevel + 1) + " \u2022 " + p[1], KELAS_COLOR[advK]), lpw(10));

        LinearLayout card = card();
        card.setGravity(Gravity.CENTER_HORIZONTAL);
        LinearLayout starRow = new LinearLayout(this);
        starRow.setOrientation(LinearLayout.HORIZONTAL);
        starRow.setGravity(Gravity.CENTER);
        TextView[] st = new TextView[3];
        for (int i = 0; i < 3; i++) {
            st[i] = text(i < stars ? "\u2B50" : "\u2606", 46, i < stars ? ORANGE : Color.parseColor("#D9C7A0"), false);
            starRow.addView(st[i], new LinearLayout.LayoutParams(WRAP, WRAP));
        }
        card.addView(starRow, lp(WRAP, 0));
        TextView sc = text("Skor: " + pScore[0], 26, INK, true);
        card.addView(sc, lp(WRAP, 10));
        TextView ben = text("Benar " + pCorrect[0] + " dari " + total, 16, INK_SOFT, false);
        card.addView(ben, lp(WRAP, 6));
        String hint = !passed ? "Butuh benar separuh atau lebih untuk membuka level berikutnya."
                : stars < 3 ? "Benar 90% ke atas dapat 3 bintang." : "Sempurna! Tiga bintang di level ini.";
        card.addView(text(hint, 13, INK_SOFT, false), lp(WRAP, 6));
        card.addView(text("\uD83C\uDF1F Total bintang: " + totalStars(), 14, GOLD_DEEP, true), lp(WRAP, 6));
        c.addView(card, lp(WRAP, 18));

        if (advWorldNew) {
            LinearLayout wd = card();
            wd.setGravity(Gravity.CENTER_HORIZONTAL);
            wd.addView(text("\uD83C\uDFF0 Dunia " + WORLD_NAME[advK] + " tamat!", 18, PURPLE, true), lp(WRAP, 0));
            wd.addView(text("Kamu menyelesaikan semua level " + KELAS_NAMES[advK] + ".", 13, DARK, false),
                    lp(WRAP, 4));
            c.addView(wd, lp(WRAP, 14));
        }
        addFreshBadges(c);

        if (hasMistakes()) {
            Button rev = button("Lihat Pembahasan", BLUE, 18);
            rev.setElevation(dp(4));
            c.addView(rev, lp(dp(56), 18));
            rev.setOnClickListener(v -> {
                reviewPlayer = 0;
                showReview();
            });
        }
        addAdvButtons(c, passed);
        wrap(c);
        if (fx) {
            playResultFx(title, st, stars, sc, ben, null, false);
            if (advWorldNew) {
                handler.postDelayed(() -> launchConfetti(200), 1800);
            }
        }
    }

    // Tombol di akhir level peta: berikutnya, ulangi, kembali ke peta
    private void addAdvButtons(LinearLayout c, boolean passed) {
        final boolean hasNext = passed && advLevel + 1 < LEVELS[advK].length;
        if (hasNext) {
            Button nx = button("Level berikutnya \u25B6", GREEN, 22);
            nx.setElevation(dp(6));
            c.addView(nx, lp(dp(64), 18));
            nx.setOnClickListener(v -> startLevel(advLevel + 1));
        }
        Button again = button("Ulangi level", hasNext ? BLUE : GREEN, hasNext ? 18 : 22);
        if (hasNext) {
            again.setBackground(ghostButtonBg());
            again.setTextColor(BLUE);
            again.setElevation(dp(2));
        } else {
            again.setElevation(dp(6));
        }
        c.addView(again, lp(dp(hasNext ? 56 : 64), hasNext ? 12 : 18));
        again.setOnClickListener(v -> startLevel(advLevel));

        Button map = button("\uD83D\uDDFA\uFE0F Kembali ke Peta", ORANGE, 20);
        map.setBackground(ghostButtonBg());
        map.setTextColor(ORANGE_DARK);
        map.setElevation(dp(2));
        c.addView(map, lp(dp(60), 12));
        map.setOnClickListener(v -> showPeta());
    }

    // Gambar tertanam (cadangan bila folder assets/img tidak ada)
    private static final class ImgData {
        static final String[] BG = {
            "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"
        };
        static final String[] BTN = {
            "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"
        };
    }
}
