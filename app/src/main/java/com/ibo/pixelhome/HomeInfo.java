package com.ibo.pixelhome;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/** Greeting, date, weather and prayer time, shared by the home screen and the screensaver. */
final class HomeInfo {
    // Location: Bandung. Change these for another city.
    static final double LAT = -6.9175, LON = 107.6191, TZ = 7;
    static final Locale ID = new Locale("id", "ID");

    private HomeInfo() { }

    static SharedPreferences prefs(Context c) {
        return c.getSharedPreferences(KeyService.PREFS, Context.MODE_PRIVATE);
    }

    static String greeting(Context c) {
        int h = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        String part = h >= 4 && h < 10 ? "PAGI" : h < 15 && h >= 10 ? "SIANG" : h >= 15 && h < 18 ? "SORE" : "MALAM";
        String name = prefs(c).getString("name", "IBO").trim();
        return "SELAMAT " + part + (name.isEmpty() ? "" : ", " + name.toUpperCase(ID));
    }

    static String date() {
        return new SimpleDateFormat("EEEE, d MMMM", ID).format(new Date()).toUpperCase(ID);
    }

    static String clock() {
        return new SimpleDateFormat("HH:mm", Locale.ROOT).format(new Date());
    }

    /** e.g. "ASHAR 14:46" */
    static String nextPrayer() {
        Calendar c = Calendar.getInstance();
        int now = c.get(Calendar.HOUR_OF_DAY) * 60 + c.get(Calendar.MINUTE);
        int[] t = PrayerTimes.compute(c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH), LAT, LON, TZ);
        int i = PrayerTimes.next(t, now);
        if (i == 0 && now >= t[4]) {           // after Isya: tomorrow's Subuh
            c.add(Calendar.DAY_OF_MONTH, 1);
            t = PrayerTimes.compute(c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH), LAT, LON, TZ);
        }
        return PrayerTimes.NAMES[i] + " " + PrayerTimes.hhmm(t[i]);
    }

    // ---------- weather ----------
    interface WeatherListener { void onWeather(int tempC, int code, boolean isDay); }

    static final long REFRESH_MS = 30 * 60 * 1000L;

    /** Shows the saved reading right away, then refreshes from the internet if it is older than 30 minutes. */
    static void weather(Context c, WeatherListener l) {
        SharedPreferences p = prefs(c);
        if (p.contains("w_temp")) l.onWeather(p.getInt("w_temp", 0), p.getInt("w_code", 0), p.getBoolean("w_day", true));
        if (System.currentTimeMillis() - p.getLong("w_time", 0) < REFRESH_MS) return;
        Handler main = new Handler(Looper.getMainLooper());
        new Thread(() -> {
            try {
                URL url = new URL("https://api.open-meteo.com/v1/forecast?latitude=" + LAT + "&longitude=" + LON
                        + "&current=temperature_2m,weather_code,is_day&timezone=auto");
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setConnectTimeout(8000);
                con.setReadTimeout(8000);
                try (InputStream in = con.getInputStream()) {
                    ByteArrayOutputStream buf = new ByteArrayOutputStream();
                    byte[] chunk = new byte[4096];
                    for (int n; (n = in.read(chunk)) > 0; ) buf.write(chunk, 0, n);
                    JSONObject cur = new JSONObject(buf.toString("UTF-8")).getJSONObject("current");
                    int temp = (int) Math.round(cur.getDouble("temperature_2m"));
                    int code = cur.getInt("weather_code");
                    boolean isDay = cur.optInt("is_day", 1) == 1;
                    p.edit().putInt("w_temp", temp).putInt("w_code", code).putBoolean("w_day", isDay)
                            .putLong("w_time", System.currentTimeMillis()).apply();
                    main.post(() -> l.onWeather(temp, code, isDay));
                } finally {
                    con.disconnect();
                }
            } catch (Exception e) {
                Log.w(MainActivity.TAG, "weather failed", e);
            }
        }).start();
    }

    // ---------- 8x8 weather icons: '#' main colour, 'y' yellow, 'b' blue ----------
    static final String[] SUN = {"..#..#..", "...##...", "#.####.#", ".######.", ".######.", "#.####.#", "...##...", "..#..#.."};
    static final String[] MOON = {"..###...", ".##.....", "##......", "##......", "##......", "##......", ".##.....", "..###..."};
    static final String[] PARTLY = {"y.y.....", ".yy.....", "yyy##...", ".y####..", ".######.", "########", "########", ".######."};
    static final String[] CLOUD = {"........", "...##...", "..####..", ".######.", "########", "########", ".######.", "........"};
    static final String[] RAIN = {"...##...", "..####..", ".######.", "########", ".######.", "........", ".b..b..b", "b..b..b."};
    static final String[] STORM = {"...##...", "..####..", ".######.", "########", ".##yy##.", "...yy...", "..yy....", "..y....."};
    static final String[] FOG = {"........", "######..", "........", "..######", "........", "######..", "........", "..######"};

    static String[] icon(int code, boolean isDay) {
        if (code == 0) return isDay ? SUN : MOON;
        if (code <= 2) return isDay ? PARTLY : CLOUD;
        if (code == 3) return CLOUD;
        if (code == 45 || code == 48) return FOG;
        if (code >= 95) return STORM;
        return RAIN;
    }

    static int iconColor(int code, boolean isDay) {
        if (code == 0) return isDay ? 0xFFFFD84A : 0xFFF6EFC4;
        if (code >= 95) return 0xFF9A9AB0;
        return 0xFFF0F0F8;
    }
}
