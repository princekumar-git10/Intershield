package com.internshield.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.internshield.app.model.SenderStatus
import com.internshield.app.ui.components.AppCard
import com.internshield.app.ui.theme.BorderHairline
import com.internshield.app.ui.theme.ObsidianBg
import com.internshield.app.ui.theme.ObsidianCardElevated
import com.internshield.app.ui.theme.SignalEmerald
import com.internshield.app.ui.theme.TextMuted
import com.internshield.app.ui.theme.TextPrimary
import com.internshield.app.ui.theme.TextSecondary
import com.internshield.app.viewmodel.AppScreen
import com.internshield.app.viewmodel.ShieldViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DemoSimulatorScreen(
    viewModel: ShieldViewModel,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    var customSender by remember { mutableStateOf("+91 98765 43210") }
    var customMessage by remember { mutableStateOf("Selected for Google Internship. Pay ₹999 registration fee today.") }
    var selectedSenderStatus by remember { mutableStateOf(SenderStatus.UNKNOWN) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianBg)
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .verticalScroll(scrollState)
    ) {
        Text(
            text = "Testing Simulator",
            style = MaterialTheme.typography.displayLarge,
            color = TextPrimary,
            modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
        )
        Text(
            text = "Trigger custom WhatsApp notification payloads for testing.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        AppCard(
            cornerRadius = 16.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 80.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                OutlinedTextField(
                    value = customSender,
                    onValueChange = { customSender = it },
                    label = { Text("Sender Name / Phone Number", color = TextMuted) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = ObsidianCardElevated,
                        unfocusedContainerColor = ObsidianCardElevated,
                        focusedBorderColor = SignalEmerald,
                        unfocusedBorderColor = BorderHairline,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = customMessage,
                    onValueChange = { customMessage = it },
                    label = { Text("WhatsApp Message Text", color = TextMuted) },
                    minLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = ObsidianCardElevated,
                        unfocusedContainerColor = ObsidianCardElevated,
                        focusedBorderColor = SignalEmerald,
                        unfocusedBorderColor = BorderHairline,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        viewModel.simulateNotification(customSender, customMessage, selectedSenderStatus)
                        viewModel.navigateTo(AppScreen.RecentDetections)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SignalEmerald, contentColor = Color(0xFF090C15)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Run Risk Engine Scan", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
