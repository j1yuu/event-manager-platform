package kkashin.dev.kafka;

import java.time.Instant;

public record UserCreatedDto(
        Long userId,
        String loginNormalized,
        Instant submittedAt
) {
}
