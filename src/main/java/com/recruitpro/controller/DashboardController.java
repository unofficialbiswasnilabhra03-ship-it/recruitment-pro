package com.recruitpro.controller;

import com.recruitpro.dto.response.ApiResponse;
import com.recruitpro.dto.response.DashboardResponse;
import com.recruitpro.service.interfaces.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "Recruitment analytics and hiring statistics")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Global dashboard — all companies (Admin)")
    public ResponseEntity<ApiResponse<DashboardResponse>> getGlobal() {
        return ResponseEntity.ok(ApiResponse.success("Dashboard data fetched",
                dashboardService.getGlobalDashboard()));
    }

    @GetMapping("/company/{companyId}")
    @PreAuthorize("hasAnyRole('HR','ADMIN')")
    @Operation(summary = "Company dashboard — scoped to one company (HR / Admin)")
    public ResponseEntity<ApiResponse<DashboardResponse>> getByCompany(
            @PathVariable Long companyId) {
        return ResponseEntity.ok(ApiResponse.success("Dashboard data fetched",
                dashboardService.getCompanyDashboard(companyId)));
    }
}
