package kkashin.dev.eventnotificator.model.domain;

import java.util.List;

public record NotificationsChangedBatchEvent(List<Long> userIds) {
}
