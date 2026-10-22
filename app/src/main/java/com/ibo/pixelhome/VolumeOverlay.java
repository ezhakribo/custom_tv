package com.ibo.pixelhome;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;

/** A pixel-art volume bar that appears at the top of the screen for 2 seconds. */
class VolumeOverlay {
    final Context ctx;
    final WindowManager wm;
    final Handler handler = new Handler(Looper.getMainLooper());
    Panel panel;
    boolean shown;

    final Runnable hide = this::hideNow;

    VolumeOverlay(Context c) {
        ctx = c;
        wm = (WindowManager) c.getSystemService(Context.WINDOW_SERVICE);
    }

    void hideNow() {
        if (shown) {
            try { wm.removeView(panel); } catch (RuntimeException ignored) { }
            shown = false;
        }
    }

    int dp(float v) {
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v, ctx.getResources().getDisplayMetrics()));
    }

    void show(int level, int max, boolean muted) {
        if (panel == null) panel = new Panel(ctx);
        panel.set(level, max, muted);
        if (!shown) {
            WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
                    dp(440), dp(76),
                    WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                            | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                            | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                    PixelFormat.TRANSLUCENT);
            lp.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
            lp.y = dp(36);
            try {
                wm.addView(panel, lp);
                shown = true;
            } catch (RuntimeException e) {
                Log.w(KeyService.TAG, "can't show volume bar", e);
            }
        }
        handler.removeCallbacks(hide);
        handler.postDelayed(hide, 2000);
    }

    /** Speaker icon + 20 chunky segments + number, all drawn without smoothing. */
    class Panel extends View {
        static final int SEGMENTS = 20;
        final String[] SPEAKER = {
                "...#....",
                "..##..#.",
                "####.#..",
                "####...#",
                "####...#",
                "####.#..",
                "..##..#.",
                "...#....",
        };
        final String[] MUTED = {
                "...#....",
                "..##....",
                "####.#.#",
                "####..#.",
                "####..#.",
                "####.#.#",
                "..##....",
                "...#....",
        };
        final Paint p = new Paint();
        final Paint text = new Paint();
        int level, max = 1;
        boolean muted;

        Panel(Context c) {
            super(c);
            p.setAntiAlias(false);
            text.setAntiAlias(false);
            Typeface tf = c.getResources().getFont(R.font.press_start_2p);
            text.setTypeface(tf);
            text.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 16, c.getResources().getDisplayMetrics()));
            text.setTextAlign(Paint.Align.RIGHT);
        }

        void set(int level, int max, boolean muted) {
            this.level = level;
            this.max = Math.max(1, max);
            this.muted = muted;
            invalidate();
        }

        @Override
        protected void onDraw(Canvas c) {
            int w = getWidth(), h = getHeight(), b = dp(4);
            // frame: ink shadow, yellow border, dark inside
            p.setColor(PixelActivity.INK);
            c.drawRect(b, b, w, h, p);
            p.setColor(muted ? Color.parseColor("#E8343B") : PixelActivity.YELLOW);
            c.drawRect(0, 0, w - b, h - b, p);
            p.setColor(Color.parseColor("#241A4A"));
            c.drawRect(b, b, w - 2 * b, h - 2 * b, p);

            // speaker
            String[] icon = muted ? MUTED : SPEAKER;
            int cell = dp(4), ix = dp(18), iy = (h - b - cell * 8) / 2;
            p.setColor(Color.WHITE);
            for (int r = 0; r < 8; r++)
                for (int col = 0; col < 8; col++)
                    if (icon[r].charAt(col) == '#')
                        c.drawRect(ix + col * cell, iy + r * cell, ix + (col + 1) * cell, iy + (r + 1) * cell, p);

            // segments
            int left = dp(64), right = w - dp(92), gap = dp(3);
            float segW = (right - left - gap * (SEGMENTS - 1)) / (float) SEGMENTS;
            int segH = dp(24), sy = (h - b - segH) / 2;
            int filled = muted ? 0 : Math.round(level * SEGMENTS / (float) max);
            for (int i = 0; i < SEGMENTS; i++) {
                float x = left + i * (segW + gap);
                if (i < filled) {
                    p.setColor(Color.parseColor(i < 12 ? "#26B257" : i < 17 ? "#FFD84A" : "#E8343B"));
                } else {
                    p.setColor(Color.parseColor("#3A2F5C"));
                }
                c.drawRect(x, sy, x + segW, sy + segH, p);
            }

            // number
            text.setColor(muted ? Color.parseColor("#E8343B") : Color.WHITE);
            String label = muted ? "MUTE" : String.valueOf(level);
            Paint.FontMetrics fm = text.getFontMetrics();
            float ty = (h - b) / 2f - (fm.ascent + fm.descent) / 2f;
            c.drawText(label, w - dp(18), ty, text);
        }
    }
}
