package com.example.midterm.model.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.midterm.model.data.local.AppDatabase;
import com.example.midterm.model.data.local.OrganizerDAO;
import com.example.midterm.model.entity.Organizer;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class OrganizerRepository {

    private final OrganizerDAO organizerDAO;
    private final ExecutorService executorService;

    public OrganizerRepository(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        organizerDAO = db.organizerDAO();
        executorService = Executors.newSingleThreadExecutor();
    }

    public void insert(Organizer organizer) {
        executorService.execute(() -> organizerDAO.insert(organizer));
    }

    public void update(Organizer organizer) {
        executorService.execute(() -> organizerDAO.update(organizer));
    }

    public void delete(Organizer organizer) {
        executorService.execute(() -> organizerDAO.delete(organizer));
    }

    public LiveData<Organizer> getOrganizerByAccountId(int accountId) {
        MutableLiveData<Organizer> data = new MutableLiveData<>();
        executorService.execute(() -> data.postValue(organizerDAO.getOrganizerByAccountId(accountId)));
        return data;
    }

    public LiveData<Organizer> observeOrganizerByAccountId(int accountId) {
        return organizerDAO.observeOrganizerByAccountId(accountId);
    }

    public boolean isOrganizerExist(int organizerId) {
        return organizerDAO.getOrganizerByAccountId(organizerId) != null;
    }
}
