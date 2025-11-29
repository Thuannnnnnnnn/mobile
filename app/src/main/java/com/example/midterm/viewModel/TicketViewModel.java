package com.example.midterm.viewModel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.midterm.model.data.local.TicketDAO;
import com.example.midterm.model.dto.TicketWithDetails;
import com.example.midterm.model.entity.Ticket;
import com.example.midterm.model.repository.TicketRepository;

import java.util.List;
import java.util.function.Consumer;

public class TicketViewModel extends AndroidViewModel {
    private final TicketRepository repository;
    private final LiveData<List<Ticket>> allTickets;

    public TicketViewModel(@NonNull Application application) {
        super(application);
        repository = new TicketRepository(application);
        allTickets = repository.getAllTickets();
    }

    public LiveData<List<Ticket>> getAllTickets() {
        return allTickets;
    }

    public void insert(Ticket ticket) {
        repository.insert(ticket);
    }

    public void insertAll(List<Ticket> tickets) {
        repository.insertAll(tickets);
    }

    // ===== SALES STATISTICS METHODS =====

    public LiveData<Ticket> getTicketById(int ticketId) {
        return repository.getTicketById(ticketId);
    }

    public LiveData<List<Ticket>> getTicketsByBuyer(int buyerId) {
        return repository.getTicketsByBuyer(buyerId);
    }

    public LiveData<List<Ticket>> getTicketsByEvent(int eventId) {
        return repository.getTicketsByEvent(eventId);
    }

    public LiveData<Integer> countTicketsSoldForEvent(int eventId) {
        return repository.countTicketsSoldForEvent(eventId);
    }

    public LiveData<Integer> countCheckedInForEvent(int eventId) {
        return repository.countCheckedInForEvent(eventId);
    }

    public LiveData<Integer> countCancelledForEvent(int eventId) {
        return repository.countCancelledForEvent(eventId);
    }

    public LiveData<List<Ticket>> getTicketsByEventAndStatus(int eventId, String status) {
        return repository.getTicketsByEventAndStatus(eventId, status);
    }

    public LiveData<List<Ticket>> getRecentSalesForEvent(int eventId) {
        return repository.getRecentSalesForEvent(eventId);
    }

    public LiveData<List<TicketDAO.DailySalesCount>> getDailySalesForEvent(int eventId) {
        return repository.getDailySalesForEvent(eventId);
    }

    public LiveData<List<Ticket>> getCancelledTicketsForEvent(int eventId) {
        return repository.getCancelledTicketsForEvent(eventId);
    }

    public void updateTicketStatus(int ticketId, String status, String checkInDate, String updatedAt) {
        repository.updateTicketStatus(ticketId, status, checkInDate, updatedAt);
    }

    public void checkInByQrCode(String qrCode, String checkInDate, String updatedAt, Consumer<Integer> callback) {
        repository.checkInByQrCode(qrCode, checkInDate, updatedAt, callback);
    }

    // Synchronous check-in method for QR scanner
    public boolean checkInTicketByQrCode(String qrCode, String checkInDate) {
        final boolean[] success = {false};
        final Object lock = new Object();

        checkInByQrCode(qrCode, checkInDate, checkInDate, result -> {
            synchronized (lock) {
                success[0] = result > 0;
                lock.notify();
            }
        });

        synchronized (lock) {
            try {
                lock.wait(5000); // Wait max 5 seconds
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return success[0];
    }

    // ===== USER TICKET METHODS =====

    public LiveData<List<Ticket>> getTicketsByUser(int userId) {
        return repository.getTicketsByBuyer(userId);
    }

    public LiveData<List<Ticket>> getUpcomingTicketsByUser(int userId) {
        return repository.getUpcomingTicketsByUser(userId);
    }

    public LiveData<List<Ticket>> getPastTicketsByUser(int userId) {
        return repository.getPastTicketsByUser(userId);
    }

    // ===== TICKET DETAILS METHODS =====

    public LiveData<List<TicketWithDetails>> getTicketsWithDetailsByEvent(int eventId) {
        return repository.getTicketsWithDetailsByEvent(eventId);
    }
}
