package com.ibo.pixelhome;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Calendar;
import java.util.Random;

/**
 * Pixel-art sky that follows the clock.
 * 8 sessions (every 3 hours). Each session looks "pure" in the middle of its 3 hours and
 * blends continuously into the next one, so the change is never visible as a jump.
 * The sun and moon move along the sky with the real time; clouds drift; stars twinkle;
 * city windows switch on and off.
 */
public class SkyView extends View {
    static final int W = 160, H = 90, HORIZON = 62;
    static final int[] SKY = {R.drawable.sky0, R.drawable.sky1, R.drawable.sky2, R.drawable.sky3,
            R.drawable.sky4, R.drawable.sky5, R.drawable.sky6, R.drawable.sky7};
    static final int[] LAND = {R.drawable.land0, R.drawable.land1, R.drawable.land2, R.drawable.land3,
            R.drawable.land4, R.drawable.land5, R.drawable.land6, R.drawable.land7};
    //                              00-03 03-06 06-09 09-12 12-15 15-18 18-21 21-24
    static final float[] NIGHT  = {1.0f, 0.6f, 0.0f, 0.0f, 0.0f, 0.0f, 0.6f, 0.9f};
    static final float[] DAY    = {0.0f, 0.05f, 0.8f, 1.0f, 1.0f, 0.8f, 0.2f, 0.0f};
    static final float[] LIGHTS = {0.5f, 0.35f, 0.08f, 0.02f, 0.02f, 0.12f, 0.9f, 0.8f};

    /** Speeds a whole day through in 48 seconds, to preview the transitions. */
    static boolean demo;
    static long demoStart;

    final Bitmap[] sky = new Bitmap[8], land = new Bitmap[8];
    final Paint bmp = new Paint(), px = new Paint();
    final Handler handler = new Handler(Looper.getMainLooper());
    final Random rnd = new Random(7);
    boolean running;

    int[] winX = new int[0], winY = new int[0];
    boolean[] lit = new boolean[0];
    final float[][] stars = new float[34][4];   // x, y, phase, speed
    final float[][] clouds = {{10, 12, 0.55f, 0}, {70, 22, 0.35f, 1}, {120, 8, 0.45f, 2}, {40, 32, 0.25f, 1}};
    float meteorX = -1, meteorY;

    static final String[][] CLOUD = {
            {"....###.....", "..#######...", ".##########.", "############"},
            {"...####...", ".########.", "##########"},
            {"..#####......", ".#########....", "#############."}};

    // what onDraw uses (updated every frame)
    int a, b;
    float blend, night, day, lights, hours;
    float scale, ox, oy;
    long last;

    public SkyView(Context c) {
        super(c);
        bmp.setFilterBitmap(false);
        bmp.setAntiAlias(false);
        px.setAntiAlias(false);
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inScaled = false;
        for (int i = 0; i < 8; i++) {
            sky[i] = BitmapFactory.decodeResource(getResources(), SKY[i], o);
            land[i] = BitmapFactory.decodeResource(getResources(), LAND[i], o);
        }
        loadWindows();
        for (float[] s : stars) {
            s[0] = rnd.nextInt(W); s[1] = rnd.nextInt(52); s[2] = rnd.nextFloat() * 6.28f; s[3] = 1 + rnd.nextFloat() * 2;
        }
        update(System.currentTimeMillis());
    }

    void loadWindows() {
        try (BufferedReader r = new BufferedReader(new InputStreamReader(getResources().openRawResource(R.raw.windows)))) {
            String[] parts = r.readLine().split(";");
            winX = new int[parts.length];
            winY = new int[parts.length];
            lit = new boolean[parts.length];
            for (int i = 0; i < parts.length; i++) {
                String[] xy = parts[i].split(",");
                winX[i] = Integer.parseInt(xy[0]);
                winY[i] = Integer.parseInt(xy[1]);
                lit[i] = rnd.nextFloat() < 0.5f;
            }
        } catch (Exception ignored) { }
    }

    // ---------- time ----------
    static float minutesNow() {
        if (demo) {
            float m = (System.currentTimeMillis() - demoStart) / 1000f * 30f;   // 1440 min in 48 s
            return m % 1440f;
        }
        Calendar c = Calendar.getInstance();
        return c.get(Calendar.HOUR_OF_DAY) * 60 + c.get(Calendar.MINUTE) + c.get(Calendar.SECOND) / 60f;
    }

    /** Which two sessions to mix and how much (0..1), smoothstep-eased. */
    static float[] mix(float minutes) {
        float pos = (minutes - 90f) / 180f;          // session i is pure at i*3h + 1h30
        if (pos < 0) pos += 8;
        int i = (int) Math.floor(pos) % 8;
        float t = pos - (float) Math.floor(pos);
        t = t * t * (3 - 2 * t);
        return new float[]{i, (i + 1) % 8, t};
    }

    void update(long now) {
        float m = minutesNow();
        float[] mx = mix(m);
        a = (int) mx[0]; b = (int) mx[1]; blend = mx[2];
        night = lerp(NIGHT[a], NIGHT[b], blend);
        day = lerp(DAY[a], DAY[b], blend);
        lights = lerp(LIGHTS[a], LIGHTS[b], blend);
        hours = m / 60f;
        float dt = last == 0 ? 0 : Math.min(1f, (now - last) / 1000f);
        last = now;
        for (float[] c : clouds) {
            c[0] += c[2] * dt * (demo ? 6 : 1);
            if (c[0] > W + 4) c[0] = -16;
        }
        if (winX.length > 0 && rnd.nextFloat() < 0.6f) {           // a few windows change each frame
            int k = rnd.nextInt(winX.length);
            lit[k] = rnd.nextFloat() < lights;
        }
        if (meteorX < 0 && night > 0.7f && rnd.nextFloat() < 0.004f) {
            meteorX = 20 + rnd.nextInt(100); meteorY = 4 + rnd.nextInt(14);
        } else if (meteorX >= 0) {
            meteorX += 3; meteorY += 1.2f;
            if (meteorY > 40) meteorX = -1;
        }
    }

    static float lerp(float x, float y, float t) { return x + (y - x) * t; }

    // ---------- animation loop (8 frames a second, only while visible) ----------
    final Runnable frame = new Runnable() {
        @Override public void run() {
            update(System.currentTimeMillis());
            invalidate();
            if (running) handler.postDelayed(this, 125);
        }
    };

    public void start() {
        if (running) return;
        running = true;
        last = 0;
        handler.post(frame);
    }

    public void stop() {
        running = false;
        handler.removeCallbacks(frame);
    }

    // ---------- drawing ----------
    void dot(Canvas c, float x, float y, int color) {
        px.setColor(color);
        float l = ox + (float) Math.floor(x) * scale, t = oy + (float) Math.floor(y) * scale;
        c.drawRect(l, t, l + scale, t + scale, px);
    }

    @Override
    protected void onDraw(Canvas c) {
        int w = getWidth(), h = getHeight();
        scale = Math.max(w / (float) W, h / (float) H);
        ox = (w - W * scale) / 2f;
        oy = (h - H * scale) / 2f;
        RectF dst = new RectF(ox, oy, ox + W * scale, oy + H * scale);
        Rect src = new Rect(0, 0, W, H);

        // sky: blend the two neighbouring sessions
        bmp.setAlpha(255);
        c.drawBitmap(sky[a], src, dst, bmp);
        bmp.setAlpha(Math.round(blend * 255));
        c.drawBitmap(sky[b], src, dst, bmp);

        // twinkling stars + shooting star
        if (night > 0.02f) {
            float time = System.currentTimeMillis() / 1000f;
            for (float[] s : stars) {
                float tw = 0.35f + 0.65f * (0.5f + 0.5f * (float) Math.sin(s[2] + time * s[3]));
                dot(c, s[0], s[1], Color.argb(Math.round(255 * night * tw), 255, 255, 255));
            }
            if (meteorX >= 0) {
                for (int k = 0; k < 6; k++) {
                    dot(c, meteorX - k * 2.5f, meteorY - k, Color.argb(Math.round(255 * night * (1 - k / 6f)), 255, 250, 220));
                }
            }
        }

        drawSun(c);
        drawMoon(c);

        // clouds (only by day)
        if (day > 0.02f) {
            int alpha = Math.round(220 * day);
            for (float[] cl : clouds) {
                String[] shape = CLOUD[(int) cl[3]];
                for (int r = 0; r < shape.length; r++)
                    for (int k = 0; k < shape[r].length(); k++)
                        if (shape[r].charAt(k) == '#')
                            dot(c, cl[0] + k, cl[1] + r, Color.argb(alpha, 255, 255, 255));
            }
        }

        // mountains + city, blended the same way
        bmp.setAlpha(255);
        c.drawBitmap(land[a], src, dst, bmp);
        bmp.setAlpha(Math.round(blend * 255));
        c.drawBitmap(land[b], src, dst, bmp);
        bmp.setAlpha(255);

        // lit windows
        int wa = Math.round(255 * Math.min(1f, lights * 1.6f));
        if (wa > 4) {
            int col = Color.argb(wa, 255, 216, 74);
            for (int i = 0; i < winX.length; i++) if (lit[i]) dot(c, winX[i], winY[i], col);
        }
    }

    void drawSun(Canvas c) {
        float t = (hours - 6f) / 12f;                       // 06:00 -> 0, 18:00 -> 1
        if (t < -0.05f || t > 1.05f) return;
        float x = 8 + t * 144, y = HORIZON + 2 - (float) Math.sin(Math.PI * Math.max(0, Math.min(1, t))) * 50;
        float high = (float) Math.sin(Math.PI * Math.max(0, Math.min(1, t)));
        int core = blendColor(Color.rgb(255, 150, 60), Color.rgb(255, 244, 170), Math.min(1, high * 1.6f));
        for (int dy = -6; dy <= 6; dy++)
            for (int dx = -6; dx <= 6; dx++) {
                float d = dx * dx + dy * dy;
                if (d <= 16) dot(c, x + dx, y + dy, core);
                else if (d <= 30) dot(c, x + dx, y + dy, Color.argb(90, Color.red(core), Color.green(core), Color.blue(core)));
            }
    }

    void drawMoon(Canvas c) {
        float t = ((hours - 18f + 24f) % 24f) / 12f;        // 18:00 -> 0, 06:00 -> 1
        if (t > 1.05f) return;
        float x = 8 + t * 144, y = HORIZON + 2 - (float) Math.sin(Math.PI * Math.min(1, t)) * 48;
        int col = Color.rgb(246, 239, 196);
        for (int dy = -5; dy <= 5; dy++)
            for (int dx = -5; dx <= 5; dx++) {
                boolean in = dx * dx + dy * dy <= 22;
                boolean cut = (dx - 3) * (dx - 3) + (dy + 1) * (dy + 1) <= 18;
                if (in && !cut) dot(c, x + dx, y + dy, col);
            }
    }

    static int blendColor(int c1, int c2, float t) {
        return Color.rgb(Math.round(lerp(Color.red(c1), Color.red(c2), t)),
                Math.round(lerp(Color.green(c1), Color.green(c2), t)),
                Math.round(lerp(Color.blue(c1), Color.blue(c2), t)));
    }
}
