package com.recruitpro.repository;

import com.recruitpro.entity.InterviewFeedback;
import com.recruitpro.enums.HireRecommendation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InterviewFeedbackRepository extends JpaRepository<InterviewFeedback, Long> {

    Optional<InterviewFeedback> findByInterviewId(Long interviewId);

    boolean existsByInterviewId(Long interviewId);

    // All feedback rounds for a given application
    @Query("SELECT f FROM InterviewFeedback f WHERE f.interview.jobApplication.id = :applicationId")
    List<InterviewFeedback> findByApplicationId(@Param("applicationId") Long applicationId);

    // All feedback given by a specific interviewer
    @Query("SELECT f FROM InterviewFeedback f WHERE f.interview.interviewer.id = :interviewerId")
    List<InterviewFeedback> findByInterviewerId(@Param("interviewerId") Long interviewerId);

    // Aggregate recommendation breakdown for an application (used for final decision view)
    @Query("SELECT f.recommendation, COUNT(f) FROM InterviewFeedback f " +
           "WHERE f.interview.jobApplication.id = :applicationId GROUP BY f.recommendation")
    List<Object[]> countRecommendationsByApplication(@Param("applicationId") Long applicationId);

    @Query("SELECT AVG(f.overallScore) FROM InterviewFeedback f " +
           "WHERE f.interview.jobApplication.id = :applicationId")
    Double avgOverallScoreByApplication(@Param("applicationId") Long applicationId);
}
