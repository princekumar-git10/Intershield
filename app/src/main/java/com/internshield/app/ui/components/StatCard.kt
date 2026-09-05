package com.internshield.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.internshield.app.model.ThreatStats
import com.internshield.app.ui.theme.WhatsAppAlertRed
import com.internshield.app.ui.theme.WhatsAppBorder
import com.internshield.app.ui.theme.WhatsAppGreen
import com.internshield.app.ui.theme.WhatsAppSurface
import com.internshield.app.ui.theme.WhatsAppTextMuted
import com.internshield.app.ui.theme.WhatsAppTextPrimary

@Composable
fun ThreatStatsSection(
    stats: ThreatStats,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StatCard(
            title = "SCANNED",
            value = stats.scannedCount.toString(),
            color = WhatsAppTextPrimary,
            modifier = Modifier.weight(1f)
        )
        StatCard(
            title = "THREATS",
            value = stats.threatCount.toString(),
            color = if (stats.threatCount > 0) WhatsAppAlertRed else WhatsAppTextPrimary,
            modifier = Modifier.weight(1f)
        )
        StatCard(
            title = "SAFE RATE",
            value = if (stats.scannedCount > 0) {
                "${((stats.scannedCount - stats.threatCount) * 100 / stats.scannedCount)}%"
            } else {
                "100%"
            },
            color = WhatsAppGreen,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(WhatsAppSurface)
            .border(1.dp, WhatsAppBorder, RoundedCornerShape(12.dp))
            .padding(vertical = 10.dp, horizontal = 6.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                color = color
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = title,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp,
                color = WhatsAppTextMuted
            )
        }
    }
}
