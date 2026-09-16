package kkashin.dev.eventmanager.model.dto.event;

import jakarta.validation.constraints.*;

import java.time.LocalDateTime;

public record CreateEventDto(
        @NotNull
        @NotBlank
        String name,

        @NotNull
        @PositiveOrZero
        Integer maxPlaces,

        @NotNull
        @FutureOrPresent
        LocalDateTime date,

        @NotNull
        @Min(1)
        Long cost,

        @NotNull
        @Min(30)
        Integer duration,

        @NotNull
        Long locationId
) {
}
