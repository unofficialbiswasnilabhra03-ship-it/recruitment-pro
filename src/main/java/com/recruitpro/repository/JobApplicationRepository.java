package com.recruitpro.repository;

import com.recruitpro.entity.JobApplication;
import com.recruitpro.enums.ApplicationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JobApplicationRepository extends JpaRepository<JobApplication, Long> {

    // --- Duplicate-apply guard (unique constraint backup) ---

    boolean existsByCandidateIdAndJobId(Long candidateId, Long jobId);

    Optional<JobApplication> findByCandidateIdAndJobId(Long candidateId, Long jobId);

    // --- By candidate ---

    Page<JobApplication> findByCandidateId(Long candidateId, Pageable pageable);

    Page<JobApplication> findByCandidateIdAndStatus(Long candidateId, ApplicationStatus status, Pageable pageable);

    List<JobApplication> findByCandidateId(Long candidateId);

    // --- By job ---

    Page<JobApplication> findByJobId(Long jobId, Pageable pageable);

    Page<JobApplication> findByJobIdAndStatus(Long jobId, ApplicationStatus status, Pageable pageable);

    List<JobApplication> findByJobId(Long jobId);

    // --- Shortlisting: applications for a job that HR can shortlist from ---

    @Query("SELECT ja FROM JobApplication ja WHERE ja.job.id = :jobId AND ja.status = 'APPLIED' " +
           "ORDER BY ja.createdAt DESC")
    Page<JobApplication> findPendingReviewForJob(@Param("jobId") Long jobId, Pageable pageable);

    @Query("SELECT ja FROM JobApplication ja WHERE ja.job.id = :jobId AND ja.status = 'SHORTLISTED'")
    List<JobApplication> findShortlistedForJob(@Param("jobId") Long jobId);

    // --- By company (HR view across all company jobs) ---

    @Query("SELECT ja FROM JobApplication ja WHERE ja.job.company.id = :companyId")
    Page<JobApplication> findByCompanyId(@Param("companyId") Long companyId, Pageable pageable);

    @Query("SELECT ja FROM JobApplication ja WHERE ja.job.company.id = :companyId AND ja.status = :status")
    Page<JobApplication> findByCompanyIdAndStatus(
            @Param("companyId") Long companyId,
            @Param("status") ApplicationStatus status,
            Pageable pageable);

    // --- Dashboard counts ---

    long countByJobId(Long jobId);

    long countByStatus(ApplicationStatus status);

    @Query("SELECT COUNT(ja) FROM JobApplication ja WHERE ja.job.company.id = :companyId")
    long countByCompanyId(@Param("companyId") Long companyId);

    @Query("SELECT COUNT(ja) FROM JobApplication ja WHERE ja.job.company.id = :companyId AND ja.status = :status")
    long countByCompanyIdAndStatus(@Param("companyId") Long companyId, @Param("status") ApplicationStatus status);

    @Query("SELECT COUNT(DISTINCT ja.candidate.id) FROM JobApplication ja WHERE ja.job.company.id = :companyId")
    long countDistinctCandidatesByCompany(@Param("companyId") Long companyId);
}
