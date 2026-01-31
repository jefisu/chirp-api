package com.plcoding.chirp.api.dto

import com.plcoding.chirp.domain.models.ChatEventType
import com.plcoding.chirp.domain.type.ChatEventId
import com.plcoding.chirp.domain.type.ChatId
import com.plcoding.chirp.domain.type.UserId
import java.time.Instant

data class ChatEventDto(
    val id: ChatEventId,
    val chatId: ChatId,
    val eventType: ChatEventType,
    val actorUserId: UserId,
    val actorUsername: String,
    val targetUserId: UserId?,
    val targetUsername: String?,
    val createdAt: Instant
)
