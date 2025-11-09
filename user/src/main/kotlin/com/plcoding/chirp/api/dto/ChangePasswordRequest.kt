package com.plcoding.chirp.api.dto

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.plcoding.chirp.api.util.Password
import jakarta.validation.constraints.NotBlank

data class ChangePasswordRequest @JsonCreator constructor(
    @JsonProperty("oldPassword")
    @field:NotBlank
    val oldPassword: String,
    @JsonProperty("newPassword")
    @field:Password
    val newPassword: String
)
