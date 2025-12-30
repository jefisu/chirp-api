package com.plcoding.chirp.api.controllers

import com.plcoding.chirp.api.dto.ChatFileUploadRequest
import com.plcoding.chirp.api.dto.ChatFileUploadDto
import com.plcoding.chirp.api.util.requestUserId
import com.plcoding.chirp.domain.type.ChatId
import com.plcoding.chirp.service.ChatMessageFileService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/chat/{chatId}")
class ChatMessageFileController(
    private val chatMessageFileService: ChatMessageFileService
) {

    @PostMapping("/files-upload")
    fun generateUploadCredentials(
        @PathVariable chatId: ChatId,
        @Valid @RequestBody requests: List<ChatFileUploadRequest>
    ): List<ChatFileUploadDto> {
        return chatMessageFileService.generateUploadCredentials(
            userId = requestUserId,
            chatId = chatId,
            requests = requests
        )
    }
}
