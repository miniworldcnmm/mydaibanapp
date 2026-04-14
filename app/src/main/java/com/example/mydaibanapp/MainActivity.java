package com.example.mydaibanapp;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.mydaibanapp.adapter.TaskAdapter;
import com.example.mydaibanapp.data.Task;
import com.example.mydaibanapp.databinding.ActivityTaskBinding;
import com.example.mydaibanapp.databinding.DialogAddTaskBinding;
import com.example.mydaibanapp.viewmodel.TaskViewModel;
import java.util.List;
import java.util.stream.Collectors;

public class MainActivity extends AppCompatActivity implements TaskAdapter.OnTaskClickListener {
    private ActivityTaskBinding binding;
    private TaskViewModel viewModel;
    private TaskAdapter adapter;
    private int currentFilter = 0; // 0:全部 1:进行中 2:已完成

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTaskBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);

        adapter = new TaskAdapter(this);
        binding.rvTasks.setAdapter(adapter);
        binding.rvTasks.setLayoutManager(new LinearLayoutManager(this));

        viewModel = new ViewModelProvider(this).get(TaskViewModel.class);
        observeTasks();

        binding.fabAddTask.setOnClickListener(v -> showAddTaskDialog());
    }

    private void observeTasks() {
        viewModel.getAllTasks().observe(this, tasks -> {
            if (currentFilter == 0) {
                adapter.submitList(tasks);
            } else if (currentFilter == 1) {
                adapter.submitList(filterActiveTasks(tasks));
            } else {
                adapter.submitList(filterCompletedTasks(tasks));
            }
        });
    }

    private List<Task> filterActiveTasks(List<Task> tasks) {
        return tasks.stream().filter(task -> !task.isCompleted()).collect(Collectors.toList());
    }

    private List<Task> filterCompletedTasks(List<Task> tasks) {
        return tasks.stream().filter(Task::isCompleted).collect(Collectors.toList());
    }

    private void showAddTaskDialog() {
        DialogAddTaskBinding dialogBinding = DialogAddTaskBinding.inflate(LayoutInflater.from(this));
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogBinding.getRoot())
                .setTitle("添加新任务")
                .setPositiveButton("添加", null)
                .setNegativeButton("取消", null)
                .create();

        dialog.setOnShowListener(d -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                String title = dialogBinding.etTitle.getText().toString().trim();
                String description = dialogBinding.etDescription.getText().toString().trim();
                if (title.isEmpty()) {
                    dialogBinding.etTitle.setError("任务标题不能为空");
                    return;
                }
                Task task = new Task(title, description.isEmpty() ? null : description);
                viewModel.insertTask(task);
                dialog.dismiss();
            });
        });
        dialog.show();
    }

    private void showEditTaskDialog(Task task) {
        DialogAddTaskBinding dialogBinding = DialogAddTaskBinding.inflate(LayoutInflater.from(this));
        dialogBinding.etTitle.setText(task.getTitle());
        dialogBinding.etDescription.setText(task.getDescription());

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogBinding.getRoot())
                .setTitle("编辑任务")
                .setPositiveButton("保存", null)
                .setNegativeButton("取消", null)
                .create();

        dialog.setOnShowListener(d -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                String title = dialogBinding.etTitle.getText().toString().trim();
                String description = dialogBinding.etDescription.getText().toString().trim();
                if (title.isEmpty()) {
                    dialogBinding.etTitle.setError("任务标题不能为空");
                    return;
                }
                Task updatedTask = new Task(title, description.isEmpty() ? null : description);
                updatedTask.setId(task.getId());
                updatedTask.setCompleted(task.isCompleted());
                viewModel.updateTask(updatedTask);
                dialog.dismiss();
            });
        });
        dialog.show();
    }

    @Override
    public void onTaskClick(Task task) {
        showEditTaskDialog(task);
    }

    @Override
    public void onTaskToggle(Task task, boolean isCompleted) {
        viewModel.toggleTaskCompletion(task.getId(), isCompleted);
    }

    @Override
    public void onTaskDelete(Task task) {
        new AlertDialog.Builder(this)
                .setTitle("删除任务")
                .setMessage("确定要删除这个任务吗？")
                .setPositiveButton("删除", (dialog, which) -> viewModel.deleteTask(task))
                .setNegativeButton("取消", null)
                .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_task_filter, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.filter_all) {
            currentFilter = 0;
            observeTasks();
            getSupportActionBar().setTitle("全部待办");
            return true;
        } else if (id == R.id.filter_active) {
            currentFilter = 1;
            observeTasks();
            getSupportActionBar().setTitle("我的待办");
            return true;
        } else if (id == R.id.filter_completed) {
            currentFilter = 2;
            observeTasks();
            getSupportActionBar().setTitle("已完成待办");
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
