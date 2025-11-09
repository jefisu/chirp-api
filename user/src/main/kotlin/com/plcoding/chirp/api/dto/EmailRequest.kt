package com.plcoding.chirp.api.dto

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.constraints.Email

data class EmailRequest @JsonCreator constructor(
    @JsonProperty("email")
    @field:Email
    val email: String
)
