package com.example.midterm.model.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.midterm.model.data.local.AppDatabase;
import com.example.midterm.model.data.local.GuestDAO;
import com.example.midterm.model.entity.Guest;
import com.example.midterm.model.entity.relations.EventGuestCrossRef;
import com.example.midterm.model.entity.relations.GuestWithEvents;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GuestRepository {
    private final GuestDAO guestDAO;
    private final ExecutorService executorService;

    public GuestRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        guestDAO = db.guestDAO();
        executorService = Executors.newSingleThreadExecutor();
    }

    public long insertSync(Guest guest) {
        return guestDAO.insert(guest);
    }
    public void insertEventGuestCrossRef(EventGuestCrossRef crossRef) {
        executorService.execute(() -> guestDAO.insertEventGuestCrossRef(crossRef));
    }

    public void delete(Guest guest) {
        executorService.execute(() -> guestDAO.deleteGuestAndCrossRefs(guest));
    }
    public void deleteGuestsAndCrossRefsByEventId(long eventId) {
        executorService.execute(() -> guestDAO.deleteGuestsAndCrossRefsByEventId(eventId));
    }

    public LiveData<List<Guest>> getGuestsForEvent(int eventId) {
        return guestDAO.getGuestsForEvent(eventId);
    }
}