/*
 * Copyright (c) 2026 Material Files contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package me.zhanghai.android.files.filelist

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.parcelize.Parcelize
import me.zhanghai.android.files.R
import me.zhanghai.android.files.databinding.BatchRenameDialogBinding
import me.zhanghai.android.files.file.FileItem
import me.zhanghai.android.files.util.ParcelableArgs
import me.zhanghai.android.files.util.args
import me.zhanghai.android.files.util.asFileNameOrNull
import me.zhanghai.android.files.util.layoutInflater
import me.zhanghai.android.files.util.putArgs
import me.zhanghai.android.files.util.show

/**
 * Renames several files at once from a pattern, where `{name}` is the original name and `{n}` a
 * progressive number, after an optional find and replace on the original name.
 */
class BatchRenameDialogFragment : AppCompatDialogFragment() {
    private val args by args<Args>()

    private lateinit var binding: BatchRenameDialogBinding

    private val listener: Listener
        get() = requireParentFragment() as Listener

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = BatchRenameDialogBinding.inflate(requireContext().layoutInflater)
        if (savedInstanceState == null) {
            binding.patternEdit.setText(NAME_PLACEHOLDER)
            binding.startNumberEdit.setText("1")
        }
        binding.patternEdit.doAfterTextChanged { updatePreview() }
        binding.startNumberEdit.doAfterTextChanged { updatePreview() }
        binding.findEdit.doAfterTextChanged { updatePreview() }
        binding.replaceEdit.doAfterTextChanged { updatePreview() }
        binding.keepExtensionCheck.setOnCheckedChangeListener { _, _ -> updatePreview() }
        updatePreview()
        return MaterialAlertDialogBuilder(requireContext(), theme)
            .setTitle(
                getString(R.string.batch_rename_title_format, args.files.size)
            )
            .setView(binding.root)
            .setPositiveButton(android.R.string.ok, null)
            .setNegativeButton(android.R.string.cancel, null)
            .create()
            .apply {
                // Override the listener here so that we have control over when to close the dialog.
                setOnShowListener {
                    getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener { onOk() }
                }
            }
    }

    private fun computeNewNames(): List<String> {
        val pattern = binding.patternEdit.text.toString()
        val startNumber = binding.startNumberEdit.text.toString().toIntOrNull() ?: 1
        val find = binding.findEdit.text.toString()
        val replace = binding.replaceEdit.text.toString()
        val keepExtension = binding.keepExtensionCheck.isChecked
        val files = args.files
        val lastNumber = startNumber + files.size - 1
        val numberWidth = maxOf(startNumber.toString().length, lastNumber.toString().length)
        return files.mapIndexed { index, file ->
            val fileName = file.name
            val baseName = if (keepExtension) file.baseName else fileName
            val extension = if (keepExtension) fileName.substring(baseName.length) else ""
            val replacedBaseName = if (find.isNotEmpty()) {
                baseName.replace(find, replace)
            } else {
                baseName
            }
            val number = (startNumber + index).toString().padStart(numberWidth, '0')
            val newBaseName = pattern
                .replace(NAME_PLACEHOLDER, replacedBaseName)
                .replace(NUMBER_PLACEHOLDER, number)
                .trim()
            newBaseName + extension
        }
    }

    private fun updatePreview() {
        val files = args.files
        val newNames = computeNewNames()
        val preview = files.zip(newNames).take(PREVIEW_COUNT).joinToString("\n") { (file, name) ->
            "${file.name} → $name"
        }
        binding.previewText.text = if (files.size > PREVIEW_COUNT) "$preview\n…" else preview
        binding.patternLayout.error = null
    }

    private fun onOk() {
        val files = args.files
        val newNames = computeNewNames()
        val error = validate(files, newNames)
        if (error != null) {
            binding.patternLayout.error = getString(error)
            return
        }
        listener.batchRenameFiles(files, newNames)
        dismiss()
    }

    private fun validate(files: List<FileItem>, newNames: List<String>): Int? {
        if (newNames.any { it.asFileNameOrNull() == null || it == "." || it == ".." }) {
            return R.string.file_name_error_invalid
        }
        if (newNames.toSet().size != newNames.size) {
            return R.string.batch_rename_error_duplicate
        }
        // A new name may only be taken by one of the files being renamed.
        val oldNames = files.map { it.name }.toSet()
        if (newNames.any { it !in oldNames && listener.hasFileWithName(it) }) {
            return R.string.file_name_error_already_exists
        }
        return null
    }

    companion object {
        private const val NAME_PLACEHOLDER = "{name}"
        private const val NUMBER_PLACEHOLDER = "{n}"
        private const val PREVIEW_COUNT = 3

        fun show(files: List<FileItem>, fragment: Fragment) {
            BatchRenameDialogFragment().putArgs(Args(files)).show(fragment)
        }
    }

    @Parcelize
    class Args(val files: List<FileItem>) : ParcelableArgs

    interface Listener {
        fun hasFileWithName(name: String): Boolean

        fun batchRenameFiles(files: List<FileItem>, newNames: List<String>)
    }
}
