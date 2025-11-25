package com.example.util;

import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;
import com.example.BaseTest;

/**
 * TestNG listener to capture debug snapshots on test failure.
 */
public class TestFailureListener implements ITestListener {

    @Override
    public void onTestFailure(ITestResult result) {
        try {
            Object instance = result.getInstance();
            if (instance instanceof BaseTest) {
                BaseTest bt = (BaseTest) instance;
                String name = (result.getMethod() != null) ? result.getMethod().getMethodName() : "unknown";
                bt.captureDebugSnapshot("listener_failure_" + name);
            }
        } catch (Exception ignored) {}
    }

    // no-op implementations for other callbacks
    @Override public void onTestStart(ITestResult result) {}
    @Override public void onTestSuccess(ITestResult result) {}
    @Override public void onTestSkipped(ITestResult result) {}
    @Override public void onTestFailedButWithinSuccessPercentage(ITestResult result) {}
    @Override public void onStart(ITestContext context) {}
    @Override public void onFinish(ITestContext context) {}
}
