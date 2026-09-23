package kkashin.dev.eventmanager.repository;

import kkashin.dev.eventmanager.model.entity.EventLocation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventLocationRepository extends JpaRepository<EventLocation, Long> {
}
