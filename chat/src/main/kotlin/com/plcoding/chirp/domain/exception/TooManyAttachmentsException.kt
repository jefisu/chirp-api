package com.plcoding.chirp.domain.exception

import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

class TooManyAttachmentsException(
    maxAttachments: Int
): ResponseStatusException(
    HttpStatus.BAD_REQUEST,
    "You cannot attach more than $maxAttachments files to a message."
)
