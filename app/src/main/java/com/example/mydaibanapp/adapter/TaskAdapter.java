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
        String description = task.getDescription();
        if (description != null && !description.trim().isEmpty()) {
            holder.tvDescription.setVisibility(View.VISIBLE);
            holder.tvDescription.setText(description);
        } else {
            holder.tvDescription.setVisibility(View.GONE);
        }
        holder.cbCompleted.setOnCheckedChangeListener(null);
        holder.cbCompleted.setChecked(task.isCompleted());

        boolean hasDate = task.getDueDate() != null;
        if (hasDate) {
            holder.tvDate.setVisibility(View.VISIBLE);
            holder.tvDate.setText(formatDueDateText(task.getDueDate()));
        } else {
            holder.tvDate.setVisibility(View.GONE);
        }

        boolean hasReminder = task.getReminderAt() != null;
        if (task.getReminderAt() != null) {
            holder.tvReminder.setVisibility(View.VISIBLE);
            holder.tvReminder.setText(formatReminderText(task.getReminderAt(), task.getDueDate()));
        } else {
            holder.tvReminder.setVisibility(View.GONE);
        }
        holder.layoutMeta.setVisibility(hasDate || hasReminder ? View.VISIBLE : View.GONE);

        setStrikeThrough(holder.tvTitle, task.isCompleted());
        setStrikeThrough(holder.tvDescription, task.isCompleted());
        setStrikeThrough(holder.tvDate, task.isCompleted());
        setStrikeThrough(holder.tvReminder, task.isCompleted());

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

    private void setStrikeThrough(TextView textView, boolean shouldStrike) {
        if (shouldStrike) {
            textView.setPaintFlags(textView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            textView.setPaintFlags(textView.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
        }
    }

    private String formatDueDateText(long dueDate) {
        Calendar due = Calendar.getInstance();
        due.setTimeInMillis(dueDate);
        Calendar today = Calendar.getInstance();
        Calendar tomorrow = (Calendar) today.clone();
        tomorrow.add(Calendar.DAY_OF_MONTH, 1);

        if (isSameDay(due, today)) {
            return "今天";
        }
        if (isSameDay(due, tomorrow)) {
            return "明天";
        }
        return String.format(Locale.getDefault(), "%d月%d日",
                due.get(Calendar.MONTH) + 1, due.get(Calendar.DAY_OF_MONTH));
    }

    private String formatReminderText(long reminderAt, Long dueDate) {
        Calendar reminder = Calendar.getInstance();
        reminder.setTimeInMillis(reminderAt);
        String time = String.format(Locale.getDefault(), "%02d:%02d",
                reminder.get(Calendar.HOUR_OF_DAY), reminder.get(Calendar.MINUTE));

        if (dueDate != null) {
            Calendar due = Calendar.getInstance();
            due.setTimeInMillis(dueDate);
            if (isSameDay(reminder, due)) {
                return "提醒 " + time;
            }
        }

        Calendar today = Calendar.getInstance();
        Calendar tomorrow = (Calendar) today.clone();
        tomorrow.add(Calendar.DAY_OF_MONTH, 1);

        if (isSameDay(reminder, today)) {
            return "今天 " + time;
        }
        if (isSameDay(reminder, tomorrow)) {
            return "明天 " + time;
        }
        return String.format(Locale.getDefault(), "%d月%d日 %s",
                reminder.get(Calendar.MONTH) + 1, reminder.get(Calendar.DAY_OF_MONTH), time);
    }

    private boolean isSameDay(Calendar first, Calendar second) {
        return first.get(Calendar.YEAR) == second.get(Calendar.YEAR)
                && first.get(Calendar.DAY_OF_YEAR) == second.get(Calendar.DAY_OF_YEAR);
    }

    public static class TaskViewHolder extends RecyclerView.ViewHolder {
        CheckBox cbCompleted;
        View priorityDot;
        ViewGroup layoutMeta;
        TextView tvTitle;
        TextView tvDescription;
        TextView tvDate;
        TextView tvReminder;
        ImageButton btnDelete;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            cbCompleted = itemView.findViewById(R.id.cbTaskCompleted);
            priorityDot = itemView.findViewById(R.id.viewPriorityDot);
            layoutMeta = itemView.findViewById(R.id.layoutTaskMeta);
            tvTitle = itemView.findViewById(R.id.tvTaskTitle);
            tvDescription = itemView.findViewById(R.id.tvTaskDescription);
            tvDate = itemView.findViewById(R.id.tvTaskDate);
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
