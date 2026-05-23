package com.smart.parking.common;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;

public final class DateTimeParseUtil {

    private DateTimeParseUtil() {
    }

    public static LocalDateTime parseFlexible(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Date/time value is required");
        }

        String trimmed = value.trim();

        try {
            return LocalDateTime.parse(trimmed);
        } catch (Exception ignored) {
            // Try common offset formats used by browsers and API clients.
        }

        try {
            return OffsetDateTime.parse(trimmed).toLocalDateTime();
        } catch (Exception ignored) {
            // Fall through.
        }

        try {
            return ZonedDateTime.parse(trimmed).toLocalDateTime();
        } catch (Exception ignored) {
            throw new IllegalArgumentException("Unsupported date/time format: " + value);
        }
    }
}