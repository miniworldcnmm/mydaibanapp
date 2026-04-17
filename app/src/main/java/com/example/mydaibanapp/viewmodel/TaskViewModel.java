package com.example.mydaibanapp.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import com.example.mydaibanapp.data.Task;
import com.example.mydaibanapp.repository.TaskRepository;
import java.util.Calendar;
import java.util.List;

public class TaskViewModel extends AndroidViewModel {
    private TaskRepository repository;
    private LiveData<List<Task>> activeTasks;
    private LiveData<List<Task>> completedTasks;
    private LiveData<List<Task>> allTasks;

    // 日期查询相关
    private final MutableLiveData<long[]> dateRange = new MutableLiveData<>();
    private final LiveData<List<Task>> tasksByDate;

    // 月份有待办日期查询
    private final MutableLiveData<long[]> monthRange = new MutableLiveData<>();
    private final LiveData<List<Long>> taskDatesInMonth;

    // 搜索相关
    private final MutableLiveData<String> searchQuery = new MutableLiveData<>();
    private final LiveData<List<Task>> searchResults;

    // 筛选状态保存（ViewModel级别，切换tab和旋转屏幕不丢失）
    private final MutableLiveData<Integer> currentFilter = new MutableLiveData<>(0);
    private final MutableLiveData<Integer> currentPriorityFilter = new MutableLiveData<>(-1);

    public TaskViewModel(@NonNull Application application) {
        super(application);
        repository = new TaskRepository(application);
        activeTasks = repository.getActiveTasks();
        completedTasks = repository.getCompletedTasks();
        allTasks = repository.getAllTasks();

        // 日期查询的LiveData，通过Transformations切换查询
        tasksByDate = Transformations.switchMap(dateRange, range ->
                repository.getTasksByDate(range[0], range[1]));

        // 月份有待办日期的LiveData
        taskDatesInMonth = Transformations.switchMap(monthRange, range ->
                repository.getTaskDatesInMonth(range[0], range[1]));

        // 搜索结果LiveData
        searchResults = Transformations.switchMap(searchQuery, query -> {
            if (query == null || query.trim().isEmpty()) {
                return allTasks; // 空查询返回全部任务
            }
            return repository.searchTasks(query);
        });

    }

    public LiveData<List<Task>> getActiveTasks() { return activeTasks; }
    public LiveData<List<Task>> getCompletedTasks() { return completedTasks; }
    public LiveData<List<Task>> getAllTasks() { return allTasks; }
    public LiveData<List<Task>> getTasksByDate() { return tasksByDate; }
    public LiveData<List<Long>> getTaskDatesInMonth() { return taskDatesInMonth; }
    public LiveData<List<Task>> getSearchResults() { return searchResults; }

    /**
     * 设置查询日期，加载该日期的所有待办
     */
    public void setDate(long startOfDay, long endOfDay) {
        dateRange.setValue(new long[]{startOfDay, endOfDay});
    }

    /**
     * 设置查询月份，加载该月份内有待办的日期
     */
    public void setMonth(long monthStart, long monthEnd) {
        monthRange.setValue(new long[]{monthStart, monthEnd});
    }

    /**
     * 设置搜索关键词，触发搜索查询
     */
    public void setSearchQuery(String query) {
        searchQuery.setValue(query);
    }

    /**
     * 清除搜索，恢复全部任务显示
     */
    public void clearSearch() {
        searchQuery.setValue(null);
    }

    // 筛选状态存取
    public int getCurrentFilter() { return currentFilter.getValue() != null ? currentFilter.getValue() : 0; }
    public void setCurrentFilter(int filter) { currentFilter.setValue(filter); }
    public int getCurrentPriorityFilter() { return currentPriorityFilter.getValue() != null ? currentPriorityFilter.getValue() : -1; }
    public void setCurrentPriorityFilter(int filter) { currentPriorityFilter.setValue(filter); }
    public String getSearchQueryValue() { return searchQuery.getValue(); }

    public void insertTask(Task task) { repository.insertTask(task); }
    public void updateTask(Task task) { repository.updateTask(task); }
    public void deleteTask(Task task) { repository.deleteTask(task); }
    public void toggleTaskCompletion(int taskId, boolean isCompleted) { repository.toggleTaskCompletion(taskId, isCompleted); }

    /**
     * 工具方法：获取某天的起始时间戳（0:00:00）
     */
    public static long getStartOfDay(Calendar cal) {
        Calendar day = (Calendar) cal.clone();
        day.set(Calendar.HOUR_OF_DAY, 0);
        day.set(Calendar.MINUTE, 0);
        day.set(Calendar.SECOND, 0);
        day.set(Calendar.MILLISECOND, 0);
        return day.getTimeInMillis();
    }

    /**
     * 工具方法：获取某天的结束时间戳（次日0:00:00）
     */
    public static long getEndOfDay(Calendar cal) {
        Calendar day = (Calendar) cal.clone();
        day.set(Calendar.HOUR_OF_DAY, 0);
        day.set(Calendar.MINUTE, 0);
        day.set(Calendar.SECOND, 0);
        day.set(Calendar.MILLISECOND, 0);
        day.add(Calendar.DAY_OF_MONTH, 1);
        return day.getTimeInMillis();
    }

    /**
     * 工具方法：获取月份的起始时间戳
     */
    public static long getMonthStart(Calendar cal) {
        Calendar month = (Calendar) cal.clone();
        month.set(Calendar.DAY_OF_MONTH, 1);
        month.set(Calendar.HOUR_OF_DAY, 0);
        month.set(Calendar.MINUTE, 0);
        month.set(Calendar.SECOND, 0);
        month.set(Calendar.MILLISECOND, 0);
        return month.getTimeInMillis();
    }

    /**
     * 工具方法：获取月份的结束时间戳（下月1日0:00:00）
     */
    public static long getMonthEnd(Calendar cal) {
        Calendar month = (Calendar) cal.clone();
        month.set(Calendar.DAY_OF_MONTH, 1);
        month.set(Calendar.HOUR_OF_DAY, 0);
        month.set(Calendar.MINUTE, 0);
        month.set(Calendar.SECOND, 0);
        month.set(Calendar.MILLISECOND, 0);
        month.add(Calendar.MONTH, 1);
        return month.getTimeInMillis();
    }
}