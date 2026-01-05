package com.plcoding.chirp.service

import com.plcoding.chirp.api.dto.ChatFileUploadDto
import com.plcoding.chirp.api.dto.ChatFileUploadRequest
import com.plcoding.chirp.domain.exception.ChatNotFoundException
import com.plcoding.chirp.domain.exception.TooManyAttachmentsException
import com.plcoding.chirp.domain.type.ChatId
import com.plcoding.chirp.domain.type.UserId
import com.plcoding.chirp.infra.database.repositories.ChatMessageFileRepository
import com.plcoding.chirp.infra.database.repositories.ChatRepository
import com.plcoding.chirp.infra.storage.StorageDestination
import com.plcoding.chirp.infra.storage.SupabaseStorageService
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
class ChatMessageFileService(
    private val supabaseStorageService: SupabaseStorageService,
    private val chatRepository: ChatRepository,
    private val chatMessageFileRepository: ChatMessageFileRepository
) {
    private val logger = LoggerFactory.getLogger(ChatMessageFileService::class.java)

    fun generateUploadCredentials(
        userId: UserId,
        chatId: ChatId,
        requests: List<ChatFileUploadRequest>
    ): List<ChatFileUploadDto> {
        chatRepository.findChatById(chatId, userId)
            ?: throw ChatNotFoundException()

        if (requests.size > 10) {
            throw TooManyAttachmentsException(10)
        }

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

    @Scheduled(cron = "0 0 3 * * *")
    fun cleanUpOrphanedFiles() {
        logger.info("Starting orphaned files cleanup")
        val bucket = "chat-images"
        val rootFiles = supabaseStorageService.listFiles(bucket = bucket)

        rootFiles.forEach { folder ->
            val chatId = folder.name
            val filesInChat = supabaseStorageService.listFiles(bucket = bucket, path = chatId)

            filesInChat.forEach { file ->
                val fullPath = "$chatId/${file.name}"
                val publicUrl = supabaseStorageService.getPublicUrl(bucket, fullPath)

                if (!chatMessageFileRepository.existsByUrl(publicUrl)) {
                    logger.info("Deleting orphaned file: $fullPath")
                    supabaseStorageService.deleteFile(publicUrl)
                }
            }
        }
        logger.info("Orphaned files cleanup finished")
    }
}
