package kkashin.dev.eventmanager.model.entity;

import jakarta.persistence.*;
import kkashin.dev.eventmanager.model.enums.OutboxStatus;
import kkashin.dev.kafka.EventChangedDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;

@Entity
@Table(name = "events_outbox")
@Getter
@Setter
@NoArgsConstructor
public class EventOutbox {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", nullable = false)
    private EventChangedDto payload;

    @Enumerated(EnumType.STRING)
    private OutboxStatus status;

    @Column(name = "claim_token", nullable = false)
    private String claimToken;

    @CreationTimestamp
    @Column(name = "createdAt", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "locked_until")
    private Instant lockedUntil;

    public static EventOutbox pending(EventChangedDto dto) {
        var entity = new EventOutbox();

        entity.setPayload(dto);
        entity.setStatus(OutboxStatus.PENDING);

        return entity;
    }
}
