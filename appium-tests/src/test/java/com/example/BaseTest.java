package com.example;

import io.appium.java_client.MobileBy;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.options.UiAutomator2Options;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.time.Instant;

/**
 * BaseTest: contains fixtures and hooks for Appium tests.
 *
 * Usage: extend this class in your test classes. The setup reads common properties from
 * system properties so you can run with -DappPath=... -DdeviceName=... -Dudid=... etc.
 */
public abstract class BaseTest {
	protected AndroidDriver driver;
	protected WebDriverWait wait;

	@BeforeClass(alwaysRun = true)
	public void setUp() throws MalformedURLException {
		String appPath = System.getProperty("appPath", "");
		String deviceName = System.getProperty("deviceName", "Android Emulator");
		String udid = System.getProperty("udid", "");
		String platformVersion = System.getProperty("platformVersion", "");

		UiAutomator2Options options = new UiAutomator2Options()
				.setPlatformName("Android")
				.setAutomationName("UiAutomator2")
				.setDeviceName(deviceName)
				.setNewCommandTimeout(Duration.ofSeconds(300));

		if (!appPath.isEmpty()) {
			options.setApp(appPath);
		}
		if (!udid.isEmpty()) {
			options.setUdid(udid);
		}
		if (!platformVersion.isEmpty()) {
			options.setPlatformVersion(platformVersion);
		}

		// Appium server URL can be customized with -DappiumServer
		String appiumServer = System.getProperty("appiumServer", "http://127.0.0.1:4723");
		driver = new AndroidDriver(new URL(appiumServer), options);
	// Slightly longer default wait to accommodate emulator/UI delays
	wait = new WebDriverWait(driver, Duration.ofSeconds(25));
	}

	@AfterClass(alwaysRun = true)
	public void tearDown() {
		if (driver != null) {
			driver.quit();
		}
	}

	// Helper: wait and find by accessibility id
	protected WebElement waitForAccessibilityId(String id) {
		return wait.until(ExpectedConditions.presenceOfElementLocated(MobileBy.AccessibilityId(id)));
	}

	// Helper: wait and find by id (resource-id)
	protected WebElement waitForId(String id) {
		return wait.until(ExpectedConditions.presenceOfElementLocated(MobileBy.id(id)));
	}

	// Helper: wait and find the first EditText on screen (useful when accessibility ids are missing)
	protected WebElement waitForFirstEditText() {
		return wait.until(ExpectedConditions.presenceOfElementLocated(MobileBy.className("android.widget.EditText")));
	}

	// Helper: wait and return the N-th EditText (0-based). Returns when that index is present.
	protected WebElement waitForNthEditText(int index) {
		return wait.until(driver -> {
			java.util.List<WebElement> els = driver.findElements(MobileBy.className("android.widget.EditText"));
			if (els.size() > index) return els.get(index);
			return null;
		});
	}

	// Helper: tap using element
	protected void tap(WebElement el) {
		el.click();
	}

	// Helper: hide keyboard if visible (safe)
	protected void hideKeyboardSafe() {
		try {
			if (driver != null) {
				driver.hideKeyboard();
			}
		} catch (Exception ignored) {
		}
	}

	// Helper: wait for exact visible text
	protected WebElement waitForTextExact(String text) {
		try {
			return wait.until(ExpectedConditions.presenceOfElementLocated(MobileBy.AndroidUIAutomator("new UiSelector().text(\"" + text + "\")")));
		} catch (Exception e) {
			// fallback to XPath exact match
			return wait.until(ExpectedConditions.presenceOfElementLocated(MobileBy.xpath("//*[@text='" + text + "']")));
		}
	}

	// Helper: wait for text contains (substring match)
	protected WebElement waitForTextContains(String text) {
		try {
			return wait.until(ExpectedConditions.presenceOfElementLocated(MobileBy.AndroidUIAutomator("new UiSelector().textContains(\"" + text + "\")")));
		} catch (Exception e) {
			return wait.until(ExpectedConditions.presenceOfElementLocated(MobileBy.xpath("//*[contains(@text,'" + text + "')]")));
		}
	}

	// Helper: wait for description (content-desc) contains
	protected WebElement waitForDescriptionContains(String text) {
		try {
			return wait.until(ExpectedConditions.presenceOfElementLocated(MobileBy.AndroidUIAutomator("new UiSelector().descriptionContains(\"" + text + "\")")));
		} catch (Exception e) {
			// AccessibilityId only supports exact match, so keep descriptionContains via UiAutomator
			return wait.until(ExpectedConditions.presenceOfElementLocated(MobileBy.AndroidUIAutomator("new UiSelector().descriptionContains(\"" + text + "\")")));
		}
	}

	// Helper: wait until the page source contains a given substring. Returns when found or throws on timeout.
	protected void waitForTextInPageSource(String text, Duration timeout) {
		Instant deadline = Instant.now().plus(timeout);
		while (Instant.now().isBefore(deadline)) {
			try {
				String src = driver.getPageSource();
				if (src != null && src.contains(text)) {
					return;
				}
			} catch (Exception ignored) {}
			try { Thread.sleep(300); } catch (InterruptedException ignored) {}
		}
		throw new org.openqa.selenium.TimeoutException("Timed out waiting for text in page source: " + text);
	}

	// Helper: tap an element by text contains, with a basic scroll attempt first
	protected void tapByTextContains(String text) {
		try {
			// Try to scroll it into view in case it's off-screen
			driver.findElement(MobileBy.AndroidUIAutomator(
					"new UiScrollable(new UiSelector().scrollable(true)).scrollIntoView(" +
					"new UiSelector().textContains(\"" + text + "\"))"));
		} catch (Exception ignored) {}
		try {
			WebElement el = driver.findElement(MobileBy.AndroidUIAutomator("new UiSelector().textContains(\"" + text + "\")"));
			el.click();
			return;
		} catch (Exception ignored) {}
		WebElement el = driver.findElement(MobileBy.xpath("//*[contains(@text,'" + text + "')]"));
		el.click();
	}

	// Helper: try to scroll into view an element containing the text. No-op if no scrollable container.
	protected void scrollIntoViewByTextContains(String text) {
		try {
			// UiScrollable will throw if no scrollable is present; that's fine
			driver.findElement(MobileBy.AndroidUIAutomator(
					"new UiScrollable(new UiSelector().scrollable(true)).scrollIntoView(" +
					"new UiSelector().textContains(\"" + text + "\"))"));
		} catch (Exception ignored) {}
	}

	// Helper: switch bottom tab by its label using multiple strategies (accessibility id, description, text)
	protected void gotoTabByText(String label) {
		// Try accessibility id (content-desc)
		try {
			WebElement tab = driver.findElement(MobileBy.AccessibilityId(label));
			tab.click();
			Thread.sleep(200);
			return;
		} catch (Exception ignored) {}
		// Try descriptionContains
		try {
			WebElement tab = driver.findElement(MobileBy.AndroidUIAutomator("new UiSelector().descriptionContains(\"" + label + "\")"));
			tab.click();
			Thread.sleep(200);
			return;
		} catch (Exception ignored) {}
		// Try visible text
		try {
			WebElement tab = driver.findElement(MobileBy.AndroidUIAutomator("new UiSelector().text(\"" + label + "\")"));
			tab.click();
			Thread.sleep(200);
			return;
		} catch (Exception ignored) {}
		try {
			WebElement tab = driver.findElement(MobileBy.AndroidUIAutomator("new UiSelector().textContains(\"" + label + "\")"));
			tab.click();
			Thread.sleep(200);
		} catch (Exception ignored) {}
	}

	protected void gotoTasksTab() { gotoTabByText("Tasks"); }

	protected void gotoHomeTab() { gotoTabByText("Home"); }

	protected void gotoSettingsTab() { gotoTabByText("Settings"); }
}

