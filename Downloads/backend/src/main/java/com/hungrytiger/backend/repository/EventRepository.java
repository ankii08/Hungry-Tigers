package com.hungrytiger.backend.repository;

import com.hungrytiger.backend.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findByStatus(Event.Status status);
}
