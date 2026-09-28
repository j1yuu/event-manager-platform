package kkashin.dev.eventnotificator.model.domain;

import java.util.Map;

public record NotificationsChangedBatchEvent(Map<Long, Long> incrementsByUserId) {
}
