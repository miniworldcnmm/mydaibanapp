package com.example.mydaibanapp.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String? = null,
    val isCompleted: Boolean = false,
    val createTime: Long = System.currentTimeMillis(),
    val dueTime: Long? = null,
    val priority: Int = 0
)
