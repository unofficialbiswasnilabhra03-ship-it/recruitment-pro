package com.recruitpro.service.interfaces;

import com.recruitpro.dto.request.*;
import com.recruitpro.dto.response.CandidateResponse;
import com.recruitpro.enums.ExperienceLevel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CandidateService {

    // Profile
    CandidateResponse getMyProfile(Long userId);

    CandidateResponse updateMyProfile(Long userId, CandidateProfileRequest request);

    CandidateResponse getCandidateById(Long candidateId);

    Page<CandidateResponse> getAllCandidates(Pageable pageable);

    Page<CandidateResponse> searchCandidates(String query, Pageable pageable);

    Page<CandidateResponse> filterCandidates(ExperienceLevel expLevel, String location, Pageable pageable);

    Page<CandidateResponse> getCandidatesBySkill(String skill, Pageable pageable);

    // Education
    CandidateResponse addEducation(Long userId, CandidateEducationRequest request);

    CandidateResponse updateEducation(Long userId, Long educationId, CandidateEducationRequest request);

    CandidateResponse deleteEducation(Long userId, Long educationId);

    // Experience
    CandidateResponse addExperience(Long userId, CandidateExperienceRequest request);

    CandidateResponse updateExperience(Long userId, Long experienceId, CandidateExperienceRequest request);

    CandidateResponse deleteExperience(Long userId, Long experienceId);

    // Skills
    CandidateResponse addSkill(Long userId, CandidateSkillRequest request);

    CandidateResponse updateSkill(Long userId, Long skillId, CandidateSkillRequest request);

    CandidateResponse deleteSkill(Long userId, Long skillId);

    // Portfolio
    CandidateResponse addPortfolioItem(Long userId, CandidatePortfolioRequest request);

    CandidateResponse updatePortfolioItem(Long userId, Long itemId, CandidatePortfolioRequest request);

    CandidateResponse deletePortfolioItem(Long userId, Long itemId);
}
