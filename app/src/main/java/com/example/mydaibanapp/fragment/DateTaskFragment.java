package com.example.mydaibanapp.fragment;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.mydaibanapp.MainActivity;
import com.example.mydaibanapp.R;
import com.example.mydaibanapp.adapter.TaskAdapter;
import com.example.mydaibanapp.data.Task;
import com.example.mydaibanapp.databinding.DialogAddTaskBinding;
import com.example.mydaibanapp.databinding.FragmentDateTaskBinding;
import com.example.mydaibanapp.viewmodel.TaskViewModel;

import java.util.Calendar;
import java.util.Locale;

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

        binding.btnPrevDate.setOnClickListener(v -> {
            currentDate.add(Calendar.DAY_OF_MONTH, -1);
            loadDateTasks();
        });

        binding.btnNextDate.setOnClickListener(v -> {
            currentDate.add(Calendar.DAY_OF_MONTH, 1);
            loadDateTasks();
        });

        binding.fabAddDateTask.setOnClickListener(v -> {
            long startOfDay = TaskViewModel.getStartOfDay(currentDate);
            AddTaskBottomSheet bottomSheet = AddTaskBottomSheet.newInstance(startOfDay);
            bottomSheet.show(getChildFragmentManager(), "AddTaskBottomSheet");
        });

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
        updateDateTitle();
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

        final Long[] editDueDate = {task.getDueDate()};
        TextView tvDueDate = dialogBinding.tvDueDate;
        ImageButton btnPickDate = dialogBinding.btnPickDate;
        ImageButton btnClearDate = dialogBinding.btnClearDate;
        updateDateDisplay(tvDueDate, btnClearDate, editDueDate[0]);

        final Long[] editReminderAt = {task.getReminderAt()};
        TextView tvReminder = dialogBinding.tvReminder;
        ImageButton btnPickReminder = dialogBinding.btnPickReminder;
        ImageButton btnClearReminder = dialogBinding.btnClearReminder;
        updateReminderDisplay(tvReminder, btnClearReminder, editReminderAt[0]);

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

        btnPickReminder.setOnClickListener(v ->
                showReminderDatePicker(editReminderAt, tvReminder, btnClearReminder));

        btnClearReminder.setOnClickListener(v -> {
            editReminderAt[0] = null;
            updateReminderDisplay(tvReminder, btnClearReminder, null);
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
                updatedTask.setReminderAt(editReminderAt[0]);
                viewModel.updateTask(updatedTask);
                dialog.dismiss();
            });
        });
        dialog.show();
    }

    private void updateDateDisplay(TextView tvDueDate, ImageButton btnClearDate, Long dueDate) {
        if (dueDate == null) {
            tvDueDate.setText("不设置日期");
            tvDueDate.setTextColor(getResources().getColor(R.color.dark_gray, null));
            btnClearDate.setVisibility(View.GONE);
        } else {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(dueDate);
            int month = cal.get(Calendar.MONTH) + 1;
            int day = cal.get(Calendar.DAY_OF_MONTH);
            String[] weekDays = {"日", "一", "二", "三", "四", "五", "六"};
            String weekDay = weekDays[cal.get(Calendar.DAY_OF_WEEK) - 1];
            tvDueDate.setText(month + "月" + day + "日 星期" + weekDay);
            tvDueDate.setTextColor(getResources().getColor(R.color.hand_drawn_blue, null));
            btnClearDate.setVisibility(View.VISIBLE);
        }
    }

    private void showReminderDatePicker(final Long[] reminderAtHolder, TextView tvReminder,
                                        ImageButton btnClearReminder) {
        Calendar cal = getInitialReminderCalendar(reminderAtHolder[0]);
        DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(),
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    Calendar selected = (Calendar) cal.clone();
                    selected.set(selectedYear, selectedMonth, selectedDay);
                    showReminderTimePicker(selected, reminderAtHolder, tvReminder, btnClearReminder);
                }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        datePickerDialog.show();
    }

    private void showReminderTimePicker(Calendar selected, final Long[] reminderAtHolder,
                                        TextView tvReminder, ImageButton btnClearReminder) {
        TimePickerDialog timePickerDialog = new TimePickerDialog(requireContext(),
                (view, hourOfDay, minute) -> {
                    selected.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    selected.set(Calendar.MINUTE, minute);
                    selected.set(Calendar.SECOND, 0);
                    selected.set(Calendar.MILLISECOND, 0);
                    if (selected.getTimeInMillis() <= System.currentTimeMillis()) {
                        Toast.makeText(requireContext(), "提醒时间必须晚于现在", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    reminderAtHolder[0] = selected.getTimeInMillis();
                    updateReminderDisplay(tvReminder, btnClearReminder, reminderAtHolder[0]);
                    requestNotificationPermissionIfNeeded();
                }, selected.get(Calendar.HOUR_OF_DAY), selected.get(Calendar.MINUTE), true);
        timePickerDialog.show();
    }

    private Calendar getInitialReminderCalendar(Long reminderAt) {
        Calendar cal = Calendar.getInstance();
        if (reminderAt != null) {
            cal.setTimeInMillis(reminderAt);
        } else {
            cal.add(Calendar.HOUR_OF_DAY, 1);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
        }
        return cal;
    }

    private void updateReminderDisplay(TextView tvReminder, ImageButton btnClearReminder, Long reminderAt) {
        if (reminderAt == null) {
            tvReminder.setText(R.string.reminder_none);
            tvReminder.setTextColor(getResources().getColor(R.color.dark_gray, null));
            btnClearReminder.setVisibility(View.GONE);
            return;
        }
        tvReminder.setText(formatReminderText(reminderAt));
        tvReminder.setTextColor(getResources().getColor(R.color.hand_drawn_blue, null));
        btnClearReminder.setVisibility(View.VISIBLE);
    }

    private String formatReminderText(long timeMillis) {
        Calendar reminder = Calendar.getInstance();
        reminder.setTimeInMillis(timeMillis);
        Calendar today = Calendar.getInstance();
        Calendar tomorrow = (Calendar) today.clone();
        tomorrow.add(Calendar.DAY_OF_MONTH, 1);
        String time = String.format(Locale.getDefault(), "%02d:%02d",
                reminder.get(Calendar.HOUR_OF_DAY), reminder.get(Calendar.MINUTE));
        if (isSameDay(reminder, today)) {
            return "今天 " + time + " 提醒";
        }
        if (isSameDay(reminder, tomorrow)) {
            return "明天 " + time + " 提醒";
        }
        return String.format(Locale.getDefault(), "%d月%d日 %s 提醒",
                reminder.get(Calendar.MONTH) + 1, reminder.get(Calendar.DAY_OF_MONTH), time);
    }

    private boolean isSameDay(Calendar first, Calendar second) {
        return first.get(Calendar.YEAR) == second.get(Calendar.YEAR)
                && first.get(Calendar.DAY_OF_YEAR) == second.get(Calendar.DAY_OF_YEAR);
    }

    private void requestNotificationPermissionIfNeeded() {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).requestNotificationPermissionIfNeeded();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
