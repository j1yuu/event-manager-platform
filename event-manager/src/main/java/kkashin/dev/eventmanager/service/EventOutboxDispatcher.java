package kkashin.dev.eventmanager.service;

import kkashin.dev.eventmanager.kafka.EventUpdatedProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Service
public class EventOutboxDispatcher {

    private static final Logger log = LoggerFactory.getLogger(EventOutboxDispatcher.class);

    private final EventUpdatedProducer producer;
    private final Duration timeout;
    private final EventOutboxService service;

    public EventOutboxDispatcher(
            EventUpdatedProducer producer,
            EventOutboxService service,
            @Value("${event-manager.scheduler.outbox.ack-timeout:30s}")
            Duration timeout
    ) {
        this.producer = producer;
        this.service = service;
        this.timeout = timeout;
    }

    @Scheduled(fixedDelayString = "${event-manager.scheduler.outbox.delay-ms:60000}")
    public void dispatch() {
        for (int i = 0; i < 100; i++) {
            var claimed = service.acquire();

            if (claimed.isEmpty()) return;

            var message = claimed.get();

            try {
                producer.send(message.dto())
                        .get(timeout.toMillis(), TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            } catch (ExecutionException | TimeoutException | RuntimeException e) {
                log.error("Failed to publish outbox message {}", message.id(), e);

                service.release(message);
                return;
            }

            if (!service.markSent(message)) {
                log.warn(
                        "Outbox message {} was published, but its claim is no longer owned; duplicate delivery is possible",
                        message.id()
                );
            };
        }
    }
}
