package com.example.midterm.model.data.local;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.example.midterm.model.entity.Guest;
import com.example.midterm.model.entity.relations.EventGuestCrossRef;
import com.example.midterm.model.entity.relations.GuestWithEvents;

import java.util.List;

@Dao
public interface GuestDAO {
    @Insert
    long insert(Guest guest);

    @Update
    void update(Guest guest);

    @Delete
    void delete(Guest guest);


    // --- Truy vấn mới để lấy Guests cho một Event cụ thể ---
    @Query("SELECT G.* FROM guest AS G " +
            "INNER JOIN event_guest_cross_ref AS Ref ON G.guestId = Ref.guestId " +
            "WHERE Ref.eventId = :eventId")
    LiveData<List<Guest>> getGuestsForEvent(int eventId);

    // Xóa tất cả các liên kết trong bảng trung gian
    @Query("DELETE FROM event_guest_cross_ref WHERE guestId = :guestId")
    void deleteGuestCrossRefs(int guestId);

    @Query("DELETE FROM guest WHERE guestId IN (SELECT guestId FROM event_guest_cross_ref WHERE eventId = :eventId)")
    void deleteGuestsByEventId(long eventId);

    @Query("DELETE FROM event_guest_cross_ref WHERE eventId = :eventId")
    void deleteCrossRefsByEventId(long eventId);

    // Xóa Guest và các liên kết
    @Transaction
    default void deleteGuestAndCrossRefs(Guest guest) {
        deleteGuestCrossRefs(guest.getGuestId());
        delete(guest);
    }

    @Transaction
    default void deleteGuestsAndCrossRefsByEventId(long eventId) {
        deleteGuestsByEventId(eventId);
        deleteCrossRefsByEventId(eventId);
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertEventGuestCrossRef(EventGuestCrossRef crossRef);
}