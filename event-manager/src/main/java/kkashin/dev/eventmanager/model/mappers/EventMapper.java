package kkashin.dev.eventmanager.model.mappers;

import kkashin.dev.eventmanager.model.dto.event.CreateEventDto;
import kkashin.dev.eventmanager.model.dto.event.EventDto;
import kkashin.dev.eventmanager.model.dto.event.EventUpdateDto;
import kkashin.dev.eventmanager.model.entity.EventEntity;
import kkashin.dev.eventmanager.model.entity.EventLocation;
import kkashin.dev.eventmanager.model.entity.UserEntity;
import kkashin.dev.eventmanager.model.enums.EventStatus;
import kkashin.dev.eventmanager.security.user.User;
import kkashin.dev.kafka.EventChangedDto;
import kkashin.dev.kafka.EventChangedFieldDto;
import kkashin.dev.kafka.EventType;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class EventMapper {
    private final Clock clock;

    public EventMapper(
            Clock clock
    ) {
        this.clock = clock;
    }

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
        entity.setCost(dto.cost());
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

    public EventChangedDto mapKafkaEvent(EventUpdateDto dto, EventEntity source, EventLocation location, User user) {
        List<EventChangedFieldDto> fields = new ArrayList<>();

        if (dto.name() != null) {
            fields.add(new EventChangedFieldDto(
                    "name",
                    source.getName(),
                    dto.name()
            ));
        }

        if (dto.maxPlaces() != null) {
            fields.add(new EventChangedFieldDto(
                    "maxPlaces",
                    source.getMaxPlaces().toString(),
                    dto.maxPlaces().toString()
            ));
        }

        if (dto.date() != null) {
            fields.add(new EventChangedFieldDto(
                    "date",
                    source.getDate().toString(),
                    dto.date().toString()
            ));
        }

        if (dto.cost() != null) {
            fields.add(new EventChangedFieldDto(
                    "cost",
                    source.getCost().toString(),
                    dto.cost().toString()
            ));
        }

        if (dto.duration() != null) {
            fields.add(new EventChangedFieldDto(
                    "duration",
                    source.getDuration().toString(),
                    dto.duration().toString()
            ));
        }

        if (location != null) {
            fields.add(new EventChangedFieldDto(
                    "location",
                    source.getEventLocation().getId().toString(),
                    location.getId().toString()
            ));
        }

        return new EventChangedDto(
                UUID.randomUUID().toString(),
                EventType.EVENT_UPDATED,
                source.getId(),
                clock.instant(),
                source.getName(),
                source.getUser().getId(),
                user.getId(),
                source.getUsers().stream().map(UserEntity::getId).toList(),
                fields
        );
    }

    public EventChangedDto mapKafkaEventStatus(EventEntity source, EventStatus newStatus) {
        List<EventChangedFieldDto> fields = new ArrayList<>();

        fields.add(new EventChangedFieldDto(
                "status",
                source.getStatus().toString(),
                newStatus.name()
        ));

        var eventType = newStatus.equals(EventStatus.CANCELLED)
                ? EventType.EVENT_CLOSED
                : EventType.EVENT_UPDATED;

        return new EventChangedDto(
                UUID.randomUUID().toString(),
                eventType,
                source.getId(),
                clock.instant(),
                source.getName(),
                source.getUser().getId(),
                null,
                source.getUsers().stream().map(UserEntity::getId).toList(),
                fields
        );
    }
}
