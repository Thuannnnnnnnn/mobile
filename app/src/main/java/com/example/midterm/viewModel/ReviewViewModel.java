package com.example.midterm.viewModel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.midterm.model.entity.Review;
import com.example.midterm.model.repository.ReviewRepository;

import java.util.List;
import java.util.function.Consumer;

public class ReviewViewModel extends AndroidViewModel {

    private final ReviewRepository reviewRepository;

    public ReviewViewModel(@NonNull Application application) {
        super(application);
        reviewRepository = new ReviewRepository(application);
    }

    public void insert(Review review, Consumer<Long> callback) {
        reviewRepository.insert(review, callback);
    }

    public void update(Review review) {
        reviewRepository.update(review);
    }

    public void delete(Review review) {
        reviewRepository.delete(review);
    }

    public LiveData<List<Review>> getReviewsByEvent(int eventId) {
        return reviewRepository.getReviewsByEvent(eventId);
    }

    public LiveData<List<Review>> getReviewsByUser(int userId) {
        return reviewRepository.getReviewsByUser(userId);
    }

    public LiveData<Float> getAverageRating(int eventId) {
        return reviewRepository.getAverageRating(eventId);
    }

    public LiveData<Integer> getReviewCount(int eventId) {
        return reviewRepository.getReviewCount(eventId);
    }

    public void checkUserReviewed(int eventId, int userId, Consumer<Review> callback) {
        reviewRepository.checkUserReviewed(eventId, userId, callback);
    }
}
