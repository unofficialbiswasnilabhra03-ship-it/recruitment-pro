package com.recruitpro.repository;

import com.recruitpro.entity.Candidate;
import com.recruitpro.entity.User;
import com.recruitpro.enums.ExperienceLevel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CandidateRepository extends JpaRepository<Candidate, Long> {

    @Query("SELECT c FROM Candidate c WHERE c.deletedAt IS NULL AND c.id = :id")
    Optional<Candidate> findActiveById(@Param("id") Long id);

    Optional<Candidate> findByUser(User user);

    Optional<Candidate> findByUserId(Long userId);

    @Query("SELECT c FROM Candidate c WHERE c.deletedAt IS NULL AND c.user.id = :userId")
    Optional<Candidate> findActiveByUserId(@Param("userId") Long userId);

    @Query("SELECT c FROM Candidate c WHERE c.deletedAt IS NULL")
    Page<Candidate> findAllActive(Pageable pageable);

    // --- Search by name / headline / location ---

    @Query("SELECT c FROM Candidate c WHERE c.deletedAt IS NULL AND (" +
           "LOWER(c.user.firstName)       LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(c.user.lastName)        LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(c.headline)             LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(c.currentLocation)      LIKE LOWER(CONCAT('%', :q, '%')))")
    Page<Candidate> search(@Param("q") String query, Pageable pageable);

    // --- Filter by experience level and/or location ---

    @Query("SELECT c FROM Candidate c WHERE c.deletedAt IS NULL AND " +
           "(:expLevel IS NULL OR c.experienceLevel = :expLevel) AND " +
           "(:location IS NULL OR LOWER(c.currentLocation) LIKE LOWER(CONCAT('%', :location, '%')))")
    Page<Candidate> filter(
            @Param("expLevel") ExperienceLevel expLevel,
            @Param("location") String location,
            Pageable pageable);

    // --- Filter candidates who have a specific skill (joins to sub-table) ---

    @Query("SELECT DISTINCT c FROM Candidate c " +
           "JOIN c.skills s " +
           "WHERE c.deletedAt IS NULL AND LOWER(s.skillName) LIKE LOWER(CONCAT('%', :skill, '%'))")
    Page<Candidate> findBySkill(@Param("skill") String skill, Pageable pageable);

    // --- Dashboard ---

    @Query("SELECT COUNT(c) FROM Candidate c WHERE c.deletedAt IS NULL")
    long countActive();

    boolean existsByUserId(Long userId);
}
