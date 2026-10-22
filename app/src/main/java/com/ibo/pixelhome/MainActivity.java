package com.ibo.pixelhome;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.media.tv.TvContract;
import android.media.tv.TvInputInfo;
import android.media.tv.TvInputManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

public class MainActivity extends PixelActivity {
    static final String TAG = "PixelHome";

    // ===================== EDIT HERE =====================
    // Each tile tries these apps in order and opens the first one that's installed.
    static final String[] YOUTUBE = {"com.teamsmart.videomanager.tv", "com.liskovsoft.smarttubetv", "com.google.android.youtube.tv"};
    static final String[] NETFLIX = {"com.netflix.ninja"};
    static final String[] DISNEY  = {"com.disney.disneyplus"};
    static final String[] SPOTIFY = {"com.spotify.tv.android"};
    // Leave empty to detect automatically, or put your TV app's package name here.
    static final String TV_APP = "";
    // =====================================================

    final Handler handler = new Handler(Looper.getMainLooper());
    TextView clock;
    View lastFocused, firstTile;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        FrameLayout root = backdrop();

        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setPadding(dp(56), dp(32), dp(56), dp(24));
        page.setClipChildren(false);
        page.setClipToPadding(false);
        root.setClipChildren(false);

        // --- top bar: title, apps, settings, clock ---
        LinearLayout top = row();
        TextView title = text("PIXEL HOME", 20, YELLOW);
        top.addView(title, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        top.addView(tile("APPS", crisp(R.drawable.thumb_apps), 40, 40, 6,
                () -> startActivity(new Intent(this, AppsActivity.class)), null));
        top.addView(tile("SETTINGS", crisp(R.drawable.thumb_settings), 40, 40, 6,
                () -> safeStart(new Intent(Settings.ACTION_SETTINGS), "Settings"), null), gap(8));
        clock = text("", 20, android.graphics.Color.WHITE);
        top.addView(clock, gap(28));
        page.addView(top);

        // --- watch & listen ---
        page.addView(header("WATCH & LISTEN"), headerParams(20));
        LinearLayout apps = row();
        firstTile = addTile(apps, "YOUTUBE", R.drawable.thumb_youtube, () -> launch("YouTube", YOUTUBE), null, 0);
        addTile(apps, "NETFLIX", R.drawable.thumb_netflix, () -> launch("Netflix", NETFLIX), null, 28);
        addTile(apps, "DISNEY+", R.drawable.thumb_disney, () -> launch("Disney+", DISNEY), null, 28);
        addTile(apps, "SPOTIFY", R.drawable.thumb_spotify, () -> launch("Spotify", SPOTIFY), null, 28);
        page.addView(apps);

        // --- inputs (hold OK on any of these to see what the TV reports) ---
        page.addView(header("INPUTS"), headerParams(26));
        LinearLayout inputs = row();
        addTile(inputs, "TV", R.drawable.thumb_tv, this::openLiveTv, this::showInputs, 0);
        addTile(inputs, "DIGITAL TV", R.drawable.thumb_digitaltv, () -> openInput("Digital TV (AV)", findAv()), this::showInputs, 28);
        addTile(inputs, "GAMES", R.drawable.thumb_games, () -> openInput("Games (HDMI 3)", findHdmi(3)), this::showInputs, 28);
        page.addView(inputs);

        root.addView(page, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, Gravity.TOP));
        setContentView(root);
        firstTile.requestFocus();
        logInputs();
        handleCommand(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleCommand(intent);
    }

    /**
     * adb shell am start -n com.ibo.pixelhome/.MainActivity --es learn home|netflix|disney
     * adb shell am start -n com.ibo.pixelhome/.MainActivity --ez forgetKeys true
     */
    void handleCommand(Intent i) {
        if (i == null) return;
        android.content.SharedPreferences p = getSharedPreferences(KeyService.PREFS, MODE_PRIVATE);
        String learn = i.getStringExtra("learn");
        if (learn == null && i.getBooleanExtra("learnKey", false)) learn = "home";
        if (learn != null) {
            String on = Settings.Secure.getString(getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            if (on == null || !on.contains(getPackageName())) {
                toast("Turn on the 'Pixel Home remote button' helper first");
                return;
            }
            p.edit().putString(KeyService.LEARN, learn.toLowerCase(Locale.ROOT)).apply();
            toast("Now press the remote button that should open " + learn);
        } else if (i.getBooleanExtra("forgetKeys", false) || i.getBooleanExtra("forgetKey", false)) {
            android.content.SharedPreferences.Editor ed = p.edit();
            for (String k : p.getAll().keySet()) {
                if (k.startsWith(KeyService.MAP) || k.equals(KeyService.LEARN)) ed.remove(k);
            }
            ed.apply();
            toast("All learned buttons forgotten - the remote works like before");
        }
    }

    View addTile(LinearLayout row, String label, int art, Runnable click, Runnable longClick, int leftGap) {
        LinearLayout t = tile(label, crisp(art), 176, 99, 11, click, longClick);
        row.addView(t, gap(leftGap));
        return t.getChildAt(0);   // the focusable frame
    }

    TextView header(String s) {
        return text(s, 10, LILAC);
    }

    LinearLayout.LayoutParams headerParams(int topDp) {
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        p.topMargin = dp(topDp);
        p.bottomMargin = dp(14);
        return p;
    }

    @Override
    void onTileFocused(View v) {
        lastFocused = v;
    }

    // ---------- clock ----------
    final Runnable tick = new Runnable() {
        @Override public void run() {
            clock.setText(new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date()));
            handler.postDelayed(this, 15_000);
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        handler.post(tick);
        if (lastFocused != null) lastFocused.requestFocus();
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(tick);
    }

    @Override
    public void onBackPressed() {
        // A home screen stays put when Back is pressed.
    }

    // ---------- apps ----------
    void launch(String name, String... packages) {
        PackageManager pm = getPackageManager();
        for (String p : packages) {
            Intent i = pm.getLeanbackLaunchIntentForPackage(p);
            if (i == null) i = pm.getLaunchIntentForPackage(p);
            if (i != null && safeStart(i, name)) return;
        }
        toast(name + " isn't installed or is turned off");
    }

    boolean safeStart(Intent i, String name) {
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            startActivity(i);
            return true;
        } catch (ActivityNotFoundException | SecurityException e) {
            Log.w(TAG, "can't open " + name, e);
            toast("Can't open " + name);
            return false;
        }
    }

    // ---------- TV inputs ----------
    List<TvInputInfo> inputs() {
        TvInputManager m = (TvInputManager) getSystemService(Context.TV_INPUT_SERVICE);
        List<TvInputInfo> out = new ArrayList<>();
        if (m == null) return out;
        for (TvInputInfo i : m.getTvInputList()) {
            if (i.getParentId() == null) out.add(i);   // skip devices plugged into a port (CEC)
        }
        out.sort(Comparator.comparing(TvInputInfo::getId));
        return out;
    }

    String label(TvInputInfo i) {
        CharSequence c = i.loadLabel(this);
        return c == null ? "" : c.toString();
    }

    TvInputInfo findHdmi(int n) {
        List<TvInputInfo> hdmi = new ArrayList<>();
        Pattern byName = Pattern.compile("(?i).*hdmi\\s*" + n + "(\\D.*)?");
        for (TvInputInfo i : inputs()) {
            if (i.getType() != TvInputInfo.TYPE_HDMI) continue;
            if (byName.matcher(label(i)).matches()) return i;
            hdmi.add(i);
        }
        return hdmi.size() >= n ? hdmi.get(n - 1) : null;   // fall back to the n-th HDMI port
    }

    TvInputInfo findAv() {
        Pattern byName = Pattern.compile("(?i).*(\\bav\\b|composite).*");
        TvInputInfo typed = null;
        for (TvInputInfo i : inputs()) {
            if (byName.matcher(label(i)).matches()) return i;
            int t = i.getType();
            if (typed == null && (t == TvInputInfo.TYPE_COMPOSITE || t == TvInputInfo.TYPE_COMPONENT
                    || t == TvInputInfo.TYPE_SCART || t == TvInputInfo.TYPE_SVIDEO)) typed = i;
        }
        return typed;
    }

    void openInput(String name, TvInputInfo info) {
        if (info == null) {
            toast("Couldn't find " + name + " on this TV (hold OK on the tile to see the inputs)");
            return;
        }
        Intent i = new Intent(Intent.ACTION_VIEW, TvContract.buildChannelUriForPassthroughInput(info.getId()));
        Log.i(TAG, "opening " + name + " -> " + info.getId());
        safeStart(i, name);
    }

    /** The app that plays TV inputs (the TV's own player). */
    String tvAppPackage() {
        if (!TV_APP.isEmpty()) return TV_APP;
        Uri probe = TvContract.buildChannelUriForPassthroughInput("probe");
        ResolveInfo r = getPackageManager().resolveActivity(new Intent(Intent.ACTION_VIEW, probe), 0);
        if (r == null || r.activityInfo == null || "android".equals(r.activityInfo.packageName)) return null;
        return r.activityInfo.packageName;
    }

    void openLiveTv() {
        for (TvInputInfo i : inputs()) {
            if (i.getType() == TvInputInfo.TYPE_TUNER && !i.isPassthroughInput()) {
                Intent v = new Intent(Intent.ACTION_VIEW, TvContract.buildChannelsUriForInput(i.getId()));
                v.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                try {
                    startActivity(v);
                    return;
                } catch (ActivityNotFoundException e) {
                    Log.i(TAG, "tuner view not handled, falling back to TV app");
                }
            }
        }
        String pkg = tvAppPackage();
        if (pkg != null) launch("TV", pkg);
        else toast("Couldn't find the TV app - set TV_APP in MainActivity.java");
    }

    void logInputs() {
        for (TvInputInfo i : inputs()) {
            Log.i(TAG, "input type=" + i.getType() + " label='" + label(i) + "' id=" + i.getId()
                    + " passthrough=" + i.isPassthroughInput());
        }
        Log.i(TAG, "tv app=" + tvAppPackage());
    }

    void showInputs() {
        StringBuilder sb = new StringBuilder("Inputs on this TV:\n");
        List<TvInputInfo> list = inputs();
        if (list.isEmpty()) sb.append("(none reported)");
        for (TvInputInfo i : list) sb.append(label(i)).append("  [type ").append(i.getType()).append("]\n");
        toast(sb.toString().trim());
        logInputs();
    }
}
