package kkashin.dev.eventmanager.service;

import kkashin.dev.eventmanager.kafka.EventUpdatedProducer;
import kkashin.dev.eventmanager.model.domain.ClaimedEventOutbox;
import kkashin.dev.eventmanager.model.entity.EventOutbox;
import kkashin.dev.eventmanager.model.enums.OutboxStatus;
import kkashin.dev.eventmanager.repository.EventOutboxRepository;
import kkashin.dev.kafka.EventChangedDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

@Service
public class EventOutboxService {

    private final EventOutboxRepository repository;
    private final Clock clock;
    private final Duration lease;

    public EventOutboxService(
            EventOutboxRepository repository,
            Clock clock,
            @Value("${event-manager.scheduler.outbox.lease-duration:5m}")
            Duration lease
    ) {
        this.repository = repository;
        this.clock = clock;
        this.lease = lease;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void enqueue(EventChangedDto dto) {
        repository.save(EventOutbox.pending(dto));
    }

    @Transactional
    public Optional<ClaimedEventOutbox> acquire() {
        var now = clock.instant();

        return repository.claimNext(now).map(entity -> {
            var token = UUID.randomUUID().toString();

            entity.setClaimToken(token);
            entity.setLockedUntil(now.plus(lease));
            entity.setStatus(OutboxStatus.PROCESSING);

            return new ClaimedEventOutbox(entity.getId(), token, entity.getPayload());
        });
    }

    @Scheduled(fixedDelayString = "${event-manager.scheduler.outbox.delay-ms:60000}")
    @Transactional
    public void clearSent() {
        var now = clock.instant();

        repository.clearSent(now);
    }

    @Transactional
    public boolean markSent(ClaimedEventOutbox message) {
        return repository.markSent(message.id(), message.token()) == 1;
    }

    @Transactional
    public boolean release(ClaimedEventOutbox message) {
        return repository.release(message.id(), message.token()) == 1;
    }
}
