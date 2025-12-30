package com.plcoding.chirp.api.controllers

import org.springframework.cache.CacheManager
import org.springframework.context.annotation.Profile
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@Profile("dev")
@RestController
@RequestMapping("/api/cache")
class CacheController(
    private val cacheManager: CacheManager
) {

    @DeleteMapping
    fun clearCache(@RequestParam("name", required = false) name: String?) {
        if (name != null) {
            cacheManager.getCache(name)?.clear()
        } else {
            cacheManager.cacheNames.forEach { cacheName ->
                cacheManager.getCache(cacheName)?.clear()
            }
        }
    }
}
