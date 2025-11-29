package com.example.midterm.model.data.local;

import com.example.midterm.model.entity.TicketType;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Update;
import androidx.room.Delete;
import androidx.room.Query;

@Dao
public interface TicketTypeDAO {

    @Insert
    void insert(TicketType ticketType);

    @Update
    void update(TicketType ticketType);

    @Delete
    void delete(TicketType ticketType);

    @Query("SELECT * FROM ticket_types WHERE event_id = :eventId ORDER BY created_at DESC")
    LiveData<List<TicketType>> getTicketsByEventId(int eventId);

    @Query("DELETE FROM ticket_types WHERE event_id = :eventId")
    void deleteTicketsByEventId(long eventId);

    @Query("SELECT * FROM ticket_types WHERE id = :id LIMIT 1")
    LiveData<TicketType> getTicketTypeById(int id);

    // Increment sold quantity when tickets are purchased
    @Query("UPDATE ticket_types SET sold_quantity = sold_quantity + :quantity WHERE id = :ticketTypeId")
    void incrementSoldQuantity(int ticketTypeId, int quantity);

    // Get available quantity
    @Query("SELECT (quantity - sold_quantity) FROM ticket_types WHERE id = :ticketTypeId")
    int getAvailableQuantity(int ticketTypeId);

    // Get ticket type by id (sync)
    @Query("SELECT * FROM ticket_types WHERE id = :id LIMIT 1")
    TicketType getTicketTypeByIdSync(int id);
}
