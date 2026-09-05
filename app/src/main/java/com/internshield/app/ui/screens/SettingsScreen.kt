package com.internshield.app.ui.screens

import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.internshield.app.ui.components.AppCard
import com.internshield.app.ui.theme.AlertCrimson
import com.internshield.app.ui.theme.CyberBlack
import com.internshield.app.ui.theme.CyberBorder
import com.internshield.app.ui.theme.CyberSurfaceElevated
import com.internshield.app.ui.theme.SolarOrange
import com.internshield.app.ui.theme.TextMuted
import com.internshield.app.ui.theme.TextSilver
import com.internshield.app.ui.theme.TextWhite
import com.internshield.app.viewmodel.AppScreen
import com.internshield.app.viewmodel.ShieldViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: ShieldViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val protectionEnabled by viewModel.protectionEnabled.collectAsState()
    val scanUnknownSendersOnly by viewModel.scanUnknownSendersOnly.collectAsState()
    val realTimeAlerts by viewModel.realTimeAlerts.collectAsState()
    val aiEnabled by viewModel.aiEnabled.collectAsState()
    val backendUrl by viewModel.backendUrl.collectAsState()
    val geminiApiKey by viewModel.geminiApiKey.collectAsState()
    val runInBackground by viewModel.runInBackground.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Settings",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { viewModel.handleBack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextWhite
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CyberBlack
                )
            )
        },
        containerColor = CyberBlack,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(scrollState)
        ) {
            // ── SECTION 1: PROTECTION CONTROLS ───────────────────────────────────────
            Text(
                text = "PROTECTION PARAMETERS",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp,
                color = TextMuted,
                modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
            )

            AppCard(
                cornerRadius = 20.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Protection status
                    SettingsToggleRow(
                        title = "Real-Time Protection",
                        subtitle = "Continuous scanning of incoming WhatsApp recruitment messages",
                        checked = protectionEnabled,
                        onCheckedChange = { viewModel.toggleProtection() }
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = CyberBorder)

                    // Scan unknown senders
                    SettingsToggleRow(
                        title = "Unknown Senders Only",
                        subtitle = "Messages from your saved phone contacts are ignored to preserve privacy",
                        checked = scanUnknownSendersOnly,
                        onCheckedChange = { viewModel.setScanUnknownSendersOnly(it) }
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = CyberBorder)

                    // Real-time alerts
                    SettingsToggleRow(
                        title = "High Risk Alerts",
                        subtitle = "Instant push notification when an unknown message contains scam signals",
                        checked = realTimeAlerts,
                        onCheckedChange = { viewModel.setRealTimeAlerts(it) }
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = CyberBorder)

                    // Background service
                    SettingsToggleRow(
                        title = "Persistent Background Service",
                        subtitle = "Maintain active protection even when app is closed",
                        checked = runInBackground,
                        onCheckedChange = { viewModel.setRunInBackground(it) }
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            try {
                                val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                val intent = Intent(Settings.ACTION_SETTINGS)
                                context.startActivity(intent)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CyberSurfaceElevated, contentColor = TextWhite),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Access",
                                tint = SolarOrange,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Manage Notification Access", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            try {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                    val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                                    context.startActivity(intent)
                                }
                            } catch (e: Exception) {
                                try {
                                    context.startActivity(Intent(Settings.ACTION_SETTINGS))
                                } catch (e2: Exception) {
                                    // Ignore
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CyberSurfaceElevated, contentColor = TextWhite),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Manage 24/7 Battery Optimization", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // ── SECTION 2: AI ENGINE CONFIGURATION ─────────────────────────────────
            Text(
                text = "AI ENGINE CONFIGURATION",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp,
                color = TextMuted,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            AppCard(
                cornerRadius = 20.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    SettingsToggleRow(
                        title = "Remote AI Backend",
                        subtitle = "Query secure Node.js threat analysis server",
                        checked = aiEnabled,
                        onCheckedChange = { viewModel.setAiEnabled(it) }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = backendUrl,
                        onValueChange = { viewModel.setBackendUrl(it) },
                        label = { Text("Server URL", color = TextMuted, fontSize = 12.sp) },
                        enabled = aiEnabled,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = CyberSurfaceElevated,
                            unfocusedContainerColor = CyberSurfaceElevated,
                            disabledContainerColor = CyberSurfaceElevated,
                            focusedBorderColor = SolarOrange,
                            unfocusedBorderColor = CyberBorder,
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = geminiApiKey,
                        onValueChange = { viewModel.setGeminiApiKey(it) },
                        label = { Text("Direct Gemini API Key (Optional)", color = TextMuted, fontSize = 12.sp) },
                        placeholder = { Text("AIzaSy...", color = TextMuted.copy(alpha = 0.5f), fontSize = 12.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = CyberSurfaceElevated,
                            unfocusedContainerColor = CyberSurfaceElevated,
                            disabledContainerColor = CyberSurfaceElevated,
                            focusedBorderColor = SolarOrange,
                            unfocusedBorderColor = CyberBorder,
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // ── SECTION 3: PRIVACY & DATA ───────────────────────────────────────────
            Text(
                text = "PRIVACY & LOCAL DATA",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp,
                color = TextMuted,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            AppCard(
                cornerRadius = 20.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 40.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "• Only incoming WhatsApp notifications from unknown senders are evaluated.\n" +
                                "• Messages from your saved phone contacts are ignored instantly.\n" +
                                "• No WhatsApp databases, private chats, or media files are ever accessed.\n" +
                                "• All history is stored locally on your device.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSilver,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = { viewModel.clearHistory() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AlertCrimson.copy(alpha = 0.15f),
                            contentColor = AlertCrimson
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Clear Local Scan History", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = TextWhite
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = SolarOrange,
                uncheckedTrackColor = CyberSurfaceElevated
            )
        )
    }
}
