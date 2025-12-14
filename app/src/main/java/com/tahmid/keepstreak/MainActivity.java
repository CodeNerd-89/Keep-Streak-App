package com.tahmid.keepstreak;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.snackbar.Snackbar;
import com.tahmid.keepstreak.data.AppDatabase;
import com.tahmid.keepstreak.data.TrackedApp;
import com.tahmid.keepstreak.databinding.ActivityMainBinding;

import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements TrackedAppAdapter.OnDeleteClickListener, TrackedAppAdapter.OnItemClickListener {

    private ActivityMainBinding binding;
    private TrackedAppAdapter adapter;
    private AppDatabase db;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("");
        }

        db = AppDatabase.getInstance(this);

        binding.recyclerStreaks.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TrackedAppAdapter(this, this, this);
        binding.recyclerStreaks.setAdapter(adapter);

        binding.addButtonMain.setOnClickListener(v -> {
            Intent intent = new Intent(this, SelectAppActivity.class);
            startActivity(intent);
        });

        binding.pieChartButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, PieChartActivity.class);
            startActivity(intent);
        });

        scheduleUsageStatsWorker();
    }

    private void scheduleUsageStatsWorker() {
        PeriodicWorkRequest usageStatsWorkRequest =
                new PeriodicWorkRequest.Builder(UsageStatsWorker.class, 1, TimeUnit.DAYS)
                        .build();

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "usageStatsWorker",
                ExistingPeriodicWorkPolicy.KEEP,
                usageStatsWorkRequest);
    }

    @Override
    protected void onResume() {
        super.onResume();
        syncAndRefresh();
    }

    private void syncAndRefresh() {
        executorService.execute(() -> {
            syncInstalledUserAppsToDb();
            refreshAll();
        });
    }
    private void refreshAll() {
        boolean hasPerm = UsagePermissionHelper.hasUsagePermission(this);
        if (!hasPerm) {
            runOnUiThread(() -> {
                Snackbar.make(binding.getRoot(), "Usage access not granted", Snackbar.LENGTH_INDEFINITE)
                        .setAction("Grant", v -> {
                            Intent intent = UsagePermissionHelper.getUsagePermissionIntent(this);
                            startActivity(intent);
                        })
                        .show();
            });
        }
        List<TrackedApp> apps = db.trackedAppDao().getAll();

        if (hasPerm) {
            for (TrackedApp app : apps) {
                long lastUsed = UsageStatsHelper.getLastTimeUsed(this, app.packageName);
                if (lastUsed == 0) continue;

                int newDay = toDayNumber(lastUsed);
                int lastDay = app.lastStreakDay;
                if (lastDay == 0) {
                    app.streakCount = 1;
                    app.lastStreakDay = newDay;
                } else if (newDay == lastDay) {
                    // already counted today
                } else if (newDay == lastDay + 1) {
                    app.streakCount += 1;
                    app.lastStreakDay = newDay;
                } else if (newDay > lastDay) {
                    app.streakCount = 1;
                    app.lastStreakDay = newDay;
                }
                app.dailyVisitCount = UsageStatsHelper.getDailyVisitCount(this, app.packageName);
                db.trackedAppDao().update(app);
            }
            apps = db.trackedAppDao().getAll(); // reload updated
        }
        List<TrackedApp> finalApps = apps;
        int count = apps.size();
        runOnUiThread(() -> {
            adapter.setItems(finalApps);
            //binding.toolbar.setTitle("Keep Streak");
            //binding.collapsingToolbar.setTitle("Tracked apps: " + count);
        });
    }
    private void syncInstalledUserAppsToDb() {
        PackageManager pm = getPackageManager();
        List<TrackedApp> trackedApps = db.trackedAppDao().getAll();
        for (TrackedApp trackedApp : trackedApps) {
            try {
                pm.getPackageInfo(trackedApp.packageName, 0);
            } catch (PackageManager.NameNotFoundException e) {
                db.trackedAppDao().delete(trackedApp);
            }
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

    @Override
    public void onDeleteClick(TrackedApp app) {
        new AlertDialog.Builder(this, R.style.AlertDialogTheme)
                .setTitle("Delete " + app.appName)
                .setMessage("Are you sure you want to delete this app? All data for this app will be lost.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    executorService.execute(() -> {
                        db.trackedAppDao().delete(app);
                        refreshAll();
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onItemClick(TrackedApp app) {
        Intent intent = new Intent(this, AppDetailActivity.class);
        intent.putExtra(AppDetailActivity.EXTRA_PACKAGE_NAME, app.packageName);
        intent.putExtra(AppDetailActivity.EXTRA_APP_NAME, app.appName);
        startActivity(intent);
    }
}