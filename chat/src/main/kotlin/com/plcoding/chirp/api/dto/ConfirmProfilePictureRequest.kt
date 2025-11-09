package com.plcoding.chirp.api.dto

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.constraints.NotBlank

data class ConfirmProfilePictureRequest @JsonCreator constructor(
    @JsonProperty("publicUrl")
    @field:NotBlank
    val publicUrl: String
)
