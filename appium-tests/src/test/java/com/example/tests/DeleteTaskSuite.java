package com.example.tests;

import com.example.BaseTest;
import org.testng.annotations.Test;
import org.testng.Assert;
import org.openqa.selenium.WebElement;

/**
 * DeleteTaskSuite: five tests covering task deletion scenarios.
 */
public class DeleteTaskSuite extends BaseTest {

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

    @Test(description = "Delete task with Vietnamese title")
    public void testDeleteVietnameseTask() throws InterruptedException {
        String title = "Xóa nhiệm vụ " + System.currentTimeMillis();
        gotoTasksTab();
        try {
            waitForAccessibilityId("add_task_button");
        } catch (Exception ignored) {}
        WebElement field = waitForFirstEditText();
        typeUsingKeyboard(field, title);
        tap(waitForAccessibilityId("add_task_button"));
        waitForTextContains(title);
        
        WebElement el = waitForTextContains(title);
        swipeLeft(el);
        Thread.sleep(200);
        Assert.assertFalse(driver.getPageSource().contains("Xóa nhiệm vụ"));
    }

    @Test(description = "Delete oldest task when multiple exist")
    public void testDeleteOldestTask() throws InterruptedException {
        String title1 = addTask("Old Task 1");
        String title2 = addTask("Old Task 2");
        String title3 = addTask("Old Task 3");
        
		// Delete the first (oldest) task
		WebElement el1 = waitForTextContains(title1);
		swipeLeft(el1);
		Thread.sleep(200);
		Assert.assertFalse(driver.getPageSource().contains(title1));        // Verify other tasks still exist
        Assert.assertTrue(driver.getPageSource().contains(title2));
        Assert.assertTrue(driver.getPageSource().contains(title3));
    }

    @Test(description = "Delete middle task when three exist")
    public void testDeleteMiddleTask() throws InterruptedException {
        String title1 = addTask("First Task");
        String title2 = addTask("Middle Task");
        String title3 = addTask("Last Task");
        
        // Delete the middle task
        WebElement el2 = waitForTextContains(title2);
        swipeLeft(el2);
        Thread.sleep(200);
        Assert.assertFalse(driver.getPageSource().contains(title2));
        
        // Verify first and last tasks still exist
        Assert.assertTrue(driver.getPageSource().contains(title1));
        Assert.assertTrue(driver.getPageSource().contains(title3));
    }

    @Test(description = "Delete all tasks sequentially")
    public void testDeleteAllTasks() throws InterruptedException {
        String title1 = addTask("Delete All 1");
        String title2 = addTask("Delete All 2");
        String title3 = addTask("Delete All 3");
        
		// Delete all tasks one by one
		WebElement el1 = waitForTextContains(title1);
		swipeLeft(el1);
		Thread.sleep(200);
		
		WebElement el2 = waitForTextContains(title2);
		swipeLeft(el2);
		Thread.sleep(200);
		
		WebElement el3 = waitForTextContains(title3);
		swipeLeft(el3);
		Thread.sleep(200);        // Verify all deleted
        Assert.assertFalse(driver.getPageSource().contains(title1));
        Assert.assertFalse(driver.getPageSource().contains(title2));
        Assert.assertFalse(driver.getPageSource().contains(title3));
    }

    @Test(description = "Delete task with numbers and special chars")
    public void testDeleteTaskWithSpecialContent() throws InterruptedException {
        String title = addTask("Task #123-456");
        WebElement el = waitForTextContains(title);
        swipeLeft(el);
        Thread.sleep(200);
        Assert.assertFalse(driver.getPageSource().contains("#123-456"));
    }

}
