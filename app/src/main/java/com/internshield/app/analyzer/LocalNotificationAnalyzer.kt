package com.internshield.app.analyzer

import android.util.Log
import com.internshield.app.model.NotificationData
import com.internshield.app.model.SenderStatus
import com.internshield.app.model.RiskLevel

/**
 * LocalNotificationAnalyzer performs entirely on-device, rule-based risk analysis
 * on WhatsApp notification content.
 *
 * IMPORTANT PRIVACY NOTE:
 * - This analyzer ONLY operates on notification text already visible on the user's screen.
 * - No data is sent to any external server or API in this implementation.
 * - No personal data is persisted beyond the in-memory detection result list.
 * - All analysis happens locally within the app process.
 *
 * This is a deterministic rule engine. The AI API integration is planned for the next phase.
 */
class LocalNotificationAnalyzer {

    companion object {
        private const val TAG = "LocalAnalyzer"

        // ─── Payment / fee signals ────────────────────────────────────────────────
        // These keywords strongly indicate a scam demanding money from the target.
        private val PAYMENT_KEYWORDS = listOf(
            "pay", "fee", "₹", "rs.", "rupee", "payment", "deposit",
            "registration fee", "security deposit", "advance", "transfer money",
            "send money", "upi", "paytm", "phonepay", "google pay"
        )

        // ─── Urgency / pressure signals ───────────────────────────────────────────
        // Scammers create artificial pressure to prevent the victim from thinking clearly.
        private val URGENCY_KEYWORDS = listOf(
            "today", "now", "immediately", "urgent", "asap", "hurry",
            "last chance", "limited time", "deadline", "within 24 hours",
            "don't miss", "act fast", "expire"
        )

        // ─── Recruitment scam signals ─────────────────────────────────────────────
        // Common phrasing used in fake internship/job offers.
        private val RECRUITMENT_KEYWORDS = listOf(
            "intern", "internship", "selected", "shortlisted", "offer letter",
            "joining letter", "job offer", "work from home", "part time",
            "earn per day", "earn from home", "₹ per day", "salary"
        )

        // ─── External redirect signals ────────────────────────────────────────────
        // Scammers frequently move victims to Telegram/other platforms to avoid tracking.
        private val REDIRECT_KEYWORDS = listOf(
            "telegram", "t.me/", "signal", "click here", "bit.ly", "tinyurl",
            "apply now at", "join group", "whatsapp group", "google form",
            "fill form", "apply link"
        )

        // ─── Suspicious URL fragments ─────────────────────────────────────────────
        // Misspelled brand domains are a classic phishing indicator.
        private val SUSPICIOUS_URL_FRAGMENTS = listOf(
            "google-hr", "amazon-intern", "tcs-recruit", "infosys-job",
            "myntra-hiring", "flipkart-career", ".xyz", ".tk", ".ml",
            "verification-pay", "confirm-seat"
        )

        // ─── Safe signal indicators ───────────────────────────────────────────────
        // Common patterns found in legitimate recruitment messages.
        private val SAFE_KEYWORDS = listOf(
            "interview scheduled", "meet.google.com", "zoom.us", "teams.microsoft",
            "your application", "thank you for applying", "careers page", "hr@"
        )

        // Score weights for each signal category
        private const val WEIGHT_PAYMENT = 30
        private const val WEIGHT_URGENCY = 20
        private const val WEIGHT_RECRUITMENT = 15
        private const val WEIGHT_REDIRECT = 25
        private const val WEIGHT_SUSPICIOUS_URL = 25
        private const val WEIGHT_UNKNOWN_SENDER = 20
        private const val MAX_SCORE = 100
    }

    /**
     * Main entry point: analyze a [NotificationData] object and produce a risk analysis result.
     *
     * SENDER STATUS GATE:
     * This method only runs full analysis for [SenderStatus.UNKNOWN] senders.
     * Messages from [SenderStatus.KNOWN] (saved contacts) are immediately returned
     * as IGNORED — we never process messages from people the user trusts.
     * [SenderStatus.UNAVAILABLE] senders are also skipped by default.
     *
     * @param notification The parsed notification data from the NotificationListenerService.
     * @return A [AnalysisResult] containing the risk score, signals detected, and recommendation.
     *
     * This method is crash-safe — null fields are handled gracefully throughout.
     */
    fun analyze(notification: NotificationData): AnalysisResult {
        Log.d(TAG, "Analyzing notification from: ${notification.packageName}, SenderStatus: ${notification.senderStatus}")

        // ── Step 0: Sender Status Gate ─────────────────────────────────────────────
        // Only UNKNOWN senders are analyzed. KNOWN = trusted contact, skip entirely.
        // UNAVAILABLE = ambiguous (e.g. group message), skip by default.
        when (notification.senderStatus) {
            SenderStatus.KNOWN -> {
                Log.d(TAG, "Sender is KNOWN (saved contact) — analysis skipped to protect privacy.")
                return AnalysisResult(
                    riskScore = 0,
                    riskLevel = RiskLevel.LOW,
                    category = "Saved Contact",
                    detectedSignals = listOf("✅ Sender is a saved contact — message not scanned"),
                    scoreBreakdown = emptyMap(),
                    recommendation = "This message is from a contact you have saved. InternShield does not scan messages from known contacts."
                )
            }
            SenderStatus.UNAVAILABLE -> {
                Log.d(TAG, "Sender status is UNAVAILABLE — analysis skipped.")
                return AnalysisResult(
                    riskScore = 0,
                    riskLevel = RiskLevel.LOW,
                    category = "Status Unavailable",
                    detectedSignals = listOf("⚪ Sender status could not be determined"),
                    scoreBreakdown = emptyMap(),
                    recommendation = "Could not determine if sender is saved. This may be a group message or notification previews may be disabled."
                )
            }
            SenderStatus.UNKNOWN -> {
                // Proceed to full analysis below
                Log.d(TAG, "Sender is UNKNOWN — proceeding with risk analysis.")
            }
        }

        // ── Step 1: Safely extract and normalize the message text ─────────────────
        // If the user disabled WhatsApp notification previews, text will be null.
        // In that case we have no text to analyze, so we return LOW risk by default.
        val rawText = notification.messageText?.lowercase()?.trim() ?: run {
            Log.d(TAG, "No message preview available — skipping analysis.")
            return AnalysisResult(
                riskScore = 0,
                riskLevel = RiskLevel.LOW,
                category = "No Preview Available",
                detectedSignals = emptyList(),
                scoreBreakdown = emptyMap(),
                recommendation = "WhatsApp notification previews are disabled. Enable them in WhatsApp Settings > Notifications to allow InternShield to scan messages."
            )
        }

        // ── Step 2: Guard against empty message text ───────────────────────────────
        if (rawText.isBlank()) {
            Log.d(TAG, "Empty notification text — returning LOW risk.")
            return AnalysisResult(
                riskScore = 0,
                riskLevel = RiskLevel.LOW,
                category = "Empty Message",
                detectedSignals = emptyList(),
                scoreBreakdown = emptyMap(),
                recommendation = "Message content was empty. No risk detected."
            )
        }

        // ── Step 3: Check for safe signals first ───────────────────────────────────
        // If the message contains clear positive/safe indicators, we short-circuit
        // and immediately classify it as LOW risk without further scoring.
        val foundSafeSignal = SAFE_KEYWORDS.any { rawText.contains(it) }
        if (foundSafeSignal) {
            Log.d(TAG, "Safe signals detected — LOW risk result.")
            return AnalysisResult(
                riskScore = 8,
                riskLevel = RiskLevel.LOW,
                category = "Legitimate Recruitment Message",
                detectedSignals = listOf("✅ Safe recruitment language detected"),
                scoreBreakdown = mapOf("Safe Signals" to 8),
                recommendation = "Message appears safe. Always verify opportunity details through official company channels."
            )
        }

        // ── Step 4: Run the signal detection engine ───────────────────────────────
        var cumulativeScore = 0
        val detectedSignals = mutableListOf<String>()
        val scoreBreakdown = mutableMapOf<String, Int>()

        // Unknown sender is always true here because the NotificationListenerService
        // only intercepts notifications from WhatsApp for unknown/unsaved numbers.
        // (In the full implementation, the sender status will be passed from the service.)
        detectedSignals.add("👤 Sender is not in your contacts")
        scoreBreakdown["Unknown Sender"] = WEIGHT_UNKNOWN_SENDER
        cumulativeScore += WEIGHT_UNKNOWN_SENDER

        // Payment signals
        val hasPayment = PAYMENT_KEYWORDS.any { rawText.contains(it) }
        if (hasPayment) {
            detectedSignals.add("💰 Payment or registration fee requested")
            scoreBreakdown["Payment Request"] = WEIGHT_PAYMENT
            cumulativeScore += WEIGHT_PAYMENT
            Log.d(TAG, "Signal detected: PAYMENT (+$WEIGHT_PAYMENT)")
        }

        // Urgency signals
        val hasUrgency = URGENCY_KEYWORDS.any { rawText.contains(it) }
        if (hasUrgency) {
            detectedSignals.add("⚠️ Urgent or high-pressure language used")
            scoreBreakdown["Urgent Language"] = WEIGHT_URGENCY
            cumulativeScore += WEIGHT_URGENCY
            Log.d(TAG, "Signal detected: URGENCY (+$WEIGHT_URGENCY)")
        }

        // Recruitment-related scam keywords
        val hasRecruitment = RECRUITMENT_KEYWORDS.any { rawText.contains(it) }
        if (hasRecruitment) {
            detectedSignals.add("🎓 Internship or job offer claim")
            scoreBreakdown["Recruitment Claim"] = WEIGHT_RECRUITMENT
            cumulativeScore += WEIGHT_RECRUITMENT
            Log.d(TAG, "Signal detected: RECRUITMENT (+$WEIGHT_RECRUITMENT)")
        }

        // External redirect (Telegram, shady links)
        val hasRedirect = REDIRECT_KEYWORDS.any { rawText.contains(it) }
        if (hasRedirect) {
            detectedSignals.add("🔗 Redirect to external app or suspicious link")
            scoreBreakdown["External Redirect"] = WEIGHT_REDIRECT
            cumulativeScore += WEIGHT_REDIRECT
            Log.d(TAG, "Signal detected: REDIRECT (+$WEIGHT_REDIRECT)")
        }

        // Suspicious misspelled brand URL fragments
        val hasSuspiciousUrl = SUSPICIOUS_URL_FRAGMENTS.any { rawText.contains(it) }
        if (hasSuspiciousUrl) {
            detectedSignals.add("🌐 Suspicious or spoofed brand URL detected")
            scoreBreakdown["Suspicious URL"] = WEIGHT_SUSPICIOUS_URL
            cumulativeScore += WEIGHT_SUSPICIOUS_URL
            Log.d(TAG, "Signal detected: SUSPICIOUS_URL (+$WEIGHT_SUSPICIOUS_URL)")
        }

        // ── Step 5: Cap the score at MAX_SCORE ─────────────────────────────────────
        val finalScore = minOf(cumulativeScore, MAX_SCORE)

        // ── Step 6: Classify into risk level ──────────────────────────────────────
        val riskLevel = when {
            finalScore >= 70 -> RiskLevel.HIGH
            finalScore >= 35 -> RiskLevel.MEDIUM
            else -> RiskLevel.LOW
        }

        // ── Step 7: Generate a category label ─────────────────────────────────────
        val category = when {
            hasPayment && hasRecruitment -> "Internship Registration Scam"
            hasPayment && hasRedirect -> "Prepaid Task / Fee Fraud"
            hasRedirect && hasRecruitment -> "Fake Job Offer"
            hasRecruitment -> "Unverified Job Offer"
            hasPayment -> "Payment Request Scam"
            else -> "Suspicious WhatsApp Message"
        }

        // ── Step 8: Generate actionable recommendation ─────────────────────────────
        val recommendation = buildRecommendation(
            riskLevel = riskLevel,
            hasPayment = hasPayment,
            hasRedirect = hasRedirect
        )

        val dos = mutableListOf<String>()
        val doNots = mutableListOf<String>()

        if (riskLevel == RiskLevel.HIGH) {
            if (hasPayment) {
                doNots.add("Pay registration fees, security deposits, or laptop charges")
                dos.add("Request a free alternative or official company verification details")
            }
            if (hasRedirect) {
                doNots.add("Click suspicious links or join unknown WhatsApp/Telegram groups")
                dos.add("Verify links using official security tools before clicking")
            }
            doNots.add("Share Aadhaar, PAN card, bank details, or OTPs")
            dos.add("Call the company's verified HR telephone number directly")
        } else if (riskLevel == RiskLevel.MEDIUM) {
            dos.add("Look up the sender's profile and company on LinkedIn")
            dos.add("Check the company's official website careers page")
            if (hasPayment) {
                doNots.add("Pay money or buy training/tools to secure an interview")
            }
            doNots.add("Share passwords, OTPs, or banking credentials")
        } else {
            dos.add("Verify opportunity details through official channels")
            doNots.add("Share sensitive personal information without verifying the source")
        }

        Log.i(TAG, "Analysis complete — Score: $finalScore, Level: $riskLevel, Category: $category")

        return AnalysisResult(
            riskScore = finalScore,
            riskLevel = riskLevel,
            category = category,
            detectedSignals = detectedSignals,
            scoreBreakdown = scoreBreakdown,
            recommendation = recommendation,
            dos = dos,
            doNots = doNots
        )
    }

    /**
     * Builds a human-readable, actionable recommendation based on detected signals.
     * The recommendation should always tell the student exactly what to do next.
     */
    private fun buildRecommendation(
        riskLevel: RiskLevel,
        hasPayment: Boolean,
        hasRedirect: Boolean
    ): String {
        return when (riskLevel) {
            RiskLevel.HIGH -> buildString {
                append("⚠️ HIGH RISK DETECTED. Do not:\n")
                if (hasPayment) append("• Pay any registration fee or deposit\n")
                if (hasRedirect) append("• Click links or join unknown groups\n")
                append("• Share your Aadhaar, PAN, or bank details\n\n")
                append("✅ Always verify by calling the company's official HR directly.")
            }
            RiskLevel.MEDIUM -> buildString {
                append("⚡ EXERCISE CAUTION. Verify this offer before responding:\n")
                append("• Check the company's official website careers page\n")
                append("• Look up the recruiter's LinkedIn profile\n")
                if (hasPayment) append("• Genuine companies never charge candidates\n")
            }
            RiskLevel.LOW -> "Message appears relatively safe, but always practice standard recruitment safety. Verify opportunities through official channels."
        }
    }
}

/**
 * The result produced by [LocalNotificationAnalyzer] after analyzing a notification.
 *
 * @property riskScore           Integer 0–100 representing threat probability.
 * @property riskLevel           Enum classification: LOW, MEDIUM, or HIGH.
 * @property category            Human-readable label for the scam type detected.
 * @property detectedSignals     List of individual signal descriptions with emoji.
 * @property scoreBreakdown      Map of signal name to individual contribution score.
 * @property recommendation      Actionable safety guidance for the student.
 */
data class AnalysisResult(
    val riskScore: Int,
    val riskLevel: RiskLevel,
    val category: String,
    val detectedSignals: List<String>,
    val scoreBreakdown: Map<String, Int>,
    val recommendation: String,
    val dos: List<String> = emptyList(),
    val doNots: List<String> = emptyList()
)
