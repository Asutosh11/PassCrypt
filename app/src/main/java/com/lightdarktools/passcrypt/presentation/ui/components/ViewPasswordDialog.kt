package com.lightdarktools.passcrypt.presentation.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.lightdarktools.passcrypt.R
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

import com.lightdarktools.passcrypt.domain.model.Password
import com.lightdarktools.passcrypt.presentation.ui.util.copyToClipboard

@Composable
fun ViewPasswordDialog(
    password: Password,
    onDismiss: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    var passwordVisible       by remember { mutableStateOf(false) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val primary = MaterialTheme.colorScheme.primary

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
                    Icon(Icons.Default.Lock, contentDescription = null, tint = primary)
                    Text(
                        text = password.name,
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
                    // Category chip
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Folder,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.tertiary
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            password.category,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    PassCryptTextField(
                        value = password.username,
                        onValueChange = {},
                        label = stringResource(R.string.detail_username),
                        readOnly = true,
                        trailingIcon = {
                            IconButton(onClick = { copyToClipboard(context, context.getString(R.string.clipboard_label_username), password.username) }) {
                                Icon(Icons.Default.ContentCopy, contentDescription = stringResource(R.string.desc_copy_username),
                                    tint = MaterialTheme.colorScheme.tertiary)
                            }
                        }
                    )

                    // Password
                    PassCryptTextField(
                        value = password.password,
                        onValueChange = {},
                        label = stringResource(R.string.detail_password),
                        readOnly = true,
                        isPassword = true,
                        passwordVisible = passwordVisible,
                        trailingIcon = {
                            Row {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        if (passwordVisible) Icons.Default.Visibility
                                        else Icons.Default.VisibilityOff,
                                        contentDescription = stringResource(R.string.desc_toggle_visibility),
                                        tint = MaterialTheme.colorScheme.tertiary
                                    )
                                }
                                IconButton(onClick = { copyToClipboard(context, context.getString(R.string.clipboard_label_password), password.password) }) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = stringResource(R.string.desc_copy_password),
                                        tint = MaterialTheme.colorScheme.tertiary)
                                }
                            }
                        }
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
                    TextButton(
                        onClick = { showDeleteConfirmation = true },
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) { Text(stringResource(R.string.btn_delete)) }

                    Spacer(Modifier.weight(1f))

                    TextButton(onClick = onDismiss) { Text(stringResource(R.string.btn_close)) }
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = onEdit, shape = MaterialTheme.shapes.medium) { Text(stringResource(R.string.btn_edit)) }
                }
            }
        }
    }

    // Delete confirmation dialog
    if (showDeleteConfirmation) {
        Dialog(
            onDismissRequest = { showDeleteConfirmation = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp, vertical = 24.dp),
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                Column {
                    // Header
                    Row(
                        modifier = Modifier.padding(start = 24.dp, end = 16.dp, top = 20.dp, bottom = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                        Text(
                            text = stringResource(R.string.title_delete_entry),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 24.dp))

                    // Body
                    Text(
                        text = stringResource(R.string.msg_delete_confirmation, password.name),
                        modifier = Modifier.padding(24.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Footer
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 24.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = { showDeleteConfirmation = false }) { Text(stringResource(R.string.btn_keep)) }
                        Spacer(Modifier.width(8.dp))
                        Button(
                            onClick = { showDeleteConfirmation = false; onDelete(); onDismiss() },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                            shape = MaterialTheme.shapes.medium
                        ) { Text(stringResource(R.string.btn_delete)) }
                    }
                }
            }
        }
    }
}
