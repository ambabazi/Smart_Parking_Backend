package com.smart.parking.notification;

import com.smart.parking.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


/**
 * USSD Controller — handles Africa's Talking USSD callbacks.
 * This allows drivers WITHOUT smartphones to book parking.
 *
 * Flow:
 *   *384# → Welcome → 1 (View Nearby) → 1 (BK Arena) → 1 slot → Confirm → Done
 *
 * Africa's Talking sends POST to /api/ussd with:
 *   sessionId, phoneNumber, networkCode, serviceCode, text
 */
@RestController
@RequestMapping("/api/ussd")
@RequiredArgsConstructor
@Slf4j
@Validated
public class UssdController {

    private final NotificationService notificationService;

    @Value("${app.ussd.service-codes:*384#,*385#,*386#}")
    private String configuredServiceCodes;

    @PostMapping(path = "/session", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
                 produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> handleUssdSession(
            @RequestParam String sessionId,
            @RequestParam String phoneNumber,
            @RequestParam String serviceCode,
            @RequestParam(defaultValue = "") String text) {
        return handleUssd(sessionId, phoneNumber, serviceCode, text);
    }

    @PostMapping(consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
                 produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> handleUssd(
            @RequestParam String sessionId,
            @RequestParam String phoneNumber,
            @RequestParam String serviceCode,
            @RequestParam(defaultValue = "") String text) {

        log.info("USSD: session={} phone={} text={}", sessionId, phoneNumber, text);

        if (!isAcceptedServiceCode(serviceCode)) {
            return ResponseEntity.ok("END Invalid USSD service code. Please dial one of the configured short codes.");
        }

        String response;
        String[] parts = text.split("\\*");
        int level = parts.length;

        if (text.isEmpty()) {
            // Level 0 — Main menu
            response = "CON Welcome to SmartPark Kigali\n" +
                       "1. View nearby parking\n" +
                       "2. My reservations\n" +
                       "3. Exit";
        } else if (text.equals("1")) {
            // Level 1 — Show top 3 nearby spaces (hardcoded for MVP)
            response = "CON Nearby Parking Spaces:\n" +
                       "1. BK Arena (RWF 500/hr) 3 free\n" +
                       "2. KCC Parking (RWF 1000/hr) 10 free\n" +
                       "3. Nyarugenge Market (RWF 300/hr) 8 free";
        } else if (text.equals("1*1") || text.equals("1*2") || text.equals("1*3")) {
            // Level 2 — Slot selection
            response = "CON How many slots? (1-3)\n" +
                       "1. 1 slot\n" +
                       "2. 2 slots\n" +
                       "3. 3 slots";
        } else if (level >= 3 && parts[0].equals("1")) {
            // Level 3 — Confirm booking
            response = "CON Confirm booking?\n" +
                       "Amount: RWF 500\n" +
                       "1. Confirm\n" +
                       "2. Cancel";
        } else if (level >= 4 && parts[level-1].equals("1")) {
            // Booking confirmed
            response = "END Booking confirmed!\n" +
                       "A host will meet you at entry.\n" +
                       "Give your phone number for verification.";
        } else if (text.equals("2")) {
            response = "END Download SmartPark app to view\n" +
                       "your full reservation history.";
        } else {
            response = "END Invalid selection. Please try again.\n" +
                       "Dial *384# to restart.";
        }

        return ResponseEntity.ok(response);
    }

    @PostMapping("/sms")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ApiResponse<SmsRequest>> sendSms(@Valid @RequestBody SmsRequest request) {
        try {
            notificationService.sendSms(request.getPhoneNumber(), request.getMessage());
            return ResponseEntity.ok(ApiResponse.success("SMS sent successfully", request));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("We couldn’t send the SMS right now. Please try again."));
        }
    }

    private boolean isAcceptedServiceCode(String serviceCode) {
        if (serviceCode == null || serviceCode.isBlank()) {
            return false;
        }

        String normalized = serviceCode.trim();
        return java.util.Arrays.stream(configuredServiceCodes.split(","))
                .map(String::trim)
                .filter(code -> !code.isBlank())
                .anyMatch(code -> code.equalsIgnoreCase(normalized));
    }
}
