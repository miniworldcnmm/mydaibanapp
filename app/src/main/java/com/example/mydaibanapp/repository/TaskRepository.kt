package com.example.mydaibanapp.repository

import com.example.mydaibanapp.data.dao.TaskDao
import com.example.mydaibanapp.data.entity.Task
import kotlinx.coroutines.flow.Flow

class TaskRepository(private val taskDao: TaskDao) {
    val activeTasks: Flow<List<Task>> = taskDao.getActiveTasks()
    val completedTasks: Flow<List<Task>> = taskDao.getCompletedTasks()

    suspend fun insertTask(task: Task) {
        taskDao.insertTask(task)
    }

    suspend fun updateTask(task: Task) {
        taskDao.updateTask(task)
    }

    suspend fun deleteTask(task: Task) {
        taskDao.deleteTask(task)
    }

    suspend fun toggleTaskCompletion(taskId: Int, isCompleted: Boolean) {
        taskDao.toggleTaskCompletion(taskId, isCompleted)
    }
}
