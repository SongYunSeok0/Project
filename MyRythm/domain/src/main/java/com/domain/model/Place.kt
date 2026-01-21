package com.domain.model

data class Place(
    val title: String,
    val address: String,
    val location: Location,
    val category: String? = null,
    val link: String? = null,
    val roadAddress: String? = null,
    val telephone: String? = null
)