package kkashin.dev.eventmanager.model.dto.event;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;

import java.time.LocalDateTime;

public record EventUpdateDto(
        @NotBlank
        String name,

        @PositiveOrZero
        Integer maxPlaces,

        @FutureOrPresent
        LocalDateTime date,

        @Min(1)
        Long cost,

        @Min(30)
        Integer duration,

        Long locationId
) {
}
