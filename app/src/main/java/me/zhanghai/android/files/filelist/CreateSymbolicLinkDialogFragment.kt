/*
 * Copyright (c) 2026 Material Files contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package me.zhanghai.android.files.filelist

import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import kotlinx.parcelize.Parcelize
import me.zhanghai.android.files.R
import me.zhanghai.android.files.file.FileItem
import me.zhanghai.android.files.util.ParcelableArgs
import me.zhanghai.android.files.util.args
import me.zhanghai.android.files.util.putArgs
import me.zhanghai.android.files.util.show

/** Asks for the name of a symbolic link to [Args.file], created in the same directory. */
class CreateSymbolicLinkDialogFragment : FileNameDialogFragment() {
    private val args by args<Args>()

    override val listener: Listener
        get() = super.listener as Listener

    @StringRes
    override val titleRes: Int = R.string.file_create_symbolic_link_title

    override val initialName: String
        get() = getString(R.string.file_create_symbolic_link_name_format, args.file.name)

    override fun onOk(name: String) {
        listener.createSymbolicLink(args.file, name)
    }

    companion object {
        fun show(file: FileItem, fragment: Fragment) {
            CreateSymbolicLinkDialogFragment().putArgs(Args(file)).show(fragment)
        }
    }

    @Parcelize
    class Args(val file: FileItem) : ParcelableArgs

    interface Listener : FileNameDialogFragment.Listener {
        fun createSymbolicLink(file: FileItem, name: String)
    }
}
