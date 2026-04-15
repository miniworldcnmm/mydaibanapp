package com.example.mydaibanapp.fragment;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.mydaibanapp.R;
import com.example.mydaibanapp.adapter.TaskAdapter;
import com.example.mydaibanapp.data.Task;
import com.example.mydaibanapp.databinding.DialogAddTaskBinding;
import com.example.mydaibanapp.databinding.FragmentTaskListBinding;
import com.example.mydaibanapp.viewmodel.TaskViewModel;
import java.util.List;
import java.util.stream.Collectors;

public class TaskListFragment extends Fragment implements TaskAdapter.OnTaskClickListener {
    private FragmentTaskListBinding binding;
    private TaskViewModel viewModel;
    private TaskAdapter adapter;
    private int currentFilter = 0; // 0:全部 1:进行中 2:已完成

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true); // 启用Fragment的菜单
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentTaskListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 设置Toolbar
        ((AppCompatActivity) requireActivity()).setSupportActionBar(binding.toolbar);
        ((AppCompatActivity) requireActivity()).getSupportActionBar().setDisplayShowTitleEnabled(false);

        adapter = new TaskAdapter(this);
        binding.rvTasks.setAdapter(adapter);
        binding.rvTasks.setLayoutManager(new LinearLayoutManager(requireContext()));

        // 使用Activity级别的ViewModel，实现数据共享
        viewModel = new ViewModelProvider(requireActivity()).get(TaskViewModel.class);
        observeTasks();

        binding.fabAddTask.setOnClickListener(v -> showAddTaskDialog());
    }

    private void observeTasks() {
        viewModel.getAllTasks().observe(getViewLifecycleOwner(), tasks -> {
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
        DialogAddTaskBinding dialogBinding = DialogAddTaskBinding.inflate(LayoutInflater.from(requireContext()));
        AlertDialog dialog = new AlertDialog.Builder(requireContext())
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
        DialogAddTaskBinding dialogBinding = DialogAddTaskBinding.inflate(LayoutInflater.from(requireContext()));
        dialogBinding.etTitle.setText(task.getTitle());
        dialogBinding.etDescription.setText(task.getDescription());

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
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
        new AlertDialog.Builder(requireContext())
                .setTitle("删除任务")
                .setMessage("确定要删除这个任务吗？")
                .setPositiveButton("删除", (dialog, which) -> viewModel.deleteTask(task))
                .setNegativeButton("取消", null)
                .show();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_task_filter, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.filter_all) {
            currentFilter = 0;
            observeTasks();
            return true;
        } else if (id == R.id.filter_active) {
            currentFilter = 1;
            observeTasks();
            return true;
        } else if (id == R.id.filter_completed) {
            currentFilter = 2;
            observeTasks();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
