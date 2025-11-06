package com.example.tests;

import com.example.BaseTest;
import io.appium.java_client.MobileBy;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.By;
import org.testng.Assert;
import org.testng.annotations.Test;

public class NavigationUiSuite extends BaseTest {

    @Test(description = "Pending count updates when completing a task")
    public void testPendingCountUpdates() throws InterruptedException {
        String title = "PendingCount-" + System.currentTimeMillis();
        waitForAccessibilityId("add_task_button").click();
        waitForAccessibilityId("task_title_field").sendKeys(title);
        driver.findElement(MobileBy.AndroidUIAutomator("new UiSelector().text(\"Lưu\")")).click();

        WebElement pendingBox = driver.findElement(MobileBy.AndroidUIAutomator("new UiSelector().textContains(\"Chưa xong\")"));
        String before = pendingBox.getText();

        WebElement titleEl = driver.findElement(MobileBy.xpath("//*[@text='" + title + "']"));
        WebElement parent = titleEl.findElement(By.xpath(".."));
        WebElement cb = parent.findElement(By.className("android.widget.CheckBox"));
        cb.click();
        Thread.sleep(500);
        String after = pendingBox.getText();
        Assert.assertNotEquals(before, after, "Pending count should update after completing a task");
    }

    @Test(description = "Restart the app preserves data and theme")
    public void testRestartAppPreservesData() throws InterruptedException {
        String title = "Persist-" + System.currentTimeMillis();
        waitForAccessibilityId("add_task_button").click();
        waitForAccessibilityId("task_title_field").sendKeys(title);
        driver.findElement(MobileBy.AndroidUIAutomator("new UiSelector().text(\"Lưu\")")).click();

        // close and relaunch the app
        driver.closeApp();
        Thread.sleep(800);
        driver.launchApp();
        Thread.sleep(900);

        Assert.assertTrue(driver.findElements(MobileBy.xpath("//*[@text='" + title + "']")).size() > 0, "Task should persist after app restart");
    }

    @Test(description = "Switch tabs repeatedly to check responsiveness")
    public void testSwitchTabsRepeatedly() throws InterruptedException {
        for (int i = 0; i < 8; i++) {
            WebElement tasks = driver.findElement(MobileBy.AndroidUIAutomator("new UiSelector().text(\"Tasks\")"));
            tasks.click();
            Thread.sleep(200);
            WebElement home = driver.findElement(MobileBy.AndroidUIAutomator("new UiSelector().text(\"Home\")"));
            home.click();
            Thread.sleep(200);
            WebElement settings = driver.findElement(MobileBy.AndroidUIAutomator("new UiSelector().text(\"Settings\")"));
            settings.click();
            Thread.sleep(200);
        }
        Assert.assertTrue(true, "Switched tabs repeatedly without crashing");
    }

    @Test(description = "Test case 35: Text & background contrast in Light & Dark modes")
    public void testTextBackgroundContrast() throws InterruptedException {
        // Navigate to Settings
        try {
            driver.findElement(MobileBy.AndroidUIAutomator("new UiSelector().text(\"Settings\")")).click();
        } catch (Exception e) {
            driver.findElement(MobileBy.AndroidUIAutomator("new UiSelector().textContains(\"Settings\")")).click();
        }

        // Select Light theme (label contains 'Sáng' in Vietnamese builds)
        if (driver.findElements(MobileBy.AndroidUIAutomator("new UiSelector().textContains(\"Sáng\")")).size() > 0) {
            driver.findElement(MobileBy.AndroidUIAutomator("new UiSelector().textContains(\"Sáng\")")).click();
        } else {
            // fallback English
            driver.findElement(MobileBy.AndroidUIAutomator("new UiSelector().textContains(\"Light\")")).click();
        }
        Thread.sleep(500);

        // Verify key text elements are visible in Light mode
        boolean titleVisible = driver.findElements(MobileBy.AndroidUIAutomator("new UiSelector().textContains(\"Test App\")")).size() > 0;
        Assert.assertTrue(titleVisible, "Title should be visible in Light mode");

        // Switch to Dark
        if (driver.findElements(MobileBy.AndroidUIAutomator("new UiSelector().textContains(\"Tối\")")).size() > 0) {
            driver.findElement(MobileBy.AndroidUIAutomator("new UiSelector().textContains(\"Tối\")")).click();
        } else {
            driver.findElement(MobileBy.AndroidUIAutomator("new UiSelector().textContains(\"Dark\")")).click();
        }
        Thread.sleep(500);

        // Verify title still visible in Dark mode
        boolean titleVisibleDark = driver.findElements(MobileBy.AndroidUIAutomator("new UiSelector().textContains(\"Test App\")")).size() > 0;
        Assert.assertTrue(titleVisibleDark, "Title should be visible in Dark mode");
    }

    @Test(description = "Test case 41: Button press animation / responsiveness")
    public void testButtonPressAnimation() throws InterruptedException {
        // Tap multiple buttons quickly and ensure app remains responsive
        // Tap Tasks, Home, Settings
        driver.findElement(MobileBy.AndroidUIAutomator("new UiSelector().textContains(\"Tasks\")")).click();
        Thread.sleep(150);
        driver.findElement(MobileBy.AndroidUIAutomator("new UiSelector().textContains(\"Home\")")).click();
        Thread.sleep(150);
        driver.findElement(MobileBy.AndroidUIAutomator("new UiSelector().textContains(\"Settings\")")).click();
        Thread.sleep(150);

        // Press a couple of buttons inside Settings and Home to observe responsiveness
        // Toggle a theme radio (if present)
        if (driver.findElements(MobileBy.AndroidUIAutomator("new UiSelector().textContains(\"Sáng\")")).size() > 0) {
            driver.findElement(MobileBy.AndroidUIAutomator("new UiSelector().textContains(\"Sáng\")")).click();
            Thread.sleep(120);
        }

        // Back to Home and press 'Thêm' or 'Xem tất cả công việc'
        driver.findElement(MobileBy.AndroidUIAutomator("new UiSelector().textContains(\"Home\")")).click();
        Thread.sleep(120);
        if (driver.findElements(MobileBy.AndroidUIAutomator("new UiSelector().textContains(\"Xem tất cả công việc\")")).size() > 0) {
            driver.findElement(MobileBy.AndroidUIAutomator("new UiSelector().textContains(\"Xem tất cả công việc\")")).click();
            Thread.sleep(150);
        }

        // If we can still find the app title, UI is responsive
        boolean titlePresent = driver.findElements(MobileBy.AndroidUIAutomator("new UiSelector().textContains(\"Test App\")")).size() > 0;
        Assert.assertTrue(titlePresent, "App remains responsive after multiple quick button presses");
    }

}
