package com.plcoding.chirp.api.dto.ws

import com.plcoding.chirp.domain.type.ChatId
import com.plcoding.chirp.domain.type.ChatMessageId

data class SendMessageDto(
    val chatId: ChatId,
    val content: String?,
    val attachedFiles: List<AttachmentFileDto> = emptyList(),
    val messageId: ChatMessageId? = null
)
