#!/usr/bin/env python3
"""Prepares a release build of this fork, to be installed alongside the official app.

- Removes the non-free parts (Firebase), like F-Droid builds do.
- Uses its own application ID, so that it doesn't conflict with the official app.
- Sets the version name, and a version code that grows with every release.

Usage: prepare_fork_release.py VERSION_NAME RUN_NUMBER
"""

import pathlib
import re
import shutil
import sys

APPLICATION_ID = "me.zhanghai.android.files.tonival94"
ORIGINAL_APPLICATION_ID = "me.zhanghai.android.files"
VERSION_CODE_BASE = 1_000_000

NONFREE_FILES = [
    "app/build.gradle",
    "app/src/main/java/me/zhanghai/android/files/app/AppInitializers.kt",
    "app/src/main/java/me/zhanghai/android/files/about/AboutFragment.kt",
]


def strip_nonfree(path: pathlib.Path) -> None:
    text = path.read_text()
    stripped, count = re.subn(
        r"^[ \t]*//#ifdef NONFREE\n.*?^[ \t]*//#endif\n", "", text, flags=re.S | re.M
    )
    if count == 0:
        sys.exit(f"No NONFREE block found in {path}")
    path.write_text(stripped)


def replace_once(path: pathlib.Path, old: str, new: str) -> None:
    text = path.read_text()
    if text.count(old) != 1:
        sys.exit(f"Expected exactly one {old!r} in {path}")
    path.write_text(text.replace(old, new))


def main() -> None:
    version_name, run_number = sys.argv[1], int(sys.argv[2])
    if not re.fullmatch(r"[0-9A-Za-z][0-9A-Za-z.\-]*", version_name):
        sys.exit(f"Invalid version name {version_name!r}")

    for file in NONFREE_FILES:
        strip_nonfree(pathlib.Path(file))
    shutil.rmtree("app/src/main/java/me/zhanghai/android/files/nonfree")
    pathlib.Path("app/google-services.json").unlink()

    build_gradle = pathlib.Path("app/build.gradle")
    replace_once(
        build_gradle,
        f"applicationId '{ORIGINAL_APPLICATION_ID}'",
        f"applicationId '{APPLICATION_ID}'",
    )
    text = build_gradle.read_text()
    text, count = re.subn(
        r"versionCode \d+", f"versionCode {VERSION_CODE_BASE + run_number}", text, count=1
    )
    if count != 1:
        sys.exit("versionCode not found")
    text, count = re.subn(r"versionName '[^']*'", f"versionName '{version_name}'", text, count=1)
    if count != 1:
        sys.exit("versionName not found")
    build_gradle.write_text(text)

    # Static shortcuts can't use the application ID placeholder.
    shortcuts = pathlib.Path("app/src/main/res/xml/shortcuts.xml")
    shortcuts.write_text(
        shortcuts.read_text().replace(
            f'android:targetPackage="{ORIGINAL_APPLICATION_ID}"',
            f'android:targetPackage="{APPLICATION_ID}"',
        )
    )


if __name__ == "__main__":
    main()
