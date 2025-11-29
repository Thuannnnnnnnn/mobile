package com.example.midterm.viewModel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.example.midterm.model.entity.EventSection;
import com.example.midterm.model.repository.EventSectionRepository;

import java.util.List;

public class EventSectionViewModel extends AndroidViewModel {

    private final EventSectionRepository repository;

    public EventSectionViewModel(@NonNull Application application) {
        super(application);
        repository = new EventSectionRepository(application);
    }

    /**
     * Lấy LiveData để Activity quan sát (observe)
     * Activity có 'long', nên VM nhận 'long'
     */
    public LiveData<List<EventSection>> getSectionsByEventId(long eventId) {
        return repository.getSectionsByEventId((int) eventId);
    }

    public void insert(EventSection eventSection) {
        repository.insert(eventSection);
    }

    public void update(EventSection eventSection) {
        repository.update(eventSection);
    }

    public void delete(EventSection eventSection) {
        repository.delete(eventSection);
    }

    public void deleteSectionsByEventId(long eventId) {
        repository.deleteSectionsByEventId(eventId);
    }
}