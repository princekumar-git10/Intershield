package com.internshield.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.internshield.app.model.RiskLevel
import com.internshield.app.ui.theme.DangerPinkRed
import com.internshield.app.ui.theme.DangerRedDark
import com.internshield.app.ui.theme.SuccessGreenDark
import com.internshield.app.ui.theme.SuccessNeonGreen
import com.internshield.app.ui.theme.WarningAmber
import com.internshield.app.ui.theme.WarningAmberDark

/**
 * Aesthetic Security Status Tag with luminous micro-indicator.
 */
@Composable
fun RiskBadge(
    riskLevel: RiskLevel,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor, borderColor, labelText) = when (riskLevel) {
        RiskLevel.HIGH -> BadgeStyle(
            DangerRedDark,
            DangerPinkRed,
            DangerPinkRed.copy(alpha = 0.4f),
            "SCAM DETECTED"
        )
        RiskLevel.MEDIUM -> BadgeStyle(
            WarningAmberDark,
            WarningAmber,
            WarningAmber.copy(alpha = 0.4f),
            "SUSPICIOUS"
        )
        RiskLevel.LOW -> BadgeStyle(
            SuccessGreenDark,
            SuccessNeonGreen,
            SuccessNeonGreen.copy(alpha = 0.4f),
            "VERIFIED SAFE"
        )
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
            .padding(horizontal = 7.dp, vertical = 3.dp)
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(textColor)
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(
            text = labelText,
            color = textColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 0.4.sp,
            maxLines = 1,
            overflow = TextOverflow.Clip
        )
    }
}

private data class BadgeStyle(val bg: Color, val text: Color, val border: Color, val label: String)
