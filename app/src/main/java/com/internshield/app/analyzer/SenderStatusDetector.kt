package com.internshield.app.analyzer

import android.content.Context
import android.util.Log
import com.internshield.app.model.SenderStatus

/**
 * SenderStatusDetector — reliably classifies WhatsApp notification senders as UNKNOWN vs KNOWN.
 */
object SenderStatusDetector {

    private const val TAG = "SenderStatusDetector"

    fun detect(context: Context, notificationTitle: String?, notificationText: String?): SenderStatus {
        return try {
            detectInternal(context, notificationTitle, notificationText)
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error in SenderStatusDetector — defaulting to UNKNOWN", e)
            SenderStatus.UNKNOWN
        }
    }

    private fun detectInternal(context: Context, title: String?, text: String?): SenderStatus {
        if (title.isNullOrBlank()) {
            Log.d(TAG, "Title is null/blank → UNKNOWN")
            return SenderStatus.UNKNOWN
        }

        val trimmedTitle = title.trim()

        // 1. Check if the contact exists in user's saved contacts address book
        val isSaved = ContactHelper.isContactSaved(context, trimmedTitle)
        if (isSaved) {
            Log.d(TAG, "Contact is verified saved in address book: '$trimmedTitle' → KNOWN")
            return SenderStatus.KNOWN
        }

        // 2. If it is not saved in contacts, it is an UNKNOWN sender (unregistered number or unsaved recruiter)
        Log.i(TAG, "Sender '$trimmedTitle' is NOT in contacts → UNKNOWN")
        return SenderStatus.UNKNOWN
    }
}
