package com.plcoding.chirp.api.controllers

import com.plcoding.chirp.api.dto.ChatImageUploadRequest
import com.plcoding.chirp.api.dto.ChatImageUploadDto
import com.plcoding.chirp.api.util.requestUserId
import com.plcoding.chirp.domain.type.ChatId
import com.plcoding.chirp.service.ChatImageService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/chat/{chatId}")
class ChatImageController(
    private val chatImageService: ChatImageService
) {

    @PostMapping("/images-upload")
    fun generateUploadCredentials(
        @PathVariable chatId: ChatId,
        @Valid @RequestBody requests: List<ChatImageUploadRequest>
    ): List<ChatImageUploadDto> {
        return chatImageService.generateUploadCredentials(
            userId = requestUserId,
            chatId = chatId,
            requests = requests
        )
    }
}
