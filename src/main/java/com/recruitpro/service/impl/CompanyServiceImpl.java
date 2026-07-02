package com.recruitpro.service.impl;

import com.recruitpro.dto.request.CompanyRequest;
import com.recruitpro.dto.response.CompanyResponse;
import com.recruitpro.entity.Company;
import com.recruitpro.exception.DuplicateResourceException;
import com.recruitpro.exception.ResourceNotFoundException;
import com.recruitpro.repository.CompanyRepository;
import com.recruitpro.repository.JobRepository;
import com.recruitpro.service.interfaces.CompanyService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CompanyServiceImpl implements CompanyService {

    private final CompanyRepository companyRepository;
    private final JobRepository     jobRepository;

    @Override
    @Transactional
    public CompanyResponse createCompany(CompanyRequest request) {
        if (companyRepository.existsByNameAndDeletedAtIsNull(request.getName())) {
            throw new DuplicateResourceException("Company", "name", request.getName());
        }
        Company company = Company.builder()
                .name(request.getName())
                .description(request.getDescription())
                .website(request.getWebsite())
                .industry(request.getIndustry())
                .size(request.getSize())
                .headquarters(request.getHeadquarters())
                .logoUrl(request.getLogoUrl())
                .linkedinUrl(request.getLinkedinUrl())
                .build();
        return toResponse(companyRepository.save(company));
    }

    @Override
    @Transactional
    public CompanyResponse updateCompany(Long id, CompanyRequest request) {
        Company company = findActive(id);
        company.setName(request.getName());
        company.setDescription(request.getDescription());
        company.setWebsite(request.getWebsite());
        company.setIndustry(request.getIndustry());
        company.setSize(request.getSize());
        company.setHeadquarters(request.getHeadquarters());
        if (request.getLogoUrl()     != null) company.setLogoUrl(request.getLogoUrl());
        if (request.getLinkedinUrl() != null) company.setLinkedinUrl(request.getLinkedinUrl());
        return toResponse(companyRepository.save(company));
    }

    @Override
    @Transactional(readOnly = true)
    public CompanyResponse getCompanyById(Long id) {
        return toResponse(findActive(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CompanyResponse> getAllCompanies(Pageable pageable) {
        return companyRepository.findAllActive(pageable).map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CompanyResponse> searchCompanies(String query, Pageable pageable) {
        return companyRepository.search(query, pageable).map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CompanyResponse> getCompaniesByIndustry(String industry, Pageable pageable) {
        return companyRepository.findByIndustry(industry, pageable).map(this::toResponse);
    }

    @Override
    @Transactional
    public void deleteCompany(Long id) {
        Company company = findActive(id);
        companyRepository.delete(company); // triggers @SQLDelete soft-delete
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Company findActive(Long id) {
        return companyRepository.findActiveById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Company", "id", id));
    }

    private CompanyResponse toResponse(Company c) {
        return CompanyResponse.builder()
                .id(c.getId())
                .name(c.getName())
                .description(c.getDescription())
                .website(c.getWebsite())
                .industry(c.getIndustry())
                .size(c.getSize())
                .headquarters(c.getHeadquarters())
                .logoUrl(c.getLogoUrl())
                .linkedinUrl(c.getLinkedinUrl())
                .activeJobCount(jobRepository.countOpenByCompany(c.getId()))
                .createdAt(c.getCreatedAt())
                .updatedAt(c.getUpdatedAt())
                .build();
    }
}
