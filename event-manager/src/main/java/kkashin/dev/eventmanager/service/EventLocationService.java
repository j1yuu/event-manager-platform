package kkashin.dev.eventmanager.service;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import kkashin.dev.eventmanager.utils.CacheNames;
import kkashin.dev.exceptions.ManagerBadRequestException;
import kkashin.dev.exceptions.ManagerNotFoundException;
import kkashin.dev.eventmanager.model.dto.location.CreateEventLocationDto;
import kkashin.dev.eventmanager.model.dto.location.EventLocationDto;
import kkashin.dev.eventmanager.model.dto.location.UpdateEventLocationDto;
import kkashin.dev.eventmanager.model.entity.EventLocation;
import kkashin.dev.eventmanager.model.mappers.EventLocationMapper;
import kkashin.dev.eventmanager.repository.EventLocationRepository;
import kkashin.dev.eventmanager.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Validated
@Service
@RequiredArgsConstructor
public class EventLocationService {
    private final EventLocationRepository eventLocationRepository;
    private final EventLocationMapper eventLocationMapper;
    private final EventRepository eventRepository;

    @Cacheable(cacheNames = CacheNames.LOCATIONS_ALL)
    public List<EventLocationDto> getAllLocations() {
        return eventLocationRepository.findAll().stream().map(eventLocationMapper::toDto).toList();
    }

    @Transactional
    @CacheEvict(cacheNames = CacheNames.LOCATIONS_ALL, allEntries = true)
    public EventLocationDto createLocation(@NotNull @Valid CreateEventLocationDto createEventLocationDto) {
        var locationToCreate = eventLocationMapper.toEntity(createEventLocationDto);
        var createdLocation = eventLocationRepository.save(locationToCreate);

        return eventLocationMapper.toDto(createdLocation);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = CacheNames.LOCATIONS_ALL, allEntries = true),
            @CacheEvict(cacheNames = CacheNames.LOCATIONS, key = "#id")
    })
    public void deleteLocation(@NotNull @Positive Long id) {
        var locationToDelete = getLocationOrThrow(id);

        if (eventRepository.existsByEventLocationId(id)) {
            throw new ManagerBadRequestException("A location used by events cannot be deleted");
        }

        eventLocationRepository.delete(locationToDelete);
    }

    @Cacheable(cacheNames = CacheNames.LOCATIONS, key = "#id")
    public EventLocationDto getLocation(@NotNull @Positive Long id) {
        return eventLocationMapper.toDto(getLocationOrThrow(id));
    }

    @Transactional
    @Caching(
            evict = { @CacheEvict(cacheNames = CacheNames.LOCATIONS_ALL, allEntries = true) },
            put = { @CachePut(cacheNames = CacheNames.LOCATIONS, key = "#id") }
    )
    public EventLocationDto updateLocation(
            @NotNull @Positive Long id,
            @NotNull @Valid UpdateEventLocationDto updateEventLocationDto
    ) {
        var locationToUpdate = getLocationOrThrow(id);

        if (eventRepository.existsByEventLocationIdAndMaxPlacesGreaterThan(id, updateEventLocationDto.capacity())) {
            throw new ManagerBadRequestException("Location capacity cannot be less than maxPlaces of its events");
        }

        locationToUpdate.updateDetails(
                updateEventLocationDto.name(),
                updateEventLocationDto.address(),
                updateEventLocationDto.capacity(),
                updateEventLocationDto.description()
        );

        return eventLocationMapper.toDto(locationToUpdate);
    }

    private EventLocation getLocationOrThrow(Long id) {
        return eventLocationRepository.findById(id).orElseThrow(
            () -> new ManagerNotFoundException("Location with given id was not found: %s".formatted(id))
        );
    }
}
