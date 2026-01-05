package com.plcoding.chirp.infra.storage

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.plcoding.chirp.domain.exception.InvalidProfilePictureException
import com.plcoding.chirp.domain.exception.StorageException
import com.plcoding.chirp.domain.models.FileUploadCredentials
import com.plcoding.chirp.domain.type.UserId
import com.plcoding.chirp.infra.storage.StorageDestination.ChatImage
import com.plcoding.chirp.infra.storage.StorageDestination.ProfilePicture
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient
import java.time.Instant
import java.util.UUID

@Service
class SupabaseStorageService(
    @param:Value("\${supabase.url}") private val supabaseUrl: String,
    private val supabaseRestClient: RestClient,
) {
    companion object {
        private val allowedMimeTypes = mapOf(
            "image/jpeg" to "jpg",
            "image/jpg" to "jpg",
            "image/png" to "png",
            "image/webp" to "webp",
        )
    }

    fun generateSignedUploadUrl(
        userId: UserId,
        mimeType: String,
        destination: StorageDestination
    ): FileUploadCredentials {
        val extension = allowedMimeTypes[mimeType]
            ?: throw InvalidProfilePictureException("Invalid mime type $mimeType")

        val fileName = "user_${userId}_${UUID.randomUUID()}.$extension"
        val path = when (destination) {
            is ProfilePicture -> "${destination.bucket}/$fileName"
            is ChatImage -> "${destination.bucket}/${destination.chatId}/$fileName"
        }

        val publicUrl = "$supabaseUrl/storage/v1/object/public/$path"
        val uploadUrl = createSignedUrl(
            path = path,
            expiresInSeconds = 300
        )

        return FileUploadCredentials(
            uploadUrl = uploadUrl,
            publicUrl = publicUrl,
            headers = mapOf(
                "Content-Type" to mimeType,
            ),
            expiresAt = Instant.now().plusSeconds(300)
        )
    }

    fun deleteFile(url: String) {
        val path = if (url.contains("/object/public/")) {
            url.substringAfter("/object/public/")
        } else throw StorageException("Invalid file URL format")

        val deleteUrl = "/storage/v1/object/$path"

        val response = supabaseRestClient
            .delete()
            .uri(deleteUrl)
            .retrieve()
            .toBodilessEntity()

        if (response.statusCode.isError) {
            throw StorageException("Unable to delete file: ${response.statusCode.value()}")
        }
    }

    fun listFiles(
        bucket: String,
        path: String? = null,
        offset: Int = 0,
        limit: Int = 100
    ): List<StorageObject> {
        val searchPath = path ?: ""
        val json = mapOf(
            "prefix" to searchPath,
            "limit" to limit,
            "offset" to offset,
            "sortBy" to mapOf(
                "column" to "name",
                "order" to "asc"
            )
        )

        return supabaseRestClient
            .post()
            .uri("/storage/v1/object/list/$bucket")
            .header("Content-Type", "application/json")
            .body(json)
            .retrieve()
            .body(Array<StorageObject>::class.java)
            ?.toList() ?: emptyList()
    }

    fun getPublicUrl(bucket: String, path: String): String {
        return "$supabaseUrl/storage/v1/object/public/$bucket/$path"
    }

    private fun createSignedUrl(
        path: String,
        expiresInSeconds: Int
    ): String {
        val json = """
            { "expiresIn": $expiresInSeconds }
        """.trimIndent()

        val response = supabaseRestClient
            .post()
            .uri("/storage/v1/object/upload/sign/$path")
            .header("Content-Type", "application/json")
            .body(json)
            .retrieve()
            .body(SignedUploadResponse::class.java)
            ?: throw StorageException("Failed to create signed URL")

        return "$supabaseUrl/storage/v1${response.url}"
    }

    private data class SignedUploadResponse @JsonCreator constructor(
        @JsonProperty("url")
        val url: String
    )

    data class StorageObject(
        val name: String,
        val id: String?,
        val metadata: Map<String, Any>?,
        @JsonProperty("created_at") val createdAt: String?,
        @JsonProperty("updated_at") val updatedAt: String?,
        @JsonProperty("last_accessed_at") val lastAccessedAt: String?
    )
}
