package com.lightdarktools.passcrypt.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface PasswordDao {
    @Query("SELECT * FROM passwords ORDER BY createdAt DESC")
    fun getAllPasswords(): Flow<List<PasswordEntity>>
    
    @Query("SELECT * FROM passwords WHERE id = :id")
    suspend fun getPasswordById(id: Int): PasswordEntity?

    @Query("SELECT * FROM passwords WHERE name = :name AND username = :username LIMIT 1")
    suspend fun getPasswordByMatch(name: String, username: String): PasswordEntity?
    
    @Query("SELECT DISTINCT category FROM passwords ORDER BY category ASC")
    fun getAllCategories(): Flow<List<String>>
    
    @Insert(onConflict = androidx.room.OnConflictStrategy.IGNORE)
    suspend fun insertAll(passwords: List<PasswordEntity>)

    @Insert
    suspend fun insertPassword(password: PasswordEntity)
    
    @Update
    suspend fun update(password: PasswordEntity)

    @Delete
    suspend fun deletePassword(password: PasswordEntity)

    @Query("DELETE FROM passwords")
    suspend fun deleteAll()
}
