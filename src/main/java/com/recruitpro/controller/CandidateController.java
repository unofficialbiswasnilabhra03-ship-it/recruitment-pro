package com.recruitpro.controller;

import com.recruitpro.dto.request.*;
import com.recruitpro.dto.response.ApiResponse;
import com.recruitpro.dto.response.CandidateResponse;
import com.recruitpro.enums.ExperienceLevel;
import com.recruitpro.security.UserPrincipal;
import com.recruitpro.service.interfaces.CandidateService;
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
@RequestMapping("/api/v1/candidates")
@RequiredArgsConstructor
@Tag(name = "Candidates", description = "Candidate profile, education, experience, skills, portfolio")
public class CandidateController {

    private final CandidateService candidateService;

    // ── Own profile (CANDIDATE) ───────────────────────────────────────────────

    @GetMapping("/me")
    @PreAuthorize("hasRole('CANDIDATE')")
    @Operation(summary = "Get my candidate profile")
    public ResponseEntity<ApiResponse<CandidateResponse>> getMyProfile(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success("Profile fetched",
                candidateService.getMyProfile(principal.getId())));
    }

    @PutMapping("/me")
    @PreAuthorize("hasRole('CANDIDATE')")
    @Operation(summary = "Update my candidate profile")
    public ResponseEntity<ApiResponse<CandidateResponse>> updateMyProfile(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CandidateProfileRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Profile updated",
                candidateService.updateMyProfile(principal.getId(), request)));
    }

    // ── Education ─────────────────────────────────────────────────────────────

    @PostMapping("/me/education")
    @PreAuthorize("hasRole('CANDIDATE')")
    @Operation(summary = "Add an education entry")
    public ResponseEntity<ApiResponse<CandidateResponse>> addEducation(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CandidateEducationRequest request) {
        return ResponseEntity.status(201).body(ApiResponse.success("Education added",
                candidateService.addEducation(principal.getId(), request)));
    }

    @PutMapping("/me/education/{id}")
    @PreAuthorize("hasRole('CANDIDATE')")
    @Operation(summary = "Update an education entry")
    public ResponseEntity<ApiResponse<CandidateResponse>> updateEducation(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id,
            @Valid @RequestBody CandidateEducationRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Education updated",
                candidateService.updateEducation(principal.getId(), id, request)));
    }

    @DeleteMapping("/me/education/{id}")
    @PreAuthorize("hasRole('CANDIDATE')")
    @Operation(summary = "Delete an education entry")
    public ResponseEntity<ApiResponse<CandidateResponse>> deleteEducation(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Education deleted",
                candidateService.deleteEducation(principal.getId(), id)));
    }

    // ── Experience ────────────────────────────────────────────────────────────

    @PostMapping("/me/experience")
    @PreAuthorize("hasRole('CANDIDATE')")
    @Operation(summary = "Add a work experience entry")
    public ResponseEntity<ApiResponse<CandidateResponse>> addExperience(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CandidateExperienceRequest request) {
        return ResponseEntity.status(201).body(ApiResponse.success("Experience added",
                candidateService.addExperience(principal.getId(), request)));
    }

    @PutMapping("/me/experience/{id}")
    @PreAuthorize("hasRole('CANDIDATE')")
    @Operation(summary = "Update a work experience entry")
    public ResponseEntity<ApiResponse<CandidateResponse>> updateExperience(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id,
            @Valid @RequestBody CandidateExperienceRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Experience updated",
                candidateService.updateExperience(principal.getId(), id, request)));
    }

    @DeleteMapping("/me/experience/{id}")
    @PreAuthorize("hasRole('CANDIDATE')")
    @Operation(summary = "Delete a work experience entry")
    public ResponseEntity<ApiResponse<CandidateResponse>> deleteExperience(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Experience deleted",
                candidateService.deleteExperience(principal.getId(), id)));
    }

    // ── Skills ────────────────────────────────────────────────────────────────

    @PostMapping("/me/skills")
    @PreAuthorize("hasRole('CANDIDATE')")
    @Operation(summary = "Add a skill")
    public ResponseEntity<ApiResponse<CandidateResponse>> addSkill(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CandidateSkillRequest request) {
        return ResponseEntity.status(201).body(ApiResponse.success("Skill added",
                candidateService.addSkill(principal.getId(), request)));
    }

    @PutMapping("/me/skills/{id}")
    @PreAuthorize("hasRole('CANDIDATE')")
    @Operation(summary = "Update a skill")
    public ResponseEntity<ApiResponse<CandidateResponse>> updateSkill(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id,
            @Valid @RequestBody CandidateSkillRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Skill updated",
                candidateService.updateSkill(principal.getId(), id, request)));
    }

    @DeleteMapping("/me/skills/{id}")
    @PreAuthorize("hasRole('CANDIDATE')")
    @Operation(summary = "Delete a skill")
    public ResponseEntity<ApiResponse<CandidateResponse>> deleteSkill(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Skill deleted",
                candidateService.deleteSkill(principal.getId(), id)));
    }

    // ── Portfolio ─────────────────────────────────────────────────────────────

    @PostMapping("/me/portfolio")
    @PreAuthorize("hasRole('CANDIDATE')")
    @Operation(summary = "Add a portfolio item")
    public ResponseEntity<ApiResponse<CandidateResponse>> addPortfolio(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CandidatePortfolioRequest request) {
        return ResponseEntity.status(201).body(ApiResponse.success("Portfolio item added",
                candidateService.addPortfolioItem(principal.getId(), request)));
    }

    @PutMapping("/me/portfolio/{id}")
    @PreAuthorize("hasRole('CANDIDATE')")
    @Operation(summary = "Update a portfolio item")
    public ResponseEntity<ApiResponse<CandidateResponse>> updatePortfolio(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id,
            @Valid @RequestBody CandidatePortfolioRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Portfolio item updated",
                candidateService.updatePortfolioItem(principal.getId(), id, request)));
    }

    @DeleteMapping("/me/portfolio/{id}")
    @PreAuthorize("hasRole('CANDIDATE')")
    @Operation(summary = "Delete a portfolio item")
    public ResponseEntity<ApiResponse<CandidateResponse>> deletePortfolio(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Portfolio item deleted",
                candidateService.deletePortfolioItem(principal.getId(), id)));
    }

    // ── HR / Admin: browse candidates ────────────────────────────────────────

    @GetMapping
    @PreAuthorize("hasAnyRole('HR','ADMIN','INTERVIEWER')")
    @Operation(summary = "List / search / filter candidates (HR / Admin / Interviewer)")
    public ResponseEntity<ApiResponse<Page<CandidateResponse>>> listCandidates(
            @RequestParam(defaultValue = "0")   int page,
            @RequestParam(defaultValue = "10")  int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false)     String search,
            @RequestParam(required = false)     ExperienceLevel experienceLevel,
            @RequestParam(required = false)     String location,
            @RequestParam(required = false)     String skill) {

        var pageable = PageRequest.of(page, size,
                sortDir.equalsIgnoreCase("asc")
                        ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending());

        Page<CandidateResponse> result;
        if (skill != null && !skill.isBlank()) {
            result = candidateService.getCandidatesBySkill(skill, pageable);
        } else if (search != null && !search.isBlank()) {
            result = candidateService.searchCandidates(search, pageable);
        } else if (experienceLevel != null || location != null) {
            result = candidateService.filterCandidates(experienceLevel, location, pageable);
        } else {
            result = candidateService.getAllCandidates(pageable);
        }
        return ResponseEntity.ok(ApiResponse.success("Candidates fetched", result));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('HR','ADMIN','INTERVIEWER')")
    @Operation(summary = "Get candidate by ID (HR / Admin / Interviewer)")
    public ResponseEntity<ApiResponse<CandidateResponse>> getCandidateById(
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Candidate fetched",
                candidateService.getCandidateById(id)));
    }
}
