package com.smart.parking.common;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public final class ReferenceCodeGenerator {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ISO_LOCAL_DATE;

    private ReferenceCodeGenerator() {
    }

    public static String parkingCode() {
        return code("PKG");
    }

    public static String reservationCode() {
        return code("RES");
    }

    public static String paymentCode() {
        return code("PAY");
    }

    private static String code(String prefix) {
        String date = LocalDate.now().format(DATE);
        String suffix = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return prefix + "-" + date + "-" + suffix;
    }
}
