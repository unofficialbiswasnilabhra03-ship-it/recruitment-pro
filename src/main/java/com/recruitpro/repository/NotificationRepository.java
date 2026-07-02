package com.recruitpro.repository;

import com.recruitpro.entity.Notification;
import com.recruitpro.enums.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Page<Notification> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    List<Notification> findByUserIdAndReadFalseOrderByCreatedAtDesc(Long userId);

    Page<Notification> findByUserIdAndReadFalseOrderByCreatedAtDesc(Long userId, Pageable pageable);

    long countByUserIdAndReadFalse(Long userId);

    // Mark a single notification as read
    @Modifying
    @Query("UPDATE Notification n SET n.read = true, n.readAt = CURRENT_TIMESTAMP " +
           "WHERE n.id = :id AND n.user.id = :userId")
    int markAsRead(@Param("id") Long id, @Param("userId") Long userId);

    // Mark all notifications for a user as read
    @Modifying
    @Query("UPDATE Notification n SET n.read = true, n.readAt = CURRENT_TIMESTAMP " +
           "WHERE n.user.id = :userId AND n.read = false")
    int markAllAsRead(@Param("userId") Long userId);

    // Filter by type
    Page<Notification> findByUserIdAndTypeOrderByCreatedAtDesc(
            Long userId, NotificationType type, Pageable pageable);

    // Delete old read notifications (housekeeping)
    @Modifying
    @Query("DELETE FROM Notification n WHERE n.user.id = :userId AND n.read = true")
    void deleteReadByUserId(@Param("userId") Long userId);
}
