package com.recruitpro.repository;

import com.recruitpro.entity.Resume;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ResumeRepository extends JpaRepository<Resume, Long> {

    Optional<Resume> findByCandidateId(Long candidateId);

    @Query("SELECT r FROM Resume r WHERE r.candidate.user.id = :userId")
    Optional<Resume> findByUserId(@Param("userId") Long userId);

    boolean existsByCandidateId(Long candidateId);

    void deleteByCandidateId(Long candidateId);
}
