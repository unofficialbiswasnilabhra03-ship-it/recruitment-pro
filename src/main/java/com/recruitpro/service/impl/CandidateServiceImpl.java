package com.recruitpro.service.impl;

import com.recruitpro.dto.request.*;
import com.recruitpro.dto.response.CandidateResponse;
import com.recruitpro.entity.*;
import com.recruitpro.enums.ExperienceLevel;
import com.recruitpro.exception.ResourceNotFoundException;
import com.recruitpro.exception.UnauthorizedException;
import com.recruitpro.repository.*;
import com.recruitpro.service.interfaces.CandidateService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CandidateServiceImpl implements CandidateService {

    private final CandidateRepository          candidateRepository;
    private final CandidateEducationRepository  educationRepository;
    private final CandidateExperienceRepository experienceRepository;
    private final CandidateSkillRepository      skillRepository;
    private final CandidatePortfolioRepository  portfolioRepository;
    private final ResumeRepository              resumeRepository;

    // ── Profile ───────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public CandidateResponse getMyProfile(Long userId) {
        Candidate candidate = findActiveByUserId(userId);
        return toResponse(candidate);
    }

    @Override
    @Transactional
    public CandidateResponse updateMyProfile(Long userId, CandidateProfileRequest request) {
        Candidate candidate = findActiveByUserId(userId);
        if (request.getHeadline()          != null) candidate.setHeadline(request.getHeadline());
        if (request.getSummary()           != null) candidate.setSummary(request.getSummary());
        if (request.getDateOfBirth()       != null) candidate.setDateOfBirth(request.getDateOfBirth());
        if (request.getCurrentLocation()   != null) candidate.setCurrentLocation(request.getCurrentLocation());
        if (request.getPreferredLocation() != null) candidate.setPreferredLocation(request.getPreferredLocation());
        if (request.getExperienceLevel()   != null) candidate.setExperienceLevel(request.getExperienceLevel());
        if (request.getYearsOfExperience() != null) candidate.setYearsOfExperience(request.getYearsOfExperience());
        if (request.getCurrentCtc()        != null) candidate.setCurrentCtc(request.getCurrentCtc());
        if (request.getExpectedCtc()       != null) candidate.setExpectedCtc(request.getExpectedCtc());
        if (request.getNoticePeriodDays()  != null) candidate.setNoticePeriodDays(request.getNoticePeriodDays());
        if (request.getLinkedinUrl()       != null) candidate.setLinkedinUrl(request.getLinkedinUrl());
        if (request.getGithubUrl()         != null) candidate.setGithubUrl(request.getGithubUrl());
        if (request.getPortfolioUrl()      != null) candidate.setPortfolioUrl(request.getPortfolioUrl());
        return toResponse(candidateRepository.save(candidate));
    }

    @Override
    @Transactional(readOnly = true)
    public CandidateResponse getCandidateById(Long candidateId) {
        return toResponse(findActive(candidateId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CandidateResponse> getAllCandidates(Pageable pageable) {
        return candidateRepository.findAllActive(pageable).map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CandidateResponse> searchCandidates(String query, Pageable pageable) {
        return candidateRepository.search(query, pageable).map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CandidateResponse> filterCandidates(ExperienceLevel expLevel,
                                                      String location, Pageable pageable) {
        return candidateRepository.filter(expLevel, location, pageable).map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CandidateResponse> getCandidatesBySkill(String skill, Pageable pageable) {
        return candidateRepository.findBySkill(skill, pageable).map(this::toResponse);
    }

    // ── Education ─────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public CandidateResponse addEducation(Long userId, CandidateEducationRequest req) {
        Candidate candidate = findActiveByUserId(userId);
        CandidateEducation edu = CandidateEducation.builder()
                .candidate(candidate)
                .institution(req.getInstitution())
                .degree(req.getDegree())
                .fieldOfStudy(req.getFieldOfStudy())
                .startDate(req.getStartDate())
                .endDate(req.getEndDate())
                .currentlyStudying(req.isCurrentlyStudying())
                .grade(req.getGrade())
                .description(req.getDescription())
                .build();
        educationRepository.save(edu);
        return toResponse(findActive(candidate.getId()));
    }

    @Override
    @Transactional
    public CandidateResponse updateEducation(Long userId, Long educationId,
                                              CandidateEducationRequest req) {
        Candidate candidate = findActiveByUserId(userId);
        CandidateEducation edu = educationRepository.findById(educationId)
                .filter(e -> e.getCandidate().getId().equals(candidate.getId()))
                .orElseThrow(() -> new ResourceNotFoundException("Education", "id", educationId));
        edu.setInstitution(req.getInstitution());
        edu.setDegree(req.getDegree());
        edu.setFieldOfStudy(req.getFieldOfStudy());
        edu.setStartDate(req.getStartDate());
        edu.setEndDate(req.getEndDate());
        edu.setCurrentlyStudying(req.isCurrentlyStudying());
        edu.setGrade(req.getGrade());
        edu.setDescription(req.getDescription());
        educationRepository.save(edu);
        return toResponse(findActive(candidate.getId()));
    }

    @Override
    @Transactional
    public CandidateResponse deleteEducation(Long userId, Long educationId) {
        Candidate candidate = findActiveByUserId(userId);
        if (!educationRepository.existsByIdAndCandidateId(educationId, candidate.getId())) {
            throw new ResourceNotFoundException("Education", "id", educationId);
        }
        educationRepository.deleteById(educationId);
        return toResponse(findActive(candidate.getId()));
    }

    // ── Experience ────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public CandidateResponse addExperience(Long userId, CandidateExperienceRequest req) {
        Candidate candidate = findActiveByUserId(userId);
        CandidateExperience exp = CandidateExperience.builder()
                .candidate(candidate)
                .companyName(req.getCompanyName())
                .jobTitle(req.getJobTitle())
                .location(req.getLocation())
                .startDate(req.getStartDate())
                .endDate(req.getEndDate())
                .currentlyWorking(req.isCurrentlyWorking())
                .description(req.getDescription())
                .build();
        experienceRepository.save(exp);
        return toResponse(findActive(candidate.getId()));
    }

    @Override
    @Transactional
    public CandidateResponse updateExperience(Long userId, Long experienceId,
                                               CandidateExperienceRequest req) {
        Candidate candidate = findActiveByUserId(userId);
        CandidateExperience exp = experienceRepository.findById(experienceId)
                .filter(e -> e.getCandidate().getId().equals(candidate.getId()))
                .orElseThrow(() -> new ResourceNotFoundException("Experience", "id", experienceId));
        exp.setCompanyName(req.getCompanyName());
        exp.setJobTitle(req.getJobTitle());
        exp.setLocation(req.getLocation());
        exp.setStartDate(req.getStartDate());
        exp.setEndDate(req.getEndDate());
        exp.setCurrentlyWorking(req.isCurrentlyWorking());
        exp.setDescription(req.getDescription());
        experienceRepository.save(exp);
        return toResponse(findActive(candidate.getId()));
    }

    @Override
    @Transactional
    public CandidateResponse deleteExperience(Long userId, Long experienceId) {
        Candidate candidate = findActiveByUserId(userId);
        if (!experienceRepository.existsByIdAndCandidateId(experienceId, candidate.getId())) {
            throw new ResourceNotFoundException("Experience", "id", experienceId);
        }
        experienceRepository.deleteById(experienceId);
        return toResponse(findActive(candidate.getId()));
    }

    // ── Skills ────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public CandidateResponse addSkill(Long userId, CandidateSkillRequest req) {
        Candidate candidate = findActiveByUserId(userId);
        CandidateSkill skill = CandidateSkill.builder()
                .candidate(candidate)
                .skillName(req.getSkillName())
                .proficiencyLevel(req.getProficiencyLevel())
                .yearsOfExperience(req.getYearsOfExperience())
                .build();
        skillRepository.save(skill);
        return toResponse(findActive(candidate.getId()));
    }

    @Override
    @Transactional
    public CandidateResponse updateSkill(Long userId, Long skillId, CandidateSkillRequest req) {
        Candidate candidate = findActiveByUserId(userId);
        CandidateSkill skill = skillRepository.findById(skillId)
                .filter(s -> s.getCandidate().getId().equals(candidate.getId()))
                .orElseThrow(() -> new ResourceNotFoundException("Skill", "id", skillId));
        skill.setSkillName(req.getSkillName());
        skill.setProficiencyLevel(req.getProficiencyLevel());
        skill.setYearsOfExperience(req.getYearsOfExperience());
        skillRepository.save(skill);
        return toResponse(findActive(candidate.getId()));
    }

    @Override
    @Transactional
    public CandidateResponse deleteSkill(Long userId, Long skillId) {
        Candidate candidate = findActiveByUserId(userId);
        if (!skillRepository.existsByIdAndCandidateId(skillId, candidate.getId())) {
            throw new ResourceNotFoundException("Skill", "id", skillId);
        }
        skillRepository.deleteById(skillId);
        return toResponse(findActive(candidate.getId()));
    }

    // ── Portfolio ─────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public CandidateResponse addPortfolioItem(Long userId, CandidatePortfolioRequest req) {
        Candidate candidate = findActiveByUserId(userId);
        CandidatePortfolio item = CandidatePortfolio.builder()
                .candidate(candidate)
                .title(req.getTitle())
                .description(req.getDescription())
                .url(req.getUrl())
                .type(req.getType())
                .build();
        portfolioRepository.save(item);
        return toResponse(findActive(candidate.getId()));
    }

    @Override
    @Transactional
    public CandidateResponse updatePortfolioItem(Long userId, Long itemId,
                                                  CandidatePortfolioRequest req) {
        Candidate candidate = findActiveByUserId(userId);
        CandidatePortfolio item = portfolioRepository.findById(itemId)
                .filter(p -> p.getCandidate().getId().equals(candidate.getId()))
                .orElseThrow(() -> new ResourceNotFoundException("Portfolio item", "id", itemId));
        item.setTitle(req.getTitle());
        item.setDescription(req.getDescription());
        item.setUrl(req.getUrl());
        item.setType(req.getType());
        portfolioRepository.save(item);
        return toResponse(findActive(candidate.getId()));
    }

    @Override
    @Transactional
    public CandidateResponse deletePortfolioItem(Long userId, Long itemId) {
        Candidate candidate = findActiveByUserId(userId);
        if (!portfolioRepository.existsByIdAndCandidateId(itemId, candidate.getId())) {
            throw new ResourceNotFoundException("Portfolio item", "id", itemId);
        }
        portfolioRepository.deleteById(itemId);
        return toResponse(findActive(candidate.getId()));
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Candidate findActive(Long id) {
        return candidateRepository.findActiveById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Candidate", "id", id));
    }

    private Candidate findActiveByUserId(Long userId) {
        return candidateRepository.findActiveByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Candidate profile not found for user id: " + userId));
    }

    private CandidateResponse toResponse(Candidate c) {
        boolean hasResume = resumeRepository.existsByCandidateId(c.getId());

        List<CandidateResponse.EducationResponse> edus = c.getEducations().stream()
                .map(e -> CandidateResponse.EducationResponse.builder()
                        .id(e.getId()).institution(e.getInstitution()).degree(e.getDegree())
                        .fieldOfStudy(e.getFieldOfStudy()).startDate(e.getStartDate())
                        .endDate(e.getEndDate()).currentlyStudying(e.isCurrentlyStudying())
                        .grade(e.getGrade()).description(e.getDescription()).build())
                .toList();

        List<CandidateResponse.ExperienceResponse> exps = c.getExperiences().stream()
                .map(e -> CandidateResponse.ExperienceResponse.builder()
                        .id(e.getId()).companyName(e.getCompanyName()).jobTitle(e.getJobTitle())
                        .location(e.getLocation()).startDate(e.getStartDate())
                        .endDate(e.getEndDate()).currentlyWorking(e.isCurrentlyWorking())
                        .description(e.getDescription()).build())
                .toList();

        List<CandidateResponse.SkillResponse> skills = c.getSkills().stream()
                .map(s -> CandidateResponse.SkillResponse.builder()
                        .id(s.getId()).skillName(s.getSkillName())
                        .proficiencyLevel(s.getProficiencyLevel())
                        .yearsOfExperience(s.getYearsOfExperience()).build())
                .toList();

        List<CandidateResponse.PortfolioResponse> portfolio = c.getPortfolioItems().stream()
                .map(p -> CandidateResponse.PortfolioResponse.builder()
                        .id(p.getId()).title(p.getTitle()).description(p.getDescription())
                        .url(p.getUrl()).type(p.getType()).build())
                .toList();

        return CandidateResponse.builder()
                .id(c.getId())
                .userId(c.getUser().getId())
                .firstName(c.getUser().getFirstName())
                .lastName(c.getUser().getLastName())
                .email(c.getUser().getEmail())
                .phone(c.getUser().getPhone())
                .headline(c.getHeadline())
                .summary(c.getSummary())
                .dateOfBirth(c.getDateOfBirth())
                .currentLocation(c.getCurrentLocation())
                .preferredLocation(c.getPreferredLocation())
                .experienceLevel(c.getExperienceLevel())
                .yearsOfExperience(c.getYearsOfExperience())
                .currentCtc(c.getCurrentCtc())
                .expectedCtc(c.getExpectedCtc())
                .noticePeriodDays(c.getNoticePeriodDays())
                .linkedinUrl(c.getLinkedinUrl())
                .githubUrl(c.getGithubUrl())
                .portfolioUrl(c.getPortfolioUrl())
                .hasResume(hasResume)
                .educations(edus)
                .experiences(exps)
                .skills(skills)
                .portfolioItems(portfolio)
                .createdAt(c.getCreatedAt())
                .updatedAt(c.getUpdatedAt())
                .build();
    }
}
