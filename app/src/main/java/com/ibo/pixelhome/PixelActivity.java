package com.ibo.pixelhome;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.SystemClock;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/** Look and feel shared by every screen: pixel font, crisp (unsmoothed) images, yellow focus frame. */
public abstract class PixelActivity extends Activity {
    static final int YELLOW = Color.parseColor("#FFD84A");
    static final int INK = Color.parseColor("#1A1430");
    static final int LILAC = Color.parseColor("#C9B8FF");

    Typeface pixel;
    SkyView sky;

    // ---------- 8-bit sounds ----------
    static SoundPool sounds;
    static int blipId, selectId;
    long quietUntil;   // no blip for the automatic first focus

    void initSounds() {
        if (sounds != null) return;
        sounds = new SoundPool.Builder().setMaxStreams(2).setAudioAttributes(new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build()).build();
        blipId = sounds.load(this, R.raw.blip, 1);
        selectId = sounds.load(this, R.raw.select, 1);
    }

    boolean soundsOn() {
        return getSharedPreferences(KeyService.PREFS, MODE_PRIVATE).getBoolean("sounds", true);
    }

    void play(int id) {
        if (sounds != null && soundsOn() && SystemClock.uptimeMillis() > quietUntil) sounds.play(id, 0.6f, 0.6f, 0, 0, 1f);
    }

    @Override
    protected void onCreate(android.os.Bundle state) {
        super.onCreate(state);
        initSounds();
        quietUntil = SystemClock.uptimeMillis() + 600;
    }

    @Override
    protected void onResume() {
        super.onResume();
        quietUntil = SystemClock.uptimeMillis() + 400;
        if (sky != null) sky.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (sky != null) sky.stop();
    }

    Typeface font() {
        if (pixel == null) pixel = getResources().getFont(R.font.press_start_2p);
        return pixel;
    }

    int dp(float v) {
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v, getResources().getDisplayMetrics()));
    }

    /** Pixel art scaled up without blurring. */
    Drawable crisp(int res) {
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inScaled = false;
        Bitmap b = BitmapFactory.decodeResource(getResources(), res, o);
        BitmapDrawable d = new BitmapDrawable(getResources(), b);
        d.setFilterBitmap(false);
        d.setAntiAlias(false);
        return d;
    }

    ImageView image(Drawable d, ImageView.ScaleType type) {
        ImageView iv = new ImageView(this);
        iv.setImageDrawable(d);
        iv.setScaleType(type);
        return iv;
    }

    TextView text(String s, float sp, int color) {
        TextView t = new TextView(this);
        t.setText(s);
        t.setTypeface(font());
        t.setTextSize(TypedValue.COMPLEX_UNIT_SP, sp);
        t.setTextColor(color);
        t.setShadowLayer(0.01f, dp(2), dp(2), INK);   // hard pixel shadow
        t.setIncludeFontPadding(false);
        return t;
    }

    /** Full-screen living pixel sky with a container on top. */
    FrameLayout backdrop() {
        FrameLayout root = new FrameLayout(this);
        sky = new SkyView(this);
        root.addView(sky, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        return root;
    }

    /**
     * A focusable tile: picture in a frame, label underneath.
     * Focused = yellow frame, slightly bigger, "> LABEL".
     */
    LinearLayout tile(String label, Drawable picture, int wDp, int hDp, float labelSp,
                      Runnable onClick, Runnable onLongClick) {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setGravity(Gravity.CENTER_HORIZONTAL);
        box.setClipChildren(false);

        FrameLayout frame = new FrameLayout(this);
        int pad = dp(4);
        frame.setPadding(pad, pad, pad, pad);
        frame.setBackgroundColor(INK);
        frame.setFocusable(true);
        frame.setClickable(true);
        frame.addView(image(picture, ImageView.ScaleType.FIT_XY),
                new FrameLayout.LayoutParams(dp(wDp), dp(hDp)));
        box.addView(frame);

        TextView name = text(label, labelSp, Color.WHITE);
        name.setGravity(Gravity.CENTER);
        name.setSingleLine(true);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dp(wDp + 24), ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.topMargin = dp(10);
        box.addView(name, lp);

        frame.setOnClickListener(v -> { play(selectId); onClick.run(); });
        if (onLongClick != null) frame.setOnLongClickListener(v -> { onLongClick.run(); return true; });
        frame.setOnFocusChangeListener((v, has) -> {
            frame.setBackgroundColor(has ? YELLOW : INK);
            name.setText(has ? "> " + label : label);
            name.setTextColor(has ? YELLOW : Color.WHITE);
            box.animate().scaleX(has ? 1.08f : 1f).scaleY(has ? 1.08f : 1f).setDuration(110).start();
            if (has) { play(blipId); onTileFocused(v); }
        });
        return box;
    }

    void onTileFocused(View v) { }

    void toast(String s) {
        Toast.makeText(this, s, Toast.LENGTH_LONG).show();
    }

    LinearLayout row() {
        LinearLayout r = new LinearLayout(this);
        r.setOrientation(LinearLayout.HORIZONTAL);
        r.setGravity(Gravity.CENTER_VERTICAL);
        r.setClipChildren(false);
        r.setClipToPadding(false);
        return r;
    }

    LinearLayout.LayoutParams gap(int leftDp) {
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        p.leftMargin = dp(leftDp);
        return p;
    }
}
