package kkashin.dev.eventmanager.service;

import kkashin.dev.eventmanager.exceptions.models.ManagerForbiddenException;
import kkashin.dev.eventmanager.exceptions.models.ManagerBadRequestException;
import kkashin.dev.eventmanager.exceptions.models.ManagerNotFoundException;
import kkashin.dev.eventmanager.model.dto.event.CreateEventDto;
import kkashin.dev.eventmanager.model.dto.event.EventDto;
import kkashin.dev.eventmanager.model.dto.event.EventSearchDto;
import kkashin.dev.eventmanager.model.dto.event.EventUpdateDto;
import kkashin.dev.eventmanager.model.entity.EventEntity;
import kkashin.dev.eventmanager.model.entity.EventLocation;
import kkashin.dev.eventmanager.model.enums.EventStatus;
import kkashin.dev.eventmanager.model.enums.UserRole;
import kkashin.dev.eventmanager.model.mappers.EventMapper;
import kkashin.dev.eventmanager.repository.EventLocationRepository;
import kkashin.dev.eventmanager.repository.EventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EventService {
    private EventRepository eventRepository;
    private EventLocationRepository eventLocationRepository;

    private EventMapper eventMapper;
    private UserService userService;
    private EventSearchFilter searchFilter;

    public EventService(
            EventRepository eventRepository,
            EventLocationRepository eventLocationRepository,
            EventMapper eventMapper,
            UserService userService,
            EventSearchFilter searchFilter
    ) {
        this.eventRepository = eventRepository;
        this.eventLocationRepository = eventLocationRepository;
        this.eventMapper = eventMapper;
        this.userService = userService;
        this.searchFilter = searchFilter;
    }

    @Transactional
    public EventDto createEvent(CreateEventDto createEventDto) {
        var user = userService.getCurrentUserEntity();
        var location = eventLocationRepository.findById(createEventDto.locationId()).orElseThrow(
                () -> new ManagerNotFoundException("Location with given id not found: %s".formatted(createEventDto.locationId()))
        );

        if (createEventDto.maxPlaces() > location.getCapacity()) {
            throw new ManagerBadRequestException("Event maxPlaces couldn't be more than location capacity");
        }

        var entity = eventMapper.fromCreateDto(createEventDto, user, location);
        var created = eventRepository.save(entity);

        return eventMapper.fromEntity(created);
    }

    @Transactional
    public void deleteEvent(Long eventId) {
        var event = findEventOrError(eventId);
        checkCanManage(event);
        if (event.getStatus() != EventStatus.WAIT_START) {
            throw new ManagerBadRequestException("Only an event waiting to start can be cancelled");
        }
        event.setStatus(EventStatus.CANCELLED);
    }

    public EventDto getEventById(Long eventId) {
        var event = findEventOrError(eventId);

        return eventMapper.fromEntity(event);
    }

    @Transactional
    public EventDto updateEvent(Long eventId, EventUpdateDto eventUpdateDto) {
        var source = findEventOrError(eventId);
        checkCanManage(source);

        EventLocation location = source.getEventLocation();
        if (eventUpdateDto.locationId() != null) {
            location = eventLocationRepository.findById(eventUpdateDto.locationId()).orElseThrow(
                    () -> new ManagerNotFoundException("Location with given id was not found: %s".formatted(eventUpdateDto.locationId()))
            );
        }

        int maxPlaces = eventUpdateDto.maxPlaces() == null ? source.getMaxPlaces() : eventUpdateDto.maxPlaces();
        if (maxPlaces < source.getOccupiedPlaces()) {
            throw new ManagerBadRequestException("Event maxPlaces cannot be less than occupiedPlaces");
        }
        if (maxPlaces > location.getCapacity()) {
            throw new ManagerBadRequestException("Event maxPlaces couldn't be more than location capacity");
        }

        var updated = eventMapper.fromUpdateDto(eventUpdateDto, source, location);
        var saved = eventRepository.save(updated);

        return eventMapper.fromEntity(saved);
    }

    public List<EventDto> searchEvents(EventSearchDto eventSearchDto) {
        if (eventSearchDto.placesMin() != null
                && eventSearchDto.placesMax() != null
                && eventSearchDto.placesMin() > eventSearchDto.placesMax()) {
            throw new ManagerBadRequestException("placesMin should be less than placesMax");
        }

        if (eventSearchDto.dateStartBefore() != null
                && eventSearchDto.dateStartAfter() != null
                && eventSearchDto.dateStartAfter().isAfter(eventSearchDto.dateStartBefore())) {
            throw new ManagerBadRequestException("dateAfter should be before dateBefore");
        }

        if (eventSearchDto.costMin() != null
                && eventSearchDto.costMax() != null
                && eventSearchDto.costMin() > eventSearchDto.costMax()) {
            throw new ManagerBadRequestException("costMin should be less than costMax");
        }

        if (eventSearchDto.durationMin() != null
                && eventSearchDto.durationMax() != null
                && eventSearchDto.durationMin() > eventSearchDto.durationMax()) {
            throw new ManagerBadRequestException("durationMin should be less than durationMax");
        }

        var events = eventRepository.findAll(
                searchFilter.byFilter(eventSearchDto)
        );

        return events.stream().map(eventMapper::fromEntity).toList();
    }

    public List<EventDto> getMyEvents() {
        var currentUser = userService.getCurrentUser();

        var events = eventRepository.findAllByUserId(currentUser.getId());

        return events.stream().map(eventMapper::fromEntity).toList();
    }

    private EventEntity findEventOrError(Long eventId) {
        return eventRepository.findById(eventId).orElseThrow(
                () -> new ManagerNotFoundException("Event with given id was not found: %s".formatted(eventId))
        );
    }

    private void checkCanManage(EventEntity event) {
        var currentUser = userService.getCurrentUser();
        if (currentUser.getUserRole() != UserRole.ADMIN && !event.getUser().getId().equals(currentUser.getId())) {
            throw new ManagerForbiddenException("Only the event owner or an administrator can modify this event");
        }
    }
}
