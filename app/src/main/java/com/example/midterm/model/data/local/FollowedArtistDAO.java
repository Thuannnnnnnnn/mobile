package com.example.midterm.model.data.local;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.midterm.model.entity.FollowedArtist;
import com.example.midterm.model.entity.Guest;

import java.util.List;

@Dao
public interface FollowedArtistDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(FollowedArtist followedArtist);

    @Update
    void update(FollowedArtist followedArtist);

    @Delete
    void delete(FollowedArtist followedArtist);

    // Get all followed artists by user
    @Query("SELECT * FROM followed_artists WHERE user_id = :userId ORDER BY followed_at DESC")
    LiveData<List<FollowedArtist>> getFollowedArtistsByUser(int userId);

    // Get followed artist details
    @Query("SELECT g.* FROM guest g " +
           "INNER JOIN followed_artists fa ON g.guestId = fa.artist_id " +
           "WHERE fa.user_id = :userId ORDER BY fa.followed_at DESC")
    LiveData<List<Guest>> getFollowedArtistDetails(int userId);

    // Check if user follows an artist
    @Query("SELECT * FROM followed_artists WHERE user_id = :userId AND artist_id = :artistId LIMIT 1")
    FollowedArtist isFollowing(int userId, int artistId);

    // Unfollow artist
    @Query("DELETE FROM followed_artists WHERE user_id = :userId AND artist_id = :artistId")
    void unfollow(int userId, int artistId);

    // Get follower count for an artist
    @Query("SELECT COUNT(*) FROM followed_artists WHERE artist_id = :artistId")
    LiveData<Integer> getFollowerCount(int artistId);

    // Toggle notifications for followed artist
    @Query("UPDATE followed_artists SET notifications_enabled = :enabled WHERE user_id = :userId AND artist_id = :artistId")
    void toggleNotifications(int userId, int artistId, boolean enabled);

    // Get users who follow an artist (for notifications)
    @Query("SELECT user_id FROM followed_artists WHERE artist_id = :artistId AND notifications_enabled = 1")
    List<Integer> getUsersFollowingArtist(int artistId);
}
