package kkashin.dev.eventmanager.service;

import kkashin.dev.eventmanager.exceptions.models.EMBadRequestException;
import kkashin.dev.eventmanager.exceptions.models.EMNotFoundException;
import kkashin.dev.eventmanager.model.dto.event.CreateEventDto;
import kkashin.dev.eventmanager.model.dto.event.EventDto;
import kkashin.dev.eventmanager.model.dto.event.EventSearchDto;
import kkashin.dev.eventmanager.model.dto.event.EventUpdateDto;
import kkashin.dev.eventmanager.model.entity.EventEntity;
import kkashin.dev.eventmanager.model.entity.EventLocation;
import kkashin.dev.eventmanager.model.mappers.EventMapper;
import kkashin.dev.eventmanager.repository.EventLocationRepository;
import kkashin.dev.eventmanager.repository.EventRepository;
import kkashin.dev.eventmanager.repository.UserRepository;
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
                () -> new EMNotFoundException("Location with given id not found: %s".formatted(createEventDto.locationId()))
        );

        if (createEventDto.maxPlaces() > location.getCapacity()) {
            throw new EMBadRequestException("Event maxPlaces couldn't be more than location capacity");
        }

        var entity = eventMapper.fromCreateDto(createEventDto, user, location);
        var created = eventRepository.save(entity);

        return eventMapper.fromEntity(created);
    }

    @Transactional
    public void deleteEvent(Long eventId) {
        if (!eventRepository.existsById(eventId)) {
            throw new EMNotFoundException("Event with given id was not found: %s".formatted(eventId));
        }

        eventRepository.deleteById(eventId);
    }

    public EventDto getEventById(Long eventId) {
        var event = findEventOrError(eventId);

        return eventMapper.fromEntity(event);
    }

    @Transactional
    public EventDto updateEvent(Long eventId, EventUpdateDto eventUpdateDto) {
        var source = findEventOrError(eventId);

        EventLocation location = source.getEventLocation();
        if (eventUpdateDto.locationId() != null) {
            location = eventLocationRepository.findById(eventUpdateDto.locationId()).orElseThrow(
                    () -> new EMNotFoundException("Location with given id was not found: %s".formatted(eventUpdateDto.locationId()))
            );
        }

        var updated = eventMapper.fromUpdateDto(eventUpdateDto, source, location);
        var saved = eventRepository.save(updated);

        return eventMapper.fromEntity(saved);
    }

    public List<EventDto> searchEvents(EventSearchDto eventSearchDto) {
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
                () -> new EMNotFoundException("Event with given id was not found: %s".formatted(eventId))
        );
    }
}
