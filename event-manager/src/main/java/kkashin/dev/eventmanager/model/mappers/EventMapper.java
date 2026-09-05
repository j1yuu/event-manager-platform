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
import kkashin.dev.kafka.FieldType;
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
                    dto.name(),
                    source.getName(),
                    FieldType.String
            ));
        }

        if (dto.maxPlaces() != null) {
            fields.add(new EventChangedFieldDto(
                    "maxPlaces",
                    dto.maxPlaces().toString(),
                    source.getMaxPlaces().toString(),
                    FieldType.Integer
            ));
        }

        if (dto.date() != null) {
            fields.add(new EventChangedFieldDto(
                    "date",
                    dto.date().toString(),
                    source.getDate().toString(),
                    FieldType.LocalDateTime
            ));
        }

        if (dto.cost() != null) {
            fields.add(new EventChangedFieldDto(
                    "cost",
                    dto.cost().toString(),
                    source.getCost().toString(),
                    FieldType.Long
            ));
        }

        if (dto.duration() != null) {
            fields.add(new EventChangedFieldDto(
                    "duration",
                    dto.duration().toString(),
                    source.getDuration().toString(),
                    FieldType.String
            ));
        }

        if (location != null) {
            fields.add(new EventChangedFieldDto(
                    "location",
                    location.getId().toString(),
                    source.getEventLocation().getId().toString(),
                    FieldType.Long
            ));
        }

        return new EventChangedDto(
                UUID.randomUUID().toString(),
                EventType.UPDATE,
                source.getId(),
                clock.instant(),
                source.getUser().getId(),
                user.getId(),
                source.getUsers().stream().map(UserEntity::getId).toList(),
                fields
        );
    }

    public EventChangedDto mapKafkaEventScheduler(EventEntity source, EventStatus newStatus) {
        List<EventChangedFieldDto> fields = new ArrayList<>();

        fields.add(new EventChangedFieldDto(
                "status",
                newStatus.name(),
                source.getStatus().toString(),
                FieldType.String
        ));

        return new EventChangedDto(
                UUID.randomUUID().toString(),
                EventType.UPDATE,
                source.getId(),
                clock.instant(),
                source.getUser().getId(),
                null,
                source.getUsers().stream().map(UserEntity::getId).toList(),
                fields
        );
    }
}
