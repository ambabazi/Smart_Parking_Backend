package com.smart.parking.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * NotificationService handles SMS and USSD notifications.
 * For MVP, uses logging. Production should integrate with Africa's Talking SDK.
 * REST API fallback: https://africastalking.com/sms/send
 */
@Service
@Slf4j
public class NotificationService {

    @Value("${africastalking.username:sandbox}")
    private String username;

    @Value("${africastalking.api-key:demo-key}")
    private String apiKey;

    @Value("${africastalking.sender-id:SmartPark}")
    private String senderId;

    /**
     * Send a booking confirmation SMS.
     * Call this from ReservationService after a booking is created.
     */
    @Async("smartParkingTaskExecutor")
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
    @Async("smartParkingTaskExecutor")
    public void sendOvertimeWarning(String phone, String parkingName) {
        String msg = String.format(
            "SmartPark: Your reservation at %s expires in 15 minutes.\n" +
            "Extend in the app to avoid overtime charges.", parkingName);
        sendSms(phone, msg);
    }

    /**
     * Notify driver of overtime charges.
     */
    @Async("smartParkingTaskExecutor")
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
    @Async("smartParkingTaskExecutor")
    public void notifyHost(String hostPhone, String driverName, int slots) {
        String msg = String.format(
            "SmartPark: %s booked %d slot(s) at your parking space.",
            driverName, slots);
        sendSms(hostPhone, msg);
    }

    @Async("smartParkingTaskExecutor")
    public void sendSms(String phone, String message) {
        // MVP: Log SMS. Production: integrate Africa's Talking SDK or REST API
        log.info("[SMS via {}] To: {} | Message: {}", senderId, phone, message);
    }
}
