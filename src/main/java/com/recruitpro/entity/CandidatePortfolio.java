package com.recruitpro.entity;

import com.recruitpro.audit.Auditable;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "candidate_portfolio")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CandidatePortfolio extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidate_id", nullable = false)
    private Candidate candidate;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "url", nullable = false, length = 500)
    private String url;

    @Column(name = "type", length = 50)
    private String type; // e.g. PROJECT, BLOG, OPEN_SOURCE, CERTIFICATION
}
