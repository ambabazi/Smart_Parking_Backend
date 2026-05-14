package com.smart.parking.payment;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class FlutterwaveEvent {
    private String event;
    private EventData data;

    @Data
    public static class EventData {
        private Long id;
        @JsonProperty("tx_ref")
        private String txRef;
        private String status;
        private Double amount;
    }
}