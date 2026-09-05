package kkashin.dev.eventmanager.model.domain;

import kkashin.dev.kafka.EventChangedDto;

public record ClaimedEventOutbox(
        Long id,
        String token,
        EventChangedDto dto
) {
}
