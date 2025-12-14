package com.tahmid.keepstreak;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.tahmid.keepstreak.data.AppDatabase;
import com.tahmid.keepstreak.data.TrackedApp;

import java.util.Calendar;
import java.util.List;

public class StreakReminderWorker extends Worker {

    private static final String CHANNEL_ID = "streak_reminder_channel";

    public StreakReminderWorker(@NonNull Context context,
                                @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context context = getApplicationContext();
        AppDatabase db = AppDatabase.getInstance(context);

        int today = toDayNumber(System.currentTimeMillis());
        int yesterday = today - 1;

        List<TrackedApp> apps = db.trackedAppDao().getAll();

        createChannel(context);

        NotificationManager nm =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        int id = 1000;
        for (TrackedApp app : apps) {
            if (app.lastStreakDay == yesterday) {
                NotificationCompat.Builder builder =
                        new NotificationCompat.Builder(context, CHANNEL_ID)
                                .setSmallIcon(android.R.drawable.ic_dialog_info)
                                .setContentTitle("Don't lose your streak!")
                                .setContentText("Open " + app.appName + " to keep your streak alive.")
                                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

                nm.notify(id++, builder.build());
            }
        }

        return Result.success();
    }

    private void createChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String name = "Streak reminders";
            String desc = "Notifications to keep your app streaks";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel =
                    new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(desc);
            NotificationManager nm =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            nm.createNotificationChannel(channel);
        }
    }

    private int toDayNumber(long timeMillis) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timeMillis);
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH) + 1;
        int day = cal.get(Calendar.DAY_OF_MONTH);
        return year * 10000 + month * 100 + day;
    }
}

