package com.example.mydaibanapp.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import com.example.mydaibanapp.R;
import com.example.mydaibanapp.databinding.FragmentCalendarBinding;
import com.example.mydaibanapp.viewmodel.TaskViewModel;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

public class CalendarFragment extends Fragment {

    private FragmentCalendarBinding binding;
    private TaskViewModel viewModel;
    private Calendar currentMonth = Calendar.getInstance();
    private final Set<Long> taskDates = new HashSet<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentCalendarBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(TaskViewModel.class);

        binding.btnPrevMonth.setOnClickListener(v -> {
            currentMonth.add(Calendar.MONTH, -1);
            loadMonthData();
        });

        binding.btnNextMonth.setOnClickListener(v -> {
            currentMonth.add(Calendar.MONTH, 1);
            loadMonthData();
        });

        // 观察月份内有待办的日期 — 只更新数据和重绘，不触发setMonth（避免无限循环）
        viewModel.getTaskDatesInMonth().observe(getViewLifecycleOwner(), dates -> {
            taskDates.clear();
            if (dates != null) {
                for (Long timestamp : dates) {
                    Calendar cal = Calendar.getInstance();
                    cal.setTimeInMillis(timestamp);
                    cal.set(Calendar.HOUR_OF_DAY, 0);
                    cal.set(Calendar.MINUTE, 0);
                    cal.set(Calendar.SECOND, 0);
                    cal.set(Calendar.MILLISECOND, 0);
                    taskDates.add(cal.getTimeInMillis());
                }
            }
            renderGrid();
        });

        // 初始加载
        loadMonthData();
    }

    /**
     * 加载当前月份的数据：更新标题、设置查询范围、触发LiveData查询。
     * 仅在初始化和月份切换时调用，不在observer中调用（避免无限循环）。
     */
    private void loadMonthData() {
        int year = currentMonth.get(Calendar.YEAR);
        int month = currentMonth.get(Calendar.MONTH) + 1;
        binding.tvMonthTitle.setText(year + "年" + month + "月");

        long monthStart = TaskViewModel.getMonthStart(currentMonth);
        long monthEnd = TaskViewModel.getMonthEnd(currentMonth);
        viewModel.setMonth(monthStart, monthEnd);
    }

    private void renderGrid() {
        binding.gridCalendar.removeAllViews();

        Calendar today = Calendar.getInstance();
        long todayStart = TaskViewModel.getStartOfDay(today);

        Calendar cal = (Calendar) currentMonth.clone();
        cal.set(Calendar.DAY_OF_MONTH, 1);

        int startDayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 1; // 0=周日
        int daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);

        int totalCells = 42;
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int cellWidth = (screenWidth - 32) / 7;

        // 从根视图高度减去头部和底部导航的高度，计算日历网格可用高度
        int cellHeight;
        int gridSize = binding.gridCalendar.getHeight();
        if (gridSize > 0) {
            // 网格已有高度，直接用
            cellHeight = gridSize / 6;
        } else {
            // 网格还没布局，从根视图反算
            View rootView = binding.getRoot();
            int rootViewHeight = rootView.getHeight();
            if (rootViewHeight > 0) {
                int monthNavHeight = binding.layoutMonthNav.getHeight();
                int weekHeaderHeight = binding.layoutWeekHeader.getHeight();
                int gridPadding = dpToPx(16); // 网格上下padding近似
                int available = rootViewHeight - monthNavHeight - weekHeaderHeight - gridPadding;
                cellHeight = Math.max(available / 6, dpToPx(40)); // 最小40dp兜底
            } else {
                // 根视图也还没布局，用屏幕高度估算
                int screenHeight = getResources().getDisplayMetrics().heightPixels;
                int statusBarApprox = dpToPx(56); // 状态栏+toolbar近似
                int bottomNavApprox = dpToPx(56); // 底部导航近似
                int headersApprox = dpToPx(86); // 月份导航+星期行近似
                int available = screenHeight - statusBarApprox - bottomNavApprox - headersApprox;
                cellHeight = Math.max(available / 6, dpToPx(40));
            }
        }

        fillGrid(startDayOfWeek, daysInMonth, cellWidth, cellHeight, todayStart, totalCells);
    }

    private void fillGrid(int startDayOfWeek, int daysInMonth, int cellWidth, int cellHeight, long todayStart, int totalCells) {
        binding.gridCalendar.removeAllViews();

        Calendar monthCal = (Calendar) currentMonth.clone();

        // 填充前面的空白格子
        for (int i = 0; i < startDayOfWeek; i++) {
            View emptyView = createEmptyCell(cellWidth, Math.max(cellHeight, 0));
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = cellWidth;
            params.height = cellHeight > 0 ? cellHeight : GridLayout.LayoutParams.WRAP_CONTENT;
            params.setGravity(Gravity.FILL);
            emptyView.setLayoutParams(params);
            binding.gridCalendar.addView(emptyView);
        }

        // 填充日期格子
        for (int day = 1; day <= daysInMonth; day++) {
            monthCal.set(Calendar.DAY_OF_MONTH, day);
            monthCal.set(Calendar.HOUR_OF_DAY, 0);
            monthCal.set(Calendar.MINUTE, 0);
            monthCal.set(Calendar.SECOND, 0);
            monthCal.set(Calendar.MILLISECOND, 0);

            long dayStart = monthCal.getTimeInMillis();
            boolean isToday = (dayStart == todayStart);
            boolean hasTask = taskDates.contains(dayStart);

            View dayCell = createDayCell(day, isToday, hasTask, cellWidth, cellHeight, dayStart);
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = cellWidth;
            params.height = cellHeight > 0 ? cellHeight : GridLayout.LayoutParams.WRAP_CONTENT;
            params.setGravity(Gravity.FILL);
            dayCell.setLayoutParams(params);
            binding.gridCalendar.addView(dayCell);
        }

        // 补齐到42个格子（6行×7列）
        int filledCells = startDayOfWeek + daysInMonth;
        int trailingEmpty = totalCells - filledCells;

        for (int i = 0; i < trailingEmpty; i++) {
            View emptyView = createEmptyCell(cellWidth, Math.max(cellHeight, 0));
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = cellWidth;
            params.height = cellHeight > 0 ? cellHeight : GridLayout.LayoutParams.WRAP_CONTENT;
            params.setGravity(Gravity.FILL);
            emptyView.setLayoutParams(params);
            binding.gridCalendar.addView(emptyView);
        }
    }

    private View createEmptyCell(int cellWidth, int cellHeight) {
        LinearLayout cell = new LinearLayout(requireContext());
        cell.setOrientation(LinearLayout.VERTICAL);
        cell.setGravity(Gravity.CENTER);
        cell.setPadding(0, 8, 0, 8);

        TextView tv = new TextView(requireContext());
        tv.setText("");
        tv.setTextSize(16);
        cell.addView(tv);

        View dotPlaceholder = new View(requireContext());
        LinearLayout.LayoutParams dotParams = new LinearLayout.LayoutParams(6, 6);
        dotParams.topMargin = 2;
        dotParams.gravity = Gravity.CENTER_HORIZONTAL;
        dotPlaceholder.setLayoutParams(dotParams);
        cell.addView(dotPlaceholder);

        return cell;
    }

    private View createDayCell(int day, boolean isToday, boolean hasTask, int cellWidth, int cellHeight, long dayStart) {
        LinearLayout cell = new LinearLayout(requireContext());
        cell.setOrientation(LinearLayout.VERTICAL);
        cell.setGravity(Gravity.CENTER);
        cell.setPadding(0, 8, 0, 4);

        TextView tvDay = new TextView(requireContext());
        tvDay.setText(String.valueOf(day));
        tvDay.setTextSize(16);
        tvDay.setGravity(Gravity.CENTER);

        if (isToday) {
            tvDay.setTextColor(Color.WHITE);
            tvDay.setBackgroundResource(R.drawable.today_circle);
            tvDay.setPadding(10, 10, 10, 10);
            tvDay.setMinimumWidth(dpToPx(32));
            tvDay.setMinimumHeight(dpToPx(42));
        } else {
            tvDay.setTextColor(getResources().getColor(R.color.dark_gray, null));
            tvDay.setPadding(8, 8, 8, 8);
        }

        cell.addView(tvDay);

        View dotView = new View(requireContext());
        LinearLayout.LayoutParams dotParams = new LinearLayout.LayoutParams(6, 6);
        dotParams.topMargin = 2;
        dotParams.gravity = Gravity.CENTER_HORIZONTAL;
        dotView.setLayoutParams(dotParams);

        if (hasTask) {
            dotView.setBackgroundResource(R.drawable.dot_indicator);
            dotView.setVisibility(View.VISIBLE);
        } else {
            dotView.setVisibility(View.INVISIBLE);
        }

        cell.addView(dotView);

        // 点击日期用动画进入日期待办页面
        cell.setOnClickListener(v -> {
            DateTaskFragment dateTaskFragment = DateTaskFragment.newInstance(dayStart);
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left,
                            R.anim.slide_in_left, R.anim.slide_out_right)
                    .replace(R.id.fragment_container, dateTaskFragment, "date_task")
                    .addToBackStack(null)
                    .commit();
        });

        return cell;
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}