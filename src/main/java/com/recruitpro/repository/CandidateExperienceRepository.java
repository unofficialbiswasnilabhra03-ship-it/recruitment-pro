package com.recruitpro.repository;

import com.recruitpro.entity.CandidateExperience;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CandidateExperienceRepository extends JpaRepository<CandidateExperience, Long> {

    List<CandidateExperience> findByCandidateIdOrderByStartDateDesc(Long candidateId);

    void deleteByCandidateId(Long candidateId);

    boolean existsByIdAndCandidateId(Long id, Long candidateId);
}
