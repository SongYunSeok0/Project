package com.shared.mapper

import android.content.Context
import com.shared.R
import com.shared.model.UiError

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