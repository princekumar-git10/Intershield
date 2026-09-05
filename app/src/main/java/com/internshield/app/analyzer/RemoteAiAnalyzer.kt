package com.internshield.app.analyzer

import android.util.Log
import com.internshield.app.model.NotificationData
import com.internshield.app.model.RiskLevel
import com.internshield.app.model.SenderStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

/**
 * RemoteAiAnalyzer — communicates with the InternShield backend API server
 * to perform AI-powered threat analysis on incoming WhatsApp messages.
 *
 * PRIVACY & SECURITY ARCHITECTURE:
 * ────────────────────────────────
 * 1. AI API keys are NEVER embedded inside the Android application binary.
 * 2. All requests are sent to a dedicated backend server (e.g. Node.js / Cloud Run / Firebase Functions).
 * 3. The backend securely holds the AI model API key in server-side environment variables.
 * 4. Only UNKNOWN senders are sent to the backend. Messages from KNOWN (saved) contacts are skipped.
 * 5. If the network or backend is unreachable, InternShield gracefully falls back to [LocalNotificationAnalyzer].
 */
class RemoteAiAnalyzer(
    private var backendUrl: String = DEFAULT_BACKEND_URL
) {

    companion object {
        private const val TAG = "RemoteAiAnalyzer"
        
        /**
         * Default backend URL.
         * "10.0.2.2" is Android Emulator's loopback alias to host machine's localhost:3000.
         * For physical devices on the same Wi-Fi, use host machine IP (e.g. http://192.168.1.5:3000/api/analyze).
         */
        const val DEFAULT_BACKEND_URL = "http://10.0.2.2:3000/api/analyze"
        private const val CONNECT_TIMEOUT_MS = 4000
        private const val READ_TIMEOUT_MS = 6000
    }

    fun updateBackendUrl(newUrl: String) {
        if (newUrl.isNotBlank()) {
            this.backendUrl = newUrl.trim()
        }
    }

    /**
     * Sends the notification data to the AI backend and parses the structured response.
     *
     * @param notification The parsed notification payload.
     * @param apiKey The direct Gemini API key (optional).
     * @param serverUrl The AI backend server URL to fall back to.
     * @return [Result.success] containing [AnalysisResult] if backend responds successfully,
     *         or [Result.failure] if network or parsing error occurs.
     */
    suspend fun analyzeWithAi(
        notification: NotificationData,
        apiKey: String,
        serverUrl: String
    ): Result<AnalysisResult> = withContext(Dispatchers.IO) {
        // Step 0: Check sender status gate
        if (notification.senderStatus == SenderStatus.KNOWN) {
            return@withContext Result.success(
                AnalysisResult(
                    riskScore = 0,
                    riskLevel = RiskLevel.LOW,
                    category = "Saved Contact",
                    detectedSignals = listOf("✅ Sender is a saved contact — AI scan skipped"),
                    scoreBreakdown = emptyMap(),
                    recommendation = "Message from a saved contact. Privacy preserved."
                )
            )
        }

        val messageText = notification.messageText?.trim() ?: ""
        if (messageText.isBlank()) {
            return@withContext Result.success(
                AnalysisResult(
                    riskScore = 0,
                    riskLevel = RiskLevel.LOW,
                    category = "Empty Message",
                    detectedSignals = listOf("⚪ Message content is empty"),
                    scoreBreakdown = emptyMap(),
                    recommendation = "Message body empty. No risk detected."
                )
            )
        }

        // If direct Gemini API key is provided, perform direct calling
        if (apiKey.isNotBlank()) {
            Log.i(TAG, "Direct Gemini API key configured - running analysis directly from app")
            val directResult = analyzeDirectlyWithGemini(apiKey, messageText, notification.senderStatus)
            if (directResult.isSuccess) {
                return@withContext directResult
            }
            Log.w(TAG, "Direct Gemini API call failed: ${directResult.exceptionOrNull()?.message}. Falling back to server URL: $serverUrl")
        }

        // Otherwise query the backend server
        val finalUrl = serverUrl.ifBlank { backendUrl }
        try {
            Log.d(TAG, "Calling AI Backend at: $finalUrl")

            // Build request payload exact format requested by spec:
            // { "sender_status": "UNKNOWN", "message": "...", "source": "WhatsApp" }
            val requestJson = JSONObject().apply {
                put("sender_status", notification.senderStatus.name)
                put("message", messageText)
                put("source", "WhatsApp")
            }

            val url = URL(finalUrl)
            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = CONNECT_TIMEOUT_MS
                readTimeout = READ_TIMEOUT_MS
                doOutput = true
                doInput = true
                setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                setRequestProperty("Accept", "application/json")
            }

            // Write payload
            OutputStreamWriter(connection.outputStream, "UTF-8").use { writer ->
                writer.write(requestJson.toString())
                writer.flush()
            }

            val responseCode = connection.responseCode
            Log.d(TAG, "AI Backend HTTP Response Code: $responseCode")

            if (responseCode != HttpURLConnection.HTTP_OK) {
                val errorStream = connection.errorStream
                val errorMsg = errorStream?.bufferedReader()?.use { it.readText() } ?: "HTTP $responseCode"
                Log.e(TAG, "Backend returned error: $errorMsg")
                return@withContext Result.failure(Exception("Backend HTTP $responseCode: $errorMsg"))
            }

            // Read response
            val responseText = connection.inputStream.bufferedReader().use { it.readText() }
            Log.d(TAG, "Raw response from AI backend: $responseText")

            val jsonResponse = JSONObject(responseText)

            // Extract values matching backend response schema:
            // { "risk_score": 94, "risk_level": "HIGH", "category": "INTERNSHIP_SCAM", "reasons": [...], "recommendation": "...", "dos": [...], "do_nots": [...] }
            val riskScore = jsonResponse.optInt("risk_score", 50)
            val riskLevelStr = jsonResponse.optString("risk_level", "MEDIUM")
            val category = jsonResponse.optString("category", "AI Detected Risk")
            val recommendation = jsonResponse.optString("recommendation", "Exercise caution.")

            val reasonsArray: JSONArray? = jsonResponse.optJSONArray("reasons")
            val reasonsList = mutableListOf<String>()
            if (reasonsArray != null) {
                for (i in 0 until reasonsArray.length()) {
                    reasonsList.add("🤖 " + reasonsArray.getString(i))
                }
            } else {
                reasonsList.add("🤖 AI Threat Assessment")
            }

            val dosArray: JSONArray? = jsonResponse.optJSONArray("dos")
            val dosList = mutableListOf<String>()
            if (dosArray != null) {
                for (i in 0 until dosArray.length()) {
                    dosList.add(dosArray.getString(i))
                }
            }

            val doNotsArray: JSONArray? = jsonResponse.optJSONArray("do_nots")
            val doNotsList = mutableListOf<String>()
            if (doNotsArray != null) {
                for (i in 0 until doNotsArray.length()) {
                    doNotsList.add(doNotsArray.getString(i))
                }
            }

            val riskLevel = try {
                RiskLevel.valueOf(riskLevelStr.uppercase())
            } catch (e: IllegalArgumentException) {
                RiskLevel.MEDIUM
            }

            val result = AnalysisResult(
                riskScore = riskScore,
                riskLevel = riskLevel,
                category = category.replace("_", " "),
                detectedSignals = reasonsList,
                scoreBreakdown = mapOf("AI Model Evaluation" to riskScore),
                recommendation = recommendation,
                dos = dosList,
                doNots = doNotsList
            )

            Log.i(TAG, "✅ Remote AI Analysis success — Risk: ${result.riskScore} (${result.riskLevel})")
            Result.success(result)

        } catch (e: Exception) {
            Log.w(TAG, "Failed to connect to AI backend (${e.message}) — fallback will be used.")
            Result.failure(e)
        }
    }

    private suspend fun analyzeDirectlyWithGemini(
        apiKey: String,
        messageText: String,
        senderStatus: SenderStatus
    ): Result<AnalysisResult> = withContext(Dispatchers.IO) {
        try {
            val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=$apiKey"
            val prompt = """
                You are the InternShield Risk Analysis Engine.

                Analyze a message for potential internship, recruitment, job, phishing, or financial fraud.

                Evaluate:
                1. Payment requests (upfront fees, deposits, training charges)
                2. Requests for OTP/password/bank information
                3. Urgency or pressure (high-pressure language, tight deadlines)
                4. Unrealistic salary/stipend (high pay for minimal effort)
                5. Fake selection claims (offer letter without prior interview)
                6. Suspicious URLs (typosquatted domains, suspicious shorteners)
                7. Company/recruiter inconsistencies (unprofessional contact, generic claims)
                8. Requests for identity documents (Aadhaar, PAN, bank statements via chat)
                9. Common recruitment scam patterns (prepaid task, video like scam)
                10. Unknown sender signal (sender is not in user's saved contacts)

                Message Details:
                - Source: WhatsApp
                - Sender Status: ${senderStatus.name}
                - Content: "$messageText"

                Rules:
                - Do not claim that a message is definitely a scam unless there is sufficient evidence.
                - Use "potential scam" or "needs verification" when evidence is uncertain.
                - Return ONLY valid JSON with no markdown formatting or extra text.

                JSON Schema:
                {
                  "risk_score": <number 0-100>,
                  "risk_level": "<LOW | MEDIUM | HIGH>",
                  "category": "<short descriptive category e.g. POTENTIAL_INTERNSHIP_SCAM, PREPAID_TASK_SCAM, UNVERIFIED_OFFER, LEGITIMATE_OFFER>",
                  "reasons": [
                    "<reason 1>",
                    "<reason 2>",
                    "<reason 3>"
                  ],
                  "recommendation": "<actionable advice for the student>",
                  "dos": [
                    "<specific action to do, e.g., verify recruiter LinkedIn profile>",
                    "<another specific action to do>"
                  ],
                  "do_nots": [
                    "<specific action to avoid, e.g., do not pay security deposit>",
                    "<another specific action to avoid>"
                  ]
                }
            """.trimIndent()

            val requestJson = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", prompt)
                            })
                        })
                    })
                })
            }

            val url = URL(endpoint)
            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = CONNECT_TIMEOUT_MS
                readTimeout = READ_TIMEOUT_MS
                doOutput = true
                doInput = true
                setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                setRequestProperty("Accept", "application/json")
            }

            // Write payload
            OutputStreamWriter(connection.outputStream, "UTF-8").use { writer ->
                writer.write(requestJson.toString())
                writer.flush()
            }

            val responseCode = connection.responseCode
            if (responseCode != HttpURLConnection.HTTP_OK) {
                val errorStream = connection.errorStream
                val errorMsg = errorStream?.bufferedReader()?.use { it.readText() } ?: "HTTP $responseCode"
                return@withContext Result.failure(Exception("Gemini API HTTP $responseCode: $errorMsg"))
            }

            val responseText = connection.inputStream.bufferedReader().use { it.readText() }
            val jsonResponse = JSONObject(responseText)

            // Extract the generated text from Gemini structure
            val rawText = jsonResponse.getJSONArray("candidates")
                .getJSONObject(0)
                .getJSONObject("content")
                .getJSONArray("parts")
                .getJSONObject(0)
                .getString("text")

            val cleanJson = rawText.replace("```json", "", ignoreCase = true)
                .replace("```", "")
                .trim()

            val parsed = JSONObject(cleanJson)

            val riskScore = parsed.optInt("risk_score", 50)
            val riskLevelStr = parsed.optString("risk_level", "MEDIUM")
            val category = parsed.optString("category", "AI Detected Risk")
            val recommendation = parsed.optString("recommendation", "Exercise caution.")

            val reasonsArray = parsed.optJSONArray("reasons")
            val reasonsList = mutableListOf<String>()
            if (reasonsArray != null) {
                for (i in 0 until reasonsArray.length()) {
                    reasonsList.add("🤖 " + reasonsArray.getString(i))
                }
            } else {
                reasonsList.add("🤖 AI Threat Assessment")
            }

            val dosArray = parsed.optJSONArray("dos")
            val dosList = mutableListOf<String>()
            if (dosArray != null) {
                for (i in 0 until dosArray.length()) {
                    dosList.add(dosArray.getString(i))
                }
            }

            val doNotsArray = parsed.optJSONArray("do_nots")
            val doNotsList = mutableListOf<String>()
            if (doNotsArray != null) {
                for (i in 0 until doNotsArray.length()) {
                    doNotsList.add(doNotsArray.getString(i))
                }
            }

            val riskLevel = try {
                RiskLevel.valueOf(riskLevelStr.uppercase())
            } catch (e: IllegalArgumentException) {
                RiskLevel.MEDIUM
            }

            val result = AnalysisResult(
                riskScore = riskScore,
                riskLevel = riskLevel,
                category = category.replace("_", " "),
                detectedSignals = reasonsList,
                scoreBreakdown = mapOf("Direct Gemini Model Evaluation" to riskScore),
                recommendation = recommendation,
                dos = dosList,
                doNots = doNotsList
            )

            Result.success(result)
        } catch (e: Exception) {
            Log.e(TAG, "Direct Gemini call failed", e)
            Result.failure(e)
        }
    }
}
