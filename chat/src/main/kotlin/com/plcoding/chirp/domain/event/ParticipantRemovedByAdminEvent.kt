package com.plcoding.chirp.domain.event

import com.plcoding.chirp.domain.type.ChatEventId
import com.plcoding.chirp.domain.type.ChatId
import com.plcoding.chirp.domain.type.UserId
import java.time.Instant

data class ParticipantRemovedByAdminEvent(
    val chatId: ChatId,
    val adminUserId: UserId,
    val adminUsername: String,
    val removedUserId: UserId,
    val removedUsername: String,
    val eventId: ChatEventId,
    val createdAt: Instant
)
