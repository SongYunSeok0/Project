package com.data.exception

import java.io.IOException

class HttpAuthException(val code: Int, message: String?) :
    IOException("HTTP $code: ${message ?: "unknown"}")