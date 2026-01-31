package com.plcoding.chirp.infra.database.entities

import com.plcoding.chirp.domain.models.ChatEventType
import com.plcoding.chirp.domain.type.ChatEventId
import com.plcoding.chirp.domain.type.ChatId
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import java.time.Instant

@Entity
@Table(
    name = "chat_events",
    schema = "chat_service",
    indexes = [
        Index(
            name = "idx_chat_events_chat_id_created_at",
            columnList = "chat_id,created_at DESC"
        )
    ]
)
class ChatEventEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: ChatEventId? = null,

    @Column(name = "chat_id", nullable = false, updatable = false)
    var chatId: ChatId,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_id", nullable = false, insertable = false, updatable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    var chat: ChatEntity? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false)
    var eventType: ChatEventType,

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "actor_user_id", nullable = false)
    var actor: ChatParticipantEntity,

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "target_user_id")
    var targetUser: ChatParticipantEntity? = null,

    @CreationTimestamp
    var createdAt: Instant = Instant.now()
)
