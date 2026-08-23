package kkashin.dev.eventmanager.model.dto.event;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import kkashin.dev.eventmanager.model.enums.EventStatus;

import java.time.LocalDateTime;

public record EventDto(
        @NotNull
        Long id,

        @NotNull
        @NotBlank
        String name,

        @NotNull
        Long ownerId,

        @NotNull
        @Min(0)
        Integer maxPlaces,

        @NotNull
        @Min(0)
        Integer occupiedPlaces,

        @NotNull
        LocalDateTime date,

        @NotNull
        @Min(0)
        Long cost,

        @NotNull
        @Min(0)
        Integer duration,

        @NotNull
        Long locationId,

        @NotNull
        @NotBlank
        EventStatus status
) {
}
