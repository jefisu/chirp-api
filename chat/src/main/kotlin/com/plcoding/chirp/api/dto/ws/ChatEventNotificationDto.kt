package com.plcoding.chirp.api.dto.ws

import com.plcoding.chirp.domain.models.ChatEventType
import com.plcoding.chirp.domain.type.ChatEventId
import com.plcoding.chirp.domain.type.ChatId
import com.plcoding.chirp.domain.type.UserId
import java.time.Instant

data class ChatEventNotificationDto(
    val chatId: ChatId,
    val eventId: ChatEventId,
    val eventType: ChatEventType,
    val actorUserId: UserId,
    val actorUsername: String,
    val targetUserId: UserId?,
    val targetUsername: String?,
    val createdAt: Instant
)
