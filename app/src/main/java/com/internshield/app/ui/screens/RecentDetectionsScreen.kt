package com.internshield.app.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.internshield.app.model.RiskLevel
import com.internshield.app.ui.components.AppCard
import com.internshield.app.ui.components.DetectionItemCard
import com.internshield.app.ui.theme.AccentOrange
import com.internshield.app.ui.theme.AccentRedOrange
import com.internshield.app.ui.theme.BgDark
import com.internshield.app.ui.theme.CardBorder
import com.internshield.app.ui.theme.CardElevated
import com.internshield.app.ui.theme.CardSurface
import com.internshield.app.ui.theme.TextMutedGray
import com.internshield.app.ui.theme.TextWhite
import com.internshield.app.viewmodel.AppScreen
import com.internshield.app.viewmodel.ShieldViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecentDetectionsScreen(
    viewModel: ShieldViewModel,
    modifier: Modifier = Modifier
) {
    val detections by viewModel.detections.collectAsState()
    var selectedFilter by remember { mutableStateOf("ALL") }

    val filteredDetections = remember(detections, selectedFilter) {
        when (selectedFilter) {
            "HIGH" -> detections.filter { it.riskLevel == RiskLevel.HIGH }
            "MEDIUM" -> detections.filter { it.riskLevel == RiskLevel.MEDIUM }
            "LOW" -> detections.filter { it.riskLevel == RiskLevel.LOW }
            else -> detections
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Scam Activity Log",
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
                    if (detections.isNotEmpty()) {
                        IconButton(onClick = { viewModel.clearHistory() }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Clear History",
                                tint = TextMutedGray
                            )
                        }
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
        ) {
            // Filter Pills Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ActivityFilterPill(
                    label = "All (${detections.size})",
                    isSelected = selectedFilter == "ALL",
                    onClick = { selectedFilter = "ALL" }
                )
                ActivityFilterPill(
                    label = "Threats",
                    isSelected = selectedFilter == "HIGH",
                    onClick = { selectedFilter = "HIGH" }
                )
                ActivityFilterPill(
                    label = "Suspicious",
                    isSelected = selectedFilter == "MEDIUM",
                    onClick = { selectedFilter = "MEDIUM" }
                )
                ActivityFilterPill(
                    label = "Safe",
                    isSelected = selectedFilter == "LOW",
                    onClick = { selectedFilter = "LOW" }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (filteredDetections.isEmpty()) {
                AppCard(
                    cornerRadius = 20.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp)
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
                                .background(CardElevated),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.List,
                                contentDescription = "No reports",
                                tint = TextMutedGray,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No logs matching this filter",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Evaluated WhatsApp messages will be logged here with risk diagnostics.",
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMutedGray
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 80.dp)
                ) {
                    items(filteredDetections, key = { it.id }) { item ->
                        DetectionItemCard(
                            result = item,
                            onClick = { viewModel.navigateTo(AppScreen.DetectionDetail(item)) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ActivityFilterPill(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isSelected) {
                    Brush.horizontalGradient(listOf(AccentOrange, AccentRedOrange))
                } else {
                    Brush.verticalGradient(listOf(CardSurface, CardSurface))
                }
            )
            .border(
                1.dp,
                if (isSelected) Color.Transparent else CardBorder,
                RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) TextWhite else TextMutedGray
        )
    }
}
