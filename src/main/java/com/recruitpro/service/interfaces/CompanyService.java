package com.recruitpro.service.interfaces;

import com.recruitpro.dto.request.CompanyRequest;
import com.recruitpro.dto.response.CompanyResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CompanyService {

    CompanyResponse createCompany(CompanyRequest request);

    CompanyResponse updateCompany(Long id, CompanyRequest request);

    CompanyResponse getCompanyById(Long id);

    Page<CompanyResponse> getAllCompanies(Pageable pageable);

    Page<CompanyResponse> searchCompanies(String query, Pageable pageable);

    Page<CompanyResponse> getCompaniesByIndustry(String industry, Pageable pageable);

    void deleteCompany(Long id);
}
