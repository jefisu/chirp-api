package com.plcoding.chirp.service

import com.plcoding.chirp.api.dto.ChatFileUploadDto
import com.plcoding.chirp.api.dto.ChatFileUploadRequest
import com.plcoding.chirp.domain.exception.ChatNotFoundException
import com.plcoding.chirp.domain.type.ChatId
import com.plcoding.chirp.domain.type.UserId
import com.plcoding.chirp.infra.database.repositories.ChatRepository
import com.plcoding.chirp.infra.storage.StorageDestination
import com.plcoding.chirp.infra.storage.SupabaseStorageService
import org.springframework.stereotype.Service

@Service
class ChatMessageFileService(
    private val supabaseStorageService: SupabaseStorageService,
    private val chatRepository: ChatRepository
) {

    fun generateUploadCredentials(
        userId: UserId,
        chatId: ChatId,
        requests: List<ChatFileUploadRequest>
    ): List<ChatFileUploadDto> {
        chatRepository.findChatById(chatId, userId)
            ?: throw ChatNotFoundException()

        return requests.map { request ->
            val credentials = supabaseStorageService.generateSignedUploadUrl(
                userId = userId,
                mimeType = request.mimeType,
                destination = StorageDestination.ChatImage(chatId)
            )
            ChatFileUploadDto(
                fileName = request.fileName,
                uploadUrl = credentials.uploadUrl,
                publicUrl = credentials.publicUrl,
                headers = credentials.headers,
                expiresAt = credentials.expiresAt
            )
        }
    }
}
