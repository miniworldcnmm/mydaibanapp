package com.example.mydaibanapp.data;

import org.junit.Test;
import static org.junit.Assert.*;

public class TaskTest {

    @Test
    public void testDefaultPriority() {
        Task task = new Task("Test", "Desc");
        assertEquals(0, task.getPriority());
    }

    @Test
    public void testSetPriority() {
        Task task = new Task("Test", "Desc");
        task.setPriority(3);
        assertEquals(3, task.getPriority());
    }

    @Test
    public void testPriorityInEquals() {
        Task task1 = new Task("Test", "Desc");
        task1.setId(1);
        task1.setPriority(2);

        Task task2 = new Task("Test", "Desc");
        task2.setId(1);
        task2.setPriority(2);
        assertEquals(task1, task2);

        Task task3 = new Task("Test", "Desc");
        task3.setId(1);
        task3.setPriority(3);
        assertNotEquals(task1, task3);
    }

    @Test
    public void testPriorityInHashCode() {
        Task task1 = new Task("Test", "Desc");
        task1.setId(1);
        task1.setPriority(1);

        Task task2 = new Task("Test", "Desc");
        task2.setId(1);
        task2.setPriority(2);
        assertNotEquals(task1.hashCode(), task2.hashCode());
    }

    @Test
    public void testAllPriorityLevels() {
        Task task = new Task("Test", "Desc");
        for (int p = 0; p <= 3; p++) {
            task.setPriority(p);
            assertEquals(p, task.getPriority());
        }
    }

    @Test
    public void testEqualsWithAllFields() {
        Task task1 = new Task("标题", "描述");
        task1.setId(1);
        task1.setCompleted(false);
        task1.setCreateTime(1000L);
        task1.setDueDate(2000L);
        task1.setPriority(2);

        Task task2 = new Task("标题", "描述");
        task2.setId(1);
        task2.setCompleted(false);
        task2.setCreateTime(1000L);
        task2.setDueDate(2000L);
        task2.setPriority(2);

        assertEquals(task1, task2);
        assertEquals(task1.hashCode(), task2.hashCode());
    }
}
