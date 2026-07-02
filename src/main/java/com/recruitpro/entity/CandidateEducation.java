package com.recruitpro.entity;

import com.recruitpro.audit.Auditable;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "candidate_education")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CandidateEducation extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidate_id", nullable = false)
    private Candidate candidate;

    @Column(name = "institution", nullable = false, length = 200)
    private String institution;

    @Column(name = "degree", nullable = false, length = 100)
    private String degree;

    @Column(name = "field_of_study", length = 150)
    private String fieldOfStudy;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "currently_studying", nullable = false)
    @Builder.Default
    private boolean currentlyStudying = false;

    @Column(name = "grade", length = 50)
    private String grade;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
}
