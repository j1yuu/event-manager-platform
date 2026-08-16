package kkashin.dev.eventmanager.service;

import jakarta.persistence.criteria.Predicate;
import kkashin.dev.eventmanager.exceptions.models.EMBadRequestException;
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
        if (
                filter.placesMin() != null
                        && filter.placesMax() != null
                        && filter.placesMin() > filter.placesMax()
        ) {
            throw new EMBadRequestException("placesMin should be less than placesMax");
        }

        if (
                filter.dateStartBefore() != null
                        && filter.dateStartAfter() != null
                        && filter.dateStartAfter().isAfter(filter.dateStartBefore())
        ) {
            throw new EMBadRequestException("dateAfter should be before dateBefore");
        }

        if (
                filter.costMin() != null
                        && filter.costMax() != null
                        && filter.costMin() > filter.costMax()
        ) {
            throw new EMBadRequestException("costMin should be less than costMax");
        }

        if (
                filter.durationMin() != null
                        && filter.durationMax() != null
                        && filter.durationMin() > filter.durationMax()
        ) {
            throw new EMBadRequestException("durationMin should be less than durationMax");
        }

        return ((root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter.name() != null) {
                predicates.add(
                        cb.like(
                                cb.lower(root.get("name")),
                                filter.name().toLowerCase(Locale.ROOT)
                        )
                );
            }

            if (filter.placesMin() != null) {
                predicates.add(
                        cb.greaterThanOrEqualTo(
                                root.get("maxPlaces"),
                                filter.placesMin()
                        )
                );
            }

            if (filter.placesMax() != null) {
                predicates.add(
                        cb.lessThanOrEqualTo(
                                root.get("maxPlaces"),
                                filter.placesMax()
                        )
                );
            }

            if (filter.costMin() != null) {
                predicates.add(
                        cb.greaterThanOrEqualTo(
                                root.get("cost"),
                                filter.placesMin()
                        )
                );
            }

            if (filter.costMax() != null) {
                predicates.add(
                        cb.lessThanOrEqualTo(
                                root.get("cost"),
                                filter.placesMax()
                        )
                );
            }

            if (filter.durationMin() != null) {
                predicates.add(
                        cb.greaterThanOrEqualTo(
                                root.get("duration"),
                                filter.durationMin()
                        )
                );
            }

            if (filter.durationMax() != null) {
                predicates.add(
                        cb.lessThanOrEqualTo(
                                root.get("duration"),
                                filter.durationMax()
                        )
                );
            }

            if (filter.locationId() != null) {
                predicates.add(
                        cb.equal(
                                root.get("location").get("id"),
                                filter.locationId()
                        )
                );
            }

            if (filter.eventStatus() != null) {
                predicates.add(
                        cb.equal(
                                root.get("eventStatus"),
                                filter.eventStatus()
                        )
                );
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        });
    }
}
