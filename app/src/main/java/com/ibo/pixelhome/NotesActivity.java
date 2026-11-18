package com.ibo.pixelhome;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Catatan Keluarga: sticky notes on the TV.
 * OK = open/edit, hold OK = colour, pin to home screen, delete.
 * Notes can also be added from the computer:
 *   adb shell am start -n com.ibo.pixelhome/.NotesActivity --es title "'Belanja'" --es add "'Beli susu dan roti'"
 */
public class NotesActivity extends PixelActivity {
    static final String KEY = "notes", PIN = "notes_pin";
    static final int[] PAPER = {0xFFFFF1A8, 0xFFFFC8D8, 0xFFBFE8FF, 0xFFC8F0B8, 0xFFE8D8FF, 0xFFFFD8B0};
    static final String[] PAPER_NAMES = {"Kuning", "Merah muda", "Biru", "Hijau", "Ungu", "Jingga"};
    static final int PER_ROW = 4;

    SharedPreferences prefs;
    LinearLayout grid;
    String focusId;
    int cardW = 180, cardH = 112;

    // ---------- storage (shared with the home screen and screensaver) ----------
    static List<JSONObject> load(Context c) {
        List<JSONObject> out = new ArrayList<>();
        try {
            JSONArray a = new JSONArray(HomeInfo.prefs(c).getString(KEY, "[]"));
            for (int i = 0; i < a.length(); i++) out.add(a.getJSONObject(i));
        } catch (JSONException ignored) { }
        return out;
    }

    static void save(Context c, List<JSONObject> notes) {
        JSONArray a = new JSONArray();
        for (JSONObject o : notes) a.put(o);
        HomeInfo.prefs(c).edit().putString(KEY, a.toString()).apply();
    }

    static String pinnedText(Context c) {
        String id = HomeInfo.prefs(c).getString(PIN, null);
        if (id == null) return "";
        for (JSONObject o : load(c)) {
            if (id.equals(o.optString("id"))) {
                String t = o.optString("title").trim();
                if (t.isEmpty()) t = o.optString("body").trim().split("\n")[0];
                t = t.toUpperCase(HomeInfo.ID);
                return t.length() > 40 ? t.substring(0, 39) + "." : t;
            }
        }
        return "";
    }

    static JSONObject newNote(String title, String body, int color) {
        JSONObject o = new JSONObject();
        try {
            o.put("id", Long.toString(System.currentTimeMillis(), 36));
            o.put("title", title);
            o.put("body", body);
            o.put("color", color);
            o.put("time", System.currentTimeMillis());
        } catch (JSONException ignored) { }
        return o;
    }

    // ---------- screen ----------
    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        prefs = HomeInfo.prefs(this);
        handleAdd(getIntent());

        FrameLayout root = backdrop();
        ScrollView scroll = new ScrollView(this);
        scroll.setClipChildren(false);
        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        cardW = fitWidth(PER_ROW, 18, 180);
        cardH = Math.round(cardW * 0.62f);
        page.setPadding(dp(sidePadDp()), dp(28), dp(sidePadDp()), dp(40));
        page.setClipChildren(false);
        page.setClipToPadding(false);
        TextView title = text("CATATAN KELUARGA", 18, SkyView.accentNow());
        page.addView(title);
        TextView hint = text("OK = BUKA   TAHAN OK = WARNA, SEMATKAN, HAPUS", 7, LILAC);
        LinearLayout.LayoutParams hp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        hp.topMargin = dp(10);
        page.addView(hint, hp);
        grid = new LinearLayout(this);
        grid.setOrientation(LinearLayout.VERTICAL);
        grid.setClipChildren(false);
        page.addView(grid);
        scroll.addView(page);
        root.addView(scroll, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        setContentView(root);
        rebuild();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (handleAdd(intent)) rebuild();
    }

    /** --es add "text" [--es title "title"] from adb. */
    boolean handleAdd(Intent i) {
        if (i == null || !i.hasExtra("add")) return false;
        List<JSONObject> notes = load(this);
        String t = i.getStringExtra("title");
        notes.add(newNote(t == null ? "" : t, i.getStringExtra("add"), notes.size() % PAPER.length));
        save(this, notes);
        toast("Catatan ditambahkan");
        return true;
    }

    void rebuild() {
        grid.removeAllViews();
        List<JSONObject> notes = load(this);
        String pin = prefs.getString(PIN, "");
        // pinned first, then newest first
        notes.sort((a, b) -> {
            boolean pa = pin.equals(a.optString("id")), pb = pin.equals(b.optString("id"));
            if (pa != pb) return pa ? -1 : 1;
            return Long.compare(b.optLong("time"), a.optLong("time"));
        });
        List<View> cards = new ArrayList<>();
        cards.add(card("+ BARU", "Tulis catatan baru", 0xFFFFFFFF, false, () -> edit(null), null, "new"));
        for (JSONObject n : notes) {
            String id = n.optString("id");
            cards.add(card(n.optString("title"), n.optString("body"), PAPER[n.optInt("color") % PAPER.length],
                    pin.equals(id), () -> edit(n), () -> menu(n), id));
        }
        LinearLayout row = null;
        View focus = null;
        for (int i = 0; i < cards.size(); i++) {
            if (i % PER_ROW == 0) {
                row = row();
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                lp.topMargin = dp(i == 0 ? 26 : 22);
                grid.addView(row, lp);
            }
            View c = cards.get(i);
            row.addView(c, gap(i % PER_ROW == 0 ? 0 : 18));
            if (focusId != null && focusId.equals(c.getTag())) focus = c;
        }
        (focus != null ? focus : cards.get(0)).requestFocus();
    }

    /** A sticky note: paper colour inside a frame that breathes when focused. */
    View card(String title, String body, int paper, boolean pinned, Runnable click, Runnable longClick, String id) {
        FrameLayout frame = new FrameLayout(this);
        frame.setTag(id);
        int pad = dp(4);
        frame.setPadding(pad, pad, pad, pad);
        frame.setBackgroundColor(INK);
        frame.setFocusable(true);
        frame.setClickable(true);

        LinearLayout inner = new LinearLayout(this);
        inner.setOrientation(LinearLayout.VERTICAL);
        inner.setBackgroundColor(paper);
        inner.setPadding(dp(10), dp(10), dp(10), dp(8));
        TextView t = plain((pinned ? "> " : "") + (title.isEmpty() ? "CATATAN" : title.toUpperCase(HomeInfo.ID)), true);
        t.setTypeface(font());
        t.setTextSize(TypedValue.COMPLEX_UNIT_SP, 8);
        t.setSingleLine(true);
        t.setEllipsize(TextUtils.TruncateAt.END);
        inner.addView(t);
        TextView b = plain(body, false);
        b.setMaxLines(4);
        b.setEllipsize(TextUtils.TruncateAt.END);
        LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f);
        bp.topMargin = dp(8);
        inner.addView(b, bp);
        frame.addView(inner, new FrameLayout.LayoutParams(dp(cardW), dp(cardH)));

        frame.setOnClickListener(v -> { play(selectId); focusId = id; click.run(); });
        if (longClick != null) frame.setOnLongClickListener(v -> { focusId = id; longClick.run(); return true; });
        frame.setOnFocusChangeListener((v, has) -> {
            if (has) { breathe(frame); play(blipId); parallaxTo(v); focusId = id; }
            else {
                if (breathFrame == frame && breath != null) { breath.cancel(); breath = null; }
                frame.setBackgroundColor(INK);
            }
            frame.animate().scaleX(has ? 1.06f : 1f).scaleY(has ? 1.06f : 1f).setDuration(140).start();
        });
        return frame;
    }

    TextView plain(String s, boolean bold) {
        TextView t = new TextView(this);
        t.setText(s);
        t.setTextColor(INK);
        t.setTextSize(TypedValue.COMPLEX_UNIT_SP, bold ? 9 : 13);
        t.setIncludeFontPadding(false);
        t.setLineSpacing(dp(2), 1f);
        return t;
    }

    // ---------- editing ----------
    void edit(JSONObject note) {
        LinearLayout form = new LinearLayout(this);
        form.setOrientation(LinearLayout.VERTICAL);
        form.setPadding(dp(24), dp(12), dp(24), 0);
        EditText title = new EditText(this);
        title.setHint("Judul (boleh kosong)");
        title.setSingleLine(true);
        title.setText(note == null ? "" : note.optString("title"));
        EditText body = new EditText(this);
        body.setHint("Tulis catatan...");
        body.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        body.setMinLines(3);
        body.setGravity(Gravity.TOP);
        body.setText(note == null ? "" : note.optString("body"));
        form.addView(title);
        form.addView(body);

        AlertDialog.Builder b = new AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Dialog_Alert)
                .setTitle(note == null ? "Catatan baru" : "Ubah catatan")
                .setView(form)
                .setPositiveButton("SIMPAN", (d, w) -> {
                    String tt = title.getText().toString().trim(), bb = body.getText().toString().trim();
                    if (tt.isEmpty() && bb.isEmpty()) return;
                    List<JSONObject> notes = load(this);
                    if (note == null) {
                        JSONObject n = newNote(tt, bb, notes.size() % PAPER.length);
                        notes.add(n);
                        focusId = n.optString("id");
                    } else {
                        for (JSONObject o : notes) if (o.optString("id").equals(note.optString("id"))) {
                            try { o.put("title", tt); o.put("body", bb); o.put("time", System.currentTimeMillis()); }
                            catch (JSONException ignored) { }
                        }
                    }
                    save(this, notes);
                    rebuild();
                })
                .setNegativeButton("BATAL", null);
        if (note != null) b.setNeutralButton("HAPUS", (d, w) -> confirmDelete(note));
        b.show();
        (note == null ? body : title).requestFocus();
    }

    void menu(JSONObject note) {
        String id = note.optString("id");
        boolean pinned = id.equals(prefs.getString(PIN, ""));
        String[] items = {"Ubah", "Ganti warna", pinned ? "Lepas dari layar utama" : "Sematkan di layar utama", "Hapus"};
        new AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Dialog_Alert)
                .setTitle(note.optString("title").isEmpty() ? "Catatan" : note.optString("title"))
                .setItems(items, (d, w) -> {
                    if (w == 0) edit(note);
                    else if (w == 1) pickColor(note);
                    else if (w == 2) {
                        prefs.edit().putString(PIN, pinned ? "" : id).apply();
                        toast(pinned ? "Dilepas dari layar utama" : "Tampil di layar utama dan screensaver");
                        rebuild();
                    } else confirmDelete(note);
                }).show();
    }

    void pickColor(JSONObject note) {
        new AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Dialog_Alert)
                .setTitle("Warna kertas")
                .setItems(PAPER_NAMES, (d, w) -> {
                    List<JSONObject> notes = load(this);
                    for (JSONObject o : notes) if (o.optString("id").equals(note.optString("id"))) {
                        try { o.put("color", w); } catch (JSONException ignored) { }
                    }
                    save(this, notes);
                    rebuild();
                }).show();
    }

    void confirmDelete(JSONObject note) {
        new AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Dialog_Alert)
                .setTitle("Hapus catatan ini?")
                .setMessage("Catatan yang dihapus tidak bisa dikembalikan.")
                .setPositiveButton("HAPUS", (d, w) -> {
                    List<JSONObject> notes = load(this);
                    notes.removeIf(o -> o.optString("id").equals(note.optString("id")));
                    save(this, notes);
                    if (note.optString("id").equals(prefs.getString(PIN, ""))) prefs.edit().remove(PIN).apply();
                    focusId = "new";
                    rebuild();
                })
                .setNegativeButton("BATAL", null).show();
    }
}
