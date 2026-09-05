package com.internshield.app.model

/**
 * NotificationData represents a parsed notification received from a supported messaging app.
 *
 * This data model holds ONLY the information made publicly available by Android's
 * NotificationListenerService API — i.e., the notification title and text that is
 * already visible on the user's lock screen/status bar.
 *
 * We do NOT access:
 *   - WhatsApp's internal message database
 *   - WhatsApp's private content providers
 *   - Any encrypted or private data
 *   - Personal contacts or call logs
 *
 * Android Permission Required: BIND_NOTIFICATION_LISTENER_SERVICE
 * (Granted explicitly by the user in system Settings > Notification Access)
 */
data class NotificationData(

    /**
     * The Android package name of the app that posted this notification.
     * Example: "com.whatsapp" or "com.whatsapp.w4b" (WhatsApp Business)
     */
    val packageName: String,

    /**
     * The sender field extracted from the notification title.
     * For WhatsApp, this is typically the contact name or phone number
     * as shown in the notification — NOT from WhatsApp's internal database.
     * This may be null if the notification did not include a title.
     */
    val senderTitle: String?,

    /**
     * The message body extracted from the notification text.
     * For WhatsApp, this is a truncated preview of the message.
     * May be null if the app or user privacy settings suppressed the body.
     */
    val messageText: String?,

    /**
     * Whether the sender is UNKNOWN (unsaved number), KNOWN (saved contact),
     * or UNAVAILABLE (could not be determined).
     *
     * Determined by [com.internshield.app.analyzer.SenderStatusDetector] using a
     * heuristic that checks if the notification title is a phone number (UNKNOWN)
     * or a contact name (KNOWN). No private WhatsApp APIs are used.
     *
     * InternShield only analyzes messages from UNKNOWN senders.
     */
    val senderStatus: SenderStatus,

    /**
     * The Unix timestamp in milliseconds when the notification was posted.
     * This is the system time at the moment InternShield received the notification.
     */
    val receivedAt: Long,

    /**
     * A unique notification key assigned by the Android system.
     * This helps avoid duplicate processing of the same notification.
     */
    val notificationKey: String,

    /**
     * True if the user has WhatsApp message previews enabled.
     * If the user has disabled message previews, messageText will be null
     * or will contain a generic placeholder like "WhatsApp Message".
     */
    val hasMessagePreview: Boolean
)

