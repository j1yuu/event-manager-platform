package kkashin.dev.eventmanager.service;

import kkashin.dev.eventmanager.model.enums.EventStatus;
import kkashin.dev.eventmanager.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class EventStatusScheduler {
    private final EventRepository eventRepository;

    @Scheduled(fixedDelayString = "${event-manager.scheduler.status-delay-ms:60000}")
    @Transactional
    public void updateStatuses() {
        var now = LocalDateTime.now();

        eventRepository.findAllByStatusAndDateLessThanEqual(EventStatus.WAIT_START, now)
                .forEach(event -> event.setStatus(EventStatus.STARTED));

        eventRepository.findAllByStatus(EventStatus.STARTED).stream()
                .filter(event -> !event.getDate().plusMinutes(event.getDuration()).isAfter(now))
                .forEach(event -> event.setStatus(EventStatus.FINISHED));
    }
}
