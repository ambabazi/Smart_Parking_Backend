package com.smart.parking.admin;

import com.smart.parking.auth.User;
import com.smart.parking.auth.UserRepository;
import com.smart.parking.reservation.ReservationRepository;
import com.smart.parking.payment.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class DriverDashboardService {

    private final UserRepository userRepository;
    private final ReservationRepository reservationRepository;
    private final PaymentRepository paymentRepository;

    public DriverDashboardDTO getDriverDashboard(String userEmail) {
        User driver = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Long totalReservations = reservationRepository.countByUserId(driver.getId());
        Long completedReservations = reservationRepository.countByUserIdAndStatus(driver.getId(), "CHECKED_OUT");
        Long activeReservations = reservationRepository.countByUserIdAndStatus(driver.getId(), "RESERVED");
        Long cancelledReservations = reservationRepository.countByUserIdAndStatus(driver.getId(), "CANCELLED");
        
        BigDecimal totalSpent = paymentRepository.sumPaymentsByUserId(driver.getId());
        if (totalSpent == null) {
            totalSpent = BigDecimal.ZERO;
        }

        Long upcomingReservations = reservationRepository.countByUserIdAndStartTimeAfter(
            driver.getId(), LocalDateTime.now()
        );

        return DriverDashboardDTO.builder()
                .totalReservations(totalReservations)
                .completedReservations(completedReservations)
                .activeReservations(activeReservations)
                .cancelledReservations(cancelledReservations)
                .upcomingReservations(upcomingReservations)
                .totalSpent(totalSpent)
                .membersince(driver.getCreatedAt())
                .build();
    }
}
