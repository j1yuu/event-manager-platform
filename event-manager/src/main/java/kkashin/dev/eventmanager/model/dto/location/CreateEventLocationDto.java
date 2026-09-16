package kkashin.dev.eventmanager.model.dto.location;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateEventLocationDto(
        @NotNull
        @NotBlank
        @Size(max = 255)
        String name,

        @NotNull
        @NotBlank
        @Size(max = 255)
        String address,

        @NotNull
        @Min(5)
        Integer capacity,

        @Nullable
        @Size(max = 2000)
        String description
) {
}
