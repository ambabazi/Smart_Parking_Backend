package com.smart.parking.parking;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ParkingSpaceDetailDTO {
    private Long id;
    private String uuid;
    private String referenceCode;
    private String name;
    private String address;
    private Double latitude;
    private Double longitude;
    private Integer totalSlots;
    private Integer availableSlots;
    private Double pricePerSlot;
    private Boolean eventEnabled;
    private Long ownerId;
    private String ownerName;
}
