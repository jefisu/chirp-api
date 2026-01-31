package com.plcoding.chirp.api.dto.ws

import com.plcoding.chirp.domain.type.ChatId

data class RemovedFromChatDto(
    val chatId: ChatId
)
