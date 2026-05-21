package com.smart.parking.admin;

import com.smart.parking.auth.User;
import com.smart.parking.auth.UserRepository;
import com.smart.parking.parking.ParkingSpaceRepository;
import com.smart.parking.reservation.ReservationRepository;
import com.smart.parking.payment.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class OwnerDashboardService {

    private final UserRepository userRepository;
    private final ParkingSpaceRepository parkingSpaceRepository;
    private final ReservationRepository reservationRepository;
    private final PaymentRepository paymentRepository;

    public OwnerDashboardDTO getOwnerDashboard(String userEmail) {
        User owner = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Long totalParkingSpaces = parkingSpaceRepository.countByOwnerId(owner.getId());
        Long totalSlots = parkingSpaceRepository.findByOwnerId(owner.getId()).stream()
                .mapToLong(p -> (long) p.getTotalSlots())
                .sum();
        Long availableSlots = parkingSpaceRepository.findByOwnerId(owner.getId()).stream()
                .mapToLong(p -> (long) p.getAvailableSlots())
                .sum();
        Long occupiedSlots = totalSlots - availableSlots;
        Long activeReservations = reservationRepository.countActiveReservationsByOwnerId(owner.getId());
        Long totalReservations = reservationRepository.countByOwnerId(owner.getId());
        
        BigDecimal totalRevenue = paymentRepository.sumPaymentsByOwnerId(owner.getId());
        if (totalRevenue == null) {
            totalRevenue = BigDecimal.ZERO;
        }

        BigDecimal revenueToday = paymentRepository.sumPaymentsByOwnerIdToday(owner.getId());
        if (revenueToday == null) {
            revenueToday = BigDecimal.ZERO;
        }

        double occupancyPercentage = totalSlots > 0
                ? ((double) occupiedSlots / totalSlots) * 100
                : 0.0;

        return OwnerDashboardDTO.builder()
                .totalParkingSpaces(totalParkingSpaces)
                .totalSlots(totalSlots)
                .availableSlots(availableSlots)
                .occupiedSlots(occupiedSlots)
                .occupancyPercentage(occupancyPercentage)
                .activeReservations(activeReservations)
                .totalReservations(totalReservations)
                .totalRevenue(totalRevenue)
                .revenueToday(revenueToday)
                .memberSince(owner.getCreatedAt())
                .build();
    }
}
