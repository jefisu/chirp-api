package com.plcoding.chirp.infra.database.repositories

import com.plcoding.chirp.infra.database.entities.ChatMessageFileEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface ChatMessageFileRepository : JpaRepository<ChatMessageFileEntity, UUID> {
    fun existsByUrl(url: String): Boolean
}
