package kkashin.dev.kafka;

public record EventChangedFieldDto (
        String field,
        String oldValue,
        String newValue
) {
}
