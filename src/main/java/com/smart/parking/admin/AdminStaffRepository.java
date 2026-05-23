package com.smart.parking.admin;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AdminStaffRepository extends JpaRepository<AdminStaffMember, Long> {
    Optional<AdminStaffMember> findByEmail(String email);
    Page<AdminStaffMember> findByStaffRoleContainingIgnoreCase(String staffRole, Pageable pageable);
}