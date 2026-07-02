package com.recruitpro.repository;

import com.recruitpro.entity.CandidateEducation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CandidateEducationRepository extends JpaRepository<CandidateEducation, Long> {

    List<CandidateEducation> findByCandidateIdOrderByStartDateDesc(Long candidateId);

    void deleteByCandidateId(Long candidateId);

    boolean existsByIdAndCandidateId(Long id, Long candidateId);
}
