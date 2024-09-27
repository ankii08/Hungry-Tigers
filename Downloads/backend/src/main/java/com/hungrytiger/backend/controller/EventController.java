package com.hungrytiger.backend.controller;
import com.hungrytiger.backend.service.UserService;

import com.hungrytiger.backend.model.Event;
import com.hungrytiger.backend.model.User;
import com.hungrytiger.backend.service.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/events")
public class EventController {

    @Autowired
    private EventService eventService;

    @Autowired
    private UserService userService;

    /**
     * Create a new event.
     */
    @PostMapping
    public ResponseEntity<?> createEvent(@Valid @RequestBody Event event) {
        // Get the authenticated user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        Optional<User> userOptional = userService.findByEmail(email);

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            event.setPostedBy(user);
            event.setStatus(Event.Status.ACTIVE);
            Event savedEvent = eventService.createEvent(event);
            return ResponseEntity.ok(savedEvent);
        } else {
            return ResponseEntity.status(404).body("User not found");
        }
    }

    /**
     * Get all active events.
     */
    @GetMapping
    public ResponseEntity<List<Event>> getActiveEvents() {
        List<Event> events = eventService.getActiveEvents();
        return ResponseEntity.ok(events);
    }

    /**
     * Update an existing event.
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateEvent(@PathVariable Long id, @Valid @RequestBody Event eventDetails) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        Optional<User> userOptional = userService.findByEmail(email);

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            Event existingEvent = eventService.getEventById(id);

            if (existingEvent.getPostedBy().getId().equals(user.getId())) {
                Event updatedEvent = eventService.updateEvent(id, eventDetails);
                return ResponseEntity.ok(updatedEvent);
            } else {
                return ResponseEntity.status(403).body("You are not authorized to update this event.");
            }
        } else {
            return ResponseEntity.status(404).body("User not found");
        }
    }

    /**
     * Delete an event.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteEvent(@PathVariable Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        Optional<User> userOptional = userService.findByEmail(email);

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            Event existingEvent = eventService.getEventById(id);

            if (existingEvent.getPostedBy().getId().equals(user.getId())) {
                eventService.deleteEvent(id);
                return ResponseEntity.ok("Event deleted successfully.");
            } else {
                return ResponseEntity.status(403).body("You are not authorized to delete this event.");
            }
        } else {
            return ResponseEntity.status(404).body("User not found");
        }
    }
}
