package com.plcoding.chirp.api.dto.ws

import com.plcoding.chirp.domain.type.ChatId
import com.plcoding.chirp.domain.type.UserId

data class OutcomingTypingEventDto(
    val chatId: ChatId,
    val userId: UserId,
    val userName: String,
    val isTyping: Boolean
)

data class IncomingTypingEventDto(
    val chatId: ChatId,
    val isTyping: Boolean
)