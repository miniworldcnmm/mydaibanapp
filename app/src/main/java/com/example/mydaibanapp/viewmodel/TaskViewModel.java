package com.example.mydaibanapp.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.example.mydaibanapp.data.Task;
import com.example.mydaibanapp.repository.TaskRepository;
import java.util.List;

public class TaskViewModel extends AndroidViewModel {
    private TaskRepository repository;
    private LiveData<List<Task>> activeTasks;
    private LiveData<List<Task>> completedTasks;
    private LiveData<List<Task>> allTasks;

    public TaskViewModel(@NonNull Application application) {
        super(application);
        repository = new TaskRepository(application);
        activeTasks = repository.getActiveTasks();
        completedTasks = repository.getCompletedTasks();
        allTasks = repository.getAllTasks();
    }

    public LiveData<List<Task>> getActiveTasks() { return activeTasks; }
    public LiveData<List<Task>> getCompletedTasks() { return completedTasks; }
    public LiveData<List<Task>> getAllTasks() { return allTasks; }

    public void insertTask(Task task) { repository.insertTask(task); }
    public void updateTask(Task task) { repository.updateTask(task); }
    public void deleteTask(Task task) { repository.deleteTask(task); }
    public void toggleTaskCompletion(int taskId, boolean isCompleted) { repository.toggleTaskCompletion(taskId, isCompleted); }
}
