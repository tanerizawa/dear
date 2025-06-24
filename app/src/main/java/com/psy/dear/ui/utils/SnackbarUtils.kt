package com.psy.dear.ui.utils

import android.content.Context
import androidx.compose.material3.SnackbarHostState
import com.psy.dear.core.UiText
import com.psy.dear.core.asString

suspend fun SnackbarHostState.showSnackbar(context: Context, message: UiText) {
    showSnackbar(message.asString(context))
}
