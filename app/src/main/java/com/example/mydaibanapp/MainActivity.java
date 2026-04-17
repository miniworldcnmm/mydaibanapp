package com.example.mydaibanapp;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import com.example.mydaibanapp.databinding.ActivityTaskBinding;
import com.example.mydaibanapp.fragment.CalendarFragment;
import com.example.mydaibanapp.fragment.SettingsFragment;
import com.example.mydaibanapp.fragment.TaskListFragment;

public class MainActivity extends AppCompatActivity {
    private ActivityTaskBinding binding;
    private TaskListFragment taskListFragment;
    private CalendarFragment calendarFragment;
    private SettingsFragment settingsFragment;
    private Fragment activeFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 应用主题设置，必须在super.onCreate之前
        applyThemeSetting();
        super.onCreate(savedInstanceState);

        binding = ActivityTaskBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 初始化Fragment
        if (savedInstanceState == null) {
            taskListFragment = new TaskListFragment();
            calendarFragment = new CalendarFragment();
            settingsFragment = new SettingsFragment();
            activeFragment = taskListFragment;

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
            activeFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        }

        // 底部导航切换
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_tasks) {
                switchFragment(taskListFragment);
                return true;
            } else if (id == R.id.nav_calendar) {
                switchFragment(calendarFragment);
                return true;
            } else if (id == R.id.nav_settings) {
                switchFragment(settingsFragment);
                return true;
            }
            return false;
        });
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
     * 切换Fragment
     * 先弹出back stack中覆盖的Fragment（如DateTaskFragment），防止残留显示
     */
    private void switchFragment(Fragment targetFragment) {
        if (activeFragment != targetFragment) {
            // 弹出overlay Fragment（如DateTaskFragment），避免切换tab时残留显示
            if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                getSupportFragmentManager().popBackStackImmediate();
            }

            getSupportFragmentManager().beginTransaction()
                    .hide(activeFragment)
                    .show(targetFragment)
                    .commit();
            activeFragment = targetFragment;
        }
    }
}