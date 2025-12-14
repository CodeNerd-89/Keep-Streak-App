package com.tahmid.keepstreak;

import android.app.usage.UsageEvents;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;

import java.util.Calendar;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class UsageStatsHelper {

    public static long getLastTimeUsed(Context context, String packageName) {
        UsageStatsManager usm = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
        if (usm == null) return 0;

        long endTime = System.currentTimeMillis();
        long startTime = endTime - TimeUnit.DAYS.toMillis(7);

        List<UsageStats> stats =
                usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startTime, endTime);

        if (stats == null || stats.isEmpty()) return 0;

        long lastUsed = 0;
        for (UsageStats s : stats) {
            if (packageName.equals(s.getPackageName())) {
                if (s.getLastTimeUsed() > lastUsed) {
                    lastUsed = s.getLastTimeUsed();
                }
            }
        }
        return lastUsed;
    }

    public static int getDailyVisitCount(Context context, String packageName) {
        UsageStatsManager usm = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
        if (usm == null) return 0;

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long startTime = calendar.getTimeInMillis();
        long endTime = System.currentTimeMillis();

        UsageEvents usageEvents = usm.queryEvents(startTime, endTime);
        if (usageEvents == null) return 0;

        int count = 0;
        long lastEventTime = 0;
        UsageEvents.Event event = new UsageEvents.Event();
        while (usageEvents.hasNextEvent()) {
            usageEvents.getNextEvent(event);
            if (packageName.equals(event.getPackageName()) && event.getEventType() == UsageEvents.Event.ACTIVITY_RESUMED) {
                if (lastEventTime == 0 || (event.getTimeStamp() - lastEventTime) > 2000) {
                    count++;
                    lastEventTime = event.getTimeStamp();
                }
            }
        }
        return count;
    }

    public static Map<Integer, Long> getUsageByHour(Context context, String packageName, Calendar calendar) {
        UsageStatsManager usm = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
        if (usm == null) return new HashMap<>();

        Calendar startCal = (Calendar) calendar.clone();
        startCal.set(Calendar.HOUR_OF_DAY, 0);
        startCal.set(Calendar.MINUTE, 0);
        startCal.set(Calendar.SECOND, 0);
        startCal.set(Calendar.MILLISECOND, 0);
        long startTime = startCal.getTimeInMillis();

        Calendar endCal = (Calendar) calendar.clone();
        endCal.set(Calendar.HOUR_OF_DAY, 23);
        endCal.set(Calendar.MINUTE, 59);
        endCal.set(Calendar.SECOND, 59);
        endCal.set(Calendar.MILLISECOND, 999);
        long endTime = endCal.getTimeInMillis();

        UsageEvents usageEvents = usm.queryEvents(startTime, endTime);
        if (usageEvents == null) return new HashMap<>();

        Map<Integer, Long> usageByHour = new HashMap<>();
        Map<String, Long> appResumeTimes = new HashMap<>();

        UsageEvents.Event event = new UsageEvents.Event();
        while (usageEvents.hasNextEvent()) {
            usageEvents.getNextEvent(event);

            String currentPackage = event.getPackageName();
            if (!currentPackage.equals(packageName)) {
                continue;
            }

            int eventType = event.getEventType();
            long eventTimestamp = event.getTimeStamp();

            if (eventType == UsageEvents.Event.ACTIVITY_RESUMED) {
                appResumeTimes.put(currentPackage, eventTimestamp);
            } else if (eventType == UsageEvents.Event.ACTIVITY_PAUSED) {
                if (appResumeTimes.containsKey(currentPackage)) {
                    long resumeTime = appResumeTimes.remove(currentPackage);
                    long pauseTime = eventTimestamp;
                    distributeUsage(usageByHour, resumeTime, pauseTime);
                }
            }
        }

        for (Map.Entry<String, Long> entry : appResumeTimes.entrySet()) {
            String currentPackage = entry.getKey();
            if (currentPackage.equals(packageName)) {
                long resumeTime = entry.getValue();
                long pauseTime = endTime;
                distributeUsage(usageByHour, resumeTime, pauseTime);
            }
        }

        Map<Integer, Long> usageByHourInMinutes = new HashMap<>();
        for (Map.Entry<Integer, Long> entry : usageByHour.entrySet()) {
            usageByHourInMinutes.put(entry.getKey(), TimeUnit.MILLISECONDS.toMinutes(entry.getValue()));
        }

        return usageByHourInMinutes;
    }

    private static void distributeUsage(Map<Integer, Long> usageByHour, long resumeTime, long pauseTime) {
        if (pauseTime <= resumeTime) return;

        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(resumeTime);

        long current = resumeTime;
        while (current < pauseTime) {
            int hour = cal.get(Calendar.HOUR_OF_DAY);
            cal.add(Calendar.HOUR_OF_DAY, 1);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            long nextHour = cal.getTimeInMillis();

            long start = current;
            long end = Math.min(nextHour, pauseTime);
            long duration = end - start;

            if (duration > 0) {
                long currentUsage = usageByHour.getOrDefault(hour, 0L);
                usageByHour.put(hour, currentUsage + duration);
            }
            current = nextHour;
        }
    }
}
