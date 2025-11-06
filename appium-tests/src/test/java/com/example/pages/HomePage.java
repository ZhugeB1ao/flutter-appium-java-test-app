package com.example.pages;

import io.appium.java_client.AppiumBy;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebElement;
import org.openqa.selenium.remote.RemoteExecuteMethod;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import io.appium.java_client.android.AndroidDriver;

import java.time.Duration;

/**
 * Page Object for the main/home screen of the Flutter app.
 * Adjust selectors to match your app (accessibility id, resource-id, xpath, etc.).
 */
public class HomePage {
    private final AndroidDriver driver;
    private final WebDriverWait wait;

    // Selectors (replace these with values used in your Flutter app)
    private final String INCREMENT_BUTTON_ACCESSIBILITY = "increment"; // semanticsLabel
    private final String COUNTER_TEXT_ID = "com.example.app:id/counter"; // resource-id example
    private final String COUNTER_TEXT_ACCESSIBILITY = "counter"; // semantics label we add in Flutter

    public HomePage(AndroidDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(12));
    }

    public WebElement getIncrementButton() {
        try {
            return (WebElement) wait.until(ExpectedConditions.presenceOfElementLocated(AppiumBy.accessibilityId(INCREMENT_BUTTON_ACCESSIBILITY)));
        } catch (Exception e) {
            // fallback to resource-id if accessibility id not present
            return (WebElement) wait.until(ExpectedConditions.presenceOfElementLocated(AppiumBy.id(COUNTER_TEXT_ID.replace("counter", "increment_button"))));
        }
    }

    public WebElement getCounterText() {
        try {
            return (WebElement) wait.until(ExpectedConditions.presenceOfElementLocated(AppiumBy.accessibilityId(COUNTER_TEXT_ACCESSIBILITY)));
        } catch (Exception e) {
            return (WebElement) wait.until(ExpectedConditions.presenceOfElementLocated(AppiumBy.id(COUNTER_TEXT_ID)));
        }
    }

    public void tapIncrement() {
        getIncrementButton().click();
    }

    public String readCounter() {
        return getCounterText().getText();
    }
}
