package com.example.midterm.model.data.local;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.midterm.model.entity.EventSection;

import java.util.List;

@Dao
public interface EventSectionDAO {
    @Insert
    long insert(EventSection eventSection);

    @Update
    void update(EventSection eventSection);

    @Delete
    void delete(EventSection eventSection);

    @Query("SELECT * FROM event_sections WHERE event_id = :eventId ORDER BY display_order ASC")
    LiveData<List<EventSection>> getSectionsByEventId(int eventId);

    @Query("DELETE FROM event_sections WHERE event_id = :eventId")
    void deleteSectionsByEventId(long eventId);

    @Query("SELECT * FROM event_sections WHERE event_id = :eventId ORDER BY display_order ASC")
    List<EventSection> getEventSectionsByEventIdSync(int eventId);
}
