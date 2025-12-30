package com.plcoding.chirp.service

import com.plcoding.chirp.domain.event.MessageDeletedEvent
import com.plcoding.chirp.domain.events.chat.ChatEvent
import com.plcoding.chirp.domain.exception.ChatNotFoundException
import com.plcoding.chirp.domain.exception.ChatParticipantNotFoundException
import com.plcoding.chirp.domain.exception.ForbiddenException
import com.plcoding.chirp.domain.exception.InvalidMessageException
import com.plcoding.chirp.domain.exception.MessageNotFoundException
import com.plcoding.chirp.domain.exception.TooManyAttachmentsException
import com.plcoding.chirp.domain.models.ChatMessage
import com.plcoding.chirp.domain.models.ChatMessageFile
import com.plcoding.chirp.domain.type.ChatId
import com.plcoding.chirp.domain.type.ChatMessageId
import com.plcoding.chirp.domain.type.UserId
import com.plcoding.chirp.infra.database.entities.ChatMessageEntity
import com.plcoding.chirp.infra.database.entities.ChatMessageFileEntity
import com.plcoding.chirp.infra.database.mappers.toChatMessage
import com.plcoding.chirp.infra.database.repositories.ChatMessageRepository
import com.plcoding.chirp.infra.database.repositories.ChatParticipantRepository
import com.plcoding.chirp.infra.database.repositories.ChatRepository
import com.plcoding.chirp.infra.message_queue.EventPublisher
import org.springframework.cache.annotation.CacheEvict
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ChatMessageService(
    private val chatRepository: ChatRepository,
    private val chatMessageRepository: ChatMessageRepository,
    private val chatParticipantRepository: ChatParticipantRepository,
    private val applicationEventPublisher: ApplicationEventPublisher,
    private val eventPublisher: EventPublisher,
    private val messageCacheManager: MessageCacheManager
) {

    @Transactional
    @CacheEvict(
        value = ["messages"],
        key = "#chatId",
    )
    fun sendMessage(
        chatId: ChatId,
        messageId: ChatMessageId,
        senderId: UserId,
        content: String?,
        attachedFiles: List<ChatMessageFile> = emptyList()
    ): ChatMessage {
        val chat = chatRepository.findChatById(chatId, senderId)
            ?: throw ChatNotFoundException()
        val sender = chatParticipantRepository.findByIdOrNull(senderId)
            ?: throw ChatParticipantNotFoundException(senderId)

        if (content.isNullOrBlank() && attachedFiles.isEmpty()) {
            throw InvalidMessageException()
        }

        if (attachedFiles.size > 10) {
            throw TooManyAttachmentsException(10)
        }

        val messageEntity = ChatMessageEntity(
            id = messageId,
            content = content?.trim(),
            chatId = chatId,
            chat = chat,
            sender = sender
        ).apply {
            this.attachedFiles = attachedFiles.map {
                ChatMessageFileEntity(
                    url = it.url,
                    type = it.type,
                    createdAt = createdAt,
                    message = this
                )
            }
        }

        val savedMessage = chatMessageRepository.saveAndFlush(messageEntity)

        eventPublisher.publish(
            event = ChatEvent.NewMessage(
                senderId = sender.userId,
                senderUsername = sender.username,
                recipientIds = chat.participants.map { it.userId }.toSet(),
                chatId = chatId,
                message = savedMessage.content ?: "Image(s)"
            )
        )

        return savedMessage.toChatMessage()
    }

    @Transactional
    fun deleteMessage(
        messageId: ChatMessageId,
        requestUserId: UserId
    ) {
        val message = chatMessageRepository.findByIdOrNull(messageId)
            ?: throw MessageNotFoundException(messageId)

        if (message.sender.userId != requestUserId) {
            throw ForbiddenException()
        }

        chatMessageRepository.delete(message)

        applicationEventPublisher.publishEvent(
            MessageDeletedEvent(
                chatId = message.chatId,
                messageId = messageId
            )
        )

        messageCacheManager.evictMessagesCache(message.chatId)
    }


}

@Component
class MessageCacheManager {
    @CacheEvict(
        value = ["messages"],
        key = "#chatId",
    )
    fun evictMessagesCache(chatId: ChatId) {
        // NO-OP: Let Spring handle the cache evict
    }
}
