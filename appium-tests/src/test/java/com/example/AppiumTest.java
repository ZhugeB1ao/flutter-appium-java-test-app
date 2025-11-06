package com.example;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.options.UiAutomator2Options;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.net.MalformedURLException;
import java.net.URL;

public class AppiumTest {
    private AndroidDriver driver;

    @BeforeClass
    public void setUp() throws MalformedURLException {
        // Read app path from system property so user can pass -DappPath=... when running
        String appPath = System.getProperty("appPath", "/path/to/app-debug.apk");

        UiAutomator2Options options = new UiAutomator2Options()
                .setPlatformName("Android")
                .setAutomationName("UiAutomator2")
                .setDeviceName("Android Emulator")
                .setApp(appPath)
                .setNewCommandTimeout(java.time.Duration.ofSeconds(300));

        driver = new AndroidDriver(new URL("http://127.0.0.1:4723"), options);
    }

    @Test
    public void simpleSmokeTest() {
        // This test expects your Flutter widgets to expose accessibility labels (semanticsLabel)
        // Example: find an element with accessibilityId "increment" and click it.
        try {
            WebElement btn = (WebElement) driver.findElement(AppiumBy.accessibilityId("increment"));
            btn.click();

            // Basic assertion: page source is non-empty (replace with real assertions)
            String src = driver.getPageSource();
            Assert.assertTrue(src != null && src.length() > 0, "Page source should not be empty");
        } catch (Exception e) {
            // If element not found, fail the test with the exception message
            Assert.fail("Test failed: " + e.getMessage());
        }
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}
