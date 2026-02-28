package com.lightdarktools.passcrypt.data.repository

import com.lightdarktools.passcrypt.data.local.PasswordDao
import com.lightdarktools.passcrypt.data.local.PasswordEntity
import com.lightdarktools.passcrypt.domain.model.Password
import com.lightdarktools.passcrypt.domain.repository.PasswordRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PasswordRepositoryImpl(private val passwordDao: PasswordDao) : PasswordRepository {

    override fun getAllPasswords(): Flow<List<Password>> {
        return passwordDao.getAllPasswords().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getAllCategories(): Flow<List<String>> {
        return passwordDao.getAllCategories()
    }

    override suspend fun getPasswordById(id: Int): Password? {
        return passwordDao.getPasswordById(id)?.toDomain()
    }

    override suspend fun insertPassword(password: Password) {
        passwordDao.insertPassword(password.toEntity())
    }

    override suspend fun deletePassword(password: Password) {
        passwordDao.deletePassword(password.toEntity())
    }

    override suspend fun updatePassword(password: Password) {
        passwordDao.update(password.toEntity())
    }

    override suspend fun deleteAll() {
        passwordDao.deleteAll()
    }

    override suspend fun insertPasswords(passwords: List<Password>) {
        passwordDao.insertAll(passwords.map { it.toEntity() })
    }

    // Mappers
    private fun PasswordEntity.toDomain(): Password = Password(
        id = id,
        name = name,
        username = username,
        password = password,
        category = category,
        createdAt = createdAt
    )

    private fun Password.toEntity(): PasswordEntity = PasswordEntity(
        id = id,
        name = name,
        username = username,
        password = password,
        category = category,
        createdAt = createdAt
    )
}
