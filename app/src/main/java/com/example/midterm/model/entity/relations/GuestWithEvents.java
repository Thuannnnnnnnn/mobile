package com.example.midterm.model.entity.relations;

import androidx.room.Embedded;
import androidx.room.Junction;
import androidx.room.Relation;

import com.example.midterm.model.entity.Event;
import com.example.midterm.model.entity.Guest;

import java.util.List;

public class GuestWithEvents {
    @Embedded
    public Guest guest;

    @Relation(
            parentColumn = "guestId", // khóa chính trong Guest
            entityColumn = "id",      // khóa chính trong Event
            associateBy = @Junction(
                    value = EventGuestCrossRef.class,
                    parentColumn = "guestId",
                    entityColumn = "eventId"
            )
    )
    public List<Event> events;
}
