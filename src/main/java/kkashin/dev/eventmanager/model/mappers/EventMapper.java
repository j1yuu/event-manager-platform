package kkashin.dev.eventmanager.model.mappers;

import kkashin.dev.eventmanager.model.dto.event.CreateEventDto;
import kkashin.dev.eventmanager.model.dto.event.EventDto;
import kkashin.dev.eventmanager.model.dto.event.EventUpdateDto;
import kkashin.dev.eventmanager.model.entity.EventEntity;
import kkashin.dev.eventmanager.model.entity.EventLocation;
import kkashin.dev.eventmanager.model.entity.UserEntity;
import kkashin.dev.eventmanager.model.enums.EventStatus;
import org.springframework.stereotype.Component;

@Component
public class EventMapper {
    public EventDto fromEntity(EventEntity entity) {
        return new EventDto(
                entity.getId(),
                entity.getName(),
                entity.getUser().getId(),
                entity.getMaxPlaces(),
                entity.getOccupiedPlaces(),
                entity.getDate(),
                entity.getCost(),
                entity.getDuration(),
                entity.getEventLocation().getId(),
                entity.getStatus()
        );
    }

    public EventEntity fromCreateDto(CreateEventDto dto, UserEntity user, EventLocation location) {
        var entity = new EventEntity();

        entity.setName(dto.name());
        entity.setUser(user);
        entity.setMaxPlaces(dto.maxPlaces());
        entity.setOccupiedPlaces(0);
        entity.setDate(dto.date());
        entity.setDuration(dto.duration());
        entity.setEventLocation(location);
        entity.setStatus(EventStatus.WAIT_START);

        return entity;
    }

    public EventEntity fromUpdateDto(EventUpdateDto dto, EventEntity source, EventLocation location) {
        if (dto.name() != null) {
            source.setName(dto.name());
        }

        if (dto.maxPlaces() != null) {
            source.setMaxPlaces(dto.maxPlaces());
        }

        if (dto.date() != null) {
            source.setDate(dto.date());
        }

        if (dto.cost() != null) {
            source.setCost(dto.cost());
        }

        if (dto.duration() != null) {
            source.setDuration(dto.duration());
        }

        if (location != null) {
            source.setEventLocation(location);
        }

        return source;
    }
}
