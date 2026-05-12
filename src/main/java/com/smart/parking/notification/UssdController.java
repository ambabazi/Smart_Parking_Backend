package com.smart.parking.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
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
public class UssdController {

    @PostMapping(consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
                 produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> handleUssd(
            @RequestParam String sessionId,
            @RequestParam String phoneNumber,
            @RequestParam String serviceCode,
            @RequestParam(defaultValue = "") String text) {

        log.info("USSD: session={} phone={} text={}", sessionId, phoneNumber, text);

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
}
