package kkashin.dev.eventmanager.model.dto.event;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import kkashin.dev.eventmanager.model.enums.EventStatus;
import org.hibernate.validator.constraints.Length;

import java.time.LocalDateTime;

public record EventUpdateDto(
        String name,

        @PositiveOrZero
        Integer maxPlaces,

        @FutureOrPresent
        LocalDateTime date,

        EventStatus status,

        @Min(1)
        Long cost,

        @Min(30)
        Integer duration,

        Long locationId
) {
}
