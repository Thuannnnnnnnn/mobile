package com.example.midterm.utils;

import android.graphics.Bitmap;
import android.graphics.Color;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class QRCodeUtils {

    private static final int DEFAULT_QR_SIZE = 512;

    /**
     * Generate a unique QR code string
     */
    public static String generateQRCodeString() {
        return UUID.randomUUID().toString();
    }

    /**
     * Generate QR code bitmap from string
     */
    public static Bitmap generateQRCode(String content) {
        return generateQRCode(content, DEFAULT_QR_SIZE);
    }

    /**
     * Generate QR code bitmap with custom size
     */
    public static Bitmap generateQRCode(String content, int size) {
        try {
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
            hints.put(EncodeHintType.MARGIN, 1);

            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, size, size, hints);

            int width = bitMatrix.getWidth();
            int height = bitMatrix.getHeight();
            int[] pixels = new int[width * height];

            for (int y = 0; y < height; y++) {
                int offset = y * width;
                for (int x = 0; x < width; x++) {
                    pixels[offset + x] = bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE;
                }
            }

            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            bitmap.setPixels(pixels, 0, width, 0, 0, width, height);

            return bitmap;
        } catch (WriterException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Generate QR code with custom colors
     */
    public static Bitmap generateQRCode(String content, int size, int foregroundColor, int backgroundColor) {
        try {
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
            hints.put(EncodeHintType.MARGIN, 1);

            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, size, size, hints);

            int width = bitMatrix.getWidth();
            int height = bitMatrix.getHeight();
            int[] pixels = new int[width * height];

            for (int y = 0; y < height; y++) {
                int offset = y * width;
                for (int x = 0; x < width; x++) {
                    pixels[offset + x] = bitMatrix.get(x, y) ? foregroundColor : backgroundColor;
                }
            }

            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            bitmap.setPixels(pixels, 0, width, 0, 0, width, height);

            return bitmap;
        } catch (WriterException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Generate ticket QR code with event and ticket info
     */
    public static String generateTicketQRContent(int eventId, int ticketId, String ticketCode) {
        return String.format("EVT:%d|TKT:%d|CODE:%s", eventId, ticketId, ticketCode);
    }

    /**
     * Parse ticket QR content
     */
    public static TicketQRData parseTicketQR(String qrContent) {
        try {
            String[] parts = qrContent.split("\\|");
            if (parts.length >= 3) {
                int eventId = Integer.parseInt(parts[0].replace("EVT:", ""));
                int ticketId = Integer.parseInt(parts[1].replace("TKT:", ""));
                String code = parts[2].replace("CODE:", "");
                return new TicketQRData(eventId, ticketId, code);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Data class for parsed ticket QR
     */
    public static class TicketQRData {
        public int eventId;
        public int ticketId;
        public String code;

        public TicketQRData(int eventId, int ticketId, String code) {
            this.eventId = eventId;
            this.ticketId = ticketId;
            this.code = code;
        }
    }
}
