package com.example.mydaibanapp.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.Delete;

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

    // 查询指定日期（当天0点~次日0点）的所有待办
    @Query("SELECT * FROM tasks WHERE dueDate >= :startOfDay AND dueDate < :endOfDay ORDER BY createTime DESC")
    LiveData<List<Task>> getTasksByDate(long startOfDay, long endOfDay);

    // 查询指定月份内有待办的所有日期（用于日历蓝点标记）
    @Query("SELECT DISTINCT dueDate FROM tasks WHERE dueDate >= :monthStart AND dueDate < :monthEnd AND dueDate IS NOT NULL")
    LiveData<List<Long>> getTaskDatesInMonth(long monthStart, long monthEnd);
}