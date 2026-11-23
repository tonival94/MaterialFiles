/*
 * Copyright (c) 2026 Material Files contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package me.zhanghai.android.files.trash

import android.os.Environment
import java8.nio.file.FileAlreadyExistsException
import java8.nio.file.LinkOption
import java8.nio.file.Path
import java8.nio.file.Paths
import java8.nio.file.StandardCopyOption
import me.zhanghai.android.files.provider.common.createDirectory
import me.zhanghai.android.files.provider.common.delete
import me.zhanghai.android.files.provider.common.deleteIfExists
import me.zhanghai.android.files.provider.common.exists
import me.zhanghai.android.files.provider.common.isDirectory
import me.zhanghai.android.files.provider.common.moveTo
import me.zhanghai.android.files.provider.common.newDirectoryStream
import me.zhanghai.android.files.provider.common.newOutputStream
import me.zhanghai.android.files.provider.common.readAllBytes
import me.zhanghai.android.files.provider.linux.isLinuxPath
import me.zhanghai.android.files.settings.Settings
import me.zhanghai.android.files.util.valueCompat
import java.io.IOException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

/**
 * A trash for files on the primary shared storage.
 *
 * Trashed files are moved (not copied) into a hidden directory on the same storage, laid out like
 * the freedesktop.org trash: `files/` holds the trashed files and `info/` holds a `.trashinfo` file
 * for each of them, recording its original path and when it was deleted.
 */
object Trash {
    private const val DIRECTORY_NAME = ".Trash-MaterialFiles"
    private const val FILES_DIRECTORY_NAME = "files"
    private const val INFO_DIRECTORY_NAME = "info"
    private const val INFO_EXTENSION = ".trashinfo"
    private const val NO_MEDIA_FILE_NAME = ".nomedia"

    private val DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    @Suppress("DEPRECATION")
    private val storageRoot: Path
        get() = Paths.get(Environment.getExternalStorageDirectory().absolutePath)

    val rootDirectory: Path
        get() = storageRoot.resolve(DIRECTORY_NAME)

    val filesDirectory: Path
        get() = rootDirectory.resolve(FILES_DIRECTORY_NAME)

    private val infoDirectory: Path
        get() = rootDirectory.resolve(INFO_DIRECTORY_NAME)

    val isEnabled: Boolean
        get() = Settings.TRASH_ENABLED.valueCompat

    /**
     * Creates the trash if needed, so that it can be opened from the navigation drawer, and deletes
     * expired items. Does I/O, so it must not be called on the main thread.
     */
    fun initialize() {
        if (!isEnabled) {
            return
        }
        // This runs at startup in the background, so it must never crash the app.
        try {
            ensureDirectories()
            purgeExpired()
        } catch (e: Exception) {
            // E.g. storage permission not granted yet.
            e.printStackTrace()
        }
    }

    /**
     * Creates the trash directories if needed, e.g. before opening the trash for the first time.
     * Does I/O, so it must not be called on the main thread.
     */
    fun ensureCreated() {
        try {
            ensureDirectories()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /** Whether [path] can be moved to the trash, instead of being deleted right away. */
    fun canMoveToTrash(path: Path): Boolean {
        if (!path.isLinuxPath) {
            return false
        }
        val storageRoot = storageRoot
        return path.startsWith(storageRoot) && path != storageRoot &&
            !path.startsWith(rootDirectory)
    }

    /** Whether [path] is a trashed item, i.e. a direct child of the trash files directory. */
    fun isTrashedItem(path: Path): Boolean = path.parent == filesDirectory

    fun isInTrash(path: Path): Boolean = path.startsWith(rootDirectory)

    @Throws(IOException::class)
    private fun ensureDirectories() {
        filesDirectory.createDirectoriesOneByOne()
        infoDirectory.createDirectoriesOneByOne()
        val noMediaFile = rootDirectory.resolve(NO_MEDIA_FILE_NAME)
        if (!noMediaFile.exists()) {
            noMediaFile.newOutputStream().close()
        }
    }

    /**
     * Prepares moving [path] to the trash: records its original location and returns where it
     * should be moved. If the move then fails, call [abandon] with the returned path.
     */
    @Throws(IOException::class)
    fun prepareMoveToTrash(path: Path): Path {
        ensureDirectories()
        val name = path.fileName.toString()
        var index = 1
        var trashedPath: Path
        var infoPath: Path
        while (true) {
            val trashedName = if (index == 1) name else name.withSuffix(" ($index)")
            trashedPath = filesDirectory.resolve(trashedName)
            infoPath = infoDirectory.resolve(trashedName + INFO_EXTENSION)
            if (!trashedPath.exists() && !infoPath.exists()) {
                break
            }
            ++index
        }
        val info = buildString {
            append("[Trash Info]\n")
            append("Path=").append(path.toString()).append('\n')
            append("DeletionDate=").append(LocalDateTime.now().format(DATE_FORMATTER)).append('\n')
        }
        infoPath.newOutputStream().use { it.write(info.toByteArray()) }
        return trashedPath
    }

    /** Moves [path] to the trash right away, without any user interaction. */
    @Throws(IOException::class)
    fun moveToTrash(path: Path) {
        val trashedPath = prepareMoveToTrash(path)
        try {
            path.moveTo(trashedPath, LinkOption.NOFOLLOW_LINKS, StandardCopyOption.ATOMIC_MOVE)
        } catch (e: IOException) {
            abandon(trashedPath)
            throw e
        }
    }

    val defaultRestoreDirectory: Path
        get() = storageRoot

    /** Removes the record of a trashed item, e.g. after a failed move or a restore. */
    fun abandon(trashedPath: Path) {
        try {
            getInfoPath(trashedPath).deleteIfExists()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    /** Returns the original path of a trashed item, or `null` if it is unknown. */
    fun getOriginalPath(trashedPath: Path): Path? =
        readInfo(trashedPath)?.let { Paths.get(it.originalPath) }

    @Throws(IOException::class)
    fun deletePermanently(trashedPath: Path) {
        // The item itself is deleted by the delete job, so only the record is left to remove.
        getInfoPath(trashedPath).deleteIfExists()
    }

    /** Lists the items in the trash. */
    @Throws(IOException::class)
    fun listItems(): List<Path> {
        if (!filesDirectory.isDirectory()) {
            return emptyList()
        }
        return filesDirectory.newDirectoryStream().use { it.toList() }
    }

    /**
     * Deletes items that have been in the trash for longer than the retention period. A retention
     * period of 0 days keeps items forever.
     */
    fun purgeExpired() {
        val retentionDays = Settings.TRASH_RETENTION_DAYS.valueCompat
        if (retentionDays <= 0) {
            return
        }
        val items = try {
            listItems()
        } catch (e: IOException) {
            e.printStackTrace()
            return
        }
        val expiryTime = LocalDateTime.now().minusDays(retentionDays.toLong())
        for (item in items) {
            val deletionDate = readInfo(item)?.deletionDate ?: continue
            if (!deletionDate.isBefore(expiryTime)) {
                continue
            }
            try {
                deleteRecursively(item)
                getInfoPath(item).deleteIfExists()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    @Throws(IOException::class)
    private fun deleteRecursively(path: Path) {
        if (path.isDirectory()) {
            path.newDirectoryStream().use { children ->
                for (child in children) {
                    deleteRecursively(child)
                }
            }
            path.delete(true)
        } else {
            path.delete(false)
        }
    }

    /**
     * Like [createDirectories], which can't be used here: it iterates over the path, and our path
     * implementations don't support that.
     */
    @Throws(IOException::class)
    fun Path.createDirectoriesOneByOne() {
        if (isDirectory()) {
            return
        }
        parent?.createDirectoriesOneByOne()
        try {
            createDirectory()
        } catch (e: FileAlreadyExistsException) {
            if (!isDirectory()) {
                throw e
            }
        }
    }

    private fun getInfoPath(trashedPath: Path): Path =
        infoDirectory.resolve(trashedPath.fileName.toString() + INFO_EXTENSION)

    private fun readInfo(trashedPath: Path): TrashInfo? {
        val text = try {
            getInfoPath(trashedPath).readAllBytes().toString(Charsets.UTF_8)
        } catch (e: IOException) {
            return null
        }
        var originalPath: String? = null
        var deletionDate: LocalDateTime? = null
        for (line in text.lineSequence()) {
            when {
                line.startsWith("Path=") -> originalPath = line.removePrefix("Path=")
                line.startsWith("DeletionDate=") -> deletionDate = try {
                    LocalDateTime.parse(line.removePrefix("DeletionDate="), DATE_FORMATTER)
                } catch (e: DateTimeParseException) {
                    null
                }
            }
        }
        // Items without a valid date are never purged automatically, only when emptying the trash.
        return originalPath?.let { TrashInfo(it, deletionDate) }
    }

    private fun String.withSuffix(suffix: String): String {
        val dotIndex = lastIndexOf('.')
        return if (dotIndex > 0) {
            substring(0, dotIndex) + suffix + substring(dotIndex)
        } else {
            this + suffix
        }
    }

    private class TrashInfo(val originalPath: String, val deletionDate: LocalDateTime?)
}
