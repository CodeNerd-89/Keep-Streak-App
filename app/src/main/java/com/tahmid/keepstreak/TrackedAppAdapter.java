package com.tahmid.keepstreak;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.tahmid.keepstreak.data.TrackedApp;
import com.tahmid.keepstreak.databinding.ItemTrackedAppBinding;

import java.util.ArrayList;
import java.util.List;

public class TrackedAppAdapter extends RecyclerView.Adapter<TrackedAppAdapter.ViewHolder> {

    private List<TrackedApp> items = new ArrayList<>();
    private final PackageManager pm;
    private final OnDeleteClickListener onDeleteClickListener;
    private final OnItemClickListener onItemClickListener;

    public interface OnDeleteClickListener {
        void onDeleteClick(TrackedApp app);
    }

    public interface OnItemClickListener {
        void onItemClick(TrackedApp app);
    }

    public TrackedAppAdapter(Context context, OnDeleteClickListener onDeleteClickListener, OnItemClickListener onItemClickListener) {
        this.pm = context.getPackageManager();
        this.onDeleteClickListener = onDeleteClickListener;
        this.onItemClickListener = onItemClickListener;
    }

    public void setItems(List<TrackedApp> newItems) {
        items = newItems;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TrackedAppAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemTrackedAppBinding binding = ItemTrackedAppBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull TrackedAppAdapter.ViewHolder holder, int position) {
        TrackedApp app = items.get(position);
        holder.bind(app, onDeleteClickListener, onItemClickListener);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ItemTrackedAppBinding binding;

        ViewHolder(ItemTrackedAppBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(TrackedApp app, OnDeleteClickListener deleteListener, OnItemClickListener itemListener) {
            binding.txtAppName.setText(app.appName);
            binding.txtStreak.setText(String.valueOf(app.streakCount));
            binding.txtDailyVisitCount.setText("Visits: " + app.dailyVisitCount);
            binding.btnDelete.setOnClickListener(v -> deleteListener.onDeleteClick(app));
            itemView.setOnClickListener(v -> itemListener.onItemClick(app));

            try {
                ApplicationInfo ai = itemView.getContext().getPackageManager().getApplicationInfo(app.packageName, 0);
                Drawable icon = itemView.getContext().getPackageManager().getApplicationIcon(ai);
                binding.imgIcon.setImageDrawable(icon);
            } catch (PackageManager.NameNotFoundException e) {
                binding.imgIcon.setImageResource(android.R.drawable.sym_def_app_icon);
            }
        }
    }
}
