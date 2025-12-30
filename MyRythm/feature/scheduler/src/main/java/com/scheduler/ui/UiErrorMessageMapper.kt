package com.scheduler.ui

import android.content.Context
import com.shared.R

fun UiError.toMessage(context: Context): String {
    return when (this) {
        UiError.NeedLogin ->
            context.getString(R.string.error_need_login)

        UiError.NetworkFailed ->
            context.getString(R.string.error_network_failed)

        UiError.ServerFailed ->
            context.getString(R.string.error_server_failed)

        is UiError.Message ->
            this.text
    }
}