SHELL := /bin/zsh

# Run full E2E suites via the appium-tests runner
e2e:
	@echo "Running E2E suites (appium-tests/run-e2e.sh)"
	@cd appium-tests && ./run-e2e.sh

# Dry-run target useful for CI validation
e2e-dry:
	@echo "Dry-run: would run appium-tests/run-e2e.sh"
	@echo "cd appium-tests && ./run-e2e.sh"

.PHONY: e2e e2e-dry
.PHONY: e2e e2e-test e2e-suite apk jdk-info

# Build APK (debug)
apk:
	cd test-app && flutter build apk --debug

# Build APK and run all Appium tests (single command from repo root)
e2e: apk
	cd appium-tests && JAVA_HOME=$$(/usr/libexec/java_home -v 17 2>/dev/null || /usr/libexec/java_home -v 11 2>/dev/null || /usr/libexec/java_home) ANDROID_HOME=$${ANDROID_HOME:-$$HOME/Library/Android/sdk} ANDROID_SDK_ROOT=$${ANDROID_SDK_ROOT:-$$HOME/Library/Android/sdk} mvn test

# Run tests only (assumes APK already built)
e2e-test:
	cd appium-tests && JAVA_HOME=$$(/usr/libexec/java_home -v 17 2>/dev/null || /usr/libexec/java_home -v 11 2>/dev/null || /usr/libexec/java_home) ANDROID_HOME=$${ANDROID_HOME:-$$HOME/Library/Android/sdk} ANDROID_SDK_ROOT=$${ANDROID_SDK_ROOT:-$$HOME/Library/Android/sdk} mvn test

# Run tests defined in TestNG suite file (testng.xml)
e2e-suite:
	cd appium-tests && JAVA_HOME=$$(/usr/libexec/java_home -v 17 2>/dev/null || /usr/libexec/java_home -v 11 2>/dev/null || /usr/libexec/java_home) ANDROID_HOME=$${ANDROID_HOME:-$$HOME/Library/Android/sdk} ANDROID_SDK_ROOT=$${ANDROID_SDK_ROOT:-$$HOME/Library/Android/sdk} mvn test -Dsurefire.suiteXmlFiles=testng.xml

# Inspect Java environment to ensure a JDK is configured
jdk-info:
	@echo "JAVA_HOME=$$JAVA_HOME"
	@which java || true
	@java -version || true
	@which javac || true
	@javac -version || true
	@/usr/libexec/java_home -V || true
