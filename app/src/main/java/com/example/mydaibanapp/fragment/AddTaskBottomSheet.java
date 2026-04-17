package com.example.mydaibanapp.fragment;

import android.app.Dialog;
import android.app.DatePickerDialog;
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
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.example.mydaibanapp.R;
import com.example.mydaibanapp.data.Task;
import com.example.mydaibanapp.databinding.FragmentAddTaskBottomSheetBinding;
import com.example.mydaibanapp.viewmodel.TaskViewModel;
import java.util.Calendar;

public class AddTaskBottomSheet extends BottomSheetDialogFragment {

    private static final String ARG_DEFAULT_DATE = "default_date";

    private FragmentAddTaskBottomSheetBinding binding;
    private TaskViewModel viewModel;
    private Long dueDate = null; // null表示不设置日期
    private int priority = 0; // 默认无优先级

    public static AddTaskBottomSheet newInstance(long defaultDate) {
        AddTaskBottomSheet fragment = new AddTaskBottomSheet();
        Bundle args = new Bundle();
        args.putLong(ARG_DEFAULT_DATE, defaultDate);
        fragment.setArguments(args);
        return fragment;
    }

    // 无参构造，用于待办列表页添加（不预设日期）
    public AddTaskBottomSheet() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 如果从日历页传入默认日期，则预设
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

        // 初始状态：发送按钮禁用
        binding.btnSend.setEnabled(false);
        binding.btnSend.setAlpha(0.38f);

        // 发送按钮点击
        binding.btnSend.setOnClickListener(v -> submitTask());

        // 键盘完成按钮提交
        binding.etTitle.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                submitTask();
                return true;
            }
            return false;
        });

        // 标题内容变化控制发送按钮状态
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

        // 日期选择（使用binding获取视图）
        ImageButton btnPickDate = binding.btnPickDate;
        TextView tvDueDate = binding.tvDueDate;
        ImageButton btnClearDate = binding.btnClearDate;

        // 如果有预设日期，显示
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

        // 优先级选择
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

        // 自动聚焦标题输入框
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

    @Override
    public void onStart() {
        super.onStart();

        Dialog dialog = getDialog();
        if (dialog != null) {
            // 设置键盘弹出时调整布局，ALWAYS_VISIBLE 强制弹出键盘
            Window window = dialog.getWindow();
            if (window != null) {
                window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
                        | WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
            }

            // 配置BottomSheet行为
            View bottomSheet = dialog.findViewById(
                    com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(bottomSheet);
                behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                behavior.setSkipCollapsed(true);
            }
        }

        // 双保险：延迟弹出键盘，确保Dialog窗口完全就绪
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
        viewModel.insertTask(task);
        dismiss();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}