package com.smart.parking.reservation;

public class QRVerificationResponse {
    private String status;
    private boolean valid;
    private String driverName;
    private String licensePlate;
    private ReservationDetail reservation;

    public QRVerificationResponse(String status, boolean valid, String driverName, String licensePlate, ReservationDetail reservation) {
        this.status = status;
        this.valid = valid;
        this.driverName = driverName;
        this.licensePlate = licensePlate;
        this.reservation = reservation;
    }

    public String getStatus() { return status; }
    public boolean isValid() { return valid; }
    public String getDriverName() { return driverName; }
    public String getLicensePlate() { return licensePlate; }
    public ReservationDetail getReservation() { return reservation; }
}
