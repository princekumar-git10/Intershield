package com.internshield.app.viewmodel

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.internshield.app.model.DetectionRepository
import com.internshield.app.analyzer.HybridNotificationAnalyzer
import com.internshield.app.analyzer.RemoteAiAnalyzer
import com.internshield.app.model.DetectionResult
import com.internshield.app.model.NotificationData
import com.internshield.app.model.RiskLevel
import com.internshield.app.model.SenderStatus
import com.internshield.app.model.SettingsRepository
import com.internshield.app.model.ThreatStats
import com.internshield.app.service.ShieldNotificationListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

sealed class AppScreen {
    object Splash : AppScreen()
    object Dashboard : AppScreen()
    object RecentDetections : AppScreen()
    object Scan : AppScreen()
    object Reports : AppScreen()
    data class DetectionDetail(val result: DetectionResult) : AppScreen()
    object Settings : AppScreen()
    object DemoSimulator : AppScreen()
}

class ShieldViewModel(application: Application) : AndroidViewModel(application) {

    private val _currentScreen = MutableStateFlow<AppScreen>(AppScreen.Dashboard)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    private val _protectionEnabled = MutableStateFlow(true)
    val protectionEnabled: StateFlow<Boolean> = _protectionEnabled.asStateFlow()

    private val _scanUnknownSendersOnly = MutableStateFlow(true)
    val scanUnknownSendersOnly: StateFlow<Boolean> = _scanUnknownSendersOnly.asStateFlow()

    private val _realTimeAlerts = MutableStateFlow(true)
    val realTimeAlerts: StateFlow<Boolean> = _realTimeAlerts.asStateFlow()

    private val _aiEnabled = MutableStateFlow(true)
    val aiEnabled: StateFlow<Boolean> = _aiEnabled.asStateFlow()

    private val _backendUrl = MutableStateFlow(RemoteAiAnalyzer.DEFAULT_BACKEND_URL)
    val backendUrl: StateFlow<String> = _backendUrl.asStateFlow()

    private val _geminiApiKey = MutableStateFlow("")
    val geminiApiKey: StateFlow<String> = _geminiApiKey.asStateFlow()

    private val _runInBackground = MutableStateFlow(true)
    val runInBackground: StateFlow<Boolean> = _runInBackground.asStateFlow()

    private val _detections = MutableStateFlow<List<DetectionResult>>(emptyList())
    val detections: StateFlow<List<DetectionResult>> = _detections.asStateFlow()

    private val _stats = MutableStateFlow(ThreatStats(0, 0, 0))
    val stats: StateFlow<ThreatStats> = _stats.asStateFlow()

    private val hybridAnalyzer = HybridNotificationAnalyzer()

    /**
     * BroadcastReceiver that listens for analysis results published by
     * [ShieldNotificationListener]. When a WhatsApp notification is analyzed,
     * the service broadcasts the result here, and we add it to the detection list.
     */
    private var notificationReceiver: BroadcastReceiver? = null

    init {
        val app = getApplication<Application>()
        _protectionEnabled.value = SettingsRepository.isProtectionEnabled(app)
        _scanUnknownSendersOnly.value = SettingsRepository.isScanUnknownSendersOnly(app)
        _realTimeAlerts.value = SettingsRepository.isRealTimeAlertsEnabled(app)
        _aiEnabled.value = SettingsRepository.isAiBackendEnabled(app)
        _backendUrl.value = SettingsRepository.getBackendUrl(app)
        _geminiApiKey.value = SettingsRepository.getGeminiApiKey(app)
        _runInBackground.value = SettingsRepository.isRunInBackgroundEnabled(app)
        loadDetections()
    }

    private fun loadDetections() {
        _detections.value = DetectionRepository.getDetections(getApplication())
        updateStats()
    }

    /**
     * Register the BroadcastReceiver so the ViewModel can receive live detection
     * results from [ShieldNotificationListener].
     */
    fun registerReceiver(context: Context) {
        if (notificationReceiver != null) return // Already registered

        notificationReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action != ShieldNotificationListener.ACTION_THREAT_DETECTED) return

                val sender = intent.getStringExtra(ShieldNotificationListener.EXTRA_SENDER)
                    ?: "Unknown Sender"
                val message = intent.getStringExtra(ShieldNotificationListener.EXTRA_MESSAGE)
                    ?: "(message not available)"
                val riskScore = intent.getIntExtra(ShieldNotificationListener.EXTRA_RISK_SCORE, 0)
                val riskLevelStr = intent.getStringExtra(ShieldNotificationListener.EXTRA_RISK_LEVEL)
                    ?: RiskLevel.LOW.name
                val category = intent.getStringExtra(ShieldNotificationListener.EXTRA_CATEGORY)
                    ?: "Unknown"
                val signals = intent.getStringArrayListExtra(ShieldNotificationListener.EXTRA_SIGNALS)
                    ?: arrayListOf()
                val recommendation = intent.getStringExtra(ShieldNotificationListener.EXTRA_RECOMMENDATION)
                    ?: "Always verify recruitment offers through official channels."

                val riskLevel = try {
                    RiskLevel.valueOf(riskLevelStr)
                } catch (e: IllegalArgumentException) {
                    RiskLevel.LOW
                }

                // Reload from repository since the service already saved the live detection
                loadDetections()
            }
        }

        val filter = IntentFilter(ShieldNotificationListener.ACTION_THREAT_DETECTED)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(notificationReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            context.registerReceiver(notificationReceiver, filter)
        }
    }

    fun unregisterReceiver(context: Context) {
        notificationReceiver?.let {
            try {
                context.unregisterReceiver(it)
            } catch (e: IllegalArgumentException) {
                // Receiver was already unregistered
            }
            notificationReceiver = null
        }
    }

    fun populateDemoMessages() {
        val app = getApplication<Application>()
        val now = System.currentTimeMillis()
        val mockList = listOf(
            DetectionResult(
                id = UUID.randomUUID().toString(),
                sender = "+91 70123 45678",
                message = "Hello, I am HR manager from YouTube media company. We are looking for online part-time workers to like videos. Earn ₹1500 to ₹5000 per day. Daily instant payment. Please add our coordinator on Telegram: @YTaskManager to start your first task.",
                timestamp = now - 1000 * 60 * 15,
                riskScore = 98,
                riskLevel = RiskLevel.HIGH,
                category = "Prepaid YouTube Task Scam",
                reasons = listOf(
                    "🤖 Unrealistic stipend/salary promised",
                    "🤖 External redirect to Telegram coordinator",
                    "👤 Sender is not in your contact list"
                ),
                recommendation = "DO NOT Message them on Telegram. This is a classic prepaid task scam designed to steal money via fake task commissions.",
                dos = listOf("Block this sender on WhatsApp", "Search online for YouTube task review scams"),
                doNots = listOf("Contact the coordinator on Telegram", "Pay any deposit to unlock review tasks")
            ),
            DetectionResult(
                id = UUID.randomUUID().toString(),
                sender = "+91 99112 23344",
                message = "Dear candidate, you are selected for the WFH Graphic Design Internship. Stipend is ₹18,000/month. A refundable delivery charge of ₹1,499 is required for sending the company-issued iMac and drawing tablet. Pay UPI: company-logistics@upi to confirm.",
                timestamp = now - 1000 * 60 * 60,
                riskScore = 94,
                riskLevel = RiskLevel.HIGH,
                category = "Upfront Equipment Deposit Scam",
                reasons = listOf(
                    "🤖 Upfront equipment delivery fee requested",
                    "🤖 Selection claim without prior interviews",
                    "👤 Sender is not in your contact list"
                ),
                recommendation = "DO NOT PAY. Genuine companies never charge candidates registration fees, security deposits, or shipping charges for work laptops.",
                dos = listOf("Ask the recruiter for official email confirmation", "Verify the hiring program on the official company careers page"),
                doNots = listOf("Pay any registration fee, security deposit, or laptop charge", "Share bank details or Aadhaar card")
            ),
            DetectionResult(
                id = UUID.randomUUID().toString(),
                sender = "+91 80808 90909",
                message = "InternShield Security: A login attempt was detected on your profile from a new device in Delhi. If this was not you, please verify your identity immediately by sharing the 6-digit OTP sent to your phone with this chat helper.",
                timestamp = now - 1000 * 60 * 120,
                riskScore = 96,
                riskLevel = RiskLevel.HIGH,
                category = "OTP & Verification Phishing",
                reasons = listOf(
                    "🤖 Requests sensitive OTP code over chat",
                    "🤖 High pressure security alert language",
                    "👤 Sender is an unknown number"
                ),
                recommendation = "DO NOT SHARE OTP. This is a credential phishing attempt to hack into your Internshala or bank account. Block this sender immediately.",
                dos = listOf("Report and block this number immediately", "Check your active logins on the official website"),
                doNots = listOf("Share any OTP, password, or security code with this sender", "Click any link in the message")
            ),
            DetectionResult(
                id = UUID.randomUUID().toString(),
                sender = "Google Careers",
                message = "Hi, thank you for applying. Your resume has been shortlisted. We would like to schedule a 45-minute technical interview on Google Meet. Please select a slot here: meet.google.com/xyz-abc-123. Regards, Google Recruitment.",
                timestamp = now - 1000 * 60 * 300,
                riskScore = 12,
                riskLevel = RiskLevel.LOW,
                category = "Legitimate Recruiter Invite",
                reasons = listOf(
                    "✅ Standard application follow-up language",
                    "✅ Legitimate company invitation links (meet.google.com)",
                    "🟢 No fees or sensitive data requested"
                ),
                recommendation = "This message appears safe. Proceed with standard selection steps by clicking the Google Meet invite.",
                dos = listOf("Confirm your attendance slot on Google Calendar", "Prepare for the interview"),
                doNots = listOf("Share passwords or OTPs if asked in subsequent informal messages")
            )
        )
        mockList.forEach { result ->
            DetectionRepository.addDetection(app, result)
        }
        loadDetections()
    }

    private fun updateStats() {
        val total = _detections.value.size
        val threatCount = _detections.value.count { it.riskLevel == RiskLevel.HIGH || it.riskLevel == RiskLevel.MEDIUM }
        val highRiskCount = _detections.value.count { it.riskLevel == RiskLevel.HIGH }
        _stats.value = ThreatStats(total, threatCount, highRiskCount)
    }

    private val backStack = ArrayDeque<AppScreen>()

    fun navigateTo(screen: AppScreen, addToBackStack: Boolean = true) {
        val current = _currentScreen.value
        if (current == screen) return

        if (screen is AppScreen.Dashboard) {
            backStack.clear()
        } else if (addToBackStack && current !is AppScreen.Splash) {
            backStack.addLast(current)
            if (backStack.size > 20) backStack.removeFirst()
        }
        _currentScreen.value = screen
    }

    fun handleBack(): Boolean {
        if (backStack.isNotEmpty()) {
            val previous = backStack.removeLast()
            _currentScreen.value = previous
            return true
        } else if (_currentScreen.value !is AppScreen.Dashboard) {
            _currentScreen.value = AppScreen.Dashboard
            return true
        }
        return false
    }

    fun canGoBack(): Boolean {
        return _currentScreen.value !is AppScreen.Dashboard
    }

    fun toggleProtection() {
        setProtectionEnabled(!_protectionEnabled.value)
    }

    fun setProtectionEnabled(enabled: Boolean) {
        val app = getApplication<Application>()
        SettingsRepository.setProtectionEnabled(app, enabled)
        _protectionEnabled.value = enabled
    }

    fun setScanUnknownSendersOnly(enabled: Boolean) {
        val app = getApplication<Application>()
        SettingsRepository.setScanUnknownSendersOnly(app, enabled)
        _scanUnknownSendersOnly.value = enabled
    }

    fun setRealTimeAlerts(enabled: Boolean) {
        val app = getApplication<Application>()
        SettingsRepository.setRealTimeAlertsEnabled(app, enabled)
        _realTimeAlerts.value = enabled
    }

    fun setAiEnabled(enabled: Boolean) {
        val app = getApplication<Application>()
        SettingsRepository.setAiBackendEnabled(app, enabled)
        _aiEnabled.value = enabled
    }

    fun setBackendUrl(url: String) {
        val app = getApplication<Application>()
        SettingsRepository.setBackendUrl(app, url)
        _backendUrl.value = url
    }

    fun setGeminiApiKey(key: String) {
        val app = getApplication<Application>()
        SettingsRepository.setGeminiApiKey(app, key)
        _geminiApiKey.value = key
    }

    fun setRunInBackground(enabled: Boolean) {
        val app = getApplication<Application>()
        SettingsRepository.setRunInBackgroundEnabled(app, enabled)
        _runInBackground.value = enabled
        
        val intent = Intent(app, ShieldNotificationListener::class.java).apply {
            action = if (enabled) "ACTION_START_FOREGROUND" else "ACTION_STOP_FOREGROUND"
        }
        try {
            if (enabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                app.startForegroundService(intent)
            } else {
                app.startService(intent)
            }
        } catch (e: java.lang.Exception) {
            android.util.Log.e("ShieldViewModel", "Failed to start/stop listener service foreground state", e)
        }
    }

    fun clearHistory() {
        DetectionRepository.clearDetections(getApplication())
        loadDetections()
    }

    /**
     * Simulates an incoming WhatsApp notification for Demo / Hackathon mode.
     *
     * Executes analysis via [HybridNotificationAnalyzer] which attempts the AI Backend
     * server (`POST /api/analyze`) and seamlessly falls back to local rules if offline.
     */
    fun simulateNotification(
        sender: String,
        messageText: String,
        senderStatus: SenderStatus = SenderStatus.UNKNOWN
    ) {
        if (!_protectionEnabled.value) return

        val app = getApplication<Application>()

        when (senderStatus) {
            SenderStatus.KNOWN -> {
                val infoResult = DetectionResult(
                    id = UUID.randomUUID().toString(),
                    sender = sender,
                    message = messageText,
                    timestamp = System.currentTimeMillis(),
                    riskScore = 0,
                    riskLevel = RiskLevel.LOW,
                    category = "Saved Contact — Not Scanned",
                    reasons = listOf("✅ Sender is a saved contact", "🔒 Message not scanned to protect your privacy"),
                    recommendation = "InternShield only analyzes messages from unknown senders. This contact is saved and trusted.",
                    scoreBreakdown = emptyMap()
                )
                DetectionRepository.addDetection(app, infoResult)
                loadDetections()
                return
            }
            SenderStatus.UNAVAILABLE -> {
                val infoResult = DetectionResult(
                    id = UUID.randomUUID().toString(),
                    sender = sender,
                    message = messageText,
                    timestamp = System.currentTimeMillis(),
                    riskScore = 0,
                    riskLevel = RiskLevel.LOW,
                    category = "Status Unavailable — Not Scanned",
                    reasons = listOf("⚪ Sender status could not be determined", "📵 May be a group message or previews disabled"),
                    recommendation = "Could not determine if sender is saved. Enable WhatsApp notification previews for full protection.",
                    scoreBreakdown = emptyMap()
                )
                DetectionRepository.addDetection(app, infoResult)
                loadDetections()
                return
            }
            SenderStatus.UNKNOWN -> { /* proceed */ }
        }

        viewModelScope.launch {
            val notificationData = NotificationData(
                packageName = "com.whatsapp",
                senderTitle = sender,
                messageText = messageText,
                senderStatus = senderStatus,
                receivedAt = System.currentTimeMillis(),
                notificationKey = UUID.randomUUID().toString(),
                hasMessagePreview = true
            )

            val analysisResult = hybridAnalyzer.analyze(app, notificationData)

            val result = DetectionResult(
                id = UUID.randomUUID().toString(),
                sender = sender,
                message = messageText,
                timestamp = System.currentTimeMillis(),
                riskScore = analysisResult.riskScore,
                riskLevel = analysisResult.riskLevel,
                category = analysisResult.category,
                reasons = analysisResult.detectedSignals,
                recommendation = analysisResult.recommendation,
                scoreBreakdown = analysisResult.scoreBreakdown
            )

            DetectionRepository.addDetection(app, result)
            loadDetections()
        }
    }
}

