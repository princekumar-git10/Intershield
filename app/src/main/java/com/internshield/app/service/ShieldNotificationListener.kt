package com.internshield.app.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import androidx.core.app.NotificationCompat
import com.internshield.app.analyzer.AnalysisResult
import com.internshield.app.analyzer.HybridNotificationAnalyzer
import com.internshield.app.analyzer.SenderStatusDetector
import com.internshield.app.model.DetectionRepository
import com.internshield.app.model.DetectionResult
import com.internshield.app.model.NotificationData
import com.internshield.app.model.SenderStatus
import com.internshield.app.model.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.util.ArrayList

/**
 * ShieldNotificationListener — 24/7 Persistent NotificationListenerService for WhatsApp Scam Defense.
 */
class ShieldNotificationListener : NotificationListenerService() {

    companion object {
        private const val TAG = "ShieldListener"

        /** WhatsApp package names we monitor. */
        private val WHATSAPP_PACKAGES = setOf(
            "com.whatsapp",
            "com.whatsapp.w4b",
            "com.whatsapp.clone",
            "com.gbwhatsapp"
        )

        private const val EXTRA_TITLE = "android.title"
        private const val EXTRA_TITLE_BIG = "android.title.big"
        private const val EXTRA_TEXT = "android.text"
        private const val EXTRA_BIG_TEXT = "android.bigText"
        private const val EXTRA_TEXT_LINES = "android.textLines"
        private const val EXTRA_MESSAGES = "android.messages"

        const val ACTION_THREAT_DETECTED = "com.internshield.app.THREAT_DETECTED"
        const val EXTRA_RISK_SCORE = "extra_risk_score"
        const val EXTRA_RISK_LEVEL = "extra_risk_level"
        const val EXTRA_CATEGORY = "extra_category"
        const val EXTRA_SENDER = "extra_sender"
        const val EXTRA_MESSAGE = "extra_message"
        const val EXTRA_SIGNALS = "extra_signals"
        const val EXTRA_RECOMMENDATION = "extra_recommendation"
        const val EXTRA_SENDER_STATUS = "extra_sender_status"

        /**
         * Rebinds the NotificationListenerService if Android or battery manager unbinds it.
         */
        fun ensureConnected(context: Context) {
            try {
                val component = ComponentName(context, ShieldNotificationListener::class.java)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    requestRebind(component)
                    Log.i(TAG, "Triggered requestRebind for ShieldNotificationListener.")
                }
            } catch (e: Exception) {
                Log.w(TAG, "requestRebind not available or failed: ${e.message}")
            }

            try {
                val pm = context.packageManager
                val component = ComponentName(context, ShieldNotificationListener::class.java)
                pm.setComponentEnabledSetting(
                    component,
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP
                )
                pm.setComponentEnabledSetting(
                    component,
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    PackageManager.DONT_KILL_APP
                )
                Log.i(TAG, "Toggled component to enforce active system binding.")
            } catch (e: Exception) {
                Log.e(TAG, "Failed component toggle", e)
            }
        }
    }

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val hybridAnalyzer = HybridNotificationAnalyzer()
    private val processedKeys = mutableSetOf<String>()

    // ─── Lifecycle ──────────────────────────────────────────────────────────────

    override fun onListenerConnected() {
        super.onListenerConnected()
        Log.i(TAG, "✅ InternShield Notification Listener connected successfully.")
        startForegroundServiceNotification()
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        processedKeys.clear()
        Log.w(TAG, "⚠️ InternShield Notification Listener disconnected — attempting immediate reconnect.")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            try {
                requestRebind(ComponentName(this, ShieldNotificationListener::class.java))
            } catch (e: Exception) {
                Log.e(TAG, "Rebind on disconnect failed", e)
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand received action: ${intent?.action}")
        startForegroundServiceNotification()
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        Log.w(TAG, "ShieldNotificationListener destroyed.")
    }

    // ─── Notification Processing ───────────────────────────────────────────────

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        try {
            if (sbn == null) return
            val packageName = sbn.packageName ?: return

            // Check if from WhatsApp
            val isWhatsApp = packageName in WHATSAPP_PACKAGES || packageName.contains("whatsapp", ignoreCase = true)
            if (!isWhatsApp) return

            // Skip group summary notifications without unique message body
            if (sbn.notification != null && (sbn.notification.flags and Notification.FLAG_GROUP_SUMMARY) != 0) {
                // If it's pure summary with no lines, skip
                val extras = sbn.notification.extras
                val lines = extras?.getCharSequenceArray(EXTRA_TEXT_LINES)
                if (lines == null || lines.isEmpty()) {
                    Log.d(TAG, "Skipping empty group summary header.")
                    return
                }
            }

            // Deduplication
            val notificationKey = sbn.key ?: "${sbn.id}_${sbn.postTime}"
            if (processedKeys.contains(notificationKey)) {
                return
            }
            processedKeys.add(notificationKey)
            if (processedKeys.size > 300) processedKeys.clear()

            val extras: Bundle = sbn.notification?.extras ?: return

            // 1. Extract Sender Title
            val senderTitle: String? = (
                extras.getCharSequence(EXTRA_TITLE)?.toString()
                    ?: extras.getCharSequence(EXTRA_TITLE_BIG)?.toString()
                )?.trim()?.takeIf { it.isNotBlank() }

            // 2. Extract Message Text (checking multiple Android Messaging extras)
            var extractedMessage: String? = (
                extras.getCharSequence(EXTRA_BIG_TEXT)?.toString()
                    ?: extras.getCharSequence(EXTRA_TEXT)?.toString()
                )?.trim()?.takeIf { it.isNotBlank() }

            // Fallback for multiline inbox style (e.g. WhatsApp multiple unread messages)
            if (extractedMessage.isNullOrBlank()) {
                val lines = extras.getCharSequenceArray(EXTRA_TEXT_LINES)
                if (lines != null && lines.isNotEmpty()) {
                    extractedMessage = lines.joinToString("\n") { it.toString() }.trim()
                }
            }

            // Fallback for MessagingStyle bundles
            if (extractedMessage.isNullOrBlank() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                @Suppress("DEPRECATION")
                val messages = extras.getParcelableArray(EXTRA_MESSAGES)
                if (messages != null && messages.isNotEmpty()) {
                    val lastMsg = messages.lastOrNull() as? Bundle
                    extractedMessage = lastMsg?.getCharSequence("text")?.toString()?.trim()
                }
            }

            if (senderTitle.isNullOrBlank() && extractedMessage.isNullOrBlank()) {
                Log.d(TAG, "Empty title and text — ignoring notification.")
                return
            }

            Log.i(TAG, "📩 Scanned WhatsApp notification -> Sender: '$senderTitle', Message: '$extractedMessage'")

            // 3. Sender Status Detection
            val senderStatus = SenderStatusDetector.detect(applicationContext, senderTitle, extractedMessage)
            Log.i(TAG, "SenderStatus: $senderStatus for sender: '$senderTitle'")

            val isProtectionEnabled = SettingsRepository.isProtectionEnabled(applicationContext)
            val scanUnknownOnly = SettingsRepository.isScanUnknownSendersOnly(applicationContext)

            if (!isProtectionEnabled) {
                Log.d(TAG, "Real-time protection is disabled by user.")
                return
            }

            // If user selected "Scan Unknown Senders Only", only skip when sender is DEFINITELY a known contact
            if (scanUnknownOnly && senderStatus == SenderStatus.KNOWN) {
                Log.i(TAG, "Sender '$senderTitle' is a verified saved contact — skipping to preserve privacy.")
                return
            }

            val notificationData = NotificationData(
                packageName = packageName,
                senderTitle = senderTitle ?: "Unknown Sender",
                messageText = extractedMessage ?: "(no text preview)",
                senderStatus = senderStatus,
                receivedAt = System.currentTimeMillis(),
                notificationKey = notificationKey,
                hasMessagePreview = !extractedMessage.isNullOrBlank()
            )

            // Async Hybrid AI Threat Analysis
            serviceScope.launch {
                try {
                    val result = hybridAnalyzer.analyze(applicationContext, notificationData)
                    Log.i(TAG, "🛡️ Analysis Complete: Category='${result.category}', Score=${result.riskScore}/100, Level=${result.riskLevel}")

                    val detectionResult = DetectionResult(
                        id = java.util.UUID.randomUUID().toString(),
                        sender = senderTitle ?: "Unknown WhatsApp Number",
                        message = extractedMessage ?: "(no message preview)",
                        timestamp = System.currentTimeMillis(),
                        riskScore = result.riskScore,
                        riskLevel = result.riskLevel,
                        category = result.category,
                        reasons = result.detectedSignals,
                        recommendation = result.recommendation,
                        scoreBreakdown = result.scoreBreakdown,
                        dos = result.dos,
                        doNots = result.doNots
                    )

                    // Always save detection to persistent local storage so it appears in Activity feed
                    DetectionRepository.addDetection(applicationContext, detectionResult)

                    // Broadcast to UI
                    broadcastDetection(
                        sender = senderTitle ?: "Unknown WhatsApp Number",
                        message = extractedMessage ?: "(no message preview)",
                        senderStatus = senderStatus,
                        result = result
                    )

                    // Post real-time heads-up notification alert if scam signals are found
                    val isRealTimeAlerts = SettingsRepository.isRealTimeAlertsEnabled(applicationContext)
                    if (isRealTimeAlerts && result.riskScore >= 30) {
                        showScamNotification(
                            context = applicationContext,
                            sender = senderTitle ?: "Unknown WhatsApp Number",
                            result = result
                        )
                    }

                } catch (e: Exception) {
                    Log.e(TAG, "Error during async hybrid analysis", e)
                }
            }

        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error in onNotificationPosted", e)
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        // No action
    }

    // ─── Foreground & Alert Notification ───────────────────────────────────────

    private fun startForegroundServiceNotification() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "internshield_background_defense"
        val channelName = "InternShield Active Defense"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_MIN
            ).apply {
                description = "Keeps InternShield active 24/7 to analyze unknown WhatsApp recruitment messages"
                setShowBadge(false)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(this, com.internshield.app.MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            1,
            intent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }
        )

        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_lock_idle_low_battery)
            .setContentTitle("🛡️ InternShield AI Guard Active")
            .setContentText("24/7 real-time WhatsApp scam protection running")
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setContentIntent(pendingIntent)
            .setOngoing(true)

        startForeground(999, builder.build())
        Log.i(TAG, "Persistent foreground service started.")
    }

    private fun showScamNotification(context: Context, sender: String, result: AnalysisResult) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "internshield_scam_alerts"
        val channelName = "InternShield Threat Alerts"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "High-priority alerts when a fake job or WhatsApp scam is detected"
                enableLights(true)
                lightColor = Color.RED
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(context, com.internshield.app.MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }
        )

        val alertEmoji = if (result.riskLevel == com.internshield.app.model.RiskLevel.HIGH) "🚨 HIGH SCAM RISK" else "⚠️ SUSPICIOUS OFFER"
        val notificationTitle = "$alertEmoji: $sender"
        val notificationText = "${result.category} • ${result.recommendation}"

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.stat_sys_warning)
            .setContentTitle(notificationTitle)
            .setContentText(notificationText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(notificationText))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        notificationManager.notify(sender.hashCode(), builder.build())
        Log.i(TAG, "Posted scam alert notification for sender: $sender")
    }

    private fun broadcastDetection(
        sender: String,
        message: String,
        senderStatus: SenderStatus,
        result: AnalysisResult
    ) {
        val intent = Intent(ACTION_THREAT_DETECTED).apply {
            `package` = packageName
            putExtra(EXTRA_SENDER, sender)
            putExtra(EXTRA_MESSAGE, message)
            putExtra(EXTRA_RISK_SCORE, result.riskScore)
            putExtra(EXTRA_RISK_LEVEL, result.riskLevel.name)
            putExtra(EXTRA_CATEGORY, result.category)
            putStringArrayListExtra(EXTRA_SIGNALS, ArrayList(result.detectedSignals))
            putExtra(EXTRA_RECOMMENDATION, result.recommendation)
            putExtra(EXTRA_SENDER_STATUS, senderStatus.name)
            putStringArrayListExtra("extra_dos", ArrayList(result.dos))
            putStringArrayListExtra("extra_do_nots", ArrayList(result.doNots))
        }
        sendBroadcast(intent)
    }
}
