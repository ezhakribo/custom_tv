package com.ibo.pixelhome;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Looper;
import android.util.TypedValue;
import android.view.View;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 20 panoramic pixel-art scenes along the bottom of the screen.
 * Random order (no repeats until all 20 have shown), a new scene every 10 seconds,
 * a pixel "dissolve" between scenes, a slow sideways pan, and the scene's title fading in.
 * The walking character strolls in front of it.
 */
public class SlideshowView extends View {
    static final int[] SLIDES = {R.drawable.slide00, R.drawable.slide01, R.drawable.slide02, R.drawable.slide03,
            R.drawable.slide04, R.drawable.slide05, R.drawable.slide06, R.drawable.slide07, R.drawable.slide08,
            R.drawable.slide09, R.drawable.slide10, R.drawable.slide11, R.drawable.slide12, R.drawable.slide13,
            R.drawable.slide14, R.drawable.slide15, R.drawable.slide16, R.drawable.slide17, R.drawable.slide18,
            R.drawable.slide19};
    static final int SW = 384, SH = 40;
    static final long SHOW_MS = 10_000, FADE_MS = 1_200;
    static final int[] BAYER = {0, 8, 2, 10, 12, 4, 14, 6, 3, 11, 1, 9, 15, 7, 13, 5};

    final Handler handler = new Handler(Looper.getMainLooper());
    final Paint bmp = new Paint(), ink = new Paint(), text = new Paint();
    final List<String> names = new ArrayList<>();
    final List<Integer> bag = new ArrayList<>();
    final Bitmap mixed = Bitmap.createBitmap(SW, SH, Bitmap.Config.ARGB_8888);
    final int[] pa = new int[SW * SH], pb = new int[SW * SH], pm = new int[SW * SH];
    final Walker walker = new Walker(getResources());
    boolean showWalker = true, running;
    Bitmap cur, next;
    int curIdx = -1, nextIdx = -1;
    long shownAt, fadeStart = -1;
    boolean panRight = true;

    public SlideshowView(Context c) {
        super(c);
        bmp.setFilterBitmap(false);
        bmp.setAntiAlias(false);
        ink.setAntiAlias(false);
        Typeface tf = c.getResources().getFont(R.font.press_start_2p);
        text.setTypeface(tf);
        text.setAntiAlias(false);
        text.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 8, c.getResources().getDisplayMetrics()));
        try (BufferedReader r = new BufferedReader(new InputStreamReader(c.getResources().openRawResource(R.raw.slide_names)))) {
            for (String line; (line = r.readLine()) != null; ) names.add(line.trim());
        } catch (Exception ignored) { }
        curIdx = take();
        cur = load(curIdx);
        shownAt = System.currentTimeMillis();
    }

    Bitmap load(int i) {
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inScaled = false;
        o.inPreferredConfig = Bitmap.Config.ARGB_8888;
        return BitmapFactory.decodeResource(getResources(), SLIDES[i], o);
    }

    /** Next slide from a shuffled bag: every scene shows once before any repeats. */
    int take() {
        if (bag.isEmpty()) {
            for (int i = 0; i < SLIDES.length; i++) if (i != curIdx) bag.add(i);
            Collections.shuffle(bag);
        }
        return bag.remove(0);
    }

    final Runnable frame = new Runnable() {
        @Override public void run() {
            long now = System.currentTimeMillis();
            if (fadeStart < 0 && now - shownAt >= SHOW_MS) {
                nextIdx = take();
                next = load(nextIdx);
                fadeStart = now;
            }
            if (fadeStart >= 0 && now - fadeStart >= FADE_MS) {
                cur = next;
                curIdx = nextIdx;
                next = null;
                fadeStart = -1;
                shownAt = now;
                panRight = !panRight;
            }
            if (showWalker && getWidth() > 0) walker.update(getWidth(), walkerPx());
            invalidate();
            if (running) handler.postDelayed(this, 125);
        }
    };

    public void start() {
        if (running) return;
        running = true;
        shownAt = System.currentTimeMillis();
        handler.post(frame);
    }

    public void stop() {
        running = false;
        handler.removeCallbacks(frame);
    }

    float walkerPx() {
        View root = getRootView();
        float sc = Math.max(root.getWidth() / 160f, root.getHeight() / 90f);
        return Math.max(2, Math.round(sc * 0.5f));
    }

    @Override
    protected void onDraw(Canvas c) {
        int w = getWidth(), h = getHeight();
        if (w == 0 || h == 0 || cur == null) return;
        long now = System.currentTimeMillis();
        float scale = h / (float) SH;
        float visible = Math.min(SW, w / scale);              // scene pixels that fit across the screen
        float range = SW - visible;
        float t = Math.min(1f, (now - shownAt) / (float) (SHOW_MS + FADE_MS));
        int pan = Math.round((panRight ? t : 1 - t) * range);  // whole pixels: a slow, steppy pixel-art pan

        Bitmap show = cur;
        if (fadeStart >= 0 && next != null) {
            float f = (now - fadeStart) / (float) FADE_MS;      // 0..1 dissolve, pixel by pixel in a dither pattern
            cur.getPixels(pa, 0, SW, 0, 0, SW, SH);
            next.getPixels(pb, 0, SW, 0, 0, SW, SH);
            for (int y = 0; y < SH; y++)
                for (int x = 0; x < SW; x++) {
                    int i = y * SW + x;
                    pm[i] = BAYER[(y & 3) * 4 + (x & 3)] / 16f < f ? pb[i] : pa[i];
                }
            mixed.setPixels(pm, 0, SW, 0, 0, SW, SH);
            show = mixed;
        }
        Rect src = new Rect(pan, 0, pan + Math.round(visible), SH);
        RectF dst = new RectF(0, 0, Math.round(visible) * scale, h);
        if (dst.right < w) dst.offset((w - dst.right) / 2f, 0);
        c.drawBitmap(show, src, dst, bmp);

        // a crisp dark edge on top so the strip sits like a frame
        ink.setColor(PixelActivity.INK);
        c.drawRect(0, 0, w, Math.max(2, scale * 0.75f), ink);

        // title of the scene: fades in after the change, then fades away
        long since = now - shownAt;
        if (fadeStart < 0 && curIdx < names.size() && since < 5_000) {
            float a = since < 600 ? since / 600f : since > 4_000 ? (5_000 - since) / 1_000f : 1f;
            String title = names.get(curIdx);
            float pad = scale * 2, tw = text.measureText(title), th = -text.ascent() + text.descent();
            ink.setColor(Color.argb(Math.round(170 * a), 26, 20, 48));
            c.drawRect(pad, pad + scale, pad + tw + pad * 2, pad + scale + th + pad * 1.5f, ink);
            text.setColor(Color.argb(Math.round(255 * a), 255, 216, 74));
            c.drawText(title, pad * 2, pad + scale + pad * 0.75f - text.ascent(), text);
        }

        if (showWalker) {
            float wp = walkerPx();
            walker.draw(c, wp, h - wp * 2);
        }
    }
}
