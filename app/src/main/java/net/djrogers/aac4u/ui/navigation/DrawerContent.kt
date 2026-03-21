package net.djrogers.aac4u.ui.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DrawerContent(
    currentRoute: String?,
    isEditMode: Boolean,
    onNavigate: (Screen) -> Unit,
    onToggleEditMode: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    ModalDrawerSheet(
        modifier = modifier.width(280.dp),
        drawerContainerColor = MaterialTheme.colorScheme.surface
    ) {
        // ── App Header ──
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 24.dp)
        ) {
            Column {
                Text(
                    text = "AAC4U",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Communication made easy",
                    fontSize = 14.sp,
                    color = Color(0xFF9E9E9E)
                )
            }
        }

        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

        Spacer(modifier = Modifier.height(8.dp))

        // ── Edit Mode Toggle ──
        DrawerMenuItem(
            label = if (isEditMode) "Exit Edit Mode" else "Edit Mode",
            emoji = if (isEditMode) "✅" else "✏️",
            isSelected = isEditMode,
            onClick = {
                onToggleEditMode()
                onClose()
            }
        )

        // ── Core Words Editor ──
        DrawerMenuItem(
            label = "Core Words",
            emoji = "⭐",
            isSelected = currentRoute == Screen.CoreWordsEditor.route,
            onClick = {
                onNavigate(Screen.CoreWordsEditor)
                onClose()
            }
        )

        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        // ── Navigation Items ──

        DrawerMenuItem(
            label = "Quick Phrases",
            emoji = "\uD83D\uDCAC",
            isSelected = currentRoute == Screen.QuickPhrases.route,
            onClick = {
                onNavigate(Screen.QuickPhrases)
                onClose()
            }
        )

        DrawerMenuItem(
            label = "Speech History",
            emoji = "\uD83D\uDCDC",
            isSelected = currentRoute == Screen.History.route,
            onClick = {
                onNavigate(Screen.History)
                onClose()
            }
        )

        DrawerMenuItem(
            label = "Profiles",
            emoji = "\uD83D\uDC64",
            isSelected = currentRoute == Screen.Profiles.route,
            onClick = {
                onNavigate(Screen.Profiles)
                onClose()
            }
        )

        DrawerMenuItem(
            label = "Settings",
            emoji = "⚙\uFE0F",
            isSelected = currentRoute == Screen.Settings.route,
            onClick = {
                onNavigate(Screen.Settings)
                onClose()
            }
        )

        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        DrawerMenuItem(
            label = "About",
            emoji = "ℹ\uFE0F",
            isSelected = currentRoute == Screen.About.route,
            onClick = {
                onNavigate(Screen.About)
                onClose()
            }
        )

        Spacer(modifier = Modifier.weight(1f))

        DrawerMenuItem(
            label = "Communication Grid",
            emoji = "🏠",
            isSelected = currentRoute == Screen.Grid.route,
            onClick = {
                onNavigate(Screen.Grid)
                onClose()
            }
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun DrawerMenuItem(
    label: String,
    emoji: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    NavigationDrawerItem(
        label = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(text = emoji, fontSize = 20.sp)
                Text(
                    text = label,
                    fontSize = 16.sp,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
            }
        },
        selected = isSelected,
        onClick = onClick,
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp),
        colors = NavigationDrawerItemDefaults.colors(
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
            unselectedContainerColor = Color.Transparent
        )
    )
}
