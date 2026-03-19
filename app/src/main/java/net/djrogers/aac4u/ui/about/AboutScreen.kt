package net.djrogers.aac4u.ui.about

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("About AAC4U") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "AAC4U",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Augmentative and Alternative Communication",
                fontSize = 16.sp,
                color = Color(0xFF757575),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Version 0.1.0",
                fontSize = 14.sp,
                color = Color(0xFF9E9E9E)
            )

            Spacer(modifier = Modifier.height(32.dp))

            HorizontalDivider()

            Spacer(modifier = Modifier.height(24.dp))

            AboutSection(
                title = "What is AAC4U?",
                body = "AAC4U is a free, open-source communication app designed to help " +
                        "people who have difficulty with spoken language. It uses symbol-based " +
                        "grids, core vocabulary, and text-to-speech to support communication."
            )

            Spacer(modifier = Modifier.height(20.dp))

            AboutSection(
                title = "Symbol Credits",
                body = "AAC4U uses symbols from the ARASAAC symbol set, created by the " +
                        "Gobierno de Aragón and distributed under the Creative Commons " +
                        "BY-NC-SA licence."
            )

            Spacer(modifier = Modifier.height(20.dp))

            AboutSection(
                title = "Open Source",
                body = "AAC4U is open-source software. Contributions, feedback, and " +
                        "suggestions are welcome. Visit our GitHub repository for more information."
            )

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = "Made with ❤ for the AAC community",
                fontSize = 14.sp,
                color = Color(0xFFBDBDBD),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun AboutSection(
    title: String,
    body: String
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = body,
            fontSize = 15.sp,
            color = Color(0xFF616161),
            lineHeight = 22.sp
        )
    }
}
