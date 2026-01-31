package com.plcoding.chirp.domain.event

import com.plcoding.chirp.domain.type.ChatId
import com.plcoding.chirp.domain.type.UserId

data class ChatDeletedByAdminEvent(
    val chatId: ChatId,
    val adminUserId: UserId,
    val memberUserIds: Set<UserId>
)
