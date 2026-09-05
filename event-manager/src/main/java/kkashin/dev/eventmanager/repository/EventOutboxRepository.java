package kkashin.dev.eventmanager.repository;

import jakarta.persistence.LockModeType;
import kkashin.dev.eventmanager.model.entity.EventOutbox;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface EventOutboxRepository extends JpaRepository<EventOutbox, Long> {

    @Query(value = """
    select e
    from events_outbox e
    where e.status = 'PENDING'
    order by e.created_at desc
    limit 100
""", nativeQuery = true)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<EventOutbox> claimBatch();

    @Modifying
    @Query("""
    update EventOutbox e
    set e.status = kkashin.dev.eventmanager.model.enums.OutboxStatus.PENDING
    where e.status = kkashin.dev.eventmanager.model.enums.OutboxStatus.PROCESSING
        and e.lockedUntil > :now
""")
    void unlockStuck(@Param("now") Instant now);

    @Modifying
    @Query("""
    update EventOutbox e
    set e.status = kkashin.dev.eventmanager.model.enums.OutboxStatus.PENDING
    where e.id = :id
""")
    void unlockById(@Param("id") Long id);

    @Modifying
    @Query("""
    update EventOutbox e
    set e.status = kkashin.dev.eventmanager.model.enums.OutboxStatus.SENT
    where e.id = :id
""")
    void markSent(@Param("id") Long id);
}
