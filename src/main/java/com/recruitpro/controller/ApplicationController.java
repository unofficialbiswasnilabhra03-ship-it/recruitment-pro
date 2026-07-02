package com.recruitpro.controller;

import com.recruitpro.dto.request.ApplicationStatusUpdateRequest;
import com.recruitpro.dto.request.JobApplicationRequest;
import com.recruitpro.dto.response.ApiResponse;
import com.recruitpro.dto.response.JobApplicationResponse;
import com.recruitpro.enums.ApplicationStatus;
import com.recruitpro.security.UserPrincipal;
import com.recruitpro.service.interfaces.JobApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/applications")
@RequiredArgsConstructor
@Tag(name = "Applications", description = "Job application lifecycle: apply, shortlist, status, withdraw")
public class ApplicationController {

    private final JobApplicationService applicationService;

    // ── Candidate ─────────────────────────────────────────────────────────────

    @PostMapping
    @PreAuthorize("hasRole('CANDIDATE')")
    @Operation(summary = "Apply for a job")
    public ResponseEntity<ApiResponse<JobApplicationResponse>> apply(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody JobApplicationRequest request) {
        return ResponseEntity.status(201).body(ApiResponse.success("Application submitted",
                applicationService.apply(principal.getId(), request)));
    }

    @DeleteMapping("/{id}/withdraw")
    @PreAuthorize("hasRole('CANDIDATE')")
    @Operation(summary = "Withdraw my application")
    public ResponseEntity<ApiResponse<Void>> withdraw(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id) {
        applicationService.withdraw(principal.getId(), id);
        return ResponseEntity.ok(ApiResponse.success("Application withdrawn"));
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('CANDIDATE')")
    @Operation(summary = "List my applications")
    public ResponseEntity<ApiResponse<Page<JobApplicationResponse>>> getMyApplications(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "0")   int page,
            @RequestParam(defaultValue = "10")  int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        var pageable = PageRequest.of(page, size,
                sortDir.equalsIgnoreCase("asc")
                        ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending());
        return ResponseEntity.ok(ApiResponse.success("Applications fetched",
                applicationService.getMyApplications(principal.getId(), pageable)));
    }

    @GetMapping("/me/{id}")
    @PreAuthorize("hasRole('CANDIDATE')")
    @Operation(summary = "Get a specific application of mine")
    public ResponseEntity<ApiResponse<JobApplicationResponse>> getMyApplication(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Application fetched",
                applicationService.getMyApplication(principal.getId(), id)));
    }

    // ── HR / Admin ────────────────────────────────────────────────────────────

    @GetMapping("/job/{jobId}")
    @PreAuthorize("hasAnyRole('HR','ADMIN')")
    @Operation(summary = "Get all applications for a job (HR / Admin)")
    public ResponseEntity<ApiResponse<Page<JobApplicationResponse>>> getByJob(
            @PathVariable Long jobId,
            @RequestParam(required = false)     ApplicationStatus status,
            @RequestParam(defaultValue = "0")   int page,
            @RequestParam(defaultValue = "10")  int size) {
        return ResponseEntity.ok(ApiResponse.success("Applications fetched",
                applicationService.getApplicationsByJob(jobId, status,
                        PageRequest.of(page, size, Sort.by("createdAt").descending()))));
    }

    @GetMapping("/company/{companyId}")
    @PreAuthorize("hasAnyRole('HR','ADMIN')")
    @Operation(summary = "Get all applications across a company's jobs (HR / Admin)")
    public ResponseEntity<ApiResponse<Page<JobApplicationResponse>>> getByCompany(
            @PathVariable Long companyId,
            @RequestParam(required = false)     ApplicationStatus status,
            @RequestParam(defaultValue = "0")   int page,
            @RequestParam(defaultValue = "10")  int size) {
        return ResponseEntity.ok(ApiResponse.success("Applications fetched",
                applicationService.getApplicationsByCompany(companyId, status,
                        PageRequest.of(page, size, Sort.by("createdAt").descending()))));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('HR','ADMIN')")
    @Operation(summary = "Get application by ID (HR / Admin)")
    public ResponseEntity<ApiResponse<JobApplicationResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Application fetched",
                applicationService.getApplicationById(id)));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('HR','ADMIN')")
    @Operation(summary = "Update application status (HR / Admin)")
    public ResponseEntity<ApiResponse<JobApplicationResponse>> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody ApplicationStatusUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Status updated",
                applicationService.updateStatus(id, request)));
    }

    @PatchMapping("/{id}/shortlist")
    @PreAuthorize("hasAnyRole('HR','ADMIN')")
    @Operation(summary = "Shortlist an application (HR / Admin)")
    public ResponseEntity<ApiResponse<JobApplicationResponse>> shortlist(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Application shortlisted",
                applicationService.shortlist(id)));
    }

    @GetMapping("/candidate/{candidateId}")
    @PreAuthorize("hasAnyRole('HR','ADMIN')")
    @Operation(summary = "Get all applications for a specific candidate (HR / Admin)")
    public ResponseEntity<ApiResponse<Page<JobApplicationResponse>>> getByCandidateId(
            @PathVariable Long candidateId,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success("Applications fetched",
                applicationService.getApplicationsByCandidate(candidateId,
                        PageRequest.of(page, size, Sort.by("createdAt").descending()))));
    }
}
