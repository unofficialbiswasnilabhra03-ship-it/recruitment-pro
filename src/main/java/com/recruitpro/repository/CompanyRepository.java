package com.recruitpro.repository;

import com.recruitpro.entity.Company;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {

    // Soft-delete aware base finders
    @Query("SELECT c FROM Company c WHERE c.deletedAt IS NULL AND c.id = :id")
    Optional<Company> findActiveById(@Param("id") Long id);

    @Query("SELECT c FROM Company c WHERE c.deletedAt IS NULL")
    Page<Company> findAllActive(Pageable pageable);

    @Query("SELECT c FROM Company c WHERE c.deletedAt IS NULL")
    List<Company> findAllActive();

    boolean existsByNameAndDeletedAtIsNull(String name);

    @Query("SELECT c FROM Company c WHERE c.deletedAt IS NULL AND (" +
           "LOWER(c.name)     LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(c.industry) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(c.headquarters) LIKE LOWER(CONCAT('%', :q, '%')))")
    Page<Company> search(@Param("q") String query, Pageable pageable);

    @Query("SELECT c FROM Company c WHERE c.deletedAt IS NULL AND LOWER(c.industry) = LOWER(:industry)")
    Page<Company> findByIndustry(@Param("industry") String industry, Pageable pageable);

    @Query("SELECT COUNT(c) FROM Company c WHERE c.deletedAt IS NULL")
    long countActive();
}
