package kkashin.dev.eventmanager.model.dto.user;

import jakarta.validation.constraints.*;
import kkashin.dev.eventmanager.model.enums.UserRole;

public record UserDto(
        @NotNull
        Long id,
        @NotNull
        @NotBlank
        String login,
        @NotNull
        @Min(0)
        @Max(200)
        Integer age,
        @NotNull
        @NotBlank
        UserRole role
) {
}
