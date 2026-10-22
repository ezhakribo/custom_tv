package com.ibo.pixelhome;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.tv.TvContract;
import android.media.tv.TvInputInfo;
import android.media.tv.TvInputManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
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

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

public class MainActivity extends PixelActivity {
    static final String TAG = "PixelHome";

    // ===================== EDIT HERE =====================
    static final String[] YOUTUBE = {"com.teamsmart.videomanager.tv", "com.liskovsoft.smarttubetv", "com.google.android.youtube.tv"};
    static final String[] NETFLIX = {"com.netflix.ninja"};
    static final String[] DISNEY  = {"com.disney.disneyplus"};
    static final String[] SPOTIFY = {"com.spotify.tv.android"};
    static final String TV_APP = "";   // leave empty to detect the TV app automatically
    // =====================================================

    /** One tile on the home screen. */
    static final class Slot {
        final String id, label, kind;
        final int art;
        final String[] pkgs;
        Slot(String id, String label, String kind, int art, String... pkgs) {
            this.id = id; this.label = label; this.kind = kind; this.art = art; this.pkgs = pkgs;
        }
    }

    static final Slot[] ROW_APPS = {
            new Slot("youtube", "YOUTUBE", "app", R.drawable.thumb_youtube, YOUTUBE),
            new Slot("netflix", "NETFLIX", "app", R.drawable.thumb_netflix, NETFLIX),
            new Slot("disney", "DISNEY+", "app", R.drawable.thumb_disney, DISNEY),
            new Slot("spotify", "SPOTIFY", "app", R.drawable.thumb_spotify, SPOTIFY)};
    static final Slot[] ROW_INPUTS = {
            new Slot("tv", "TV", "tv", R.drawable.thumb_tv),
            new Slot("av", "DIGITAL TV", "av", R.drawable.thumb_digitaltv),
            new Slot("hdmi3", "GAMES", "hdmi", R.drawable.thumb_games),
            new Slot("fav", "FAVORIT", "fav", R.drawable.thumb_favorite)};
    static final String[] SMARTTUBE_THEN_YOUTUBE = {"com.teamsmart.videomanager.tv", "com.liskovsoft.smarttubetv.beta",
            "com.liskovsoft.smarttubetv", "com.google.android.youtube.tv"};
    static final int REQ_PHOTO = 7;

    final Handler handler = new Handler(Looper.getMainLooper());
    SharedPreferences prefs;
    TextView clock, greeting, date, infoText;
    PixelIcon weatherIcon;
    View firstTile;
    String pendingPhotoSlot;
    final List<View> tiles = new ArrayList<>();
    SlideshowView slides;
    static boolean splashShown;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        prefs = getSharedPreferences(KeyService.PREFS, MODE_PRIVATE);
        labelsOnFocusOnly = true;
        FrameLayout root = backdrop();
        root.setClipChildren(false);

        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setPadding(dp(56), dp(20), dp(56), dp(0));
        page.setClipChildren(false);
        page.setClipToPadding(false);

        // --- top bar: greeting + date | apps, settings | clock + weather/prayer ---
        LinearLayout top = row();
        LinearLayout left = new LinearLayout(this);
        left.setOrientation(LinearLayout.VERTICAL);
        greeting = text("", 16, YELLOW);
        date = text("", 9, LILAC);
        left.addView(greeting);
        LinearLayout.LayoutParams dlp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dlp.topMargin = dp(10);
        left.addView(date, dlp);
        top.addView(left, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        top.addView(tile("APLIKASI", crisp(R.drawable.thumb_apps), 40, 40, 6,
                () -> startActivity(new Intent(this, AppsActivity.class)), null));
        top.addView(tile("SETELAN", crisp(R.drawable.thumb_settings), 40, 40, 6,
                () -> safeStart(new Intent(Settings.ACTION_SETTINGS), "Setelan"), this::pixelMenu), gap(8));

        LinearLayout right = new LinearLayout(this);
        right.setOrientation(LinearLayout.VERTICAL);
        right.setGravity(Gravity.END);
        clock = text("", 20, Color.WHITE);
        right.addView(clock);
        LinearLayout info = row();
        weatherIcon = new PixelIcon(this);
        info.addView(weatherIcon, new LinearLayout.LayoutParams(dp(16), dp(16)));
        infoText = text("", 8, Color.WHITE);
        info.addView(infoText, gap(6));
        LinearLayout.LayoutParams ilp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        ilp.topMargin = dp(8);
        right.addView(info, ilp);
        top.addView(right, gap(24));
        page.addView(top);

        // --- the two rows of tiles, in the order saved on the TV ---
        page.addView(header("TONTON & DENGAR"), headerParams(12));
        page.addView(buildRow("row0", ROW_APPS));
        page.addView(header("INPUT & FAVORIT"), headerParams(14));
        page.addView(buildRow("row1", ROW_INPUTS));

        root.addView(page, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, Gravity.TOP));

        // full-width pixel-art slideshow along the bottom edge; the walker moves onto it
        boolean walkerOn = prefs.getBoolean("walker", true);
        if (prefs.getBoolean("slides", true)) {
            slides = new SlideshowView(this);
            slides.showWalker = walkerOn;
            sky.showWalker = false;
            root.addView(slides, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(120), Gravity.BOTTOM));
        }
        if (!splashShown) {                       // opening animation, once per TV start
            splashShown = true;
            root.addView(new SplashView(this), new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        }
        setContentView(root);

        View restore = null;
        String want = prefs.getString("focus_id", null);
        for (View t : tiles) if (want != null && want.equals(t.getTag())) restore = t;
        (restore != null ? restore : firstTile).requestFocus();
        logInputs();
        handleCommand(getIntent());
    }

    // ---------- rows & slots ----------
    LinearLayout buildRow(String key, Slot[] slots) {
        LinearLayout r = row();
        List<Slot> ordered = order(key, slots);
        for (int i = 0; i < ordered.size(); i++) {
            Slot s = ordered.get(i);
            LinearLayout t = tile(label(s), art(s), 160, 90, 10, () -> open(s), () -> slotMenu(key, slots, s));
            View frame = t.getChildAt(0);
            frame.setTag(s.id);
            tiles.add(frame);
            if (firstTile == null) firstTile = frame;
            r.addView(t, gap(i == 0 ? 0 : 28));
        }
        return r;
    }

    List<Slot> order(String key, Slot[] slots) {
        List<Slot> out = new ArrayList<>();
        for (String id : prefs.getString("order_" + key, "").split(","))
            for (Slot s : slots) if (s.id.equals(id) && !out.contains(s)) out.add(s);
        for (Slot s : slots) if (!out.contains(s)) out.add(s);
        return out;
    }

    String label(Slot s) {
        String custom = prefs.getString("label_" + s.id, null);
        if (custom != null) return custom;
        if (s.kind.equals("fav")) return prefs.getString("fav_name", s.label).toUpperCase(Locale.ROOT);
        return s.label;
    }

    Drawable art(Slot s) {
        String path = prefs.getString("img_" + s.id, null);
        if (path != null && new File(path).exists()) {
            Bitmap b = BitmapFactory.decodeFile(path);
            if (b != null) {
                BitmapDrawable d = new BitmapDrawable(getResources(), b);
                d.setFilterBitmap(false);
                return d;
            }
        }
        return crisp(s.art);
    }

    void open(Slot s) {
        String pkg = prefs.getString("pkg_" + s.id, null);
        if (pkg != null) { launch(label(s), pkg); return; }
        switch (s.kind) {
            case "tv": openLiveTv(); break;
            case "av": openInput("Digital TV (AV)", findAv()); break;
            case "hdmi": openInput("Games (HDMI 3)", findHdmi(3)); break;
            case "fav": openFavorite(); break;
            default: launch(label(s), s.pkgs);
        }
    }

    @Override
    void onTileFocused(View v) {
        if (v.getTag() != null) prefs.edit().putString("focus_id", (String) v.getTag()).apply();
    }

    // ---------- hold OK on a tile: edit it right on the TV ----------
    void slotMenu(String rowKey, Slot[] slots, Slot s) {
        List<String> items = new ArrayList<>();
        List<Runnable> acts = new ArrayList<>();
        items.add("Ganti aplikasi"); acts.add(() -> pickApp(s));
        items.add("Pakai foto sendiri"); acts.add(() -> pickPhoto(s.id));
        if (prefs.contains("pkg_" + s.id) || prefs.contains("img_" + s.id) || prefs.contains("label_" + s.id)) {
            items.add("Kembalikan seperti semula");
            acts.add(() -> {
                prefs.edit().remove("pkg_" + s.id).remove("img_" + s.id).remove("label_" + s.id).apply();
                recreate();
            });
        }
        items.add("Geser ke kiri"); acts.add(() -> move(rowKey, slots, s, -1));
        items.add("Geser ke kanan"); acts.add(() -> move(rowKey, slots, s, 1));
        if (!s.kind.equals("app") && !s.kind.equals("fav")) { items.add("Lihat daftar input"); acts.add(this::showInputs); }
        items.add("Suara 8-bit: " + (soundsOn() ? "NYALA" : "MATI"));
        acts.add(() -> prefs.edit().putBoolean("sounds", !soundsOn()).apply());
        items.add("Demo langit 24 jam (48 detik)"); acts.add(this::skyDemo);
        new AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Dialog_Alert)
                .setTitle(label(s))
                .setItems(items.toArray(new String[0]), (d, which) -> acts.get(which).run())
                .show();
    }

    /** Hold OK on SETELAN: the Pixel Home options that aren't tied to one tile. */
    void pixelMenu() {
        boolean walker = prefs.getBoolean("walker", true);
        String[] items = {"Suara 8-bit: " + (soundsOn() ? "NYALA" : "MATI"), "Demo langit 24 jam (48 detik)",
                "Atur ulang semua tile", "Karakter berjalan: " + (walker ? "NYALA" : "MATI"),
                "Slideshow: " + (prefs.getBoolean("slides", true) ? "NYALA" : "MATI")};
        new AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Dialog_Alert)
                .setTitle("PIXEL HOME")
                .setItems(items, (d, which) -> {
                    if (which == 0) prefs.edit().putBoolean("sounds", !soundsOn()).apply();
                    else if (which == 1) skyDemo();
                    else if (which == 3) { prefs.edit().putBoolean("walker", !walker).apply(); recreate(); }
                    else if (which == 4) { prefs.edit().putBoolean("slides", !prefs.getBoolean("slides", true)).apply(); recreate(); }
                    else {
                        SharedPreferences.Editor ed = prefs.edit();
                        for (String k : prefs.getAll().keySet())
                            if (k.startsWith("pkg_") || k.startsWith("img_") || k.startsWith("label_") || k.startsWith("order_")) ed.remove(k);
                        ed.apply();
                        recreate();
                    }
                }).show();
    }

    void skyDemo() {
        SkyView.demo = true;
        SkyView.demoStart = System.currentTimeMillis();
        toast("Demo: 24 jam dalam 48 detik");
        handler.postDelayed(() -> SkyView.demo = false, 48_000);
    }

    void move(String rowKey, Slot[] slots, Slot s, int dir) {
        List<Slot> o = order(rowKey, slots);
        int i = o.indexOf(s), j = i + dir;
        if (j < 0 || j >= o.size()) return;
        o.set(i, o.get(j));
        o.set(j, s);
        StringBuilder sb = new StringBuilder();
        for (Slot x : o) sb.append(sb.length() == 0 ? "" : ",").append(x.id);
        prefs.edit().putString("order_" + rowKey, sb.toString()).putString("focus_id", s.id).apply();
        recreate();
    }

    void pickApp(Slot s) {
        PackageManager pm = getPackageManager();
        List<ResolveInfo> apps = pm.queryIntentActivities(
                new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER), 0);
        apps.sort(Comparator.comparing(r -> r.loadLabel(pm).toString().toLowerCase(Locale.ROOT)));
        List<String> names = new ArrayList<>(), pkgs = new ArrayList<>();
        for (ResolveInfo r : apps) {
            if (r.activityInfo.packageName.equals(getPackageName()) || pkgs.contains(r.activityInfo.packageName)) continue;
            names.add(r.loadLabel(pm).toString());
            pkgs.add(r.activityInfo.packageName);
        }
        new AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Dialog_Alert)
                .setTitle("Pilih aplikasi")
                .setItems(names.toArray(new String[0]), (d, w) -> {
                    String l = names.get(w).toUpperCase(Locale.ROOT);
                    prefs.edit().putString("pkg_" + s.id, pkgs.get(w))
                            .putString("label_" + s.id, l.length() > 12 ? l.substring(0, 12) : l).apply();
                    recreate();
                }).show();
    }

    // ---------- own photos -> pixel art ----------
    void pickPhoto(String slotId) {
        pendingPhotoSlot = slotId;
        String perm = Build.VERSION.SDK_INT >= 33 ? "android.permission.READ_MEDIA_IMAGES" : Manifest.permission.READ_EXTERNAL_STORAGE;
        if (checkSelfPermission(perm) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{perm}, REQ_PHOTO);
            return;
        }
        showPhotos();
    }

    @Override
    public void onRequestPermissionsResult(int code, String[] perms, int[] results) {
        super.onRequestPermissionsResult(code, perms, results);
        if (code == REQ_PHOTO && results.length > 0 && results[0] == PackageManager.PERMISSION_GRANTED) showPhotos();
        else if (code == REQ_PHOTO) toast("Izin foto ditolak - tidak bisa membaca flashdisk");
    }

    List<File> findPhotos() {
        List<File> dirs = new ArrayList<>();
        File own = getExternalFilesDir("tiles");
        if (own != null) dirs.add(own);
        File ext = Environment.getExternalStorageDirectory();
        for (String d : new String[]{"PixelHome", "Pictures", "Download", "DCIM"}) dirs.add(new File(ext, d));
        File[] volumes = new File("/storage").listFiles();
        if (volumes != null) for (File v : volumes) {
            if (v.getName().equals("emulated") || v.getName().equals("self")) continue;
            dirs.add(v);
            dirs.add(new File(v, "PixelHome"));
        }
        List<File> out = new ArrayList<>();
        for (File d : dirs) {
            File[] fs = d.listFiles();
            if (fs == null) continue;
            Arrays.sort(fs);
            for (File f : fs) {
                String n = f.getName().toLowerCase(Locale.ROOT);
                if (f.isFile() && (n.endsWith(".jpg") || n.endsWith(".jpeg") || n.endsWith(".png") || n.endsWith(".webp"))) out.add(f);
                if (out.size() >= 80) return out;
            }
        }
        return out;
    }

    void showPhotos() {
        List<File> files = findPhotos();
        if (files.isEmpty()) {
            toast("Tidak ada foto. Taruh foto di folder PixelHome di flashdisk, lalu coba lagi.");
            return;
        }
        String[] names = new String[files.size()];
        for (int i = 0; i < names.length; i++) names[i] = files.get(i).getParentFile().getName() + "/" + files.get(i).getName();
        String slot = pendingPhotoSlot;
        new AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Dialog_Alert)
                .setTitle("Pilih foto")
                .setItems(names, (d, w) -> {
                    toast("Mengubah jadi pixel art...");
                    File src = files.get(w);
                    new Thread(() -> {
                        Bitmap px = pixelate(src);
                        if (px == null) { runOnUiThread(() -> toast("Foto tidak bisa dibuka")); return; }
                        File out = new File(getFilesDir(), "tile_" + slot + ".png");
                        try (FileOutputStream fo = new FileOutputStream(out)) {
                            px.compress(Bitmap.CompressFormat.PNG, 100, fo);
                        } catch (Exception e) {
                            runOnUiThread(() -> toast("Gagal menyimpan foto"));
                            return;
                        }
                        prefs.edit().putString("img_" + slot, out.getAbsolutePath()).apply();
                        runOnUiThread(this::recreate);
                    }).start();
                }).show();
    }

    /** Centre-crop to 16:9, shrink to 64x36, reduce colours, add the dark tile border. */
    static Bitmap pixelate(File f) {
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(f.getAbsolutePath(), o);
        int sample = 1;
        while (Math.min(o.outWidth, o.outHeight) / (sample * 2) >= 256) sample *= 2;
        o = new BitmapFactory.Options();
        o.inSampleSize = sample;
        Bitmap src = BitmapFactory.decodeFile(f.getAbsolutePath(), o);
        if (src == null) return null;
        int w = src.getWidth(), h = src.getHeight(), cw = w, ch = w * 9 / 16;
        if (ch > h) { ch = h; cw = h * 16 / 9; }
        Bitmap crop = Bitmap.createBitmap(src, (w - cw) / 2, (h - ch) / 2, cw, ch);
        Bitmap small = Bitmap.createScaledBitmap(crop, 64, 36, true).copy(Bitmap.Config.ARGB_8888, true);
        for (int y = 0; y < 36; y++)
            for (int x = 0; x < 64; x++) {
                int c = small.getPixel(x, y);
                int r = Math.round(Color.red(c) / 51f) * 51, g = Math.round(Color.green(c) / 51f) * 51, b = Math.round(Color.blue(c) / 51f) * 51;
                boolean edge = x == 0 || y == 0 || x == 63 || y == 35;
                small.setPixel(x, y, edge ? 0xFF0B0820 : Color.rgb(r, g, b));
            }
        return small;
    }

    // ---------- favorite video / channel ----------
    void openFavorite() {
        String url = prefs.getString("fav_url", "");
        if (url.isEmpty()) {
            toast("Tile FAVORIT belum diatur - lihat README (favUrl)");
            return;
        }
        Uri u = Uri.parse(url);
        for (String pkg : SMARTTUBE_THEN_YOUTUBE) {
            if (getPackageManager().getLaunchIntentForPackage(pkg) == null
                    && getPackageManager().getLeanbackLaunchIntentForPackage(pkg) == null) continue;
            Intent i = new Intent(Intent.ACTION_VIEW, u).setPackage(pkg).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            try { startActivity(i); return; } catch (ActivityNotFoundException ignored) { }
        }
        safeStart(new Intent(Intent.ACTION_VIEW, u), "Favorit");
    }

    // ---------- header texts ----------
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

    // ---------- clock, greeting, weather, prayer ----------
    String weatherText = "";

    final Runnable tick = new Runnable() {
        @Override public void run() {
            clock.setText(HomeInfo.clock());
            greeting.setText(HomeInfo.greeting(MainActivity.this));
            greeting.setTextColor(SkyView.accentNow());
            date.setText(HomeInfo.date());
            infoText.setText(weatherText + HomeInfo.nextPrayer());
            HomeInfo.weather(MainActivity.this, (t, code, isDay) -> {
                weatherIcon.set(HomeInfo.icon(code, isDay), HomeInfo.iconColor(code, isDay));
                weatherText = t + "C  ";
                infoText.setText(weatherText + HomeInfo.nextPrayer());
            });
            handler.postDelayed(this, 15_000);
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        if (slides != null) slides.start();
        handler.removeCallbacks(tick);
        handler.post(tick);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (slides != null) slides.stop();
        handler.removeCallbacks(tick);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleCommand(intent);
    }

    @Override
    public void onBackPressed() {
        // A home screen stays put when Back is pressed.
    }

    /**
     * Commands over adb, e.g.
     *   am start -n com.ibo.pixelhome/.MainActivity --es learn home|netflix|disney
     *   ... --ez forgetKeys true | --ez pixelVolume true|false | --ez sounds true|false
     *   ... --es name IBO | --es favUrl https://www.youtube.com/@channel | --es favName LOFI | --ez skyDemo true
     */
    void handleCommand(Intent i) {
        if (i == null || i.getExtras() == null) return;
        SharedPreferences p = prefs;
        String learn = i.getStringExtra("learn");
        if (learn == null && i.getBooleanExtra("learnKey", false)) learn = "home";
        if (learn != null) {
            String on = Settings.Secure.getString(getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            if (on == null || !on.contains(getPackageName())) {
                toast("Nyalakan dulu helper 'Pixel Home remote button'");
                return;
            }
            p.edit().putString(KeyService.LEARN, learn.toLowerCase(Locale.ROOT)).apply();
            toast("Sekarang tekan tombol remote untuk membuka " + learn);
        } else if (i.hasExtra("pixelVolume")) {
            boolean on = i.getBooleanExtra("pixelVolume", false);
            p.edit().putBoolean(KeyService.PIXEL_VOLUME, on).apply();
            toast(on ? "Bar volume pixel NYALA" : "Bar volume pixel MATI");
        } else if (i.getBooleanExtra("forgetKeys", false) || i.getBooleanExtra("forgetKey", false)) {
            SharedPreferences.Editor ed = p.edit();
            for (String k : p.getAll().keySet())
                if (k.startsWith(KeyService.MAP) || k.equals(KeyService.LEARN)) ed.remove(k);
            ed.apply();
            toast("Semua tombol yang dipelajari sudah dilupakan");
        }
        if (i.hasExtra("sounds")) p.edit().putBoolean("sounds", i.getBooleanExtra("sounds", true)).apply();
        if (i.hasExtra("name")) p.edit().putString("name", i.getStringExtra("name")).apply();
        if (i.hasExtra("favUrl")) p.edit().putString("fav_url", i.getStringExtra("favUrl")).apply();
        if (i.hasExtra("favName")) { p.edit().putString("fav_name", i.getStringExtra("favName")).apply(); recreate(); }
        if (i.getBooleanExtra("skyDemo", false)) skyDemo();
        if (i.hasExtra("slides")) { p.edit().putBoolean("slides", i.getBooleanExtra("slides", true)).apply(); recreate(); }
        if (i.hasExtra("walker")) { p.edit().putBoolean("walker", i.getBooleanExtra("walker", true)).apply(); recreate(); }
        if (i.hasExtra("name")) { handler.removeCallbacks(tick); handler.post(tick); }
    }

    // ---------- apps ----------
    void launch(String name, String... packages) {
        PackageManager pm = getPackageManager();
        for (String p : packages) {
            Intent i = pm.getLeanbackLaunchIntentForPackage(p);
            if (i == null) i = pm.getLaunchIntentForPackage(p);
            if (i != null && safeStart(i, name)) return;
        }
        toast(name + " belum terpasang atau dimatikan");
    }

    boolean safeStart(Intent i, String name) {
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            startActivity(i);
            return true;
        } catch (ActivityNotFoundException | SecurityException e) {
            Log.w(TAG, "can't open " + name, e);
            toast("Tidak bisa membuka " + name);
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
            toast(name + " tidak ditemukan di TV ini (tahan OK pada tile untuk melihat daftar input)");
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
        else toast("Aplikasi TV tidak ditemukan - isi TV_APP di MainActivity.java");
    }

    void logInputs() {
        for (TvInputInfo i : inputs()) {
            Log.i(TAG, "input type=" + i.getType() + " label='" + label(i) + "' id=" + i.getId()
                    + " passthrough=" + i.isPassthroughInput());
        }
        Log.i(TAG, "tv app=" + tvAppPackage());
    }

    void showInputs() {
        StringBuilder sb = new StringBuilder("Input di TV ini:\n");
        List<TvInputInfo> list = inputs();
        if (list.isEmpty()) sb.append("(tidak ada)");
        for (TvInputInfo i : list) sb.append(label(i)).append("  [type ").append(i.getType()).append("]\n");
        toast(sb.toString().trim());
        logInputs();
    }
}
