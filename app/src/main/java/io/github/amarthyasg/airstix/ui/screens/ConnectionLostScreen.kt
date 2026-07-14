package io.github.amarthyasg.airstix.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.amarthyasg.airstix.R
import io.github.amarthyasg.airstix.data.PreviewBase
import io.github.amarthyasg.airstix.data.PreviewHeightDp
import io.github.amarthyasg.airstix.data.PreviewWidthDp
import io.github.amarthyasg.airstix.ui.theme.NeonBlue
import io.github.amarthyasg.airstix.ui.theme.PureBlack

@Composable
fun ConnectionLostScreen(
    errorMessage: String?,
    onReconnect: () -> Unit,
    onNavigateToMainMenu: () -> Unit,
) {
    // Intercept hardware back button to return to Main Menu safely rather than popping to a blank screen
    BackHandler {
        onNavigateToMainMenu()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.connecting_error_title).uppercase(),
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            val displayError = errorMessage?.let {
                stringResource(R.string.gamepad_connection_lost_error, it)
            } ?: stringResource(R.string.gamepad_connection_lost)

            Text(
                text = displayError,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            Spacer(modifier = Modifier.height(48.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Reconnect Button
                Button(
                    onClick = onReconnect,
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text(
                        text = "reconnect",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                }

                // Main Menu Button
                Button(
                    onClick = onNavigateToMainMenu,
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text(
                        text = "Main Menu",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Preview(
    name = "Connection Lost Screen - Landscape",
    device = "spec:width=${PreviewWidthDp}dp,height=${PreviewHeightDp}dp,orientation=landscape,dpi=420",
    showBackground = true
)
@Composable
fun ConnectionLostScreenPreview() {
    PreviewBase {
        ConnectionLostScreen(
            errorMessage = "Server closed connection",
            onReconnect = {},
            onNavigateToMainMenu = {}
        )
    }
}
