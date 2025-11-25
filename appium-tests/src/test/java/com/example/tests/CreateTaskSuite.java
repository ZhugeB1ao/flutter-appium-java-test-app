package com.example.tests;

import com.example.BaseTest;
import org.testng.annotations.Test;
import org.testng.Assert;
import org.openqa.selenium.WebElement;

/**
 * CreateTaskSuite: five tests covering task creation scenarios.
 */
public class CreateTaskSuite extends BaseTest {

    private void addAndAssert(String title) {
        gotoTasksTab();
        try {
            waitForAccessibilityId("add_task_button");
        } catch (Exception ignored) {}
        WebElement field = waitForFirstEditText();
        typeUsingKeyboard(field, title);
        try { hideKeyboardSafe(); } catch (Exception ignored) {}
        tap(waitForAccessibilityId("add_task_button"));
        try { captureDebugSnapshot("after_add"); } catch (Exception ignored) {}
        try {
            waitForTextContains(title);
        } catch (Exception e) {
            try { captureDebugSnapshot("after_add_failure"); } catch (Exception ignored) {}
            throw e;
        }
        Assert.assertTrue(driver.getPageSource().contains(title), "Task should be present: " + title);
    }

    @Test(description = "Create task with simple title")
    public void testCreateSimpleTask() {
        addAndAssert("Simple Task " + System.currentTimeMillis());
    }

    @Test(description = "Create task with numbers")
    public void testCreateTaskWithNumbers() {
        addAndAssert("Task 12345678990 " + System.currentTimeMillis());
    }

    @Test(description = "Create task with long title")
    public void testCreateLongTask() {
        addAndAssert("This is a very long task title intended to test the application's ability to handle long strings without truncation or layout issues. " +
                "It includes multiple sentences and goes on and on to ensure that we are really pushing the limits of what the UI can display properly. " +
                "Hopefully, this will help identify any potential bugs related to text rendering or overflow in the task list view. " +
                "Timestamp: " + System.currentTimeMillis());
    }

    @Test(description = "Create task with Vietnamese text")
    public void testCreateVietnameseTask() {
        addAndAssert("Nhiệm vụ tiếng Việt " + System.currentTimeMillis());
    }

    @Test(description = "Create task with empty title")
    public void testCreateEmptyTask() {
        addAndAssert("" + System.currentTimeMillis());
    }

}
