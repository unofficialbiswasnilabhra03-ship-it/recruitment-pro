package com.recruitpro.entity;

import com.recruitpro.audit.Auditable;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "candidate_skills")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CandidateSkill extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidate_id", nullable = false)
    private Candidate candidate;

    @Column(name = "skill_name", nullable = false, length = 100)
    private String skillName;

    @Column(name = "proficiency_level", length = 30)
    private String proficiencyLevel; // BEGINNER / INTERMEDIATE / ADVANCED / EXPERT

    @Column(name = "years_of_experience")
    private Integer yearsOfExperience;
}
