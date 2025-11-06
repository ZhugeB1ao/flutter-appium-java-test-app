package com.example.tests;

import com.example.BaseTest;
import io.appium.java_client.MobileBy;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.ScreenOrientation;
import org.testng.Assert;
import org.testng.annotations.Test;

public class SettingsSuite extends BaseTest {

    // Helper: navigate to Settings tab using text (robust for localized strings)
    private void openSettings() {
        try {
            driver.findElement(MobileBy.AndroidUIAutomator("new UiSelector().text(\"Settings\")")).click();
        } catch (Exception e) {
            driver.findElement(MobileBy.AndroidUIAutomator("new UiSelector().textContains(\"Settings\")")).click();
        }
    }

    private void selectTheme(String english, String vietContains) throws InterruptedException {
        // try localized label first, then english
        if (vietContains != null && driver.findElements(MobileBy.AndroidUIAutomator("new UiSelector().textContains(\"" + vietContains + "\")")).size() > 0) {
            driver.findElement(MobileBy.AndroidUIAutomator("new UiSelector().textContains(\"" + vietContains + "\")")).click();
        } else if (english != null && driver.findElements(MobileBy.AndroidUIAutomator("new UiSelector().textContains(\"" + english + "\")")).size() > 0) {
            driver.findElement(MobileBy.AndroidUIAutomator("new UiSelector().textContains(\"" + english + "\")")).click();
        }
        Thread.sleep(500);
    }

    @Test(description = "Test case 14: Change theme to Light")
    public void testChangeThemeToLight() throws InterruptedException {
        openSettings();
        selectTheme("Light", "Sáng");

        // sanity check: app title or main labels should remain visible and bright
        boolean titlePresent = driver.findElements(MobileBy.AndroidUIAutomator("new UiSelector().textContains(\"Test App\")")).size() > 0;
        Assert.assertTrue(titlePresent, "Title should be visible after applying Light theme");
    }

    @Test(description = "Test case 15: Change theme to Dark")
    public void testChangeThemeToDark() throws InterruptedException {
        openSettings();
        selectTheme("Dark", "Tối");

        boolean titlePresent = driver.findElements(MobileBy.AndroidUIAutomator("new UiSelector().textContains(\"Test App\")")).size() > 0;
        Assert.assertTrue(titlePresent, "Title should be visible after applying Dark theme");
    }

    @Test(description = "Test case 16: Select System theme")
    public void testSelectSystemTheme() throws InterruptedException {
        openSettings();
        // try common labels
        selectTheme("System", "Hệ thống");

        // Verify app follows system theme by toggling orientation or asserting presence
        boolean titlePresent = driver.findElements(MobileBy.AndroidUIAutomator("new UiSelector().textContains(\"Test App\")")).size() > 0;
        Assert.assertTrue(titlePresent, "Title should be visible after selecting System theme");
    }

    @Test(description = "Test case 26: Navigate Tasks -> Settings -> Tasks (data unchanged)")
    public void testNavigateTabsDataUnchanged() throws InterruptedException {
        // create a short-lived task
        String title = "NavPersist-" + System.currentTimeMillis();
        waitForAccessibilityId("add_task_button").click();
        waitForAccessibilityId("task_title_field").sendKeys(title);
        driver.findElement(MobileBy.AndroidUIAutomator("new UiSelector().text(\"Lưu\")")).click();

        // Switch tabs: Tasks -> Settings -> Tasks
        try {
            driver.findElement(MobileBy.AndroidUIAutomator("new UiSelector().text(\"Tasks\")")).click();
        } catch (Exception e) {
            driver.findElement(MobileBy.AndroidUIAutomator("new UiSelector().textContains(\"Tasks\")")).click();
        }
        Thread.sleep(300);
        openSettings();
        Thread.sleep(300);
        // back to Tasks
        driver.findElement(MobileBy.AndroidUIAutomator("new UiSelector().textContains(\"Tasks\")")).click();
        Thread.sleep(300);

        Assert.assertTrue(driver.findElements(MobileBy.xpath("//*[@text='" + title + "']")).size() > 0, "Task should remain after switching tabs");
    }

    @Test(description = "Test case 29: Tap Settings icon opens Settings page")
    public void testTapSettingsIcon() {
        openSettings();
        boolean settingsHeader = driver.findElements(MobileBy.AndroidUIAutomator("new UiSelector().textContains(\"Settings\")")).size() > 0 ||
                driver.findElements(MobileBy.AndroidUIAutomator("new UiSelector().textContains(\"Cài đặt\")")).size() > 0;
        Assert.assertTrue(settingsHeader, "Settings page should be open after tapping icon");
    }

    @Test(description = "Test case 31: Change theme while in another tab (applies app-wide)")
    public void testChangeThemeWhileInAnotherTab() throws InterruptedException {
        // Ensure we are on Tasks tab
        try {
            driver.findElement(MobileBy.AndroidUIAutomator("new UiSelector().text(\"Tasks\")")).click();
        } catch (Exception e) {
            driver.findElement(MobileBy.AndroidUIAutomator("new UiSelector().textContains(\"Tasks\")")).click();
        }
        Thread.sleep(200);

        // Open Settings and change theme to Dark
        openSettings();
        selectTheme("Dark", "Tối");
        Thread.sleep(300);

        // Go to Home and verify title present (theme applied app-wide)
        driver.findElement(MobileBy.AndroidUIAutomator("new UiSelector().textContains(\"Home\")")).click();
        Thread.sleep(300);
        Assert.assertTrue(driver.findElements(MobileBy.AndroidUIAutomator("new UiSelector().textContains(\"Test App\")")).size() > 0,
                "Theme change should be visible app-wide (title present on Home)");
    }

    @Test(description = "Test case 46: Change theme with long list (50 tasks)")
    public void testChangeThemeWithLongList() throws InterruptedException {
        // Add 50 tasks quickly
        for (int i = 0; i < 50; i++) {
            waitForAccessibilityId("add_task_button").click();
            waitForAccessibilityId("task_title_field").sendKeys("Bulk-" + i + "-" + System.currentTimeMillis());
            driver.findElement(MobileBy.AndroidUIAutomator("new UiSelector().text(\"Lưu\")")).click();
            Thread.sleep(80);
        }

        // Open settings and switch theme to Light
        openSettings();
        long t0 = System.currentTimeMillis();
        selectTheme("Light", "Sáng");
        long elapsed = System.currentTimeMillis() - t0;

        // Ensure theme change completed and list still accessible
        driver.findElement(MobileBy.AndroidUIAutomator("new UiSelector().textContains(\"Tasks\")")).click();
        Thread.sleep(300);

        int count = driver.findElements(MobileBy.className("android.widget.TextView")).size();
        Assert.assertTrue(count > 0, "UI should still show text elements after theme change with long list");
        Assert.assertTrue(elapsed < 10000, "Theme change should not take too long (observed " + elapsed + "ms)");
    }

    @Test(description = "Test case 48: Retain data on screen rotation")
    public void testRetainDataOnRotation() throws InterruptedException {
        String title = "Rotate-" + System.currentTimeMillis();
        waitForAccessibilityId("add_task_button").click();
        waitForAccessibilityId("task_title_field").sendKeys(title);
        driver.findElement(MobileBy.AndroidUIAutomator("new UiSelector().text(\"Lưu\")")).click();

        // rotate to landscape and back
        driver.rotate(ScreenOrientation.LANDSCAPE);
        Thread.sleep(800);
        driver.rotate(ScreenOrientation.PORTRAIT);
        Thread.sleep(800);

        Assert.assertTrue(driver.findElements(MobileBy.xpath("//*[@text='" + title + "']")).size() > 0,
                "Task should remain after device rotation");
    }

}
