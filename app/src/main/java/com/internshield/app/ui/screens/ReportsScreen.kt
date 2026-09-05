package com.internshield.app.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.internshield.app.ui.components.AppCard
import com.internshield.app.ui.components.RiskCircularScore
import com.internshield.app.ui.components.ThreatStatsSection
import com.internshield.app.ui.theme.ObsidianBg
import com.internshield.app.ui.theme.SignalAmber
import com.internshield.app.ui.theme.SignalCrimson
import com.internshield.app.ui.theme.SignalEmerald
import com.internshield.app.ui.theme.TextMuted
import com.internshield.app.ui.theme.TextPrimary
import com.internshield.app.ui.theme.TextSecondary
import com.internshield.app.viewmodel.ShieldViewModel

/**
 * ReportsScreen — Threat Intelligence & Analytics Dashboard.
 */
@Composable
fun ReportsScreen(
    viewModel: ShieldViewModel,
    modifier: Modifier = Modifier
) {
    val stats by viewModel.stats.collectAsState()
    val detections by viewModel.detections.collectAsState()
    val scrollState = rememberScrollState()

    // Calculate Overall System Risk Gauge
    val maxRiskScore = detections.maxOfOrNull { it.riskScore } ?: 0

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianBg)
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .verticalScroll(scrollState)
    ) {
        Text(
            text = "Reports & Analytics",
            style = MaterialTheme.typography.displayLarge,
            color = TextPrimary,
            modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
        )
        Text(
            text = "Threat intelligence summary and scam detection statistics.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // System Risk Gauge Card
        AppCard(
            cornerRadius = 16.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "Peak Threat Exposure",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(14.dp))

                RiskCircularScore(score = maxRiskScore, size = 130.dp)

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Evaluated across ${stats.scannedCount} WhatsApp messages",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )
            }
        }

        // Activity Stats Summary
        Text(
            text = "THREAT METRICS",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.8.sp,
            color = TextMuted,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        ThreatStatsSection(
            stats = stats,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Threat Distribution Breakdown
        Text(
            text = "SCAM CATEGORIES DETECTED",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.8.sp,
            color = TextMuted,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        AppCard(
            cornerRadius = 16.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 80.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                ReportMetricRow(
                    label = "Registration Fee Scams",
                    count = detections.count { it.category.contains("Fee", ignoreCase = true) || it.category.contains("REGISTRATION", ignoreCase = true) },
                    barColor = SignalCrimson
                )
                ReportMetricRow(
                    label = "Prepaid Task Scams",
                    count = detections.count { it.category.contains("Task", ignoreCase = true) },
                    barColor = SignalAmber
                )
                ReportMetricRow(
                    label = "Phishing Links & KYC",
                    count = detections.count { it.category.contains("PHISHING", ignoreCase = true) || it.category.contains("Link", ignoreCase = true) },
                    barColor = SignalCrimson
                )
                ReportMetricRow(
                    label = "Legitimate Communications",
                    count = detections.count { it.riskScore < 30 },
                    barColor = SignalEmerald
                )
            }
        }
    }
}

@Composable
private fun ReportMetricRow(
    label: String,
    count: Int,
    barColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(barColor)
            )
            Spacer(modifier = Modifier.padding(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimary
            )
        }
        Text(
            text = "$count",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = barColor
        )
    }
}
