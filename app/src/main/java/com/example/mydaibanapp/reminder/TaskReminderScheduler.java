package com.example.mydaibanapp.reminder;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.example.mydaibanapp.data.AppDatabase;
import com.example.mydaibanapp.data.Task;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TaskReminderScheduler {
    public static final String ACTION_TASK_REMINDER = "com.example.mydaibanapp.ACTION_TASK_REMINDER";
    public static final String EXTRA_TASK_ID = "extra_task_id";
    public static final String EXTRA_REMINDER_AT = "extra_reminder_at";
    public static final String CHANNEL_ID = "task_reminders";

    private final Context appContext;
    private final AlarmManager alarmManager;

    public TaskReminderScheduler(Context context) {
        appContext = context.getApplicationContext();
        alarmManager = (AlarmManager) appContext.getSystemService(Context.ALARM_SERVICE);
    }

    public void schedule(Task task) {
        if (task == null || task.getId() <= 0 || task.isCompleted()
                || task.getReminderAt() == null || task.getReminderAt() <= System.currentTimeMillis()) {
            if (task != null && task.getId() > 0) {
                cancel(task.getId());
            }
            return;
        }
        PendingIntent pendingIntent = createPendingIntent(
                task.getId(), task.getReminderAt(), PendingIntent.FLAG_UPDATE_CURRENT);
        if (alarmManager != null) {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, task.getReminderAt(), pendingIntent);
        }
    }

    public void cancel(int taskId) {
        if (taskId <= 0) {
            return;
        }
        PendingIntent pendingIntent = createPendingIntent(taskId, null, PendingIntent.FLAG_NO_CREATE);
        if (pendingIntent != null) {
            if (alarmManager != null) {
                alarmManager.cancel(pendingIntent);
            }
            pendingIntent.cancel();
        }
    }

    public void rescheduleAll() {
        List<Task> tasks = AppDatabase.getDatabase(appContext)
                .taskDao()
                .getPendingReminderTasks(System.currentTimeMillis());
        for (Task task : tasks) {
            schedule(task);
        }
    }

    public static void rescheduleAllAsync(Context context) {
        Context appContext = context.getApplicationContext();
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            try {
                new TaskReminderScheduler(appContext).rescheduleAll();
            } finally {
                executorService.shutdown();
            }
        });
    }

    private PendingIntent createPendingIntent(int taskId, Long reminderAt, int flag) {
        Intent intent = new Intent(appContext, TaskReminderReceiver.class);
        intent.setAction(ACTION_TASK_REMINDER);
        intent.putExtra(EXTRA_TASK_ID, taskId);
        if (reminderAt != null) {
            intent.putExtra(EXTRA_REMINDER_AT, reminderAt);
        }
        return PendingIntent.getBroadcast(
                appContext,
                taskId,
                intent,
                flag | PendingIntent.FLAG_IMMUTABLE
        );
    }
}
