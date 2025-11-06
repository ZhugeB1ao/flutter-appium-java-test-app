package com.example.tests;

import com.example.BaseTest;
import io.appium.java_client.MobileBy;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.OutputType;
import org.testng.Assert;
import org.testng.annotations.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class HomeOverviewSuite extends BaseTest {

    @Test(description = "Test case 25: Navigate Home -> Tasks -> Home and verify data is unchanged")
    public void testNavigateHomeTasksHome() throws InterruptedException {
        String title = "NavHome-" + System.currentTimeMillis();
        waitForAccessibilityId("add_task_button").click();
        waitForAccessibilityId("task_title_field").sendKeys(title);
        driver.findElement(MobileBy.AndroidUIAutomator("new UiSelector().text(\"Lưu\")")).click();

        // Switch to Tasks, then back to Home
        try {
            driver.findElement(MobileBy.AndroidUIAutomator("new UiSelector().text(\"Tasks\")")).click();
        } catch (Exception e) {
            driver.findElement(MobileBy.AndroidUIAutomator("new UiSelector().textContains(\"Tasks\")")).click();
        }
        Thread.sleep(300);
        driver.findElement(MobileBy.AndroidUIAutomator("new UiSelector().textContains(\"Home\")")).click();
        Thread.sleep(300);

        // Verify task still present in the list if overview shows recent tasks
        boolean present = driver.findElements(MobileBy.xpath("//*[@text='" + title + "']")).size() > 0;
        Assert.assertTrue(present, "Task should remain after switching between Home and Tasks");
    }

    @Test(description = "Test case 27: Tap Home icon navigates to Home")
    public void testTapHomeIcon() throws InterruptedException {
        // Try tapping the Home tab
        try {
            driver.findElement(MobileBy.AndroidUIAutomator("new UiSelector().text(\"Home\")")).click();
        } catch (Exception e) {
            driver.findElement(MobileBy.AndroidUIAutomator("new UiSelector().textContains(\"Home\")")).click();
        }
        Thread.sleep(300);

        boolean titlePresent = driver.findElements(MobileBy.AndroidUIAutomator("new UiSelector().textContains(\"Test App\")")).size() > 0;
        Assert.assertTrue(titlePresent, "Tapping Home should navigate to Home and show the app title/overview");
    }

    @Test(description = "Test case 37: Highlight 'Delete all tasks' button - check presence and contrast")
    public void testHighlightDeleteAllButton() throws InterruptedException, IOException {
        // Navigate to Settings
        try {
            driver.findElement(MobileBy.AndroidUIAutomator("new UiSelector().text(\"Settings\")")).click();
        } catch (Exception e) {
            driver.findElement(MobileBy.AndroidUIAutomator("new UiSelector().textContains(\"Settings\")")).click();
        }
        Thread.sleep(400);

        // Find the delete all button by likely labels (VN/EN)
        WebElement deleteBtn = null;
        if (driver.findElements(MobileBy.AndroidUIAutomator("new UiSelector().textContains(\"Xóa tất cả\")")).size() > 0) {
            deleteBtn = driver.findElement(MobileBy.AndroidUIAutomator("new UiSelector().textContains(\"Xóa tất cả\")"));
        } else if (driver.findElements(MobileBy.AndroidUIAutomator("new UiSelector().textContains(\"Xoá tất cả\")")).size() > 0) {
            deleteBtn = driver.findElement(MobileBy.AndroidUIAutomator("new UiSelector().textContains(\"Xoá tất cả\")"));
        } else if (driver.findElements(MobileBy.AndroidUIAutomator("new UiSelector().textContains(\"Delete all\")")).size() > 0) {
            deleteBtn = driver.findElement(MobileBy.AndroidUIAutomator("new UiSelector().textContains(\"Delete all\")"));
        } else if (driver.findElements(MobileBy.AndroidUIAutomator("new UiSelector().textContains(\"Delete\")")).size() > 0) {
            deleteBtn = driver.findElement(MobileBy.AndroidUIAutomator("new UiSelector().textContains(\"Delete\")"));
        }

        Assert.assertNotNull(deleteBtn, "Delete all tasks button should be present in Settings");

        // Save element screenshot for inspection and analyze average color for a heuristic
        File elShot = deleteBtn.getScreenshotAs(OutputType.FILE);
        File out = new File("appium-tests/target/screenshots/delete_button.png");
        out.getParentFile().mkdirs();
        elShot.renameTo(out);

        BufferedImage img = ImageIO.read(out);
        int w = img.getWidth();
        int h = img.getHeight();
        long rSum = 0, gSum = 0, bSum = 0, total = 0;
        for (int x = 0; x < w; x += Math.max(1, w/20)) {
            for (int y = 0; y < h; y += Math.max(1, h/20)) {
                int rgb = img.getRGB(x, y);
                int r = (rgb >> 16) & 0xff;
                int g = (rgb >> 8) & 0xff;
                int b = rgb & 0xff;
                rSum += r; gSum += g; bSum += b; total++;
            }
        }
        double avgR = rSum / (double) total;
        double avgG = gSum / (double) total;
        double avgB = bSum / (double) total;

        // Heuristic: button is strongly red if avgR significantly higher than G and B
        boolean isRed = (avgR > avgG + 20) && (avgR > avgB + 20);
        Assert.assertTrue(isRed, "Delete-all button background should be a standout (red) color - observed R=" + avgR + ", G=" + avgG + ", B=" + avgB);

        // Contrast check vs white text: compute relative luminance and contrast ratio (WCAG)
        double bgL = relativeLuminance(avgR, avgG, avgB);
        double whiteL = 1.0; // white
        double contrast = (whiteL + 0.05) / (bgL + 0.05);

        // Require contrast >= 4.5 for good readability; if lower, test fails (matches observation)
        Assert.assertTrue(contrast >= 4.5, "Contrast ratio vs white text too low: " + String.format("%.2f", contrast));
    }

    private double relativeLuminance(double r, double g, double b) {
        // r,g,b are 0-255; convert to 0-1 linear
        double rs = srgbToLinear(r/255.0);
        double gs = srgbToLinear(g/255.0);
        double bs = srgbToLinear(b/255.0);
        return 0.2126 * rs + 0.7152 * gs + 0.0722 * bs;
    }

    private double srgbToLinear(double c) {
        if (c <= 0.03928) return c / 12.92;
        return Math.pow((c + 0.055) / 1.055, 2.4);
    }

}
