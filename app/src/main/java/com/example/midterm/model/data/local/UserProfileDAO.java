package com.example.midterm.model.data.local;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.midterm.model.entity.UserProfile;


@Dao
public interface UserProfileDAO {
    @Insert
    void insert(UserProfile userProfile);

    @Update
    int update(UserProfile userProfile);

    @Delete
    void delete(UserProfile userProfile);

    @Query("SELECT * FROM user_profile WHERE user_id = :userId LIMIT 1")
    UserProfile getUserById(int userId);

    @Query("DELETE FROM user_profile WHERE user_id = :userId")
    void deleteByUserId(int userId);
}
