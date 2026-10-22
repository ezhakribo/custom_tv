package com.ibo.pixelhome;

/**
 * Prayer times calculated on the TV, no internet needed.
 * Kemenag RI criteria: Subuh -20 deg, Isya -18 deg, Ashar shadow factor 1, plus 2 minutes ihtiyat.
 */
public final class PrayerTimes {
    public static final String[] NAMES = {"SUBUH", "DZUHUR", "ASHAR", "MAGHRIB", "ISYA"};
    static final double FAJR_ANGLE = 20, ISHA_ANGLE = 18, IHTIYAT_MIN = 2;
    /** Height above sea level in metres (Bandung ~ 700 m): the sun sets later seen from up high. */
    static double elevation = 700;

    private PrayerTimes() { }

    /** Minutes after local midnight for Subuh, Dzuhur, Ashar, Maghrib, Isya. */
    public static int[] compute(int year, int month, int day, double lat, double lon, double tz) {
        double jd = julian(year, month, day) - lon / (15 * 24.0);
        double fajr = sunAngleTime(jd, lat, FAJR_ANGLE, 5 / 24.0, true);
        double dhuhr = midDay(jd, 12 / 24.0);
        double asr = asrTime(jd, lat, 1, 13 / 24.0);
        double maghrib = sunAngleTime(jd, lat, 0.833 + 0.0347 * Math.sqrt(Math.max(0, elevation)), 18 / 24.0, false);
        double isha = sunAngleTime(jd, lat, ISHA_ANGLE, 18 / 24.0, false);
        double[] t = {fajr, dhuhr, asr, maghrib, isha};
        int[] out = new int[5];
        for (int i = 0; i < 5; i++) {
            double local = t[i] + tz - lon / 15.0;              // hours
            out[i] = (int) Math.ceil(local * 60 - 1e-6 + IHTIYAT_MIN + (i == 1 || i == 3 ? 1 : 0));   // Dzuhur, Maghrib +1 like Kemenag
        }
        return out;
    }

    /** Index of the next prayer at minute-of-day {@code now}, or 0 (tomorrow's Subuh) after Isya. */
    public static int next(int[] times, int now) {
        for (int i = 0; i < times.length; i++) if (times[i] > now) return i;
        return 0;
    }

    public static String hhmm(int minutes) {
        return String.format(java.util.Locale.ROOT, "%02d:%02d", (minutes / 60) % 24, minutes % 60);
    }

    // ---- astronomy (after praytimes.org) ----
    static double julian(int y, int m, int d) {
        if (m <= 2) { y -= 1; m += 12; }
        double a = Math.floor(y / 100.0);
        double b = 2 - a + Math.floor(a / 4);
        return Math.floor(365.25 * (y + 4716)) + Math.floor(30.6001 * (m + 1)) + d + b - 1524.5;
    }

    static double[] sun(double jd) {
        double d = jd - 2451545.0;
        double g = fixAngle(357.529 + 0.98560028 * d);
        double q = fixAngle(280.459 + 0.98564736 * d);
        double l = fixAngle(q + 1.915 * dsin(g) + 0.020 * dsin(2 * g));
        double e = 23.439 - 0.00000036 * d;
        double ra = darctan2(dcos(e) * dsin(l), dcos(l)) / 15;
        double eqt = q / 15 - fixHour(ra);
        double decl = darcsin(dsin(e) * dsin(l));
        return new double[]{decl, eqt};
    }

    static double midDay(double jd, double t) {
        return fixHour(12 - sun(jd + t)[1]);
    }

    static double sunAngleTime(double jd, double lat, double angle, double t, boolean beforeNoon) {
        double decl = sun(jd + t)[0];
        double noon = midDay(jd, t);
        double x = (-dsin(angle) - dsin(decl) * dsin(lat)) / (dcos(decl) * dcos(lat));
        double h = darccos(Math.max(-1, Math.min(1, x))) / 15;
        return noon + (beforeNoon ? -h : h);
    }

    static double asrTime(double jd, double lat, double factor, double t) {
        double decl = sun(jd + t)[0];
        double angle = -darccot(factor + dtan(Math.abs(lat - decl)));
        return sunAngleTime(jd, lat, angle, t, false);
    }

    static double dsin(double d) { return Math.sin(Math.toRadians(d)); }
    static double dcos(double d) { return Math.cos(Math.toRadians(d)); }
    static double dtan(double d) { return Math.tan(Math.toRadians(d)); }
    static double darcsin(double x) { return Math.toDegrees(Math.asin(x)); }
    static double darccos(double x) { return Math.toDegrees(Math.acos(x)); }
    static double darctan2(double y, double x) { return Math.toDegrees(Math.atan2(y, x)); }
    static double darccot(double x) { return Math.toDegrees(Math.atan(1 / x)); }
    static double fixAngle(double a) { a = a - 360 * Math.floor(a / 360); return a < 0 ? a + 360 : a; }
    static double fixHour(double a) { a = a - 24 * Math.floor(a / 24); return a < 0 ? a + 24 : a; }
}
