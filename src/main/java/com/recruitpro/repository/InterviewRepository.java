package com.recruitpro.repository;

import com.recruitpro.entity.Interview;
import com.recruitpro.entity.User;
import com.recruitpro.enums.InterviewStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface InterviewRepository extends JpaRepository<Interview, Long> {

    // --- By application ---

    List<Interview> findByJobApplicationId(Long jobApplicationId);

    List<Interview> findByJobApplicationIdAndStatus(Long jobApplicationId, InterviewStatus status);

    // --- By interviewer ---

    Page<Interview> findByInterviewer(User interviewer, Pageable pageable);

    @Query("SELECT i FROM Interview i WHERE i.interviewer = :interviewer AND i.status = :status")
    Page<Interview> findByInterviewerAndStatus(
            @Param("interviewer") User interviewer,
            @Param("status") InterviewStatus status,
            Pageable pageable);

    // --- By candidate (joins through application) ---

    @Query("SELECT i FROM Interview i WHERE i.jobApplication.candidate.id = :candidateId")
    Page<Interview> findByCandidateId(@Param("candidateId") Long candidateId, Pageable pageable);

    @Query("SELECT i FROM Interview i WHERE i.jobApplication.candidate.id = :candidateId " +
           "AND i.status = :status")
    List<Interview> findByCandidateIdAndStatus(
            @Param("candidateId") Long candidateId,
            @Param("status") InterviewStatus status);

    // --- By company ---

    @Query("SELECT i FROM Interview i WHERE i.jobApplication.job.company.id = :companyId")
    Page<Interview> findByCompanyId(@Param("companyId") Long companyId, Pageable pageable);

    // --- InterviewReminderScheduler: find upcoming interviews needing reminder emails ---

    @Query("SELECT i FROM Interview i WHERE i.status = 'SCHEDULED' " +
           "AND i.reminderSent = false " +
           "AND i.scheduledAt BETWEEN :from AND :to")
    List<Interview> findScheduledBetweenWithNoReminder(
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to);

    // --- Upcoming interviews for a specific user (interviewer panel) ---

    @Query("SELECT i FROM Interview i WHERE i.interviewer.id = :userId " +
           "AND i.status = 'SCHEDULED' AND i.scheduledAt >= :now " +
           "ORDER BY i.scheduledAt ASC")
    List<Interview> findUpcomingForInterviewer(
            @Param("userId") Long userId,
            @Param("now") LocalDateTime now);

    // --- Dashboard counts ---

    long countByStatus(InterviewStatus status);

    @Query("SELECT COUNT(i) FROM Interview i WHERE i.jobApplication.job.company.id = :companyId")
    long countByCompanyId(@Param("companyId") Long companyId);

    @Query("SELECT COUNT(i) FROM Interview i WHERE i.jobApplication.job.company.id = :companyId " +
           "AND i.status = :status")
    long countByCompanyIdAndStatus(@Param("companyId") Long companyId, @Param("status") InterviewStatus status);
}
