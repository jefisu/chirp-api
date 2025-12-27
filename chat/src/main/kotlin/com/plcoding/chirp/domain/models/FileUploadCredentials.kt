package com.plcoding.chirp.domain.models

import java.time.Instant

data class FileUploadCredentials(
    val uploadUrl: String,
    val publicUrl: String,
    val headers: Map<String, String>,
    val expiresAt: Instant
)
