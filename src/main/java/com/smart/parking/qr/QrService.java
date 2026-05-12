package com.smart.parking.qr;

import com.google.zxing.*;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.Base64;

@Service
public class QrService {

    /**
     * Generates a QR code PNG as a Base64 string.
     * The QR content encodes: reservationId|userId|timestamp
     */
    public String generateQrBase64(Long reservationId, Long userId) throws Exception {
        String content = reservationId + "|" + userId + "|" + System.currentTimeMillis();
        QRCodeWriter writer = new QRCodeWriter();
        BitMatrix matrix = writer.encode(content, BarcodeFormat.QR_CODE, 300, 300);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(matrix, "PNG", baos);
        return Base64.getEncoder().encodeToString(baos.toByteArray());
    }

    /**
     * Generates a QR code as raw PNG bytes.
     * Used by QrController to return image/png response.
     */
    public byte[] generateQrBytes(Long reservationId, Long userId) throws Exception {
        String content = reservationId + "|" + userId + "|" + System.currentTimeMillis();
        QRCodeWriter writer = new QRCodeWriter();
        BitMatrix matrix = writer.encode(content, BarcodeFormat.QR_CODE, 300, 300);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(matrix, "PNG", baos);
        return baos.toByteArray();
    }

    /**
     * Verifies QR content format.
     * Returns true if the content matches the expected pattern.
     */
    public boolean verifyQrContent(String qrContent, Long expectedReservationId) {
        String[] parts = qrContent.split("\\|");
        if (parts.length < 2) return false;
        try {
            long resId = Long.parseLong(parts[0]);
            return resId == expectedReservationId;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}