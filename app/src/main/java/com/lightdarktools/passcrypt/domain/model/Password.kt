package com.lightdarktools.passcrypt.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Password(
    val id: Int = 0,
    val name: String,
    val username: String,
    val password: String,
    val category: String,
    val createdAt: Long
)
