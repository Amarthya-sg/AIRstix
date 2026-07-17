package io.github.amarthyasg.airstix.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.amarthyasg.airstix.R
import io.github.amarthyasg.airstix.data.MinimalistPalette

@Composable
private fun VirtualGamePadMobileThemePreview(
    palette: MinimalistPalette = MinimalistPalette.CRIMSON_ARCADE
) {
    VirtualGamePadMobileTheme(
        minimalistPalette = palette
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(R.string.theme_preview_title),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold
            )

            // Primary Colors Section
            ColorSection(
                title = stringResource(R.string.theme_preview_primary),
                colors = listOf(
                    ColorInfo("Primary (Accent)", MaterialTheme.colorScheme.primary),
                    ColorInfo("On Primary", MaterialTheme.colorScheme.onPrimary),
                    ColorInfo("Primary Container", MaterialTheme.colorScheme.primaryContainer),
                    ColorInfo("On Primary Container", MaterialTheme.colorScheme.onPrimaryContainer)
                )
            )

            // Secondary Colors Section
            ColorSection(
                title = stringResource(R.string.theme_preview_secondary),
                colors = listOf(
                    ColorInfo("Secondary (Muted)", MaterialTheme.colorScheme.secondary),
                    ColorInfo("On Secondary", MaterialTheme.colorScheme.onSecondary),
                    ColorInfo("Secondary Container", MaterialTheme.colorScheme.secondaryContainer),
                    ColorInfo("On Secondary Container", MaterialTheme.colorScheme.onSecondaryContainer)
                )
            )

            // Background & Surface Colors Section
            ColorSection(
                title = stringResource(R.string.theme_preview_background),
                colors = listOf(
                    ColorInfo("Background", MaterialTheme.colorScheme.background),
                    ColorInfo("On Background (Text)", MaterialTheme.colorScheme.onBackground),
                    ColorInfo("Surface", MaterialTheme.colorScheme.surface),
                    ColorInfo("On Surface (Text)", MaterialTheme.colorScheme.onSurface)
                )
            )

            // Other Colors Section
            ColorSection(
                title = stringResource(R.string.theme_preview_other),
                colors = listOf(
                    ColorInfo("Outline (Muted)", MaterialTheme.colorScheme.outline),
                    ColorInfo("Outline Variant", MaterialTheme.colorScheme.outlineVariant),
                    ColorInfo("Scrim", MaterialTheme.colorScheme.scrim)
                )
            )
        }
    }
}

@Composable
private fun ColorSection(
    title: String,
    colors: List<ColorInfo>
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.SemiBold
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            colors.forEach { colorInfo ->
                ColorCard(
                    colorInfo = colorInfo,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun ColorCard(
    colorInfo: ColorInfo,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = colorInfo.color,
                        shape = RoundedCornerShape(6.dp)
                    )
            )

            Text(
                text = colorInfo.name,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 10.sp
            )

            Text(
                text = "#${Integer.toHexString(colorInfo.color.toArgb()).uppercase().substring(2)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 8.sp
            )
        }
    }
}

private data class ColorInfo(
    val name: String,
    val color: Color
)

@Composable
@Preview(showBackground = true)
private fun PreviewCrimsonArcade() {
    VirtualGamePadMobileThemePreview(MinimalistPalette.CRIMSON_ARCADE)
}

@Composable
@Preview(showBackground = true)
private fun PreviewMoltenAmber() {
    VirtualGamePadMobileThemePreview(MinimalistPalette.MOLTEN_AMBER)
}

@Composable
@Preview(showBackground = true)
private fun PreviewGoldenCircuit() {
    VirtualGamePadMobileThemePreview(MinimalistPalette.GOLDEN_CIRCUIT)
}

@Composable
@Preview(showBackground = true)
private fun PreviewTerminalPhosphor() {
    VirtualGamePadMobileThemePreview(MinimalistPalette.TERMINAL_PHOSPHOR)
}

@Composable
@Preview(showBackground = true)
private fun PreviewGlacierCyan() {
    VirtualGamePadMobileThemePreview(MinimalistPalette.GLACIER_CYAN)
}

@Composable
@Preview(showBackground = true)
private fun PreviewSlateBlue() {
    VirtualGamePadMobileThemePreview(MinimalistPalette.SLATE_BLUE)
}

@Composable
@Preview(showBackground = true)
private fun PreviewDeepViolet() {
    VirtualGamePadMobileThemePreview(MinimalistPalette.DEEP_VIOLET)
}

@Composable
@Preview(showBackground = true)
private fun PreviewNeonOrchid() {
    VirtualGamePadMobileThemePreview(MinimalistPalette.NEON_ORCHID)
}

@Composable
@Preview(showBackground = true)
private fun PreviewBubblegumPulse() {
    VirtualGamePadMobileThemePreview(MinimalistPalette.BUBBLEGUM_PULSE)
}

@Composable
@Preview(showBackground = true)
private fun PreviewMonoSteel() {
    VirtualGamePadMobileThemePreview(MinimalistPalette.MONO_STEEL)
}
