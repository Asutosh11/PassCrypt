package com.lightdarktools.passcrypt.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(tableName = "passwords")
@Serializable
data class PasswordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val username: String,
    val password: String,
    val category: String = "Personal",
    val createdAt: Long = System.currentTimeMillis()
)
