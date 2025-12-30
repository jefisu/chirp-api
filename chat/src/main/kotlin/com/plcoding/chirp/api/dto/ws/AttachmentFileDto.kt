package com.plcoding.chirp.api.dto.ws

import com.plcoding.chirp.domain.models.ChatMessageFileType
import java.util.UUID

data class AttachmentFileDto(
    val url: String,
    val type: ChatMessageFileType,
    val id: UUID? = null,
)
