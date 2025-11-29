package com.example.midterm.model.data.local;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Update;
import androidx.room.Delete;
import androidx.room.Query;

import com.example.midterm.model.entity.Organizer;


@Dao
public interface OrganizerDAO {

    @Insert
    void insert(Organizer organizer);

    @Update
    void update(Organizer organizer);

    @Delete
    void delete(Organizer organizer);

    @Query("SELECT * FROM organizers WHERE organizerId = :accountId LIMIT 1")
    Organizer getOrganizerByAccountId(int accountId);

    @Query("SELECT * FROM organizers WHERE organizerId = :accountId LIMIT 1")
    LiveData<Organizer> observeOrganizerByAccountId(int accountId);
}
