package com.example.midterm.model.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;
import com.example.midterm.model.data.local.AppDatabase;
import com.example.midterm.model.data.local.EventSectionDAO;
import com.example.midterm.model.entity.EventSection;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EventSectionRepository {
    private final EventSectionDAO eventSectionDAO;
    private final ExecutorService executorService;

    public EventSectionRepository(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        eventSectionDAO = db.eventSectionDAO();
        executorService = Executors.newSingleThreadExecutor();
    }

    public LiveData<List<EventSection>> getSectionsByEventId(int eventId) {
        return eventSectionDAO.getSectionsByEventId(eventId);
    }

    public void insert(EventSection eventSection) {
        executorService.execute(() -> eventSectionDAO.insert(eventSection));
    }

    public void update(EventSection eventSection) {
        executorService.execute(() -> eventSectionDAO.update(eventSection));
    }

    public void delete(EventSection eventSection) {
        executorService.execute(() -> eventSectionDAO.delete(eventSection));
    }

    // Dùng cho logic Hủy
    public void deleteSectionsByEventId(long eventId) {
        executorService.execute(() -> eventSectionDAO.deleteSectionsByEventId(eventId));
    }
}