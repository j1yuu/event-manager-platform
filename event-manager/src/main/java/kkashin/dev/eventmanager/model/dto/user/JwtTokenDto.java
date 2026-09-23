package kkashin.dev.eventmanager.model.dto.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record JwtTokenDto(
        @JsonProperty("jwt_token")
        @NotNull
        @NotBlank
        String jwtToken
) {
}
