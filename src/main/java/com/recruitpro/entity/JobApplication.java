package com.recruitpro.entity;

import com.recruitpro.audit.Auditable;
import com.recruitpro.enums.ApplicationStatus;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "job_applications",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"candidate_id", "job_id"})
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobApplication extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidate_id", nullable = false)
    private Candidate candidate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", nullable = false)
    private Job job;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    @Builder.Default
    private ApplicationStatus status = ApplicationStatus.APPLIED;

    @Column(name = "cover_letter", columnDefinition = "TEXT")
    private String coverLetter;

    @Column(name = "hr_notes", columnDefinition = "TEXT")
    private String hrNotes; // internal notes by HR, not visible to candidate

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    @OneToMany(mappedBy = "jobApplication", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Interview> interviews = new ArrayList<>();
}
