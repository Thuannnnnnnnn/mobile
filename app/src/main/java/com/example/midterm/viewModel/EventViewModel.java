package com.example.midterm.viewModel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.midterm.model.entity.Event;
import com.example.midterm.model.entity.relations.EventWithGuests;
import com.example.midterm.model.entity.relations.EventWithTicketTypes;
import com.example.midterm.model.repository.EventRepository;

import java.util.List;
import java.util.function.Consumer;

public class EventViewModel extends AndroidViewModel {

    private final EventRepository eventRepository;
    private final LiveData<List<String>> bannerUrls;
    private final LiveData<List<String>> allGenres;
    private final LiveData<List<Event>> upcomingEvents;

    public EventViewModel(@NonNull Application application) {
        super(application);
        eventRepository = new EventRepository(application);

        bannerUrls = eventRepository.getBannerUrls();
        allGenres = eventRepository.getAllGenres();
        upcomingEvents = eventRepository.getUpcomingEvents();
    }
    public void insert(Event event, Consumer<Long> callback) {
        eventRepository.insertEvent(event, callback);
    }

    public void update(Event event) {
        eventRepository.updateEvent(event);
    }

    public void delete(Event event) {
        eventRepository.deleteEvent(event);
    }
    public void deleteEventById(long eventId) {
        eventRepository.deleteEventById(eventId);
    }

    public LiveData<List<Event>> getActiveEvents(long organizerId) {
        return eventRepository.getActiveEventsByOrganizer(organizerId);
    }

    public LiveData<List<Event>> getPastEvents(long organizerId) {
        return eventRepository.getPastEventsByOrganizer(organizerId);
    }

    public LiveData<EventWithTicketTypes> getEventWithTickets(int eventId) {
        return eventRepository.getEventWithTickets(eventId);
    }

    public LiveData<EventWithGuests> getEventWithGuests(int eventId) {
        return eventRepository.getEventWithGuests(eventId);
    }

    /// Phía user
    public LiveData<List<String>> getBannerUrls() {
        return bannerUrls;
    }

    public LiveData<List<String>> getAllGenres() {
        return allGenres;
    }

    public LiveData<List<Event>> getUpcomingEvents() {
        return upcomingEvents;
    }

    // ===== STATISTICS METHODS =====

    public LiveData<List<Event>> getEventsByOrganizerId(int organizerId) {
        return eventRepository.getEventsByOrganizerId(organizerId);
    }

    public LiveData<List<Event>> getActiveEventsByOrganizer(int organizerId) {
        return eventRepository.getActiveEventsByOrganizer(organizerId);
    }

    public LiveData<List<Event>> getPastEventsByOrganizer(int organizerId) {
        return eventRepository.getPastEventsByOrganizer(organizerId);
    }

    public LiveData<Event> getEventById(int eventId) {
        return eventRepository.getEventById(eventId);
    }

    public LiveData<Integer> getTotalEventCount(int organizerId) {
        return eventRepository.getTotalEventCount(organizerId);
    }

    public LiveData<Integer> getActiveEventCount(int organizerId) {
        return eventRepository.getActiveEventCount(organizerId);
    }

    public LiveData<Integer> getPastEventCount(int organizerId) {
        return eventRepository.getPastEventCount(organizerId);
    }

    public LiveData<Integer> getTotalTicketsSoldForEvent(int eventId) {
        return eventRepository.getTotalTicketsSoldForEvent(eventId);
    }

    public LiveData<Double> getTotalRevenueForEvent(int eventId) {
        return eventRepository.getTotalRevenueForEvent(eventId);
    }

    public LiveData<Integer> getTotalCapacityForEvent(int eventId) {
        return eventRepository.getTotalCapacityForEvent(eventId);
    }

    public LiveData<List<Event>> searchEventsByOrganizer(int organizerId, String searchQuery) {
        return eventRepository.searchEventsByOrganizer(organizerId, searchQuery);
    }

    public LiveData<List<Event>> getEventsByGenre(int organizerId, String genre) {
        return eventRepository.getEventsByGenre(organizerId, genre);
    }

    public LiveData<List<Event>> getEventsByDateRange(int organizerId, String startDate, String endDate) {
        return eventRepository.getEventsByDateRange(organizerId, startDate, endDate);
    }

    public LiveData<List<Event>> getDraftEvents(int organizerId) {
        return eventRepository.getDraftEvents(organizerId);
    }

    public LiveData<Double> getTotalRevenueByOrganizer(int organizerId) {
        return eventRepository.getTotalRevenueByOrganizer(organizerId);
    }

    public LiveData<Integer> getTotalTicketsSoldByOrganizer(int organizerId) {
        return eventRepository.getTotalTicketsSoldByOrganizer(organizerId);
    }

    public LiveData<List<Event>> getEventsSortedByPopularity(int organizerId) {
        return eventRepository.getEventsSortedByPopularity(organizerId);
    }

    // ===== USER SEARCH METHODS =====

    public LiveData<List<Event>> searchEvents(String query) {
        return eventRepository.searchEvents(query);
    }

    public LiveData<List<Event>> getEventsByGenreForUser(String genre) {
        return eventRepository.getEventsByGenreForUser(genre);
    }

    public LiveData<List<Event>> getEventsByCity(String city) {
        return eventRepository.getEventsByCity(city);
    }

    public LiveData<List<Event>> getEventsByDateRangeForUser(String startDate, String endDate) {
        return eventRepository.getEventsByDateRangeForUser(startDate, endDate);
    }

    public LiveData<List<String>> getAllCities() {
        return eventRepository.getAllCities();
    }

    public LiveData<List<Event>> getHotEvents() {
        return eventRepository.getHotEvents();
    }

    public LiveData<List<Event>> searchEventsWithFilters(String query, String genre, String city) {
        return eventRepository.searchEventsWithFilters(query, genre, city);
    }
}
