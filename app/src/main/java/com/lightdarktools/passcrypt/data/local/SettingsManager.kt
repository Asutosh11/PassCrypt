package com.lightdarktools.passcrypt.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsManager(private val context: Context) {
    private val introExpandedKey = booleanPreferencesKey("intro_expanded")
    private val transferIntroExpandedKey = booleanPreferencesKey("transfer_intro_expanded")
    private val hasSeenTutorialKey = booleanPreferencesKey("has_seen_tutorial")
    private val appOpenCountKey = intPreferencesKey("app_open_count")
    private val hasRatedAppKey = booleanPreferencesKey("has_rated_app")

    val isIntroExpanded: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[introExpandedKey] ?: true
        }

    val isTransferIntroExpanded: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[transferIntroExpandedKey] ?: false
        }

    val hasSeenTutorial: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[hasSeenTutorialKey] ?: false
        }

    val appOpenCount: Flow<Int> = context.dataStore.data
        .map { preferences ->
            preferences[appOpenCountKey] ?: 0
        }

    val hasRatedApp: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[hasRatedAppKey] ?: false
        }

    suspend fun setIntroExpanded(expanded: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[introExpandedKey] = expanded
        }
    }

    suspend fun setTransferIntroExpanded(expanded: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[transferIntroExpandedKey] = expanded
        }
    }

    suspend fun setHasSeenTutorial(seen: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[hasSeenTutorialKey] = seen
        }
    }

    suspend fun incrementAppOpenCount() {
        context.dataStore.edit { preferences ->
            val currentCount = preferences[appOpenCountKey] ?: 0
            preferences[appOpenCountKey] = currentCount + 1
        }
    }

    suspend fun setHasRatedApp(rated: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[hasRatedAppKey] = rated
        }
    }
}
