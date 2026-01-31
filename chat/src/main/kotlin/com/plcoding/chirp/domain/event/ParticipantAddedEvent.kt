package com.plcoding.chirp.domain.event

import com.plcoding.chirp.domain.type.ChatEventId
import com.plcoding.chirp.domain.type.ChatId
import com.plcoding.chirp.domain.type.UserId
import java.time.Instant

data class ParticipantAddedEvent(
    val chatId: ChatId,
    val addedByUserId: UserId,
    val addedByUsername: String,
    val addedUserId: UserId,
    val addedUsername: String,
    val eventId: ChatEventId,
    val createdAt: Instant
)
