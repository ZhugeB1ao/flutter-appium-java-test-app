#!/usr/bin/env bash
set -euo pipefail

# Simple one-command runner for Appium TestNG tests.
# Usage examples:
#   bash run-tests.sh                        # run suite from testng.xml
#   bash run-tests.sh com.example.tests.TaskFlowTest
#   bash run-tests.sh com.example.tests.TaskCrudSuite#testAddTaskWithOnlyTitle

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"

# Defaults (override via env vars if needed)
APP_PATH_DEFAULT="$REPO_ROOT/test-app/build/app/outputs/flutter-apk/app-debug.apk"
APP_PATH="${APP_PATH:-$APP_PATH_DEFAULT}"
UDID="${UDID:-emulator-5554}"
DEVICE_NAME="${DEVICE_NAME:-Android Emulator}"
APPIUM_SERVER="${APPIUM_SERVER:-http://127.0.0.1:4723}"

TEST_SELECTOR="${1:-}"

if [[ ! -f "$APP_PATH" ]]; then
  echo "[run-tests] APK not found at: $APP_PATH" >&2
  echo "[run-tests] Build it first from $REPO_ROOT/test-app (e.g., 'flutter build apk')" >&2
  exit 1
fi

cd "$SCRIPT_DIR"

MVN_ARGS=(
  -q
  -DappPath="$APP_PATH"
  -DdeviceName="$DEVICE_NAME"
  -Dudid="$UDID"
  -DappiumServer="$APPIUM_SERVER"
)

# Respect testng.xml so you can curate classes there
MVN_ARGS+=( -Dsurefire.suiteXmlFiles=testng.xml )

if [[ -n "$TEST_SELECTOR" ]]; then
  MVN_ARGS+=( -Dtest="$TEST_SELECTOR" )
fi

echo "[run-tests] Using APK:        $APP_PATH"
echo "[run-tests] Using UDID:       $UDID"
echo "[run-tests] Using DeviceName: $DEVICE_NAME"
echo "[run-tests] Appium Server:    $APPIUM_SERVER"
if [[ -n "$TEST_SELECTOR" ]]; then
  echo "[run-tests] Only test:       $TEST_SELECTOR"
fi

mvn "${MVN_ARGS[@]}" test
