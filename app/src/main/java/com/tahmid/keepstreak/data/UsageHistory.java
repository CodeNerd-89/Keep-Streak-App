package com.tahmid.keepstreak.data;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "usage_history")
public class UsageHistory {

    @PrimaryKey(autoGenerate = true)
    public int id;

    @NonNull
    public String packageName;

    public long date; // yyyyMMdd
    public long usageTime; // in minutes

    public UsageHistory(@NonNull String packageName, long date, long usageTime) {
        this.packageName = packageName;
        this.date = date;
        this.usageTime = usageTime;
    }
}
