package com.lightdarktools.passcrypt.presentation.ui.components

import com.lightdarktools.passcrypt.presentation.ui.theme.*
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.EnhancedEncryption
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.ImportExport
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.lightdarktools.passcrypt.R
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun IntroSection(
    isExpanded: Boolean,
    onToggle: () -> Unit
) {
    val primary = MaterialTheme.colorScheme.primary

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable(onClick = onToggle),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.intro_title),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = primary,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) stringResource(R.string.intro_collapse) else stringResource(R.string.intro_expand),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier.padding(top = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    IntroItem(
                        icon = Icons.Default.Lock,
                        title = stringResource(R.string.intro_storage_title),
                        shortDesc = stringResource(R.string.intro_storage_short),
                        longDesc = stringResource(R.string.intro_storage_long)
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                    IntroItem(
                        icon = Icons.Default.WifiOff,
                        title = stringResource(R.string.intro_network_title),
                        shortDesc = stringResource(R.string.intro_network_short),
                        longDesc = stringResource(R.string.intro_network_long)
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                    IntroItem(
                        icon = Icons.Default.Code,
                        title = stringResource(R.string.intro_source_title),
                        shortDesc = stringResource(R.string.intro_source_short),
                        longDesc = stringResource(R.string.intro_source_long)
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                    IntroItem(
                        icon = Icons.Default.EnhancedEncryption,
                        title = stringResource(R.string.intro_crypto_title),
                        shortDesc = stringResource(R.string.intro_crypto_short),
                        longDesc = stringResource(R.string.intro_crypto_long)
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                    IntroItem(
                        icon = Icons.Default.ImportExport,
                        title = stringResource(R.string.intro_transfer_header),
                        shortDesc = stringResource(R.string.intro_transfer_short),
                        longDesc = stringResource(R.string.intro_transfer_long)
                    )
                }
            }
        }
    }
}

@Composable
private fun IntroItem(
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
            .clickable { expanded = !expanded },
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.tertiary,
            modifier = Modifier
                .size(24.dp)
                .padding(top = 2.dp)
        )
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                if (expanded) longDesc else shortDesc,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                if (expanded) stringResource(R.string.label_show_less) else stringResource(R.string.label_read_more),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                color = primary
            )
        }
    }
}
