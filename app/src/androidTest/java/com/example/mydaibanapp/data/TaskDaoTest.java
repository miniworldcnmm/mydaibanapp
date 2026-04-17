package com.example.mydaibanapp.data;

import android.content.Context;
import android.os.Looper;
import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class TaskDaoTest {
    private AppDatabase db;
    private TaskDao taskDao;

    @Before
    public void setUp() {
        Context context = ApplicationProvider.getApplicationContext();
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase.class)
                .allowMainThreadQueries()
                .build();
        taskDao = db.taskDao();
    }

    @After
    public void tearDown() {
        db.close();
    }

    @Test
    public void testInsertWithPriority() throws InterruptedException {
        Task task = new Task("测试任务", "描述");
        task.setPriority(3);
        taskDao.insertTask(task);

        List<Task> tasks = getValue(taskDao.getAllTasks());
        assertEquals(1, tasks.size());
        assertEquals(3, tasks.get(0).getPriority());
    }

    @Test
    public void testDefaultPriorityIsZero() throws InterruptedException {
        Task task = new Task("测试任务", "描述");
        taskDao.insertTask(task);

        List<Task> tasks = getValue(taskDao.getAllTasks());
        assertEquals(0, tasks.get(0).getPriority());
    }

    @Test
    public void testSearchByTitle() throws InterruptedException {
        Task task1 = new Task("交项目报告", "周一前交");
        Task task2 = new Task("买咖啡", "楼下咖啡店");
        taskDao.insertTask(task1);
        taskDao.insertTask(task2);

        List<Task> results = getValue(taskDao.searchTasks("报告"));
        assertEquals(1, results.size());
        assertEquals("交项目报告", results.get(0).getTitle());
    }

    @Test
    public void testSearchByDescription() throws InterruptedException {
        Task task1 = new Task("任务A", "关于项目报告的描述");
        Task task2 = new Task("任务B", "关于买咖啡的描述");
        taskDao.insertTask(task1);
        taskDao.insertTask(task2);

        List<Task> results = getValue(taskDao.searchTasks("报告"));
        assertEquals(1, results.size());
        assertEquals("任务A", results.get(0).getTitle());
    }

    @Test
    public void testSearchNoMatch() throws InterruptedException {
        Task task = new Task("测试", "描述");
        taskDao.insertTask(task);

        List<Task> results = getValue(taskDao.searchTasks("不存在的关键词"));
        assertTrue(results.isEmpty());
    }

    @Test
    public void testGetTasksByPriority() throws InterruptedException {
        Task high = new Task("高优先级", "");
        high.setPriority(3);
        Task med = new Task("中优先级", "");
        med.setPriority(2);
        Task low = new Task("低优先级", "");
        low.setPriority(1);
        Task none = new Task("无优先级", "");
        none.setPriority(0);

        taskDao.insertTask(high);
        taskDao.insertTask(med);
        taskDao.insertTask(low);
        taskDao.insertTask(none);

        List<Task> highTasks = getValue(taskDao.getTasksByPriority(3));
        assertEquals(1, highTasks.size());
        assertEquals("高优先级", highTasks.get(0).getTitle());

        List<Task> noneTasks = getValue(taskDao.getTasksByPriority(0));
        assertEquals(1, noneTasks.size());
        assertEquals("无优先级", noneTasks.get(0).getTitle());
    }

    @Test
    public void testPriorityOrdering() throws InterruptedException {
        Task low = new Task("低", "");
        low.setPriority(1);
        Task high = new Task("高", "");
        high.setPriority(3);
        Task med = new Task("中", "");
        med.setPriority(2);

        taskDao.insertTask(low);
        taskDao.insertTask(high);
        taskDao.insertTask(med);

        List<Task> tasks = getValue(taskDao.getAllTasksOrderByPriority());
        assertEquals("高", tasks.get(0).getTitle());
        assertEquals("中", tasks.get(1).getTitle());
        assertEquals("低", tasks.get(2).getTitle());
    }

    /**
     * Helper method to get value from LiveData synchronously
     * observeForever必须在主线程调用，用runOnMainSync切换到主线程
     */
    private <T> T getValue(final androidx.lifecycle.LiveData<T> liveData) throws InterruptedException {
        final Object[] data = new Object[1];
        final CountDownLatch latch = new CountDownLatch(1);
        InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
            liveData.observeForever(new androidx.lifecycle.Observer<T>() {
                @Override
                public void onChanged(T o) {
                    data[0] = o;
                    latch.countDown();
                    liveData.removeObserver(this);
                }
            });
        });
        latch.await(2, TimeUnit.SECONDS);
        @SuppressWarnings("unchecked")
        T result = (T) data[0];
        return result;
    }
}
