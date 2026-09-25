package kkashin.dev.eventmanager.model.mappers;

import kkashin.dev.eventmanager.model.dto.location.CreateEventLocationDto;
import kkashin.dev.eventmanager.model.dto.location.EventLocationDto;
import kkashin.dev.eventmanager.model.dto.location.UpdateEventLocationDto;
import kkashin.dev.eventmanager.model.entity.EventLocation;
import org.springframework.stereotype.Component;

@Component
public class EventLocationMapper {
    public EventLocation toEntity(CreateEventLocationDto createEventLocationDto) {
        return new EventLocation(
                createEventLocationDto.name(),
                createEventLocationDto.address(),
                createEventLocationDto.capacity(),
                createEventLocationDto.description()
        );
    }

    public EventLocation toEntity(UpdateEventLocationDto updateEventLocationDto) {
        return new EventLocation(
                updateEventLocationDto.name(),
                updateEventLocationDto.address(),
                updateEventLocationDto.capacity(),
                updateEventLocationDto.description()
        );
    }

    public EventLocation toEntity(EventLocationDto eventLocationDto) {
        return new EventLocation(
                eventLocationDto.name(),
                eventLocationDto.address(),
                eventLocationDto.capacity(),
                eventLocationDto.description()
        );
    }

    public EventLocationDto toDto(EventLocation eventLocation) {
        return new EventLocationDto(
                eventLocation.getId(),
                eventLocation.getName(),
                eventLocation.getAddress(),
                eventLocation.getCapacity(),
                eventLocation.getDescription()
        );
    }
}
