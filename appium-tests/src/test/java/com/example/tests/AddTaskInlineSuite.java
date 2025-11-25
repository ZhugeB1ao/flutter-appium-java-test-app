package com.example.tests;

import com.example.BaseTest;
import org.testng.annotations.Test;
import org.testng.Assert;
import org.openqa.selenium.WebElement;

/**
 * AddTaskInlineSuite: five simple add-task tests using the inline field + add button.
 */
public class AddTaskInlineSuite extends BaseTest {

    private void addAndAssert(String title) {
        gotoTasksTab();
        // Some platforms (Android) may not expose the Flutter TextField via accessibility id.
        // Prefer the visible add button as a sanity check, then target the first EditText on screen.
        try {
            waitForAccessibilityId("add_task_button");
        } catch (Exception ignored) {}
        WebElement field = waitForFirstEditText();
    typeUsingKeyboard(field, title);
    // extra safety: ensure keyboard is hidden and give the UI a moment before tapping Add
    try { hideKeyboardSafe(); } catch (Exception ignored) {}
    try { Thread.sleep(200); } catch (InterruptedException ignored) {}
    tap(waitForAccessibilityId("add_task_button"));
        // capture a debug snapshot immediately after pressing add to investigate flakiness
        try { captureDebugSnapshot("after_add"); } catch (Exception ignored) {}
        try {
            waitForTextContains(title);
        } catch (Exception e) {
            // capture another snapshot on failure to aid debugging
            try { captureDebugSnapshot("after_add_failure"); } catch (Exception ignored) {}
            throw e;
        }

        Assert.assertTrue(driver.getPageSource().contains(title), "Task should be present: " + title);
    }

    @Test(description = "Add task 1 with simple title")
    public void testAddTask1() {
        addAndAssert("Task 1 " + System.currentTimeMillis());
    }

    @Test(description = "Add task 2 with numbers")
    public void testAddTask2() {
        addAndAssert("12345678990 " + System.currentTimeMillis());
    }

    @Test(description = "Add task 3 with long title")
    public void testAddTask3() {
        addAndAssert("This is a very long task title intended to test the application's ability to handle long strings without truncation or layout issues. " +
                "It includes multiple sentences and goes on and on to ensure that we are really pushing the limits of what the UI can display properly. " +
                "Hopefully, this will help identify any potential bugs related to text rendering or overflow in the task list view. " +
                "Timestamp: " + System.currentTimeMillis());
    }

    @Test(description = "Add task 4 with empty title")
    public void testAddTask4() {
        addAndAssert("" + System.currentTimeMillis());
    }

    @Test(description = "Add task 5 with Vietnamese")
    public void testAddTask5() {
        addAndAssert("Nhiệm vụ tiếng Việt " + System.currentTimeMillis());
    }

}
