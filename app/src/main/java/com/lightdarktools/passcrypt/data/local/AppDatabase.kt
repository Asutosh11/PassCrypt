package com.lightdarktools.passcrypt.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory
import java.nio.charset.StandardCharsets

@Database(entities = [PasswordEntity::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun passwordDao(): PasswordDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        // Legacy key for migration purposes only. DO NOT USE FOR NEW INSTALLS.
        private const val LEGACY_DB_PASSPHRASE = "YourSecurePassphrase123!@#"
        
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                System.loadLibrary("sqlcipher")
                
                val passphrase = getOrCreatePassphrase(context.applicationContext)
                val factory = SupportOpenHelperFactory(passphrase)
                
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "passcrypt_database"
                )
                    .openHelperFactory(factory)
                    .fallbackToDestructiveMigration()
                    .build()
                
                INSTANCE = instance
                instance
            }
        }

        private fun getOrCreatePassphrase(context: Context): ByteArray {
            val masterKey = androidx.security.crypto.MasterKey.Builder(context)
                .setKeyScheme(androidx.security.crypto.MasterKey.KeyScheme.AES256_GCM)
                .build()

            val sharedPreferences = androidx.security.crypto.EncryptedSharedPreferences.create(
                context,
                "secure_vault_prefs",
                masterKey,
                androidx.security.crypto.EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                androidx.security.crypto.EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )

            var passphraseStr = sharedPreferences.getString("db_passphrase", null)
            
            // Check if we need to migrate from legacy key
            val dbFile = context.getDatabasePath("passcrypt_database")
            if (dbFile.exists() && passphraseStr == null) {
                // Database exists but no secure key found. 
                // In a production app, we would perform a PRAGMA rekey here.
                // For now, we fallback to legacy to avoid data loss, but flag for future migration.
                return LEGACY_DB_PASSPHRASE.toByteArray(StandardCharsets.UTF_8)
            }

            if (passphraseStr == null) {
                val random = java.security.SecureRandom()
                val bytes = ByteArray(32)
                random.nextBytes(bytes)
                passphraseStr = android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP)
                sharedPreferences.edit().putString("db_passphrase", passphraseStr).apply()
            }
            
            return passphraseStr.toByteArray(StandardCharsets.UTF_8)
        }
    }
}
