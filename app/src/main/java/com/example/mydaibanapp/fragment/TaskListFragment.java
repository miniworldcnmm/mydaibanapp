package com.example.mydaibanapp.fragment;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.stream.Collectors;

public class TaskListFragment extends Fragment implements TaskAdapter.OnTaskClickListener {
    private FragmentTaskListBinding binding;
    private TaskViewModel viewModel;
    private TaskAdapter adapter;
    private int currentFilter = 0; // 0:全部 1:进行中 2:已完成
    private boolean isSearchVisible = false;
    private int currentPriorityFilter = -1; // -1=全部优先级, 0=无, 1=低, 2=中, 3=高
    private List<Task> cachedAllTasks = new ArrayList<>();
    private List<Task> cachedSearchResults = new ArrayList<>();

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

        // 从ViewModel恢复筛选状态（切换tab回来时不丢失）
        currentFilter = viewModel.getCurrentFilter();
        currentPriorityFilter = viewModel.getCurrentPriorityFilter();

        // 恢复搜索状态（屏幕旋转时）
        if (savedInstanceState != null) {
            isSearchVisible = savedInstanceState.getBoolean("isSearchVisible", false);
            if (isSearchVisible) {
                binding.searchOverlay.setVisibility(View.VISIBLE);
                String lastQuery = viewModel.getSearchQueryValue();
                if (lastQuery != null && !lastQuery.isEmpty()) {
                    binding.etSearch.setText(lastQuery);
                    binding.btnClearSearch.setVisibility(View.VISIBLE);
                }
            }
        } else {
            // 首次创建，检查ViewModel中是否有搜索状态
            String lastSearchQuery = viewModel.getSearchQueryValue();
            if (lastSearchQuery != null && !lastSearchQuery.isEmpty()) {
                isSearchVisible = true;
                binding.searchOverlay.setVisibility(View.VISIBLE);
                binding.etSearch.setText(lastSearchQuery);
                binding.btnClearSearch.setVisibility(View.VISIBLE);
            }
        }

        observeTasks();

        binding.fabAddTask.setOnClickListener(v -> {
            if (getChildFragmentManager().findFragmentByTag("AddTaskBottomSheet") != null) {
                return;
            }
            new AddTaskBottomSheet().show(getChildFragmentManager(), "AddTaskBottomSheet");
        });

        // 搜索按钮点击 - 切换搜索框显示/隐藏
        binding.btnSearch.setOnClickListener(v -> toggleSearch());

        // 搜索输入监听
        binding.etSearch.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(android.text.Editable s) {
                String query = s.toString().trim();
                binding.btnClearSearch.setVisibility(query.isEmpty() ? View.GONE : View.VISIBLE);
                if (query.isEmpty()) {
                    viewModel.clearSearch();
                } else {
                    viewModel.setSearchQuery(query);
                }
            }
        });

        // 清除搜索按钮
        binding.btnClearSearch.setOnClickListener(v -> {
            binding.etSearch.setText("");
            viewModel.clearSearch();
        });

        // 搜索键盘完成按钮
        binding.etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH) {
                android.view.inputmethod.InputMethodManager imm = (android.view.inputmethod.InputMethodManager) requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
                if (imm != null) imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                return true;
            }
            return false;
        });
    }

    private void toggleSearch() {
        isSearchVisible = !isSearchVisible;
        if (isSearchVisible) {
            // 显示搜索框 - 从右上角动画展开
            binding.searchOverlay.setVisibility(View.VISIBLE);
            binding.searchOverlay.setAlpha(0f);
            binding.searchOverlay.setScaleX(0.5f);
            binding.searchOverlay.setPivotX(binding.searchOverlay.getWidth() - binding.searchOverlay.getPaddingEnd());
            binding.searchOverlay.animate()
                    .alpha(1f)
                    .scaleX(1f)
                    .setDuration(250)
                    .setInterpolator(new android.view.animation.OvershootInterpolator())
                    .start();
            binding.etSearch.requestFocus();
            android.view.inputmethod.InputMethodManager imm = (android.view.inputmethod.InputMethodManager) requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
            if (imm != null) imm.showSoftInput(binding.etSearch, 0);
        } else {
            // 隐藏搜索框 - 动画收缩
            binding.searchOverlay.animate()
                    .alpha(0f)
                    .scaleX(0.5f)
                    .setDuration(200)
                    .setInterpolator(new android.view.animation.AccelerateInterpolator())
                    .withEndAction(() -> {
                        binding.searchOverlay.setVisibility(View.GONE);
                        binding.etSearch.setText("");
                        viewModel.clearSearch();
                    })
                    .start();
            android.view.inputmethod.InputMethodManager imm = (android.view.inputmethod.InputMethodManager) requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
            if (imm != null) imm.hideSoftInputFromWindow(binding.etSearch.getWindowToken(), 0);
        }
    }

    private void observeTasks() {
        viewModel.getAllTasks().observe(getViewLifecycleOwner(), tasks -> {
            cachedAllTasks = tasks != null ? tasks : new ArrayList<>();
            refreshList();
        });
        viewModel.getSearchResults().observe(getViewLifecycleOwner(), tasks -> {
            cachedSearchResults = tasks != null ? tasks : new ArrayList<>();
            if (isSearchVisible) {
                refreshList();
            }
        });
    }

    private void refreshList() {
        List<Task> source = isSearchVisible ? cachedSearchResults : cachedAllTasks;
        List<Task> filtered = applyFilters(source);
        adapter.submitList(filtered);
        updateEmptyState(filtered);
    }

    private List<Task> applyFilters(List<Task> tasks) {
        List<Task> result = tasks;
        // 按完成状态筛选
        if (currentFilter == 1) {
            result = filterActiveTasks(result);
        } else if (currentFilter == 2) {
            result = filterCompletedTasks(result);
        }
        // 按优先级筛选
        if (currentPriorityFilter >= 0) {
            final int pf = currentPriorityFilter;
            result = result.stream().filter(t -> t.getPriority() == pf).collect(Collectors.toList());
        }
        // 按优先级排序（高→低），相同优先级按创建时间排序
        result = new ArrayList<>(result);
        result.sort((a, b) -> {
            int priorityCompare = Integer.compare(b.getPriority(), a.getPriority());
            if (priorityCompare != 0) return priorityCompare;
            return Long.compare(b.getCreateTime(), a.getCreateTime());
        });
        return result;
    }

    private List<Task> filterActiveTasks(List<Task> tasks) {
        return tasks.stream().filter(task -> !task.isCompleted()).collect(Collectors.toList());
    }

    private List<Task> filterCompletedTasks(List<Task> tasks) {
        return tasks.stream().filter(Task::isCompleted).collect(Collectors.toList());
    }

    private void updateEmptyState(List<Task> tasks) {
        if (tasks.isEmpty()) {
            binding.tvEmptyState.setVisibility(View.VISIBLE);
            if (isSearchVisible) {
                binding.tvEmptyState.setText("没有找到匹配的任务");
            } else if (currentPriorityFilter >= 0) {
                binding.tvEmptyState.setText("该优先级下没有任务");
            } else if (currentFilter == 1) {
                binding.tvEmptyState.setText("没有进行中的任务");
            } else if (currentFilter == 2) {
                binding.tvEmptyState.setText("没有已完成的任务");
            } else {
                binding.tvEmptyState.setText("还没有待办任务，点击右下角添加");
            }
        } else {
            binding.tvEmptyState.setVisibility(View.GONE);
        }
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
            viewModel.setCurrentFilter(0);
            refreshList();
            return true;
        } else if (id == R.id.filter_active) {
            currentFilter = 1;
            viewModel.setCurrentFilter(1);
            refreshList();
            return true;
        } else if (id == R.id.filter_completed) {
            currentFilter = 2;
            viewModel.setCurrentFilter(2);
            refreshList();
            return true;
        } else if (id == R.id.filter_priority_all) {
            currentPriorityFilter = -1;
            viewModel.setCurrentPriorityFilter(-1);
            refreshList();
            return true;
        } else if (id == R.id.filter_priority_high) {
            currentPriorityFilter = 3;
            viewModel.setCurrentPriorityFilter(3);
            refreshList();
            return true;
        } else if (id == R.id.filter_priority_medium) {
            currentPriorityFilter = 2;
            viewModel.setCurrentPriorityFilter(2);
            refreshList();
            return true;
        } else if (id == R.id.filter_priority_low) {
            currentPriorityFilter = 1;
            viewModel.setCurrentPriorityFilter(1);
            refreshList();
            return true;
        } else if (id == R.id.filter_priority_none) {
            currentPriorityFilter = 0;
            viewModel.setCurrentPriorityFilter(0);
            refreshList();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("isSearchVisible", isSearchVisible);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}