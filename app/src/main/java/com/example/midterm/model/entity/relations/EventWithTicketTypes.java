package com.example.midterm.model.entity.relations;

import androidx.room.Embedded;
import androidx.room.Relation;

import com.example.midterm.model.entity.Event;
import com.example.midterm.model.entity.TicketType;

import java.util.List;

public class EventWithTicketTypes {
    @Embedded
    private Event event;

    @Relation(
            parentColumn = "id",
            entityColumn = "event_id"
    )
    private List<TicketType> tickets;

    public Event getEvent() { return event; }
    public void setEvent(Event event) { this.event = event; }

    public List<TicketType> getTickets() { return tickets; }
    public void setTickets(List<TicketType> tickets) { this.tickets = tickets; }
}
