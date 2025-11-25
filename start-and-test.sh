#!/bin/bash
set -e

echo "========================================="
echo "  Automated E2E Test Runner"
echo "========================================="
echo ""

# Set JAVA_HOME if not already set
if [ -z "$JAVA_HOME" ]; then
  if [ -d "/Library/Java/JavaVirtualMachines/jdk-24.jdk/Contents/Home" ]; then
    export JAVA_HOME="/Library/Java/JavaVirtualMachines/jdk-24.jdk/Contents/Home"
    echo "✓ Set JAVA_HOME to JDK 24"
  elif [ -d "/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home" ]; then
    export JAVA_HOME="/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home"
    echo "✓ Set JAVA_HOME to JDK 17"
  else
    echo "⚠ Warning: No JDK found. Maven compilation may fail."
  fi
else
  # Check if existing JAVA_HOME is a JRE, if so try to find a JDK
  if [[ "$JAVA_HOME" == *"JavaAppletPlugin"* ]] || [[ "$JAVA_HOME" == *"jre"* ]]; then
    echo "⚠ Detected JRE in JAVA_HOME, switching to JDK..."
    if [ -d "/Library/Java/JavaVirtualMachines/jdk-24.jdk/Contents/Home" ]; then
      export JAVA_HOME="/Library/Java/JavaVirtualMachines/jdk-24.jdk/Contents/Home"
      echo "✓ Set JAVA_HOME to JDK 24"
    elif [ -d "/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home" ]; then
      export JAVA_HOME="/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home"
      echo "✓ Set JAVA_HOME to JDK 17"
    fi
  else
    echo "✓ Using existing JAVA_HOME: $JAVA_HOME"
  fi
fi

# Set Android SDK environment
if [ -z "$ANDROID_HOME" ]; then
  export ANDROID_HOME="$HOME/Library/Android/sdk"
  export ANDROID_SDK_ROOT="$ANDROID_HOME"
  echo "✓ Set ANDROID_HOME to $ANDROID_HOME"
else
  echo "✓ Using existing ANDROID_HOME: $ANDROID_HOME"
fi

# Check if emulator is running
echo ""
echo "Checking emulator status..."
DEVICE_COUNT=$("$ANDROID_HOME/platform-tools/adb" devices | grep -w "device" | wc -l | tr -d ' ')

if [ "$DEVICE_COUNT" -eq "0" ]; then
  echo "⚠ No emulator running. Starting emulator..."
  
  # Get first available AVD
  AVD_NAME=$("$ANDROID_HOME/emulator/emulator" -list-avds | head -n 1)
  
  if [ -z "$AVD_NAME" ]; then
    echo "❌ Error: No AVD found. Please create an Android Virtual Device first."
    exit 1
  fi
  
  echo "  Starting AVD: $AVD_NAME"
  "$ANDROID_HOME/emulator/emulator" -avd "$AVD_NAME" -no-snapshot-load > /tmp/emulator.log 2>&1 &
  EMULATOR_PID=$!
  echo "  Emulator PID: $EMULATOR_PID"
  
  # Wait for emulator to boot
  echo "  Waiting for emulator to boot..."
  "$ANDROID_HOME/platform-tools/adb" wait-for-device
  
  # Wait for boot to complete
  BOOT_COMPLETE=""
  while [ "$BOOT_COMPLETE" != "1" ]; do
    BOOT_COMPLETE=$("$ANDROID_HOME/platform-tools/adb" shell getprop sys.boot_completed 2>/dev/null | tr -d '\r')
    sleep 2
  done
  
  echo "✓ Emulator is ready"
  STARTED_EMULATOR=true
else
  echo "✓ Emulator already running"
  STARTED_EMULATOR=false
fi

# Check if Appium is running
echo ""
echo "Checking Appium server..."
if curl -s http://127.0.0.1:4723/status > /dev/null 2>&1; then
  echo "✓ Appium server already running"
  STARTED_APPIUM=false
else
  echo "⚠ Appium not running. Starting Appium..."
  npx appium --log-level info > /tmp/appium.log 2>&1 &
  APPIUM_PID=$!
  echo "  Appium PID: $APPIUM_PID"
  
  # Wait for Appium to be ready
  echo "  Waiting for Appium to be ready..."
  for i in {1..30}; do
    if curl -s http://127.0.0.1:4723/status > /dev/null 2>&1; then
      echo "✓ Appium server is ready"
      STARTED_APPIUM=true
      break
    fi
    sleep 1
  done
  
  if [ "$STARTED_APPIUM" != "true" ]; then
    echo "❌ Error: Appium failed to start. Check /tmp/appium.log"
    exit 1
  fi
fi

# Run tests
echo ""
echo "========================================="
echo "  Running E2E Tests"
echo "========================================="
echo ""

./run-e2e.sh
TEST_EXIT_CODE=$?

# Cleanup
echo ""
echo "========================================="
echo "  Cleanup"
echo "========================================="

if [ "$STARTED_APPIUM" = "true" ] && [ ! -z "$APPIUM_PID" ]; then
  echo "Stopping Appium (PID: $APPIUM_PID)..."
  kill $APPIUM_PID 2>/dev/null || true
fi

if [ "$STARTED_EMULATOR" = "true" ]; then
  echo "Note: Emulator left running for faster subsequent runs."
  echo "To stop manually: $ANDROID_HOME/platform-tools/adb emu kill"
fi

echo ""
if [ $TEST_EXIT_CODE -eq 0 ]; then
  echo "✅ Tests completed successfully!"
else
  echo "❌ Tests failed with exit code $TEST_EXIT_CODE"
  echo "Check logs in appium-tests/target/surefire-reports/"
fi

exit $TEST_EXIT_CODE
