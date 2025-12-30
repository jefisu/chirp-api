package com.plcoding.chirp.infra.database

import com.plcoding.chirp.domain.type.UserId
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.transaction.annotation.Transactional

interface DeviceTokenRepository: JpaRepository<DeviceTokenEntity, Long> {
    fun findByUserIdIn(userIds: List<UserId>): List<DeviceTokenEntity>
    fun findByToken(token: String): DeviceTokenEntity?
    
    @Transactional
    fun deleteByToken(token: String)
}