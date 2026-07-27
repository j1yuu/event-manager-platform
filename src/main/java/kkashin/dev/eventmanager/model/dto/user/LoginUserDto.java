package kkashin.dev.eventmanager.model.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;

public record LoginUserDto(
        @NotNull
        @NotBlank
        @Length(min = 3, max = 36)
        String login,
        @NotNull
        @NotBlank
        @Length(min = 6, max = 256)
        String password
) {
}
