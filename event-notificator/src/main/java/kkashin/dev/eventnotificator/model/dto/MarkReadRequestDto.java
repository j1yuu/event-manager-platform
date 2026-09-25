package kkashin.dev.eventnotificator.model.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record MarkReadRequestDto(
        @NotEmpty
        List<@NotNull Long> notificationIds
) {
}
