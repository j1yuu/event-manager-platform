package kkashin.dev.eventmanager.controller;

import kkashin.dev.eventmanager.model.dto.location.CreateEventLocationDto;
import kkashin.dev.eventmanager.model.dto.location.EventLocationDto;
import kkashin.dev.eventmanager.model.dto.location.UpdateEventLocationDto;
import kkashin.dev.eventmanager.service.EventLocationService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/locations")
@AllArgsConstructor
public class EventLocationsController {
    private EventLocationService eventLocationService;

    @GetMapping
    public List<EventLocationDto> getAllLocations() {
        return eventLocationService.getAllLocations();
    }

    @PostMapping
    public EventLocationDto createLocation(@RequestBody CreateEventLocationDto createEventLocationDto) {
        return eventLocationService.createLocation(createEventLocationDto);
    }

    @DeleteMapping("/{locationId}")
    public void deleteLocation(@PathVariable Long locationId) {
        eventLocationService.deleteLocation(locationId);
    }

    @GetMapping("/{locationId}")
    public EventLocationDto getLocation(@PathVariable Long locationId) {
        return eventLocationService.getLocation(locationId);
    }

    @PutMapping("/{locationId}")
    public EventLocationDto updateLocation(@PathVariable Long locationId, @RequestBody UpdateEventLocationDto updateEventLocationDto) {
        return eventLocationService.updateLocation(locationId, updateEventLocationDto);
    }
}
