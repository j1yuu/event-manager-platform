package kkashin.dev.kafka;

public record EventChangedFieldDto (
        String field,
        String value,
        String newValue,
        FiledType filedType
) {
}
