package com.recruitpro.controller;

import com.recruitpro.dto.response.ApiResponse;
import com.recruitpro.entity.EmailLog;
import com.recruitpro.enums.EmailStatus;
import com.recruitpro.repository.EmailLogRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/emails")
@RequiredArgsConstructor
@Tag(name = "Email Logs", description = "Admin: audit trail of all outbound emails")
public class EmailController {

    private final EmailLogRepository emailLogRepository;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "List all email logs (Admin)")
    public ResponseEntity<ApiResponse<Page<EmailLog>>> getLogs(
            @RequestParam(defaultValue = "0")   int page,
            @RequestParam(defaultValue = "20")  int size,
            @RequestParam(required = false)     EmailStatus status) {

        PageRequest pageable = PageRequest.of(page, size,
                Sort.by("createdAt").descending());

        Page<EmailLog> result = status != null
                ? emailLogRepository.findByStatusOrderByCreatedAtDesc(status, pageable)
                : emailLogRepository.findAll(pageable);

        return ResponseEntity.ok(ApiResponse.success("Email logs fetched", result));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get email log by ID (Admin)")
    public ResponseEntity<ApiResponse<EmailLog>> getById(@PathVariable Long id) {
        EmailLog log = emailLogRepository.findById(id)
                .orElseThrow(() -> new com.recruitpro.exception.ResourceNotFoundException(
                        "EmailLog", "id", id));
        return ResponseEntity.ok(ApiResponse.success("Email log fetched", log));
    }

    @GetMapping("/recipient/{email}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get emails sent to a specific address (Admin)")
    public ResponseEntity<ApiResponse<Page<EmailLog>>> getByRecipient(
            @PathVariable String email,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success("Email logs fetched",
                emailLogRepository.findByRecipientEmailOrderByCreatedAtDesc(
                        email, PageRequest.of(page, size))));
    }
}
