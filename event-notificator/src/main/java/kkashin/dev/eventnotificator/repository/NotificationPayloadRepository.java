package kkashin.dev.eventnotificator.repository;

import kkashin.dev.eventnotificator.model.entity.NotificationPayload;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface NotificationPayloadRepository extends JpaRepository<NotificationPayload, Long> {
    @Modifying
    @Query(value = """
       delete from notification_payloads np
       where not exists (
            select 1
            from notifications n
            where n.payload_id = np.payload_id
       )
""")
    void removeWithoutNotifications();
}
