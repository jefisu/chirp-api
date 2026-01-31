package com.plcoding.chirp.infra.database.repositories

import com.plcoding.chirp.domain.type.ChatEventId
import com.plcoding.chirp.domain.type.ChatId
import com.plcoding.chirp.infra.database.entities.ChatEventEntity
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.Instant

interface ChatEventRepository : JpaRepository<ChatEventEntity, ChatEventId> {

    @Query("""
        SELECT e
        FROM ChatEventEntity e
        WHERE e.chatId = :chatId
        AND e.createdAt < :before
        ORDER BY e.createdAt DESC
    """)
    fun findByChatIdBefore(
        chatId: ChatId,
        before: Instant,
        pageable: Pageable
    ): Slice<ChatEventEntity>
}
