package kkashin.dev.eventnotificator.model.entity;

import jakarta.persistence.*;
import kkashin.dev.kafka.EventChangedFieldDto;
import kkashin.dev.kafka.EventType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "notification_payloads", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"message_id"})
})
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

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false)
    private EventType eventType;

    @Column(name = "event_name", nullable = false)
    private String eventName;

    @Column(name = "message", nullable = false)
    private String message;

    @Column(name = "event_id", nullable = false)
    private Long eventId;

    @Column(name = "changed_by")
    private Long changedBy;

    @Column(name = "owner_id")
    private Long ownerId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private List<EventChangedFieldDto> payload;

    @OneToMany(mappedBy = "payload", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Notification> notifications;

    @Column(name = "occurred_at", nullable = false, updatable = false)
    private Instant occurredAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
