package com.recruitpro.controller;

import com.recruitpro.dto.request.CompanyRequest;
import com.recruitpro.dto.response.ApiResponse;
import com.recruitpro.dto.response.CompanyResponse;
import com.recruitpro.service.interfaces.CompanyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/companies")
@RequiredArgsConstructor
@Tag(name = "Companies", description = "Company profile management")
public class CompanyController {

    private final CompanyService companyService;

    @PostMapping
    @PreAuthorize("hasAnyRole('HR','ADMIN')")
    @Operation(summary = "Create a company (HR / Admin)")
    public ResponseEntity<ApiResponse<CompanyResponse>> createCompany(
            @Valid @RequestBody CompanyRequest request) {
        return ResponseEntity.status(201).body(
                ApiResponse.success("Company created", companyService.createCompany(request)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('HR','ADMIN')")
    @Operation(summary = "Update a company (HR / Admin)")
    public ResponseEntity<ApiResponse<CompanyResponse>> updateCompany(
            @PathVariable Long id,
            @Valid @RequestBody CompanyRequest request) {
        return ResponseEntity.ok(
                ApiResponse.success("Company updated", companyService.updateCompany(id, request)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get company by ID (public)")
    public ResponseEntity<ApiResponse<CompanyResponse>> getCompany(@PathVariable Long id) {
        return ResponseEntity.ok(
                ApiResponse.success("Company fetched", companyService.getCompanyById(id)));
    }

    @GetMapping
    @Operation(summary = "List / search companies (public)")
    public ResponseEntity<ApiResponse<Page<CompanyResponse>>> listCompanies(
            @RequestParam(defaultValue = "0")       int page,
            @RequestParam(defaultValue = "10")      int size,
            @RequestParam(defaultValue = "name")    String sortBy,
            @RequestParam(defaultValue = "asc")     String sortDir,
            @RequestParam(required = false)         String search,
            @RequestParam(required = false)         String industry) {

        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        PageRequest pageable = PageRequest.of(page, size, sort);

        Page<CompanyResponse> result;
        if (search != null && !search.isBlank()) {
            result = companyService.searchCompanies(search, pageable);
        } else if (industry != null && !industry.isBlank()) {
            result = companyService.getCompaniesByIndustry(industry, pageable);
        } else {
            result = companyService.getAllCompanies(pageable);
        }
        return ResponseEntity.ok(ApiResponse.success("Companies fetched", result));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('HR','ADMIN')")
    @Operation(summary = "Soft-delete a company (HR / Admin)")
    public ResponseEntity<ApiResponse<Void>> deleteCompany(@PathVariable Long id) {
        companyService.deleteCompany(id);
        return ResponseEntity.ok(ApiResponse.success("Company deleted"));
    }
}
