package com.example.midterm.model.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.midterm.model.data.local.AppDatabase;
import com.example.midterm.model.data.local.DiscountDAO;
import com.example.midterm.model.data.local.EventDAO;
import com.example.midterm.model.entity.Discount;
import com.example.midterm.model.entity.Event;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DiscountRepository {
    private DiscountDAO discountDAO;
    private EventDAO eventDAO;
    private ExecutorService executor;

    public DiscountRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        discountDAO = db.discountDAO();
        eventDAO = db.eventDAO();
        executor = Executors.newSingleThreadExecutor();
    }

    public LiveData<List<Discount>> getDiscountsByEvent(int eventId) {
        return discountDAO.getDiscountsByEvent(eventId);
    }

    public LiveData<List<Event>> getAllEvents(int organizerID) {
        return eventDAO.getEventsByOrganizerId(organizerID);
    }

    public void insert(Discount discount, OnActionCallback callback) {
        executor.execute(() -> {
            try {
                discountDAO.insert(discount);
                if (callback != null) callback.onSuccess();
            } catch (Exception e) {
                if (callback != null) callback.onError(e.getMessage());
            }
        });
    }

    public void update(Discount discount, OnActionCallback callback) {
        executor.execute(() -> {
            try {
                discountDAO.update(discount);
                if (callback != null) callback.onSuccess();
            } catch (Exception e) {
                if (callback != null) callback.onError(e.getMessage());
            }
        });
    }

    public void delete(Discount discount, OnActionCallback callback) {
        executor.execute(() -> {
            try {
                discountDAO.delete(discount);
                if (callback != null) callback.onSuccess();
            } catch (Exception e) {
                if (callback != null) callback.onError(e.getMessage());
            }
        });
    }

    // Interface để nhận kết quả trả về UI
    public interface OnActionCallback {
        void onSuccess();
        void onError(String message);
    }
}
