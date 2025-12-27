package com.plcoding.chirp.infra.storage

import com.plcoding.chirp.domain.type.ChatId

sealed class StorageDestination(val bucket: String) {
    data object ProfilePicture : StorageDestination("profile-pictures")
    data class ChatImage(val chatId: ChatId) : StorageDestination("chat-images")
}
