package com.example.mydaibanapp.repository;

import android.app.Application;
import androidx.lifecycle.LiveData;
import com.example.mydaibanapp.data.AppDatabase;
import com.example.mydaibanapp.data.Task;
import com.example.mydaibanapp.data.TaskDao;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TaskRepository {
    private TaskDao taskDao;
    private LiveData<List<Task>> activeTasks;
    private LiveData<List<Task>> completedTasks;
    private LiveData<List<Task>> allTasks;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    public TaskRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        taskDao = db.taskDao();
        activeTasks = taskDao.getActiveTasks();
        completedTasks = taskDao.getCompletedTasks();
        allTasks = taskDao.getAllTasks();
    }

    public LiveData<List<Task>> getActiveTasks() { return activeTasks; }
    public LiveData<List<Task>> getCompletedTasks() { return completedTasks; }
    public LiveData<List<Task>> getAllTasks() { return allTasks; }

    public LiveData<List<Task>> getTasksByDate(long startOfDay, long endOfDay) {
        return taskDao.getTasksByDate(startOfDay, endOfDay);
    }

    public LiveData<List<Long>> getTaskDatesInMonth(long monthStart, long monthEnd) {
        return taskDao.getTaskDatesInMonth(monthStart, monthEnd);
    }

    public void insertTask(Task task) {
        executorService.execute(() -> taskDao.insertTask(task));
    }

    public void updateTask(Task task) {
        executorService.execute(() -> taskDao.updateTask(task));
    }

    public void deleteTask(Task task) {
        executorService.execute(() -> taskDao.deleteTask(task));
    }

    public void toggleTaskCompletion(int taskId, boolean isCompleted) {
        executorService.execute(() -> taskDao.toggleTaskCompletion(taskId, isCompleted));
    }
}