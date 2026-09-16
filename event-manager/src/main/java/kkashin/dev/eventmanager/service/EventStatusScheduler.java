package kkashin.dev.eventmanager.service;

import kkashin.dev.eventmanager.model.entity.EventEntity;
import kkashin.dev.eventmanager.model.enums.EventStatus;
import kkashin.dev.eventmanager.model.mappers.EventMapper;
import kkashin.dev.eventmanager.repository.EventRepository;
import kkashin.dev.kafka.EventChangedDto;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

@Service
@RequiredArgsConstructor
public class EventStatusScheduler {
    private final EventRepository repository;
    private final EventMapper mapper;
    private final EventOutboxService outboxService;
    private final Clock clock;

    @Scheduled(fixedDelayString = "${event-manager.scheduler.status-delay-ms:60000}")
    @Transactional
    public void updateStatuses() {
        var now = LocalDateTime.ofInstant(clock.instant(), ZoneId.of("UTC"));
        List<EventChangedDto> kafkaDtos = new ArrayList<>();

        var eventsToStart = repository.findAllByStatusAndDateLessThanEqual(EventStatus.WAIT_START, now);
        var eventsToFinish = repository.findAllByStatus(EventStatus.STARTED).stream()
                .filter(event -> !event.getDate().plusMinutes(event.getDuration()).isAfter(now))
                .toList();

        updateListAndMap(eventsToStart, kafkaDtos, EventStatus.STARTED);
        updateListAndMap(eventsToFinish, kafkaDtos, EventStatus.FINISHED);

        for (EventChangedDto e : kafkaDtos) {
            outboxService.enqueue(e);
        }
    }

    private void updateListAndMap(List<EventEntity> entities, List<EventChangedDto> kafkaDtos, EventStatus status) {
        for (EventEntity e : entities) {
            var mapped = mapper.mapKafkaEventStatus(e, status);

            kafkaDtos.add(mapped);
            e.setStatus(status);
        }
    }
}
