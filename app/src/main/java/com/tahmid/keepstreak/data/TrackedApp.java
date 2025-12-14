package com.tahmid.keepstreak.data;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "tracked_apps")
public class TrackedApp {

    @PrimaryKey
    @NonNull
    public String packageName;

    public String appName;
    public int streakCount;
    public int lastStreakDay; // yyyyMMdd
    public int dailyVisitCount;

    public TrackedApp(@NonNull String packageName, String appName) {
        this.packageName = packageName;
        this.appName = appName;
        this.streakCount = 0;
        this.lastStreakDay = 0;
        this.dailyVisitCount = 0;
    }
}
