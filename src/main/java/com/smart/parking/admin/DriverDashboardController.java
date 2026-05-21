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
@RequestMapping("/api/driver")
@RequiredArgsConstructor
public class DriverDashboardController {

    private final DriverDashboardService driverDashboardService;

    /**
     * GET /api/driver/dashboard
     * Returns driver-specific dashboard statistics including reservations, spending, and member info
     * Requires DRIVER role
     */
    @GetMapping("/dashboard")
    @PreAuthorize("hasAuthority('DRIVER')")
    public ResponseEntity<ApiResponse<DriverDashboardDTO>> getDriverDashboard(Authentication authentication) {
        String userEmail = authentication.getName();
        DriverDashboardDTO dashboard = driverDashboardService.getDriverDashboard(userEmail);
        return ResponseEntity.ok(ApiResponse.success("Driver dashboard statistics", dashboard));
    }
}
