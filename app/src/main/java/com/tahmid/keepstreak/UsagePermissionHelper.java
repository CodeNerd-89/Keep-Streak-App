package com.tahmid.keepstreak;

import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;

public class UsagePermissionHelper {

    public static boolean hasUsagePermission(Context context) {
        AppOpsManager appOps = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow("android:get_usage_stats",
                android.os.Process.myUid(), context.getPackageName());
        return mode == AppOpsManager.MODE_ALLOWED;
    }

    public static Intent getUsagePermissionIntent(Context context) {
        return new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
    }
}
