package com.smart.parking.admin;

import com.smart.parking.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/dashboard")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ApiResponse<DashboardDTO>> getDashboard() {
        DashboardDTO dashboard = adminService.getDashboardStats();
        return ResponseEntity.ok(ApiResponse.success("Dashboard statistics", dashboard));
    }

    @GetMapping("/staff")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ApiResponse<Page<AdminStaffResponse>>> getStaff(
            @RequestParam(required = false) String staffRole,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false, defaultValue = "ASC") Sort.Direction direction) {
        Pageable pageable = buildPageable(page, size, sort, direction);
        return ResponseEntity.ok(ApiResponse.success("Staff list", adminService.listStaff(staffRole, pageable)));
    }

    @PostMapping("/staff")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ApiResponse<AdminStaffResponse>> createStaff(@Valid @RequestBody AdminStaffRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Staff member created", adminService.createStaff(request)));
    }

    @PatchMapping("/staff/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ApiResponse<AdminStaffResponse>> updateStaff(
            @PathVariable Long id,
            @Valid @RequestBody AdminStaffRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Staff member updated", adminService.updateStaff(id, request)));
    }

    private Pageable buildPageable(int page, int size, String sort, Sort.Direction direction) {
        if (sort == null || sort.isBlank()) {
            return PageRequest.of(page, size);
        }

        String sortProperty = sort.trim();
        if (sortProperty.isEmpty() || sortProperty.equalsIgnoreCase("string")) {
            return PageRequest.of(page, size);
        }

        return PageRequest.of(page, size, Sort.by(direction, sortProperty));
    }
}
