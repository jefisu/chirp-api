package com.plcoding.chirp.infra.database.mappers

import com.plcoding.chirp.api.dto.ChatEventDto
import com.plcoding.chirp.domain.models.Chat
import com.plcoding.chirp.domain.models.ChatMessage
import com.plcoding.chirp.domain.models.ChatParticipant
import com.plcoding.chirp.infra.database.entities.ChatEntity
import com.plcoding.chirp.infra.database.entities.ChatEventEntity
import com.plcoding.chirp.infra.database.entities.ChatMessageEntity
import com.plcoding.chirp.infra.database.entities.ChatParticipantEntity

fun ChatEntity.toChat(lastMessage: ChatMessage? = null): Chat {
    return Chat(
        id = id!!,
        participants = participants.map {
            it.toChatParticipant()
        }.toSet(),
        creator = creator.toChatParticipant(),
        lastActivityAt = lastMessage?.createdAt ?: createdAt,
        createdAt = createdAt,
        lastMessage = lastMessage
    )
}

fun ChatParticipantEntity.toChatParticipant(): ChatParticipant {
    return ChatParticipant(
        userId = userId,
        username = username,
        email = email,
        profilePictureUrl = profilePictureUrl
    )
}

fun ChatParticipant.toChatParticipantEntity(): ChatParticipantEntity {
    return ChatParticipantEntity(
        userId = userId,
        username = username,
        email = email,
        profilePictureUrl = profilePictureUrl
    )
}

fun ChatMessageEntity.toChatMessage(): ChatMessage {
    return ChatMessage(
        id = id!!,
        chatId = chatId,
        sender = sender.toChatParticipant(),
        content = content,
        attachedFiles = attachedFiles.map { it.toChatMessageFile() },
        createdAt = createdAt
    )
}

fun ChatEventEntity.toChatEventDto(): ChatEventDto {
    return ChatEventDto(
        id = id!!,
        chatId = chatId,
        eventType = eventType,
        actorUserId = actor.userId,
        actorUsername = actor.username,
        targetUserId = targetUser?.userId,
        targetUsername = targetUser?.username,
        createdAt = createdAt
    )
}
