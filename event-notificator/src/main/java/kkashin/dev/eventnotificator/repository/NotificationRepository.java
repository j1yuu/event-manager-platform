package kkashin.dev.eventnotificator.repository;

import kkashin.dev.eventnotificator.model.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    @Modifying
    @Query(value = """
        update notifications
        set is_read = true,
            read_at = current_timestamp
        where user_id = :userId
          and is_read = false
""", nativeQuery = true)
    int markReadByUserId(@Param("userId") Long userId);
}
