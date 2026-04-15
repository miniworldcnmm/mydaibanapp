package com.example.mydaibanapp.fragment;

import android.app.Dialog;
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
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.example.mydaibanapp.data.Task;
import com.example.mydaibanapp.databinding.FragmentAddTaskBottomSheetBinding;
import com.example.mydaibanapp.viewmodel.TaskViewModel;

public class AddTaskBottomSheet extends BottomSheetDialogFragment {

    private FragmentAddTaskBottomSheetBinding binding;
    private TaskViewModel viewModel;

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

        // 自动聚焦标题输入框
        binding.etTitle.requestFocus();
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
        viewModel.insertTask(task);
        dismiss();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
