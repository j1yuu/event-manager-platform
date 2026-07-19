package kkashin.dev.eventmanager.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

public record HttpExceptionDto(
        @JsonProperty("message")
        String message,
        @JsonProperty("detailed_message")
        String detailedMessage,
        @JsonProperty("date_time")
        LocalDateTime dateTime
) {
}
