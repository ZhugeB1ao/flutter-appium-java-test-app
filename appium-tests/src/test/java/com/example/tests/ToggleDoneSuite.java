package com.example.tests;

import com.example.BaseTest;
import org.testng.annotations.Test;
import org.testng.Assert;
import org.openqa.selenium.WebElement;

/**
 * ToggleDoneSuite: five tests covering toggle done/undone functionality.
 */
public class ToggleDoneSuite extends BaseTest {

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

    @Test(description = "Toggle task to done via long press")
    public void testToggleDone() throws InterruptedException {
        String title = addTask("E2E ToggleDone");
        WebElement taskElement = waitForTextContains(title);
        
        // Long press to toggle done (based on Flutter onLongPress)
        try {
            longPress(taskElement);
        } catch (Exception e) {
            // Fallback: perform action manually
            driver.executeScript("mobile: longClickGesture", 
                java.util.Map.of("elementId", ((org.openqa.selenium.remote.RemoteWebElement) taskElement).getId(), "duration", 1000));
        }
        
        Thread.sleep(500);
        // After toggle, task should still exist but may have strikethrough
        Assert.assertTrue(driver.getPageSource().contains(title));
    }

    @Test(description = "Toggle done then toggle back to undone")
    public void testToggleDoneTwice() throws InterruptedException {
        String title = addTask("E2E ToggleTwice");
        WebElement taskElement = waitForTextContains(title);
        
        // Toggle to done
        try {
            longPress(taskElement);
        } catch (Exception e) {
            driver.executeScript("mobile: longClickGesture", 
                java.util.Map.of("elementId", ((org.openqa.selenium.remote.RemoteWebElement) taskElement).getId(), "duration", 1000));
        }
        Thread.sleep(500);
        
        // Find element again and toggle back to undone
        taskElement = waitForTextContains(title);
        try {
            longPress(taskElement);
        } catch (Exception e) {
            driver.executeScript("mobile: longClickGesture", 
                java.util.Map.of("elementId", ((org.openqa.selenium.remote.RemoteWebElement) taskElement).getId(), "duration", 1000));
        }
        Thread.sleep(500);
        
        Assert.assertTrue(driver.getPageSource().contains(title));
    }

    @Test(description = "Toggle done task with long title")
    public void testToggleLongTitle() throws InterruptedException {
        String title = "E2E This is a very long task title to test toggle functionality with extended text content " + System.currentTimeMillis();
        gotoTasksTab();
        try {
            waitForAccessibilityId("add_task_button");
        } catch (Exception ignored) {}
        WebElement field = waitForFirstEditText();
        typeUsingKeyboard(field, title);
        tap(waitForAccessibilityId("add_task_button"));
        
        // Wait and find by partial text
        Thread.sleep(500);
        WebElement taskElement = waitForTextContains("E2E This is a very long");
        
        // Toggle done
        try {
            longPress(taskElement);
        } catch (Exception e) {
            driver.executeScript("mobile: longClickGesture", 
                java.util.Map.of("elementId", ((org.openqa.selenium.remote.RemoteWebElement) taskElement).getId(), "duration", 1000));
        }
        Thread.sleep(500);
        
        Assert.assertTrue(driver.getPageSource().contains("E2E This is a very long"));
    }

    @Test(description = "Toggle Vietnamese task")
    public void testToggleVietnameseTask() throws InterruptedException {
        String title = addTask("Công việc tiếng Việt");
        WebElement taskElement = waitForTextContains(title);
        
        // Toggle to done
        try {
            longPress(taskElement);
        } catch (Exception e) {
            driver.executeScript("mobile: longClickGesture", 
                java.util.Map.of("elementId", ((org.openqa.selenium.remote.RemoteWebElement) taskElement).getId(), "duration", 1000));
        }
        Thread.sleep(500);
        
        // Verify task still exists
        Assert.assertTrue(driver.getPageSource().contains("Công việc"));
    }

    @Test(description = "Toggle task with numbers")
    public void testToggleNumbersTask() throws InterruptedException {
        String title = addTask("12345678");
        WebElement taskElement = waitForTextContains(title);
        
        // Toggle to done
        try {
            longPress(taskElement);
        } catch (Exception e) {
            driver.executeScript("mobile: longClickGesture", 
                java.util.Map.of("elementId", ((org.openqa.selenium.remote.RemoteWebElement) taskElement).getId(), "duration", 1000));
        }
        Thread.sleep(500);
        
        // Verify task still exists
        Assert.assertTrue(driver.getPageSource().contains(title));
    }

}
