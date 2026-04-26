package com.example.mydaibanapp;

import android.Manifest;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Build;
import android.content.pm.PackageManager;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import com.example.mydaibanapp.databinding.ActivityTaskBinding;
import com.example.mydaibanapp.fragment.CalendarFragment;
import com.example.mydaibanapp.fragment.SettingsFragment;
import com.example.mydaibanapp.fragment.TaskListFragment;
import com.example.mydaibanapp.reminder.TaskReminderScheduler;

public class MainActivity extends AppCompatActivity {
    private static final String KEY_SELECTED_TAB = "selected_tab";
    private static final int REQUEST_NOTIFICATION_PERMISSION = 1001;

    private ActivityTaskBinding binding;
    private TaskListFragment taskListFragment;
    private CalendarFragment calendarFragment;
    private SettingsFragment settingsFragment;
    private Fragment activeFragment;
    private int currentTabId = R.id.nav_tasks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 应用主题设置，必须在super.onCreate之前
        applyThemeSetting();
        super.onCreate(savedInstanceState);

        binding = ActivityTaskBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        TaskReminderScheduler.rescheduleAllAsync(this);

        // 初始化Fragment
        if (savedInstanceState == null) {
            taskListFragment = new TaskListFragment();
            calendarFragment = new CalendarFragment();
            settingsFragment = new SettingsFragment();
            activeFragment = taskListFragment;
            currentTabId = R.id.nav_tasks;

            // 添加所有Fragment，默认显示任务列表
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, settingsFragment, "settings")
                    .hide(settingsFragment)
                    .add(R.id.fragment_container, calendarFragment, "calendar")
                    .hide(calendarFragment)
                    .add(R.id.fragment_container, taskListFragment, "tasks")
                    .commit();
        } else {
            // 恢复Fragment引用
            taskListFragment = (TaskListFragment) getSupportFragmentManager().findFragmentByTag("tasks");
            calendarFragment = (CalendarFragment) getSupportFragmentManager().findFragmentByTag("calendar");
            settingsFragment = (SettingsFragment) getSupportFragmentManager().findFragmentByTag("settings");

            currentTabId = savedInstanceState.getInt(KEY_SELECTED_TAB, resolveVisibleTabId());
            activeFragment = getRootFragmentByTabId(currentTabId);

            if (activeFragment == null) {
                currentTabId = resolveVisibleTabId();
                activeFragment = getRootFragmentByTabId(currentTabId);
            }
        }

        binding.bottomNavigation.setSelectedItemId(currentTabId);
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            switchFragment(item.getItemId());
            return true;
        });

        // 主题切换重建后，强制同步一次根Fragment可见状态，避免旧页面残留在上层
        if (savedInstanceState != null) {
            syncRootFragmentState();
        }
    }


    public void requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.POST_NOTIFICATIONS},
                    REQUEST_NOTIFICATION_PERMISSION
            );
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_NOTIFICATION_PERMISSION
                && grantResults.length > 0
                && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, R.string.reminder_permission_hint, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_SELECTED_TAB, currentTabId);
    }

    /**
     * 应用主题设置
     */
    private void applyThemeSetting() {
        SharedPreferences prefs = getSharedPreferences("app_settings", MODE_PRIVATE);
        boolean isDarkModeSet = prefs.contains("dark_mode");

        if (!isDarkModeSet) {
            // 首次启动，检测系统主题
            int nightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
            boolean isSystemDark = nightMode == Configuration.UI_MODE_NIGHT_YES;

            // 应用主题
            AppCompatDelegate.setDefaultNightMode(
                    isSystemDark ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
            );

            // 保存设置
            prefs.edit().putBoolean("dark_mode", isSystemDark).apply();
        } else {
            // 使用用户之前的设置
            boolean isDarkMode = prefs.getBoolean("dark_mode", false);
            AppCompatDelegate.setDefaultNightMode(
                    isDarkMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
            );
        }
    }

    /**
     * 切换根Fragment
     * 先弹出back stack中覆盖的Fragment（如DateTaskFragment），再统一隐藏所有root fragment，避免残留显示
     */
    private void switchFragment(int tabId) {
        currentTabId = tabId;
        syncRootFragmentState();
    }

    private void syncRootFragmentState() {
        Fragment targetFragment = getRootFragmentByTabId(currentTabId);
        if (targetFragment == null || !targetFragment.isAdded()) {
            return;
        }

        // 弹出overlay Fragment（如DateTaskFragment），避免切换tab时残留显示
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStackImmediate();
        }

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        hideRootFragment(transaction, taskListFragment);
        hideRootFragment(transaction, calendarFragment);
        hideRootFragment(transaction, settingsFragment);
        transaction.show(targetFragment).commit();

        activeFragment = targetFragment;
    }

    private void hideRootFragment(FragmentTransaction transaction, Fragment fragment) {
        if (fragment != null && fragment.isAdded()) {
            transaction.hide(fragment);
        }
    }

    private Fragment getRootFragmentByTabId(int tabId) {
        if (tabId == R.id.nav_calendar) {
            return calendarFragment;
        }
        if (tabId == R.id.nav_settings) {
            return settingsFragment;
        }
        return taskListFragment;
    }

    private int resolveVisibleTabId() {
        if (settingsFragment != null && settingsFragment.isAdded() && !settingsFragment.isHidden()) {
            return R.id.nav_settings;
        }
        if (calendarFragment != null && calendarFragment.isAdded() && !calendarFragment.isHidden()) {
            return R.id.nav_calendar;
        }
        return R.id.nav_tasks;
    }
}

