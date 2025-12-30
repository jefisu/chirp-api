package com.plcoding.chirp.domain.models

import com.plcoding.chirp.domain.type.ChatMessageId
import java.time.Instant
import java.util.UUID

data class ChatMessageFile(
    val id: UUID,
    val messageId: ChatMessageId,
    val url: String,
    val type: ChatMessageFileType,
    val createdAt: Instant
)

enum class ChatMessageFileType {
    IMAGE
}