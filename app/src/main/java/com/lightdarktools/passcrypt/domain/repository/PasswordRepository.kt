package com.lightdarktools.passcrypt.domain.repository

import com.lightdarktools.passcrypt.domain.model.Password
import kotlinx.coroutines.flow.Flow

interface PasswordRepository {
    fun getAllPasswords(): Flow<List<Password>>
    fun getAllCategories(): Flow<List<String>>
    suspend fun getPasswordById(id: Int): Password?
    suspend fun insertPassword(password: Password)
    suspend fun deletePassword(password: Password)
    suspend fun updatePassword(password: Password)
    suspend fun deleteAll()
    suspend fun insertPasswords(passwords: List<Password>)
}
