package com.plcoding.chirp.api.dto

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import java.time.Instant

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "itemType"
)
@JsonSubTypes(
    JsonSubTypes.Type(value = ChatHistoryItemDto.Message::class, name = "MESSAGE"),
    JsonSubTypes.Type(value = ChatHistoryItemDto.Event::class, name = "EVENT")
)
sealed interface ChatHistoryItemDto {
    val createdAt: Instant

    data class Message(
        val message: ChatMessageDto,
        override val createdAt: Instant
    ) : ChatHistoryItemDto

    data class Event(
        val event: ChatEventDto,
        override val createdAt: Instant
    ) : ChatHistoryItemDto
}
