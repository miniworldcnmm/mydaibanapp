package com.example.mydaibanapp.fragment;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.mydaibanapp.R;
import com.example.mydaibanapp.adapter.TaskAdapter;
import com.example.mydaibanapp.data.Task;
import com.example.mydaibanapp.databinding.DialogAddTaskBinding;
import com.example.mydaibanapp.databinding.FragmentDateTaskBinding;
import com.example.mydaibanapp.viewmodel.TaskViewModel;
import java.util.Calendar;

public class DateTaskFragment extends Fragment implements TaskAdapter.OnTaskClickListener {

    private static final String ARG_DATE_START = "date_start";

    private FragmentDateTaskBinding binding;
    private TaskViewModel viewModel;
    private TaskAdapter adapter;
    private Calendar currentDate = Calendar.getInstance();

    public static DateTaskFragment newInstance(long dateStart) {
        DateTaskFragment fragment = new DateTaskFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_DATE_START, dateStart);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            currentDate.setTimeInMillis(getArguments().getLong(ARG_DATE_START));
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentDateTaskBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(TaskViewModel.class);

        adapter = new TaskAdapter(this);
        binding.rvDateTasks.setAdapter(adapter);
        binding.rvDateTasks.setLayoutManager(new LinearLayoutManager(requireContext()));

        // 左右切换日期
        binding.btnPrevDate.setOnClickListener(v -> {
            currentDate.add(Calendar.DAY_OF_MONTH, -1);
            loadDateTasks();
        });

        binding.btnNextDate.setOnClickListener(v -> {
            currentDate.add(Calendar.DAY_OF_MONTH, 1);
            loadDateTasks();
        });

        // 添加待办按钮，传入当前日期作为默认日期
        binding.fabAddDateTask.setOnClickListener(v -> {
            long startOfDay = TaskViewModel.getStartOfDay(currentDate);
            AddTaskBottomSheet bottomSheet = AddTaskBottomSheet.newInstance(startOfDay);
            bottomSheet.show(getChildFragmentManager(), "AddTaskBottomSheet");
        });

        // 观察当日待办数据
        viewModel.getTasksByDate().observe(getViewLifecycleOwner(), tasks -> {
            if (tasks != null && !tasks.isEmpty()) {
                binding.tvEmptyState.setVisibility(View.GONE);
                binding.rvDateTasks.setVisibility(View.VISIBLE);
                adapter.submitList(tasks);
            } else {
                binding.tvEmptyState.setVisibility(View.VISIBLE);
                binding.rvDateTasks.setVisibility(View.GONE);
                adapter.submitList(tasks);
            }
        });

        loadDateTasks();
    }

    private void loadDateTasks() {
        // 更新标题
        updateDateTitle();

        // 设置查询日期范围
        long startOfDay = TaskViewModel.getStartOfDay(currentDate);
        long endOfDay = TaskViewModel.getEndOfDay(currentDate);
        viewModel.setDate(startOfDay, endOfDay);
    }

    private void updateDateTitle() {
        int month = currentDate.get(Calendar.MONTH) + 1;
        int day = currentDate.get(Calendar.DAY_OF_MONTH);
        String[] weekDays = {"日", "一", "二", "三", "四", "五", "六"};
        String weekDay = weekDays[currentDate.get(Calendar.DAY_OF_WEEK) - 1];
        binding.tvDateTitle.setText(month + "月" + day + "日 星期" + weekDay);
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

    private void showEditTaskDialog(Task task) {
        DialogAddTaskBinding dialogBinding = DialogAddTaskBinding.inflate(LayoutInflater.from(requireContext()));
        dialogBinding.etTitle.setText(task.getTitle());
        if (task.getDescription() != null) {
            dialogBinding.etDescription.setText(task.getDescription());
        }

        // 日期选择 - 使用数组包装以在lambda中修改
        final Long[] editDueDate = {task.getDueDate()};
        TextView tvDueDate = dialogBinding.tvDueDate;
        ImageButton btnPickDate = dialogBinding.btnPickDate;
        ImageButton btnClearDate = dialogBinding.btnClearDate;

        updateDateDisplay(tvDueDate, btnClearDate, editDueDate[0]);

        // 优先级选择
        com.google.android.material.chip.ChipGroup chipGroupPriority = dialogBinding.chipGroupPriority;
        int taskPriority = task.getPriority();
        switch (taskPriority) {
            case 3: chipGroupPriority.check(R.id.chipPriorityHigh); break;
            case 2: chipGroupPriority.check(R.id.chipPriorityMedium); break;
            case 1: chipGroupPriority.check(R.id.chipPriorityLow); break;
            default: chipGroupPriority.check(R.id.chipPriorityNone); break;
        }
        final int[] editPriority = {taskPriority};
        chipGroupPriority.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.chipPriorityNone) {
                editPriority[0] = 0;
            } else if (checkedId == R.id.chipPriorityLow) {
                editPriority[0] = 1;
            } else if (checkedId == R.id.chipPriorityMedium) {
                editPriority[0] = 2;
            } else if (checkedId == R.id.chipPriorityHigh) {
                editPriority[0] = 3;
            }
        });

        btnPickDate.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            if (editDueDate[0] != null) {
                cal.setTimeInMillis(editDueDate[0]);
            }
            int year = cal.get(Calendar.YEAR);
            int month = cal.get(Calendar.MONTH);
            int day = cal.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(),
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        Calendar selected = Calendar.getInstance();
                        selected.set(selectedYear, selectedMonth, selectedDay, 0, 0, 0);
                        selected.set(Calendar.MILLISECOND, 0);
                        editDueDate[0] = selected.getTimeInMillis();
                        updateDateDisplay(tvDueDate, btnClearDate, editDueDate[0]);
                    }, year, month, day);
            datePickerDialog.show();
        });

        btnClearDate.setOnClickListener(v -> {
            editDueDate[0] = null;
            updateDateDisplay(tvDueDate, btnClearDate, null);
        });

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
                updatedTask.setCreateTime(task.getCreateTime());
                updatedTask.setDueDate(editDueDate[0]);
                updatedTask.setPriority(editPriority[0]);
                viewModel.updateTask(updatedTask);
                dialog.dismiss();
            });
        });
        dialog.show();
    }

    private void updateDateDisplay(TextView tvDueDate, ImageButton btnClearDate, Long dueDate) {
        if (dueDate == null) {
            tvDueDate.setText("不设置日期");
            tvDueDate.setTextColor(getResources().getColor(com.example.mydaibanapp.R.color.dark_gray, null));
            btnClearDate.setVisibility(View.GONE);
        } else {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(dueDate);
            int month = cal.get(Calendar.MONTH) + 1;
            int day = cal.get(Calendar.DAY_OF_MONTH);
            String[] weekDays = {"日", "一", "二", "三", "四", "五", "六"};
            String weekDay = weekDays[cal.get(Calendar.DAY_OF_WEEK) - 1];
            tvDueDate.setText(month + "月" + day + "日 星期" + weekDay);
            tvDueDate.setTextColor(getResources().getColor(com.example.mydaibanapp.R.color.hand_drawn_blue, null));
            btnClearDate.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}