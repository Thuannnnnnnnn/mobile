package com.example.midterm.model.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.example.midterm.model.data.local.AppDatabase;
import com.example.midterm.model.data.local.EventDAO;
import com.example.midterm.model.entity.Event;
import com.example.midterm.model.entity.relations.EventWithGuests;
import com.example.midterm.model.entity.relations.EventWithTicketTypes;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class EventRepository {

    private final EventDAO eventDAO;
    private final ExecutorService executorService;

    public EventRepository(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        eventDAO = db.eventDAO();
        executorService = Executors.newSingleThreadExecutor();
    }
    public void insertEvent(Event event, Consumer<Long> callback) {
        executorService.execute(() -> {
            long id = eventDAO.insert(event);
            if (callback != null) callback.accept(id);
        });
    }
    public void updateEvent(Event event) {
        executorService.execute(() -> eventDAO.update(event));
    }
    public void deleteEvent(Event event) {
        executorService.execute(() -> eventDAO.delete(event));
    }
    public void deleteEventById(long eventId) {
        executorService.execute(() -> eventDAO.deleteEventById(eventId));
    }

    //Lấy tất cả sự kiện của organizer id đó
    public LiveData<List<Event>> getEventsByOrganizerId(long organizerId) {
        return eventDAO.getEventsByOrganizer(organizerId);
    }
    //Lấy sự kiện đang hoạt động hoặt sắp diễn ra
    public LiveData<List<Event>> getActiveEventsByOrganizer(long organizerId) {
        return eventDAO.getActiveEventsByOrganizer(organizerId);
    }
    //Lấy sự kiện đã kết thúc
    public LiveData<List<Event>> getPastEventsByOrganizer(long organizerId) {
        return eventDAO.getPastEventsByOrganizer(organizerId);
    }

    public LiveData<EventWithTicketTypes> getEventWithTickets(int eventId) {
        return eventDAO.getEventWithTickets(eventId);
    }

    /*** Lấy 1 Sự kiện và danh sách Khách mời của nó*/
    public LiveData<EventWithGuests> getEventWithGuests(int eventId) {
        return eventDAO.getEventWithGuests(eventId);
    }

    /// Phía user

    public LiveData<List<String>> getBannerUrls() {
        return eventDAO.getBannerUrls();
    }

    public LiveData<List<String>> getAllGenres() {
        return eventDAO.getAllGenres();
    }

    public LiveData<List<Event>> getUpcomingEvents() {
        return eventDAO.getUpcomingEvents();
    }

    // ===== STATISTICS METHODS =====

    public LiveData<Event> getEventById(int eventId) {
        return eventDAO.getEventById(eventId);
    }

    public LiveData<Integer> getTotalEventCount(int organizerId) {
        return eventDAO.getTotalEventCount(organizerId);
    }

    public LiveData<Integer> getActiveEventCount(int organizerId) {
        return eventDAO.getActiveEventCount(organizerId);
    }

    public LiveData<Integer> getPastEventCount(int organizerId) {
        return eventDAO.getPastEventCount(organizerId);
    }

    public LiveData<Integer> getTotalTicketsSoldForEvent(int eventId) {
        return eventDAO.getTotalTicketsSoldForEvent(eventId);
    }

    public LiveData<Double> getTotalRevenueForEvent(int eventId) {
        return eventDAO.getTotalRevenueForEvent(eventId);
    }

    public LiveData<Integer> getTotalCapacityForEvent(int eventId) {
        return eventDAO.getTotalCapacityForEvent(eventId);
    }

    public LiveData<List<Event>> searchEventsByOrganizer(int organizerId, String searchQuery) {
        return eventDAO.searchEventsByOrganizer(organizerId, searchQuery);
    }

    public LiveData<List<Event>> getEventsByGenre(int organizerId, String genre) {
        return eventDAO.getEventsByGenre(organizerId, genre);
    }

    public LiveData<List<Event>> getEventsByDateRange(int organizerId, String startDate, String endDate) {
        return eventDAO.getEventsByDateRange(organizerId, startDate, endDate);
    }

    public LiveData<List<Event>> getDraftEvents(int organizerId) {
        return eventDAO.getDraftEvents(organizerId);
    }

    public LiveData<Double> getTotalRevenueByOrganizer(int organizerId) {
        return eventDAO.getTotalRevenueByOrganizer(organizerId);
    }

    public LiveData<Integer> getTotalTicketsSoldByOrganizer(int organizerId) {
        return eventDAO.getTotalTicketsSoldByOrganizer(organizerId);
    }

    public LiveData<List<Event>> getEventsSortedByPopularity(int organizerId) {
        return eventDAO.getEventsSortedByPopularity(organizerId);
    }

    // ===== USER SEARCH METHODS =====

    public LiveData<List<Event>> searchEvents(String query) {
        return eventDAO.searchEvents(query);
    }

    public LiveData<List<Event>> getEventsByGenreForUser(String genre) {
        return eventDAO.getEventsByGenreForUser(genre);
    }

    public LiveData<List<Event>> getEventsByCity(String city) {
        return eventDAO.getEventsByCity(city);
    }

    public LiveData<List<Event>> getEventsByDateRangeForUser(String startDate, String endDate) {
        return eventDAO.getEventsByDateRangeForUser(startDate, endDate);
    }

    public LiveData<List<String>> getAllCities() {
        return eventDAO.getAllCities();
    }

    public LiveData<List<Event>> getHotEvents() {
        return eventDAO.getHotEvents();
    }

    public LiveData<List<Event>> searchEventsWithFilters(String query, String genre, String city) {
        return eventDAO.searchEventsWithFilters(query, genre, city);
    }
}