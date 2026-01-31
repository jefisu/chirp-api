package com.plcoding.chirp.service

import com.plcoding.chirp.api.dto.ChatEventDto
import com.plcoding.chirp.api.dto.ChatHistoryItemDto
import com.plcoding.chirp.api.dto.ChatMessageDto
import com.plcoding.chirp.api.mappers.toChatMessageDto
import com.plcoding.chirp.domain.event.ChatCreatedEvent
import com.plcoding.chirp.domain.event.ChatDeletedByAdminEvent
import com.plcoding.chirp.domain.event.ChatParticipantLeftEvent
import com.plcoding.chirp.domain.event.ChatParticipantsJoinedEvent
import com.plcoding.chirp.domain.event.ParticipantAddedEvent
import com.plcoding.chirp.domain.event.ParticipantRemovedByAdminEvent
import com.plcoding.chirp.domain.events.chat.ChatEvent
import com.plcoding.chirp.domain.exception.AdminLeaveRequiresConfirmationException
import com.plcoding.chirp.domain.exception.CannotRemoveSelfException
import com.plcoding.chirp.domain.exception.ChatNotFoundException
import com.plcoding.chirp.domain.exception.ChatParticipantNotFoundException
import com.plcoding.chirp.domain.exception.ForbiddenException
import com.plcoding.chirp.domain.exception.InvalidChatSizeException
import com.plcoding.chirp.domain.exception.NotChatAdminException
import com.plcoding.chirp.domain.models.Chat
import com.plcoding.chirp.domain.models.ChatEventType
import com.plcoding.chirp.domain.models.ChatMessage
import com.plcoding.chirp.domain.type.ChatId
import com.plcoding.chirp.infra.database.entities.ChatEntity
import com.plcoding.chirp.infra.database.entities.ChatEventEntity
import com.plcoding.chirp.infra.database.mappers.toChat
import com.plcoding.chirp.infra.database.mappers.toChatEventDto
import com.plcoding.chirp.infra.database.repositories.ChatEventRepository
import com.plcoding.chirp.infra.database.repositories.ChatParticipantRepository
import com.plcoding.chirp.infra.database.repositories.ChatRepository
import com.plcoding.chirp.domain.type.UserId
import com.plcoding.chirp.infra.database.mappers.toChatMessage
import com.plcoding.chirp.infra.database.repositories.ChatMessageRepository
import com.plcoding.chirp.infra.message_queue.EventPublisher
import org.springframework.cache.annotation.Cacheable
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.domain.PageRequest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class ChatService(
    private val chatRepository: ChatRepository,
    private val chatParticipantRepository: ChatParticipantRepository,
    private val chatMessageRepository: ChatMessageRepository,
    private val chatEventRepository: ChatEventRepository,
    private val applicationEventPublisher: ApplicationEventPublisher,
    private val eventPublisher: EventPublisher,
) {

    @Cacheable(
        value = ["messages"],
        key = "#chatId",
        condition = "#before == null && #pageSize <= 50",
        sync = true
    )
    fun getChatMessages(
        chatId: ChatId,
        before: Instant?,
        pageSize: Int
    ): List<ChatMessageDto> {
        return chatMessageRepository
            .findByChatIdBefore(
                chatId = chatId,
                before = before ?: Instant.now(),
                pageable = PageRequest.of(0, pageSize)
            )
            .content
            .asReversed()
            .map { it.toChatMessage().toChatMessageDto() }
    }

    fun getChatById(
        chatId: ChatId,
        requestUserId: UserId
    ): Chat? {
        return chatRepository
            .findChatById(chatId, requestUserId)
            ?.toChat(lastMessageForChat(chatId))
    }

    fun findChatsByUser(userId: UserId): List<Chat> {
        val chatEntities = chatRepository.findAllByUserId(userId)
        val chatIds = chatEntities.mapNotNull { it.id }
        val latestMessages = chatMessageRepository
            .findLatestMessagesByChatIds(chatIds.toSet())
            .associateBy { it.chatId }

        return chatEntities
            .map {
                it.toChat(lastMessage = latestMessages[it.id]?.toChatMessage())
            }
            .sortedByDescending { it.lastActivityAt }
    }

    @Transactional
    fun createChat(
        creatorId: UserId,
        otherUserIds: Set<UserId>
    ): Chat {
        val otherParticipants = chatParticipantRepository.findByUserIdIn(
            userIds = otherUserIds
        )

        val allParticipants = (otherParticipants + creatorId)
        if(allParticipants.size < 2) {
            throw InvalidChatSizeException()
        }

        val creator = chatParticipantRepository.findByIdOrNull(creatorId)
            ?: throw ChatParticipantNotFoundException(creatorId)

        return chatRepository.saveAndFlush(
            ChatEntity(
                creator = creator,
                participants = setOf(creator) + otherParticipants
            )
        ).toChat(lastMessage = null).also {
            applicationEventPublisher.publishEvent(
                ChatCreatedEvent(
                    chatId = it.id,
                    participantIds = it.participants.map { it.userId }
                )
            )
        }
    }

    @Transactional
    fun addParticipantsToChat(
        requestUserId: UserId,
        chatId: ChatId,
        userIds: Set<UserId>
    ): Chat {
        val chat = chatRepository.findByIdOrNull(chatId)
            ?: throw ChatNotFoundException()

        val addedByParticipant = chat.participants.find { it.userId == requestUserId }
            ?: throw ForbiddenException()

        val users = userIds.map { userId ->
            chatParticipantRepository.findByIdOrNull(userId)
                ?: throw ChatParticipantNotFoundException(userId)
        }

        val lastMessage = lastMessageForChat(chatId)
        val updatedChat = chatRepository.save(
            chat.apply {
                this.participants = chat.participants + users
            }
        ).toChat(lastMessage)

        applicationEventPublisher.publishEvent(
            ChatParticipantsJoinedEvent(
                chatId = chatId,
                userIds = userIds
            )
        )

        users.forEach { addedUser ->
            val event = chatEventRepository.save(
                ChatEventEntity(
                    chatId = chatId,
                    eventType = ChatEventType.PARTICIPANT_ADDED,
                    actor = addedByParticipant,
                    targetUser = addedUser
                )
            )

            applicationEventPublisher.publishEvent(
                ParticipantAddedEvent(
                    chatId = chatId,
                    addedByUserId = requestUserId,
                    addedByUsername = addedByParticipant.username,
                    addedUserId = addedUser.userId,
                    addedUsername = addedUser.username,
                    eventId = event.id!!,
                    createdAt = event.createdAt
                )
            )

            eventPublisher.publish(
                ChatEvent.ParticipantAdded(
                    addedUserId = addedUser.userId,
                    addedUsername = addedUser.username,
                    chatId = chatId
                )
            )
        }

        return updatedChat
    }

    @Transactional
    fun removeParticipantFromChat(
        chatId: ChatId,
        userId: UserId
    ) {
        val chat = chatRepository.findByIdOrNull(chatId)
            ?: throw ChatNotFoundException()
        val participant = chat.participants.find { it.userId == userId }
            ?: throw ChatParticipantNotFoundException(userId)

        val newParticipantsSize = chat.participants.size - 1
        if(newParticipantsSize == 0) {
            chatRepository.deleteById(chatId)
            return
        }

        chatRepository.save(
            chat.apply {
                this.participants = chat.participants - participant
            }
        )

        applicationEventPublisher.publishEvent(
            ChatParticipantLeftEvent(
                chatId = chatId,
                userId = userId
            )
        )
    }

    private fun lastMessageForChat(chatId: ChatId): ChatMessage? {
        return chatMessageRepository
            .findLatestMessagesByChatIds(setOf(chatId))
            .firstOrNull()
            ?.toChatMessage()
    }

    @Transactional
    fun removeParticipantByAdmin(
        chatId: ChatId,
        adminUserId: UserId,
        targetUserId: UserId
    ) {
        val chat = chatRepository.findByIdOrNull(chatId)
            ?: throw ChatNotFoundException()

        if (chat.creator.userId != adminUserId) {
            throw NotChatAdminException()
        }

        if (adminUserId == targetUserId) {
            throw CannotRemoveSelfException()
        }

        val targetParticipant = chat.participants.find { it.userId == targetUserId }
            ?: throw ChatParticipantNotFoundException(targetUserId)

        val adminParticipant = chat.participants.find { it.userId == adminUserId }
            ?: throw ChatParticipantNotFoundException(adminUserId)

        chatRepository.save(
            chat.apply {
                this.participants = chat.participants - targetParticipant
            }
        )

        val event = chatEventRepository.save(
            ChatEventEntity(
                chatId = chatId,
                eventType = ChatEventType.PARTICIPANT_REMOVED,
                actor = adminParticipant,
                targetUser = targetParticipant
            )
        )

        applicationEventPublisher.publishEvent(
            ParticipantRemovedByAdminEvent(
                chatId = chatId,
                adminUserId = adminUserId,
                adminUsername = adminParticipant.username,
                removedUserId = targetUserId,
                removedUsername = targetParticipant.username,
                eventId = event.id!!,
                createdAt = event.createdAt
            )
        )

        eventPublisher.publish(
            ChatEvent.ParticipantRemoved(
                removedUserId = targetUserId,
                removedUsername = targetParticipant.username,
                chatId = chatId
            )
        )
    }

    @Transactional
    fun leaveChat(
        chatId: ChatId,
        userId: UserId,
        confirmDelete: Boolean = false
    ) {
        val chat = chatRepository.findByIdOrNull(chatId)
            ?: throw ChatNotFoundException()

        val isAdmin = chat.creator.userId == userId

        if (isAdmin) {
            if (!confirmDelete) {
                throw AdminLeaveRequiresConfirmationException()
            }

            val memberUserIds = chat.participants.map { it.userId }.toSet()
            chatRepository.deleteById(chatId)
            applicationEventPublisher.publishEvent(
                ChatDeletedByAdminEvent(
                    chatId = chatId,
                    adminUserId = userId,
                    memberUserIds = memberUserIds
                )
            )
        } else {
            removeParticipantFromChat(chatId, userId)
        }
    }

    @Cacheable(
        value = ["chat_history"],
        key = "#chatId",
        condition = "#before == null && #pageSize <= 50",
        sync = true
    )
    fun getChatHistory(
        chatId: ChatId,
        before: Instant?,
        pageSize: Int
    ): List<ChatHistoryItemDto> {
        val beforeTime = before ?: Instant.now()

        val messages = chatMessageRepository.findByChatIdBefore(
            chatId = chatId,
            before = beforeTime,
            pageable = PageRequest.of(0, pageSize)
        ).content

        val events = chatEventRepository.findByChatIdBefore(
            chatId = chatId,
            before = beforeTime,
            pageable = PageRequest.of(0, pageSize)
        ).content

        val messageItems = messages.map {
            ChatHistoryItemDto.Message(
                message = it.toChatMessage().toChatMessageDto(),
                createdAt = it.createdAt
            )
        }

        val eventItems = events.map {
            ChatHistoryItemDto.Event(
                event = it.toChatEventDto(),
                createdAt = it.createdAt
            )
        }

        return (messageItems + eventItems)
            .sortedByDescending { it.createdAt }
            .take(pageSize)
    }
}