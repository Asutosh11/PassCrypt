package com.lightdarktools.passcrypt.presentation.ui.components

import com.lightdarktools.passcrypt.presentation.ui.theme.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EnhancedEncryption
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.lightdarktools.passcrypt.R
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.lightdarktools.passcrypt.data.LegalContent

@Composable
fun SecurityInfoDialog(onDismiss: () -> Unit) {
    var showPrivacy by remember { mutableStateOf(false) }
    var showToS     by remember { mutableStateOf(false) }
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
                    Icon(Icons.Default.Security, contentDescription = null, tint = primary)
                    Text(
                        text = stringResource(R.string.title_security_privacy),
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
                    SecurityFeatureItem(
                        icon = Icons.Default.FlashOn,
                        title = stringResource(R.string.feature_offline_title),
                        shortDesc = stringResource(R.string.feature_offline_short),
                        longDesc = stringResource(R.string.intro_network_long)
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    SecurityFeatureItem(
                        icon = Icons.Default.EnhancedEncryption,
                        title = stringResource(R.string.feature_aes_title),
                        shortDesc = stringResource(R.string.feature_aes_short),
                        longDesc = stringResource(R.string.intro_crypto_long)
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    SecurityFeatureItem(
                        icon = Icons.Default.Security,
                        title = stringResource(R.string.feature_verified_title),
                        shortDesc = stringResource(R.string.feature_verified_short),
                        longDesc = stringResource(R.string.intro_network_long)
                    )

                    Spacer(Modifier.height(4.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(
                            onClick = { showPrivacy = true },
                            modifier = Modifier.weight(1f),
                            shape = MaterialTheme.shapes.medium
                        ) { Text(stringResource(R.string.btn_privacy)) }
                        OutlinedButton(
                            onClick = { showToS = true },
                            modifier = Modifier.weight(1f),
                            shape = MaterialTheme.shapes.medium
                        ) { Text(stringResource(R.string.btn_terms)) }
                    }
                }

                // Footer
                HorizontalDivider(modifier = Modifier.padding(horizontal = 24.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(onClick = onDismiss) { Text(stringResource(R.string.btn_got_it)) }
                }
            }
        }
    }

    if (showPrivacy) {
        LegalDocumentDialog(
            title = stringResource(R.string.title_privacy_policy),
            content = LegalContent.PRIVACY_POLICY,
            onDismiss = { showPrivacy = false }
        )
    }
    if (showToS) {
        LegalDocumentDialog(
            title = stringResource(R.string.title_terms_service),
            content = LegalContent.TERMS_OF_SERVICE,
            onDismiss = { showToS = false }
        )
    }
}

@Composable
private fun SecurityFeatureItem(
    icon: ImageVector,
    title: String,
    shortDesc: String,
    longDesc: String
) {
    var expanded by remember { mutableStateOf(false) }
    val primary = MaterialTheme.colorScheme.primary

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.tertiary,
            modifier = Modifier.size(24.dp).padding(top = 2.dp)
        )
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Text(
                if (expanded) longDesc else shortDesc,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                if (expanded) stringResource(R.string.btn_show_less) else stringResource(R.string.btn_read_more),
                style = MaterialTheme.typography.labelSmall,
                color = primary,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
