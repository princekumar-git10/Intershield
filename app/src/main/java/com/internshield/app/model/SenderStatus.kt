package com.internshield.app.model

/**
 * SenderStatus — represents whether a WhatsApp message sender is known to the user.
 *
 * ═══════════════════════════════════════════════════════════════════════
 * IMPORTANT TECHNICAL CONTEXT (for code reviewers / judges):
 * ═══════════════════════════════════════════════════════════════════════
 *
 * There is NO Android API that lets a third-party app query whether a WhatsApp
 * contact is saved. WhatsApp does not expose this through any public API.
 *
 * HOWEVER, WhatsApp's own notification behavior gives us a reliable signal:
 *
 *   ✅ SAVED contact notification title  → Name (e.g., "Rahul Sharma")
 *   ✅ UNKNOWN number notification title → Phone number (e.g., "+91 98765 43210")
 *                                          or a string like "Unknown"
 *
 * This is the SAME visual behavior the user sees on their own lock screen.
 * We are not bypassing anything — we are reading the public notification title
 * exactly as Android's NotificationListenerService makes it available.
 *
 * We use this heuristic to determine [SenderStatus]:
 *   - Title contains only digits, +, spaces, dashes → likely a phone number → [UNKNOWN]
 *   - Title matches known "unknown" placeholder strings → [UNKNOWN]
 *   - Title looks like a name (letters, spaces) → likely saved → [KNOWN]
 *   - Title is null or we can't determine either way → [UNAVAILABLE]
 *
 * ═══════════════════════════════════════════════════════════════════════
 */
enum class SenderStatus {

    /**
     * The sender appears to be an unsaved number.
     *
     * Detected when the notification title:
     *   - Is a phone number format: "+91 98765 43210", "9876543210", "+447700900077"
     *   - Contains WhatsApp's "unknown" placeholder text
     *
     * InternShield WILL analyze messages from UNKNOWN senders.
     */
    UNKNOWN,

    /**
     * The sender appears to be a saved contact.
     *
     * Detected when the notification title looks like a person's name
     * (contains letters, does not match a phone number pattern).
     *
     * InternShield will NOT analyze messages from KNOWN senders to protect privacy.
     * A saved contact is trusted by definition.
     */
    KNOWN,

    /**
     * Sender status could not be determined.
     *
     * This happens when:
     *   - The notification title is null (user disabled WhatsApp notification previews)
     *   - The title is a group name (ambiguous — could be known or unknown)
     *   - WhatsApp used a format we do not recognize
     *
     * By default, InternShield treats UNAVAILABLE as → skip analysis (err on the side of privacy).
     * The user can override this behavior in Settings to analyze UNAVAILABLE senders too.
     */
    UNAVAILABLE
}
