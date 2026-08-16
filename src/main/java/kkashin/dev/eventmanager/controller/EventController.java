package kkashin.dev.eventmanager.controller;

import jakarta.validation.Valid;
import kkashin.dev.eventmanager.model.dto.event.CreateEventDto;
import kkashin.dev.eventmanager.model.dto.event.EventDto;
import kkashin.dev.eventmanager.model.dto.event.EventSearchDto;
import kkashin.dev.eventmanager.model.dto.event.EventUpdateDto;
import kkashin.dev.eventmanager.service.EventService;
import kkashin.dev.eventmanager.service.RegistrationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/events")
public class EventController {
    private EventService eventService;
    private RegistrationService registrationService;

    public EventController(
            EventService eventService,
            RegistrationService registrationService
    ) {
        this.eventService = eventService;
        this.registrationService = registrationService;
    }

    @PostMapping
    public ResponseEntity<EventDto> createEvent(@RequestBody @Valid CreateEventDto createEventDto) {
        var eventDto = eventService.createEvent(createEventDto);

        return ResponseEntity.status(HttpStatus.CREATED).body(eventDto);
    }

    @DeleteMapping("/{eventId}")
    public ResponseEntity<Void> deleteEvent(@PathVariable Long eventId) {
        eventService.deleteEvent(eventId);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{eventId}")
    public ResponseEntity<EventDto> getEventById(@PathVariable Long eventId) {
        var eventDto = eventService.getEventById(eventId);

        return ResponseEntity.status(HttpStatus.OK).body(eventDto);
    }

    @PutMapping("/{eventId}")
    public ResponseEntity<EventDto> updateEvent(@PathVariable Long eventId, @RequestBody @Valid EventUpdateDto eventUpdateDto) {
        var updatedEventDto = eventService.updateEvent(eventId, eventUpdateDto);

        return ResponseEntity.status(HttpStatus.OK).body(updatedEventDto);
    }

    @PostMapping("/search")
    public ResponseEntity<List<EventDto>> searchEvents(@RequestBody @Valid EventSearchDto searchDto) {
        var foundEvents = eventService.searchEvents(searchDto);

        return ResponseEntity.status(HttpStatus.OK).body(foundEvents);
    }

    @GetMapping("/my")
    public ResponseEntity<List<EventDto>> getMyEvents() {
        var foundEvents = eventService.getMyEvents();

        return ResponseEntity.status(HttpStatus.OK).body(foundEvents);
    }

    @PostMapping("/registrations/{eventId}")
    public ResponseEntity<Void> registerOnEvent(@PathVariable Long eventId) {
        registrationService.registerOnEvent(eventId);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @DeleteMapping("/registrations/{eventId}")
    public ResponseEntity<Void> cancelRegistrationOnEvent(@PathVariable Long eventId) {
        registrationService.cancelRegistrationOnEvent(eventId);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping("/registrations/my")
    public ResponseEntity<List<EventDto>> getMyRegistrations() {
        var registrations = registrationService.getMyRegistrations();

        return ResponseEntity.status(HttpStatus.OK).body(registrations);
    }
}
