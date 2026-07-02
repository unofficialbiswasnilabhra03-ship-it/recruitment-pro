package com.recruitpro.repository;

import com.recruitpro.entity.Job;
import com.recruitpro.entity.User;
import com.recruitpro.enums.ExperienceLevel;
import com.recruitpro.enums.JobStatus;
import com.recruitpro.enums.JobType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface JobRepository extends JpaRepository<Job, Long> {

    // --- Soft-delete aware base finders ---

    @Query("SELECT j FROM Job j WHERE j.deletedAt IS NULL AND j.id = :id")
    Optional<Job> findActiveById(@Param("id") Long id);

    @Query("SELECT j FROM Job j WHERE j.deletedAt IS NULL AND j.status = :status")
    Page<Job> findByStatus(@Param("status") JobStatus status, Pageable pageable);

    @Query("SELECT j FROM Job j WHERE j.deletedAt IS NULL AND j.company.id = :companyId")
    Page<Job> findByCompanyId(@Param("companyId") Long companyId, Pageable pageable);

    @Query("SELECT j FROM Job j WHERE j.deletedAt IS NULL AND j.postedBy = :user")
    Page<Job> findByPostedBy(@Param("user") User user, Pageable pageable);

    // --- Search (keyword hits title, description, requirements) ---

    @Query("SELECT j FROM Job j WHERE j.deletedAt IS NULL AND j.status = 'OPEN' AND (" +
           "LOWER(j.title)       LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(j.description) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(j.requirements) LIKE LOWER(CONCAT('%', :q, '%')))")
    Page<Job> searchOpen(@Param("q") String query, Pageable pageable);

    // --- Multi-criteria filter (all parameters optional via IS NULL OR logic) ---

    @Query("SELECT j FROM Job j WHERE j.deletedAt IS NULL AND j.status = 'OPEN' AND " +
           "(:jobType       IS NULL OR j.jobType = :jobType) AND " +
           "(:expLevel      IS NULL OR j.experienceLevel = :expLevel) AND " +
           "(:location      IS NULL OR LOWER(j.location) LIKE LOWER(CONCAT('%', :location, '%'))) AND " +
           "(:companyId     IS NULL OR j.company.id = :companyId) AND " +
           "(:salaryMin     IS NULL OR j.salaryMax >= :salaryMin) AND " +
           "(:salaryMax     IS NULL OR j.salaryMin <= :salaryMax) AND " +
           "(:keyword       IS NULL OR LOWER(j.title) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Job> filterJobs(
            @Param("jobType")   JobType jobType,
            @Param("expLevel")  ExperienceLevel expLevel,
            @Param("location")  String location,
            @Param("companyId") Long companyId,
            @Param("salaryMin") BigDecimal salaryMin,
            @Param("salaryMax") BigDecimal salaryMax,
            @Param("keyword")   String keyword,
            Pageable pageable);

    // --- Dashboard counts ---

    @Query("SELECT COUNT(j) FROM Job j WHERE j.deletedAt IS NULL")
    long countActive();

    @Query("SELECT COUNT(j) FROM Job j WHERE j.deletedAt IS NULL AND j.status = 'OPEN'")
    long countOpen();

    @Query("SELECT COUNT(j) FROM Job j WHERE j.deletedAt IS NULL AND j.company.id = :companyId AND j.status = 'OPEN'")
    long countOpenByCompany(@Param("companyId") Long companyId);

    // --- Close job (status update without soft delete) ---

    @Query("SELECT j FROM Job j WHERE j.deletedAt IS NULL AND j.status = 'OPEN' AND j.company.id = :companyId")
    List<Job> findOpenJobsByCompany(@Param("companyId") Long companyId);
}
