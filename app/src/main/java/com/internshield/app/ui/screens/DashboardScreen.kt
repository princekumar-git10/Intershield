package com.internshield.app.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.internshield.app.model.DetectionResult
import com.internshield.app.model.RiskLevel
import com.internshield.app.ui.components.AppCard
import com.internshield.app.ui.components.HolographicAiCore
import com.internshield.app.ui.components.RiskBadge
import com.internshield.app.ui.components.ShieldLogo
import com.internshield.app.ui.theme.AlertCrimson
import com.internshield.app.ui.theme.CyberBorder
import com.internshield.app.ui.theme.CyberSurface
import com.internshield.app.ui.theme.CyberSurfaceElevated
import com.internshield.app.ui.theme.SafeEmerald
import com.internshield.app.ui.theme.SolarOrange
import com.internshield.app.ui.theme.SolarOrangeDark
import com.internshield.app.ui.theme.SolarOrangeLight
import com.internshield.app.ui.theme.TextMuted
import com.internshield.app.ui.theme.TextSilver
import com.internshield.app.ui.theme.TextWhite
import com.internshield.app.ui.theme.WarningGold
import com.internshield.app.viewmodel.AppScreen
import com.internshield.app.viewmodel.ShieldViewModel

@Composable
fun DashboardScreen(
    viewModel: ShieldViewModel,
    modifier: Modifier = Modifier
) {
    val protectionEnabled by viewModel.protectionEnabled.collectAsState()
    val stats by viewModel.stats.collectAsState()
    val detections by viewModel.detections.collectAsState()
    val scrollState = rememberScrollState()

    val statusColor by animateColorAsState(
        targetValue = if (protectionEnabled) SolarOrange else AlertCrimson,
        label = "status_color"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .verticalScroll(scrollState)
    ) {
        // ── Top Bar Header (matching reference: Avatar/Logo + Title + Settings) ───
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp, top = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ShieldLogo(size = 40.dp, animatedGlow = protectionEnabled)
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "InternShield",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = TextWhite
                    )
                    Text(
                        text = "WhatsApp AI Cyber Guard",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                }
            }

            IconButton(
                onClick = { viewModel.navigateTo(AppScreen.Settings) },
                modifier = Modifier
                    .clip(CircleShape)
                    .background(CyberSurfaceElevated)
                    .size(38.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = TextSilver,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        // ── Holographic AI Core Section (Reference Centerpiece) ───────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            HolographicAiCore(size = 120.dp, isActive = protectionEnabled)

            Spacer(modifier = Modifier.height(12.dp))

            // Title: "Your Personal [AI] Cyber Guard"
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Your Personal ",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite
                )

                // Glowing AI Pill Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(SolarOrange, SolarOrangeDark)
                            )
                        )
                        .padding(horizontal = 7.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "AI",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }

                Text(
                    text = " Cyber Guard",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Subtitle & Toggle Switch Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(CyberSurface)
                    .border(1.dp, CyberBorder, RoundedCornerShape(20.dp))
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(if (protectionEnabled) SolarOrange else AlertCrimson)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (protectionEnabled) "Live WhatsApp Defense Active" else "Shield Paused",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (protectionEnabled) SolarOrangeLight else AlertCrimson
                )
                Spacer(modifier = Modifier.width(10.dp))
                Switch(
                    checked = protectionEnabled,
                    onCheckedChange = { viewModel.toggleProtection() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = SolarOrange,
                        uncheckedTrackColor = CyberSurfaceElevated
                    ),
                    modifier = Modifier.height(24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // ── Solar Magma Hero Action Card (Matching Reference Action Banner) ──────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            SolarOrangeLight,
                            SolarOrange,
                            SolarOrangeDark
                        )
                    )
                )
                .clickable { viewModel.navigateTo(AppScreen.Scan) }
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.22f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Scan",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Start Your Security Scan",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                        Text(
                            text = "Check WhatsApp offers before threats appear.",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.25f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Go",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ── "Needs Attention" / Threat Intercept Section (Matching Reference) ─────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Needs Attention",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextWhite
            )
            if (detections.isNotEmpty()) {
                Text(
                    text = "See All (${detections.size})",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = SolarOrangeLight,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable { viewModel.navigateTo(AppScreen.RecentDetections) }
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                )
            }
        }

        if (detections.isEmpty()) {
            AppCard(
                cornerRadius = 20.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 80.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(CyberSurfaceElevated),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Clean",
                            tint = TextMuted,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "System Secure & Clean",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "No pending scam alerts. All WhatsApp recruitment messages are monitored.",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )
                }
            }
        } else {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.padding(bottom = 80.dp)
            ) {
                detections.take(4).forEach { item ->
                    ReferenceStyleThreatCard(
                        result = item,
                        onClick = { viewModel.navigateTo(AppScreen.DetectionDetail(item)) }
                    )
                }
            }
        }
    }
}

/**
 * List card styled exactly like the reference design's "Needs Attention" tiles.
 */
@Composable
private fun ReferenceStyleThreatCard(
    result: DetectionResult,
    onClick: () -> Unit
) {
    val isHighRisk = result.riskLevel == RiskLevel.HIGH
    val accentColor = if (isHighRisk) AlertCrimson else SafeEmerald

    AppCard(
        onClick = onClick,
        cornerRadius = 18.dp,
        accentGlow = if (isHighRisk) AlertCrimson else null,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Left circular icon
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(CyberSurfaceElevated)
                        .border(1.dp, accentColor.copy(alpha = 0.4f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isHighRisk) Icons.Default.Warning else Icons.Default.Search,
                        contentDescription = "Status",
                        tint = accentColor,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = result.sender,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        RiskBadge(riskLevel = result.riskLevel)
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = result.message,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSilver,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Right action arrow
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(CyberSurfaceElevated),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Details",
                    tint = TextSilver,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}
