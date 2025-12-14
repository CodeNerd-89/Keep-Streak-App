package com.tahmid.keepstreak;

import android.content.Context;
import android.content.SharedPreferences;

public class StreakStorage {

    private static final String PREF_NAME = "streak_prefs";
    private static final String KEY_PACKAGE = "tracked_package";
    private static final String KEY_STREAK = "streak_count";
    private static final String KEY_LAST_DAY = "last_streak_day";

    private final SharedPreferences prefs;

    public StreakStorage(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void setTrackedPackage(String pkg) {
        prefs.edit().putString(KEY_PACKAGE, pkg).apply();
    }

    public String getTrackedPackage() {
        return prefs.getString(KEY_PACKAGE, null);
    }

    public void setStreak(int streak) {
        prefs.edit().putInt(KEY_STREAK, streak).apply();
    }

    public int getStreak() {
        return prefs.getInt(KEY_STREAK, 0);
    }

    public void setLastStreakDay(int day) {
        prefs.edit().putInt(KEY_LAST_DAY, day).apply();
    }

    public int getLastStreakDay() {
        return prefs.getInt(KEY_LAST_DAY, 0);
    }
}
