package com.example.mydaibanapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mydaibanapp.R;

public class TaskGroupHeaderAdapter extends RecyclerView.Adapter<TaskGroupHeaderAdapter.HeaderViewHolder> {
    private final String title;
    private int count;

    public TaskGroupHeaderAdapter(String title) {
        this.title = title;
    }

    public void submitCount(int count) {
        boolean wasVisible = getItemCount() > 0;
        this.count = count;
        boolean isVisible = getItemCount() > 0;
        if (wasVisible != isVisible) {
            notifyDataSetChanged();
        } else if (isVisible) {
            notifyItemChanged(0);
        }
    }

    @NonNull
    @Override
    public HeaderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_task_group_header, parent, false);
        return new HeaderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HeaderViewHolder holder, int position) {
        holder.tvTitle.setText(title + " " + count);
    }

    @Override
    public int getItemCount() {
        return count > 0 ? 1 : 0;
    }

    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle;

        HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTaskGroupTitle);
        }
    }
}
