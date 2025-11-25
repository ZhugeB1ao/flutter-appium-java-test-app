package com.example.tests;

import com.example.BaseTest;
import org.testng.annotations.Test;
import org.testng.Assert;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.By;
import java.util.List;

/**
 * UpdateTaskSuite: five tests covering task update scenarios.
 */
public class UpdateTaskSuite extends BaseTest {

    private String addTask(String base) {
        String title = base + " " + System.currentTimeMillis();
        gotoTasksTab();
        try {
            waitForAccessibilityId("add_task_button");
        } catch (Exception ignored) {}
        WebElement field = waitForFirstEditText();
        typeUsingKeyboard(field, title);
        tap(waitForAccessibilityId("add_task_button"));
        waitForTextContains(title);
        return title;
    }

    private void editTask(String originalTitle, String newTitle) throws InterruptedException {
        WebElement taskText = waitForTextContains(originalTitle);
        
		// Double-click to enter edit mode
		try {
			taskText.click();
			Thread.sleep(100);
			taskText.click();
			Thread.sleep(200);
		} catch (Exception e) {
			taskText.click();
		}        // Find the edit field that contains our task title
        List<WebElement> editFields = driver.findElements(By.className("android.widget.EditText"));
        WebElement edit = null;
        for (int i = editFields.size() - 1; i >= 0; i--) {
            WebElement field = editFields.get(i);
            String text = field.getText();
            if (text != null && text.contains(originalTitle.split(" ")[0])) {
                edit = field;
                break;
            }
        }
        
        if (edit == null && editFields.size() > 1) {
            edit = editFields.get(editFields.size() - 1);
        }
        
        if (edit == null) {
            throw new RuntimeException("Could not find edit field after double-tap");
        }
        
        edit.clear();
        typeUsingKeyboard(edit, newTitle);
        hideKeyboardSafe();
        waitForTextContains(newTitle);
        Assert.assertTrue(driver.getPageSource().contains(newTitle));
    }

    @Test(description = "Update task with simple text")
    public void testUpdateSimpleTask() throws InterruptedException {
        String title = addTask("Update Simple");
        String updated = title + " Updated";
        editTask(title, updated);
    }

    @Test(description = "Update task to Vietnamese")
    public void testUpdateToVietnamese() throws InterruptedException {
        String title = addTask("Update VN");
        String updated = "Nhiệm vụ đã cập nhật " + System.currentTimeMillis();
        editTask(title, updated);
    }

    @Test(description = "Update task to longer text")
    public void testUpdateToLongerText() throws InterruptedException {
        String title = addTask("Short");
        String updated = "This is now a much longer task title that replaces the original short one " + System.currentTimeMillis();
        editTask(title, updated);
    }

    @Test(description = "Update task with numbers")
    public void testUpdateWithNumbers() throws InterruptedException {
        String title = addTask("Update Num");
        String updated = "Task 999888777 " + System.currentTimeMillis();
        editTask(title, updated);
    }

    @Test(description = "Update task multiple times")
    public void testUpdateMultipleTimes() throws InterruptedException {
        String title = addTask("Multi Update");
        
        // First update
        String updated1 = title + " v1";
        editTask(title, updated1);
        
        // Second update
        String updated2 = updated1 + " v2";
        editTask(updated1, updated2);
        
        Assert.assertTrue(driver.getPageSource().contains(updated2));
    }

}
