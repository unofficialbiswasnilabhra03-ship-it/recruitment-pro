package com.recruitpro.entity;

import com.recruitpro.audit.Auditable;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "companies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@org.hibernate.annotations.SQLDelete(sql = "UPDATE companies SET deleted_at = NOW() WHERE id = ?")
@org.hibernate.annotations.FilterDef(name = "deletedCompanyFilter",
        parameters = @org.hibernate.annotations.ParamDef(name = "isDeleted", type = Boolean.class))
@org.hibernate.annotations.Filter(name = "deletedCompanyFilter", condition = "deleted_at IS NULL")
public class Company extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "website", length = 300)
    private String website;

    @Column(name = "industry", length = 100)
    private String industry;

    @Column(name = "size", length = 50)
    private String size; // e.g. "1-50", "51-200", "201-500", "500+"

    @Column(name = "headquarters", length = 200)
    private String headquarters;

    @Column(name = "logo_url")
    private String logoUrl;

    @Column(name = "linkedin_url")
    private String linkedinUrl;

    // Soft delete
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Job> jobs = new ArrayList<>();
}
