package com.ibo.pixelhome;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.view.View;
import android.view.ViewGroup;

/**
 * Shown once when the TV starts: "PIXEL HOME" appears pixel by pixel in a dither pattern,
 * stays for a moment, then the whole thing fades away to reveal the home screen.
 */
class SplashView extends View {
    static final int W = 160, H = 90;
    static final long REVEAL = 1100, HOLD = 700, FADE = 500;
    static final int[] BAYER = {0, 8, 2, 10, 12, 4, 14, 6, 3, 11, 1, 9, 15, 7, 13, 5};

    final Bitmap art = Bitmap.createBitmap(W, H, Bitmap.Config.ARGB_8888);
    final Bitmap shown = Bitmap.createBitmap(W, H, Bitmap.Config.ARGB_8888);
    final int[] src = new int[W * H], out = new int[W * H];
    final Paint p = new Paint();
    final long start = System.currentTimeMillis();
    boolean fading;

    SplashView(Context c) {
        super(c);
        p.setFilterBitmap(false);
        p.setAntiAlias(false);
        setBackgroundColor(PixelActivity.INK);
        Canvas a = new Canvas(art);
        Paint t = new Paint();
        t.setAntiAlias(false);
        t.setTypeface(c.getResources().getFont(R.font.press_start_2p));
        t.setTextSize(8);
        t.setTextAlign(Paint.Align.CENTER);
        t.setColor(PixelActivity.INK);
        a.drawText("PIXEL HOME", W / 2f + 1, H / 2f + 5, t);           // hard shadow
        t.setColor(SkyView.accentNow());
        a.drawText("PIXEL HOME", W / 2f, H / 2f + 4, t);
        // a small row of pixel "stars" under the title
        Paint s = new Paint();
        s.setColor(0xFFFFFFFF);
        for (int i = 0; i < 5; i++) a.drawRect(W / 2f - 12 + i * 6, H / 2f + 12, W / 2f - 11 + i * 6, H / 2f + 13, s);
        art.getPixels(src, 0, W, 0, 0, W, H);
    }

    @Override
    protected void onDraw(Canvas c) {
        long t = System.currentTimeMillis() - start;
        float f = Math.min(1f, t / (float) REVEAL);
        for (int y = 0; y < H; y++)
            for (int x = 0; x < W; x++) {
                int i = y * W + x;
                out[i] = BAYER[(y & 3) * 4 + (x & 3)] / 16f < f ? src[i] : 0;
            }
        shown.setPixels(out, 0, W, 0, 0, W, H);
        float scale = Math.max(getWidth() / (float) W, getHeight() / (float) H);
        float ox = (getWidth() - W * scale) / 2f, oy = (getHeight() - H * scale) / 2f;
        c.drawBitmap(shown, new Rect(0, 0, W, H), new RectF(ox, oy, ox + W * scale, oy + H * scale), p);

        if (t < REVEAL + HOLD) {
            postInvalidateOnAnimation();
        } else if (!fading) {
            fading = true;
            animate().alpha(0f).setDuration(FADE).withEndAction(() -> {
                if (getParent() instanceof ViewGroup) ((ViewGroup) getParent()).removeView(this);
            }).start();
        }
    }
}
