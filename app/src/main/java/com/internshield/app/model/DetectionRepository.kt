package com.internshield.app.model

import android.content.Context
import android.util.Log
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.UUID

object DetectionRepository {
    private const val TAG = "DetectionRepository"
    private const val FILE_NAME = "detections_v1.json"
    
    private val lock = Any()
    
    fun getDetections(context: Context): List<DetectionResult> = synchronized(lock) {
        val file = File(context.filesDir, FILE_NAME)
        if (!file.exists()) {
            // Return default mock data and save it
            val defaultData = getMockData()
            saveDetectionsInternal(file, defaultData)
            return defaultData
        }
        
        try {
            val jsonStr = file.readText()
            val jsonArray = JSONArray(jsonStr)
            val list = mutableListOf<DetectionResult>()
            for (i in 0 until jsonArray.length()) {
                try {
                    list.add(fromJSONObject(jsonArray.getJSONObject(i)))
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing item $i", e)
                }
            }
            return list
        } catch (e: Exception) {
            Log.e(TAG, "Error reading file, returning empty", e)
            return emptyList()
        }
    }
    
    fun addDetection(context: Context, result: DetectionResult) = synchronized(lock) {
        val current = getDetections(context).toMutableList()
        // Deduplicate: ignore identical messages from the same sender in short span (< 10 seconds)
        val duplicate = current.any { 
            it.message == result.message && 
            it.sender == result.sender && 
            Math.abs(it.timestamp - result.timestamp) < 10000 
        }
        if (duplicate) {
            Log.d(TAG, "Duplicate detection found - skipping save")
            return
        }
        
        current.add(0, result) // Prepend
        val file = File(context.filesDir, FILE_NAME)
        saveDetectionsInternal(file, current)
    }
    
    fun clearDetections(context: Context) = synchronized(lock) {
        val file = File(context.filesDir, FILE_NAME)
        saveDetectionsInternal(file, emptyList())
    }
    
    private fun saveDetectionsInternal(file: File, list: List<DetectionResult>) {
        try {
            val jsonArray = JSONArray()
            list.forEach { jsonArray.put(toJSONObject(it)) }
            file.writeText(jsonArray.toString(2))
        } catch (e: Exception) {
            Log.e(TAG, "Error saving detections", e)
        }
    }
    
    private fun toJSONObject(result: DetectionResult): JSONObject {
        return JSONObject().apply {
            put("id", result.id)
            put("sender", result.sender)
            put("message", result.message)
            put("timestamp", result.timestamp)
            put("riskScore", result.riskScore)
            put("riskLevel", result.riskLevel.name)
            put("category", result.category)
            put("reasons", JSONArray(result.reasons))
            put("recommendation", result.recommendation)
            put("dos", JSONArray(result.dos))
            put("doNots", JSONArray(result.doNots))
            
            val breakdownObj = JSONObject()
            result.scoreBreakdown.forEach { (k, v) -> breakdownObj.put(k, v) }
            put("scoreBreakdown", breakdownObj)
            
            put("source", result.source)
        }
    }
    
    private fun fromJSONObject(obj: JSONObject): DetectionResult {
        val id = obj.getString("id")
        val sender = obj.getString("sender")
        val message = obj.getString("message")
        val timestamp = obj.getLong("timestamp")
        val riskScore = obj.getInt("riskScore")
        val riskLevel = RiskLevel.valueOf(obj.getString("riskLevel"))
        val category = obj.getString("category")
        
        val reasonsArray = obj.getJSONArray("reasons")
        val reasons = mutableListOf<String>()
        for (i in 0 until reasonsArray.length()) {
            reasons.add(reasonsArray.getString(i))
        }
        
        val recommendation = obj.getString("recommendation")

        val dosArray = obj.optJSONArray("dos")
        val dos = mutableListOf<String>()
        if (dosArray != null) {
            for (i in 0 until dosArray.length()) {
                dos.add(dosArray.getString(i))
            }
        }

        val doNotsArray = obj.optJSONArray("doNots")
        val doNots = mutableListOf<String>()
        if (doNotsArray != null) {
            for (i in 0 until doNotsArray.length()) {
                doNots.add(doNotsArray.getString(i))
            }
        }
        
        val breakdownObj = obj.optJSONObject("scoreBreakdown")
        val scoreBreakdown = mutableMapOf<String, Int>()
        if (breakdownObj != null) {
            val keys = breakdownObj.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                scoreBreakdown[key] = breakdownObj.getInt(key)
            }
        }
        
        val source = obj.optString("source", "WhatsApp")
        
        return DetectionResult(
            id = id,
            sender = sender,
            message = message,
            timestamp = timestamp,
            riskScore = riskScore,
            riskLevel = riskLevel,
            category = category,
            reasons = reasons,
            recommendation = recommendation,
            scoreBreakdown = scoreBreakdown,
            source = source,
            dos = dos,
            doNots = doNots
        )
    }
    
    private fun getMockData(): List<DetectionResult> {
        val now = System.currentTimeMillis()
        return listOf(
            DetectionResult(
                id = UUID.randomUUID().toString(),
                sender = "+91 98765 43210",
                message = "Congratulations! You are selected for the Google Internship Program. Pay ₹999 registration fee today to confirm your seat and receive your laptop.",
                timestamp = now - 1000 * 60 * 15,
                riskScore = 94,
                riskLevel = RiskLevel.HIGH,
                category = "Internship Registration Scam",
                reasons = listOf(
                    "🤖 Upfront payment requested",
                    "🤖 Urgent language",
                    "🤖 Unverified selection claim",
                    "👤 Sender is not in your contact list"
                ),
                recommendation = "Do not pay any fee. Verified companies never ask for money or security deposits during selection. Verify directly on the official careers page.",
                scoreBreakdown = mapOf(
                    "Unknown Sender" to 20,
                    "Payment Request" to 30,
                    "Urgent Language" to 20,
                    "Suspicious Claim" to 24
                ),
                dos = listOf(
                    "Visit the company's official website directly",
                    "Verify the recruiter's identity on LinkedIn"
                ),
                doNots = listOf(
                    "Pay any registration fee, seat confirmation fee, or laptop charge",
                    "Share credentials or personal documents"
                )
            ),
            DetectionResult(
                id = UUID.randomUUID().toString(),
                sender = "+44 7700 900077",
                message = "Earn ₹5000/day by just liking YouTube videos. Work from home part-time. Contact on Telegram @YoutubeEarners right now!",
                timestamp = now - 1000 * 60 * 120,
                riskScore = 98,
                riskLevel = RiskLevel.HIGH,
                category = "Prepaid Task/Job Scam",
                reasons = listOf(
                    "🤖 Unrealistic stipend/salary promised",
                    "🤖 External redirect to Telegram or WhatsApp group",
                    "🤖 High pressure urgency"
                ),
                recommendation = "This is a classic 'prepaid task scam'. Do not message them on Telegram. Block this contact immediately.",
                scoreBreakdown = mapOf(
                    "Unknown Sender" to 20,
                    "External Redirect" to 25,
                    "Unrealistic Stipend" to 25,
                    "Suspicious Rewards" to 28
                ),
                dos = listOf(
                    "Search online for prepaid task scams",
                    "Block this sender on WhatsApp"
                ),
                doNots = listOf(
                    "Message them on Telegram or join unofficial groups",
                    "Perform tasks in expectation of quick money"
                )
            ),
            DetectionResult(
                id = UUID.randomUUID().toString(),
                sender = "HR Solutions",
                message = "We have opened a training internship for Web Development. A fee of ₹2000 is required for certified program tools.",
                timestamp = now - 1000 * 60 * 480,
                riskScore = 55,
                riskLevel = RiskLevel.MEDIUM,
                category = "Paid Training Program",
                reasons = listOf(
                    "💰 Fee required for training/tools",
                    "💼 Genuine looking but charges students"
                ),
                recommendation = "Exercise caution. Look up reviews for 'HR Solutions' online. Check if the tools can be downloaded for free.",
                scoreBreakdown = mapOf(
                    "Payment Request" to 30,
                    "Need Verification" to 25
                ),
                dos = listOf(
                    "Check if the training tools can be obtained for free",
                    "Verify reviews of HR Solutions on Google or LinkedIn"
                ),
                doNots = listOf(
                    "Pay upfront for the certified program tools without research"
                )
            ),
            DetectionResult(
                id = UUID.randomUUID().toString(),
                sender = "Infosys Careers",
                message = "Thank you for applying. Please check your email for the test link of your system engineer profile.",
                timestamp = now - 1000 * 60 * 1440,
                riskScore = 12,
                riskLevel = RiskLevel.LOW,
                category = "Verified recruitment info",
                reasons = listOf(
                    "🟢 Standard application follow-up",
                    "🟢 No suspicious requests detected"
                ),
                recommendation = "This message appears safe. Proceed with standard verification by checking the official Infosys careers portal.",
                scoreBreakdown = mapOf(
                    "Safe Signals" to 12
                ),
                dos = listOf(
                    "Check your email and the official Infosys careers portal",
                    "Proceed with standard verification"
                ),
                doNots = listOf(
                    "Share OTP or system password if prompted via test link"
                )
            )
        )
    }
}
