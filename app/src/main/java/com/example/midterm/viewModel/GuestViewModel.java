package com.example.midterm.viewModel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.midterm.model.entity.Guest;
import com.example.midterm.model.entity.relations.EventGuestCrossRef;
import com.example.midterm.model.entity.relations.GuestWithEvents;
import com.example.midterm.model.repository.GuestRepository;

import java.util.List;

public class GuestViewModel extends AndroidViewModel {
    private final GuestRepository repository;

    public GuestViewModel(@NonNull Application application) {
        super(application);
        repository = new GuestRepository(application);
    }

    public long insertSync(Guest guest) {
        return repository.insertSync(guest);
    }
    // --- Relationship ---
    public void insertEventGuestCrossRef(EventGuestCrossRef crossRef) {
        repository.insertEventGuestCrossRef(crossRef);
    }

    public void delete(Guest guest) {
        // Gọi Repository, Repository sẽ gọi phương thức giao dịch xóa toàn bộ trong DAO
        repository.delete(guest);
    }
    public void deleteGuestsAndCrossRefsByEventId(long eventId) {
        repository.deleteGuestsAndCrossRefsByEventId(eventId);
    }

    // Lấy tất cả guests theo eventID
    public LiveData<List<Guest>> getGuestsForEvent(int eventId) {
        return repository.getGuestsForEvent(eventId);
    }

}