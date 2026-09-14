package kkashin.dev.eventnotificator.repository;

import kkashin.dev.eventnotificator.model.entity.Notification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    @Modifying
    @Query(value = """
        update notifications
        set is_read = true,
            read_at = :now
        where id in (:notificationIds)
            and is_read = false
            and user_id = :userId
""", nativeQuery = true)
    int markRead(@Param("notificationIds") List<Long> notificationIds, @Param("now") Instant now, @Param("userId") Long userId);

    @Query(value = """
    select n.*
    from notifications n
    join notification_payloads p on p.payload_id = n.payload_id
    where n.user_id = :userId
        and n.is_read = false
    order by n.created_at desc
""", nativeQuery = true)
    List<Notification> getNotificationsByUserId(@Param("userId") Long userId);

    @Modifying
    @Query(value = """
        delete from notifications
        where is_read = true
            and read_at <= :timestapmp
""", nativeQuery = true)
    void removeOldReads(@Param("timestamp") Instant timestamp);
}
