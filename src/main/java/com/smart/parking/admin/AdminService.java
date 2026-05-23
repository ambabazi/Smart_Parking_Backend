package com.smart.parking.admin;

import com.smart.parking.parking.ParkingSpaceRepository;
import com.smart.parking.reservation.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final ParkingSpaceRepository parkingSpaceRepository;
    private final ReservationRepository reservationRepository;
        private final AdminStaffRepository adminStaffRepository;

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

        public Page<AdminStaffResponse> listStaff(String staffRole, Pageable pageable) {
                Page<AdminStaffMember> staff = (staffRole == null || staffRole.isBlank())
                                ? adminStaffRepository.findAll(pageable)
                                : adminStaffRepository.findByStaffRoleContainingIgnoreCase(staffRole.trim(), pageable);
                return staff.map(this::toResponse);
        }

        @Transactional
        public AdminStaffResponse createStaff(AdminStaffRequest request) {
                AdminStaffMember staffMember = adminStaffRepository.findByEmail(request.getEmail().trim())
                                .orElseGet(AdminStaffMember::new);

                staffMember.setEmail(request.getEmail().trim());
                staffMember.setFullName(request.getFullName().trim());
                staffMember.setPhone(request.getPhone() == null ? null : request.getPhone().trim());
                staffMember.setStaffRole(request.getStaffRole().trim());
                if (request.getActive() != null) {
                        staffMember.setActive(request.getActive());
                }

                return toResponse(adminStaffRepository.save(staffMember));
        }

        @Transactional
        public AdminStaffResponse updateStaff(Long id, AdminStaffRequest request) {
                AdminStaffMember staffMember = adminStaffRepository.findById(id)
                                .orElseThrow(() -> new IllegalArgumentException("Staff member not found"));

                adminStaffRepository.findByEmail(request.getEmail().trim())
                        .filter(existing -> !existing.getId().equals(id))
                        .ifPresent(existing -> {
                            throw new IllegalArgumentException("A staff member already exists with that email");
                        });

                staffMember.setEmail(request.getEmail().trim());
                staffMember.setFullName(request.getFullName().trim());
                staffMember.setPhone(request.getPhone() == null ? null : request.getPhone().trim());
                staffMember.setStaffRole(request.getStaffRole().trim());
                if (request.getActive() != null) {
                        staffMember.setActive(request.getActive());
                }

                return toResponse(adminStaffRepository.save(staffMember));
        }

        private AdminStaffResponse toResponse(AdminStaffMember staffMember) {
                return AdminStaffResponse.builder()
                                .id(staffMember.getId())
                                .email(staffMember.getEmail())
                                .fullName(staffMember.getFullName())
                                .phone(staffMember.getPhone())
                                .staffRole(staffMember.getStaffRole())
                                .active(staffMember.getActive())
                                .createdAt(staffMember.getCreatedAt())
                                .updatedAt(staffMember.getUpdatedAt())
                                .build();
        }

    private Long getTotalAvailableSlots() {
        return parkingSpaceRepository.findAll().stream()
                .mapToLong(p -> (long) p.getAvailableSlots())
                .sum();
    }
}
