package com.lightdarktools.passcrypt.domain.usecase

import com.lightdarktools.passcrypt.domain.model.Password
import com.lightdarktools.passcrypt.domain.repository.PasswordRepository
import kotlinx.coroutines.flow.Flow

class GetPasswordsUseCase(private val repository: PasswordRepository) {
    operator fun invoke(): Flow<List<Password>> = repository.getAllPasswords()
}

class AddPasswordUseCase(private val repository: PasswordRepository) {
    suspend operator fun invoke(password: Password) = repository.insertPassword(password)
}

class DeletePasswordUseCase(private val repository: PasswordRepository) {
    suspend operator fun invoke(password: Password) = repository.deletePassword(password)
}

class UpdatePasswordUseCase(private val repository: PasswordRepository) {
    suspend operator fun invoke(password: Password) = repository.updatePassword(password)
}

class DeleteAllPasswordsUseCase(private val repository: PasswordRepository) {
    suspend operator fun invoke() = repository.deleteAll()
}

class GetCategoriesUseCase(private val repository: PasswordRepository) {
    operator fun invoke(): Flow<List<String>> = repository.getAllCategories()
}

class AddPasswordsUseCase(private val repository: PasswordRepository) {
    suspend operator fun invoke(passwords: List<Password>) = repository.insertPasswords(passwords)
}

data class PasswordUseCases(
    val getPasswords: GetPasswordsUseCase,
    val addPassword: AddPasswordUseCase,
    val deletePassword: DeletePasswordUseCase,
    val updatePassword: UpdatePasswordUseCase,
    val deleteAllPasswords: DeleteAllPasswordsUseCase,
    val getCategories: GetCategoriesUseCase,
    val addPasswords: AddPasswordsUseCase
)
