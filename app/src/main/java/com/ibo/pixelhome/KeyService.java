package com.ibo.pixelhome;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Toast;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Lets remote buttons you teach it open things:
 *   home    -> Pixel Home
 *   netflix -> Netflix
 *   disney  -> Disney+
 *   (or any package name)
 * Every other button passes through untouched.
 */
public class KeyService extends AccessibilityService {
    static final String TAG = "PixelHomeKeys";
    static final String PREFS = "pixelhome";
    static final String LEARN = "learn_target";   // what the next pressed button should open
    static final String MAP = "key_";             // key_<keycode> -> target

    static final Map<String, String> ALIASES = new HashMap<>();
    static {
        ALIASES.put("netflix", "com.netflix.ninja");
        ALIASES.put("disney", "com.disney.disneyplus");
        ALIASES.put("prime", "com.amazon.amazonvideo.livingroom");
        ALIASES.put("youtube", "com.teamsmart.videomanager.tv");
        ALIASES.put("spotify", "com.spotify.tv.android");
    }

    // Never taken over: navigation, volume, power, and the Google voice / search buttons.
    static final Set<Integer> NEVER = new HashSet<>(Arrays.asList(
            KeyEvent.KEYCODE_DPAD_UP, KeyEvent.KEYCODE_DPAD_DOWN, KeyEvent.KEYCODE_DPAD_LEFT,
            KeyEvent.KEYCODE_DPAD_RIGHT, KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER,
            KeyEvent.KEYCODE_BACK, KeyEvent.KEYCODE_HOME, KeyEvent.KEYCODE_VOLUME_UP,
            KeyEvent.KEYCODE_VOLUME_DOWN, KeyEvent.KEYCODE_VOLUME_MUTE, KeyEvent.KEYCODE_POWER,
            KeyEvent.KEYCODE_SEARCH, KeyEvent.KEYCODE_ASSIST, KeyEvent.KEYCODE_VOICE_ASSIST));

    @Override
    protected boolean onKeyEvent(KeyEvent e) {
        int code = e.getKeyCode();
        boolean up = e.getAction() == KeyEvent.ACTION_UP;
        if (e.getAction() == KeyEvent.ACTION_DOWN && e.getRepeatCount() == 0) {
            Log.i(TAG, "key " + code + " " + KeyEvent.keyCodeToString(code));
        }
        if (NEVER.contains(code)) return false;
        SharedPreferences p = getSharedPreferences(PREFS, MODE_PRIVATE);

        String learning = p.getString(LEARN, null);
        if (learning != null) {
            if (up) {
                p.edit().putString(MAP + code, learning).remove(LEARN).apply();
                Toast.makeText(this, "Learned! This button now opens " + learning
                        + " (" + KeyEvent.keyCodeToString(code) + ")", Toast.LENGTH_LONG).show();
                open(learning);
            }
            return true;
        }
        String target = p.getString(MAP + code, null);
        if (target != null) {
            if (up) open(target);
            return true;   // swallow it so the old handler doesn't also react
        }
        return false;
    }

    void open(String target) {
        Intent i;
        if ("home".equals(target)) {
            i = new Intent(this, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        } else {
            String pkg = ALIASES.containsKey(target) ? ALIASES.get(target) : target;
            PackageManager pm = getPackageManager();
            i = pm.getLeanbackLaunchIntentForPackage(pkg);
            if (i == null) i = pm.getLaunchIntentForPackage(pkg);
            if (i == null) {
                Toast.makeText(this, target + " isn't installed or is turned off", Toast.LENGTH_LONG).show();
                return;
            }
        }
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            startActivity(i);
        } catch (RuntimeException ex) {
            Log.w(TAG, "can't open " + target, ex);
        }
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) { }

    @Override
    public void onInterrupt() { }
}
