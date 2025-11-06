# Appium E2E Tests (Java + TestNG)

This module runs Android E2E tests for the Flutter app using Appium (UiAutomator2).

## One-command runs from repo root

- Build APK and run tests (TaskFlowTest only):
	make e2e

- Run tests only (assumes APK already built):
	make e2e-test

Both targets auto-set ANDROID_HOME/ANDROID_SDK_ROOT to `~/Library/Android/sdk` on macOS if these env vars are not already exported.

## Defaults (overridable)

- Appium server: http://127.0.0.1:4723
- Device UDID: emulator-5554
- APK path: ../test-app/build/app/outputs/flutter-apk/app-debug.apk

Override examples:

	cd appium-tests && ANDROID_HOME=$HOME/Library/Android/sdk mvn -q -DappiumServer=http://127.0.0.1:4725 -Dudid=emulator-5556 test

## Scope and stability

- The test runner is configured (Surefire includes) to execute only `com/example/tests/TaskFlowTest.java` by default to keep the suite reliable.
- Other experimental or flaky classes are excluded by configuration.

## Prereqs

- Java JDK 17+
- Appium server running locally
- Android emulator/device available and unlocked
