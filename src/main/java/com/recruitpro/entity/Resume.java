package com.recruitpro.entity;

import com.recruitpro.audit.Auditable;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "resumes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Resume extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidate_id", nullable = false, unique = true)
    private Candidate candidate;

    @Column(name = "original_file_name", nullable = false, length = 255)
    private String originalFileName;

    @Column(name = "stored_file_name", nullable = false, length = 255)
    private String storedFileName;

    @Column(name = "file_path", nullable = false)
    private String filePath; // relative path (local) or S3 key (prod)

    @Column(name = "file_size_bytes")
    private Long fileSizeBytes;

    @Column(name = "content_type", length = 100)
    private String contentType; // application/pdf or application/vnd.openxmlformats-officedocument.wordprocessingml.document

    @Column(name = "storage_backend", length = 10)
    @Builder.Default
    private String storageBackend = "LOCAL"; // LOCAL or S3
}
