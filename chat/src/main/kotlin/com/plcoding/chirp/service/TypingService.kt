package com.plcoding.chirp.service

import com.plcoding.chirp.api.dto.ws.OutcomingTypingEventDto
import com.plcoding.chirp.domain.type.ChatId
import com.plcoding.chirp.domain.type.UserId
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Service
import java.time.Duration

@Service
class TypingService(
    private val redisTemplate: StringRedisTemplate
) {

    fun processTypingEvent(event: OutcomingTypingEventDto) {
        val key = "typing:${event.chatId}:${event.userId}"

        if (event.isTyping) {
            redisTemplate.opsForValue().set(key, event.userName, Duration.ofSeconds(4))
        } else {
            redisTemplate.delete(key)
        }
    }

    fun clearTypingStatus(chatId: ChatId, userId: UserId) {
        val key = "typing:$chatId:$userId"
        redisTemplate.delete(key)
    }
}