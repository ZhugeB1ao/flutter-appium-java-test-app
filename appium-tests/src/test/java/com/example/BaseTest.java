package com.example;

import io.appium.java_client.MobileBy;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.options.UiAutomator2Options;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.ITestResult;
import io.appium.java_client.AppiumBy;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.time.Instant;
import org.openqa.selenium.remote.RemoteWebElement;
import java.util.Map;
import java.util.HashMap;
import java.io.File;
import java.io.FileWriter;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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
				.setNewCommandTimeout(Duration.ofSeconds(300))
				// Avoid reinstalling/resetting the app between sessions to speed up test runs
				.setNoReset(true)
				.setAutoGrantPermissions(true);

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
		// Default wait: configurable via -DwaitSeconds (seconds). Default 6s.
		long waitSeconds = 6;
		try {
			String ws = System.getProperty("waitSeconds", "6");
			waitSeconds = Long.parseLong(ws);
		} catch (Exception ignored) {}
		wait = new WebDriverWait(driver, Duration.ofSeconds(waitSeconds));
	}

	/**
	 * Capture a debug snapshot: page source and a screenshot saved under target/debug-snapshots.
	 * Files are named with the provided label and a timestamp.
	 */
	public void captureDebugSnapshot(String label) {
		if (driver == null) return;
		try {
			String base = System.getProperty("debugSnapshotDir", "target/debug-snapshots");
			Path dir = Paths.get(base);
			Files.createDirectories(dir);
			String ts = String.valueOf(Instant.now().toEpochMilli());
			String safeLabel = label.replaceAll("[^a-zA-Z0-9_\\-]", "_");
			// page source
			try {
				String src = driver.getPageSource();
				Path xmlPath = dir.resolve(safeLabel + "_" + ts + ".xml");
				try (FileWriter w = new FileWriter(xmlPath.toFile())) {
					w.write(src);
				}
			} catch (Exception ignored) {}
			// screenshot
			try {
				if (driver instanceof TakesScreenshot) {
					File tmp = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
					Path pngPath = dir.resolve(safeLabel + "_" + ts + ".png");
					Files.copy(tmp.toPath(), pngPath);
				}
			} catch (Exception ignored) {}
		} catch (Exception ignored) {}
	}

	@AfterClass(alwaysRun = true)
	public void tearDown() {
		if (driver != null) {
			driver.quit();
		}
	}

	/**
	 * After each test, attempt to close any open dialogs (press 'Huỷ' / Cancel) and
	 * return to the Tasks view so the next test runs from a known state.
	 */
	@AfterMethod(alwaysRun = true)
	public void afterEach(ITestResult result) {
		String testName = result.getMethod().getMethodName();
		String testClass = result.getTestClass().getName();
		long duration = result.getEndMillis() - result.getStartMillis();
		
		// Log test result
		switch (result.getStatus()) {
			case ITestResult.SUCCESS:
				System.out.println("\n✅ PASSED: " + testClass + "." + testName + " (" + duration + "ms)");
				break;
			case ITestResult.FAILURE:
				System.out.println("\n❌ FAILED: " + testClass + "." + testName + " (" + duration + "ms)");
				if (result.getThrowable() != null) {
					System.out.println("   Error: " + result.getThrowable().getMessage());
				}
				break;
			case ITestResult.SKIP:
				System.out.println("\n⏭️  SKIPPED: " + testClass + "." + testName);
				if (result.getThrowable() != null) {
					System.out.println("   Reason: " + result.getThrowable().getMessage());
				}
				break;
		}
		
		// App has no dialogs, just ensure keyboard is hidden
		try { hideKeyboardSafe(); } catch (Exception ignored) {}

		// Quick sanity check: if the Tasks add button isn't visible, the app may have navigated away or crashed.
		// Relaunch the app as a recovery step so subsequent tests start from a known state.
		try {
			try {
				waitForAccessibilityId("add_task_button");
			} catch (Exception e) {
				// attempt to relaunch app and wait briefly for main UI
				try {
					if (driver != null) {
						driver.launchApp();
						Thread.sleep(1000);
						// best-effort wait for add button
						waitForAccessibilityId("add_task_button");
					}
				} catch (Exception ignored) {}
			}
		} catch (Exception ignored) {}

		// If the test failed, capture a debug snapshot for investigation
		try {
			if (result != null && result.getStatus() != ITestResult.SUCCESS) {
				String name = (result.getMethod() != null) ? result.getMethod().getMethodName() : "failed_test";
				captureDebugSnapshot("failure_" + name);
			}
		} catch (Exception ignored) {}
	}

	/**
	 * Ensure the app is launched and main UI is present before each test.
	 */
	@BeforeMethod(alwaysRun = true)
	public void ensureAppReady() {
		if (driver == null) return;
		try {
			// Try to bring the app to foreground and wait for the add button
			driver.activateApp("com.example.test_app");
			waitForAccessibilityId("add_task_button");
		} catch (Exception ignored) {
			try {
				driver.launchApp();
				waitForAccessibilityId("add_task_button");
			} catch (Exception ignored2) {}
		}
	}

	protected WebElement waitForAccessibilityId(String id) {
		return wait.until(ExpectedConditions.presenceOfElementLocated(MobileBy.AccessibilityId(id)));
	}

	protected WebElement waitForFirstEditText() {
		return wait.until(ExpectedConditions.presenceOfElementLocated(MobileBy.className("android.widget.EditText")));
	}

	protected void tap(WebElement el) {
		el.click();
	}

	protected void hideKeyboardSafe() {
		try {
			if (driver != null) {
				driver.hideKeyboard();
			}
		} catch (Exception ignored) {
		}
	}

	/**
	 * Wait for text contains (substring match) using multiple strategies
	 */
	protected WebElement waitForTextContains(String text) {
		// Try multiple strategies: textContains, descriptionContains (content-desc), and xpath checking both text and content-desc
		try {
			return wait.until(ExpectedConditions.presenceOfElementLocated(MobileBy.AndroidUIAutomator("new UiSelector().textContains(\"" + text + "\")")));
		} catch (Exception ignored) {}
		try {
			return wait.until(ExpectedConditions.presenceOfElementLocated(MobileBy.AndroidUIAutomator("new UiSelector().descriptionContains(\"" + text + "\")")));
		} catch (Exception ignored) {}
		// Fallback: xpath that checks @text or @content-desc
		try {
			return wait.until(ExpectedConditions.presenceOfElementLocated(MobileBy.xpath("//*[contains(@text,'" + text + "') or contains(@content-desc,'" + text + "') or contains(@contentDescription,'" + text + "')]")));
		} catch (Exception e) {
			// rethrow the last exception to surface the failure
			throw e;
		}
	}

	/**
	 * Simulate keyboard input into an element with fallback strategies
	 */
	protected void typeUsingKeyboard(WebElement el, String text) {
		try {
			el.click();
			try { el.clear(); } catch (Exception ignored) {}
			// send keys and hide keyboard to commit value
			try { el.sendKeys(text); } catch (Exception e) { /* fallback below */ }
			hideKeyboardSafe();
			try { Thread.sleep(200); } catch (InterruptedException ignored) {}
			return;
		} catch (Exception ignored) {
			// fallback: try sendKeys directly
		}
		try {
			el.sendKeys(text);
			hideKeyboardSafe();
			try { Thread.sleep(200); } catch (InterruptedException ignored) {}
		} catch (Exception ignored2) {
			// last-resort: use driver.executeScript to set value for element
			try {
				if (el instanceof RemoteWebElement) {
					String id = ((RemoteWebElement) el).getId();
					Map<String, Object> args = new HashMap<>();
					args.put("id", id);
					args.put("text", text);
					driver.executeScript("mobile: setValue", args);
					hideKeyboardSafe();
					try { Thread.sleep(200); } catch (InterruptedException ignored) {}
				}
			} catch (Exception ignored3) {}
		}
	}

	protected void gotoTasksTab() {
		// App only has Tasks page, method kept for consistency with afterEach
	}

	protected void swipeLeft(org.openqa.selenium.WebElement el) {
		try {
			if (el instanceof RemoteWebElement) {
				String id = ((RemoteWebElement) el).getId();
				Map<String, Object> args = new HashMap<>();
				args.put("elementId", id);
				args.put("direction", "left");
				args.put("percent", 0.75);
				driver.executeScript("mobile: swipeGesture", args);
				return;
			}
			// Fallback: attempt to click a delete affordance inside element
			try {
				el.findElement(AppiumBy.accessibilityId("delete_task_button")).click();
				return;
			} catch (Exception ignored) {}
		} catch (Exception ignored) {}
	}

}

