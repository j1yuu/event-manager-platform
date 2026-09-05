package kkashin.dev.eventmanager.service;

import kkashin.dev.eventmanager.kafka.EventUpdatedProducer;
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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Service
public class EventOutboxService {

    private final EventUpdatedProducer producer;
    private final EventOutboxRepository repository;
    private final Clock clock;
    private final Duration timeout;
    private final Duration lease;

    public EventOutboxService(
            EventUpdatedProducer producer,
            EventOutboxRepository repository,
            Clock clock,
            @Value("${event-manager.scheduler.outbox.ack-timeout:30s}")
            Duration timeout,
            @Value("${event-manager.scheduler.outbox.lease-duration:5m}")
            Duration lease
    ) {
        this.producer = producer;
        this.repository = repository;
        this.clock = clock;
        this.timeout = timeout;
        this.lease = lease;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void enqueue(EventChangedDto dto) {
        repository.save(EventOutbox.pending(dto));
    }

    @Scheduled(fixedDelayString = "${event-manager.scheduler.outbox.delay-ms:60000}")
    @Transactional
    public void acquireAndSend() {
        var events = repository.claimBatch();

        for (EventOutbox event : events) {
            var now = clock.instant();

            event.setStatus(OutboxStatus.PROCESSING);
            event.setLockedUntil(now.plusMillis(lease.toMillis()));

            repository.saveAndFlush(event);

            try {
                publishAndAwaitAck(event);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            } catch (ExecutionException | TimeoutException | RuntimeException e) {
                repository.unlockById(event.getId());
                continue;
            }

            repository.markSent(event.getId());
        }
    }

    @Scheduled(fixedDelayString = "${event-manager.scheduler.outbox.delay-ms:60000}")
    @Transactional
    public void unlockStuck() {
        var now = clock.instant();

        repository.unlockStuck(now);
    }

    private void publishAndAwaitAck(EventOutbox message)
            throws ExecutionException, InterruptedException, TimeoutException {
        producer.send(message.getPayload()).get(timeout.toMillis(), TimeUnit.MILLISECONDS);
    }
}
