/*
 * Copyright (c) 2026 Material Files contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package me.zhanghai.android.files.storage

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import me.zhanghai.android.files.provider.ftp.client.Authority as FtpAuthority
import me.zhanghai.android.files.provider.ftp.client.Mode as FtpMode
import me.zhanghai.android.files.provider.ftp.client.Protocol as FtpProtocol
import me.zhanghai.android.files.provider.sftp.client.Authority as SftpAuthority
import me.zhanghai.android.files.provider.sftp.client.PasswordAuthentication as SftpPasswordAuthentication
import me.zhanghai.android.files.provider.smb.client.Authority as SmbAuthority
import me.zhanghai.android.files.provider.webdav.client.AccessTokenAuthentication as WebDavAccessTokenAuthentication
import me.zhanghai.android.files.provider.webdav.client.Authority as WebDavAuthority
import me.zhanghai.android.files.provider.webdav.client.NoneAuthentication as WebDavNoneAuthentication
import me.zhanghai.android.files.provider.webdav.client.PasswordAuthentication as WebDavPasswordAuthentication
import me.zhanghai.android.files.provider.webdav.client.Protocol as WebDavProtocol

/**
 * Exports and imports saved network servers (SMB, FTP, SFTP, WebDAV) as JSON.
 *
 * Secrets (passwords, private keys, access tokens) are never exported: imported servers have empty
 * credentials, to be entered again by editing them.
 */
object ServerSettingsBackup {
    private const val FORMAT = "material-files-servers"
    private const val VERSION = 1

    private const val TYPE_SMB = "smb"
    private const val TYPE_FTP = "ftp"
    private const val TYPE_SFTP = "sftp"
    private const val TYPE_WEBDAV = "webdav"

    private const val AUTHENTICATION_PASSWORD = "password"
    private const val AUTHENTICATION_PUBLIC_KEY = "public_key"
    private const val AUTHENTICATION_ACCESS_TOKEN = "access_token"
    private const val AUTHENTICATION_NONE = "none"

    fun export(storages: List<Storage>): String {
        val servers = JSONArray()
        for (storage in storages) {
            val server = when (storage) {
                is SmbServer -> JSONObject()
                    .put("type", TYPE_SMB)
                    .put("host", storage.authority.host)
                    .put("port", storage.authority.port)
                    .put("username", storage.authority.username)
                    .putOpt("domain", storage.authority.domain)
                is FtpServer -> JSONObject()
                    .put("type", TYPE_FTP)
                    .put("protocol", storage.authority.protocol.name)
                    .put("host", storage.authority.host)
                    .put("port", storage.authority.port)
                    .put("username", storage.authority.username)
                    .put("mode", storage.authority.mode.name)
                    .put("encoding", storage.authority.encoding)
                    .put("anonymous", storage.authority.username == FtpAuthority.ANONYMOUS_USERNAME)
                is SftpServer -> JSONObject()
                    .put("type", TYPE_SFTP)
                    .put("host", storage.authority.host)
                    .put("port", storage.authority.port)
                    .put("username", storage.authority.username)
                    .put(
                        "authentication",
                        if (storage.authentication is SftpPasswordAuthentication) {
                            AUTHENTICATION_PASSWORD
                        } else {
                            AUTHENTICATION_PUBLIC_KEY
                        }
                    )
                is WebDavServer -> JSONObject()
                    .put("type", TYPE_WEBDAV)
                    .put("protocol", storage.authority.protocol.name)
                    .put("host", storage.authority.host)
                    .put("port", storage.authority.port)
                    .put("username", storage.authority.username)
                    .put(
                        "authentication",
                        when (storage.authentication) {
                            is WebDavPasswordAuthentication -> AUTHENTICATION_PASSWORD
                            is WebDavAccessTokenAuthentication -> AUTHENTICATION_ACCESS_TOKEN
                            else -> AUTHENTICATION_NONE
                        }
                    )
                else -> null
            } ?: continue
            server
                .putOpt("name", storage.customName)
                .put("path", storage.relativePathOrEmpty)
            servers.put(server)
        }
        return JSONObject()
            .put("format", FORMAT)
            .put("version", VERSION)
            .put("servers", servers)
            .toString(2)
    }

    /** Parses servers from [json]. Throws [JSONException] if it isn't an export of this app. */
    @Throws(JSONException::class)
    fun import(json: String): List<Storage> {
        val root = JSONObject(json)
        if (root.optString("format") != FORMAT) {
            throw JSONException("Not a server export")
        }
        val servers = root.getJSONArray("servers")
        val storages = mutableListOf<Storage>()
        for (index in 0..<servers.length()) {
            val server = servers.getJSONObject(index)
            val name = server.optStringOrNull("name")
            val path = server.optString("path")
            val host = server.getString("host")
            val username = server.optString("username")
            val storage = when (server.getString("type")) {
                TYPE_SMB -> SmbServer(
                    null, name,
                    SmbAuthority(
                        host, server.optInt("port", SmbAuthority.DEFAULT_PORT), username,
                        server.optStringOrNull("domain")
                    ),
                    "", path
                )
                TYPE_FTP -> {
                    val protocol = enumValueOrDefault(
                        server.optString("protocol"), FtpProtocol.FTP
                    )
                    val isAnonymous = server.optBoolean("anonymous")
                    FtpServer(
                        null, name,
                        FtpAuthority(
                            protocol, host, server.optInt("port", protocol.defaultPort),
                            username,
                            enumValueOrDefault(server.optString("mode"), FtpAuthority.DEFAULT_MODE),
                            server.optStringOrNull("encoding") ?: FtpAuthority.DEFAULT_ENCODING
                        ),
                        if (isAnonymous) FtpAuthority.ANONYMOUS_PASSWORD else "",
                        path
                    )
                }
                TYPE_SFTP -> SftpServer(
                    null, name,
                    SftpAuthority(host, server.optInt("port", SftpAuthority.DEFAULT_PORT), username),
                    // The secret has to be entered again, as a password or a private key.
                    SftpPasswordAuthentication(""),
                    path
                )
                TYPE_WEBDAV -> {
                    val protocol = enumValueOrDefault(
                        server.optString("protocol"), WebDavProtocol.DAVS
                    )
                    val authentication = when (server.optString("authentication")) {
                        AUTHENTICATION_PASSWORD -> WebDavPasswordAuthentication("")
                        AUTHENTICATION_ACCESS_TOKEN -> WebDavAccessTokenAuthentication("")
                        else -> WebDavNoneAuthentication
                    }
                    WebDavServer(
                        null, name,
                        WebDavAuthority(
                            protocol, host, server.optInt("port", protocol.defaultPort), username
                        ),
                        authentication, path
                    )
                }
                else -> null
            } ?: continue
            storages += storage
        }
        return storages
    }

    /**
     * Adds [imported] servers to [storages], skipping the ones that are already there (same type,
     * address, user and path).
     */
    fun merge(storages: List<Storage>, imported: List<Storage>): List<Storage> {
        val existingKeys = storages.mapNotNull { it.serverKey }.toSet()
        return storages + imported.filter { it.serverKey !in existingKeys }
    }

    private val Storage.serverKey: String?
        get() = when (this) {
            is SmbServer -> "$TYPE_SMB|$authority|$relativePath"
            is FtpServer -> "$TYPE_FTP|$authority|$relativePath"
            is SftpServer -> "$TYPE_SFTP|$authority|$relativePath"
            is WebDavServer -> "$TYPE_WEBDAV|$authority|$relativePath"
            else -> null
        }

    private val Storage.relativePathOrEmpty: String
        get() = when (this) {
            is SmbServer -> relativePath
            is FtpServer -> relativePath
            is SftpServer -> relativePath
            is WebDavServer -> relativePath
            else -> ""
        }

    private fun JSONObject.optStringOrNull(name: String): String? =
        if (has(name) && !isNull(name)) optString(name).ifEmpty { null } else null

    private inline fun <reified E : Enum<E>> enumValueOrDefault(name: String, default: E): E =
        enumValues<E>().firstOrNull { it.name == name } ?: default
}
