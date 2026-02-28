package com.lightdarktools.passcrypt.presentation.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lightdarktools.passcrypt.data.local.SettingsManager
import com.lightdarktools.passcrypt.domain.model.Password
import com.lightdarktools.passcrypt.domain.usecase.PasswordUseCases
import com.lightdarktools.passcrypt.common.CryptoUtils
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class PasswordViewModel(
    private val useCases: PasswordUseCases,
    private val settingsManager: SettingsManager
) : ViewModel() {
    
    sealed interface UiEvent {
        data class ShowToast(val message: String) : UiEvent
        data class ShowStringResourceToast(val resId: Int, val args: Array<out Any> = emptyArray()) : UiEvent
    }

    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _currentTab = MutableStateFlow(0)
    val currentTab: StateFlow<Int> = _currentTab.asStateFlow()

    fun setTab(index: Int) {
        _currentTab.value = index
    }

    fun showToast(message: String) {
        viewModelScope.launch {
            _uiEvent.emit(UiEvent.ShowToast(message))
        }
    }

    fun showStringResourceToast(resId: Int, vararg args: Any) {
        viewModelScope.launch {
            _uiEvent.emit(UiEvent.ShowStringResourceToast(resId, args))
        }
    }

    private val _isIntroExpanded = MutableStateFlow(false)
    val isIntroExpanded: StateFlow<Boolean> = _isIntroExpanded.asStateFlow()

    val isTransferIntroExpanded: StateFlow<Boolean> = settingsManager.isTransferIntroExpanded
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    val hasSeenTutorial: StateFlow<Boolean> = settingsManager.hasSeenTutorial
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true
        )

    val appOpenCount: StateFlow<Int> = settingsManager.appOpenCount
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = 0
        )

    val hasRatedApp: StateFlow<Boolean> = settingsManager.hasRatedApp
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = false
        )

    private val _showRatingDialog = MutableStateFlow(false)
    val showRatingDialog: StateFlow<Boolean> = _showRatingDialog.asStateFlow()

    val categories: StateFlow<List<String>> = useCases.getCategories()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun toggleIntroExpanded() {
        _isIntroExpanded.value = !_isIntroExpanded.value
    }

    fun toggleTransferIntroExpanded() {
        viewModelScope.launch {
            settingsManager.setTransferIntroExpanded(!isTransferIntroExpanded.value)
        }
    }

    fun markTutorialSeen() {
        viewModelScope.launch {
            settingsManager.setHasSeenTutorial(true)
        }
    }

    fun incrementAppOpenCount() {
        viewModelScope.launch {
            settingsManager.incrementAppOpenCount()
        }
    }

    fun setHasRatedApp(rated: Boolean) {
        viewModelScope.launch {
            settingsManager.setHasRatedApp(rated)
            _showRatingDialog.value = false
        }
    }

    fun dismissRatingDialog() {
        _showRatingDialog.value = false
    }

    fun onSearchQueryChange(newQuery: String) {
        _searchQuery.value = newQuery
    }

    fun addPassword(name: String, username: String, password: String, category: String) {
        viewModelScope.launch {
            val domainPassword = Password(
                name = name,
                username = username,
                password = password,
                category = category.trim().ifBlank { "Personal" },
                createdAt = System.currentTimeMillis()
            )
            useCases.addPassword(domainPassword)
            _uiEvent.emit(UiEvent.ShowToast("Credential added: $name"))
        }
    }

    fun checkAndTriggerRatingDialog() {
        if (appOpenCount.value >= 5 && !hasRatedApp.value) {
            _showRatingDialog.value = true
        }
    }

    fun updatePassword(password: Password) {
        viewModelScope.launch {
            useCases.updatePassword(password)
        }
    }
    
    fun deletePassword(password: Password) {
        viewModelScope.launch {
            useCases.deletePassword(password)
            _uiEvent.emit(UiEvent.ShowToast("Entry '${password.name}' erased")) // Keep for now as it uses dynamic data
        }
    }
    
    // --- Organization Logic ---
    enum class SortOption {
        NAME_ASC, NAME_DESC, DATE_NEWEST, DATE_OLDEST, CATEGORY
    }

    private val _sortOption = MutableStateFlow(SortOption.CATEGORY)
    val sortOption: StateFlow<SortOption> = _sortOption.asStateFlow()

    private val _categoryFilter = MutableStateFlow<String?>(null)
    val categoryFilter: StateFlow<String?> = _categoryFilter.asStateFlow()

    fun setSortOption(option: SortOption) {
        _sortOption.value = option
    }

    fun setCategoryFilter(category: String?) {
        _categoryFilter.value = category
    }

    val currentPasswords: StateFlow<List<Password>> = combine(
        useCases.getPasswords(),
        _searchQuery,
        _sortOption,
        _categoryFilter
    ) { passwords, query, sort, filter ->
        var result = passwords

        if (filter != null) {
            result = result.filter { it.category == filter }
        }

        if (query.isNotBlank()) {
            result = result.filter { 
                it.name.contains(query, ignoreCase = true) || 
                it.username.contains(query, ignoreCase = true) ||
                it.category.contains(query, ignoreCase = true)
            }
        }

        when (sort) {
            SortOption.NAME_ASC -> result.sortedBy { it.name.lowercase() }
            SortOption.NAME_DESC -> result.sortedByDescending { it.name.lowercase() }
            SortOption.DATE_NEWEST -> result.sortedByDescending { it.createdAt }
            SortOption.DATE_OLDEST -> result.sortedBy { it.createdAt }
            SortOption.CATEGORY -> result.sortedWith(compareBy({ it.category }, { it.name }))
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val groupedPasswords: StateFlow<Map<String, List<Password>>> = currentPasswords.map { list ->
        if (_sortOption.value == SortOption.CATEGORY) {
            list.groupBy { it.category }.toSortedMap()
        } else {
            if (list.isEmpty()) emptyMap() else mapOf("All" to list)
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyMap())

    // --- Export/Import Logic (Multi-QR) ---

    @Serializable
    data class QrChunk(
        val index: Int,
        val total: Int,
        val payload: String
    )

    private val _importError = MutableStateFlow<String?>(null)
    val importError: StateFlow<String?> = _importError.asStateFlow()

    private val _scannedChunks = MutableStateFlow<Map<Int, List<Password>>>(emptyMap())
    val scannedChunksCount: StateFlow<Int> = _scannedChunks.map { it.size }.stateIn(viewModelScope, SharingStarted.Lazily, 0)
    private val processedIndices = java.util.Collections.synchronizedSet(mutableSetOf<Int>())
    var isImportComplete = false
        private set
    var isConsolidating by mutableStateOf(false)
        private set
    var totalChunksExpected by mutableIntStateOf(0)
        private set

    fun clearImportState() {
        _importError.value = null
        _scannedChunks.value = emptyMap()
        totalChunksExpected = 0
        processedIndices.clear()
        isImportComplete = false
        isConsolidating = false
    }

    suspend fun prepareExportChunks(pin: String): List<String> {
        val allPasswords = useCases.getPasswords().first()
        val chunks = allPasswords.chunked(8)
        val total = chunks.size
        
        return chunks.mapIndexed { index, list ->
            // Use domain models for serialization
            val json = Json { ignoreUnknownKeys = true }
            // Note: PasswordEntity is used in older data structure, but here we can serialize Password domain model if we want
            // For compatibility, we might need PasswordEntity if the receiver expects it. 
            // Let's assume Password domain model is serializable (I'll need to add @Serializable to it)
            val encryptedPayload = CryptoUtils.encrypt(Json.encodeToString(list), pin)
            val chunk = QrChunk(index + 1, total, encryptedPayload)
            Json.encodeToString(chunk)
        }
    }

    suspend fun processScannedChunk(scannedData: String, pin: String): Boolean {
        return try {
            val chunk = Json.decodeFromString<QrChunk>(scannedData)
            totalChunksExpected = chunk.total
            
            if (processedIndices.contains(chunk.index)) return false
            
            val decryptedJson = CryptoUtils.decrypt(chunk.payload, pin)
            val json = Json { ignoreUnknownKeys = true }
            val passwords = json.decodeFromString<List<Password>>(decryptedJson)
            
            if (processedIndices.contains(chunk.index)) return false
            processedIndices.add(chunk.index)
            
            var shouldConsolidate = false
            var finalData: List<Password>? = null

            _scannedChunks.update { currentMap ->
                val newMap = currentMap.toMutableMap()
                newMap[chunk.index] = passwords
                
                if (newMap.size == totalChunksExpected) {
                    synchronized(this) {
                        if (!isImportComplete) {
                            isImportComplete = true
                            shouldConsolidate = true
                            finalData = newMap.values.flatten()
                        }
                    }
                }
                newMap
            }

            if (shouldConsolidate && finalData != null) {
                isConsolidating = true
                consolidateImport(finalData!!)
                isConsolidating = false
                return true
            }

            false
        } catch (e: Exception) {
            processedIndices.remove(try { Json.decodeFromString<QrChunk>(scannedData).index } catch(err: Exception) { -1 })
            _importError.value = "Failed to process chunk. Check PIN or signal." // Localize in UI instead, or use resource ID
            false
        }
    }

    private suspend fun consolidateImport(data: List<Password>) {
        try {
            val currentVault = useCases.getPasswords().first()
            val existingKeys = currentVault.map { it.name.lowercase() + "||" + it.username.lowercase() }.toSet()
            
            val newEntries = data.filter { 
                val key = it.name.lowercase() + "||" + it.username.lowercase()
                !existingKeys.contains(key)
            }.map { it.copy(id = 0) }
            
            if (newEntries.isNotEmpty()) {
                useCases.addPasswords(newEntries)
                _uiEvent.emit(UiEvent.ShowToast("Successfully imported ${newEntries.size} entries"))
            }
        } catch (e: Exception) {
            _importError.value = "Storage error: ${e.message}"
            isImportComplete = false 
        }
    }

    // --- PDF / CSV Export Helpers ---
    // (Translating these to use useCases and Password domain model)

    suspend fun generateBackupPdf(pin: String): ByteArray {
        val allPasswords = useCases.getPasswords().first()
        val out = java.io.ByteArrayOutputStream()
        val document = com.tom_roush.pdfbox.pdmodel.PDDocument()
        try {
            val page = com.tom_roush.pdfbox.pdmodel.PDPage(com.tom_roush.pdfbox.pdmodel.common.PDRectangle.A4)
            document.addPage(page)
            val contentStream = com.tom_roush.pdfbox.pdmodel.PDPageContentStream(document, page)
            contentStream.beginText()
            contentStream.setFont(com.tom_roush.pdfbox.pdmodel.font.PDType1Font.HELVETICA_BOLD, 18f)
            contentStream.newLineAtOffset(50f, 750f)
            contentStream.showText("PassCrypt Vault Backup")
            contentStream.endText()
            
            contentStream.beginText()
            contentStream.setFont(com.tom_roush.pdfbox.pdmodel.font.PDType1Font.HELVETICA, 12f)
            contentStream.newLineAtOffset(50f, 720f)
            
            allPasswords.forEachIndexed { index, pw ->
                contentStream.showText("${index + 1}. ${pw.name}")
                contentStream.newLineAtOffset(0f, -15f)
                contentStream.showText("   User: ${pw.username}")
                contentStream.newLineAtOffset(0f, -15f)
                contentStream.showText("   Pass: ${pw.password}")
                contentStream.newLineAtOffset(0f, -15f)
                contentStream.showText("   Cat: ${pw.category}")
                contentStream.newLineAtOffset(0f, -25f)
            }
            contentStream.endText()
            contentStream.close()
            
            val spp = com.tom_roush.pdfbox.pdmodel.encryption.StandardProtectionPolicy(pin, pin, com.tom_roush.pdfbox.pdmodel.encryption.AccessPermission())
            spp.encryptionKeyLength = 128
            document.protect(spp)
            document.save(out)
        } finally {
            document.close()
        }
        return out.toByteArray()
    }

    suspend fun importFromPdf(pdfBytes: ByteArray, pin: String): Int {
        val incoming = mutableListOf<Password>()
        try {
            val document = com.tom_roush.pdfbox.pdmodel.PDDocument.load(pdfBytes, pin)
            try {
                val stripper = com.tom_roush.pdfbox.text.PDFTextStripper()
                val text = stripper.getText(document)
                val regex = Regex("""\d+\.\s+(.+)\r?\n\s+User:\s+(.+)\r?\n\s+Pass:\s+([^\r\n]+)(?:\r?\n\s+Cat:\s+([^\r\n]+))?""")
                val matches = regex.findAll(text)
                matches.forEach { match ->
                    val name = match.groups[1]?.value?.trim() ?: ""
                    val user = match.groups[2]?.value?.trim() ?: ""
                    val pass = match.groups[3]?.value?.trim() ?: ""
                    val cat = match.groups[4]?.value?.trim() ?: "Imported"
                    if (name.isNotEmpty()) {
                        incoming.add(Password(name = name, username = user, password = pass, category = cat, createdAt = System.currentTimeMillis()))
                    }
                }
            } finally {
                document.close()
            }
        } catch (e: Exception) {
            throw Exception("Failed to decrypt or parse PDF. Check PIN.")
        }

        if (incoming.isEmpty()) return 0

        val existing = useCases.getPasswords().first()
        val keys = existing.map { it.name.lowercase() + "||" + it.username.lowercase() }.toSet()
        val newEntries = incoming.filter {
            (it.name.lowercase() + "||" + it.username.lowercase()) !in keys
        }.map { it.copy(id = 0) }

        if (newEntries.isNotEmpty()) {
            useCases.addPasswords(newEntries)
            _uiEvent.emit(UiEvent.ShowToast("Imported ${newEntries.size} entries from PDF!"))
        } else {
            _uiEvent.emit(UiEvent.ShowToast("No new entries found in PDF."))
        }
        return newEntries.size
    }

    suspend fun exportVaultAsEncryptedString(pin: String): String {
        val allPasswords = useCases.getPasswords().first()
        val jsonStr = Json.encodeToString(allPasswords)
        return CryptoUtils.encrypt(jsonStr, pin)
    }

    suspend fun importFromEncryptedString(encryptedData: String, pin: String): Int {
        val decryptedJson = CryptoUtils.decrypt(encryptedData, pin)
        val json = Json { ignoreUnknownKeys = true }
        val incoming = json.decodeFromString<List<Password>>(decryptedJson)

        val existing = useCases.getPasswords().first()
        val keys = existing.map { it.name.lowercase() + "||" + it.username.lowercase() }.toSet()
        val newEntries = incoming.filter {
            (it.name.lowercase() + "||" + it.username.lowercase()) !in keys
        }.map { it.copy(id = 0) }

        if (newEntries.isNotEmpty()) {
            useCases.addPasswords(newEntries)
            _uiEvent.emit(UiEvent.ShowToast("Imported ${newEntries.size} new entries!"))
        } else {
            _uiEvent.emit(UiEvent.ShowToast("No new entries to import."))
        }
        return newEntries.size
    }

    suspend fun generateBackupCsv(): String {
        val allPasswords = useCases.getPasswords().first()
        val csv = StringBuilder()
        csv.append("Title,URL,Username,Password,Notes,OTPAuth\n")
        allPasswords.forEach { pw ->
            val row = listOf(pw.name, "", pw.username, pw.password, "Category: ${pw.category}", "")
                .joinToString(",") { "\"${it.replace("\"", "\"\"")}\"" }
            csv.append("$row\n")
        }
        return csv.toString()
    }
}
