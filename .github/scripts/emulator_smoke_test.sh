#!/bin/bash
# Installs and starts the release and debug APKs, then prints any crash.
set -u
PACKAGE=me.zhanghai.android.files.tonival94
ACTIVITY=me.zhanghai.android.files.filelist.FileListActivity

run_test() {
    local name=$1 apk=$2 package=$3
    echo "=============== $name ==============="
    adb logcat -c
    adb install -r -g "$apk" || { echo "INSTALL FAILED"; return; }
    adb shell am start -W -n "$package/$ACTIVITY"
    sleep 15
    adb shell pidof "$package" && echo "PROCESS ALIVE" || echo "PROCESS NOT RUNNING"
    adb logcat -d -v brief AndroidRuntime:E '*:S' > "emulator-$name-crash.txt"
    adb logcat -d -v brief > "emulator-$name-full.txt"
    echo "--- AndroidRuntime errors ---"
    cat "emulator-$name-crash.txt"
    echo "--- app process errors ---"
    grep -E " E/| F/" "emulator-$name-full.txt" | grep -iE "files|fatal|exception|caused by" | head -60
}

run_test release app/build/outputs/apk/release/app-release.apk "$PACKAGE"
run_test debug app/build/outputs/apk/debug/app-debug.apk "$PACKAGE"
exit 0
