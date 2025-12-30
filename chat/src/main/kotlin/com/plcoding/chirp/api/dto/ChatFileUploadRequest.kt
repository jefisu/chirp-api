package com.plcoding.chirp.api.dto

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.constraints.NotBlank

data class ChatFileUploadRequest @JsonCreator constructor(
    @JsonProperty("fileName")
    @field:NotBlank
    val fileName: String,
    @JsonProperty("mimeType")
    @field:NotBlank
    val mimeType: String
)
