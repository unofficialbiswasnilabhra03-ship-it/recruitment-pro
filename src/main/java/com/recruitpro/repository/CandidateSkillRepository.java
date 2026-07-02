package com.recruitpro.repository;

import com.recruitpro.entity.CandidateSkill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CandidateSkillRepository extends JpaRepository<CandidateSkill, Long> {

    List<CandidateSkill> findByCandidateId(Long candidateId);

    void deleteByCandidateId(Long candidateId);

    boolean existsByIdAndCandidateId(Long id, Long candidateId);

    boolean existsByCandidateIdAndSkillNameIgnoreCase(Long candidateId, String skillName);

    @Query("SELECT DISTINCT s.skillName FROM CandidateSkill s ORDER BY s.skillName")
    List<String> findAllDistinctSkillNames();

    @Query("SELECT s FROM CandidateSkill s WHERE s.candidate.id = :candidateId " +
           "AND LOWER(s.skillName) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<CandidateSkill> searchByName(@Param("candidateId") Long candidateId, @Param("name") String name);
}
