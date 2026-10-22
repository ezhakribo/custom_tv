package com.ibo.pixelhome;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Looper;
import android.service.dreams.DreamService;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

/** Screensaver: the living pixel sky with a big clock. No ads, nothing to sign in to. */
public class PixelDream extends DreamService {
    final Handler handler = new Handler(Looper.getMainLooper());
    SkyView sky;
    TextView clock, date, info;
    PixelIcon weather;
    String weatherText = "";

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        setInteractive(false);
        setFullscreen(true);
        Typeface font = getResources().getFont(R.font.press_start_2p);

        FrameLayout root = new FrameLayout(this);
        sky = new SkyView(this);
        root.addView(sky, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        LinearLayout col = new LinearLayout(this);
        col.setOrientation(LinearLayout.VERTICAL);
        col.setGravity(Gravity.CENTER_HORIZONTAL);
        clock = text(font, 56, Color.WHITE);
        date = text(font, 12, PixelActivity.YELLOW);
        LinearLayout row = new LinearLayout(this);
        row.setGravity(Gravity.CENTER_VERTICAL);
        weather = new PixelIcon(this);
        row.addView(weather, new LinearLayout.LayoutParams(dp(24), dp(24)));
        info = text(font, 11, Color.WHITE);
        LinearLayout.LayoutParams ip = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        ip.leftMargin = dp(10);
        row.addView(info, ip);
        col.addView(clock);
        col.addView(date, margins(18));
        col.addView(row, margins(16));
        FrameLayout.LayoutParams cp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL | Gravity.TOP);
        cp.topMargin = dp(70);
        root.addView(col, cp);
        setContentView(root);
    }

    int dp(float v) {
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v, getResources().getDisplayMetrics()));
    }

    LinearLayout.LayoutParams margins(int topDp) {
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        p.topMargin = dp(topDp);
        return p;
    }

    TextView text(Typeface f, float sp, int color) {
        TextView t = new TextView(this);
        t.setTypeface(f);
        t.setTextSize(TypedValue.COMPLEX_UNIT_SP, sp);
        t.setTextColor(color);
        t.setShadowLayer(0.01f, dp(3), dp(3), PixelActivity.INK);
        t.setIncludeFontPadding(false);
        return t;
    }

    final Runnable tick = new Runnable() {
        @Override public void run() {
            clock.setText(HomeInfo.clock());
            date.setText(HomeInfo.date());
            info.setText(weatherText + HomeInfo.nextPrayer());
            HomeInfo.weather(PixelDream.this, (t, code, isDay) -> {
                weather.set(HomeInfo.icon(code, isDay), HomeInfo.iconColor(code, isDay));
                weatherText = t + "C   ";
                info.setText(weatherText + HomeInfo.nextPrayer());
            });
            handler.postDelayed(this, 15_000);
        }
    };

    @Override
    public void onDreamingStarted() {
        super.onDreamingStarted();
        sky.start();
        handler.post(tick);
    }

    @Override
    public void onDreamingStopped() {
        super.onDreamingStopped();
        sky.stop();
        handler.removeCallbacks(tick);
    }
}
