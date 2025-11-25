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
        Thread.sleep(300);
        Assert.assertFalse(driver.getPageSource().contains("Xóa nhiệm vụ"));
    }

    @Test(description = "Delete first task in list")
    public void testDeleteFirstTask() throws InterruptedException {
        String title = addTask("Delete First");
        WebElement el = waitForTextContains(title);
        swipeLeft(el);
        Thread.sleep(300);
        Assert.assertFalse(driver.getPageSource().contains(title));
    }

    @Test(description = "Delete task immediately after creation")
    public void testDeleteAfterCreation() throws InterruptedException {
        String title = addTask("Delete Immediate");
        WebElement el = waitForTextContains(title);
        swipeLeft(el);
        Thread.sleep(300);
        Assert.assertFalse(driver.getPageSource().contains(title));
    }

    @Test(description = "Delete multiple tasks")
    public void testDeleteMultipleTasks() throws InterruptedException {
        String title1 = addTask("Delete Multi 1");
        String title2 = addTask("Delete Multi 2");
        
        // Delete first task
        WebElement el1 = waitForTextContains(title1);
        swipeLeft(el1);
        Thread.sleep(300);
        Assert.assertFalse(driver.getPageSource().contains(title1));
        
        // Delete second task
        WebElement el2 = waitForTextContains(title2);
        swipeLeft(el2);
        Thread.sleep(300);
        Assert.assertFalse(driver.getPageSource().contains(title2));
    }

    @Test(description = "Delete task with simple title")
    public void testDeleteSimpleTask() throws InterruptedException {
        String title = addTask("Simple Delete");
        WebElement el = waitForTextContains(title);
        swipeLeft(el);
        Thread.sleep(300);
        Assert.assertFalse(driver.getPageSource().contains(title));
    }

}
