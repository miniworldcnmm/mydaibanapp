package com.example.mydaibanapp.reminder;

import android.annotation.SuppressLint;
import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.example.mydaibanapp.MainActivity;
import com.example.mydaibanapp.R;
import com.example.mydaibanapp.data.AppDatabase;
import com.example.mydaibanapp.data.Task;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TaskReminderReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || !TaskReminderScheduler.ACTION_TASK_REMINDER.equals(intent.getAction())) {
            return;
        }
        int taskId = intent.getIntExtra(TaskReminderScheduler.EXTRA_TASK_ID, -1);
        if (taskId <= 0) {
            return;
        }

        PendingResult pendingResult = goAsync();
        Context appContext = context.getApplicationContext();
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            try {
                Task task = AppDatabase.getDatabase(appContext).taskDao().getTaskByIdSync(taskId);
                if (!shouldShowReminder(task)) {
                    return;
                }
                if (!hasNotificationPermission(appContext)) {
                    return;
                }
                createNotificationChannel(appContext);
                showNotification(appContext, task);
            } finally {
                executorService.shutdown();
                pendingResult.finish();
            }
        });
    }

    private boolean shouldShowReminder(Task task) {
        return task != null
                && !task.isCompleted()
                && task.getReminderAt() != null
                && task.getReminderAt() <= System.currentTimeMillis();
    }

    private boolean hasNotificationPermission(Context context) {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU
                || ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return;
        }
        NotificationChannel channel = new NotificationChannel(
                TaskReminderScheduler.CHANNEL_ID,
                context.getString(R.string.notification_channel_task_reminders),
                NotificationManager.IMPORTANCE_DEFAULT
        );
        channel.setDescription(context.getString(R.string.notification_channel_task_reminders_desc));
        NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
        if (notificationManager != null) {
            notificationManager.createNotificationChannel(channel);
        }
    }

    @SuppressLint("MissingPermission")
    private void showNotification(Context context, Task task) {
        if (!hasNotificationPermission(context)) {
            return;
        }
        Intent openIntent = new Intent(context, MainActivity.class);
        openIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent contentIntent = PendingIntent.getActivity(
                context,
                task.getId(),
                openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        String description = task.getDescription();
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, TaskReminderScheduler.CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_tasks)
                .setContentTitle(context.getString(R.string.notification_task_reminder_title))
                .setContentText(task.getTitle())
                .setContentIntent(contentIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        if (description != null && !description.trim().isEmpty()) {
            builder.setStyle(new NotificationCompat.BigTextStyle()
                    .bigText(task.getTitle() + "\n" + description));
        }

        try {
            NotificationManagerCompat.from(context).notify(task.getId(), builder.build());
        } catch (SecurityException ignored) {
        }
    }
}
