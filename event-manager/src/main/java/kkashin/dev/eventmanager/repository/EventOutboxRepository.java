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
import java.util.Optional;

public interface EventOutboxRepository extends JpaRepository<EventOutbox, Long> {

    @Query(value = """
    select e.*
    from events_outbox e
    where e.status = 'PENDING'
        or (e.status = 'PROCESSING' and e.locked_until < :now)
    order by e.created_at
    limit 1
    for update skip locked
""", nativeQuery = true)
    Optional<EventOutbox> claimNext(@Param("now") Instant now);

    @Modifying
    @Query(value = """
    delete from events_outbox e
    where e.claim_token = null
        and e.status = 'SENT'
""", nativeQuery = true)
    void clearSent(@Param("now") Instant now);

    @Modifying
    @Query(value = """
    update events_outbox e
    set e.status = 'SENT',
        locked_until = null,
        claim_token = null
    where id = :id
        and status = 'PROCESSING'
        and claim_token = :token
""", nativeQuery = true)
    int markSent(@Param("id") Long id, @Param("token") String token);

    @Modifying
    @Query(value = """
    update events_outbox e
    set e.status = 'PENDING',
        locked_until = null,
        claim_token = null
    where id = :id
        and status = 'PROCESSING'
        and claim_token = :token
""", nativeQuery = true)
    int release(@Param("id") Long id, @Param("token") String token);
}
