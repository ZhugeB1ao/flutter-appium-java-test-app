package com.example.util;

import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

public class LoggingListener implements ITestListener {
    @Override
    public void onTestStart(ITestResult result) {
        System.out.println("[START] " + result.getMethod().getMethodName());
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        System.out.println("[PASS]  " + result.getMethod().getMethodName());
    }

    @Override
    public void onTestFailure(ITestResult result) {
        System.out.println("[FAIL]  " + result.getMethod().getMethodName() + " - " + result.getThrowable());
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        System.out.println("[SKIP]  " + result.getMethod().getMethodName());
    }

    @Override
    public void onStart(ITestContext context) {
        System.out.println("[SUITE-START] " + context.getSuite().getName());
    }

    @Override
    public void onFinish(ITestContext context) {
        int passed = context.getPassedTests().size();
        int failed = context.getFailedTests().size();
        int skipped = context.getSkippedTests().size();
        System.out.println(String.format("[SUITE-END] %s -> passed=%d, failed=%d, skipped=%d",
                context.getSuite().getName(), passed, failed, skipped));
    }
}
