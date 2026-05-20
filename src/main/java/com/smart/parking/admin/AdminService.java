package com.smart.parking.admin;

import com.smart.parking.parking.ParkingSpaceRepository;
import com.smart.parking.reservation.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final ParkingSpaceRepository parkingSpaceRepository;
    private final ReservationRepository reservationRepository;

    @Cacheable(cacheNames = "dashboardStats", key = "'singleton'")
    public DashboardDTO getDashboardStats() {
        long totalSpaces = parkingSpaceRepository.count();
        Long totalSlots = parkingSpaceRepository.findAll().stream()
                .mapToLong(p -> (long) p.getTotalSlots())
                .sum();
        Long activeReservations = reservationRepository.countActiveReservations();
        Long bookingsToday = reservationRepository.countBookingsToday();
        BigDecimal revenueToday = reservationRepository.revenueToday() != null
                ? reservationRepository.revenueToday()
                : BigDecimal.ZERO;

        double occupancyPercentage = totalSlots > 0
                ? ((double) (totalSlots - getTotalAvailableSlots()) / totalSlots) * 100
                : 0.0;

        return DashboardDTO.builder()
                .totalParkingSpaces(totalSpaces)
                .totalReservationSlots(totalSlots)
                .activeReservations(activeReservations)
                .bookingsToday(bookingsToday)
                .revenueToday(revenueToday)
                .occupancyPercentage(occupancyPercentage)
                .build();
    }

    private Long getTotalAvailableSlots() {
        return parkingSpaceRepository.findAll().stream()
                .mapToLong(p -> (long) p.getAvailableSlots())
                .sum();
    }
}
