package com.example.midterm.viewModel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.midterm.model.entity.Discount;
import com.example.midterm.model.entity.Event;
import com.example.midterm.model.repository.DiscountRepository;

import java.util.List;

public class DiscountViewModel extends AndroidViewModel {
    private DiscountRepository repository;

    public DiscountViewModel(@NonNull Application application) {
        super(application);
        repository = new DiscountRepository(application);
    }

    // Lấy danh sách voucher (Quan sát LiveData này từ Activity)
    public LiveData<List<Discount>> getDiscountsByEvent(int eventId) {
        return repository.getDiscountsByEvent(eventId);
    }

    public LiveData<List<Event>> getAllEvents(int organizerId) {
        return repository.getAllEvents(organizerId);
    }

    // Thêm mới
    public void insertDiscount(Discount discount, DiscountRepository.OnActionCallback callback) {
        repository.insert(discount, callback);
    }

    // Cập nhật
    public void updateDiscount(Discount discount, DiscountRepository.OnActionCallback callback) {
        repository.update(discount, callback);
    }

    // Xóa
    public void deleteDiscount(Discount discount, DiscountRepository.OnActionCallback callback) {
        repository.delete(discount, callback);
    }
}