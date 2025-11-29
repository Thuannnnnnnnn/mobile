package com.example.midterm.viewModel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.example.midterm.model.entity.Seat;
import com.example.midterm.model.repository.SeatRepository;

import java.util.List;

public class SeatViewModel extends AndroidViewModel {

    private final SeatRepository repository;

    public SeatViewModel(@NonNull Application application) {
        super(application);
        repository = new SeatRepository(application);
    }

    //Lấy LiveData để Activity (CreateSeatMap) quan sát

    public LiveData<List<Seat>> getSeatsBySectionId(long sectionId) {
        return repository.getSeatsBySectionId(sectionId);
    }

    /*** Gọi chèn hàng loạt (cho nút LƯU)*/
    public void insertAll(List<Seat> seats) {
        repository.insertAll(seats);
    }

    /*** Gọi xóa tất cả các ghế của Sự kiện (dùng khi Hủy)*/
    public void deleteSeatsByEventId(long eventId) {
        repository.deleteSeatsByEventId(eventId);
    }
}