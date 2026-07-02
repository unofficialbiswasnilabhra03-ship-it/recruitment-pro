package com.recruitpro.repository;

import com.recruitpro.entity.EmailLog;
import com.recruitpro.enums.EmailStatus;
import com.recruitpro.enums.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EmailLogRepository extends JpaRepository<EmailLog, Long> {

    Page<EmailLog> findByRecipientEmailOrderByCreatedAtDesc(String email, Pageable pageable);

    Page<EmailLog> findByStatusOrderByCreatedAtDesc(EmailStatus status, Pageable pageable);

    List<EmailLog> findByStatusAndCreatedAtBefore(EmailStatus status, LocalDateTime before);

    // Emails that failed and can be retried
    @Query("SELECT e FROM EmailLog e WHERE e.status = 'FAILED' AND e.createdAt >= :since")
    List<EmailLog> findRecentFailed(@Param("since") LocalDateTime since);

    // Audit: was a specific email type already sent for a reference entity?
    boolean existsByReferenceIdAndReferenceTypeAndEmailTypeAndStatus(
            Long referenceId, String referenceType,
            NotificationType emailType, EmailStatus status);

    @Query("SELECT COUNT(e) FROM EmailLog e WHERE e.status = :status")
    long countByStatus(@Param("status") EmailStatus status);

    Page<EmailLog> findByEmailTypeOrderByCreatedAtDesc(NotificationType emailType, Pageable pageable);
}
