package com.example.mydaibanapp.adapter;

import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mydaibanapp.R;
import com.example.mydaibanapp.data.Task;

import java.util.Calendar;
import java.util.Locale;

public class TaskAdapter extends ListAdapter<Task, TaskAdapter.TaskViewHolder> {
    private OnTaskClickListener listener;

    public interface OnTaskClickListener {
        void onTaskClick(Task task);
        void onTaskToggle(Task task, boolean isCompleted);
        void onTaskDelete(Task task);
    }

    public TaskAdapter(OnTaskClickListener listener) {
        super(new TaskDiffCallback());
        this.listener = listener;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = getItem(position);
        holder.tvTitle.setText(task.getTitle());
        holder.tvDescription.setText(task.getDescription() != null ? task.getDescription() : "无描述");
        holder.cbCompleted.setOnCheckedChangeListener(null);
        holder.cbCompleted.setChecked(task.isCompleted());

        if (task.getReminderAt() != null) {
            holder.tvReminder.setVisibility(View.VISIBLE);
            holder.tvReminder.setText(formatReminderText(task.getReminderAt()));
        } else {
            holder.tvReminder.setVisibility(View.GONE);
        }

        if (task.isCompleted()) {
            holder.tvTitle.setPaintFlags(holder.tvTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.tvDescription.setPaintFlags(holder.tvDescription.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.tvReminder.setPaintFlags(holder.tvReminder.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            holder.tvTitle.setPaintFlags(holder.tvTitle.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            holder.tvDescription.setPaintFlags(holder.tvDescription.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            holder.tvReminder.setPaintFlags(holder.tvReminder.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
        }

        // 优先级圆点显示
        if (task.getPriority() > 0) {
            holder.priorityDot.setVisibility(View.VISIBLE);
            int colorRes;
            switch (task.getPriority()) {
                case 3: colorRes = R.color.priority_high; break;
                case 2: colorRes = R.color.priority_medium; break;
                case 1: colorRes = R.color.priority_low; break;
                default: colorRes = R.color.priority_none; break;
            }
            holder.priorityDot.setBackgroundResource(R.drawable.priority_dot);
            DrawableCompat.setTint(holder.priorityDot.getBackground(), ContextCompat.getColor(holder.itemView.getContext(), colorRes));
        } else {
            holder.priorityDot.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> listener.onTaskClick(task));
        holder.cbCompleted.setOnCheckedChangeListener((buttonView, isChecked) -> listener.onTaskToggle(task, isChecked));
        holder.btnDelete.setOnClickListener(v -> listener.onTaskDelete(task));
    }

    private String formatReminderText(long reminderAt) {
        Calendar reminder = Calendar.getInstance();
        reminder.setTimeInMillis(reminderAt);
        Calendar today = Calendar.getInstance();
        Calendar tomorrow = (Calendar) today.clone();
        tomorrow.add(Calendar.DAY_OF_MONTH, 1);

        String time = String.format(Locale.getDefault(), "%02d:%02d",
                reminder.get(Calendar.HOUR_OF_DAY), reminder.get(Calendar.MINUTE));
        if (isSameDay(reminder, today)) {
            return "今天 " + time + " 提醒";
        }
        if (isSameDay(reminder, tomorrow)) {
            return "明天 " + time + " 提醒";
        }
        return String.format(Locale.getDefault(), "%d月%d日 %s 提醒",
                reminder.get(Calendar.MONTH) + 1, reminder.get(Calendar.DAY_OF_MONTH), time);
    }

    private boolean isSameDay(Calendar first, Calendar second) {
        return first.get(Calendar.YEAR) == second.get(Calendar.YEAR)
                && first.get(Calendar.DAY_OF_YEAR) == second.get(Calendar.DAY_OF_YEAR);
    }

    public static class TaskViewHolder extends RecyclerView.ViewHolder {
        CheckBox cbCompleted;
        View priorityDot;
        TextView tvTitle;
        TextView tvDescription;
        TextView tvReminder;
        ImageButton btnDelete;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            cbCompleted = itemView.findViewById(R.id.cbTaskCompleted);
            priorityDot = itemView.findViewById(R.id.viewPriorityDot);
            tvTitle = itemView.findViewById(R.id.tvTaskTitle);
            tvDescription = itemView.findViewById(R.id.tvTaskDescription);
            tvReminder = itemView.findViewById(R.id.tvTaskReminder);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }

    public static class TaskDiffCallback extends DiffUtil.ItemCallback<Task> {
        @Override
        public boolean areItemsTheSame(@NonNull Task oldItem, @NonNull Task newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull Task oldItem, @NonNull Task newItem) {
            return oldItem.equals(newItem);
        }
    }
}
