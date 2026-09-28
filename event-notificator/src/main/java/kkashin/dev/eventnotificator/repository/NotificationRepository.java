package kkashin.dev.eventnotificator.repository;

import kkashin.dev.eventnotificator.model.domain.UserUnreadCountProjection;
import kkashin.dev.eventnotificator.model.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    @Modifying
    @Query(value = """
        update notifications
        set is_read = true,
            read_at = :now
        where notification_id in (:notificationIds)
            and is_read = false
            and user_id = :userId
""", nativeQuery = true)
    int markRead(@Param("notificationIds") List<Long> notificationIds, @Param("now") Instant now, @Param("userId") Long userId);

    @Query("""
    select n
    from Notification n
    join fetch n.payload
    where n.userId = :userId
        and n.isRead = false
    order by n.createdAt desc
""")
    List<Notification> getNotificationsByUserId(@Param("userId") Long userId);

    @Query(value = """
    select count(*)
    from notifications n
    where n.user_id = :userId
        and n.is_read = false
""", nativeQuery = true)
    long getUnreadCountForUserById(@Param("userId") Long userId);

    @Query(value = """
    select user_id as userId, count(*) as unreadCount
    from notifications n
    where user_id in (:userIds)
        and is_read = false
    group by user_id
""", nativeQuery = true)
    List<UserUnreadCountProjection> getUnreadCountByUserId(@Param("userIds") List<Long> userIds);

    @Modifying
    @Query(value = """
        delete from notifications
        where created_at <= :timestamp
""", nativeQuery = true)
    void removeOldReads(@Param("timestamp") Instant timestamp);

    @Modifying
    @Query(value = """
    insert into notifications (
                               user_id,
                               payload_id,
                               is_read,
                               created_at
    )
    select user_id,
           :payloadId,
           false,
           now()
    from unnest(cast(:ids as bigint[])) as user_id
    on conflict (user_id, payload_id)
    do nothing
""", nativeQuery = true)
    void insertIfAbsentBatch(@Param("ids") Long[] ids, @Param("payloadId") Long payloadId);
}
