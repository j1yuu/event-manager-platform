package kkashin.dev.eventmanager.model.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;

public record LoginUserDto(
        @NotNull
        @NotBlank
        @Length(min = 3)
        String login,
        @NotNull
        @NotBlank
        @Length(min = 6)
        String password
) {
}
