.PHONY: e2e e2e-test apk

# Build APK (debug)
apk:
	cd test-app && flutter build apk --debug

# Build APK and run all Appium tests (single command from repo root)
e2e: apk
	cd appium-tests && ANDROID_HOME=$${ANDROID_HOME:-$$HOME/Library/Android/sdk} ANDROID_SDK_ROOT=$${ANDROID_SDK_ROOT:-$$HOME/Library/Android/sdk} mvn test

# Run tests only (assumes APK already built)
e2e-test:
	cd appium-tests && ANDROID_HOME=$${ANDROID_HOME:-$$HOME/Library/Android/sdk} ANDROID_SDK_ROOT=$${ANDROID_SDK_ROOT:-$$HOME/Library/Android/sdk} mvn test
