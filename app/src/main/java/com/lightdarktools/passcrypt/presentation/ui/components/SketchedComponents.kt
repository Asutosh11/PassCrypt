package com.lightdarktools.passcrypt.presentation.ui.components

/**
 * MaterialComponents.kt — replaces the old SketchedComponents.kt.
 *
 * All "Sketched*" widgets are gone.  This file exports:
 *   - PassCryptTextField   (OutlinedTextField wrapper)
 *   - CategoryDropdown     (ExposedDropdownMenuBox wrapper)
 *   - getLabelIcon / getCategoryIcon  (unchanged utility fns)
 *
 * Callers (AddEditPasswordDialog, ViewPasswordDialog) are updated
 * separately to use these names.
 */

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.lightdarktools.passcrypt.R
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp

// ─────────────────────────────────────────────────────────────────────────────
// Text field
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun PassCryptTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    isPassword: Boolean = false,
    passwordVisible: Boolean = false,
    readOnly: Boolean = false,
    onToggleVisibility: () -> Unit = {},
    leadingIcon: ImageVector? = null,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        readOnly = readOnly,
        label = { Text(label) },
        placeholder = if (placeholder.isNotEmpty()) ({ Text(placeholder) }) else null,
        leadingIcon = {
            Icon(
                imageVector = leadingIcon ?: getLabelIcon(label),
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
        },
        trailingIcon = trailingIcon,
        visualTransformation = if (isPassword && !passwordVisible)
            PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = if (isPassword)
            KeyboardOptions(keyboardType = KeyboardType.Password)
        else KeyboardOptions.Default,
        singleLine = true,
        shape = MaterialTheme.shapes.medium
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// Category dropdown
// ─────────────────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryDropdown(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    options: List<String>,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            label = { Text(label) },
            leadingIcon = {
                Icon(Icons.Default.Folder, contentDescription = null, modifier = Modifier.size(20.dp))
            },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            placeholder = { Text(stringResource(R.string.placeholder_select_category)) },
            shape = MaterialTheme.shapes.medium
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    leadingIcon = {
                        Icon(
                            getCategoryIcon(option),
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    text = { Text(option) },
                    onClick = { onValueChange(option); expanded = false }
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Icon helpers
// ─────────────────────────────────────────────────────────────────────────────
internal fun getLabelIcon(label: String): ImageVector {
    val l = label.lowercase()
    return when {
        l.contains("name") || l.contains("credential") || l.contains("for what") -> Icons.Default.Label
        l.contains("username") || l.contains("user") -> Icons.Default.Person
        l.contains("password") || l.contains("pass") -> Icons.Default.VpnKey
        l.contains("category") -> Icons.Default.Folder
        else -> Icons.Default.Edit
    }
}

internal fun getCategoryIcon(category: String): ImageVector = when (category.lowercase()) {
    "finance", "banking", "money" -> Icons.Default.AccountBalance
    "work", "office", "business" -> Icons.Default.BusinessCenter
    "social", "media", "chat"    -> Icons.Default.Forum
    "shopping", "store", "ecommerce" -> Icons.Default.ShoppingCart
    "gaming", "games", "play"    -> Icons.Default.Games
    "personal", "private"        -> Icons.Default.Person
    else                         -> Icons.Default.Inventory2
}
