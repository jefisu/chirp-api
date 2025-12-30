package com.plcoding.chirp.api.dto

import com.plcoding.chirp.domain.models.ChatMessageFileType
import java.time.Instant
import java.util.UUID

data class ChatMessageFileDto(
    val id: UUID,
    val url: String,
    val type: ChatMessageFileType,
    val createdAt: Instant
)
