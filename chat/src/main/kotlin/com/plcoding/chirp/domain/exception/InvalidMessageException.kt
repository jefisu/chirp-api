package com.plcoding.chirp.domain.exception

class InvalidMessageException(
    message: String = "Message content cannot be empty if no images are attached"
) : RuntimeException(message)
