package kkashin.dev.eventmanager.service;

import kkashin.dev.eventmanager.exceptions.models.ManagerBadRequestException;
import kkashin.dev.eventmanager.exceptions.models.ManagerNotFoundException;
import kkashin.dev.eventmanager.model.dto.event.EventDto;
import kkashin.dev.eventmanager.model.enums.EventStatus;
import kkashin.dev.eventmanager.model.mappers.EventMapper;
import kkashin.dev.eventmanager.repository.EventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RegistrationService {
    private EventRepository eventRepository;

    private UserService userService;
    private EventMapper eventMapper;

    public RegistrationService(
            UserService userService,
            EventRepository eventRepository,
            EventMapper eventMapper
    ) {
        this.userService = userService;
        this.eventRepository = eventRepository;
        this.eventMapper = eventMapper;
    }

    @Transactional
    public void registerOnEvent(Long eventId) {
        var user = userService.getCurrentUserEntity();
        var event = eventRepository.findByIdAndLock(eventId).orElseThrow(
                () -> new ManagerNotFoundException("Event with given id was not found: %s".formatted(eventId))
        );

        if (!event.getStatus().equals(EventStatus.WAIT_START)) {
            throw new ManagerBadRequestException("Registration on this event is no longer available");
        }

        if (event.getOccupiedPlaces() < event.getMaxPlaces()) {
            throw new ManagerBadRequestException("Event has no available places");
        }

        event.registerUser(user);
    }

    @Transactional
    public void cancelRegistrationOnEvent(Long eventId) {
        var user = userService.getCurrentUserEntity();
        var event = eventRepository.findByIdAndLock(eventId).orElseThrow(
            () -> new ManagerNotFoundException("Event with given id was not found: %s".formatted(eventId))
        );

        if (!event.getStatus().equals(EventStatus.WAIT_START) && !event.getStatus().equals(EventStatus.CANCELLED)) {
            throw new ManagerBadRequestException("You cannot cancel your registration on this event anymore");
        }

        event.cancelRegistration(user);
    }

    public List<EventDto> getMyRegistrations() {
        var user = userService.getCurrentUserEntity();
        var events = user.getRegistrations();

        return events.stream().map(eventMapper::fromEntity).toList();
    }
}
