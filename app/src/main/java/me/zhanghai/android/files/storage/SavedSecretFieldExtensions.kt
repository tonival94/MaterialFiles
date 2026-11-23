/*
 * Copyright (c) 2026 Material Files contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package me.zhanghai.android.files.storage

import android.text.method.PasswordTransformationMethod
import androidx.core.widget.doAfterTextChanged
import com.google.android.material.textfield.TextInputLayout

/**
 * Keeps a saved secret (e.g. a server password) from being revealed when its server is edited:
 * while the field still holds the saved secret, it stays masked and the "show password" toggle is
 * removed. The toggle comes back once the user types a different value.
 */
fun TextInputLayout.hideToggleWhileSavedSecretUnchanged(savedSecret: String?) {
    if (savedSecret.isNullOrEmpty()) {
        return
    }
    val editText = editText ?: return
    val update = {
        val isUnchanged = editText.text?.toString() == savedSecret
        if (isUnchanged) {
            if (endIconMode != TextInputLayout.END_ICON_NONE) {
                endIconMode = TextInputLayout.END_ICON_NONE
            }
            if (editText.transformationMethod !is PasswordTransformationMethod) {
                editText.transformationMethod = PasswordTransformationMethod.getInstance()
            }
        } else if (endIconMode != TextInputLayout.END_ICON_PASSWORD_TOGGLE) {
            endIconMode = TextInputLayout.END_ICON_PASSWORD_TOGGLE
        }
    }
    update()
    editText.doAfterTextChanged { update() }
}
