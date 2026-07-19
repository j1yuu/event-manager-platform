package kkashin.dev.eventmanager.controller;

import kkashin.dev.eventmanager.model.dto.location.CreateEventLocationDto;
import kkashin.dev.eventmanager.model.dto.location.EventLocationDto;
import kkashin.dev.eventmanager.model.dto.location.UpdateEventLocationDto;
import kkashin.dev.eventmanager.service.EventLocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/locations")
@RequiredArgsConstructor
public class EventLocationsController {
    private final EventLocationService eventLocationService;

    @GetMapping
    public ResponseEntity<List<EventLocationDto>> getAllLocations() {
        var locations = eventLocationService.getAllLocations();

        return ResponseEntity.ok(locations);
    }

    @PostMapping
    public ResponseEntity<EventLocationDto> createLocation(@RequestBody CreateEventLocationDto createEventLocationDto) {
        var location = eventLocationService.createLocation(createEventLocationDto);

        return ResponseEntity.status(HttpStatus.CREATED).body(location);
    }

    @DeleteMapping("/{locationId}")
    public ResponseEntity<Void> deleteLocation(@PathVariable Long locationId) {
        eventLocationService.deleteLocation(locationId);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{locationId}")
    public ResponseEntity<EventLocationDto> getLocation(@PathVariable Long locationId) {
        var location = eventLocationService.getLocation(locationId);

        return ResponseEntity.ok(location);
    }

    @PutMapping("/{locationId}")
    public ResponseEntity<EventLocationDto> updateLocation(
            @PathVariable Long locationId,
            @RequestBody UpdateEventLocationDto updateEventLocationDto
    ) {
        var location = eventLocationService.updateLocation(locationId, updateEventLocationDto);

        return ResponseEntity.ok(location);
    }
}
