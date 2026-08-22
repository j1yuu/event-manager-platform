package kkashin.dev.eventmanager.repository;

import jakarta.persistence.LockModeType;
import kkashin.dev.eventmanager.model.entity.EventEntity;
import kkashin.dev.eventmanager.model.enums.EventStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.time.LocalDateTime;
import java.util.Optional;

public interface EventRepository extends JpaRepository<EventEntity, Long>, JpaSpecificationExecutor<EventEntity> {
    boolean existsByEventLocationIdAndMaxPlacesGreaterThan(Long locationId, Integer capacity);
    boolean existsByEventLocationId(Long locationId);

    List<EventEntity> findAllByStatusAndDateLessThanEqual(
            EventStatus status,
            LocalDateTime date
    );

    List<EventEntity> findAllByStatus(EventStatus status);

    @Query("""
    select ev
    from EventEntity ev
    where ev.user.id = :userId
""")
    List<EventEntity> findAllByUserId(@Param("userId") Long userId);

    @Query("""
    select ev
    from EventEntity ev
    where ev.id = :eventId
""")
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<EventEntity> findByIdAndLock(@Param("eventId") Long eventId);
}
