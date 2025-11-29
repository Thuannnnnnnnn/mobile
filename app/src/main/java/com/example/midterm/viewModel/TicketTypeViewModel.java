package com.example.midterm.viewModel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.midterm.model.entity.TicketType;
import com.example.midterm.model.repository.TicketTypeRepository;

import java.util.List;

public class TicketTypeViewModel extends AndroidViewModel {
    private final TicketTypeRepository ticketTypeRepository;

    public TicketTypeViewModel(@NonNull Application application) {
        super(application);
        ticketTypeRepository = new TicketTypeRepository(application);
    }

    public LiveData<List<TicketType>> getTicketsByEventId(int eventId) {
        return ticketTypeRepository.getTicketsByEventId(eventId);
    }

    public void insertTicket(TicketType ticketType) {
        ticketTypeRepository.insertTicket(ticketType);
    }

    public void updateTicket(TicketType ticketType) {
        ticketTypeRepository.updateTicket(ticketType);
    }

    public void deleteTicket(TicketType ticketType) {
        ticketTypeRepository.deleteTicket(ticketType);
    }

    public void deleteTicketsByEventId(long eventId) {
        ticketTypeRepository.deleteTicketsByEventId(eventId);
    }
}

