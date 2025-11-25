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
		// Default wait: configurable via -DwaitSeconds (seconds). Default 10s.
		long waitSeconds = 10;
		try {
			String ws = System.getProperty("waitSeconds", "10");
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
		
		// Only attempt to cancel if a dialog appears to still be open (EditText present).
		boolean dialogOpen = false;
		try {
			dialogOpen = driver.findElements(MobileBy.className("android.widget.EditText")).size() > 0;
		} catch (Exception ignored) {}
		if (dialogOpen) {
			try {
				WebElement cancel = null;
				try { cancel = driver.findElement(AppiumBy.accessibilityId("cancel_task_button")); } catch (Exception ignored) {}
				if (cancel == null) {
					try { cancel = driver.findElement(AppiumBy.accessibilityId("Huỷ")); } catch (Exception ignored) {}
				}
				if (cancel == null) {
					try { cancel = driver.findElement(MobileBy.AndroidUIAutomator("new UiSelector().text(\"Huỷ\")")); } catch (Exception ignored) {}
				}
				if (cancel != null) {
					try { cancel.click(); } catch (Exception ignored) {}
				}
			} catch (Exception ignored) {}
			try { Thread.sleep(200); } catch (InterruptedException ignored) {}
		}
		// Ensure we're on Tasks tab
		try { gotoTasksTab(); } catch (Exception ignored) {}

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

	// Helper: wait and find by accessibility id
	protected WebElement waitForAccessibilityId(String id) {
		return wait.until(ExpectedConditions.presenceOfElementLocated(MobileBy.AccessibilityId(id)));
	}

	// Helper: wait and find by id (resource-id)


	// Helper: wait and find the first EditText on screen (useful when accessibility ids are missing)
	protected WebElement waitForFirstEditText() {
		return wait.until(ExpectedConditions.presenceOfElementLocated(MobileBy.className("android.widget.EditText")));
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



	// Helper: wait for text contains (substring match)
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
	 * Try to simulate keyboard input into an element. Prefer using the driver's keyboard if available
	 * otherwise fall back to element.sendKeys(). This focuses the element first.
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


    
	// Helper: perform a long press on an element (Android)
	protected void longPress(org.openqa.selenium.WebElement el) {
		try {
			if (el instanceof RemoteWebElement) {
				String id = ((RemoteWebElement) el).getId();
				Map<String, Object> args = new HashMap<>();
				args.put("elementId", id);
				args.put("duration", 1000); // ms
				driver.executeScript("mobile: longClickGesture", args);
				return;
			}
			// Fallback: try click as a degrade
			el.click();
		} catch (Exception ignored) {
			try { el.click(); } catch (Exception ignored2) {}
		}
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

	/**
	 * If the device has navigated away from the app (launcher or other package shown),
	 * activate the app package and wait for the main UI add button. This helps tests
	 * that accidentally send a back navigation which can leave the app backgrounded.
	 */
	protected void ensureAppForegrounded() {
		try {
			if (driver == null) return;
			String current = null;
			try {
				current = driver.getCurrentPackage();
			} catch (Exception ignored) {}
			if (current == null || !current.equals("com.example.test_app")) {
				try {
					driver.activateApp("com.example.test_app");
				} catch (Exception ignored) {
					try { driver.launchApp(); } catch (Exception ignored2) {}
				}
				try { Thread.sleep(300); } catch (InterruptedException ignored) {}
				try { waitForAccessibilityId("add_task_button"); } catch (Exception ignored) {}
			}
		} catch (Exception ignored) {}
	}

}

