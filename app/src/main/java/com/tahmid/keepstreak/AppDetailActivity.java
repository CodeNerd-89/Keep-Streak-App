package com.tahmid.keepstreak;

import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import com.tahmid.keepstreak.data.TrackedApp;
import com.tahmid.keepstreak.databinding.ActivityAppDetailBinding;

import com.tahmid.keepstreak.data.AppDatabase;

import java.util.Calendar;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AppDetailActivity extends AppCompatActivity {

    public static final String EXTRA_PACKAGE_NAME = "extra_package_name";
    public static final String EXTRA_APP_NAME = "extra_app_name";

    private ActivityAppDetailBinding binding;
    private String packageName;
    private String appName;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAppDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        packageName = getIntent().getStringExtra(EXTRA_PACKAGE_NAME);
        appName = getIntent().getStringExtra(EXTRA_APP_NAME);

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(appName);
        }

        try {
            Drawable icon = getPackageManager().getApplicationIcon(packageName);
            binding.appIconHeader.setImageDrawable(icon);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        loadUsageData();
    }

    private void loadUsageData() {
        executorService.execute(() -> {
            AppDatabase db = AppDatabase.getInstance(this);
            TrackedApp trackedApp = db.trackedAppDao().findByPackageName(packageName);

            Map<Integer, Long> usageByHour = UsageStatsHelper.getUsageByHour(this, packageName, Calendar.getInstance());
            long totalUsage = 0;
            for (long usage : usageByHour.values()) {
                totalUsage += usage;
            }
            int dailyVisits = UsageStatsHelper.getDailyVisitCount(this, packageName);

            long finalTotalUsage = totalUsage;
            runOnUiThread(() -> {
                binding.txtTotalUsage.setText(formatUsageTime(finalTotalUsage));
                binding.txtDailyVisits.setText(String.valueOf(dailyVisits));
                if (trackedApp != null) {
                    binding.txtStreak.setText(trackedApp.streakCount + " days");
                } else {
                    binding.txtStreak.setText("Not tracked");
                }
            });
        });
    }

    private String formatUsageTime(long usageInMinutes) {
        long hours = usageInMinutes / 60;
        long minutes = usageInMinutes % 60;
        if (hours > 0) {
            return hours + "h " + minutes + "m";
        } else {
            return minutes + "m";
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}