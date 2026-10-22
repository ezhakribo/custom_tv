package com.ibo.pixelhome;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Every app on the TV, as pixel-framed tiles. Back returns to Pixel Home. */
public class AppsActivity extends PixelActivity {
    static final int PER_ROW = 5;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        FrameLayout root = backdrop();
        ScrollView scroll = new ScrollView(this);
        scroll.setClipChildren(false);

        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setPadding(dp(56), dp(32), dp(56), dp(48));
        page.setClipChildren(false);
        page.setClipToPadding(false);
        page.addView(text("ALL APPS", 20, YELLOW));

        PackageManager pm = getPackageManager();
        Intent query = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER);
        List<ResolveInfo> apps = pm.queryIntentActivities(query, 0);
        apps.sort((a, b) -> a.loadLabel(pm).toString().compareToIgnoreCase(b.loadLabel(pm).toString()));

        Set<String> seen = new HashSet<>();
        LinearLayout row = null;
        int n = 0;
        View first = null;
        for (ResolveInfo r : apps) {
            ActivityInfo a = r.activityInfo;
            if (a.packageName.equals(getPackageName()) || !seen.add(a.packageName + "/" + a.name)) continue;
            if (n % PER_ROW == 0) {
                row = row();
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                lp.topMargin = dp(n == 0 ? 28 : 18);
                page.addView(row, lp);
            }
            Drawable art = a.loadBanner(pm);
            if (art == null) art = a.loadIcon(pm);
            String label = r.loadLabel(pm).toString().toUpperCase();
            if (label.length() > 12) label = label.substring(0, 12);
            ComponentName cn = new ComponentName(a.packageName, a.name);
            LinearLayout t = tile(label, art, 144, 81, 8, () -> open(cn), null);
            row.addView(t, gap(n % PER_ROW == 0 ? 0 : 24));
            if (first == null) first = t.getChildAt(0);
            n++;
        }

        scroll.addView(page);
        root.addView(scroll, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        setContentView(root);
        if (first != null) first.requestFocus();
    }

    void open(ComponentName cn) {
        Intent i = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER)
                .setComponent(cn).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            startActivity(i);
        } catch (RuntimeException e) {
            toast("Can't open that app");
        }
    }
}
