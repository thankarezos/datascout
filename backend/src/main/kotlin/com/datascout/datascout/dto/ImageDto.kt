package com.datascout.datascout.dto

import jakarta.persistence.*

data class ImageDto(
    val id: Long,
    val userId: Long,
    val path: String?,
    val labels: Set<LabelDto>?
)

data class LabelDto(
    val label: String,
    val count: Int
)
