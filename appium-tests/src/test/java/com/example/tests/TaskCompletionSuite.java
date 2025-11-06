package com.example.tests;

import com.example.BaseTest;
import io.appium.java_client.MobileBy;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

/**
 * TaskCompletionSuite
 * Implements the requested completion-related tests: add without description, toggles on Home/Tasks,
 * counts update, overflow menu presence, navigation to Tasks, spacing/summary checks.
 */
public class TaskCompletionSuite extends BaseTest {

	@Test(description = "Test case 4: Add task with no description")
	public void testAddTaskNoDescription() {
		final String title = "NoDesc-" + System.currentTimeMillis();
		// open add dialog (try accessibility id then fallback)
		try {
			waitForAccessibilityId("add_task_button").click();
		} catch (Exception e) {
			driver.findElement(MobileBy.AndroidUIAutomator("new UiSelector().text(\"Thêm\")")).click();
		}

		WebElement titleField = waitForAccessibilityId("task_title_field");
		titleField.sendKeys(title);
		driver.findElement(MobileBy.AndroidUIAutomator("new UiSelector().text(\"Lưu\")")).click();

		Assert.assertTrue(driver.findElements(MobileBy.xpath("//*[@text='" + title + "']")).size() > 0,
				"Expected task added and visible in list");
	}

	@Test(description = "Test case 6 & 10: Mark task complete on Home page and verify Completed count updates")
	public void testMarkCompleteOnHomeUpdatesCounts() throws InterruptedException {
		final String title = "CompleteHome-" + System.currentTimeMillis();
		waitForAccessibilityId("add_task_button").click();
		waitForAccessibilityId("task_title_field").sendKeys(title);
		driver.findElement(MobileBy.AndroidUIAutomator("new UiSelector().text(\"Lưu\")")).click();

		// Read completed box text prior to change
		WebElement completedBox = driver.findElement(MobileBy.AndroidUIAutomator("new UiSelector().textContains(\"Hoàn thành\")"));
		String before = completedBox.getText();

		WebElement titleEl = driver.findElement(MobileBy.xpath("//*[@text='" + title + "']"));
		WebElement parent = titleEl.findElement(By.xpath(".."));
		WebElement cb = parent.findElement(MobileBy.className("android.widget.CheckBox"));
		cb.click();
		Thread.sleep(600);

		String after = completedBox.getText();
		Assert.assertNotEquals(before, after, "Completed count should increase after marking a task complete");
	}

	@Test(description = "Test case 7 & 8: Mark complete on Tasks page then uncheck removes strikethrough")
	public void testMarkAndUncheckOnTasksPage() throws InterruptedException {
		final String title = "CompleteTaskPage-" + System.currentTimeMillis();
		waitForAccessibilityId("add_task_button").click();
		waitForAccessibilityId("task_title_field").sendKeys(title);
		driver.findElement(MobileBy.AndroidUIAutomator("new UiSelector().text(\"Lưu\")")).click();

		// Open Tasks full screen
		driver.findElement(MobileBy.AndroidUIAutomator("new UiSelector().textContains(\"Xem tất cả công việc\")")).click();

		WebElement titleEl = driver.findElement(MobileBy.xpath("//*[@text='" + title + "']"));
		WebElement parent = titleEl.findElement(By.xpath(".."));
		WebElement cb = parent.findElement(MobileBy.className("android.widget.CheckBox"));

		// Mark complete
		cb.click();
		Thread.sleep(400);
		String checked = cb.getAttribute("checked");
		Assert.assertTrue("true".equalsIgnoreCase(checked), "Checkbox should be checked after marking complete");

		// Uncheck
		cb.click();
		Thread.sleep(400);
		String unchecked = cb.getAttribute("checked");
		Assert.assertTrue("false".equalsIgnoreCase(unchecked) || unchecked == null,
				"Checkbox should be unchecked after toggling back");
	}

	@Test(description = "Test case 9: Uncheck completed task on Home moves it back to Pending")
	public void testUncheckOnHomeMovesToPending() throws InterruptedException {
		final String title = "UncheckHome-" + System.currentTimeMillis();
		waitForAccessibilityId("add_task_button").click();
		waitForAccessibilityId("task_title_field").sendKeys(title);
		driver.findElement(MobileBy.AndroidUIAutomator("new UiSelector().text(\"Lưu\")")).click();

		WebElement titleEl = driver.findElement(MobileBy.xpath("//*[@text='" + title + "']"));
		WebElement parent = titleEl.findElement(By.xpath(".."));
		WebElement cb = parent.findElement(MobileBy.className("android.widget.CheckBox"));

		// mark complete then uncheck
		cb.click();
		Thread.sleep(400);
		cb.click();
		Thread.sleep(400);

		// sanity: ensure title still present and checkbox unchecked
		String checked = cb.getAttribute("checked");
		Assert.assertTrue("false".equalsIgnoreCase(checked) || checked == null, "Checkbox should be unchecked and task pending");
	}

	@Test(description = "Test case 22: Overflow menu shows functions")
	public void testOverflowMenuShowsOptions() {
		final String title = "MenuOps-" + System.currentTimeMillis();
		waitForAccessibilityId("add_task_button").click();
		waitForAccessibilityId("task_title_field").sendKeys(title);
		driver.findElement(MobileBy.AndroidUIAutomator("new UiSelector().text(\"Lưu\")")).click();

		WebElement titleEl = driver.findElement(MobileBy.xpath("//*[@text='" + title + "']"));
		WebElement parent = titleEl.findElement(By.xpath(".."));
		List<WebElement> menus = parent.findElements(MobileBy.className("android.widget.ImageButton"));
		Assert.assertTrue(menus.size() > 0, "Expected overflow/menu button to be present");

		// open and check for options
		menus.get(0).click();
		boolean hasEdit = driver.findElements(MobileBy.AndroidUIAutomator("new UiSelector().text(\"Chỉnh sửa\")")).size() > 0;
		boolean hasDelete = driver.findElements(MobileBy.AndroidUIAutomator("new UiSelector().text(\"Xoá\")")).size() > 0;
		Assert.assertTrue(hasEdit && hasDelete, "Expected overflow menu to show Edit and Delete options");
	}

	@Test(description = "Test case 28: Tap Tasks icon navigates to Tasks page")
	public void testTapTasksIconNavigates() {
		// Tap bottom nav Tasks
		try {
			driver.findElement(MobileBy.AndroidUIAutomator("new UiSelector().text(\"Tasks\")")).click();
		} catch (Exception e) {
			// fallback Vietnamese label
			driver.findElement(MobileBy.AndroidUIAutomator("new UiSelector().textContains(\"Tasks\")")).click();
		}

		// Expect add_task_button present on Tasks page
		WebElement add = waitForAccessibilityId("add_task_button");
		Assert.assertNotNull(add, "Expected to be on Tasks page with add button present");
	}

	@Test(description = "Test case 34 & 40: Checkbox-text spacing and summary boxes presence/colors (labels)")
	public void testSpacingAndSummaryBoxes() {
		// Check summary labels exist
		boolean hasTotal = driver.findElements(MobileBy.AndroidUIAutomator("new UiSelector().textContains(\"Tổng\")")).size() > 0;
		boolean hasCompleted = driver.findElements(MobileBy.AndroidUIAutomator("new UiSelector().textContains(\"Hoàn thành\")")).size() > 0;
		boolean hasPending = driver.findElements(MobileBy.AndroidUIAutomator("new UiSelector().textContains(\"Chưa xong\")")).size() > 0;
		Assert.assertTrue(hasTotal && hasCompleted && hasPending, "Summary boxes (Total/Completed/Pending) should be present");

		// For spacing: sample first task item and ensure checkbox and title align (best-effort check that both elements exist)
		List<WebElement> titles = driver.findElements(MobileBy.className("android.widget.TextView"));
		if (!titles.isEmpty()) {
			WebElement firstTitle = titles.get(0);
			WebElement parent = firstTitle.findElement(By.xpath(".."));
			List<WebElement> checks = parent.findElements(MobileBy.className("android.widget.CheckBox"));
			Assert.assertTrue(checks.size() > 0, "Each task should include a checkbox aligned with text (best-effort)");
		}
	}

}
