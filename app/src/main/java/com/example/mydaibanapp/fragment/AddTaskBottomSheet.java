package com.example.mydaibanapp.fragment;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.example.mydaibanapp.MainActivity;
import com.example.mydaibanapp.R;
import com.example.mydaibanapp.data.Task;
import com.example.mydaibanapp.databinding.FragmentAddTaskBottomSheetBinding;
import com.example.mydaibanapp.viewmodel.TaskViewModel;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.Calendar;
import java.util.Locale;

public class AddTaskBottomSheet extends BottomSheetDialogFragment {

    private static final String ARG_DEFAULT_DATE = "default_date";

    private FragmentAddTaskBottomSheetBinding binding;
    private TaskViewModel viewModel;
    private Long dueDate = null; // null表示不设置日期
    private Long reminderAt = null; // null表示不设置提醒
    private int priority = 0; // 默认无优先级

    public static AddTaskBottomSheet newInstance(long defaultDate) {
        AddTaskBottomSheet fragment = new AddTaskBottomSheet();
        Bundle args = new Bundle();
        args.putLong(ARG_DEFAULT_DATE, defaultDate);
        fragment.setArguments(args);
        return fragment;
    }

    public AddTaskBottomSheet() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null && getArguments().containsKey(ARG_DEFAULT_DATE)) {
            dueDate = getArguments().getLong(ARG_DEFAULT_DATE);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentAddTaskBottomSheetBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(TaskViewModel.class);

        binding.btnSend.setEnabled(false);
        binding.btnSend.setAlpha(0.38f);
        binding.btnSend.setOnClickListener(v -> submitTask());

        binding.etTitle.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                submitTask();
                return true;
            }
            return false;
        });

        binding.etTitle.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                boolean hasTitle = s.toString().trim().length() > 0;
                binding.btnSend.setEnabled(hasTitle);
                binding.btnSend.setAlpha(hasTitle ? 1.0f : 0.38f);
            }
        });

        ImageButton btnPickDate = binding.btnPickDate;
        TextView tvDueDate = binding.tvDueDate;
        ImageButton btnClearDate = binding.btnClearDate;

        if (dueDate != null) {
            updateDateDisplay(tvDueDate, btnClearDate);
        }

        btnPickDate.setOnClickListener(v -> showDatePicker(tvDueDate, btnClearDate));
        btnClearDate.setOnClickListener(v -> {
            dueDate = null;
            tvDueDate.setText("不设置日期");
            tvDueDate.setTextColor(getResources().getColor(R.color.dark_gray, null));
            btnClearDate.setVisibility(View.GONE);
        });

        ImageButton btnPickReminder = binding.btnPickReminder;
        TextView tvReminder = binding.tvReminder;
        ImageButton btnClearReminder = binding.btnClearReminder;
        updateReminderDisplay(tvReminder, btnClearReminder);
        btnPickReminder.setOnClickListener(v -> showReminderDatePicker(tvReminder, btnClearReminder));
        btnClearReminder.setOnClickListener(v -> {
            reminderAt = null;
            updateReminderDisplay(tvReminder, btnClearReminder);
        });

        binding.chipGroupPriority.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.chipPriorityNone) {
                priority = 0;
            } else if (checkedId == R.id.chipPriorityLow) {
                priority = 1;
            } else if (checkedId == R.id.chipPriorityMedium) {
                priority = 2;
            } else if (checkedId == R.id.chipPriorityHigh) {
                priority = 3;
            }
        });

        binding.etTitle.requestFocus();
    }

    private void showDatePicker(TextView tvDueDate, ImageButton btnClearDate) {
        Calendar cal = Calendar.getInstance();
        if (dueDate != null) {
            cal.setTimeInMillis(dueDate);
        }
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(),
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    Calendar selected = Calendar.getInstance();
                    selected.set(selectedYear, selectedMonth, selectedDay, 0, 0, 0);
                    selected.set(Calendar.MILLISECOND, 0);
                    dueDate = selected.getTimeInMillis();
                    updateDateDisplay(tvDueDate, btnClearDate);
                }, year, month, day);
        datePickerDialog.show();
    }

    private void showReminderDatePicker(TextView tvReminder, ImageButton btnClearReminder) {
        Calendar cal = getInitialReminderCalendar();
        DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(),
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    Calendar selected = (Calendar) cal.clone();
                    selected.set(selectedYear, selectedMonth, selectedDay);
                    showReminderTimePicker(selected, tvReminder, btnClearReminder);
                }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        datePickerDialog.show();
    }

    private void showReminderTimePicker(Calendar selected, TextView tvReminder, ImageButton btnClearReminder) {
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
                    reminderAt = selected.getTimeInMillis();
                    updateReminderDisplay(tvReminder, btnClearReminder);
                    requestNotificationPermissionIfNeeded();
                }, selected.get(Calendar.HOUR_OF_DAY), selected.get(Calendar.MINUTE), true);
        timePickerDialog.show();
    }

    private Calendar getInitialReminderCalendar() {
        Calendar cal = Calendar.getInstance();
        if (reminderAt != null) {
            cal.setTimeInMillis(reminderAt);
            return cal;
        }
        if (dueDate != null) {
            cal.setTimeInMillis(dueDate);
            cal.set(Calendar.HOUR_OF_DAY, 9);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            if (cal.getTimeInMillis() > System.currentTimeMillis()) {
                return cal;
            }
        }
        cal.add(Calendar.HOUR_OF_DAY, 1);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal;
    }

    private void updateDateDisplay(TextView tvDueDate, ImageButton btnClearDate) {
        if (dueDate == null) {
            tvDueDate.setText("不设置日期");
            tvDueDate.setTextColor(getResources().getColor(R.color.dark_gray, null));
            btnClearDate.setVisibility(View.GONE);
            return;
        }
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

    private void updateReminderDisplay(TextView tvReminder, ImageButton btnClearReminder) {
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

    @Override
    public void onStart() {
        super.onStart();

        Dialog dialog = getDialog();
        if (dialog != null) {
            Window window = dialog.getWindow();
            if (window != null) {
                window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
                        | WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
            }

            View bottomSheet = dialog.findViewById(
                    com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(bottomSheet);
                behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                behavior.setSkipCollapsed(true);
            }
        }

        if (binding != null) {
            binding.etTitle.postDelayed(() -> {
                if (binding != null && isAdded()) {
                    InputMethodManager imm = (InputMethodManager) requireContext()
                            .getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (imm != null) {
                        imm.showSoftInput(binding.etTitle, InputMethodManager.SHOW_FORCED);
                    }
                }
            }, 200);
        }
    }

    private void submitTask() {
        String title = binding.etTitle.getText().toString().trim();
        String description = binding.etDescription.getText().toString().trim();

        if (title.isEmpty()) {
            return;
        }

        Task task = new Task(title, description.isEmpty() ? null : description);
        task.setDueDate(dueDate);
        task.setPriority(priority);
        task.setReminderAt(reminderAt);
        viewModel.insertTask(task);
        dismiss();
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
