package com.internshield.app.model

enum class RiskLevel {
    LOW,
    MEDIUM,
    HIGH
}

data class DetectionResult(
    val id: String,
    val sender: String,
    val message: String,
    val timestamp: Long,
    val riskScore: Int,
    val riskLevel: RiskLevel,
    val category: String,
    val reasons: List<String>,
    val recommendation: String,
    val scoreBreakdown: Map<String, Int> = emptyMap(),
    val source: String = "WhatsApp",
    val dos: List<String> = emptyList(),
    val doNots: List<String> = emptyList()
)

data class ThreatStats(
    val scannedCount: Int,
    val threatCount: Int,
    val highRiskCount: Int
)
