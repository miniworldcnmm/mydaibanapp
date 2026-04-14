package com.example.mydaibanapp.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface TaskDao {
    @Query("SELECT * FROM tasks WHERE isCompleted = 0 ORDER BY createTime DESC")
    LiveData<List<Task>> getActiveTasks();

    @Query("SELECT * FROM tasks WHERE isCompleted = 1 ORDER BY createTime DESC")
    LiveData<List<Task>> getCompletedTasks();

    @Query("SELECT * FROM tasks ORDER BY createTime DESC")
    LiveData<List<Task>> getAllTasks();

    @Insert
    void insertTask(Task task);

    @Update
    void updateTask(Task task);

    @Delete
    void deleteTask(Task task);

    @Query("UPDATE tasks SET isCompleted = :isCompleted WHERE id = :taskId")
    void toggleTaskCompletion(int taskId, boolean isCompleted);
}
