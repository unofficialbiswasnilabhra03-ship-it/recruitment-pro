package com.recruitpro.controller;

import com.recruitpro.dto.response.ApiResponse;
import com.recruitpro.dto.response.ResumeResponse;
import com.recruitpro.security.UserPrincipal;
import com.recruitpro.service.interfaces.ResumeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/resumes")
@RequiredArgsConstructor
@Tag(name = "Resumes", description = "Resume upload, download, and delete")
public class ResumeController {

    private final ResumeService resumeService;

    // ── Candidate: manage own resume ──────────────────────────────────────────

    @PostMapping(value = "/me", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('CANDIDATE')")
    @Operation(summary = "Upload or replace my resume (PDF / DOCX, max 5 MB)")
    public ResponseEntity<ApiResponse<ResumeResponse>> uploadResume(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam("file") MultipartFile file) {
        return ResponseEntity.status(201).body(ApiResponse.success("Resume uploaded",
                resumeService.uploadResume(principal.getId(), file)));
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('CANDIDATE')")
    @Operation(summary = "Get my resume metadata")
    public ResponseEntity<ApiResponse<ResumeResponse>> getMyResume(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success("Resume fetched",
                resumeService.getMyResume(principal.getId())));
    }

    @DeleteMapping("/me")
    @PreAuthorize("hasRole('CANDIDATE')")
    @Operation(summary = "Delete my resume")
    public ResponseEntity<ApiResponse<Void>> deleteMyResume(
            @AuthenticationPrincipal UserPrincipal principal) {
        resumeService.deleteResume(principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Resume deleted"));
    }

    // ── HR / Admin / Interviewer: view + download a candidate's resume ────────

    @GetMapping("/candidate/{candidateId}")
    @PreAuthorize("hasAnyRole('HR','ADMIN','INTERVIEWER')")
    @Operation(summary = "Get resume metadata for a candidate (HR / Admin / Interviewer)")
    public ResponseEntity<ApiResponse<ResumeResponse>> getResumeByCandidateId(
            @PathVariable Long candidateId) {
        return ResponseEntity.ok(ApiResponse.success("Resume fetched",
                resumeService.getResumeByCandidateId(candidateId)));
    }

    @GetMapping("/candidate/{candidateId}/download")
    @PreAuthorize("hasAnyRole('HR','ADMIN','INTERVIEWER','CANDIDATE')")
    @Operation(summary = "Download a candidate's resume file")
    public ResponseEntity<Resource> downloadResume(@PathVariable Long candidateId) {
        Resource resource = resumeService.downloadResume(candidateId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + resource.getFilename() + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }
}
