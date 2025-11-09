package com.plcoding.chirp.api.dto

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.plcoding.chirp.api.util.Password
import jakarta.validation.constraints.NotBlank

data class ResetPasswordRequest @JsonCreator constructor(
    @JsonProperty("token")
    @field:NotBlank
    val token: String,
    @JsonProperty("newPassword")
    @field:Password
    val newPassword: String
)
