package com.example.mydaibanapp.ui.activity

import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mydaibanapp.data.dao.TaskDao
import com.example.mydaibanapp.data.database.AppDatabase
import com.example.mydaibanapp.data.entity.Task
import com.example.mydaibanapp.databinding.ActivityTaskBinding
import com.example.mydaibanapp.databinding.DialogAddTaskBinding
import com.example.mydaibanapp.repository.TaskRepository
import com.example.mydaibanapp.ui.adapter.TaskAdapter
import com.example.mydaibanapp.ui.viewmodel.TaskViewModel
import com.example.mydaibanapp.ui.viewmodel.TaskViewModelFactory

class TaskActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTaskBinding
    private lateinit var viewModel: TaskViewModel
    private lateinit var adapter: TaskAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTaskBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 初始化数据库和Repository
        val taskDao: TaskDao = AppDatabase.getDatabase(this).taskDao()
        val repository = TaskRepository(taskDao)
        val viewModelFactory = TaskViewModelFactory(repository)
        viewModel = ViewModelProvider(this, viewModelFactory)[TaskViewModel::class.java]

        // 初始化RecyclerView
        adapter = TaskAdapter(
            onTaskClick = { task -> showEditTaskDialog(task) },
            onTaskToggle = { task, isCompleted ->
                viewModel.toggleTaskCompletion(task.id, isCompleted)
            },
            onTaskDelete = { task ->
                AlertDialog.Builder(this)
                    .setTitle("删除任务")
                    .setMessage("确定要删除这个任务吗？")
                    .setPositiveButton("删除") { _, _ ->
                        viewModel.deleteTask(task)
                    }
                    .setNegativeButton("取消", null)
                    .show()
            }
        )

        binding.rvTasks.adapter = adapter
        binding.rvTasks.layoutManager = LinearLayoutManager(this)

        // 观察任务列表变化
        viewModel.activeTasks.observe(this) { tasks ->
            adapter.submitList(tasks)
        }

        // 添加任务按钮点击事件
        binding.fabAddTask.setOnClickListener {
            showAddTaskDialog()
        }
    }

    private fun showAddTaskDialog() {
        val dialogBinding = DialogAddTaskBinding.inflate(LayoutInflater.from(this))
        AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .setTitle("添加新任务")
            .setPositiveButton("添加") { _, _ ->
                val title = dialogBinding.etTitle.text.toString().trim()
                val description = dialogBinding.etDescription.text.toString().trim()
                if (title.isNotEmpty()) {
                    val task = Task(
                        title = title,
                        description = description.takeIf { it.isNotEmpty() }
                    )
                    viewModel.insertTask(task)
                }
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun showEditTaskDialog(task: Task) {
        val dialogBinding = DialogAddTaskBinding.inflate(LayoutInflater.from(this))
        dialogBinding.etTitle.setText(task.title)
        dialogBinding.etDescription.setText(task.description)

        AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .setTitle("编辑任务")
            .setPositiveButton("保存") { _, _ ->
                val title = dialogBinding.etTitle.text.toString().trim()
                val description = dialogBinding.etDescription.text.toString().trim()
                if (title.isNotEmpty()) {
                    val updatedTask = task.copy(
                        title = title,
                        description = description.takeIf { it.isNotEmpty() }
                    )
                    viewModel.updateTask(updatedTask)
                }
            }
            .setNegativeButton("取消", null)
            .show()
    }
}
