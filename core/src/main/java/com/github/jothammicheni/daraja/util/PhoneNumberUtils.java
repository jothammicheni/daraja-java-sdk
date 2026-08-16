package com.github.jothammicheni.daraja.util;

import java.util.regex.Pattern;

/**
 * Utility class for validating and normalizing Kenyan phone numbers.
 * Supports all common Kenyan formats:
 * - +2547XXXXXXXX
 * - 2547XXXXXXXX
 * - 07XXXXXXXX
 * - 01XXXXXXXX (Safaricom 01xxx numbers)
 * - 7XXXXXXXX (without leading 0)
 */
public final class PhoneNumberUtils {

    private static final Pattern VALID_PHONE_PATTERN = Pattern.compile(
            "^(\\+?254|0)?[17]\\d{8}$"
    );

    private PhoneNumberUtils() {
        // Utility class - prevent instantiation
    }

    /**
     * Validates if a phone number is a valid Kenyan M-Pesa number.
     *
     * @param phoneNumber The phone number to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidKenyanPhone(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isBlank()) {
            return false;
        }

        // Remove all spaces, dashes, parentheses, dots
        String cleaned = phoneNumber.replaceAll("[\\s\\-().]", "");

        // Must start with +254, 254, 0, or 1/7
        return VALID_PHONE_PATTERN.matcher(cleaned).matches();
    }

    /**
     * Normalizes a Kenyan phone number to M-Pesa format.
     * Converts any valid format to 2547XXXXXXXX.
     *
     * @param phoneNumber The phone number to normalize
     * @return Normalized phone number in format 2547XXXXXXXX
     * @throws IllegalArgumentException if the phone number is invalid
     */
    public static String normalizeKenyanPhone(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isBlank()) {
            throw new IllegalArgumentException("Phone number cannot be null or empty");
        }

        // Remove all non-digit characters except '+'
        String cleaned = phoneNumber.replaceAll("[^\\d+]", "");

        // Remove leading '+'
        if (cleaned.startsWith("+")) {
            cleaned = cleaned.substring(1);
        }

        // Handle country code: if it starts with 254, remove it
        if (cleaned.startsWith("254")) {
            cleaned = cleaned.substring(3);
        } else if (cleaned.startsWith("0")) {
            // Remove leading '0'
            cleaned = cleaned.substring(1);
        }

        // Handle a leftover trunk '0' after stripping the country code
        // (e.g. +2540712345678 -> "254" was stripped, leaving "0712345678")
        if (cleaned.length() > 9 && cleaned.startsWith("0")) {
            cleaned = cleaned.substring(1);
        }

        // If the number is still longer than 9 digits, take the FIRST 9 digits.
        // Anything after a valid 9-digit subscriber number is treated as noise
        // (e.g. accidental extra digits appended to the end).
        // This handles cases like +2547123456789012 -> 712345678 -> 254712345678
        if (cleaned.length() > 9) {
            cleaned = cleaned.substring(0, 9);
        }

        // Validate length
        if (cleaned.length() < 9) {
            throw new IllegalArgumentException("Invalid phone number length: " + phoneNumber);
        }

        // Ensure it starts with 7 or 1 (Safaricom prefixes)
        if (!cleaned.startsWith("7") && !cleaned.startsWith("1")) {
            throw new IllegalArgumentException("Invalid Safaricom number: " + phoneNumber);
        }

        // Add '254' prefix
        return "254" + cleaned;
    }

    /**
     * Detects the phone number type for logging/audit purposes.
     *
     * @param phoneNumber The phone number to detect
     * @return The phone type (SAFARICOM_PREPAID, SAFARICOM_POSTPAID, UNKNOWN)
     */
    public static PhoneType detectPhoneType(String phoneNumber) {
        // Handle null/empty first
        if (phoneNumber == null || phoneNumber.isBlank()) {
            return PhoneType.INVALID;
        }

        try {
            // Check if it's a valid Safaricom number
            if (!isValidKenyanPhone(phoneNumber)) {
                // Check if it's an Airtel or other network (starts with 5)
                String cleaned = phoneNumber.replaceAll("[^\\d]", "");
                if (cleaned.startsWith("5") || cleaned.contains("5")) {
                    return PhoneType.UNKNOWN;
                }
                return PhoneType.INVALID;
            }

            String normalized = normalizeKenyanPhone(phoneNumber);
            if (normalized.startsWith("2547")) {
                return PhoneType.SAFARICOM_PREPAID;
            } else if (normalized.startsWith("2541")) {
                return PhoneType.SAFARICOM_POSTPAID;
            }
            return PhoneType.UNKNOWN;
        } catch (IllegalArgumentException e) {
            // Check if it's an Airtel or other network
            if (phoneNumber != null) {
                String cleaned = phoneNumber.replaceAll("[^\\d]", "");
                if (cleaned.startsWith("5") || cleaned.contains("5")) {
                    return PhoneType.UNKNOWN;
                }
            }
            return PhoneType.INVALID;
        }
    }

    /**
     * Masks a phone number for logging (shows only last 4 digits).
     *
     * @param phoneNumber The phone number to mask
     * @return Masked phone number (e.g., ******4149)
     */
    public static String maskPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isBlank()) {
            return "****";
        }
        String normalized = phoneNumber.replaceAll("[^\\d]", "");
        if (normalized.length() < 4) {
            return "****";
        }
        return "******" + normalized.substring(normalized.length() - 4);
    }

    /**
     * Phone types supported by Safaricom.
     */
    public enum PhoneType {
        SAFARICOM_PREPAID("Prepaid"),
        SAFARICOM_POSTPAID("Postpaid"),
        UNKNOWN("Unknown"),
        INVALID("Invalid");

        private final String displayName;

        PhoneType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}