package com.example.midterm.model.repository;

import android.app.Application;
import androidx.lifecycle.LiveData;
import com.example.midterm.model.data.local.AppDatabase;
import com.example.midterm.model.data.local.SeatDAO;
import com.example.midterm.model.entity.Seat;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SeatRepository {

    private final SeatDAO seatDAO;
    private final ExecutorService executorService;

    public SeatRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        seatDAO = db.seatDAO();
        executorService = Executors.newSingleThreadExecutor();
    }

    /*** Lấy LiveData danh sách ghế cho một khu vực*/
    public LiveData<List<Seat>> getSeatsBySectionId(long sectionId) {
        return seatDAO.getSeatsBySectionId(sectionId);
    }

    /*** Chèn toàn bộ danh sách ghế (chạy trên luồng riêng)*/
    public void insertAll(List<Seat> seats) {
        executorService.execute(() -> seatDAO.insertAll(seats));
    }

    /*** Xóa tất cả các ghế của một Sự kiện (dùng khi Hủy)*/
    public void deleteSeatsByEventId(long eventId) {
        executorService.execute(() -> seatDAO.deleteSeatsByEventId(eventId));
    }
}
