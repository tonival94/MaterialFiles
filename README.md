# Material Files (tonival94 fork)

This is a fork of [Material Files](https://github.com/zhanghai/MaterialFiles) by Hai Zhang, **maintained by [tonival94](https://github.com/tonival94)**. It is not affiliated with or endorsed by the original author. Report problems and requests in [this repository's issues](https://github.com/tonival94/MaterialFiles/issues), not to the original project.

The fork collects bug fixes and features that were proposed for the original app but not merged, plus its own fixes and features. Credit for the integrated pull requests goes to their authors; each one keeps its original author in the history.

## Download

Signed APKs are published in [Releases](https://github.com/tonival94/MaterialFiles/releases), and can be installed and updated with [Obtainium](https://github.com/ImranR98/Obtainium) using `https://github.com/tonival94/MaterialFiles`.

- **Application ID** `me.zhanghai.android.files.tonival94`: as the original README asks, the fork uses its own package name, so it installs next to the official app instead of conflicting with it.
- **No Google services**: release builds don't include Firebase (analytics and crash reporting), like the F-Droid build of the original.

## Changes from the original

### Integrated pull requests from the original repository

- Fix data loss when "Save as" overwrites the source file itself: [#1676](https://github.com/zhanghai/MaterialFiles/pull/1676) (fixes [#1664](https://github.com/zhanghai/MaterialFiles/issues/1664))
- SSH host key verification, instead of accepting any server: [#1579](https://github.com/zhanghai/MaterialFiles/pull/1579) (fixes [#544](https://github.com/zhanghai/MaterialFiles/issues/544))
- Clearing the search text refreshes the file list: [#1517](https://github.com/zhanghai/MaterialFiles/pull/1517)
- WebDAV: delete and move directories correctly: [#1535](https://github.com/zhanghai/MaterialFiles/pull/1535)
- Respect dismissal of the notification permission reminder: [#1640](https://github.com/zhanghai/MaterialFiles/pull/1640) (fixes [#1643](https://github.com/zhanghai/MaterialFiles/issues/1643), [#1650](https://github.com/zhanghai/MaterialFiles/issues/1650))
- Remove the advertising ID permission: [#985](https://github.com/zhanghai/MaterialFiles/pull/985)
- Readable message when creating a file inside an archive: [#1515](https://github.com/zhanghai/MaterialFiles/pull/1515)
- Denser file list layout: [#1264](https://github.com/zhanghai/MaterialFiles/pull/1264) ([#236](https://github.com/zhanghai/MaterialFiles/issues/236), [#1602](https://github.com/zhanghai/MaterialFiles/issues/1602), [#1084](https://github.com/zhanghai/MaterialFiles/issues/1084))
- Better file names and configurable grid columns: [#1525](https://github.com/zhanghai/MaterialFiles/pull/1525) ([#1494](https://github.com/zhanghai/MaterialFiles/issues/1494), [#1028](https://github.com/zhanghai/MaterialFiles/issues/1028), [#1363](https://github.com/zhanghai/MaterialFiles/issues/1363), [#1524](https://github.com/zhanghai/MaterialFiles/issues/1524), [#1545](https://github.com/zhanghai/MaterialFiles/issues/1545))
- "Save as" for multiple shared files: [#1652](https://github.com/zhanghai/MaterialFiles/pull/1652) (fixes [#1646](https://github.com/zhanghai/MaterialFiles/issues/1646))
- Markdown and code preview with syntax highlighting: [#1587](https://github.com/zhanghai/MaterialFiles/pull/1587), with the preview isolated from the network and local files
- Faster SFTP uploads: [#1603](https://github.com/zhanghai/MaterialFiles/pull/1603) (performance part; [#1350](https://github.com/zhanghai/MaterialFiles/issues/1350), [#1528](https://github.com/zhanghai/MaterialFiles/issues/1528), [#895](https://github.com/zhanghai/MaterialFiles/issues/895))
- Recent files in the navigation drawer, can be turned off: [#1631](https://github.com/zhanghai/MaterialFiles/pull/1631) ([#146](https://github.com/zhanghai/MaterialFiles/issues/146), [#1637](https://github.com/zhanghai/MaterialFiles/issues/1637), [#1433](https://github.com/zhanghai/MaterialFiles/issues/1433), [#1556](https://github.com/zhanghai/MaterialFiles/issues/1556), [#1607](https://github.com/zhanghai/MaterialFiles/issues/1607), [#1117](https://github.com/zhanghai/MaterialFiles/issues/1117), [#1443](https://github.com/zhanghai/MaterialFiles/issues/1443))
- "Open in terminal" with Termux: [#1671](https://github.com/zhanghai/MaterialFiles/pull/1671) ([#901](https://github.com/zhanghai/MaterialFiles/issues/901), [#1620](https://github.com/zhanghai/MaterialFiles/issues/1620), [#905](https://github.com/zhanghai/MaterialFiles/issues/905), [#1465](https://github.com/zhanghai/MaterialFiles/issues/1465), [#1318](https://github.com/zhanghai/MaterialFiles/issues/1318), [#888](https://github.com/zhanghai/MaterialFiles/issues/888))
- SMB3 session encryption: [#1665](https://github.com/zhanghai/MaterialFiles/pull/1665) (fixes [#486](https://github.com/zhanghai/MaterialFiles/issues/486))

### Fixes and features of this fork

- **Encrypted server credentials**: SMB, FTP, SFTP and WebDAV credentials are encrypted with a key in the Android Keystore, and saved passwords can no longer be revealed when editing a server ([#1302](https://github.com/zhanghai/MaterialFiles/issues/1302), [#1366](https://github.com/zhanghai/MaterialFiles/issues/1366), [#971](https://github.com/zhanghai/MaterialFiles/issues/971))
- **Faster writes to SMB servers**, sent in 1 MiB chunks instead of 16 KiB ([#1486](https://github.com/zhanghai/MaterialFiles/issues/1486), [#1588](https://github.com/zhanghai/MaterialFiles/issues/1588), [#796](https://github.com/zhanghai/MaterialFiles/issues/796))
- **Trash** for internal storage, with restore and automatic cleanup after a configurable number of days ([#276](https://github.com/zhanghai/MaterialFiles/issues/276), [#1430](https://github.com/zhanghai/MaterialFiles/issues/1430), [#931](https://github.com/zhanghai/MaterialFiles/issues/931), [#897](https://github.com/zhanghai/MaterialFiles/issues/897))
- **Batch rename** of multiple selected files, with patterns, numbering and find and replace ([#278](https://github.com/zhanghai/MaterialFiles/issues/278), [#893](https://github.com/zhanghai/MaterialFiles/issues/893), [#1570](https://github.com/zhanghai/MaterialFiles/issues/1570), [#1432](https://github.com/zhanghai/MaterialFiles/issues/1432))
- **Long press and drag selection** of ranges, in list and grid, replacing [#1091](https://github.com/zhanghai/MaterialFiles/pull/1091)
- **Copy paths** of multiple selected files ([#1619](https://github.com/zhanghai/MaterialFiles/issues/1619))
- **Number of items** shown under local folders, can be turned off ([#331](https://github.com/zhanghai/MaterialFiles/issues/331), [#1024](https://github.com/zhanghai/MaterialFiles/issues/1024), [#1136](https://github.com/zhanghai/MaterialFiles/issues/1136), [#1549](https://github.com/zhanghai/MaterialFiles/issues/1549), [#1666](https://github.com/zhanghai/MaterialFiles/issues/1666))
- **Create symbolic links** ([#702](https://github.com/zhanghai/MaterialFiles/issues/702))
- **Export and import network servers**, without passwords ([#523](https://github.com/zhanghai/MaterialFiles/issues/523), [#1327](https://github.com/zhanghai/MaterialFiles/issues/1327), [#1559](https://github.com/zhanghai/MaterialFiles/issues/1559))
- Renaming a file when only the letter case changes ([#1208](https://github.com/zhanghai/MaterialFiles/issues/1208))
- Search results inside hidden folders stay hidden ([#1128](https://github.com/zhanghai/MaterialFiles/issues/1128))
- Readable error messages, and going back when the current folder no longer exists ([#1613](https://github.com/zhanghai/MaterialFiles/issues/1613), [#1530](https://github.com/zhanghai/MaterialFiles/issues/1530))
- Deleting images from the image viewer on SMB and WebDAV ([#1472](https://github.com/zhanghai/MaterialFiles/issues/1472), [#1538](https://github.com/zhanghai/MaterialFiles/issues/1538))

The rest of this page is the original README. Its download badges point to the original app, not to this fork.

---

# Material Files

[本文中文版](README_zh-CN.md)

[![Android CI status](https://github.com/zhanghai/MaterialFiles/workflows/Android%20CI/badge.svg)](https://github.com/zhanghai/MaterialFiles/actions) [![GitHub release](https://img.shields.io/github/v/release/zhanghai/MaterialFiles)](https://github.com/zhanghai/MaterialFiles/releases) [![License](https://img.shields.io/github/license/zhanghai/MaterialFiles?color=blue)](LICENSE)

An open source Material Design file manager, for Android 5.0+.

[<img alt="Get it on Google Play" src="https://play.google.com/intl/en_us/badges/static/images/badges/en_badge_web_generic.png" width="240">](https://play.google.com/store/apps/details?id=me.zhanghai.android.files) [<img alt="Get it on F-Droid" src="https://fdroid.gitlab.io/artwork/badge/get-it-on.png" width="240">](https://f-droid.org/packages/me.zhanghai.android.files) [<img alt="Get it on GitHub" src="https://raw.githubusercontent.com/Kunzisoft/Github-badge/main/get-it-on-github.png" width="240">](https://github.com/zhanghai/MaterialFiles/releases/latest/download/app-release-universal.apk)

[Help translation on Transifex](https://www.transifex.com/zhanghai/MaterialFiles/) ([Search Android & GNOME translations](https://translations.zhanghai.me/), [Microsoft language resources](https://learn.microsoft.com/en-us/globalization/reference/microsoft-language-resources), [MIME type translations](https://gitlab.freedesktop.org/xdg/shared-mime-info/-/tree/master/po))

## Preview

<p><img src="fastlane/metadata/android/en-US/images/phoneScreenshots/1.png" width="32%" /> <img src="fastlane/metadata/android/en-US/images/phoneScreenshots/2.png" width="32%" /> <img src="fastlane/metadata/android/en-US/images/phoneScreenshots/3.png" width="32%" />
<img src="fastlane/metadata/android/en-US/images/phoneScreenshots/4.png" width="32%" /> <img src="fastlane/metadata/android/en-US/images/phoneScreenshots/5.png" width="32%" /> <img src="fastlane/metadata/android/en-US/images/phoneScreenshots/6.png" width="32%" /></p>

## Features

- Open source: Lightweight, clean and secure.
- Material Design: Follows Material Design guidelines, with attention into details.
- Breadcrumbs: Navigate in the filesystem with ease.
- Root support: View and manage files with root access.
- Archive support: View, extract and create common compressed files.
- NAS support: View and manage files on FTP, SFTP, SMB and WebDAV servers.
- Themes: Customizable UI colors, plus night mode with optional true black.
- Linux-aware: Like [Nautilus](https://apps.gnome.org/Nautilus/), knows symbolic links, file permissions and SELinux context.
- Robust: Uses Linux system calls under the hood, not yet another [`ls` parser](https://news.ycombinator.com/item?id=7994720).
- Well-implemented: Built upon the right things, including [Java NIO2 File API](https://docs.oracle.com/javase/8/docs/api/java/nio/file/package-summary.html) and [LiveData](https://developer.android.com/topic/libraries/architecture/livedata).

## Why Material Files?

Because I like Material Design, and clean Material Design.

There are already a handful of powerful file managers, but most of them just aren't Material Design. And even among the ones with Material Design, they usually have various minor design flaws (layout, alignment, padding, icon, font, etc) across the app which makes me uncomfortable, while still being minor enough so that not everybody would care to fix it. So I had to create my own.

Because I want an open source file manager.

Most of the popular and reliable file managers are just closed source, and I sometimes use them to view and modify files that require root access. But deep down inside, I just feel uneasy with giving any closed source app the root access to my device. After all, that means giving literally full access to my device, which stays with me every day and stores my own information, and what apps do with such access merely depends on their good intent.

Because I want a file manager that is implemented the right way.

- This app implemented [Java NIO2 File API](https://docs.oracle.com/javase/8/docs/api/java/nio/file/package-summary.html) as its backend, instead of inventing a custom model for file information/operations, which often gets coupled with UI logic and grows into a mixture of everything ([example](https://github.com/TeamAmaze/AmazeFileManager/blob/master/app/src/main/java/com/amaze/filemanager/filesystem/HybridFile.java)). On the contrary, a decoupled backend allows cleaner code (which means less bugs), and easier addition of support for other file systems.

- This app doesn't use `java.io.File` or parse the output of `ls`, but built bindings to Linux syscalls to properly access the file system. `java.io.File` is an old API missing many features, and just can't handle things like symbolic links correctly, which is the reason why many people rather parse `ls` instead. However parsing the output `ls` is not only slow, but also [unreliable](https://news.ycombinator.com/item?id=7994720), which made [Cabinet](https://github.com/aminb/cabinet/blob/master/app/src/main/java/com/afollestad/cabinet/file/root/LsParser.java) broken on newer Android versions. By virtue of using Linux syscalls, this app is able to be fast and smooth, and handle advanced things like Linux permissions, symbolic links and even SELinux context. It can also handle file names with invalid UTF-8 encoding because paths are not naively stored as Java `String`s, which most file managers does and fails during file operation.

- This app built its frontend upon modern `ViewModel` and `LiveData` which enables a clear code structure and support for rotation. It also properly handles things like errors during file operation, file conflicts and foreground/background state.

In a word, this app tries to follow the best practices on Android and do the right thing, while keeping its source code clean and maintainable.

Because I know people can do it right.

[Nautilus](https://wiki.gnome.org/Apps/Files) is a beautifully-designed and user-friendly file manager on Linux desktop, and it's fully Linux-aware. [Phonograph](https://github.com/kabouzeid/Phonograph) is an open source Material Design music player app (which I've been using for years), and it has just the right Material Design and implementation.

So, it's time for yet another Android file manager.

## Inclusion in custom ROMs

Thank you if you choose to include Material Files in your custom ROM! However since I've received several user complaints due to improper inclusion, I'd like to offer some suggestions on including this app properly for the good of end users:

- Please don't replace the AOSP [DocumentsUI](https://android.googlesource.com/platform/packages/apps/DocumentsUI/) app with this app. This app is not designed to replace DocumentsUI and can't handle a number of functionalities in DocumentsUI - in fact, it relies on DocumentsUI to do things like granting external SD card access.

- Please make sure this app can be uninstalled or at least disabled. Some users may not want this app for a variety of reasons, and get very upset when they can't remove it.

- Please avoid conflict with the Play/F-Droid version of this app. App stores cannot update apps signed with a different certificate, so you can either ship an APK that's signed by me (or F-Droid) so that users will be able to update it on Play/F-Droid, or fork this project and rename the package name when you need to sign the APK with a different certificate and potentially making other changes.

## License

    Copyright (C) 2018 Hai Zhang

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.
