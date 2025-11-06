package com.example.tests;

import com.example.BaseTest;
import com.example.pages.HomePage;
import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Test suite for the counter functionality.
 * Demonstrates test structure: fixtures (@BeforeClass in BaseTest), hooks (@BeforeMethod),
 * selectors (in HomePage), actions (tapIncrement), and assertions (TestNG asserts).
 */
public class CounterTest extends BaseTest {
    private HomePage home;

    @BeforeMethod(alwaysRun = true)
    public void beforeEach() {
        // Re-create page object using the driver from BaseTest
        home = new HomePage((AndroidDriver) driver);
    }

    @Test(description = "Increment counter once and verify")
    public void testIncrementOnce() {
        // Arrange: read initial value
        String before = "";
        try {
            before = home.readCounter();
        } catch (Exception e) {
            // If cannot read counter, fail with helpful message
            Assert.fail("Failed to read counter before action: " + e.getMessage());
        }

        int beforeVal;
        try {
            beforeVal = Integer.parseInt(before.trim());
        } catch (Exception e) {
            // If parsing fails, assume 0
            beforeVal = 0;
        }

        // Act: tap increment once
        home.tapIncrement();

        // Assert: counter increased by 1
        String after = home.readCounter();
        int afterVal;
        try {
            afterVal = Integer.parseInt(after.trim());
        } catch (Exception e) {
            Assert.fail("Failed to parse counter after action: " + e.getMessage());
            return; // unreachable, but keeps compiler happy
        }

        Assert.assertEquals(afterVal, beforeVal + 1, "Counter should increment by 1 after tapping increment button");
    }

    @Test(description = "Increment counter three times and verify")
    public void testIncrementThreeTimes() {
        int clicks = 3;
        int beforeVal;
        try {
            beforeVal = Integer.parseInt(home.readCounter().trim());
        } catch (Exception e) {
            beforeVal = 0;
        }

        for (int i = 0; i < clicks; i++) {
            home.tapIncrement();
        }

        int afterVal;
        try {
            afterVal = Integer.parseInt(home.readCounter().trim());
        } catch (Exception e) {
            Assert.fail("Failed to parse counter after multiple increments: " + e.getMessage());
            return;
        }

        Assert.assertEquals(afterVal, beforeVal + clicks, "Counter should increase by number of clicks");
    }
}
