package com.tahmid.keepstreak;

import android.content.Context;

import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.util.concurrent.TimeUnit;

public class StreakReminderScheduler {

    private static final String UNIQUE_NAME = "streak_reminder_work";

    public static void scheduleDailyCheck(Context context) {
        PeriodicWorkRequest workRequest =
                new PeriodicWorkRequest.Builder(
                        StreakReminderWorker.class,
                        24, TimeUnit.HOURS
                ).build();

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                UNIQUE_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
        );
    }
}
