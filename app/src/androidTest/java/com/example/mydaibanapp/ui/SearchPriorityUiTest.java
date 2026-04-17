package com.example.mydaibanapp.ui;

import androidx.test.espresso.Espresso;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.example.mydaibanapp.MainActivity;
import com.example.mydaibanapp.R;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.*;
import static androidx.test.espresso.assertion.ViewAssertions.*;
import static androidx.test.espresso.matcher.ViewMatchers.*;

@RunWith(AndroidJUnit4.class)
public class SearchPriorityUiTest {

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule = new ActivityScenarioRule<>(MainActivity.class);

    @Test
    public void testSearchButtonVisible() {
        onView(withId(R.id.btnSearch)).check(matches(isDisplayed()));
    }

    @Test
    public void testSearchOverlayToggle() {
        onView(withId(R.id.btnSearch)).perform(click());
        onView(withId(R.id.searchOverlay)).check(matches(isDisplayed()));
        onView(withId(R.id.etSearch)).check(matches(isDisplayed()));
        Espresso.pressBack();
    }

    @Test
    public void testSearchInput() {
        onView(withId(R.id.btnSearch)).perform(click());
        onView(withId(R.id.etSearch)).perform(typeText("测试"), closeSoftKeyboard());
        onView(withId(R.id.btnClearSearch)).check(matches(isDisplayed()));
    }

    @Test
    public void testAddTaskWithPriority() {
        onView(withId(R.id.fabAddTask)).perform(click());
        onView(withId(R.id.etTitle)).perform(typeText("高优先级测试任务"), closeSoftKeyboard());
        onView(withId(R.id.chipPriorityHigh)).perform(click());
        onView(withId(R.id.btnSend)).perform(click());
    }

    @Test
    public void testFilterMenuExists() {
        Espresso.openActionBarOverflowOrOptionsMenu(
                androidx.test.platform.app.InstrumentationRegistry.getInstrumentation().getTargetContext());
        onView(withText("按优先级")).check(matches(isDisplayed()));
    }
}
