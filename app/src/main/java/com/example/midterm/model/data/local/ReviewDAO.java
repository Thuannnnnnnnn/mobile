package com.example.midterm.model.data.local;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.midterm.model.entity.Review;

import java.util.List;

@Dao
public interface ReviewDAO {
    @Insert
    long insert(Review review);

    @Update
    void update(Review review);

    @Delete
    void delete(Review review);

    // Get all reviews for an event
    @Query("SELECT * FROM reviews WHERE event_id = :eventId ORDER BY created_at DESC")
    LiveData<List<Review>> getReviewsByEvent(int eventId);

    // Get reviews by user
    @Query("SELECT * FROM reviews WHERE user_id = :userId ORDER BY created_at DESC")
    LiveData<List<Review>> getReviewsByUser(int userId);

    // Get a specific review
    @Query("SELECT * FROM reviews WHERE event_id = :eventId AND user_id = :userId")
    LiveData<Review> getReviewByUserAndEvent(int eventId, int userId);

    // Check if user has reviewed an event
    @Query("SELECT * FROM reviews WHERE event_id = :eventId AND user_id = :userId LIMIT 1")
    Review hasUserReviewed(int eventId, int userId);

    // Get average rating for an event
    @Query("SELECT AVG(rating) FROM reviews WHERE event_id = :eventId")
    LiveData<Float> getAverageRating(int eventId);

    // Get review count for an event
    @Query("SELECT COUNT(*) FROM reviews WHERE event_id = :eventId")
    LiveData<Integer> getReviewCount(int eventId);

    // Get reviews with specific rating
    @Query("SELECT * FROM reviews WHERE event_id = :eventId AND rating = :rating ORDER BY created_at DESC")
    LiveData<List<Review>> getReviewsByRating(int eventId, float rating);

    // Delete review by id
    @Query("DELETE FROM reviews WHERE id = :reviewId")
    void deleteById(int reviewId);
}
