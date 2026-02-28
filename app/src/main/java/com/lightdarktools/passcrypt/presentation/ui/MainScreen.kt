package com.lightdarktools.passcrypt.presentation.ui

import android.annotation.SuppressLint
import com.lightdarktools.passcrypt.R
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.LaunchedEffect
import com.lightdarktools.passcrypt.presentation.viewmodel.PasswordViewModel
import com.lightdarktools.passcrypt.presentation.ui.components.*
import com.lightdarktools.passcrypt.domain.model.Password
import com.lightdarktools.passcrypt.common.PasswordGenerator
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass

@SuppressLint("ContextInComposition")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun MainScreen(
    viewModel: PasswordViewModel,
    windowWidthSizeClass: WindowWidthSizeClass = WindowWidthSizeClass.Compact
) {
    val groupedPasswords  by viewModel.groupedPasswords.collectAsState()
    val searchQuery       by viewModel.searchQuery.collectAsState()
    val isIntroExpanded   by viewModel.isIntroExpanded.collectAsState()
    val sortOption        by viewModel.sortOption.collectAsState()
    val categoryFilter    by viewModel.categoryFilter.collectAsState()
    val categories        by viewModel.categories.collectAsState()

    val context = LocalContext.current
    val playStoreUrl = stringResource(R.string.play_store_url)

    var showAddDialog      by remember { mutableStateOf(false) }
    var showSecurityDialog by remember { mutableStateOf(false) }
    var showSortMenu       by remember { mutableStateOf(false) }
    var selectedPassword   by remember { mutableStateOf<Password?>(null) }

    Scaffold(
        topBar = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // ── App bar ────────────────────────────────────────────
                TopAppBar(
                    title = {
                        Text(stringResource(R.string.app_name), fontWeight = FontWeight.Bold)
                    },
                    actions = {
                        IconButton(onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(playStoreUrl))
                            context.startActivity(intent)
                        }) {
                            Icon(
                                Icons.Default.Star,
                                contentDescription = stringResource(R.string.action_rate_app),
                                tint = if (isSystemInDarkTheme()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onPrimary
                            )
                        }

                        IconButton(onClick = { showSecurityDialog = true }) {
                            Icon(
                                Icons.Default.Security,
                                contentDescription = stringResource(R.string.action_security_info),
                                tint = if (isSystemInDarkTheme()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = if (isSystemInDarkTheme()) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.primary,
                        titleContentColor = if (isSystemInDarkTheme()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onPrimary,
                        navigationIconContentColor = if (isSystemInDarkTheme()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onPrimary,
                        actionIconContentColor = if (isSystemInDarkTheme()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onPrimary
                    )
                )

                // ── Search bar ─────────────────────────────────────────
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.onSearchQueryChange(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 4.dp),
                    placeholder = { Text(stringResource(R.string.search_placeholder)) },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                                Icon(Icons.Default.Clear, contentDescription = stringResource(R.string.action_clear_search))
                            }
                        }
                    },
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium
                )

                // ── Sort + Category filter row ─────────────────────────
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.padding(start = 8.dp)) {
                        IconButton(onClick = { showSortMenu = true }) {
                            Icon(
                                Icons.Default.Sort,
                                contentDescription = stringResource(R.string.action_sort),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false }
                        ) {
                            PasswordViewModel.SortOption.entries.forEach { option ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            option.name.replace("_", " ")
                                                .lowercase()
                                                .replaceFirstChar { it.uppercase() }
                                        )
                                    },
                                    leadingIcon = if (option == sortOption) ({
                                        Icon(
                                            Icons.Default.Lock,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }) else null,
                                    onClick = {
                                        viewModel.setSortOption(option)
                                        showSortMenu = false
                                    }
                                )
                            }
                        }
                    }

                    VerticalDivider(
                        modifier = Modifier
                            .height(24.dp)
                            .padding(horizontal = 4.dp),
                        color = MaterialTheme.colorScheme.outlineVariant
                    )

                    LazyRow(
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(start = 4.dp, end = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            FilterChip(
                                selected = categoryFilter == null,
                                onClick = { viewModel.setCategoryFilter(null) },
                                label = { Text(stringResource(R.string.filter_all)) }
                            )
                        }
                        items(categories) { category ->
                            FilterChip(
                                selected = categoryFilter == category,
                                onClick = {
                                    viewModel.setCategoryFilter(
                                        if (categoryFilter == category) null else category
                                    )
                                },
                                label = { Text(category) }
                            )
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            }
        },
        floatingActionButton = {
            val showHint = groupedPasswords.isEmpty() && searchQuery.isEmpty()
            val infiniteTransition = rememberInfiniteTransition(label = "fab_hint_pulse")
            val hintAlpha by infiniteTransition.animateFloat(
                initialValue = 0.6f, targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(900, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "hint_alpha"
            )
            val primaryColor = MaterialTheme.colorScheme.primary

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (showHint) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = primaryColor.copy(alpha = hintAlpha),
                        modifier = Modifier
                            .drawBehind {
                                // Draw a small downward-pointing triangle caret
                                val caretSize = 8.dp.toPx()
                                val path = Path().apply {
                                    moveTo(size.width / 2f - caretSize, size.height)
                                    lineTo(size.width / 2f + caretSize, size.height)
                                    lineTo(size.width / 2f, size.height + caretSize)
                                    close()
                                }
                                drawPath(path, primaryColor.copy(alpha = hintAlpha))
                            }
                    ) {
                        Text(
                            text = stringResource(R.string.fab_hint_first_password),
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }
                ExtendedFloatingActionButton(
                    text = { Text(stringResource(R.string.btn_add)) },
                    icon = { Icon(Icons.Default.Add, contentDescription = stringResource(R.string.desc_add_credential)) },
                    onClick = { showAddDialog = true }
                )
            }
        }
    ) { paddingValues ->

        val contentModifier = if (windowWidthSizeClass == WindowWidthSizeClass.Medium) {
            Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 48.dp)
        } else {
            Modifier.fillMaxSize().padding(paddingValues)
        }

        Surface(
            modifier = contentModifier,
            color = MaterialTheme.colorScheme.background
        ) {
            if (windowWidthSizeClass == WindowWidthSizeClass.Expanded) {
                // Expanded mode: Master-Detail
                Row(modifier = Modifier.fillMaxSize()) {
                    Box(modifier = Modifier.weight(1f)) {
                        PasswordListPanel(
                            groupedPasswords = groupedPasswords,
                            searchQuery = searchQuery,
                            isIntroExpanded = isIntroExpanded,
                            onPasswordSelect = { selectedPassword = it },
                            viewModel = viewModel
                        )
                    }
                    VerticalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Box(modifier = Modifier.weight(1.2f).fillMaxHeight()) {
                        if (selectedPassword != null) {
                            PasswordDetailPanel(
                                password = selectedPassword!!,
                                onEdit = { showAddDialog = true },
                                onDelete = {
                                    viewModel.deletePassword(selectedPassword!!)
                                    selectedPassword = null
                                }
                            )
                        } else {
                            EmptyDetailState()
                        }
                    }
                }
            } else {
                PasswordListPanel(
                    groupedPasswords = groupedPasswords,
                    searchQuery = searchQuery,
                    isIntroExpanded = isIntroExpanded,
                    onPasswordSelect = { selectedPassword = it },
                    viewModel = viewModel
                )
            }
        }
    }

    // Dialogs
    if (showSecurityDialog) {
        SecurityInfoDialog(onDismiss = { showSecurityDialog = false })
    }

    if (showAddDialog) {
        val passwordToEdit = selectedPassword
        AddEditPasswordDialog(
            passwordToEdit = passwordToEdit,
            existingCategories = categories,
            onDismiss = { showAddDialog = false },
            onSave = { name, username, password, category ->
                if (passwordToEdit != null) {
                    viewModel.updatePassword(
                        passwordToEdit.copy(
                            name = name,
                            username = username,
                            password = password,
                            category = category
                        )
                    )
                    selectedPassword = null
                } else {
                    viewModel.addPassword(name, username, password, category)
                }
                showAddDialog = false
            }
        )
    }

    if (selectedPassword != null && !showAddDialog &&
        windowWidthSizeClass != WindowWidthSizeClass.Expanded
    ) {
        selectedPassword?.let { pw ->
            ViewPasswordDialog(
                password = pw,
                onDismiss = { selectedPassword = null },
                onDelete = { viewModel.deletePassword(pw); selectedPassword = null },
                onEdit = { showAddDialog = true }
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// List Panel
// ─────────────────────────────────────────────────────────────────────────────
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PasswordListPanel(
    groupedPasswords: Map<String, List<Password>>,
    searchQuery: String,
    isIntroExpanded: Boolean,
    onPasswordSelect: (Password) -> Unit,
    viewModel: PasswordViewModel
) {
    val primary = MaterialTheme.colorScheme.primary

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 88.dp)
    ) {
        if (groupedPasswords.isEmpty()) {
            item {
                EmptyState(
                    icon = if (searchQuery.isNotEmpty()) Icons.Default.Search else Icons.Default.Lock,
                    title = if (searchQuery.isNotEmpty()) stringResource(R.string.no_matches_title) else stringResource(R.string.empty_vault_title),
                    description = if (searchQuery.isNotEmpty())
                        stringResource(R.string.no_matches_desc)
                    else
                        stringResource(R.string.empty_vault_desc)
                )
            }
        } else {
            groupedPasswords.forEach { (category, categoryPasswords) ->
                stickyHeader {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.95f)
                    ) {
                        Text(
                            text = category,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = primary
                        )
                    }
                }

                items(categoryPasswords) { password ->
                    PasswordListItem(
                        password = password,
                        onClick = { onPasswordSelect(password) }
                    )
                }
            }
        }

        if (searchQuery.isEmpty()) {
            item {
                IntroSection(
                    isExpanded = isIntroExpanded,
                    onToggle = { viewModel.toggleIntroExpanded() }
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Detail Panel (Expanded layout)
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun PasswordDetailPanel(
    password: Password,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val primary = MaterialTheme.colorScheme.primary
    val errorColor = MaterialTheme.colorScheme.error

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = password.name,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = primary
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        DetailItem(label = stringResource(R.string.detail_username), value = password.username)
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        DetailItem(label = stringResource(R.string.detail_password), value = password.password, isPassword = true)
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        DetailItem(label = stringResource(R.string.detail_category), value = password.category)

        Spacer(modifier = Modifier.weight(1f))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onDelete,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = errorColor),
                shape = MaterialTheme.shapes.medium
            ) { Text(stringResource(R.string.btn_delete)) }
            Button(
                onClick = onEdit,
                modifier = Modifier.weight(1.5f),
                shape = MaterialTheme.shapes.medium
            ) { Text(stringResource(R.string.btn_edit)) }
        }
    }
}

@Composable
private fun DetailItem(label: String, value: String, isPassword: Boolean = false) {
    var revealPassword by remember { mutableStateOf(!isPassword) }

    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = if (isPassword && !revealPassword) "••••••••" else value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = isPassword) { revealPassword = !revealPassword }
        )
    }
}

@Composable
private fun EmptyDetailState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                Icons.Default.Lock,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
            )
            Text(
                text = stringResource(R.string.detail_empty_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
            )
            Text(
                text = stringResource(R.string.detail_empty_desc),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
            )
        }
    }
}
