package com.example.midterm.model.data.local;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.midterm.model.dto.TicketWithDetails;
import com.example.midterm.model.entity.Ticket;

import java.util.List;

@Dao
public interface TicketDAO {
    @Insert
    long insert(Ticket ticket);

    @Insert
    void insertAll(List<Ticket> tickets);

    @Update
    void update(Ticket ticket);

    @Delete
    void delete(Ticket ticket);

    @Query("SELECT * FROM tickets")
    LiveData<List<Ticket>> getAllTickets();

    @Query("SELECT * FROM tickets WHERE ticket_type_id = :ticketTypeId")
    LiveData<List<Ticket>> getTicketsByTicketType(int ticketTypeId);

    // ===== SALES STATISTICS QUERIES =====

    // Get ticket by ID
    @Query("SELECT * FROM tickets WHERE id = :ticketId")
    LiveData<Ticket> getTicketById(int ticketId);

    // Get ticket by QR code
    @Query("SELECT * FROM tickets WHERE qr_code = :qrCode")
    Ticket getTicketByQrCode(String qrCode);

    // Get tickets by buyer ID
    @Query("SELECT * FROM tickets WHERE buyer_id = :buyerId ORDER BY purchase_date DESC")
    LiveData<List<Ticket>> getTicketsByBuyer(int buyerId);

    // Get tickets for an event (through ticket_type)
    @Query("SELECT t.* FROM tickets t " +
           "INNER JOIN ticket_types tt ON t.ticket_type_id = tt.id " +
           "WHERE tt.event_id = :eventId ORDER BY t.purchase_date DESC")
    LiveData<List<Ticket>> getTicketsByEvent(int eventId);

    // Count total tickets sold for an event
    @Query("SELECT COUNT(*) FROM tickets t " +
           "INNER JOIN ticket_types tt ON t.ticket_type_id = tt.id " +
           "WHERE tt.event_id = :eventId")
    LiveData<Integer> countTicketsSoldForEvent(int eventId);

    // Count checked-in tickets for an event
    @Query("SELECT COUNT(*) FROM tickets t " +
           "INNER JOIN ticket_types tt ON t.ticket_type_id = tt.id " +
           "WHERE tt.event_id = :eventId AND t.status = 'checked_in'")
    LiveData<Integer> countCheckedInForEvent(int eventId);

    // Count cancelled tickets for an event
    @Query("SELECT COUNT(*) FROM tickets t " +
           "INNER JOIN ticket_types tt ON t.ticket_type_id = tt.id " +
           "WHERE tt.event_id = :eventId AND t.status = 'cancelled'")
    LiveData<Integer> countCancelledForEvent(int eventId);

    // Get tickets by status for an event
    @Query("SELECT t.* FROM tickets t " +
           "INNER JOIN ticket_types tt ON t.ticket_type_id = tt.id " +
           "WHERE tt.event_id = :eventId AND t.status = :status")
    LiveData<List<Ticket>> getTicketsByEventAndStatus(int eventId, String status);

    // Get recent sales for an event (last 7 days)
    @Query("SELECT t.* FROM tickets t " +
           "INNER JOIN ticket_types tt ON t.ticket_type_id = tt.id " +
           "WHERE tt.event_id = :eventId AND t.purchase_date >= datetime('now', '-7 days') " +
           "ORDER BY t.purchase_date DESC")
    LiveData<List<Ticket>> getRecentSalesForEvent(int eventId);

    // Count tickets sold per day for an event (for charts)
    @Query("SELECT DATE(t.purchase_date) as date, COUNT(*) as count FROM tickets t " +
           "INNER JOIN ticket_types tt ON t.ticket_type_id = tt.id " +
           "WHERE tt.event_id = :eventId " +
           "GROUP BY DATE(t.purchase_date) ORDER BY date ASC")
    LiveData<List<DailySalesCount>> getDailySalesForEvent(int eventId);

    // Get cancelled tickets for an event
    @Query("SELECT t.* FROM tickets t " +
           "INNER JOIN ticket_types tt ON t.ticket_type_id = tt.id " +
           "WHERE tt.event_id = :eventId AND t.status = 'cancelled'")
    LiveData<List<Ticket>> getCancelledTicketsForEvent(int eventId);

    // Update ticket status
    @Query("UPDATE tickets SET status = :status, checkin_date = :checkInDate, updated_at = :updatedAt WHERE id = :ticketId")
    void updateTicketStatus(int ticketId, String status, String checkInDate, String updatedAt);

    // Check-in ticket by QR code
    @Query("UPDATE tickets SET status = 'checked_in', checkin_date = :checkInDate, updated_at = :updatedAt WHERE qr_code = :qrCode")
    int checkInByQrCode(String qrCode, String checkInDate, String updatedAt);

    // Get all tickets for export (by event)
    @Query("SELECT t.* FROM tickets t " +
           "INNER JOIN ticket_types tt ON t.ticket_type_id = tt.id " +
           "WHERE tt.event_id = :eventId")
    List<Ticket> getTicketsForExport(int eventId);

    // Inner class for daily sales count
    class DailySalesCount {
        public String date;
        public int count;
    }

    // ===== USER TICKET QUERIES =====

    // Get upcoming tickets for user (events that haven't ended yet)
    @Query("SELECT t.* FROM tickets t " +
           "INNER JOIN ticket_types tt ON t.ticket_type_id = tt.id " +
           "INNER JOIN events e ON tt.event_id = e.id " +
           "WHERE t.buyer_id = :userId AND e.end_date >= datetime('now', 'localtime') " +
           "ORDER BY e.start_date ASC")
    LiveData<List<Ticket>> getUpcomingTicketsByUser(int userId);

    // Get past tickets for user (events that have ended)
    @Query("SELECT t.* FROM tickets t " +
           "INNER JOIN ticket_types tt ON t.ticket_type_id = tt.id " +
           "INNER JOIN events e ON tt.event_id = e.id " +
           "WHERE t.buyer_id = :userId AND e.end_date < datetime('now', 'localtime') " +
           "ORDER BY e.end_date DESC")
    LiveData<List<Ticket>> getPastTicketsByUser(int userId);

    // ===== TICKET DETAILS QUERIES =====

    // Get tickets with full details (buyer info, ticket type) for an event
    @Query("SELECT t.id as ticket_id, t.qr_code, t.purchase_date, t.checkin_date, t.status, " +
           "COALESCE(up.full_name, a.email) as buyer_name, a.email as buyer_email, " +
           "tt.code as ticket_type_code, tt.price as ticket_price " +
           "FROM tickets t " +
           "INNER JOIN ticket_types tt ON t.ticket_type_id = tt.id " +
           "INNER JOIN accounts a ON t.buyer_id = a.id " +
           "LEFT JOIN user_profile up ON a.id = up.user_id " +
           "WHERE tt.event_id = :eventId " +
           "ORDER BY t.purchase_date DESC")
    LiveData<List<TicketWithDetails>> getTicketsWithDetailsByEvent(int eventId);

    // Get unique attendee count for an event (for notifications)
    @Query("SELECT COUNT(DISTINCT t.buyer_id) FROM tickets t " +
            "INNER JOIN ticket_types tt ON t.ticket_type_id = tt.id " +
            "WHERE tt.event_id = :eventId")
    int getUniqueAttendeeCountSync(int eventId);

    @Query("SELECT * FROM tickets WHERE qr_code = :qrCode LIMIT 1")
    Ticket getTicketByQr(String qrCode);

    @Update
    void updateTicket(Ticket ticket);
}
