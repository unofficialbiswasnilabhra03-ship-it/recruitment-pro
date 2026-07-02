package com.recruitpro.repository;

import com.recruitpro.entity.CandidatePortfolio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CandidatePortfolioRepository extends JpaRepository<CandidatePortfolio, Long> {

    List<CandidatePortfolio> findByCandidateId(Long candidateId);

    void deleteByCandidateId(Long candidateId);

    boolean existsByIdAndCandidateId(Long id, Long candidateId);

    List<CandidatePortfolio> findByCandidateIdAndType(Long candidateId, String type);
}
