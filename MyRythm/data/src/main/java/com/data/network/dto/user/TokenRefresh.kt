package com.data.network.dto.user

data class RefreshRequest(val refresh: String)
data class RefreshResponse(val access: String, val refresh: String?)