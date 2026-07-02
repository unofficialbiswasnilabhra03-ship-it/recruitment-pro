package com.recruitpro.dto.response;

import com.recruitpro.enums.ExperienceLevel;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class CandidateResponse {

    private Long id;
    private Long userId;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String headline;
    private String summary;
    private LocalDate dateOfBirth;
    private String currentLocation;
    private String preferredLocation;
    private ExperienceLevel experienceLevel;
    private Integer yearsOfExperience;
    private BigDecimal currentCtc;
    private BigDecimal expectedCtc;
    private Integer noticePeriodDays;
    private String linkedinUrl;
    private String githubUrl;
    private String portfolioUrl;
    private boolean hasResume;
    private List<EducationResponse> educations;
    private List<ExperienceResponse> experiences;
    private List<SkillResponse> skills;
    private List<PortfolioResponse> portfolioItems;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ── Nested response types ─────────────────────────────────────────────────

    @Data
    @Builder
    public static class EducationResponse {
        private Long id;
        private String institution;
        private String degree;
        private String fieldOfStudy;
        private LocalDate startDate;
        private LocalDate endDate;
        private boolean currentlyStudying;
        private String grade;
        private String description;
    }

    @Data
    @Builder
    public static class ExperienceResponse {
        private Long id;
        private String companyName;
        private String jobTitle;
        private String location;
        private LocalDate startDate;
        private LocalDate endDate;
        private boolean currentlyWorking;
        private String description;
    }

    @Data
    @Builder
    public static class SkillResponse {
        private Long id;
        private String skillName;
        private String proficiencyLevel;
        private Integer yearsOfExperience;
    }

    @Data
    @Builder
    public static class PortfolioResponse {
        private Long id;
        private String title;
        private String description;
        private String url;
        private String type;
    }
}
