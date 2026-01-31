package com.plcoding.chirp.api.dto.ws

import com.plcoding.chirp.domain.type.ChatId
import com.plcoding.chirp.domain.type.UserId

data class ChatDeletedDto(
    val chatId: ChatId,
    val deletedByUserId: UserId
)
