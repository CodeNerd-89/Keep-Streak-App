package com.tahmid.keepstreak;

import android.app.usage.UsageEvents;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.material.appbar.MaterialToolbar;
import com.tahmid.keepstreak.data.AppDatabase;
import com.tahmid.keepstreak.data.TrackedApp;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PieChartActivity extends AppCompatActivity {

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pie_chart);


        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        BarChart barChart = findViewById(R.id.bar_chart_activity);
        setupBarChart(barChart);

        if (UsagePermissionHelper.hasUsagePermission(this)) {
            loadUsageData(barChart);
        } else {
            Toast.makeText(this, "Usage access not granted", Toast.LENGTH_LONG).show();
            Intent intent = UsagePermissionHelper.getUsagePermissionIntent(this);
            startActivity(intent);
        }
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void setupBarChart(BarChart barChart) {
        barChart.getDescription().setEnabled(false);
        barChart.setDrawGridBackground(false);
        barChart.setDrawValueAboveBar(true);

        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setTextColor(getColor(R.color.dark_orange));

        YAxis leftAxis = barChart.getAxisLeft();
        leftAxis.setDrawGridLines(false);
        leftAxis.setAxisMinimum(0f);
        leftAxis.setTextColor(getColor(R.color.dark_orange));

        YAxis rightAxis = barChart.getAxisRight();
        rightAxis.setDrawGridLines(false);
        rightAxis.setDrawLabels(false);
    }

    private Map<String, Integer> getDailyVisitCountForAllApps(Context context) {
        UsageStatsManager usm = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
        if (usm == null) return new HashMap<>();

        Calendar calendar = Calendar.getInstance();
        long endTime = calendar.getTimeInMillis();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long startTime = calendar.getTimeInMillis();

        UsageEvents usageEvents = usm.queryEvents(startTime, endTime);
        if (usageEvents == null) return new HashMap<>();

        Map<String, Integer> visitCounts = new HashMap<>();
        Map<String, Long> lastEventTimes = new HashMap<>();
        UsageEvents.Event event = new UsageEvents.Event();
        while (usageEvents.hasNextEvent()) {
            usageEvents.getNextEvent(event);
            if (event.getEventType() == UsageEvents.Event.ACTIVITY_RESUMED) {
                String packageName = event.getPackageName();
                long lastEventTime = lastEventTimes.getOrDefault(packageName, 0L);
                if (lastEventTime == 0 || (event.getTimeStamp() - lastEventTime) > 2000) {
                    int count = visitCounts.getOrDefault(packageName, 0);
                    visitCounts.put(packageName, count + 1);
                }
                lastEventTimes.put(packageName, event.getTimeStamp());
            }
        }
        return visitCounts;
    }

    private void loadUsageData(BarChart barChart) {
        executorService.execute(() -> {
            AppDatabase db = AppDatabase.getInstance(this);
            List<TrackedApp> trackedApps = db.trackedAppDao().getAll();
            Set<String> trackedPackages = new HashSet<>();
            for (TrackedApp app : trackedApps) {
                trackedPackages.add(app.packageName);
            }

            Map<String, Integer> visitCounts = getDailyVisitCountForAllApps(this);

            if (visitCounts.isEmpty()) {
                return;
            }
            
            List<Map.Entry<String, Integer>> sortedApps = new ArrayList<>();
            for(Map.Entry<String, Integer> entry : visitCounts.entrySet()) {
                if(trackedPackages.contains(entry.getKey())) {
                    sortedApps.add(entry);
                }
            }

            Collections.sort(sortedApps, (a, b) -> b.getValue().compareTo(a.getValue()));

            ArrayList<BarEntry> entries = new ArrayList<>();
            ArrayList<String> labels = new ArrayList<>();
            int count = 0;
            for (Map.Entry<String, Integer> entry : sortedApps) {
                String packageName = entry.getKey();
                int visitCount = entry.getValue();

                try {
                    PackageManager pm = getPackageManager();
                    String appName = pm.getApplicationLabel(pm.getApplicationInfo(packageName, PackageManager.GET_META_DATA)).toString();

                    entries.add(new BarEntry(count, visitCount));
                    labels.add(appName);

                    count++;
                    if (count >= 5) {
                        break;
                    }
                } catch (PackageManager.NameNotFoundException e) {
                    // Ignore this app
                }
            }
            
            runOnUiThread(() -> {
                XAxis xAxis = barChart.getXAxis();
                xAxis.setLabelCount(labels.size());
                xAxis.setValueFormatter(new ValueFormatter() {
                    @Override
                    public String getFormattedValue(float value) {
                        if(value >= 0 && value < labels.size()){
                            return labels.get((int) value);
                        }
                        return "";
                    }
                });
                xAxis.setLabelRotationAngle(-45);


                BarDataSet dataSet = new BarDataSet(entries, "Daily Visits");
                dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
                dataSet.setDrawValues(true);
                dataSet.setValueFormatter(new CountValueFormatter());

                BarData data = new BarData(dataSet);
                data.setValueTextSize(12f);
                data.setValueTextColor(getColor(R.color.dark_orange));
                data.setBarWidth(0.5f);

                barChart.setData(data);
                barChart.getLegend().setTextColor(getColor(R.color.dark_red));
                barChart.invalidate();
            });
        });
    }

    public static class CountValueFormatter extends ValueFormatter {
        @Override
        public String getFormattedValue(float value) {
            return String.valueOf((int) value);
        }
    }
}
