package kkashin.dev.eventnotificator.repository;

import kkashin.dev.eventnotificator.model.entity.NotificationPayload;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationPayloadRepository extends JpaRepository<NotificationPayload, Long> {
}
