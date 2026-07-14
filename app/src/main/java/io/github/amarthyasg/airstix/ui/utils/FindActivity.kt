package io.github.amarthyasg.airstix.ui.utils

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper


fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
