package com.recruitpro.entity;

import com.recruitpro.audit.Auditable;
import com.recruitpro.enums.HireRecommendation;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "interview_feedback")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InterviewFeedback extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "interview_id", nullable = false, unique = true)
    private Interview interview;

    @Column(name = "technical_score")
    private Integer technicalScore; // 1-10

    @Column(name = "communication_score")
    private Integer communicationScore; // 1-10

    @Column(name = "problem_solving_score")
    private Integer problemSolvingScore; // 1-10

    @Column(name = "cultural_fit_score")
    private Integer culturalFitScore; // 1-10

    @Column(name = "overall_score")
    private Integer overallScore; // 1-10

    @Column(name = "strengths", columnDefinition = "TEXT")
    private String strengths;

    @Column(name = "weaknesses", columnDefinition = "TEXT")
    private String weaknesses;

    @Column(name = "detailed_notes", columnDefinition = "TEXT")
    private String detailedNotes;

    /**
     * Final Result per round (architecture decision: lives on feedback, not on application).
     * Rollup to OFFER_EXTENDED / HIRED is done at the Application level when HR
     * reviews all feedback rounds and makes the final call.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "recommendation", nullable = false, length = 25)
    private HireRecommendation recommendation;
}
