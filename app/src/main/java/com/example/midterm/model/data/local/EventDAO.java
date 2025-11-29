package com.example.midterm.model.data.local;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.example.midterm.model.entity.Event;
import com.example.midterm.model.entity.relations.EventWithGuests;
import com.example.midterm.model.entity.relations.EventWithTicketTypes;

import java.util.List;

@Dao
public interface EventDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Event event);

    @Update
    void update(Event event);

    @Delete
    void delete(Event event);

    @Query("DELETE FROM events WHERE id = :eventId")
    void deleteEventById(long eventId);

    @Query("SELECT * FROM events WHERE id = :eventId")
    LiveData<Event> getEventById(int eventId);

    @Query("SELECT * FROM events")
    LiveData<List<Event>> getAllEvents();

    @Query("SELECT * FROM events WHERE organizer_id = :organizerId")
    LiveData<List<Event>> getEventsByOrganizer(long organizerId);

    // Get events that are currently promoted and have not ended yet
    @Query("SELECT * FROM events WHERE is_promoted = 1 AND end_date >= date('now', 'localtime') ORDER BY start_date ASC")
    LiveData<List<Event>> getPromotedEvents();

    // Get all upcoming events
    @Query("SELECT * FROM events WHERE start_date >= date('now', 'localtime') ORDER BY start_date ASC")
    LiveData<List<Event>> getUpcomingEvents();

    // Get all past events
    @Query("SELECT * FROM events WHERE end_date < date('now', 'localtime') ORDER BY end_date DESC")
    LiveData<List<Event>> getPastEvents();

    // Search events by name
    @Query("SELECT * FROM events WHERE event_name LIKE :query ORDER BY start_date ASC")
    LiveData<List<Event>> searchEventsByName(String query);

    // Get events by genre
    @Query("SELECT * FROM events WHERE genre = :genre ORDER BY start_date ASC")
    LiveData<List<Event>> getEventsByGenre(String genre);

    // Get events within a date range
    @Query("SELECT * FROM events WHERE start_date BETWEEN :startDate AND :endDate ORDER BY start_date ASC")
    LiveData<List<Event>> getEventsByDateRange(String startDate, String endDate);

    // Transaction to get an event with its associated ticket types
    @Transaction
    @Query("SELECT * FROM events WHERE id = :eventId")
    LiveData<EventWithTicketTypes> getEventWithTickets(int eventId);

    // Transaction to get an event with its associated guests
    @Transaction
    @Query("SELECT * FROM events WHERE id = :eventId")
    LiveData<EventWithGuests> getEventWithGuests(int eventId);

    // Custom queries for EventRepository
    @Query("SELECT * FROM events WHERE organizer_id = :organizerId AND end_date >= date('now', 'localtime')")
    LiveData<List<Event>> getActiveEventsByOrganizer(long organizerId);

    @Query("SELECT * FROM events WHERE organizer_id = :organizerId AND end_date < date('now', 'localtime')")
    LiveData<List<Event>> getPastEventsByOrganizer(long organizerId);

    @Query("SELECT banner_url FROM events")
    LiveData<List<String>> getBannerUrls();

    @Query("SELECT DISTINCT genre FROM events")
    LiveData<List<String>> getAllGenres();

    @Query("SELECT COUNT(*) FROM events WHERE organizer_id = :organizerId")
    LiveData<Integer> getTotalEventCount(int organizerId);

    @Query("SELECT COUNT(*) FROM events WHERE organizer_id = :organizerId AND end_date >= date('now', 'localtime')")
    LiveData<Integer> getActiveEventCount(int organizerId);

    @Query("SELECT COUNT(*) FROM events WHERE organizer_id = :organizerId AND end_date < date('now', 'localtime')")
    LiveData<Integer> getPastEventCount(int organizerId);

    @Query("SELECT SUM(quantity) FROM tickets WHERE event_id = :eventId")
    LiveData<Integer> getTotalTicketsSoldForEvent(int eventId);

    @Query("SELECT SUM(price * quantity) FROM tickets WHERE event_id = :eventId")
    LiveData<Double> getTotalRevenueForEvent(int eventId);

    @Query("SELECT SUM(quantity) FROM ticket_types WHERE event_id = :eventId")
    LiveData<Integer> getTotalCapacityForEvent(int eventId);

    @Query("SELECT * FROM events WHERE organizer_id = :organizerId AND event_name LIKE :searchQuery")
    LiveData<List<Event>> searchEventsByOrganizer(int organizerId, String searchQuery);

    @Query("SELECT * FROM events WHERE organizer_id = :organizerId AND genre = :genre")
    LiveData<List<Event>> getEventsByGenre(int organizerId, String genre);

    @Query("SELECT * FROM events WHERE organizer_id = :organizerId AND start_date BETWEEN :startDate AND :endDate")
    LiveData<List<Event>> getEventsByDateRange(int organizerId, String startDate, String endDate);

    @Query("SELECT * FROM events WHERE organizer_id = :organizerId AND status = 'draft'")
    LiveData<List<Event>> getDraftEvents(int organizerId);

    @Query("SELECT SUM(t.price * t.quantity) FROM events e JOIN tickets t ON e.id = t.event_id WHERE e.organizer_id = :organizerId")
    LiveData<Double> getTotalRevenueByOrganizer(int organizerId);

    @Query("SELECT SUM(t.quantity) FROM events e JOIN tickets t ON e.id = t.event_id WHERE e.organizer_id = :organizerId")
    LiveData<Integer> getTotalTicketsSoldByOrganizer(int organizerId);

    @Query("SELECT e.* FROM events e LEFT JOIN (SELECT event_id, COUNT(*) as ticket_count FROM tickets GROUP BY event_id) t ON e.id = t.event_id WHERE e.organizer_id = :organizerId ORDER BY t.ticket_count DESC")
    LiveData<List<Event>> getEventsSortedByPopularity(int organizerId);

    @Query("SELECT * FROM events WHERE event_name LIKE :query")
    LiveData<List<Event>> searchEvents(String query);

    @Query("SELECT * FROM events WHERE genre = :genre")
    LiveData<List<Event>> getEventsByGenreForUser(String genre);

    @Query("SELECT * FROM events WHERE location LIKE '%' || :city || '%'")
    LiveData<List<Event>> getEventsByCity(String city);

    @Query("SELECT * FROM events WHERE start_date BETWEEN :startDate AND :endDate")
    LiveData<List<Event>> getEventsByDateRangeForUser(String startDate, String endDate);

    @Query("SELECT DISTINCT location FROM events")
    LiveData<List<String>> getAllCities();

    @Query("SELECT * FROM events WHERE is_promoted = 1 ORDER BY start_date ASC")
    LiveData<List<Event>> getHotEvents();

    @Query("SELECT * FROM events WHERE event_name LIKE :query AND genre LIKE :genre AND location LIKE :city")
    LiveData<List<Event>> searchEventsWithFilters(String query, String genre, String city);
}