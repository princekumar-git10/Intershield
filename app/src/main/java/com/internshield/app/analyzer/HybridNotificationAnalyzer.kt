package com.internshield.app.analyzer

import android.content.Context
import android.util.Log
import com.internshield.app.model.NotificationData
import com.internshield.app.model.SenderStatus
import com.internshield.app.model.SettingsRepository
import com.internshield.app.model.RiskLevel

/**
 * HybridNotificationAnalyzer orchestrates threat analysis by combining:
 *   1. Remote AI Backend Server (Primary when enabled)
 *   2. Local Rule-Based Signal Engine (Fallback when offline / AI disabled)
 *
 * This provides zero-downtime protection for students: if the AI server is
 * unreachable or offline, the app continues protecting the user using on-device rules.
 */
class HybridNotificationAnalyzer {

    companion object {
        private const val TAG = "HybridAnalyzer"
    }

    private val localAnalyzer = LocalNotificationAnalyzer()
    private val remoteAiAnalyzer = RemoteAiAnalyzer()

    /**
     * Analyzes notification data using AI backend with automatic local engine fallback.
     */
    suspend fun analyze(context: Context, notification: NotificationData): AnalysisResult {
        // Gate check: Is real-time protection enabled?
        val isProtectionEnabled = SettingsRepository.isProtectionEnabled(context)
        if (!isProtectionEnabled) {
            return AnalysisResult(
                riskScore = 0,
                riskLevel = RiskLevel.LOW,
                category = "Protection Paused",
                detectedSignals = listOf("⚪ Real-time protection is currently disabled"),
                scoreBreakdown = emptyMap(),
                recommendation = "Enable Real-time Protection in settings to scan messages."
            )
        }

        // Gate check: Only scan unknown senders if settings specify
        val scanUnknownOnly = SettingsRepository.isScanUnknownSendersOnly(context)
        val isMatchGate = if (scanUnknownOnly) {
            notification.senderStatus == SenderStatus.UNKNOWN
        } else {
            notification.senderStatus != SenderStatus.KNOWN
        }

        if (!isMatchGate) {
            return localAnalyzer.analyze(notification)
        }

        val isAiEnabled = SettingsRepository.isAiBackendEnabled(context)
        val geminiApiKey = SettingsRepository.getGeminiApiKey(context)
        val serverUrl = SettingsRepository.getBackendUrl(context)

        if (isAiEnabled || geminiApiKey.isNotBlank()) {
            Log.d(TAG, "Attempting Remote AI Backend analysis...")
            val aiResult = remoteAiAnalyzer.analyzeWithAi(notification, geminiApiKey, serverUrl)
            
            if (aiResult.isSuccess) {
                val result = aiResult.getOrThrow()
                Log.i(TAG, "Analysis powered by Remote AI Backend — Score: ${result.riskScore}")
                return result
            } else {
                Log.w(TAG, "Remote AI Backend call failed (${aiResult.exceptionOrNull()?.message}). Falling back to Local Rule Engine.")
            }
        } else {
            Log.d(TAG, "AI Backend is disabled — using Local Rule Engine.")
        }

        // Fallback to local rule-based engine
        val localResult = localAnalyzer.analyze(notification)
        return localResult.copy(
            detectedSignals = localResult.detectedSignals + "⚡ Analyzed via On-Device Engine (Fallback)"
        )
    }

    /**
     * Synchronous overload for backwards compatibility or fallback callers.
     */
    fun analyzeLocalOnly(notification: NotificationData): AnalysisResult {
        return localAnalyzer.analyze(notification)
    }
}

