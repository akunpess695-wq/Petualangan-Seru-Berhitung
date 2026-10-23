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

    private static final String APP_NAME = "Belajar Bersama";
    private static final String BANK_FILE = "bank.json";
    private static final int REQ_IMPORT = 41;
    private static final int REQ_BACKUP_SAVE = 42;
    private static final int REQ_BACKUP_OPEN = 43;
    private static final String APP_VERSION = "6.6.4";
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

    private static final String[] AVATARS = {
            "\uD83E\uDD81", "\uD83D\uDC2F", "\uD83D\uDC3C", "\uD83D\uDC28", "\uD83D\uDC38", "\uD83E\uDD8A",
            "\uD83D\uDC30", "\uD83D\uDC36", "\uD83D\uDC31", "\uD83E\uDD84", "\uD83D\uDC27", "\uD83E\uDD89",
            "\uD83D\uDE42"
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

    @Override
    public void onConfigurationChanged(android.content.res.Configuration nc) {
        super.onConfigurationChanged(nc);
        if (prefs == null) {
            return;
        }
        applyDisplayProfile();
        boolean dialogUp = (menuDialog != null && menuDialog.isShowing())
                || (pickDialog != null && pickDialog.isShowing());
        if (screen == 0 && !dialogUp) {
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
        saveStats();
        if (screen == 2) {
            handler.removeCallbacksAndMessages(null);
        }
        pauseGame();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        resumeGame();
    }

    @Override
    protected void onDestroy() {
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

    private Drawable background() {
        return new BgDrawable();
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
            p.setColor(base);
            cv.drawRoundRect(rf, rad, rad, p);
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
        }

        @Override
        public void getOutline(Outline outline) {
            outline.setRoundRect(getBounds(), dp(22));
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
        t.setBackground(box(color, 20));
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
        b.setBackground(box(color, 22));
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
        l.setBackground(box(Color.WHITE, 26));
        l.setElevation(dp(5));
        return l;
    }

    private void wrap(LinearLayout c) {
        ScrollView sv = new ScrollView(this);
        sv.setFillViewport(true);
        sv.setBackground(background());
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
        return "best_k" + kelas + (mode == 7 ? "_b" + subj + (alamOn() ? "a" : "") : "") + (timed ? "_t" : "");
    }

    private String timeLabel() {
        return timed ? "Berwaktu " + secsNow() + " detik" : "Tanpa waktu";
    }

    private String modeLabel() {
        if (advLevel >= 0) {
            return "Petualangan \u2022 " + timeLabel();
        }
        String base = mode == 7 ? (alamOn() ? "Kuis Alam \u2022 " : "") + SUBJ_NAMES[subj] + " \u2022 " + timeLabel() : timeLabel();
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
            avatar[i] = Math.max(0, Math.min(AVATARS.length - 1, prefs.getInt("avatar" + i, i)));
            genderOf[i] = prefs.getInt("gender" + i, 0) == 1 ? 1 : 0;
        }
    }

    private void showMenu() {
        handler.removeCallbacksAndMessages(null);
        screen = 0;
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
        TextView snd = text(sfxOn ? "\uD83D\uDD0A" : "\uD83D\uDD07", 22, DARK, false);
        snd.setBackground(box(Color.WHITE, 26));
        snd.setElevation(dp(3));
        snd.setOnClickListener(v -> {
            prefs.edit().putBoolean("sfx", !sfxOn).apply();
            showMenu();
            sfx(SFX_CORRECT);
        });
        bar.addView(snd, new LinearLayout.LayoutParams(dp(52), dp(52)));
        TextView dsp = text("\uD83D\uDCFA", 22, DARK, false);
        dsp.setBackground(box(Color.WHITE, 26));
        dsp.setElevation(dp(3));
        dsp.setOnClickListener(v -> dialogTampilan());
        LinearLayout.LayoutParams dspLp = new LinearLayout.LayoutParams(dp(52), dp(52));
        dspLp.leftMargin = dp(10);
        bar.addView(dsp, dspLp);
        bar.addView(new View(this), new LinearLayout.LayoutParams(0, 1, 1f));
        AvatarView avb = avatarView(0, 1);
        avb.setElevation(dp(3));
        avb.setOnClickListener(v -> showToko());
        bar.addView(avb, new LinearLayout.LayoutParams(dp(52), dp(52)));
        c.addView(bar, lp(WRAP, 0));

        // Spanduk judul
        LinearLayout hero = new LinearLayout(this);
        hero.setOrientation(LinearLayout.VERTICAL);
        hero.setGravity(Gravity.CENTER_HORIZONTAL);
        hero.setPadding(dp(16), dp(20), dp(16), dp(20));
        GradientDrawable hg = new GradientDrawable(GradientDrawable.Orientation.TL_BR,
                new int[]{Color.parseColor("#2E7D32"), Color.parseColor("#29B6F6")});
        hg.setCornerRadius(dp(32));
        hero.setBackground(hg);
        hero.setElevation(dp(6));
        hero.setOnLongClickListener(v -> {
            openAdmin();
            return true;
        });
        ImageView logo = new ImageView(this);
        logo.setImageResource(R.drawable.logo);
        logo.setScaleType(ImageView.ScaleType.FIT_CENTER);
        logo.setPadding(dp(6), dp(6), dp(6), dp(6));
        logo.setBackground(box(Color.WHITE, 24));
        logo.setElevation(dp(4));
        hero.addView(logo, new LinearLayout.LayoutParams(dp(96), dp(96)));
        hero.addView(text(APP_NAME, 26, Color.WHITE, true), lp(WRAP, 10));
        TextView alam = text("\uD83C\uDF33 SD ALAM \uD83C\uDF3F", 20, Color.WHITE, true);
        alam.setBackground(box(Color.argb(70, 255, 255, 255), 20));
        alam.setPadding(dp(16), dp(4), dp(16), dp(4));
        hero.addView(alam, lpw(8));
        hero.addView(text("Muhammadiyah Kandangan", 14, Color.WHITE, true), lp(WRAP, 6));
        hero.addView(text("Ayo belajar bersama alam!", 14,
                Color.parseColor("#E8F5E9"), false), lp(WRAP, 4));
        hero.addView(text("\uD83C\uDF31 \uD83C\uDF3B \uD83E\uDD8B \uD83C\uDF33 \uD83C\uDF43", 20,
                Color.WHITE, false), lp(WRAP, 6));
        c.addView(hero, lp(WRAP, 10));

        // Tiga tombol pengaturan: kelas, pemain, waktu
        LinearLayout opt = new LinearLayout(this);
        opt.setOrientation(LinearLayout.HORIZONTAL);
        Button bk = button("\uD83C\uDF93 " + KELAS_NAMES[kelas], KELAS_COLOR[kelas], 13);
        flat(bk);
        bk.setElevation(dp(3));
        bk.setOnClickListener(v -> dialogKelas());
        opt.addView(bk, weight(0, 4));
        Button bp = button("\uD83D\uDC65 " + (np == 1 ? "Sendiri" : np + " pemain"), Color.WHITE, 13);
        bp.setTextColor(DARK);
        flat(bp);
        bp.setElevation(dp(3));
        bp.setOnClickListener(v -> dialogPemain());
        opt.addView(bp, weight(4, 4));
        Button bw = button("\u23F1\uFE0F " + (timed ? secsNow() + " detik" : "Bebas"), Color.WHITE, 13);
        bw.setTextColor(DARK);
        flat(bw);
        bw.setElevation(dp(3));
        bw.setOnClickListener(v -> dialogWaktu());
        opt.addView(bw, weight(4, 0));
        c.addView(opt, lp(dp(54), 16));

        // Pilihan nyawa: ada nyawa atau belajar santai
        Button bn = button(relaxed ? "\uD83D\uDC9A Belajar santai \u2022 tanpa nyawa"
                : "\u2764\uFE0F Ada nyawa", Color.WHITE, 14);
        bn.setTextColor(DARK);
        flat(bn);
        bn.setElevation(dp(3));
        bn.setOnClickListener(v -> dialogNyawa());
        c.addView(bn, lp(dp(46), 10));

        // Peta Petualangan
        LinearLayout mp = subjectTile("\uD83D\uDDFA\uFE0F", "Peta Petualangan", advSummary(), GREEN);
        mp.setOnClickListener(v -> showPeta());
        rt.addView(mp, lp(dp(96), wide ? 0 : 18));

        // Pilih pelajaran
        rt.addView(text("Pilih pelajaran", 16, DARK, true), lp(WRAP, wide ? 16 : 20));
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
            t.setOnClickListener(v -> openSubject(sj));
            rt.addView(t, lp(dp(96), 14));
        }

        // Toko Avatar
        Button tk = button("\uD83D\uDECD\uFE0F Toko Avatar \u2022 \uD83D\uDCB0 " + coins() + " koin", PINK, 15);
        flat(tk);
        tk.setElevation(dp(3));
        tk.setOnClickListener(v -> showToko());
        c.addView(tk, lp(dp(54), 18));

        // Soal dari guru dan rekor skor, sebaris
        LinearLayout foot = new LinearLayout(this);
        foot.setOrientation(LinearLayout.HORIZONTAL);
        Button gr = button((hasPin() ? "\uD83D\uDD12" : "\uD83D\uDCDA") + " Soal Guru", Color.WHITE, 14);
        gr.setTextColor(DARK);
        flat(gr);
        gr.setElevation(dp(3));
        gr.setOnClickListener(v -> openGuru());
        foot.addView(gr, weight(0, 4));
        Button rk = button("\uD83C\uDFC5 Rekor & Lencana", Color.WHITE, 13);
        rk.setTextColor(DARK);
        flat(rk);
        rk.setElevation(dp(3));
        foot.addView(rk, weight(4, 0));
        c.addView(foot, lp(dp(54), 22));
        rk.setOnClickListener(v -> showRekor());

        c.setFocusableInTouchMode(true);
        c.requestFocus();
        if (wide) {
            wrapTwo(c, cr);
        } else {
            wrap(c);
        }
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
        screen = 8;
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
        t.setClickable(true);
        t.setFocusable(true);
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
        startGame(7);
    }

    // ---------- Menu Matematika ----------

    private void showMath() {
        handler.removeCallbacksAndMessages(null);
        screen = 6;
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
        screen = 7;
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
            return alamOn() ? "Kuis Alam" : "Kuis soal";
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
        screen = 11;
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
            importMsg = "Berkas ini bukan cadangan Belajar Bersama.";
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
            {6, "#212121", "Rambut Hitam Pendek", 30},
            {6, "#6D4C41", "Rambut Acak-acakan", 40},
            {6, "#F9C74F", "Rambut Pirang Jabrik", 60},
            {6, "#F06292", "Rambut Pink Panjang", 100},
            {6, "#B0BEC5", "Rambut Perak Keren", 150},
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
  {6, "#BCAAA4", "Jilbab Cokelat", 60}
    };
    private static final int ITEM_GALAKSI = 31;
    // Gender tiap barang, urut sama dengan ITEMS: u = semua, b = putra, g = putri.
    // Untuk memindah barang ke gender lain, cukup ubah hurufnya di sini.
    private static final String ITEM_GENDER =
  "bggubbub" + "ugbu" + "bugubbu" + "uuuuuuuu" + "uuuuuuu" + "bbbbb" + "bbbbb" + "ggggg" + "bb" + "ggggg";

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
    private static final String[] SHOP_TAB_ICON = {
            "\uD83D\uDC3E", "\uD83C\uDFA9", "\uD83D\uDD76\uFE0F", "\u26BD", "\uD83D\uDC23", "\uD83C\uDFA8",
            "\uD83D\uDC55", "\uD83D\uDC87"};
    private static final String[] SHOP_TAB_NAME = {"Hewan", "Topi", "Wajah", "Pegang", "Teman", "Latar", "Pakaian", "Rambut"};

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
    private static final String[] HOOD_MAIN = {"#F2B24C", "#F28C28", "#FFFFFF", "#9AA5AD", "#5DBB4A", "#E8631C",
            "#F6EEF3", "#A8744A", "#F2A65A", "#F3EAF7", "#37474F", "#8D6E4B"};
    private static final String[] HOOD_ACC = {"#B5651D", "#212121", "#212121", "#DDE3E6", "#FFFFFF", "#FFFFFF",
            "#F8BBD0", "#5D3A1E", "#C77B30", "#F06292", "#FFFFFF", "#E6D5B8"};

    // ---------- Avatar 3D tahap 1: data mesh balok ----------
    private static final int MAXF = 700;
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
        private final float[] srcPts = new float[8];
        private final android.graphics.Matrix decalM = new android.graphics.Matrix();
        private int fn = 0;
        int gender = 0;
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
        private boolean curHood = false;
        private boolean curSkipFace = false; private float zBias = 0f; private boolean skipTop = false;
        private float curHt = 13.5f;

        private int shade(int color, float k) {
            int r = Math.min(255, (int) (Color.red(color) * k));
            int g = Math.min(255, (int) (Color.green(color) * k));
            int b = Math.min(255, (int) (Color.blue(color) * k));
            return Color.rgb(r, g, b);
        }

        // kind: 0 polos, 1 kepala, 2 badan, 3 lengan kanan (barang pegang)
        private void addBox(float x0, float y0, float x1, float y1, float z0, float z1,
                            int colSide, int colTop, int colBack, int kind) {
            for (int i = 0; i < 8; i++) {
                float x = ((i & 1) != 0 ? x1 : x0) - 50f;
                float y = ((i & 2) != 0 ? y1 : y0) - 50f;
                float z = (i & 4) != 0 ? z1 : z0;
                float xr = x * c3t + z * s3t;
                float zr = -x * s3t + z * c3t;
                float y2 = y * c3p - zr * s3p;
                float z2 = y * s3p + zr * c3p;
                float sc = 260f / (260f - z2);
                pxs[i] = fx(50f + xr * sc);
                pys[i] = fy(50f + y2 * sc);
                pzs[i] = z2;
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
                fz[i] = (pzs[q[0]] + pzs[q[1]] + pzs[q[2]] + pzs[q[3]]) / 4f + zBias;
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
            if (kind == 11) {
                if (curHid == 42) {
                    rect(c, 31f, 20f, 38f, 58f, 3f, curHairC);
                    rect(c, 62f, 20f, 69f, 58f, 3f, curHairC);
                }
            } else if (kind == 1) {
                drawFaceAll(c);
            } else if (kind == 4) {
                drawHat(c, it[0], curHt);
            } else if (kind == 2) {
                if (it[5] >= 0) {
                    drawClothes(c, it[5]);
                } else {
                    drawBadge(c);
                }
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
                // Gradasi lembut pada tiap sisi balok agar bentuk terasa seperti mainan 3D.
                float minX = Math.min(Math.min(fq[o], fq[o + 2]), Math.min(fq[o + 4], fq[o + 6]));
                float maxX = Math.max(Math.max(fq[o], fq[o + 2]), Math.max(fq[o + 4], fq[o + 6]));
                float minY = Math.min(Math.min(fq[o + 1], fq[o + 3]), Math.min(fq[o + 5], fq[o + 7]));
                float maxY = Math.max(Math.max(fq[o + 1], fq[o + 3]), Math.max(fq[o + 5], fq[o + 7]));
                p.setShader(new LinearGradient(minX, minY, maxX + 1f, maxY + 1f,
                        shade(fc[i], 1.13f), shade(fc[i], 0.88f), Shader.TileMode.CLAMP));
                c.drawPath(path, p);
                p.setShader(null);
                p.setStyle(Paint.Style.STROKE);
                p.setStrokeWidth(0.55f * u);
                p.setStrokeJoin(Paint.Join.ROUND);
                p.setColor(shade(fc[i], 0.72f));
                c.drawPath(path, p);
                p.setStyle(Paint.Style.FILL);
                if (fk[i] != 0) {
                    decal(c, i, fk[i]);
                }
            }
        }

        // Telinga / tanduk tudung hewan sebagai balok 3D
        private void addEars(int a, int main, int acc, float hx0, float hx1, float hy0, float hy1) {
            if (a == 0 || a == 1 || a == 2 || a == 3) {
                float w = a == 3 ? 11f : 9f;
                float h = a == 3 ? 9f : 7f;
                int ec = (a == 0 || a == 2) ? acc : main;
                addBox(hx0 - 3f, hy0 - h - 2f, hx0 - 3f + w, hy0 - 1f, -4.5f, 4.5f, ec, ec, ec, 0);
                addBox(hx1 + 3f - w, hy0 - h - 2f, hx1 + 3f, hy0 - 1f, -4.5f, 4.5f, ec, ec, ec, 0);
            } else if (a == 4) {
                addBox(hx0 + 2f, hy0 - 9f, hx0 + 13f, hy0 - 1f, -5f, 5f, main, main, main, 0);
                addBox(hx1 - 13f, hy0 - 9f, hx1 - 2f, hy0 - 1f, -5f, 5f, main, main, main, 0);
            } else if (a == 5 || a == 8) {
                addBox(hx0 - 1f, hy0 - 13f, hx0 + 7f, hy0 - 1f, -3f, 3f, main, main, main, 0);
                addBox(hx1 - 7f, hy0 - 13f, hx1 + 1f, hy0 - 1f, -3f, 3f, main, main, main, 0);
            } else if (a == 6) {
                addBox(hx0 + 5f, hy0 - 22f, hx0 + 12f, hy0 - 1f, -3f, 3f, main, main, main, 0);
                addBox(hx1 - 12f, hy0 - 22f, hx1 - 5f, hy0 - 1f, -3f, 3f, main, main, main, 0);
            } else if (a == 7) {
                addBox(hx0 - 7f, hy0 + 2f, hx0 - 1f, hy0 + 24f, -5f, 5f, acc, acc, acc, 0);
                addBox(hx1 + 1f, hy0 + 2f, hx1 + 7f, hy0 + 24f, -5f, 5f, acc, acc, acc, 0);
            } else if (a == 9) {
                int gold = col("#FFD54F");
                addBox(47f, hy0 - 15f, 53f, hy0 - 1f, -3f, 3f, gold, gold, gold, 0);
                addBox(hx0, hy0 - 8f, hx0 + 7f, hy0 - 1f, -3f, 3f, main, main, main, 0);
                addBox(hx1 - 7f, hy0 - 8f, hx1, hy0 - 1f, -3f, 3f, main, main, main, 0);
            } else if (a == 11) {
                int tuft = col("#6D4C41");
                addBox(hx0, hy0 - 11f, hx0 + 6f, hy0 - 1f, -3f, 3f, tuft, tuft, tuft, 0);
                addBox(hx1 - 6f, hy0 - 11f, hx1, hy0 - 1f, -3f, 3f, tuft, tuft, tuft, 0);
            }
        }

        // Avatar chibi anak SD: seragam putih-merah, putra / putri (kerudung), rambut dan tudung 3D.
        private void drawChar3D(Canvas c) {
            final int skin = col("#F6D3A8");
            final boolean hood = animal >= 0 && animal < NO_HOOD;
            final boolean girl = gender == 1;
            final boolean hijab = girl && !hood;
            curHood = hood;
            curHt = hood ? 8f : 13.5f;
            curHid = it[6];
            curHairC = curHid >= 0 ? col(itIcon(curHid)) : col("#4E342E");

            float th = (float) Math.toRadians(avatarAngle);
            float pitch = (float) Math.toRadians(-16f);
            c3t = (float) Math.cos(th);
            s3t = (float) Math.sin(th);
            c3p = (float) Math.cos(pitch);
            s3p = (float) Math.sin(pitch);

            p.setStyle(Paint.Style.FILL);
            p.setColor(0x33000000);
            rf.set(fx(20f), fy(86.5f), fx(80f), fy(96f));
            c.drawOval(rf, p);
            if (it[3] >= 0) {
                emoji(c, itIcon(it[3]), 14f, 78f, 24f);
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
            int shoe = col("#212121");
            final float hx0 = 31f;
            final float hx1 = 69f;
            final float hy0 = 8f;
            final float hy1 = 42f;
            final float hz = 17f;
            float armLen = longSleeve ? 16f : 8f;

            fn = 0;
            // Tas sekolah 3D di punggung. Kedalaman negatif membuatnya berada di belakang badan.
            // Tali bahu digambar sebagai balok tipis di depan dada agar tetap terlihat saat diputar.
            int bagMain = col("#2E7D32");
            int bagTop = col("#43A047");
            int bagDark = col("#1B5E20");
            addBox(39f, 45f, 61f, 61f, -13.5f, -7.2f, bagMain, bagTop, bagDark, 0);
            addBox(42f, 43.5f, 58f, 48f, -14.5f, -7.5f, bagTop, bagTop, bagMain, 0);
            addBox(40f, 45f, 43.5f, 58f, 7.1f, 8.3f, bagDark, bagDark, bagDark, 0);
            addBox(56.5f, 45f, 60f, 58f, 7.1f, 8.3f, bagDark, bagDark, bagDark, 0);
            addBox(46f, 50f, 54f, 53f, 8.2f, 8.8f, col("#A5D6A7"), col("#A5D6A7"), col("#2E7D32"), 0);
            // kaki / rok + sepatu
            float shoeTop = girl ? 85f : 83f;
            if (girl) {
                // Rok bertingkat sedikit melebar di bagian bawah agar siluet putri lebih jelas.
                addBox(35f, 62f, 65f, 85.5f, -8f, 8f, red, red, red, 0);
                addBox(33.5f, 72f, 66.5f, 85.5f, -8.4f, 8.4f, red, red, red, 0);
                int hem = col("#7F1722");
                addBox(34f, 83.8f, 66f, 85.6f, -8.6f, 8.6f, hem, hem, hem, 0);
            } else {
                addBox(38f, 64f, 49.7f, 83f, -5.5f, 5.5f, red, red, red, 0);
                addBox(50.3f, 64f, 62f, 83f, -5.5f, 5.5f, red, red, red, 0);
            }
            addBox(37f, shoeTop, 49.7f, 86.6f, -5.5f, 9f, shoe, shoe, shoe, 0); addBox(36.6f, 86.6f, 50.1f, 89f, -5.9f, 9.4f, col("#F5F5F5"), col("#F5F5F5"), col("#F5F5F5"), 0);
            addBox(50.3f, shoeTop, 63f, 86.6f, -5.5f, 9f, shoe, shoe, shoe, 0); addBox(49.9f, 86.6f, 63.4f, 89f, -5.9f, 9.4f, col("#F5F5F5"), col("#F5F5F5"), col("#F5F5F5"), 0);
            // tangan: kulit bawah, lengan baju atas
            addBox(27f, 43f + armLen, 36f, 61f, -4.5f, 4.5f, skin, skin, skin, 0);
            addBox(64f, 43f + armLen, 73f, 61f, -4.5f, 4.5f, skin, skin, skin, 0);
            addBox(27f, 43f, 36f, 43f + armLen, -4.5f, 4.5f, slv, slv, slv, 0);
            srcRect(66f, 45f, 76f, longSleeve ? 61f : 53f);
            addBox(64f, 43f, 73f, 43f + armLen, -4.5f, 4.5f, slv, slv, slv, 3);
            // badan
            srcRect(34f, 44f, 66f, 68f);
            addBox(36f, 42f, 64f, 64f, -7f, 7f, shirt, shirt, shirt, 2); if (!girl) { zBias = 6f; skipTop = true; addBox(35.5f, 60.6f, 64.5f, 64.6f, -7.5f, 7.5f, col("#2B2B2B"), col("#2B2B2B"), col("#2B2B2B"), 0); skipTop = false; zBias = 0f; }
            if (girl) {
                int wh = (curHid >= 0) ? curHairC : col("#F7F7F7");
                addBox(34f, 42f, 66f, 53f, 6.5f, 9f, wh, wh, wh, 0);
            }
            // kepala
            int hc = hood ? col(HOOD_MAIN[animal]) : (hijab ? ((curHid >= 0) ? curHairC : col("#F7F7F7")) : curHairC);
            srcRect(33f, 14f, 67f, 46f);
            addBox(hx0, hy0, hx1, hy1, -hz, hz, skin, hc, hc, 1);
            float m = (hood || hijab) ? 3f : 1.5f;
            // penutup atas + poni (stiker topi menempel di sisi depan balok ini)
            srcRect(33f, 14f, 67f, 46f);
            addBox(hx0 - m, hy0 - m - 1.5f, hx1 + m, hy0 + 7f, -hz - m, hz + m, hc, hc, hc, 0);
            // belakang kepala
            float backBottom = hood ? hy1 - 6f : hijab ? hy1 + 12f : (curHid == 42 ? hy1 + 8f : hy0 + 24f);
            addBox(hx0 - m, hy0 - m, hx1 + m, backBottom, -hz - m, -hz + 3f, hc, hc, hc, 0);
            // sisi kepala
            float sideBottom = hood ? hy1 - 10f : hijab ? hy1 + 6f : (curHid == 42 ? hy1 + 8f : hy0 + 14f);
            float sideZ1 = (hood || hijab) ? hz + m : 0f;
            addBox(hx0 - m, hy0 - m, hx0 + 3f, sideBottom, -hz - m, sideZ1, hc, hc, hc, 0);
            addBox(hx1 - 3f, hy0 - m, hx1 + m, sideBottom, -hz - m, sideZ1, hc, hc, hc, 0);
            if (hood) {
                // sedikit poni di dahi di bawah tudung
                int bangC = (girl && curHid < 0) ? col("#F7F7F7") : curHairC; addBox(hx0 + 3f, hy0 + 7f, hx1 - 3f, hy0 + 11f, hz - 0.5f, hz + 0.8f, bangC, bangC, bangC, 0);
                addEars(animal, hc, col(HOOD_ACC[animal]), hx0, hx1, hy0, hy1);
            } else if (!hijab) {
                if (curHid == 40) {
                    for (int k = 0; k < 3; k++) {
                        float cx = 38f + k * 12f;
                        addBox(cx - 4f, hy0 - 10f, cx + 4f, hy0 - 1.5f, -6f, 6f, hc, hc, hc, 0);
                    }
                } else if (curHid == 41) {
                    for (int k = 0; k < 4; k++) {
                        float cx = 36f + k * 10f;
                        addBox(cx - 3.5f, hy0 - 15f, cx + 3.5f, hy0 - 1.5f, -5f, 5f, hc, hc, hc, 0);
                    }
                } else if (curHid == 43) {
                    addBox(hx0 - m, hy0 + 7f, hx0 + 22f, hy0 + 14f, hz - 1f, hz + m, hc, hc, hc, 0);
                } else {
                    addBox(hx0 - m, hy0 + 7f, hx1 + m, hy0 + 12f, hz - 1f, hz + m, hc, hc, hc, 0);
                }
            }
            float hairLift = 0f;
            if (!hood && !hijab && (curHid < 0 || curHid == 39)) {
                addCurlyHair(hx0, hx1, hy0, hy1, hz, m, hc);
                hairLift = 5f;
            }
            curSkipFace = it[1] == 8 || it[1] == 10;
            if (it[0] >= 0) {
                addHat3D(it[0], hy0 - m - 1.5f - hairLift);
            }
            if (curSkipFace) {
                boolean cover = hood || hijab; zBias = c3t > 0.2f ? 40f : 0f; addGlasses3D(it[1], hx0, hx1, hz, cover ? m + 0.9f : 0.9f, cover ? -4f : 0f, cover ? hz + m + 0.5f : hz + 1.2f); zBias = 0f;
            }
            flushFaces(c);
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
            } else {
                int dk = col("#111111");
                for (int k = 0; k < 2; k++) {
                    float cx = ex[k];
                    bx(cx - 6.5f, ey - 4.2f, cx + 6.5f, ey + 4f, hz, hz + 1.2f, dk);
                }
                bx(48.5f, ey - 2.5f, 51.5f, ey - 1f, hz, hz + 1.2f, dk);
                bx(hx0 - off, ey - 2.5f, hx0 - off + 1.2f, ey - 1f, zb, zf, dk);
                bx(hx1 + off - 1.2f, ey - 2.5f, hx1 + off, ey - 1f, zb, zf, dk);
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

        // Lencana sekolah (perisai hijau) di dada seragam polos
        private void drawBadge(Canvas c) {
            path.reset();
            path.moveTo(fx(41f), fy(47f));
            path.lineTo(fx(49f), fy(47f));
            path.lineTo(fx(49f), fy(52.5f));
            path.lineTo(fx(45f), fy(56.5f));
            path.lineTo(fx(41f), fy(52.5f));
            path.close();
            p.setStyle(Paint.Style.FILL);
            p.setColor(col("#2E7D32"));
            c.drawPath(path, p);
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(0.7f * u);
            p.setStrokeJoin(Paint.Join.ROUND);
            p.setColor(col("#FFC107"));
            c.drawPath(path, p);
            p.setStyle(Paint.Style.FILL);
            dot(c, 45f, 50.6f, 1.3f, col("#FFC107"));
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
                float breath = (float) Math.sin(t * 2.0f) * 0.55f;
                c.translate(0f, breath * u);
                float pulse = 1f + (float) Math.sin(t * 2.0f) * 0.0025f;
                c.scale(pulse, pulse, fx(50f), fy(54f));
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
            final boolean hood = animal >= 0 && animal < NO_HOOD;
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
                rect(c, 35f, 64f, 65f, 86f, 3f, pants);
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
            float armEnd = longSleeve ? 61f : 53f;
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
            if (hid == 42 && gender != 1) {
                rect(c, 31f, 20f, 38f, 58f, 3f, hairC);
                rect(c, 62f, 20f, 69f, 58f, 3f, hairC);
            }
            rect(c, 33f, 14f, 67f, 46f, 6f, skin);
            boolean hijab2d = gender == 1 && !hood;
            if (hijab2d) {
                int wh = (hid >= 0) ? hairC : col("#F7F7F7");
                hoodRects(c, wh, 0f);
                rect(c, 36f, 45f, 64f, 53f, 3f, wh);
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
            if (hid == 40) {
                tri(c, 36f, 15f, 40f, 6f, 44f, 15f, hairC);
                tri(c, 45f, 15f, 50f, 5f, 55f, 15f, hairC);
                tri(c, 56f, 15f, 61f, 7f, 64f, 15f, hairC);
            } else if (hid == 41) {
                tri(c, 35f, 15f, 39f, 3f, 43f, 15f, hairC);
                tri(c, 42f, 15f, 46f, 1f, 50f, 15f, hairC);
                tri(c, 49f, 15f, 53f, 2f, 57f, 15f, hairC);
                tri(c, 56f, 15f, 61f, 4f, 65f, 15f, hairC);
            } else if (hid == 42) {
                rect(c, 33f, 13f, 39f, 40f, 2f, hairC);
                rect(c, 61f, 13f, 67f, 40f, 2f, hairC);
            } else if (hid == 43) {
                path.reset();
                path.moveTo(fx(33f), fy(13f));
                path.lineTo(fx(67f), fy(13f));
                path.lineTo(fx(67f), fy(21f));
                path.lineTo(fx(53f), fy(27f));
                path.lineTo(fx(41f), fy(23f));
                path.lineTo(fx(33f), fy(31f));
                path.close();
                p.setStyle(Paint.Style.FILL);
                p.setColor(hairC);
                c.drawPath(path, p);
            } else {
                rect(c, 36f, 22f, 64f, 26f, 1.5f, hairC);
            }
        }

        private void hoodRects(Canvas c, int color, float g) {
            flat(c, 30f - g, 8f - g, 70f + g, 23f + g, 6f + g, color);
            flat(c, 30f - g, 14f - g, 36.5f + g, 44f + g, 3f + g, color);
            flat(c, 63.5f - g, 14f - g, 70f + g, 44f + g, 3f + g, color);
        }

        private void drawHood(Canvas c, int a) {
            int main = col(HOOD_MAIN[a]);
            int acc = col(HOOD_ACC[a]);
            int inner = col("#FFE0B2");
            if (a == 0) {
                ball(c, 33f, 10f, 5f, acc);
                ball(c, 67f, 10f, 5f, acc);
                ball(c, 28f, 22f, 4.5f, acc);
                ball(c, 28f, 32f, 4.5f, acc);
                ball(c, 72f, 22f, 4.5f, acc);
                ball(c, 72f, 32f, 4.5f, acc);
            } else if (a == 1) {
                ball(c, 32f, 10f, 5f, main);
                ball(c, 68f, 10f, 5f, main);
                dot(c, 32f, 10f, 2.3f, inner);
                dot(c, 68f, 10f, 2.3f, inner);
            } else if (a == 2) {
                ball(c, 32f, 9f, 5.5f, acc);
                ball(c, 68f, 9f, 5.5f, acc);
            } else if (a == 3) {
                ball(c, 29f, 13f, 7f, main);
                ball(c, 71f, 13f, 7f, main);
                dot(c, 29f, 13f, 4f, acc);
                dot(c, 71f, 13f, 4f, acc);
            } else if (a == 4) {
                ball(c, 37f, 8f, 5.5f, main);
                ball(c, 63f, 8f, 5.5f, main);
            } else if (a == 5 || a == 8) {
                tri(c, 31f, 15f, 33f, -1f, 43f, 9f, main);
                tri(c, 69f, 15f, 67f, -1f, 57f, 9f, main);
                tri(c, 34f, 12f, 35f, 3f, 40f, 9f, a == 5 ? inner : acc);
                tri(c, 66f, 12f, 65f, 3f, 60f, 9f, a == 5 ? inner : acc);
            } else if (a == 6) {
                rect(c, 35f, -4f, 43f, 12f, 4f, main);
                rect(c, 57f, -4f, 65f, 12f, 4f, main);
                flat(c, 37f, -1f, 41f, 10f, 2f, acc);
                flat(c, 59f, -1f, 63f, 10f, 2f, acc);
            } else if (a == 7) {
                rect(c, 23f, 14f, 33f, 38f, 4f, acc);
                rect(c, 67f, 14f, 77f, 38f, 4f, acc);
            } else if (a == 9) {
                tri(c, 33f, 12f, 35f, 3f, 41f, 9f, main);
                tri(c, 67f, 12f, 65f, 3f, 59f, 9f, main);
                tri(c, 46f, 9f, 50f, -5f, 54f, 9f, col("#FFD54F"));
            } else if (a == 11) {
                tri(c, 31f, 15f, 32f, 1f, 42f, 9f, col("#6D4C41"));
                tri(c, 69f, 15f, 68f, 1f, 58f, 9f, col("#6D4C41"));
            }

            hoodRects(c, 0x55000000, 0.5f);
            hoodRects(c, main, 0f);

            if (a == 1 || a == 8) {
                int sc = a == 1 ? acc : col("#C77B30");
                flat(c, 44f, 9f, 46f, 15f, 0.8f, sc);
                flat(c, 49f, 9f, 51f, 16f, 0.8f, sc);
                flat(c, 54f, 9f, 56f, 15f, 0.8f, sc);
                if (a == 1) {
                    flat(c, 30.5f, 26f, 36f, 28f, 0.6f, acc);
                    flat(c, 30.5f, 33f, 36f, 35f, 0.6f, acc);
                    flat(c, 64f, 26f, 69.5f, 28f, 0.6f, acc);
                    flat(c, 64f, 33f, 69.5f, 35f, 0.6f, acc);
                }
            } else if (a == 2) {
                dot(c, 33.2f, 30f, 2.6f, acc);
                dot(c, 66.8f, 30f, 2.6f, acc);
            } else if (a == 3) {
                flat(c, 46f, 10f, 54f, 15f, 2.5f, col("#455A64"));
            } else if (a == 4) {
                dot(c, 37f, 8f, 3.8f, Color.WHITE);
                dot(c, 63f, 8f, 3.8f, Color.WHITE);
                dot(c, 37.5f, 8.3f, 1.8f, Color.BLACK);
                dot(c, 62.5f, 8.3f, 1.8f, Color.BLACK);
            } else if (a == 5) {
                tri(c, 30f, 44f, 30f, 34f, 36.5f, 44f, Color.WHITE);
                tri(c, 70f, 44f, 70f, 34f, 63.5f, 44f, Color.WHITE);
            } else if (a == 9) {
                rect(c, 65.5f, 10f, 72f, 38f, 3f, acc);
            } else if (a == 10) {
                tri(c, 46f, 10f, 54f, 10f, 50f, 16f, col("#FFB300"));
                flat(c, 31f, 27f, 36.2f, 41f, 2f, Color.WHITE);
                flat(c, 63.8f, 27f, 69f, 41f, 2f, Color.WHITE);
            } else if (a == 11) {
                dot(c, 42.5f, 14f, 4.8f, Color.WHITE);
                dot(c, 57.5f, 14f, 4.8f, Color.WHITE);
                dot(c, 42.5f, 14f, 2.2f, col("#5D4037"));
                dot(c, 57.5f, 14f, 2.2f, col("#5D4037"));
                tri(c, 48.5f, 17f, 51.5f, 17f, 50f, 21f, col("#FFB300"));
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
        AvatarView av3 = new AvatarView(this, a >= NO_HOOD ? -1 : a, items, mode, true);
        av3.gender = genderOf[pl];
        return av3;
    }

    private AvatarView previewView(int animalIdx, int slot, int id, int mode) {
        int[] items = {-1, -1, -1, -1, -1, -1, -1};
        if (slot >= 0) {
            items[slot] = id;
        }
        return new AvatarView(this, animalIdx, items, mode, false);
    }

    private View shopCell(final int id) {
        final boolean own = owns(id);
        final boolean on = eq(shopPl, itSlot(id)) == id;
        int rar = itRar(id);
        int rc = RAR_COLOR[rar];
        LinearLayout cell = new LinearLayout(this);
        cell.setOrientation(LinearLayout.VERTICAL);
        cell.setGravity(Gravity.CENTER_HORIZONTAL);
        cell.setPadding(dp(4), dp(10), dp(4), dp(8));
        GradientDrawable bg = box(Color.WHITE, 18);
        bg.setStroke(dp(on ? 4 : 2), on ? GREEN : rc);
        cell.setBackground(bg);
        cell.setElevation(dp(3));
        if (itSlot(id) == 4) {
            View sw = new View(this);
            sw.setBackground(bgDrawable(id, dp(12)));
            cell.addView(sw, new LinearLayout.LayoutParams(dp(40), dp(40)));
        } else if (itSlot(id) == 3) {
            cell.addView(text(itIcon(id), 30, DARK, false), new LinearLayout.LayoutParams(WRAP, WRAP));
        } else {
            int sl = itSlot(id);
            AvatarView pvw = previewView(-1, sl, id, (sl == 2 || sl == 5) ? 2 : 1); pvw.gender = itGender(id) < 0 ? genderOf[shopPl] : itGender(id);
            pvw.setBackground(box(Color.parseColor("#ECEFF1"), 12));
            cell.addView(pvw, new LinearLayout.LayoutParams(dp(56), dp(56)));
        }
        cell.addView(text(itName(id), 11, DARK, true), lp(WRAP, 4));
        cell.addView(text(RAR_NAME[rar], 10, rc, true), lp(WRAP, 0));
        TextView st;
        if (on) {
            st = text("\u2705 Dipakai", 12, Color.WHITE, true);
            st.setBackground(box(GREEN, 12));
        } else if (own) {
            st = text("Pakai", 12, Color.WHITE, true);
            st.setBackground(box(BLUE, 12));
        } else {
            st = text("\uD83D\uDCB0 " + itPrice(id), 12, Color.WHITE, true);
            st.setBackground(box(coins() >= itPrice(id) ? ORANGE : SILVER, 12));
        }
        st.setPadding(dp(8), dp(3), dp(8), dp(3));
        cell.addView(st, lpw(6));
        cell.setOnClickListener(v -> tapItem(id));
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
        screen = 20;
        nameFields = null;
        loadPrefs();
        if (shopPl >= np) {
            shopPl = 0;
        }

        LinearLayout c = column();
        c.addView(text("\uD83D\uDECD\uFE0F Toko Avatar", 28, ORANGE_DARK, true), lp(WRAP, 0));
        c.addView(pill("\uD83D\uDCB0 " + coins() + " koin", ORANGE), lpw(8));

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
            c.addView(pr, lp(dp(44), 12));
        }

        LinearLayout pv = card();
        pv.setGravity(Gravity.CENTER_HORIZONTAL);
        pv.addView(avatarView(shopPl), new LinearLayout.LayoutParams(dp(250), dp(250)));
        LinearLayout gr = new LinearLayout(this);
        gr.setOrientation(LinearLayout.HORIZONTAL);
        for (int g = 0; g < 2; g++) {
            final int gg = g;
            boolean gsel = genderOf[shopPl] == g;
            TextView gt = text(g == 0 ? "\uD83D\uDC66 Putra" : "\uD83D\uDC67 Putri", 14, gsel ? Color.WHITE : DARK, true);
            gt.setGravity(Gravity.CENTER);
            gt.setBackground(box(gsel ? (g == 0 ? BLUE : PINK) : GRAY, 16));
            gt.setOnClickListener(v -> {
                genderOf[shopPl] = gg;
                prefs.edit().putInt("gender" + shopPl, gg).apply();
                showToko();
            });
            gr.addView(gt, weight(g == 0 ? 0 : 4, 0));
        }
        pv.addView(gr, lp(dp(44), 8));
        pv.addView(text(names[shopPl], 16, DARK, true), lp(WRAP, 8));
        pv.addView(text("Ketuk barang untuk membeli, ketuk lagi untuk memakai atau melepas", 12, DARK, false),
                lp(WRAP, 2));
        c.addView(pv, lp(WRAP, 12));

        // Kategori ringkas: semua kategori terlihat tanpa geser horizontal.
        LinearLayout tabGrid = new LinearLayout(this);
        tabGrid.setOrientation(LinearLayout.VERTICAL);
        for (int start = 0; start < SHOP_TAB_NAME.length; start += 4) {
            LinearLayout tabRow = new LinearLayout(this);
            tabRow.setOrientation(LinearLayout.HORIZONTAL);
            for (int q = 0; q < 4; q++) {
                final int tab = start + q;
                if (tab >= SHOP_TAB_NAME.length) break;
                boolean sel = tab == shopTab;
                TextView chip = text(SHOP_TAB_ICON[tab] + "\n" + ((tab == 7 && genderOf[shopPl] == 1) ? "Jilbab" : SHOP_TAB_NAME[tab]), 12, sel ? Color.WHITE : DARK, true);
                chip.setGravity(Gravity.CENTER);
                chip.setMinHeight(dp(48));
                chip.setPadding(dp(3), dp(5), dp(3), dp(5));
                chip.setBackground(box(sel ? ORANGE : GRAY, 14));
                chip.setOnClickListener(v -> {
                    shopTab = tab;
                    showToko();
                });
                LinearLayout.LayoutParams cl = new LinearLayout.LayoutParams(0, WRAP, 1f);
                cl.setMargins(dp(3), dp(3), dp(3), dp(3));
                tabRow.addView(chip, cl);
            }
            tabGrid.addView(tabRow, lp(WRAP, 0));
        }
        c.addView(tabGrid, lp(WRAP, 12));

        if (shopTab == 0) {
            c.addView(text("Pilih tudung hewan (gratis)", 13, DARK, true), lp(WRAP, 12));
            for (int r = 0; r < 4; r++) {
                LinearLayout row = new LinearLayout(this);
                row.setOrientation(LinearLayout.HORIZONTAL);
                for (int q = 0; q < 4; q++) {
                    final int k = r * 4 + q;
                    if (k >= AVATARS.length) {
                        row.addView(new View(this), weight(q == 0 ? 0 : 4, q == 3 ? 0 : 4));
                        continue;
                    }
                    boolean on = avatar[shopPl] == k;
                    AvatarView t = previewView(k >= NO_HOOD ? -1 : k, -1, -1, 1); t.gender = genderOf[shopPl];
                    t.setBackground(on ? box(PLAYER_COLORS[shopPl], 16) : box(GRAY, 16));
                    t.setOnClickListener(v -> {
                        for (int j = 0; j < np; j++) {
                            if (k < NO_HOOD && j != shopPl && avatar[j] == k) {
                                avatar[j] = avatar[shopPl];
                                prefs.edit().putInt("avatar" + j, avatar[j]).apply();
                            }
                        }
                        avatar[shopPl] = k;
                        prefs.edit().putInt("avatar" + shopPl, k).apply();
                        showToko();
                    });
                    row.addView(t, weight(q == 0 ? 0 : 4, q == 3 ? 0 : 4));
                }
                c.addView(row, lp(dp(64), 8));
            }
            c.addView(text("Kotak terakhir: tanpa tudung, supaya rambutmu terlihat penuh", 12, DARK, false),
                    lp(WRAP, 6));
        } else {
            int slot = shopTab - 1;
            ArrayList<Integer> ids = new ArrayList<>();
            for (int i = 0; i < ITEMS.length; i++) {
                if (itSlot(i) == slot && itFits(i, genderOf[shopPl])) {
                    ids.add(i);
                }
            }
            for (int s = 0; s < ids.size(); s += 3) {
                LinearLayout row = new LinearLayout(this);
                row.setOrientation(LinearLayout.HORIZONTAL);
                for (int q = 0; q < 3; q++) {
                    if (s + q < ids.size()) {
                        row.addView(shopCell(ids.get(s + q)), weight(q == 0 ? 0 : 5, q == 2 ? 0 : 5));
                    } else {
                        row.addView(new View(this), weight(q == 0 ? 0 : 5, q == 2 ? 0 : 5));
                    }
                }
                c.addView(row, lp(WRAP, 10));
            }
            int have = 0;
            for (int i = 0; i < ITEMS.length; i++) {
                if (owns(i)) {
                    have++;
                }
            }
            c.addView(text("\uD83D\uDDC2\uFE0F Koleksi: " + have + "/" + ITEMS.length + " barang", 13, DARK, true),
                    lp(WRAP, 16));
        }

        Button back = button("Kembali", GRAY, 14);
        back.setTextColor(DARK);
        c.addView(back, lp(dp(46), 18));
        back.setOnClickListener(v -> showMenu());

        wrap(c);
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
        screen = 9;
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
        screen = 9;
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
        screen = 12;
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
        screen = 9;
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
        screen = 3;
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
        screen = 1;
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
        top.addView(pill(who, np > 1 ? PLAYER_COLORS[cur] : ORANGE),
                new LinearLayout.LayoutParams(WRAP, WRAP));
        top.addView(new View(this), new LinearLayout.LayoutParams(0, 1, 1f));
        tvHearts = text("", 18, DARK, false);
        top.addView(tvHearts, new LinearLayout.LayoutParams(WRAP, WRAP));

        // Penanda kemajuan soal
        LinearLayout dotsRow = new LinearLayout(this);
        dotsRow.setOrientation(LinearLayout.HORIZONTAL);
        c.addView(dotsRow, lp(dp(8), 16));
        for (int i = 0; i < total; i++) {
            View d = new View(this);
            d.setBackground(box(GRAY_D, 4));
            dotsRow.addView(d, weight(i == 0 ? 0 : 2, i == total - 1 ? 0 : 2));
            dots[i] = d;
        }

        // Nomor soal dan skor
        LinearLayout info = new LinearLayout(this);
        info.setOrientation(LinearLayout.HORIZONTAL);
        c.addView(info, lp(WRAP, 10));
        tvStep = text("", 15, DARK, true);
        tvStep.setGravity(Gravity.START);
        info.addView(tvStep, new LinearLayout.LayoutParams(0, WRAP, 1f));
        tvScore = text("", 15, ORANGE_DARK, true);
        tvScore.setGravity(Gravity.END);
        info.addView(tvScore, new LinearLayout.LayoutParams(0, WRAP, 1f));

        // Penghitung waktu
        tvTimer = text(" ", 15, DARK, true);
        c.addView(tvTimer, lp(WRAP, 10));
        timerBox = new FrameLayout(this);
        timerBox.setBackground(box(GRAY, 6));
        timerFill = new View(this);
        timerFill.setBackground(box(GREEN, 6));
        timerFill.setPivotX(0f);
        timerBox.addView(timerFill, new FrameLayout.LayoutParams(MATCH, MATCH));
        c.addView(timerBox, lp(dp(10), 6));
        if (!timed) {
            tvTimer.setVisibility(View.GONE);
            timerBox.setVisibility(View.GONE);
        }

        // Kartu soal
        cardQ = new LinearLayout(this);
        cardQ.setOrientation(LinearLayout.VERTICAL);
        cardQ.setGravity(Gravity.CENTER);
        cardQ.setBackground(box(Color.WHITE, 24));
        cardQ.setElevation(dp(6));
        cardQ.setPadding(dp(16), dp(28), dp(16), dp(28));
        tvQuestion = text("", 46, ORANGE_DARK, true);
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

        Button back = button(advLevel >= 0 ? "Kembali ke peta" : "Kembali ke menu", GRAY, 14);
        back.setTextColor(DARK);
        rt.addView(back, lp(dp(46), 22));
        back.setOnClickListener(v -> confirmExit());

        if (wide) {
            wrapTwo(c, cr);
        } else {
            wrap(c);
        }
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
            opts[i].setTextSize((q.sopts != null ? bankOptionSize(ot.length()) : optionSize(ot.length())) * ui);
            opts[i].setBackground(box(OPT_COLORS[i], 16));
            opts[i].setAlpha(1f);
            opts[i].setOnClickListener(v -> choose(idx));
        }
        correctBtn = opts[q.correctIdx];
        dots[qi - 1].setBackground(box(ORANGE, 4));

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
        tvTimer.setTextColor(timeLeft <= 3 ? RED : DARK);
        timerFill.setBackground(box(timeLeft <= 3 ? RED : GREEN, 6));
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
            opts[idx].setBackground(box(GREEN, 16));
            dots[qi - 1].setBackground(box(GREEN, 4));
            tvFeedback.setText("Benar! \uD83C\uDF89");
            tvFeedback.setTextColor(GREEN);
            sfx(SFX_CORRECT);
            buzz(25);
            pop(opts[idx], 1.12f);
            pop(tvFeedback, 1.25f);
            pop(tvScore, 1.45f);
        } else {
            lives--;
            opts[idx].setBackground(box(RED, 16));
            correctBtn.setBackground(box(GREEN, 16));
            dots[qi - 1].setBackground(box(RED, 4));
            tvFeedback.setText("Oops! Jawabannya " + optText(q, q.correctIdx));
            tvFeedback.setTextColor(RED);
            sfx(SFX_WRONG);
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
        correctBtn.setBackground(box(GREEN, 16));
        dots[qi - 1].setBackground(box(RED, 4));
        tvFeedback.setText("Jawabannya " + optText(q, q.correctIdx));
        tvFeedback.setTextColor(RED);
        sfx(SFX_TIMEOUT);
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
        screen = 2;
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
        screen = 2;
        boolean rekor = rekorNow;
        int stars = soloStars();

        LinearLayout c = column();
        c.setGravity(Gravity.CENTER);
        c.addView(text(av(0), 48, Color.WHITE, false), lp(WRAP, 0));
        TextView title = text(lives <= 0 ? "Nyawa habis!" : "Selesai!", 32, ORANGE_DARK, true);
        c.addView(title, lp(WRAP, 4));
        c.addView(pill(KELAS_NAMES[kelas] + " \u2022 " + modeLabel(), KELAS_COLOR[kelas]), lpw(10));

        LinearLayout card = card();
        card.setGravity(Gravity.CENTER_HORIZONTAL);
        LinearLayout starRow = new LinearLayout(this);
        starRow.setOrientation(LinearLayout.HORIZONTAL);
        starRow.setGravity(Gravity.CENTER);
        TextView[] st = new TextView[3];
        for (int i = 0; i < 3; i++) {
            st[i] = text(i < stars ? "\u2B50" : "\u2606", 44, ORANGE, false);
            starRow.addView(st[i], new LinearLayout.LayoutParams(WRAP, WRAP));
        }
        card.addView(starRow, lp(WRAP, 0));
        TextView sc = text("Skor: " + pScore[0], 30, DARK, true);
        card.addView(sc, lp(WRAP, 10));
        TextView ben = text("Benar " + pCorrect[0] + " dari " + total, 16, DARK, false);
        card.addView(ben, lp(WRAP, 6));
        card.addView(text("\uD83C\uDF1F Total bintang: " + totalStars(), 14, DARK, false), lp(WRAP, 6));
        if (!countsForRecord()) {
            card.addView(text("Mode santai: skor tidak masuk rekor", 12, DARK, false), lp(WRAP, 4));
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
        screen = 2;
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
        c.addView(text("Hasil Akhir", 32, ORANGE_DARK, true), lp(WRAP, 0));
        c.addView(pill(KELAS_NAMES[kelas] + " \u2022 " + modeLabel(), KELAS_COLOR[kelas]), lpw(10));

        TextView banner = text(winners > 1 ? "Seri! Skor sama \uD83E\uDD1D"
                : "\uD83C\uDFC6 " + names[order[0]] + " menang!", 22, Color.WHITE, true);
        banner.setBackground(box(winners > 1 ? PURPLE : GOLD, 20));
        banner.setPadding(dp(16), dp(14), dp(16), dp(14));
        banner.setElevation(dp(4));
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
            row.setBackground(box(Color.WHITE, 18));
            row.setElevation(dp(3));
            row.setPadding(dp(12), dp(10), dp(14), dp(10));

            TextView rank = text(String.valueOf(r + 1), 18, Color.WHITE, true);
            rank.setBackground(box(rankColor[r], 18));
            row.addView(rank, new LinearLayout.LayoutParams(dp(36), dp(36)));

            TextView nm = text(av(p) + " " + names[p], 18, DARK, true);
            nm.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
            LinearLayout.LayoutParams nmp = new LinearLayout.LayoutParams(0, WRAP, 1f);
            nmp.leftMargin = dp(12);
            row.addView(nm, nmp);

            TextView sc = text(pScore[p] + " poin\nbenar " + pCorrect[p] + "/" + total,
                    14, PURPLE, true);
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
        screen = 5;
        int p = reviewPlayer;

        LinearLayout c = column();
        c.setGravity(Gravity.CENTER_HORIZONTAL);
        c.addView(text("Pembahasan", 30, ORANGE_DARK, true), lp(WRAP, 0));
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
            c.addView(text("Ini soal yang masih salah. Yuk, lihat jawaban yang benar.", 14, DARK, false),
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
            rev.setElevation(dp(4));
            c.addView(rev, lp(dp(60), 22));
            rev.setOnClickListener(v -> {
                reviewPlayer = 0;
                showReview();
            });
        }
        Button again = button("Main Lagi", GREEN, 22);
        again.setElevation(dp(4));
        c.addView(again, lp(dp(64), mistakes ? 12 : 26));
        again.setOnClickListener(v -> startGame(mode));

        Button menu = button("Menu Utama", ORANGE, 20);
        menu.setElevation(dp(4));
        c.addView(menu, lp(dp(60), 12));
        menu.setOnClickListener(v -> showMenu());
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
        screen = 10;
        advLevel = -1;
        nameFields = null;
        final int k = kelas;
        final int n = LEVELS[k].length;
        final int done = advDone(k);

        LinearLayout c = column();
        c.addView(text("\uD83D\uDDFA\uFE0F Peta Petualangan", 24, ORANGE_DARK, true), lp(WRAP, 0));
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
        GradientDrawable hg = new GradientDrawable(GradientDrawable.Orientation.TL_BR,
                new int[]{Color.parseColor(WORLD_PAL[k]), Color.parseColor(WORLD_PAL2[k])});
        hg.setCornerRadius(dp(28));
        hero.setBackground(hg);
        hero.setElevation(dp(5));
        hero.addView(text(WORLD_ICON[k] + " " + WORLD_NAME[k], 22, Color.WHITE, true), lp(WRAP, 0));
        hero.addView(text(KELAS_NAMES[k] + " \u2022 " + done + " dari " + n + " level \u2022 \u2B50 "
                + advStarSum(k) + "/" + (3 * n), 13, Color.WHITE, false), lp(WRAP, 4));
        LinearLayout bar = new LinearLayout(this);
        bar.setOrientation(LinearLayout.HORIZONTAL);
        bar.setBackground(box(Color.argb(90, 255, 255, 255), 6));
        if (done > 0) {
            View fill = new View(this);
            fill.setBackground(box(Color.WHITE, 6));
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
            go.setElevation(dp(4));
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

        wrap(c);
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
        startGame(7);
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
        screen = 2;
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
        screen = 2;
        final int stars = advStarsFor(pCorrect[0], total);
        boolean passed = stars >= 1;
        String[] p = lv(advK, advLevel);

        LinearLayout c = column();
        c.setGravity(Gravity.CENTER);
        c.addView(text(BUDDY_ICON[advK], 48, Color.WHITE, false), lp(WRAP, 0));
        TextView title = text(passed ? "Level selesai!" : "Ayo coba lagi!", 30, ORANGE_DARK, true);
        c.addView(title, lp(WRAP, 4));
        c.addView(pill("Level " + (advLevel + 1) + " \u2022 " + p[1], KELAS_COLOR[advK]), lpw(10));

        LinearLayout card = card();
        card.setGravity(Gravity.CENTER_HORIZONTAL);
        LinearLayout starRow = new LinearLayout(this);
        starRow.setOrientation(LinearLayout.HORIZONTAL);
        starRow.setGravity(Gravity.CENTER);
        TextView[] st = new TextView[3];
        for (int i = 0; i < 3; i++) {
            st[i] = text(i < stars ? "\u2B50" : "\u2606", 44, ORANGE, false);
            starRow.addView(st[i], new LinearLayout.LayoutParams(WRAP, WRAP));
        }
        card.addView(starRow, lp(WRAP, 0));
        TextView sc = text("Skor: " + pScore[0], 24, DARK, true);
        card.addView(sc, lp(WRAP, 10));
        TextView ben = text("Benar " + pCorrect[0] + " dari " + total, 16, DARK, false);
        card.addView(ben, lp(WRAP, 6));
        String hint = !passed ? "Butuh benar separuh atau lebih untuk membuka level berikutnya."
                : stars < 3 ? "Benar 90% ke atas dapat 3 bintang." : "Sempurna! Tiga bintang di level ini.";
        card.addView(text(hint, 13, DARK, false), lp(WRAP, 6));
        card.addView(text("\uD83C\uDF1F Total bintang: " + totalStars(), 14, DARK, false), lp(WRAP, 6));
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
            nx.setElevation(dp(4));
            c.addView(nx, lp(dp(64), 18));
            nx.setOnClickListener(v -> startLevel(advLevel + 1));
        }
        Button again = button("Ulangi level", hasNext ? BLUE : GREEN, hasNext ? 18 : 22);
        again.setElevation(dp(4));
        c.addView(again, lp(dp(hasNext ? 56 : 64), hasNext ? 12 : 18));
        again.setOnClickListener(v -> startLevel(advLevel));

        Button map = button("\uD83D\uDDFA\uFE0F Kembali ke Peta", ORANGE, 20);
        map.setElevation(dp(4));
        c.addView(map, lp(dp(60), 12));
        map.setOnClickListener(v -> showPeta());
    }
}
