package com.example.midterm.utils;

import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class OTPUtils {

    private static final SecureRandom random = new SecureRandom();
    private static final int OTP_LENGTH = 6;
    private static final int OTP_EXPIRY_MINUTES = 5;

    /**
     * Generate a random 6-digit OTP code
     */
    public static String generateOTP() {
        StringBuilder otp = new StringBuilder();
        for (int i = 0; i < OTP_LENGTH; i++) {
            otp.append(random.nextInt(10));
        }
        return otp.toString();
    }

    /**
     * Get expiry time for OTP (current time + 5 minutes)
     */
    public static String getExpiryTime() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, OTP_EXPIRY_MINUTES);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return sdf.format(calendar.getTime());
    }

    /**
     * Get current timestamp
     */
    public static String getCurrentTimestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date());
    }

    /**
     * Check if OTP is expired
     */
    public static boolean isExpired(String expiryTime) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            Date expiry = sdf.parse(expiryTime);
            Date now = new Date();
            return now.after(expiry);
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * Verify OTP code
     */
    public static boolean verifyOTP(String inputOTP, String storedOTP, String expiryTime) {
        if (inputOTP == null || storedOTP == null || expiryTime == null) {
            return false;
        }

        if (isExpired(expiryTime)) {
            return false;
        }

        return inputOTP.equals(storedOTP);
    }

    /**
     * Generate verification token for email links
     */
    public static String generateToken() {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        StringBuilder token = new StringBuilder();
        for (byte b : bytes) {
            token.append(String.format("%02x", b));
        }
        return token.toString();
    }

    /**
     * Get remaining time in seconds
     */
    public static long getRemainingSeconds(String expiryTime) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            Date expiry = sdf.parse(expiryTime);
            Date now = new Date();
            long diff = expiry.getTime() - now.getTime();
            return Math.max(0, diff / 1000);
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Format remaining time as MM:SS
     */
    public static String formatRemainingTime(long seconds) {
        long minutes = seconds / 60;
        long secs = seconds % 60;
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, secs);
    }
}
