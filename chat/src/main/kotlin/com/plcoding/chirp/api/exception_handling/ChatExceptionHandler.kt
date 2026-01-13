package com.plcoding.chirp.api.exception_handling

import com.plcoding.chirp.domain.exception.ChatNotFoundException
import com.plcoding.chirp.domain.exception.ChatParticipantNotFoundException
import com.plcoding.chirp.domain.exception.InvalidChatSizeException
import com.plcoding.chirp.domain.exception.InvalidMessageException
import com.plcoding.chirp.domain.exception.InvalidProfilePictureException
import com.plcoding.chirp.domain.exception.MessageNotFoundException
import com.plcoding.chirp.domain.exception.StorageException
import com.plcoding.chirp.domain.exception.TooManyAttachmentsException
import com.plcoding.chirp.domain.exception.UserNotInChatException
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class ChatExceptionHandler {

    @ExceptionHandler(
        ChatNotFoundException::class,
        MessageNotFoundException::class,
        ChatParticipantNotFoundException::class,
    )
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun onNotFound(e: Exception) = mapOf(
        "code" to "NOT_FOUND",
        "message" to e.message
    )

    @ExceptionHandler(InvalidChatSizeException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun onInvalidChatSize(e: InvalidChatSizeException) = mapOf(
        "code" to "INVALID_CHAT_SIZE",
        "message" to e.message
    )

    @ExceptionHandler(InvalidProfilePictureException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun onInvalidProfilePicture(e: InvalidProfilePictureException) = mapOf(
        "code" to "INVALID_PROFILE_PICTURE",
        "message" to e.message
    )

    @ExceptionHandler(StorageException::class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    fun onStorageError(e: StorageException) = mapOf(
        "code" to "STORAGE_ERROR",
        "message" to e.message
    )

    @ExceptionHandler(InvalidMessageException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun onInvalidMessage(e: InvalidMessageException) = mapOf(
        "code" to "INVALID_MESSAGE_ERROR",
        "message" to e.message
    )

    @ExceptionHandler(TooManyAttachmentsException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun onTooManyAttachments(e: TooManyAttachmentsException) = mapOf(
        "code" to "TOO_MANY_ATTACHMENTS",
        "message" to e.message
    )

    @ExceptionHandler(UserNotInChatException::class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    fun onUserNotInChat(e: UserNotInChatException) = mapOf(
        "code" to "USER_NOT_IN_CHAT",
        "message" to e.message
    )
}