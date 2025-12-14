package com.tahmid.keepstreak;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.tahmid.keepstreak.databinding.ItemTrackedAppBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AppListAdapter extends RecyclerView.Adapter<AppListAdapter.ViewHolder> {

    private final Context context;
    private List<ResolveInfo> apps;
    private final List<ResolveInfo> originalApps;
    private final OnAppClickListener listener;
    private final PackageManager pm;

    public interface OnAppClickListener {
        void onAppClick(ResolveInfo app);
    }

    public AppListAdapter(Context context, List<ResolveInfo> apps, OnAppClickListener listener) {
        this.context = context;
        this.apps = new ArrayList<>(apps);
        this.originalApps = new ArrayList<>(apps);
        this.listener = listener;
        this.pm = context.getPackageManager();
    }

    public void filter(String query) {
        apps.clear();
        if (query.isEmpty()) {
            apps.addAll(originalApps);
        } else {
            apps.addAll(originalApps.stream()
                    .filter(app -> app.loadLabel(pm).toString().toLowerCase().contains(query.toLowerCase()))
                    .collect(Collectors.toList()));
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemTrackedAppBinding binding = ItemTrackedAppBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ResolveInfo app = apps.get(position);
        holder.bind(app, listener);
    }

    @Override
    public int getItemCount() {
        return apps.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemTrackedAppBinding binding;

        public ViewHolder(ItemTrackedAppBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            binding.txtStreak.setVisibility(View.GONE);
            binding.btnDelete.setVisibility(View.GONE);
        }

        public void bind(final ResolveInfo app, final OnAppClickListener listener) {
            binding.txtAppName.setText(app.loadLabel(context.getPackageManager()));
            binding.imgIcon.setImageDrawable(app.loadIcon(context.getPackageManager()));
            itemView.setOnClickListener(v -> listener.onAppClick(app));
        }
    }
}
