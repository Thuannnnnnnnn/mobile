package com.example.midterm.model.entity.relations;

import androidx.room.Embedded;
import androidx.room.Junction;
import androidx.room.Relation;

import com.example.midterm.model.entity.Event;
import com.example.midterm.model.entity.Guest;

import java.util.List;

public class EventWithGuests {
    @Embedded
    public Event event;

    @Relation(
            parentColumn = "id", // cột khóa chính trong Event
            entityColumn = "guestId", // cột khóa chính trong Guest
            associateBy = @Junction(
                    value = EventGuestCrossRef.class,
                    parentColumn = "eventId", // cột bên junction tương ứng với Event
                    entityColumn = "guestId"  // cột bên junction tương ứng với Guest
            )
    )
    public List<Guest> guests;
}
