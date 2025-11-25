package com.example.tests;

import com.example.BaseTest;
import org.testng.annotations.Test;
import org.testng.Assert;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.By;
import java.util.List;

/**
 * EditDeleteInlineSuite: five tests covering inline edit and delete flows.
 */
public class EditDeleteInlineSuite extends BaseTest {

    private String addTask(String base) {
        String title = base + " " + System.currentTimeMillis();
        gotoTasksTab();
        // Try to ensure add button present, but type into the first EditText since Flutter TextField may not map to accessibility id
        try {
            waitForAccessibilityId("add_task_button");
        } catch (Exception ignored) {}
        WebElement field = waitForFirstEditText();
        typeUsingKeyboard(field, title);
        tap(waitForAccessibilityId("add_task_button"));
        waitForTextContains(title);
        return title;
    }

    @Test(description = "Edit a task title inline")
    public void testEditTaskTitleInline() throws InterruptedException {
        String title = addTask("E2E EditTitle");
        // Find the task text element
        WebElement taskText = waitForTextContains(title);
        
        // Double-click directly on the text to trigger edit mode
        try {
            taskText.click();
            Thread.sleep(150);
            taskText.click();
            Thread.sleep(300); // Wait for edit field to appear
        } catch (Exception e) {
            taskText.click();
        }
        
        // After double-click, the TextField should replace the Text in the same ListTile
        // Find all EditText fields and use the one that's NOT the add field (index > 0)
        List<WebElement> editFields = driver.findElements(By.className("android.widget.EditText"));
        WebElement edit = null;
        for (int i = editFields.size() - 1; i >= 0; i--) {
            WebElement field = editFields.get(i);
            String text = field.getText();
            if (text != null && text.contains("EditTitle")) {
                edit = field;
                break;
            }
        }
        
        if (edit == null && editFields.size() > 1) {
            // Fallback: get the last EditText (newest one, which is the inline edit field)
            edit = editFields.get(editFields.size() - 1);
        }
        
        if (edit == null) {
            throw new RuntimeException("Could not find edit field after double-tap");
        }
        
        edit.clear();
        String updated = title + " updated";
        typeUsingKeyboard(edit, updated);
        hideKeyboardSafe();
        waitForTextContains(updated);
        Assert.assertTrue(driver.getPageSource().contains(updated));
    }

    @Test(description = "Edit task with Vietnamese characters")
    public void testEditTaskVietnamese() throws InterruptedException {
        String title = addTask("E2E VietnameseEdit");
        WebElement taskText = waitForTextContains(title);
        
        // Double-click to enter edit mode
        try {
            taskText.click();
            Thread.sleep(150);
            taskText.click();
            Thread.sleep(300);
        } catch (Exception e) {
            taskText.click();
        }
        
        // Find the edit field that contains our task title
        List<WebElement> editFields = driver.findElements(By.className("android.widget.EditText"));
        WebElement edit = null;
        for (int i = editFields.size() - 1; i >= 0; i--) {
            WebElement field = editFields.get(i);
            String text = field.getText();
            if (text != null && text.contains("VietnameseEdit")) {
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
        String updated = "Nhiệm vụ tiếng Việt " + System.currentTimeMillis();
        typeUsingKeyboard(edit, updated);
        hideKeyboardSafe();
        waitForTextContains(updated);
        Assert.assertTrue(driver.getPageSource().contains(updated));
    }

    @Test(description = "Edit task to empty title")
    public void testEditTaskToEmpty() throws InterruptedException {
        String title = addTask("E2E EditEmpty");
        WebElement taskText = waitForTextContains(title);
        
        // Double-click to enter edit mode
        try {
            taskText.click();
            Thread.sleep(150);
            taskText.click();
            Thread.sleep(300);
        } catch (Exception e) {
            taskText.click();
        }
        
        // Find the edit field
        List<WebElement> editFields = driver.findElements(By.className("android.widget.EditText"));
        WebElement edit = null;
        for (int i = editFields.size() - 1; i >= 0; i--) {
            WebElement field = editFields.get(i);
            String text = field.getText();
            if (text != null && text.contains("EditEmpty")) {
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
        
        // Clear and submit empty (should keep original or reject)
        edit.clear();
        typeUsingKeyboard(edit, "");
        hideKeyboardSafe();
        try { Thread.sleep(500); } catch (InterruptedException ignored) {}
        // Flutter implementation keeps original title if empty, so verify original still exists
        Assert.assertTrue(driver.getPageSource().contains(title));
    }

    @Test(description = "Delete a task using delete button")
    public void testDeleteTaskButton() throws InterruptedException {
        String title = addTask("E2E DeleteBtn");
        waitForTextContains(title);
        // Try direct delete button
        try {
            WebElement del = waitForAccessibilityId("delete_task_button");
            tap(del);
        } catch (Exception e) {
            // fallback: find task element and swipe or click remove inside parent
            WebElement el = waitForTextContains(title);
            try { swipeLeft(el); } catch (Exception ignored) {}
        }
        // Wait and assert gone
        try { Thread.sleep(600); } catch (InterruptedException ignored) {}
        Assert.assertFalse(driver.getPageSource().contains(title));
    }

    @Test(description = "Delete a task via swipe reveal")
    public void testDeleteTaskSwipe() throws InterruptedException {
        String title = addTask("E2E DeleteSwipe");
        WebElement el = waitForTextContains(title);
        swipeLeft(el);
        // After swipe, expect delete affordance or immediate removal
        try { Thread.sleep(400); } catch (InterruptedException ignored) {}
        Assert.assertFalse(driver.getPageSource().contains(title));
    }

}
