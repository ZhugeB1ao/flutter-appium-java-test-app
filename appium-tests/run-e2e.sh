#!/usr/bin/env zsh
set -euo pipefail

# Simple runner to execute the focused Appium TestNG suites with one command.
# Usage:
#   ./run-e2e.sh                 # uses defaults (JAVA_HOME fallback, local appium)
#   APP_PATH=/path/to/app.apk UDID=emulator-5554 APPIUM_SERVER=http://127.0.0.1:4723 ./run-e2e.sh

# Default JAVA_HOME if not already set (adjust if your JDK path differs)
JAVA_HOME_DEFAULT="/Library/Java/JavaVirtualMachines/jdk-24.jdk/Contents/Home"
export JAVA_HOME=${JAVA_HOME:-$JAVA_HOME_DEFAULT}

APP_PATH=${APP_PATH:-}
UDID=${UDID:-}
APPIUM_SERVER=${APPIUM_SERVER:-http://127.0.0.1:4723}
APP_PACKAGE=${APP_PACKAGE:-com.example.test_app}
WAIT_SECONDS=${WAIT_SECONDS:-10}
FAST=${FAST:-false}

echo "Using JAVA_HOME=$JAVA_HOME"
echo "APP_PATH=${APP_PATH:-<none>}"
echo "UDID=${UDID:-<none>}"
echo "APPIUM_SERVER=$APPIUM_SERVER"
echo "APP_PACKAGE=$APP_PACKAGE"

# Ensure debug snapshot dir exists so failures are captured consistently
mkdir -p target/debug-snapshots

MAVEN_CMD=(mvn -Dsurefire.useFile=false -Dsurefire.suiteXmlFiles=testng.xml)

# Pass optional properties to Maven if present
if [ -n "$APP_PATH" ]; then
  MAVEN_CMD+=("-DappPath=$APP_PATH")
fi
if [ -n "$UDID" ]; then
  MAVEN_CMD+=("-Dudid=$UDID")
fi
MAVEN_CMD+=("-DappiumServer=$APPIUM_SERVER" "-DappPackage=$APP_PACKAGE" "-DwaitSeconds=$WAIT_SECONDS")

if [ "$FAST" = "true" ]; then
  echo "FAST mode: skipping extra install steps and using shorter waits (waitSeconds=$WAIT_SECONDS)"
fi

echo "Running: ${MAVEN_CMD[*]} test"

"${MAVEN_CMD[@]}" test

EXIT_CODE=$?
echo "Test run finished with exit code $EXIT_CODE"
exit $EXIT_CODE
