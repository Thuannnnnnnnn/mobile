package com.example.midterm.model.data.local;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.midterm.model.entity.Seat;

import java.util.List;

@Dao
public interface SeatDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Seat seat);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Seat> seats);

    @Query("SELECT * FROM seats WHERE section_id = :sectionId ORDER BY seat_row ASC, seat_number ASC")
    LiveData<List<Seat>> getSeatsBySectionId(long sectionId);

    @Query("DELETE FROM seats WHERE section_id IN (SELECT section_id FROM event_sections WHERE event_id = :eventId)")
    void deleteSeatsByEventId(long eventId);

    // ===== USER SEAT SELECTION QUERIES =====

    /**
     * Get all seats for an event (through sections)
     */
    @Query("SELECT s.* FROM seats s " +
           "INNER JOIN event_sections es ON s.section_id = es.section_id " +
           "WHERE es.event_id = :eventId ORDER BY es.name, s.seat_row, s.seat_number")
    LiveData<List<Seat>> getSeatsByEventId(int eventId);

    @Query("SELECT * FROM seats WHERE ticket_type_id = :ticketTypeId AND status = 'available' " +
           "ORDER BY seat_row ASC, seat_number ASC")
    LiveData<List<Seat>> getAvailableSeatsByTicketType(int ticketTypeId);


    @Query("SELECT * FROM seats WHERE section_id = :sectionId ORDER BY seat_row ASC, seat_number ASC")
    List<Seat> getSeatsBySectionIdSync(long sectionId);


    @Query("UPDATE seats SET status = :status, updated_at = :updatedAt WHERE id = :seatId")
    void updateSeatStatus(long seatId, String status, String updatedAt);


    @Query("UPDATE seats SET status = 'booked', updated_at = :updatedAt WHERE id IN (:seatIds)")
    void bookSeats(List<Long> seatIds, String updatedAt);


    @Query("SELECT * FROM seats WHERE id = :seatId")
    Seat getSeatById(long seatId);

    @Query("SELECT COUNT(*) FROM seats WHERE ticket_type_id = :ticketTypeId AND status = 'available'")
    int countAvailableSeats(int ticketTypeId);

    @Query("DELETE FROM seats WHERE ticket_type_id = :ticketTypeId")
    void deleteByTicketTypeId(int ticketTypeId);
}