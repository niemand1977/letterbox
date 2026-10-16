#!/usr/bin/env bash
# SPDX-License-Identifier: GPL-3.0-or-later
#
# Fastlane phone screenshots from a headless emulator.
#
# Usage:
#   scripts/screenshots.sh                 # shoot into the fastlane directory
#   scripts/screenshots.sh --out DIR       # shoot elsewhere (e.g. a preview)
#   scripts/screenshots.sh --keep-running  # leave the emulator up afterwards
#
# Requirements: ANDROID_HOME (or ~/Android/Sdk) with platform-tools, a current
# emulator package, the x86_64 system image below, a JDK, and python3.
set -euo pipefail

cd "$(dirname "$0")/.."

# ---------------------------------------------------------------- CONFIGURE --
APP_ID="ch.cryptobit.letterbox"
MAIN_ACTIVITY=".AnimalsOnTop"
GRADLE_ARGS=(:app:assembleDebug)
APK="app/build/outputs/apk/debug/app-debug.apk"

AVD_NAME="${AVD_NAME:-shots}"
SYSTEM_IMAGE="system-images;android-35;default;x86_64"
OUT_DIR="fastlane/metadata/android/en-US/images/phoneScreenshots"

# A few practised letters, so the selection dialog shows progress badges.
stage_demo_data() {
  local now
  now="$(date +%s)000"
  "$ADB" shell "run-as $APP_ID mkdir -p files shared_prefs"
  "$ADB" shell "run-as $APP_ID sh -c 'cat > files/letter_progress.json'" <<DEMO
{"version":1,"languages":{"en":{"A":{"level":5,"lastSeen":$now},"B":{"level":3,"lastSeen":$now},"C":{"level":1,"lastSeen":$now},"E":{"level":4,"lastSeen":$now},"L":{"level":2,"lastSeen":$now}}}}
DEMO
  "$ADB" shell "run-as $APP_ID sh -c 'cat > shared_prefs/letterbox_prefs.xml'" <<'DEMO'
<?xml version='1.0' encoding='utf-8' standalone='yes' ?>
<map>
    <boolean name="progress_enabled" value="true" />
</map>
DEMO
}
# -------------------------------------------------------------- /CONFIGURE ---

KEEP_RUNNING=0
while [ $# -gt 0 ]; do
  case "$1" in
    --out) OUT_DIR="$2"; shift 2 ;;
    --keep-running) KEEP_RUNNING=1; shift ;;
    *) echo "unknown option: $1" >&2; exit 2 ;;
  esac
done

ANDROID_HOME="${ANDROID_HOME:-$HOME/Android/Sdk}"
export ANDROID_HOME
ADB="$ANDROID_HOME/platform-tools/adb"
EMULATOR="$ANDROID_HOME/emulator/emulator"
AVDMANAGER="$ANDROID_HOME/cmdline-tools/latest/bin/avdmanager"
[ -x "$ADB" ] || { echo "adb not found at $ADB" >&2; exit 1; }
[ -x "$EMULATOR" ] || { echo "emulator not found at $EMULATOR" >&2; exit 1; }

# Gradle needs a JDK (javac), not just a JRE. Distros often ship only a JRE, so
# if JAVA_HOME is unset or stale, look in the usual places before failing.
if [ ! -x "${JAVA_HOME:-/nonexistent}/bin/javac" ]; then
  JAVA_HOME="$(dirname "$(dirname "$(ls "$HOME"/.local/share/jdk-*/bin/javac \
    /usr/lib/jvm/*/bin/javac 2>/dev/null | head -1)")")"
  [ -x "${JAVA_HOME:-/nonexistent}/bin/javac" ] || {
    echo "No JDK found. Install one (e.g. Temurin into ~/.local/share/) or" >&2
    echo "set JAVA_HOME to a JDK — a plain JRE is not enough." >&2
    exit 1
  }
  export JAVA_HOME
fi

say() { printf '==> %s\n' "$*"; }

say "Building debug APK"
./gradlew "${GRADLE_ARGS[@]}" -q

if ! "$EMULATOR" -list-avds 2>/dev/null | grep -qx "$AVD_NAME"; then
  say "Creating AVD $AVD_NAME (pixel_6, $SYSTEM_IMAGE)"
  echo no | "$AVDMANAGER" create avd -n "$AVD_NAME" -k "$SYSTEM_IMAGE" -d pixel_6
fi

STARTED_EMULATOR=0
cleanup() {
  if [ "$STARTED_EMULATOR" = 1 ] && [ "$KEEP_RUNNING" = 0 ]; then
    "$ADB" emu kill >/dev/null 2>&1 || true
  fi
}
trap cleanup EXIT

if ! "$ADB" get-state >/dev/null 2>&1; then
  say "Booting $AVD_NAME headless"
  "$EMULATOR" -avd "$AVD_NAME" -no-window -gpu swiftshader_indirect \
    -no-audio -no-boot-anim -no-snapshot >/dev/null 2>&1 &
  STARTED_EMULATOR=1
  "$ADB" wait-for-device
  until [ "$("$ADB" shell getprop sys.boot_completed 2>/dev/null | tr -d '\r')" = "1" ]; do
    sleep 5
  done
  # sys.boot_completed fires while the device is still busy — SystemUI can ANR
  # right as the first screenshot is taken. Settle, and clear the keyguard.
  "$ADB" shell wm dismiss-keyguard >/dev/null 2>&1 || true
  sleep 8
fi
say "Device ready: Android $("$ADB" shell getprop ro.build.version.release | tr -d '\r')"

say "Installing the app"
"$ADB" install -r "$APK" >/dev/null

say "Staging demo data"
stage_demo_data

# Screen geometry, so swipes work on whatever AVD is configured.
# "wm size" prints "Physical size:" and, when one is set, an "Override size:"
# that is what actually renders — so take the last match, not the first.
read -r SCREEN_W SCREEN_H <<<"$("$ADB" shell wm size | tr -d '\r' | \
  sed -n 's/.*: \([0-9]*\)x\([0-9]*\).*/\1 \2/p' | tail -1)"
MID_X=$((SCREEN_W / 2))

say "Setting demo-mode status bar"
"$ADB" shell settings put global sysui_demo_allowed 1
demo() { "$ADB" shell am broadcast -a com.android.systemui.demo "$@" >/dev/null; }
demo -e command enter
demo -e command clock -e hhmm 1000
demo -e command battery -e level 100 -e plugged false
demo -e command network -e wifi show -e level 4
demo -e command notifications -e visible false

# Center coordinates ("x y") of the first element whose dump attribute matches
# exactly, or empty if none. Works for any attribute (text, content-desc,
# resource-id) because uiautomator always writes bounds last in the tag.
element_coords() { # element_coords <attr> <value>
  "$ADB" shell uiautomator dump /sdcard/ui.xml >/dev/null 2>&1 || return 0
  "$ADB" shell cat /sdcard/ui.xml | ATTR="$1" VALUE="$2" python3 -c "
import os, re, sys
attr, value = os.environ['ATTR'], os.environ['VALUE']
pattern = attr + '=\"' + re.escape(value) + r'\"[^>]*bounds=\"\[(\d+),(\d+)\]\[(\d+),(\d+)\]\"'
m = re.search(pattern, sys.stdin.read())
if m:
    x1, y1, x2, y2 = map(int, m.groups())
    print((x1 + x2) // 2, (y1 + y2) // 2)
"
}

# Poll (~30s) for an element instead of guessing a sleep, dismissing a
# "System UI isn't responding" dialog if one is covering the screen.
wait_for_element() { # wait_for_element <attr> <value>
  local coords i
  for i in $(seq 1 15); do
    coords="$(element_coords "$1" "$2")"
    [ -n "$coords" ] && { printf '%s' "$coords"; return 0; }
    if [ -n "$(element_coords text "Wait")" ]; then
      "$ADB" shell input tap $(element_coords text "Wait") >/dev/null 2>&1 || true
    fi
    sleep 2
  done
  echo "timed out waiting for $1=$2" >&2
  return 1
}

# shellcheck disable=SC2046 — coords are an unquoted "x y" pair on purpose
tap_element() { "$ADB" shell input tap $(wait_for_element "$1" "$2"); }

# Swipe up until an element scrolls into view, then echo its center ("x y").
scroll_to_element() { # scroll_to_element <attr> <value>
  local coords i
  for i in $(seq 1 8); do
    coords="$(element_coords "$1" "$2")"
    [ -n "$coords" ] && { printf '%s' "$coords"; return 0; }
    "$ADB" shell input swipe "$MID_X" $((SCREEN_H * 70 / 100)) \
      "$MID_X" $((SCREEN_H * 30 / 100)) 300 >/dev/null 2>&1
    sleep 1
  done
  echo "could not scroll to $1=$2" >&2
  return 1
}

# Cold-launch into the game; it is drawn once the letter buttons exist.
launch() {
  "$ADB" shell am force-stop "$APP_ID"
  "$ADB" shell am start -n "$APP_ID/$MAIN_ACTIVITY" >/dev/null
  wait_for_element resource-id "$APP_ID:id/mainLetter1" >/dev/null
  sleep 2
}

shot() { "$ADB" exec-out screencap -p > "$1"; say "captured $1"; }

mkdir -p "$OUT_DIR"
say "Capturing screenshots"

# ------------------------------------------------------------------ CAPTURE --
"$ADB" shell cmd locale set-app-locales "$APP_ID" --locales en-US

# 1. The game: an animal and three letters.
launch
shot "$OUT_DIR/1.png"

# 2. The menu with language, letters and progress.
tap_element content-desc "Menu"
wait_for_element text "Letters" >/dev/null
sleep 1
shot "$OUT_DIR/2.png"

# 3. Letter selection with progress badges.
tap_element text "Letters"
wait_for_element resource-id "$APP_ID:id/btnTop10" >/dev/null
sleep 1
shot "$OUT_DIR/3.png"

# 4. The game in a different script.
"$ADB" shell cmd locale set-app-locales "$APP_ID" --locales ja
launch
shot "$OUT_DIR/4.png"
"$ADB" shell cmd locale set-app-locales "$APP_ID" --locales en-US
# ----------------------------------------------------------------- /CAPTURE --

demo -e command exit
say "Done: $(ls "$OUT_DIR" | tr '\n' ' ')"
