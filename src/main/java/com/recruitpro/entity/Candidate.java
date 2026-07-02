package com.recruitpro.entity;

import com.recruitpro.audit.Auditable;
import com.recruitpro.enums.ExperienceLevel;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "candidates")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@org.hibernate.annotations.SQLDelete(sql = "UPDATE candidates SET deleted_at = NOW() WHERE id = ?")
@org.hibernate.annotations.FilterDef(name = "deletedCandidateFilter",
        parameters = @org.hibernate.annotations.ParamDef(name = "isDeleted", type = Boolean.class))
@org.hibernate.annotations.Filter(name = "deletedCandidateFilter", condition = "deleted_at IS NULL")
public class Candidate extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "headline", length = 300)
    private String headline; // e.g. "Java Backend Developer | 3 YOE"

    @Column(name = "summary", columnDefinition = "TEXT")
    private String summary;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "current_location", length = 200)
    private String currentLocation;

    @Column(name = "preferred_location", length = 200)
    private String preferredLocation;

    @Enumerated(EnumType.STRING)
    @Column(name = "experience_level", length = 20)
    private ExperienceLevel experienceLevel;

    @Column(name = "years_of_experience")
    private Integer yearsOfExperience;

    @Column(name = "current_ctc", precision = 12, scale = 2)
    private java.math.BigDecimal currentCtc;

    @Column(name = "expected_ctc", precision = 12, scale = 2)
    private java.math.BigDecimal expectedCtc;

    @Column(name = "notice_period_days")
    private Integer noticePeriodDays;

    @Column(name = "linkedin_url", length = 300)
    private String linkedinUrl;

    @Column(name = "github_url", length = 300)
    private String githubUrl;

    @Column(name = "portfolio_url", length = 300)
    private String portfolioUrl;

    // Soft delete
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    // One resume per candidate (architecture decision)
    @OneToOne(mappedBy = "candidate", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Resume resume;

    // Normalized sub-profile tables
    @OneToMany(mappedBy = "candidate", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @Builder.Default
    private List<CandidateEducation> educations = new ArrayList<>();

    @OneToMany(mappedBy = "candidate", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @Builder.Default
    private List<CandidateExperience> experiences = new ArrayList<>();

    @OneToMany(mappedBy = "candidate", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @Builder.Default
    private List<CandidateSkill> skills = new ArrayList<>();

    @OneToMany(mappedBy = "candidate", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @Builder.Default
    private List<CandidatePortfolio> portfolioItems = new ArrayList<>();

    @OneToMany(mappedBy = "candidate", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<JobApplication> applications = new ArrayList<>();
}
