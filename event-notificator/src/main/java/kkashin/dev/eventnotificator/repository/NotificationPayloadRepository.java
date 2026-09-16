package kkashin.dev.eventnotificator.repository;

import kkashin.dev.eventnotificator.model.entity.NotificationPayload;
import kkashin.dev.kafka.EventChangedFieldDto;
import kkashin.dev.kafka.EventType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface NotificationPayloadRepository extends JpaRepository<NotificationPayload, Long> {
    @Modifying
    @Query(value = """
       delete from notification_payloads np
       where not exists (
            select 1
            from notifications n
            where n.payload_id = np.payload_id
       )
""", nativeQuery = true)
    void removeWithoutNotifications();

    @Query(value = """
    insert into notification_payloads (
                                       message_id,
                                       event_type,
                                       event_name,
                                       event_id,
                                       changed_by,
                                       owner_id,
                                       payload,
                                       occurred_at,
                                       created_at
    )
    values (
            :messageId,
            :eventType,
            :eventName,
            :eventId,
            :changedBy,
            :ownerId,
            cast(:payload as jsonb),
            :occurredAt,
            now()
    )
    on conflict (message_id)
    do update set
        payload = excluded.payload
    returning payload_id
""", nativeQuery = true)
    Long insertIfAbsentReturningId(
            @Param("messageId") String messageId,
            @Param("eventType") String eventType,
            @Param("eventName") String eventName,
            @Param("eventId") Long eventId,
            @Param("changedBy") Long changedBy,
            @Param("ownerId") Long ownerId,
            @Param("payload") String payload,
            @Param("occurredAt") Instant occurredAt
    );
}
