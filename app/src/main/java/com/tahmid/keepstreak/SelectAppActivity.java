package com.tahmid.keepstreak;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.tahmid.keepstreak.data.AppDatabase;
import com.tahmid.keepstreak.data.TrackedApp;
import com.tahmid.keepstreak.databinding.ActivitySelectAppBinding;

import java.util.List;
import java.util.concurrent.Executors;

public class SelectAppActivity extends AppCompatActivity {

    private ActivitySelectAppBinding binding;
    private AppListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySelectAppBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        AppDatabase db = AppDatabase.getInstance(this);
        PackageManager pm = getPackageManager();
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

        List<ResolveInfo> appList = pm.queryIntentActivities(mainIntent, 0);

        adapter = new AppListAdapter(this, appList, app -> {
            String pkg = app.activityInfo.packageName;
            String appName = app.loadLabel(pm).toString();

            Executors.newSingleThreadExecutor().execute(() -> {
                TrackedApp trackedApp = db.trackedAppDao().findByPackageName(pkg);
                if (trackedApp == null) {
                    db.trackedAppDao().insert(new TrackedApp(pkg, appName));
                }
            });

            Toast.makeText(this, "Added " + appName + " to streaks", Toast.LENGTH_SHORT).show();
            finish();
        });

        binding.listApps.setLayoutManager(new LinearLayoutManager(this));
        binding.listApps.setAdapter(adapter);

        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                adapter.filter(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.filter(newText);
                return true;
            }
        });
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
