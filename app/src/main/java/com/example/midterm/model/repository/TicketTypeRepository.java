package com.example.midterm.model.repository;

import com.example.midterm.model.data.local.TicketDAO;
import com.example.midterm.model.data.local.TicketTypeDAO;
import com.example.midterm.model.entity.Ticket;
import com.example.midterm.model.entity.TicketType;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import android.app.Application;
import androidx.lifecycle.LiveData;
import com.example.midterm.model.data.local.AppDatabase;

import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TicketTypeRepository {
    private final TicketTypeDAO ticketTypeDAO;
    private final ExecutorService executorService;

    public TicketTypeRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        ticketTypeDAO = db.ticketTypeDAO();
        executorService = Executors.newSingleThreadExecutor();
    }

    public LiveData<List<TicketType>> getTicketsByEventId(int eventId) {
        return ticketTypeDAO.getTicketsByEventId(eventId);
    }

    public void insertTicket(TicketType ticketType) {
        executorService.execute(() -> ticketTypeDAO.insert(ticketType));
    }
    public void updateTicket(TicketType ticketType) {
        executorService.execute(() -> ticketTypeDAO.update(ticketType));
    }
    public void deleteTicket(TicketType ticketType) {
        executorService.execute(() -> ticketTypeDAO.delete(ticketType));
    }
    //Xóa ticket của eventID cụ thể
    public void deleteTicketsByEventId(long eventId) {
        executorService.execute(() -> ticketTypeDAO.deleteTicketsByEventId(eventId));
    }
}


