package com.tahmid.keepstreak;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.tahmid.keepstreak.data.AppDatabase;
import com.tahmid.keepstreak.data.TrackedApp;
import com.tahmid.keepstreak.data.TrackedAppDao; 
import com.tahmid.keepstreak.data.UsageHistory;
import com.tahmid.keepstreak.data.UsageHistoryDao; 
import com.tahmid.keepstreak.UsageStatsHelper; 

import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class UsageStatsWorker extends Worker {

    public UsageStatsWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context context = getApplicationContext();
        AppDatabase db = AppDatabase.getInstance(context);
        TrackedAppDao trackedAppDao = db.trackedAppDao();
        UsageHistoryDao usageHistoryDao = db.usageHistoryDao();

        List<TrackedApp> trackedApps = trackedAppDao.getAll();

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -1);
        long date = calendar.get(Calendar.YEAR) * 10000L + (calendar.get(Calendar.MONTH) + 1) * 100L + calendar.get(Calendar.DAY_OF_MONTH);

        for (TrackedApp app : trackedApps) {
            Map<Integer, Long> usageByHour = UsageStatsHelper.getUsageByHour(context, app.packageName, calendar);
            long totalUsage = 0;
            for (long usage : usageByHour.values()) {
                totalUsage += usage;
            }
            usageHistoryDao.insert(new UsageHistory(app.packageName, date, totalUsage));
        }

        return Result.success();
    }
}
