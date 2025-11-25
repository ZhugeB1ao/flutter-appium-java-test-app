#!/usr/bin/env zsh
set -euo pipefail

# Repo-level runner that forwards to appium-tests/run-e2e.sh
# Usage (from repo root):
#   ./run-e2e.sh                 # runs tests with defaults
#   ./run-e2e.sh --dry-run       # print the underlying command and exit
# Environment variables forwarded: APP_PATH, UDID, APPIUM_SERVER, APP_PACKAGE, JAVA_HOME

SCRIPT_DIR=$(cd "$(dirname "$0")" && pwd)
RUNNER="$SCRIPT_DIR/appium-tests/run-e2e.sh"

if [ ! -f "$RUNNER" ]; then
  echo "ERROR: runner not found at $RUNNER"
  exit 2
fi

DRY=false
FAST=false
# parse args (support --dry-run and --fast in any order)
for a in "$@"; do
  case "$a" in
    --dry-run) DRY=true ;;
    --fast) FAST=true ;;
  esac
done

# Default JAVA_HOME fallback if user hasn't set one
JAVA_HOME_DEFAULT="/Library/Java/JavaVirtualMachines/jdk-24.jdk/Contents/Home"
export JAVA_HOME=${JAVA_HOME:-$JAVA_HOME_DEFAULT}

APP_PATH=${APP_PATH:-}
UDID=${UDID:-}
APPIUM_SERVER=${APPIUM_SERVER:-http://127.0.0.1:4723}
APP_PACKAGE=${APP_PACKAGE:-com.example.test_app}
WAIT_SECONDS=${WAIT_SECONDS:-10}

if [ "$FAST" = true ]; then
  echo "Fast mode enabled: passing FAST=true to test runner"
  CMD=(env FAST=true WAIT_SECONDS="$WAIT_SECONDS" APPIUM_SERVER="$APPIUM_SERVER" APP_PACKAGE="$APP_PACKAGE" "$RUNNER")
else
  CMD=(env WAIT_SECONDS="$WAIT_SECONDS" APPIUM_SERVER="$APPIUM_SERVER" APP_PACKAGE="$APP_PACKAGE" "$RUNNER")
fi
if [ -n "$APP_PATH" ]; then
  CMD=(env APP_PATH="$APP_PATH" "${CMD[@]}")
fi
if [ -n "$UDID" ]; then
  CMD=(env UDID="$UDID" "${CMD[@]}")
fi

echo "JAVA_HOME=$JAVA_HOME"
echo "Runner: $RUNNER"
echo "App path: ${APP_PATH:-<none>}  UDID: ${UDID:-<none>}  APPIUM_SERVER: $APPIUM_SERVER  APP_PACKAGE: $APP_PACKAGE"

if [ "$DRY" = true ]; then
  echo "DRY RUN: ${CMD[*]}"
  exit 0
fi

cd "$SCRIPT_DIR/appium-tests"
exec ${CMD[@]}
