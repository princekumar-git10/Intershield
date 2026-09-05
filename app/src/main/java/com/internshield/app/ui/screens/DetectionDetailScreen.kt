package com.internshield.app.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.internshield.app.model.DetectionResult
import com.internshield.app.model.RiskLevel
import com.internshield.app.ui.components.AppCard
import com.internshield.app.ui.components.RiskBadge
import com.internshield.app.ui.components.RiskCircularScore
import com.internshield.app.ui.theme.AccentOrange
import com.internshield.app.ui.theme.BgDark
import com.internshield.app.ui.theme.CardBorder
import com.internshield.app.ui.theme.CardBorderSubtle
import com.internshield.app.ui.theme.CardSubtle
import com.internshield.app.ui.theme.DangerPinkRed
import com.internshield.app.ui.theme.SuccessNeonGreen
import com.internshield.app.ui.theme.TextMutedGray
import com.internshield.app.ui.theme.TextNearWhite
import com.internshield.app.ui.theme.TextSilver
import com.internshield.app.ui.theme.TextWhite
import com.internshield.app.ui.theme.WarningAmber
import com.internshield.app.viewmodel.AppScreen
import com.internshield.app.viewmodel.ShieldViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetectionDetailScreen(
    result: DetectionResult,
    viewModel: ShieldViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val riskColor = when (result.riskLevel) {
        RiskLevel.HIGH -> DangerPinkRed
        RiskLevel.MEDIUM -> WarningAmber
        RiskLevel.LOW -> SuccessNeonGreen
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Scam Diagnosis",
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
                actions = {
                    IconButton(
                        onClick = {
                            val clip = ClipData.newPlainText(
                                "InternShield Warning",
                                "⚠️ InternShield Fraud Warning\nSender: ${result.sender}\nRisk Score: ${result.riskScore}/100\nCategory: ${result.category}\nRecommendation: ${result.recommendation}"
                            )
                            val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            cm.setPrimaryClip(clip)
                            Toast.makeText(context, "Threat report copied to clipboard", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share Warning",
                            tint = TextMutedGray
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BgDark
                )
            )
        },
        containerColor = BgDark,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(scrollState)
        ) {
            // ── Hero Threat Score Gauge Card ─────────────────────────────────────────
            AppCard(
                cornerRadius = 24.dp,
                accentGlow = riskColor,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    RiskCircularScore(score = result.riskScore, size = 130.dp)

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = result.category,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    RiskBadge(riskLevel = result.riskLevel)
                }
            }

            // ── Scanned Message Content ──────────────────────────────────────────────
            Text(
                text = "SCANNED MESSAGE CONTENT",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp,
                color = TextMutedGray,
                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
            )

            AppCard(
                cornerRadius = 18.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "From: ${result.sender}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(CardSubtle)
                            .border(1.dp, CardBorderSubtle, RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "\"${result.message}\"",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextNearWhite
                        )
                    }
                }
            }

            // ── Threat Signals Detected ──────────────────────────────────────────────
            Text(
                text = "THREAT SIGNALS DETECTED",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp,
                color = TextMutedGray,
                modifier = Modifier.padding(top = 20.dp, bottom = 8.dp)
            )

            result.reasons.forEach { rawReason ->
                val isEngineNote = rawReason.contains("Engine", ignoreCase = true) || rawReason.contains("Fallback", ignoreCase = true)
                val dotColor = if (isEngineNote) AccentOrange else if (result.riskLevel == RiskLevel.HIGH) DangerPinkRed else SuccessNeonGreen

                AppCard(
                    cornerRadius = 14.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
                    ) {
                        if (isEngineNote) {
                            Text(
                                text = "⚡",
                                fontSize = 13.sp,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(dotColor)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                        }

                        Text(
                            text = rawReason.removePrefix("🤖 ").removePrefix("💰 ").removePrefix("👤 ").removePrefix("⚠️ ").removePrefix("🎓 ").removePrefix("🌐 "),
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isEngineNote) TextSilver else TextWhite
                        )
                    }
                }
            }

            // ── Score Breakdown ──────────────────────────────────────────────────────
            if (result.scoreBreakdown.isNotEmpty()) {
                Text(
                    text = "RISK SCORE BREAKDOWN",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp,
                    color = TextMutedGray,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )

                AppCard(
                    cornerRadius = 16.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        result.scoreBreakdown.forEach { (item, score) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = item,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextMutedGray
                                )
                                Text(
                                    text = "+$score pts",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = riskColor
                                )
                            }
                        }
                    }
                }
            }

            // ── Safety Action Guide ──────────────────────────────────────────────────
            Text(
                text = "ACTIONABLE DEFENSE GUIDE",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp,
                color = TextMutedGray,
                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
            )

            val displayDoNots = if (result.doNots.isNotEmpty()) {
                result.doNots
            } else {
                listOf(
                    "Do NOT pay registration fees, security deposits, or laptop shipping charges",
                    "Do NOT share OTPs, passwords, or net banking credentials",
                    "Do NOT send identity documents (Aadhaar, PAN) over WhatsApp"
                )
            }

            val displayDos = if (result.dos.isNotEmpty()) {
                result.dos
            } else {
                listOf(
                    "Block and report this sender on WhatsApp immediately",
                    "Verify recruiter identity on LinkedIn and official company careers portal"
                )
            }

            AppCard(
                cornerRadius = 18.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    // Do Nots
                    displayDoNots.forEach { item ->
                        Row(
                            verticalAlignment = Alignment.Top,
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Don't",
                                tint = DangerPinkRed,
                                modifier = Modifier
                                    .size(16.dp)
                                    .padding(top = 2.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = item,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextWhite
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Dos
                    displayDos.forEach { item ->
                        Row(
                            verticalAlignment = Alignment.Top,
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Do",
                                tint = SuccessNeonGreen,
                                modifier = Modifier
                                    .size(16.dp)
                                    .padding(top = 2.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = item,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextWhite
                            )
                        }
                    }
                }
            }

            // ── Primary Actions ──────────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 40.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = { viewModel.navigateTo(AppScreen.Scan) },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Scan Another", fontSize = 13.sp, color = TextWhite)
                }

                Button(
                    onClick = { viewModel.navigateTo(AppScreen.Dashboard) },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentOrange, contentColor = Color.White),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Back to Shield", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
