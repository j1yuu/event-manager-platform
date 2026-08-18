package kkashin.dev.eventmanager.service;

import jakarta.persistence.criteria.Predicate;
import kkashin.dev.eventmanager.exceptions.models.ManagerBadRequestException;
import kkashin.dev.eventmanager.model.dto.event.EventSearchDto;
import kkashin.dev.eventmanager.model.entity.EventEntity;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Component
public class EventSearchFilter {
    public Specification<EventEntity> byFilter(EventSearchDto filter) {
        return ((root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter.name() != null) {
                predicates.add(cb.like(
                        cb.lower(root.get("name")),
                        filter.name().toLowerCase(Locale.ROOT))
                );
            }

            if (filter.placesMin() != null) {
                predicates.add(cb.greaterThanOrEqualTo(
                                root.get("maxPlaces"),
                                filter.placesMin())
                );
            }

            if (filter.placesMax() != null) {
                predicates.add(cb.lessThanOrEqualTo(
                                root.get("maxPlaces"),
                                filter.placesMax())
                );
            }

            if (filter.costMin() != null) {
                predicates.add(cb.greaterThanOrEqualTo(
                                root.get("cost"),
                                filter.costMin())
                );
            }

            if (filter.costMax() != null) {
                predicates.add(cb.lessThanOrEqualTo(
                                root.get("cost"),
                                filter.costMax())
                );
            }

            if (filter.durationMin() != null) {
                predicates.add(cb.greaterThanOrEqualTo(
                                root.get("duration"),
                                filter.durationMin())
                );
            }

            if (filter.durationMax() != null) {
                predicates.add(cb.lessThanOrEqualTo(
                                root.get("duration"),
                                filter.durationMax())
                );
            }

            if (filter.locationId() != null) {
                predicates.add(cb.equal(
                                root.get("location").get("id"),
                                filter.locationId())
                );
            }

            if (filter.eventStatus() != null) {
                predicates.add(cb.equal(
                                root.get("eventStatus"),
                                filter.eventStatus())
                );
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        });
    }
}
