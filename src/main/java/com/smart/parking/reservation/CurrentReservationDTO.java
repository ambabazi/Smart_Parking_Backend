package com.smart.parking.reservation;

import java.time.LocalDateTime;

public class CurrentReservationDTO {
    private String referenceCode;
    private String parkingSpaceName;
    private String address;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer slotCount;
    private String status;
    private Long minutesRemaining;

    public CurrentReservationDTO(String referenceCode, String parkingSpaceName, String address, LocalDateTime startTime,
                                 LocalDateTime endTime, Integer slotCount, String status, Long minutesRemaining) {
        this.referenceCode = referenceCode;
        this.parkingSpaceName = parkingSpaceName;
        this.address = address;
        this.startTime = startTime;
        this.endTime = endTime;
        this.slotCount = slotCount;
        this.status = status;
        this.minutesRemaining = minutesRemaining;
    }

    public String getReferenceCode() { return referenceCode; }
    public String getParkingSpaceName() { return parkingSpaceName; }
    public String getAddress() { return address; }
    public LocalDateTime getStartTime() { return startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public Integer getSlotCount() { return slotCount; }
    public String getStatus() { return status; }
    public Long getMinutesRemaining() { return minutesRemaining; }
}
