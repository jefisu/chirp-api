package com.plcoding.chirp.infra.database.mappers

import com.plcoding.chirp.domain.models.ChatMessageFile
import com.plcoding.chirp.infra.database.entities.ChatMessageFileEntity

fun ChatMessageFileEntity.toChatMessageFile(): ChatMessageFile {
    return ChatMessageFile(
        id = id!!,
        messageId = message.id!!,
        url = url,
        type = type,
        createdAt = createdAt
    )
}
