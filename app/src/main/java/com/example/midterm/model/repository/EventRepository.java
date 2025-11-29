package com.example.midterm.model.repository;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;

import com.example.midterm.model.data.local.AppDatabase;
import com.example.midterm.model.data.local.EventDAO;
import com.example.midterm.model.data.remote.FirestoreHelper; // Import Firebase Helper
import com.example.midterm.model.entity.Event;
import com.example.midterm.model.entity.relations.EventWithGuests;
import com.example.midterm.model.entity.relations.EventWithTicketTypes;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class EventRepository {

    private final EventDAO eventDAO;
    private final FirestoreHelper firestoreHelper; // Khai báo Firebase Helper
    private final ExecutorService executorService;

    public EventRepository(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        eventDAO = db.eventDAO();
        firestoreHelper = new FirestoreHelper(); // Khởi tạo Firebase Helper
        executorService = Executors.newSingleThreadExecutor();

        // Kích hoạt lắng nghe dữ liệu Realtime từ Firebase ngay khi Repository được tạo
        // (Ví dụ: Khi mở app)
        initRealtimeUpdates();
    }

    // --- LOGIC ĐỒNG BỘ REALTIME (SYNC DOWN) ---
    private void initRealtimeUpdates() {
        firestoreHelper.listenForEventUpdates(eventsFromCloud -> {
            executorService.execute(() -> {
                if (eventsFromCloud != null && !eventsFromCloud.isEmpty()) {
                    for (Event event : eventsFromCloud) {
                        // Lưu dữ liệu từ Cloud về Local Room để cập nhật UI
                        // (Dùng conflict strategy REPLACE trong DAO để tự động cập nhật)
                        eventDAO.insert(event);
                    }
                    Log.d("EventRepo", "Đã đồng bộ " + eventsFromCloud.size() + " sự kiện từ Cloud.");
                }
            });
        });
    }

    // --- CÁC HÀM THAO TÁC DỮ LIỆU (SYNC UP) ---

    public void insertEvent(Event event, Consumer<Long> callback) {
        executorService.execute(() -> {
            // 1. Lưu vào Local (Room) trước để lấy ID tự sinh
            long id = eventDAO.insert(event);
            event.setId((int) id); // Cập nhật ID thật cho object

            // 2. Đẩy lên Firebase (Cloud)
            firestoreHelper.syncEventToCloud(event);

            if (callback != null) callback.accept(id);
        });
    }

    public void updateEvent(Event event) {
        executorService.execute(() -> {
            // 1. Cập nhật Local
            eventDAO.update(event);
            // 2. Cập nhật Cloud
            firestoreHelper.syncEventToCloud(event);
        });
    }

    public void deleteEvent(Event event) {
        executorService.execute(() -> {
            // 1. Xóa Local
            eventDAO.delete(event);
            // 2. Xóa trên Cloud
            firestoreHelper.deleteEventFromCloud(event.getId());
        });
    }

    public void deleteEventById(long eventId) {
        executorService.execute(() -> {
            eventDAO.deleteEventById(eventId);
            firestoreHelper.deleteEventFromCloud(eventId);
        });
    }

    // --- CÁC HÀM GET DỮ LIỆU (GIỮ NGUYÊN - VẪN LẤY TỪ ROOM VÌ ROOM ĐÃ ĐƯỢC SYNC) ---

    public LiveData<List<Event>> getEventsByOrganizerId(long organizerId) {
        return eventDAO.getEventsByOrganizerId(organizerId);
    }

    public LiveData<List<Event>> getActiveEventsByOrganizer(long organizerId) {
        return eventDAO.getActiveEventsByOrganizer(organizerId);
    }

    public LiveData<List<Event>> getPastEventsByOrganizer(long organizerId) {
        return eventDAO.getPastEventsByOrganizer(organizerId);
    }

    public LiveData<EventWithTicketTypes> getEventWithTickets(int eventId) {
        return eventDAO.getEventWithTickets(eventId);
    }

    public LiveData<EventWithGuests> getEventWithGuests(int eventId) {
        return eventDAO.getEventWithGuests(eventId);
    }

    public LiveData<List<String>> getBannerUrls() {
        return eventDAO.getBannerUrls();
    }

    public LiveData<List<String>> getAllGenres() {
        return eventDAO.getAllGenres();
    }

    public LiveData<List<Event>> getUpcomingEvents() {
        return eventDAO.getUpcomingEvents();
    }

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