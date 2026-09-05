package com.internshield.app.analyzer

import android.content.Context
import android.net.Uri
import android.provider.ContactsContract
import androidx.core.content.ContextCompat
import android.Manifest
import android.content.pm.PackageManager
import android.util.Log

object ContactHelper {
    private const val TAG = "ContactHelper"

    /**
     * Checks if the sender name or phone number exists in the device's contacts database.
     * Requires READ_CONTACTS permission.
     */
    fun isContactSaved(context: Context, title: String?): Boolean {
        if (title.isNullOrBlank()) return false
        
        // 1. Check runtime permission
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) 
            != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "READ_CONTACTS permission not granted — treating as unsaved sender.")
            // Without contacts permission, we cannot verify it is in contacts
            return false
        }

        return try {
            val trimmedTitle = title.trim()
            // 2. If it matches phone number pattern, check by phone lookup
            if (looksLikePhoneNumber(trimmedTitle)) {
                isPhoneNumberSaved(context, trimmedTitle)
            } else {
                // 3. Otherwise check by display name
                isDisplayNameSaved(context, trimmedTitle)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking contacts database — treating as unsaved sender", e)
            false
        }
    }

    private fun looksLikePhoneNumber(text: String): Boolean {
        val digitsCount = text.count { it.isDigit() }
        return digitsCount >= 7 && text.all { it.isDigit() || it.isWhitespace() || it == '+' || it == '-' || it == '(' || it == ')' }
    }

    private fun isPhoneNumberSaved(context: Context, phoneNumber: String): Boolean {
        val cleanNumber = phoneNumber.replace(Regex("""[^\d+]"""), "")
        val lookupUri = Uri.withAppendedPath(
            ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
            Uri.encode(if (cleanNumber.isNotBlank()) cleanNumber else phoneNumber)
        )
        val projection = arrayOf(ContactsContract.PhoneLookup.DISPLAY_NAME)
        context.contentResolver.query(lookupUri, projection, null, null, null)?.use { cursor ->
            if (cursor.count > 0) {
                Log.d(TAG, "Phone number '$phoneNumber' found in device contacts.")
                return true
            }
        }
        return false
    }

    private fun isDisplayNameSaved(context: Context, name: String): Boolean {
        val uri = ContactsContract.Contacts.CONTENT_URI
        val projection = arrayOf(ContactsContract.Contacts.DISPLAY_NAME)
        val selection = "${ContactsContract.Contacts.DISPLAY_NAME} = ? COLLATE NOCASE"
        val selectionArgs = arrayOf(name)
        context.contentResolver.query(uri, projection, selection, selectionArgs, null)?.use { cursor ->
            if (cursor.count > 0) {
                Log.d(TAG, "Display name '$name' found in device contacts.")
                return true
            }
        }
        return false
    }
}
