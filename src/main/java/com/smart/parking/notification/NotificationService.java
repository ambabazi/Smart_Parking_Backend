package com.smart.parking.notification;

import com.africastalking.AfricasTalking;
import com.africastalking.SmsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.List;

@Service
@Slf4j
public class NotificationService {

    @Value("${africastalking.username}")
    private String username;

    @Value("${africastalking.api-key}")
    private String apiKey;

    @Value("${africastalking.sender-id}")
    private String senderId;

    private SmsService smsService;

    @PostConstruct
    public void init() {
        try {
            AfricasTalking.initialize(username, apiKey);
            smsService = AfricasTalking.getService(AfricasTalking.SERVICE_SMS);
            log.info("Africa's Talking SMS initialized ({})", username);
        } catch (Exception e) {
            log.warn("Africa's Talking init failed: {}. SMS disabled.", e.getMessage());
        }
    }

    /**
     * Send a booking confirmation SMS.
     * Call this from ReservationService after a booking is created.
     */
    public void sendBookingConfirmation(String phone, String parkingName,
                                        String startTime, int slots) {
        String msg = String.format(
            "SmartPark: Booking confirmed!\n" +
            "Location: %s\n" +
            "Start: %s\n" +
            "%d slot(s) reserved.\nSee app for QR code.",
            parkingName, startTime, slots);
        sendSms(phone, msg);
    }

    /**
     * Notify driver that time is almost up (overtime warning).
     */
    public void sendOvertimeWarning(String phone, String parkingName) {
        String msg = String.format(
            "SmartPark: Your reservation at %s expires in 15 minutes.\n" +
            "Extend in the app to avoid overtime charges.", parkingName);
        sendSms(phone, msg);
    }

    /**
     * Notify driver of overtime charges.
     */
    public void sendOvertimeCharge(String phone, String parkingName, double amount) {
        String msg = String.format(
            "SmartPark: Overtime at %s.\n" +
            "Additional charge: RWF %.0f.\nPlease vacate soon.",
            parkingName, amount);
        sendSms(phone, msg);
    }

    /**
     * Notify host that a driver has booked their space.
     */
    public void notifyHost(String hostPhone, String driverName, int slots) {
        String msg = String.format(
            "SmartPark: %s booked %d slot(s) at your parking space.",
            driverName, slots);
        sendSms(hostPhone, msg);
    }

    private void sendSms(String phone, String message) {
        if (smsService == null) {
            log.info("[SMS MOCK] To: {} | {}", phone, message);
            return;
        }
        try {
            smsService.send(message, senderId, List.of(phone));
            log.info("SMS sent to {}", phone);
        } catch (Exception e) {
            log.error("SMS failed to {}: {}", phone, e.getMessage());
        }
    }
}
