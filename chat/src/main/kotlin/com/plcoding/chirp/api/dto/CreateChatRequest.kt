package com.plcoding.chirp.api.dto

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.plcoding.chirp.domain.type.UserId
import jakarta.validation.constraints.Size

data class CreateChatRequest @JsonCreator constructor(
    @JsonProperty("otherUserIds")
    @field:Size(
        min = 1,
        message = "Chats must have at least 2 unique participants"
    )
    val otherUserIds: List<UserId>
)
