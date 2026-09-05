package com.internshield.app.ui.screens

import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.internshield.app.model.SenderStatus
import com.internshield.app.ui.components.AppCard
import com.internshield.app.ui.theme.AlertCrimson
import com.internshield.app.ui.theme.CyberBorder
import com.internshield.app.ui.theme.CyberSurface
import com.internshield.app.ui.theme.CyberSurfaceElevated
import com.internshield.app.ui.theme.CyberSurfaceSubtle
import com.internshield.app.ui.theme.SolarOrange
import com.internshield.app.ui.theme.SolarOrangeDark
import com.internshield.app.ui.theme.SolarOrangeLight
import com.internshield.app.ui.theme.TextMuted
import com.internshield.app.ui.theme.TextSilver
import com.internshield.app.ui.theme.TextWhite
import com.internshield.app.viewmodel.AppScreen
import com.internshield.app.viewmodel.ShieldViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ScanScreen(
    viewModel: ShieldViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()

    var messageText by remember { mutableStateOf("") }
    var isScanning by remember { mutableStateOf(false) }
    var scanStepText by remember { mutableStateOf("Analyzing message content...") }

    fun runScanSequence() {
        if (messageText.isBlank()) return
        isScanning = true

        scope.launch {
            scanStepText = "Scanning recruitment linguistic signals..."
            delay(400)
            scanStepText = "Checking UPI demands & spoofed domains..."
            delay(400)
            scanStepText = "Generating scam probability score..."
            delay(300)

            viewModel.simulateNotification(
                sender = "Manual Scan",
                messageText = messageText,
                senderStatus = SenderStatus.UNKNOWN
            )
            isScanning = false
            viewModel.navigateTo(AppScreen.RecentDetections)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .verticalScroll(scrollState)
    ) {
        Text(
            text = "AI Threat Scanner",
            style = MaterialTheme.typography.displayLarge,
            color = TextWhite,
            modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
        )
        Text(
            text = "Deep AI scan for fake job offers, upfront fee scams, and phishing links.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSilver,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // ── Input Surface Card ───────────────────────────────────────────────────
        AppCard(
            cornerRadius = 22.dp,
            accentGlow = SolarOrange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "OFFER CONTENT / MESSAGE",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp,
                        color = TextMuted
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Quick Paste from Clipboard
                        Text(
                            text = "📋 Paste",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = SolarOrangeLight,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .clickable {
                                    val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = cm.primaryClip
                                    if (clip != null && clip.itemCount > 0) {
                                        messageText = clip.getItemAt(0).text.toString()
                                    }
                                }
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )

                        if (messageText.isNotBlank() && !isScanning) {
                            Text(
                                text = "Clear",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = AlertCrimson,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable { messageText = "" }
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = messageText,
                    onValueChange = { messageText = it },
                    placeholder = { Text("Paste suspicious WhatsApp job offer, stipend claim, or verification link...", color = TextMuted, fontSize = 13.sp) },
                    minLines = 5,
                    maxLines = 8,
                    enabled = !isScanning,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = CyberSurfaceSubtle,
                        unfocusedContainerColor = CyberSurfaceSubtle,
                        disabledContainerColor = CyberSurfaceSubtle,
                        focusedBorderColor = SolarOrange,
                        unfocusedBorderColor = CyberBorder,
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { runScanSequence() },
                    enabled = messageText.isNotBlank() && !isScanning,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SolarOrange,
                        contentColor = Color.White,
                        disabledContainerColor = SolarOrange.copy(alpha = 0.2f),
                        disabledContentColor = TextMuted
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(vertical = 6.dp)
                    ) {
                        if (isScanning) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.2.dp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(scanStepText, fontWeight = FontWeight.Black, fontSize = 13.sp)
                        } else {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Scan",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Launch Threat Analysis", fontWeight = FontWeight.Black, fontSize = 14.sp)
                        }
                    }
                }
            }
        }

        // ── Instant Test Scenario Chips ───────────────────────────────────────────
        Text(
            text = "INSTANT TEST PATTERNS",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.8.sp,
            color = TextMuted,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 80.dp)
        ) {
            SampleChip(
                label = "🚨 Upfront Fee Scam (₹999)",
                onClick = {
                    messageText = "Congratulations! You have been selected for Google Internship. Pay ₹999 registration fee today to confirm your seat."
                }
            )
            SampleChip(
                label = "💸 YouTube Task Scam",
                onClick = {
                    messageText = "Work from home! Earn ₹5000/day by liking YouTube videos. Click http://bit.ly/yt-task-earn and message coordinator on Telegram @TaskEarners."
                }
            )
            SampleChip(
                label = "🔑 KYC Phishing Scam",
                onClick = {
                    messageText = "URGENT: Your Internshala account will be terminated today. Verify immediately at http://internshala-kyc.xyz/login"
                }
            )
            SampleChip(
                label = "✅ Genuine Interview Link",
                onClick = {
                    messageText = "Hi, thank you for applying. Your resume has been shortlisted. We would like to schedule a 45-minute technical interview on Google Meet: meet.google.com/xyz-abc-123."
                }
            )
        }
    }
}

@Composable
private fun SampleChip(
    label: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(CyberSurface)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextWhite
        )
    }
}
