package com.example.mydaibanapp.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import java.util.Objects;

@Entity(tableName = "tasks")
public class Task {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String title;
    private String description;
    private boolean isCompleted;
    private long createTime = System.currentTimeMillis();
    private Long dueDate; // 待办日期的时间戳，null表示未设置
    private int priority = 0; // 0=无 1=低 2=中 3=高
    private Long reminderAt; // 提醒时间戳，null表示不提醒
    public Task(String title, String description) {
        this.title = title;
        this.description = description;
        this.isCompleted = false;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public boolean isCompleted() { return isCompleted; }
    public void setCompleted(boolean completed) { isCompleted = completed; }
    public long getCreateTime() { return createTime; }
    public void setCreateTime(long createTime) { this.createTime = createTime; }
    public Long getDueDate() { return dueDate; }
    public void setDueDate(Long dueDate) { this.dueDate = dueDate; }
    public int getPriority() { return priority; }
    public void setPriority(int priority) { this.priority = priority; }
    public Long getReminderAt() { return reminderAt; }
    public void setReminderAt(Long reminderAt) { this.reminderAt = reminderAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return id == task.id && isCompleted == task.isCompleted && createTime == task.createTime
                && priority == task.priority
                && Objects.equals(title, task.title) && Objects.equals(description, task.description)
                && Objects.equals(dueDate, task.dueDate)
                && Objects.equals(reminderAt, task.reminderAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, description, isCompleted, createTime, dueDate, priority, reminderAt);
    }
}




