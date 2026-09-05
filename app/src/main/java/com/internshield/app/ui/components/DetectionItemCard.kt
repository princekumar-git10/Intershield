package com.internshield.app.ui.components

import android.text.format.DateUtils
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.internshield.app.model.DetectionResult
import com.internshield.app.model.RiskLevel
import com.internshield.app.ui.theme.CardBorder
import com.internshield.app.ui.theme.CardBorderSubtle
import com.internshield.app.ui.theme.CardElevated
import com.internshield.app.ui.theme.CardSubtle
import com.internshield.app.ui.theme.CardSurface
import com.internshield.app.ui.theme.DangerPinkRed
import com.internshield.app.ui.theme.DangerRedDark
import com.internshield.app.ui.theme.SuccessGreenDark
import com.internshield.app.ui.theme.SuccessNeonGreen
import com.internshield.app.ui.theme.TextMutedGray
import com.internshield.app.ui.theme.TextNearWhite
import com.internshield.app.ui.theme.TextWhite
import com.internshield.app.ui.theme.WarningAmber

/**
 * Premium Activity Log Detection Card (matching user specification).
 */
@Composable
fun DetectionItemCard(
    result: DetectionResult,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isHighRisk = result.riskLevel == RiskLevel.HIGH
    val isSafe = result.riskLevel == RiskLevel.LOW

    val accentColor = when (result.riskLevel) {
        RiskLevel.HIGH -> DangerPinkRed
        RiskLevel.MEDIUM -> WarningAmber
        RiskLevel.LOW -> SuccessNeonGreen
    }

    val cardBg = when (result.riskLevel) {
        RiskLevel.HIGH -> Color(0xFF181014)   // Subtle dark burgundy/black glass
        RiskLevel.MEDIUM -> Color(0xFF181510) // Subtle dark amber glass
        RiskLevel.LOW -> Color(0xFF101614)    // Subtle dark emerald glass
    }

    AppCard(
        onClick = onClick,
        cornerRadius = 20.dp,
        backgroundColor = cardBg,
        accentGlow = if (isHighRisk) DangerPinkRed else if (isSafe) SuccessNeonGreen else WarningAmber,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            // Top Header: Tag + Category + Timestamp
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f).padding(end = 6.dp)
                ) {
                    RiskBadge(riskLevel = result.riskLevel)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = result.category,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = accentColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Text(
                    text = getRelativeTimeString(result.timestamp),
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMutedGray
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Sender Row
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(accentColor.copy(alpha = 0.25f), CardSubtle)
                            )
                        )
                        .border(1.dp, accentColor.copy(alpha = 0.5f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = result.sender.take(1).uppercase(),
                        color = accentColor,
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = result.sender,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Message Quote Snippet
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(CardSubtle)
                    .border(1.dp, CardBorderSubtle, RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                Text(
                    text = "\"${result.message}\"",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextNearWhite,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Footer
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (isHighRisk) "🚨 Upfront payment requested" else "Verified communications",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextMutedGray
                )

                Text(
                    text = "Score ${result.riskScore}/100 →",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = accentColor
                )
            }
        }
    }
}

private fun getRelativeTimeString(timeMs: Long): String {
    return DateUtils.getRelativeTimeSpanString(
        timeMs,
        System.currentTimeMillis(),
        DateUtils.MINUTE_IN_MILLIS,
        DateUtils.FORMAT_ABBREV_RELATIVE
    ).toString()
}
