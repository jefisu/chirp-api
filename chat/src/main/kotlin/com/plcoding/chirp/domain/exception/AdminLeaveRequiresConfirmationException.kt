package com.plcoding.chirp.domain.exception

class AdminLeaveRequiresConfirmationException : RuntimeException(
    "Admin leaving requires confirmation as it will delete the chat for all members"
)
