package com.plcoding.chirp.api.mappers

import com.plcoding.chirp.api.dto.ChatMessageFileDto
import com.plcoding.chirp.domain.models.ChatMessageFile

fun ChatMessageFile.toChatMessageFileDto(): ChatMessageFileDto {
    return ChatMessageFileDto(
        id = id,
        url = url,
        type = type,
        createdAt = createdAt
    )
}
