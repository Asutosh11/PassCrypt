package com.lightdarktools.passcrypt.presentation.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCard
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lightdarktools.passcrypt.R
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.platform.LocalContext
import com.lightdarktools.passcrypt.presentation.ui.util.copyToClipboard
import com.lightdarktools.passcrypt.domain.model.Password
import com.lightdarktools.passcrypt.common.PasswordGenerator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditPasswordDialog(
    passwordToEdit: Password? = null,
    existingCategories: List<String>,
    onDismiss: () -> Unit,
    onSave: (name: String, username: String, password: String, category: String) -> Unit
) {
    var name     by remember { mutableStateOf(passwordToEdit?.name ?: "") }
    var username by remember {
        mutableStateOf(
            if (passwordToEdit?.username == "there is no user name for this") ""
            else passwordToEdit?.username ?: ""
        )
    }
    var password        by remember { mutableStateOf(passwordToEdit?.password ?: "") }
    var category        by remember { mutableStateOf(passwordToEdit?.category ?: "") }
    var passwordVisible by remember { mutableStateOf(false) }

    val isEditMode = passwordToEdit != null
    val primary = MaterialTheme.colorScheme.primary
    val context = LocalContext.current

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 24.dp),
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Header
                Row(
                    modifier = Modifier.padding(start = 24.dp, end = 16.dp, top = 20.dp, bottom = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        if (isEditMode) Icons.Default.Edit else Icons.Default.AddCard,
                        contentDescription = null,
                        tint = primary
                    )
                    Text(
                        text = if (isEditMode) stringResource(R.string.title_edit_credential) else stringResource(R.string.title_new_credential),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = primary,
                        modifier = Modifier.weight(1f)
                    )
                }
                HorizontalDivider(modifier = Modifier.padding(horizontal = 24.dp))

                // Body
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Name field
                    PassCryptTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = stringResource(R.string.label_service_name),
                        placeholder = stringResource(R.string.placeholder_service_name)
                    )

                    // Username field
                    PassCryptTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = stringResource(R.string.label_username),
                        placeholder = stringResource(R.string.label_optional)
                    )

                    // Password field
                    PassCryptTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = stringResource(R.string.label_password),
                        placeholder = stringResource(R.string.placeholder_password),
                        isPassword = true,
                        passwordVisible = passwordVisible,
                        trailingIcon = {
                            Row {
                                IconButton(onClick = {
                                    password = PasswordGenerator.generatePassword()
                                    passwordVisible = true
                                }) {
                                    Icon(
                                        Icons.Default.AutoFixHigh,
                                        contentDescription = stringResource(R.string.desc_generate_password),
                                        tint = MaterialTheme.colorScheme.tertiary
                                    )
                                }
                                if (password.isNotBlank()) {
                                    IconButton(onClick = { copyToClipboard(context, context.getString(R.string.clipboard_label_password), password) }) {
                                        Icon(
                                            Icons.Default.ContentCopy,
                                            contentDescription = stringResource(R.string.desc_copy_password),
                                            tint = MaterialTheme.colorScheme.tertiary
                                        )
                                    }
                                }
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        if (passwordVisible) Icons.Default.Visibility
                                        else Icons.Default.VisibilityOff,
                                        contentDescription = stringResource(R.string.desc_toggle_visibility),
                                        tint = MaterialTheme.colorScheme.tertiary
                                    )
                                }
                            }
                        }
                    )

                    // Category selection
                    if (existingCategories.isNotEmpty()) {
                        CategoryDropdown(
                            value = if (existingCategories.contains(category)) category else "",
                            onValueChange = { category = it },
                            label = stringResource(R.string.label_select_category),
                            options = existingCategories
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            HorizontalDivider(modifier = Modifier.weight(1f))
                            Text(
                                stringResource(R.string.label_or),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            HorizontalDivider(modifier = Modifier.weight(1f))
                        }
                    }

                    PassCryptTextField(
                        value = if (existingCategories.contains(category)) "" else category,
                        onValueChange = { category = it },
                        label = stringResource(R.string.label_new_category),
                        placeholder = stringResource(R.string.placeholder_category)
                    )
                }

                // Footer
                HorizontalDivider(modifier = Modifier.padding(horizontal = 24.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) { Text(stringResource(R.string.btn_cancel)) }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (name.isNotBlank() && password.isNotBlank()) {
                                val finalUsername = username.trim().ifBlank { context.getString(R.string.placeholder_no_username) }
                                val finalCategory = category.trim().ifBlank { context.getString(R.string.default_category) }
                                onSave(name.trim(), finalUsername, password, finalCategory)
                                onDismiss()
                            }
                        },
                        enabled = name.isNotBlank() && password.isNotBlank(),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text(stringResource(R.string.btn_save))
                    }
                }
            }
        }
    }
}
