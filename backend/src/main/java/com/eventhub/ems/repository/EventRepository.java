package com.eventhub.ems.repository;

import com.eventhub.ems.model.Event;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EventRepository extends JpaRepository<Event, Long> {

    /**
     * Search/filter events. Parameters use non-null sentinels (empty string / epoch date)
     * to disable a filter, which keeps the query portable across MySQL, PostgreSQL and H2
     * (PostgreSQL rejects untyped NULL bind parameters in "(:p IS NULL OR ...)" patterns).
     * Matches on title/description text, location, category and an on-or-after date.
     */
    @Query("""
            SELECT DISTINCT e FROM Event e
            WHERE (LOWER(e.title) LIKE LOWER(CONCAT('%', :search, '%'))
                   OR LOWER(e.description) LIKE LOWER(CONCAT('%', :search, '%')))
              AND (:location = '' OR LOWER(e.location) = LOWER(:location))
              AND (:category = '' OR LOWER(e.category) = LOWER(:category))
              AND e.dateTime >= :fromDate
            ORDER BY e.dateTime ASC
            """)
    List<Event> search(@Param("search") String search,
                       @Param("location") String location,
                       @Param("category") String category,
                       @Param("fromDate") LocalDateTime fromDate);

    List<Event> findByDateTimeBetween(LocalDateTime start, LocalDateTime end);
}
