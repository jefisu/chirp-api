package com.plcoding.chirp.infra.storage

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.web.client.RestClient

@Configuration
class SupabaseRestClientConfig(
    @param:Value("\${supabase.url}") private val supabaseUrl: String,
    @param:Value("\${supabase.service-key}") private val supabaseServiceKey: String,
) {

    @Bean
    fun supabaseRestClient(objectMapper: ObjectMapper): RestClient {
        @Suppress("DEPRECATION")
        return RestClient.builder()
            .baseUrl(supabaseUrl)
            .defaultHeader("Authorization", "Bearer $supabaseServiceKey")
            .messageConverters { converters ->
                converters.removeIf { it is MappingJackson2HttpMessageConverter }
                converters.add(0, MappingJackson2HttpMessageConverter(objectMapper))
            }
            .build()
    }
}
