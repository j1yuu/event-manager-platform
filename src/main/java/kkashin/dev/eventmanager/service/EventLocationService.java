package kkashin.dev.eventmanager.service;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import kkashin.dev.eventmanager.exceptions.models.EMNotFoundException;
import kkashin.dev.eventmanager.model.dto.location.CreateEventLocationDto;
import kkashin.dev.eventmanager.model.dto.location.EventLocationDto;
import kkashin.dev.eventmanager.model.dto.location.UpdateEventLocationDto;
import kkashin.dev.eventmanager.model.entity.EventLocation;
import kkashin.dev.eventmanager.model.mappers.EventLocationMapper;
import kkashin.dev.eventmanager.repository.EventLocationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Validated
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventLocationService {
    private final EventLocationRepository eventLocationRepository;
    private final EventLocationMapper eventLocationMapper;

    public List<EventLocationDto> getAllLocations() {
        return eventLocationRepository.findAll().stream().map(eventLocationMapper::toDto).toList();
    }

    @Transactional
    public EventLocationDto createLocation(@NotNull @Valid CreateEventLocationDto createEventLocationDto) {
        var locationToCreate = eventLocationMapper.toEntity(createEventLocationDto);
        var createdLocation = eventLocationRepository.save(locationToCreate);

        return eventLocationMapper.toDto(createdLocation);
    }

    @Transactional
    public void deleteLocation(@NotNull @Positive Long id) {
        var locationToDelete = getLocationOrThrow(id);

        eventLocationRepository.delete(locationToDelete);
    }

    public EventLocationDto getLocation(@NotNull @Positive Long id) {
        return eventLocationMapper.toDto(getLocationOrThrow(id));
    }

    @Transactional
    public EventLocationDto updateLocation(
            @NotNull @Positive Long id,
            @NotNull @Valid UpdateEventLocationDto updateEventLocationDto
    ) {
        var locationToUpdate = getLocationOrThrow(id);

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
            () -> new EMNotFoundException("Location with given id was not found: %s".formatted(id))
        );
    }
}
