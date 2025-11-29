package com.example.midterm.model.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.example.midterm.model.data.local.AppDatabase;
import com.example.midterm.model.data.local.ReviewDAO;
import com.example.midterm.model.entity.Review;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class ReviewRepository {

    private final ReviewDAO reviewDAO;
    private final ExecutorService executorService;

    public ReviewRepository(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        reviewDAO = db.reviewDAO();
        executorService = Executors.newSingleThreadExecutor();
    }

    public void insert(Review review, Consumer<Long> callback) {
        executorService.execute(() -> {
            long id = reviewDAO.insert(review);
            if (callback != null) callback.accept(id);
        });
    }

    public void update(Review review) {
        executorService.execute(() -> reviewDAO.update(review));
    }

    public void delete(Review review) {
        executorService.execute(() -> reviewDAO.delete(review));
    }

    public LiveData<List<Review>> getReviewsByEvent(int eventId) {
        return reviewDAO.getReviewsByEvent(eventId);
    }

    public LiveData<List<Review>> getReviewsByUser(int userId) {
        return reviewDAO.getReviewsByUser(userId);
    }

    public LiveData<Float> getAverageRating(int eventId) {
        return reviewDAO.getAverageRating(eventId);
    }

    public LiveData<Integer> getReviewCount(int eventId) {
        return reviewDAO.getReviewCount(eventId);
    }

    public void checkUserReviewed(int eventId, int userId, Consumer<Review> callback) {
        executorService.execute(() -> {
            Review review = reviewDAO.hasUserReviewed(eventId, userId);
            if (callback != null) callback.accept(review);
        });
    }
}
