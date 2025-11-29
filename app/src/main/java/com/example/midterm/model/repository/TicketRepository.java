package com.example.midterm.model.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.midterm.model.data.local.AppDatabase;
import com.example.midterm.model.data.local.TicketDAO;
import com.example.midterm.model.dto.TicketWithDetails;
import com.example.midterm.model.entity.Ticket;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TicketRepository {
    private final TicketDAO ticketDAO;
    private final ExecutorService executorService;
    private final LiveData<List<Ticket>> allTickets;
    public TicketRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        ticketDAO = db.ticketDAO();
        allTickets = ticketDAO.getAllTickets();
        executorService = Executors.newSingleThreadExecutor();
    }
    public LiveData<List<Ticket>> getAllTickets() {
        return allTickets;
    }

    public void insert(Ticket ticket) {
        executorService.execute(() -> {
            ticketDAO.insert(ticket);
        });
    }
    // Phương thức chèn NHIỀU vé
    public void insertAll(List<Ticket> tickets) {
        executorService.execute(() -> {
            ticketDAO.insertAll(tickets);
        });
    }

    // ===== SALES STATISTICS METHODS =====

    public LiveData<Ticket> getTicketById(int ticketId) {
        return ticketDAO.getTicketById(ticketId);
    }

    public LiveData<List<Ticket>> getTicketsByBuyer(int buyerId) {
        return ticketDAO.getTicketsByBuyer(buyerId);
    }

    public LiveData<List<Ticket>> getTicketsByEvent(int eventId) {
        return ticketDAO.getTicketsByEvent(eventId);
    }

    public LiveData<Integer> countTicketsSoldForEvent(int eventId) {
        return ticketDAO.countTicketsSoldForEvent(eventId);
    }

    public LiveData<Integer> countCheckedInForEvent(int eventId) {
        return ticketDAO.countCheckedInForEvent(eventId);
    }

    public LiveData<Integer> countCancelledForEvent(int eventId) {
        return ticketDAO.countCancelledForEvent(eventId);
    }

    public LiveData<List<Ticket>> getTicketsByEventAndStatus(int eventId, String status) {
        return ticketDAO.getTicketsByEventAndStatus(eventId, status);
    }

    public LiveData<List<Ticket>> getRecentSalesForEvent(int eventId) {
        return ticketDAO.getRecentSalesForEvent(eventId);
    }

    public LiveData<List<TicketDAO.DailySalesCount>> getDailySalesForEvent(int eventId) {
        return ticketDAO.getDailySalesForEvent(eventId);
    }

    public LiveData<List<Ticket>> getCancelledTicketsForEvent(int eventId) {
        return ticketDAO.getCancelledTicketsForEvent(eventId);
    }

    public void updateTicketStatus(int ticketId, String status, String checkInDate, String updatedAt) {
        executorService.execute(() -> ticketDAO.updateTicketStatus(ticketId, status, checkInDate, updatedAt));
    }

    public void checkInByQrCode(String qrCode, String checkInDate, String updatedAt, java.util.function.Consumer<Integer> callback) {
        executorService.execute(() -> {
            int result = ticketDAO.checkInByQrCode(qrCode, checkInDate, updatedAt);
            if (callback != null) callback.accept(result);
        });
    }

    public List<Ticket> getTicketsForExport(int eventId) {
        return ticketDAO.getTicketsForExport(eventId);
    }

    // ===== USER TICKET METHODS =====

    public LiveData<List<Ticket>> getUpcomingTicketsByUser(int userId) {
        return ticketDAO.getUpcomingTicketsByUser(userId);
    }

    public LiveData<List<Ticket>> getPastTicketsByUser(int userId) {
        return ticketDAO.getPastTicketsByUser(userId);
    }

    // ===== TICKET DETAILS METHODS =====

    public LiveData<List<TicketWithDetails>> getTicketsWithDetailsByEvent(int eventId) {
        return ticketDAO.getTicketsWithDetailsByEvent(eventId);
    }
}
