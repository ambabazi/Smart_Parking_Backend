package com.smart.parking.admin;

import com.smart.parking.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/owner")
@RequiredArgsConstructor
public class OwnerDashboardController {

    private final OwnerDashboardService ownerDashboardService;

    /**
     * GET /api/owner/dashboard
     * Returns owner/host-specific dashboard statistics including parking spaces, occupancy, revenue, and reservations
     * Requires HOST or ADMIN role
     */
    @GetMapping("/dashboard")
    @PreAuthorize("hasAuthority('HOST') or hasAuthority('ADMIN')")
    public ResponseEntity<ApiResponse<OwnerDashboardDTO>> getOwnerDashboard(Authentication authentication) {
        String userEmail = authentication.getName();
        OwnerDashboardDTO dashboard = ownerDashboardService.getOwnerDashboard(userEmail);
        return ResponseEntity.ok(ApiResponse.success("Owner dashboard statistics", dashboard));
    }
}
