package kkashin.dev.eventmanager.model.dto.event;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.PositiveOrZero;
import kkashin.dev.eventmanager.model.enums.EventStatus;

import java.time.LocalDateTime;

public record EventSearchDto(
        String name,

        @PositiveOrZero
        Integer placesMin,

        @PositiveOrZero
        Integer placesMax,

        LocalDateTime dateStartAfter,
        LocalDateTime dateStartBefore,

        @Min(1)
        Integer costMin,

        @Min(1)
        Integer costMax,

        @Min(30)
        Integer durationMin,
        @Min(30)
        Integer durationMax,

        Long locationId,
        EventStatus eventStatus
) {
}
