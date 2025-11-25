# Appium E2E Tests (Java + TestNG)

This module runs Android E2E tests for the Flutter app using Appium (UiAutomator2).

## Quick one-line runs (from repo root)

- Run all suites (uses defaults):

```bash
./run-e2e.sh
```

- Dry-run (print command, no tests):

```bash
./run-e2e.sh --dry-run
```

- Fast mode (assumes app already installed / warmed device):

```bash
./run-e2e.sh --fast
```

## Environment variables

- `JAVA_HOME` — path to a JDK (required by Maven). Example: `/Library/Java/JavaVirtualMachines/jdk-24.jdk/Contents/Home`
- `APP_PATH` — path to the APK to install/run (optional)
- `UDID` — device/emulator id (optional)
- `APPIUM_SERVER` — Appium URL (default `http://127.0.0.1:4723`)
- `APP_PACKAGE` — app package used by tests (default `com.example.test_app`)
- `WAIT_SECONDS` — number of seconds for explicit waits (default `10`)
- `FAST` — if `true`, run in fast mode (skip extra install steps)

## Run a single test (fast iteration)

```bash
cd appium-tests
mvn -Dtest=com.example.tests.AddTaskInlineSuite#testAddTask1 -Dsurefire.useFile=false test
```

## Where artifacts land

- Failure snapshots (page source + screenshots): `appium-tests/target/debug-snapshots/`

## Troubleshooting

- If Maven complains "No compiler is provided", set `JAVA_HOME` to a JDK:

```bash
export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-24.jdk/Contents/Home
```

- Start Appium in a separate terminal if it's not running:

```bash
npx appium --log-level info
```

- Ensure emulator/device is visible:

```bash
/Users/macbook/Library/Android/sdk/platform-tools/adb devices
```

## CI notes

- Export `JAVA_HOME`, `UDID`, and `APPIUM_SERVER` in your CI environment and run `./run-e2e.sh` or `make e2e`.
