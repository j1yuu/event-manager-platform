package kkashin.dev.eventnotificator.model.entity;

import jakarta.persistence.*;
import kkashin.dev.kafka.EventChangedDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;

@Entity
@Table(name = "notification_payloads")
@Getter
@Setter
@NoArgsConstructor
public class NotificationPayload {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payload_id")
    private Long id;

    @Column(name = "message_id", nullable = false)
    private String messageId;

    @Column(name = "event_type", nullable = false)
    private String eventType;

    @Column(name = "event_id", nullable = false)
    private Long eventId;

    @Column(name = "changed_by")
    private Long changedBy;

    @Column(name = "owner_id")
    private Long ownerId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private EventChangedDto payload;

    @OneToOne(mappedBy = "payload")
    private Notification notification;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
