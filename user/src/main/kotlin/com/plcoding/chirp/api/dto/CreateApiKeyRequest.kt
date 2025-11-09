package com.plcoding.chirp.api.dto

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

data class CreateApiKeyRequest @JsonCreator constructor(
    @JsonProperty("email") val email: String
)
